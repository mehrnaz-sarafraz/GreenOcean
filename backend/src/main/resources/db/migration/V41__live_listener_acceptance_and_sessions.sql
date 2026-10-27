-- Model A: preserve historical chats/roles; no seeded human availability or country enablement.
CREATE TABLE listener_live_authorizations (
 application_id UUID PRIMARY KEY REFERENCES listener_applications(id) ON DELETE CASCADE,
 listener_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 reviewed_by UUID NOT NULL REFERENCES users(id),
 practice_reviewed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
 conduct_version INTEGER NOT NULL CHECK(conduct_version>0),
 revoked_at TIMESTAMPTZ,
 CHECK(listener_id<>reviewed_by)
);
CREATE INDEX idx_live_authorization_listener ON listener_live_authorizations(listener_id);
CREATE TABLE listener_presence (
 listener_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 session_id UUID NOT NULL REFERENCES user_sessions(id) ON DELETE CASCADE,
 token_version INTEGER NOT NULL,
 lease_id UUID NOT NULL,
 ready BOOLEAN NOT NULL DEFAULT FALSE,
 expires_at TIMESTAMPTZ NOT NULL,
 last_assigned_at TIMESTAMPTZ
);
CREATE TABLE live_support_requests (
 id UUID PRIMARY KEY DEFAULT uuidv7(),
 member_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 listener_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 client_request_id UUID NOT NULL,
 status VARCHAR(24) NOT NULL DEFAULT 'REQUESTED' CHECK(status IN
 ('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED','DECLINED','CANCELLED','EXPIRED','COMPLETED','ENDED_SAFETY')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
 expires_at TIMESTAMPTZ NOT NULL,
 accepted_at TIMESTAMPTZ,
 activation_expires_at TIMESTAMPTZ,
 activated_at TIMESTAMPTZ,
 member_seen_at TIMESTAMPTZ,
 listener_seen_at TIMESTAMPTZ,
 interrupted_at TIMESTAMPTZ,
 ended_at TIMESTAMPTZ,
 reason_code VARCHAR(40),
 conversation_id UUID UNIQUE REFERENCES conversations(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED,
 presence_seconds INTEGER NOT NULL CHECK(presence_seconds BETWEEN 30 AND 120),
 reconnect_seconds INTEGER NOT NULL CHECK(reconnect_seconds BETWEEN 30 AND 600),
 CHECK(member_id<>listener_id),
 CHECK(status NOT IN ('ACCEPTED','ACTIVE','INTERRUPTED') OR (accepted_at IS NOT NULL AND activation_expires_at IS NOT NULL)),
 CHECK(status NOT IN ('ACTIVE','INTERRUPTED') OR (conversation_id IS NOT NULL AND activated_at IS NOT NULL AND member_seen_at IS NOT NULL AND listener_seen_at IS NOT NULL)),
 UNIQUE(member_id,client_request_id)
);
CREATE UNIQUE INDEX uq_live_member_capacity ON live_support_requests(member_id)
 WHERE status IN ('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED');
CREATE UNIQUE INDEX uq_live_listener_capacity ON live_support_requests(listener_id)
 WHERE status IN ('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED');
CREATE INDEX idx_live_pending_deadline ON live_support_requests(expires_at) WHERE status='REQUESTED';
CREATE INDEX idx_live_listener_incoming ON live_support_requests(listener_id,created_at DESC);
CREATE INDEX idx_live_member_history ON live_support_requests(member_id,created_at DESC);
ALTER TABLE messages ADD COLUMN client_message_id UUID;
CREATE UNIQUE INDEX uq_live_message_retry ON messages(conversation_id,sender_id,client_message_id) WHERE client_message_id IS NOT NULL;

CREATE FUNCTION live_account_eligible(who UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM users u JOIN account_eligibility e ON e.user_id=u.id
 WHERE u.id=who AND u.status='ACTIVE' AND u.email_verified AND e.age_result='ADULT'
 AND EXISTS(SELECT 1 FROM regional_services r WHERE r.country_code=e.residence_country AND r.service='LISTENER' AND r.status='AVAILABLE')
 AND (e.current_country IS NULL OR EXISTS(SELECT 1 FROM regional_services r WHERE r.country_code=e.current_country AND r.service='LISTENER' AND r.status='AVAILABLE')))
$$;
CREATE FUNCTION live_listener_qualified(who UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT live_account_eligible(who) AND EXISTS(
 SELECT 1 FROM listener_profiles lp JOIN listener_live_authorizations a ON a.listener_id=lp.user_id
 JOIN listener_applications application ON application.id=a.application_id AND application.user_id=lp.user_id
 WHERE lp.user_id=who AND lp.status='ACTIVE' AND application.status='APPROVED' AND application.accepted_code
 AND a.revoked_at IS NULL AND application.reviewed_by IS NOT NULL AND application.reviewed_at IS NOT NULL
 AND EXISTS(SELECT 1 FROM users reviewer JOIN user_roles ur ON ur.user_id=reviewer.id JOIN roles role ON role.id=ur.role_id
   WHERE reviewer.id=a.reviewed_by AND reviewer.status='ACTIVE' AND role.name IN ('ADMIN','MODERATOR'))
 AND a.conduct_version=(SELECT MAX(version) FROM policy_documents WHERE policy_key='listener-code' AND status='PUBLISHED' AND published_at<=CURRENT_TIMESTAMP AND content IS NOT NULL)
 AND EXISTS(SELECT 1 FROM listener_training_modules WHERE is_active)
 AND NOT EXISTS(SELECT 1 FROM listener_training_modules module WHERE module.is_active AND NOT EXISTS(
 SELECT 1 FROM listener_training_progress progress WHERE progress.user_id=who AND progress.module_key=module.module_key)))
$$;
CREATE FUNCTION live_listener_ready(who UUID) RETURNS BOOLEAN LANGUAGE SQL VOLATILE AS $$
 SELECT live_listener_qualified(who) AND EXISTS(SELECT 1 FROM listener_presence p JOIN user_sessions s ON s.id=p.session_id
 WHERE p.listener_id=who AND s.user_id=who AND p.ready AND p.expires_at>clock_timestamp()
 AND s.revoked_at IS NULL AND s.token_version=p.token_version AND s.session_scope<>'PENDING'
 AND s.expires_at>clock_timestamp() AND s.overall_expires_at>clock_timestamp() AND s.idle_expires_at>clock_timestamp())
$$;
CREATE FUNCTION live_pair_blocked(member UUID,listener UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM blocks WHERE (blocker_id=member AND blocked_id=listener) OR(blocker_id=listener AND blocked_id=member))
$$;
-- All live transitions and safety invalidations take this transaction lock. It is DB-wide, not process-local.
CREATE FUNCTION invalidate_live_support() RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
 PERFORM pg_advisory_xact_lock(72491001);
 UPDATE live_support_requests SET status='ENDED_SAFETY',reason_code='AUTHORIZATION_CHANGED',ended_at=clock_timestamp()
 WHERE status IN ('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED') AND
 (NOT live_account_eligible(member_id) OR NOT live_listener_qualified(listener_id) OR live_pair_blocked(member_id,listener_id));
 DELETE FROM listener_presence WHERE NOT live_listener_qualified(listener_id);
END $$;
CREATE FUNCTION expire_live_support() RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
 PERFORM pg_advisory_xact_lock(72491001);
 PERFORM invalidate_live_support();
 UPDATE live_support_requests SET status='EXPIRED',reason_code='ACCEPTANCE_TIMEOUT',ended_at=clock_timestamp()
 WHERE id IN (SELECT id FROM live_support_requests WHERE status='REQUESTED' AND expires_at<=clock_timestamp() LIMIT 500);
 UPDATE live_support_requests SET status='EXPIRED',reason_code='ACTIVATION_TIMEOUT',ended_at=clock_timestamp()
 WHERE id IN (SELECT id FROM live_support_requests WHERE status='ACCEPTED' AND activation_expires_at<=clock_timestamp() LIMIT 500);
 UPDATE live_support_requests SET status=CASE WHEN status='REQUESTED' THEN 'DECLINED' ELSE 'CANCELLED' END,
 reason_code='LISTENER_UNAVAILABLE',ended_at=clock_timestamp()
 WHERE status IN ('REQUESTED','ACCEPTED') AND NOT live_listener_ready(listener_id);
 UPDATE live_support_requests SET status='INTERRUPTED',reason_code='CONNECTION_INTERRUPTED',
 interrupted_at=LEAST(member_seen_at,listener_seen_at)+(presence_seconds*INTERVAL '1 second')
 WHERE status='ACTIVE' AND LEAST(member_seen_at,listener_seen_at)+(presence_seconds*INTERVAL '1 second')<=clock_timestamp();
 UPDATE live_support_requests SET status='COMPLETED',reason_code='CONNECTION_TIMEOUT',ended_at=clock_timestamp()
 WHERE status='INTERRUPTED' AND interrupted_at+(reconnect_seconds*INTERVAL '1 second')<=clock_timestamp();
 DELETE FROM listener_presence WHERE expires_at<=clock_timestamp();
END $$;
CREATE FUNCTION live_safety_change() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 PERFORM invalidate_live_support();
 RETURN NULL;
END $$;
CREATE TRIGGER trg_live_block AFTER INSERT OR UPDATE ON blocks FOR EACH STATEMENT EXECUTE FUNCTION live_safety_change();
CREATE TRIGGER trg_live_user_status AFTER UPDATE OF status,email_verified ON users FOR EACH STATEMENT EXECUTE FUNCTION live_safety_change();
CREATE TRIGGER trg_live_region AFTER UPDATE OR DELETE ON regional_services FOR EACH STATEMENT EXECUTE FUNCTION live_safety_change();
CREATE TRIGGER trg_live_eligibility AFTER UPDATE OR DELETE ON account_eligibility FOR EACH STATEMENT EXECUTE FUNCTION live_safety_change();
CREATE TRIGGER trg_live_listener_status AFTER UPDATE OF status OR DELETE ON listener_profiles FOR EACH STATEMENT EXECUTE FUNCTION live_safety_change();
CREATE TRIGGER trg_live_authorization AFTER UPDATE OR DELETE ON listener_live_authorizations FOR EACH STATEMENT EXECUTE FUNCTION live_safety_change();
CREATE TRIGGER trg_live_application AFTER UPDATE OF status OR DELETE ON listener_applications FOR EACH STATEMENT EXECUTE FUNCTION live_safety_change();
CREATE TRIGGER trg_live_training AFTER DELETE ON listener_training_progress FOR EACH STATEMENT EXECUTE FUNCTION live_safety_change();
CREATE FUNCTION invalidate_listener_session_presence() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 PERFORM pg_advisory_xact_lock(72491001);
 DELETE FROM listener_presence WHERE session_id=NEW.id AND (NEW.revoked_at IS NOT NULL OR token_version<>NEW.token_version);
 RETURN NEW;
END $$;
CREATE TRIGGER trg_live_auth_session AFTER UPDATE OF revoked_at,token_version ON user_sessions FOR EACH ROW EXECUTE FUNCTION invalidate_listener_session_presence();

CREATE FUNCTION can_read_live_conversation(conversation UUID,viewer UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM live_support_requests r WHERE r.conversation_id=conversation
 AND viewer IN(r.member_id,r.listener_id) AND r.status IN ('ACTIVE','INTERRUPTED','COMPLETED')
 AND NOT live_pair_blocked(r.member_id,r.listener_id) AND live_account_eligible(viewer))
$$;
CREATE FUNCTION guard_live_message() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF EXISTS(SELECT 1 FROM conversations WHERE id=NEW.conversation_id AND service_kind='LISTENER') THEN
  PERFORM expire_live_support();
  IF NOT EXISTS(SELECT 1 FROM live_support_requests r WHERE r.conversation_id=NEW.conversation_id AND r.status='ACTIVE'
   AND NEW.sender_id IN(r.member_id,r.listener_id) AND live_account_eligible(r.member_id)
   AND live_listener_qualified(r.listener_id) AND NOT live_pair_blocked(r.member_id,r.listener_id))
  THEN RAISE EXCEPTION 'Live support session does not permit sending' USING ERRCODE='42501'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER trg_live_message BEFORE INSERT ON messages FOR EACH ROW WHEN (NEW.message_kind='USER') EXECUTE FUNCTION guard_live_message();
CREATE FUNCTION check_live_participants() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE conversation UUID; session_record live_support_requests%ROWTYPE;
BEGIN
 IF TG_TABLE_NAME='live_support_requests' THEN conversation=NEW.conversation_id;
 ELSE conversation=COALESCE(NEW.conversation_id,OLD.conversation_id); END IF;
 SELECT * INTO session_record FROM live_support_requests WHERE conversation_id=conversation;
 IF FOUND AND (NOT EXISTS(SELECT 1 FROM conversations WHERE id=conversation AND service_kind='LISTENER' AND conversation_kind='DIRECT')
 OR (SELECT COUNT(*) FROM conversation_members WHERE conversation_id=conversation)<>2
 OR EXISTS(SELECT 1 FROM conversation_members WHERE conversation_id=conversation AND user_id NOT IN(session_record.member_id,session_record.listener_id)))
 THEN RAISE EXCEPTION 'Live support participants are immutable' USING ERRCODE='23514'; END IF;
 RETURN NULL;
END $$;
CREATE CONSTRAINT TRIGGER trg_live_request_participants AFTER INSERT OR UPDATE ON live_support_requests
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_live_participants();
CREATE CONSTRAINT TRIGGER trg_live_conversation_participants AFTER INSERT OR UPDATE OR DELETE ON conversation_members
 DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION check_live_participants();

CREATE FUNCTION close_listener_readiness() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.status IN ('COMPLETED','ENDED_SAFETY') AND OLD.status IS DISTINCT FROM NEW.status THEN
 UPDATE listener_presence SET ready=FALSE,lease_id=uuidv7() WHERE listener_id=NEW.listener_id;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER trg_live_finish_readiness AFTER UPDATE OF status ON live_support_requests FOR EACH ROW EXECUTE FUNCTION close_listener_readiness();
