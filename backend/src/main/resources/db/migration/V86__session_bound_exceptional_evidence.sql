CREATE TABLE social_attribution_requests(
 id UUID PRIMARY KEY DEFAULT uuidv7(),assessment_id UUID NOT NULL REFERENCES social_assessments(id) ON DELETE CASCADE,revision BIGINT NOT NULL,
 requester_id UUID NOT NULL REFERENCES users(id),request_session UUID NOT NULL,request_version INTEGER NOT NULL,
 purpose TEXT NOT NULL CHECK(purpose IN('THREAT_INVESTIGATION','PRIVACY_COMPLAINT','ACCOUNT_SECURITY')),
 approved_by UUID REFERENCES users(id),approval_session UUID,approval_version INTEGER,
 expires_at TIMESTAMPTZ NOT NULL,revoked_at TIMESTAMPTZ,consumed_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(approved_by IS NULL OR approved_by<>requester_id),CHECK(expires_at>created_at AND expires_at<=created_at+INTERVAL '15 minutes')
);
CREATE INDEX social_attribution_review ON social_attribution_requests(expires_at) WHERE revoked_at IS NULL AND consumed_at IS NULL;
-- Session IDs are immutable historical approval bindings, not session-retention foreign keys.
-- Every use separately requires a current live session; deleting it must invalidate access, not block erasure.
ALTER TABLE clinic_record_access_requests ADD COLUMN request_session UUID;
ALTER TABLE clinic_record_access_requests ADD COLUMN request_version INTEGER;
ALTER TABLE clinic_record_access_requests ADD COLUMN policy_id UUID REFERENCES clinic_record_policies(id);
ALTER TABLE clinic_record_access_requests ADD COLUMN revision BIGINT NOT NULL DEFAULT 1;
ALTER TABLE clinic_record_access_requests ADD COLUMN revoked_at TIMESTAMPTZ;
ALTER TABLE clinic_record_access_grants ADD COLUMN approval_session UUID;
ALTER TABLE clinic_record_access_grants ADD COLUMN approval_version INTEGER;
-- Historical unbound grants remain evidence but are not silently promoted to a new session/policy approval.
CREATE FUNCTION protect_exceptional_request() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF to_jsonb(NEW)-ARRAY['revoked_at','revision'] IS DISTINCT FROM to_jsonb(OLD)-ARRAY['revoked_at','revision']
 OR (OLD.revoked_at IS NOT NULL AND NEW.revoked_at IS DISTINCT FROM OLD.revoked_at)
 THEN RAISE EXCEPTION 'Exceptional request scope is immutable; make a new request';END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER clinical_exceptional_request_scope BEFORE UPDATE ON clinic_record_access_requests FOR EACH ROW EXECUTE FUNCTION protect_exceptional_request();
CREATE FUNCTION protect_exceptional_grant() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF to_jsonb(NEW)-'revoked_at' IS DISTINCT FROM to_jsonb(OLD)-'revoked_at'
 OR (OLD.revoked_at IS NOT NULL AND NEW.revoked_at IS DISTINCT FROM OLD.revoked_at)
 THEN RAISE EXCEPTION 'Exceptional grant scope is immutable';END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER clinical_exceptional_grant_scope BEFORE UPDATE ON clinic_record_access_grants FOR EACH ROW EXECUTE FUNCTION protect_exceptional_grant();
CREATE FUNCTION protect_social_evidence_request() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF ROW(NEW.assessment_id,NEW.revision,NEW.requester_id,NEW.request_session,NEW.request_version,NEW.purpose,NEW.expires_at)
 IS DISTINCT FROM ROW(OLD.assessment_id,OLD.revision,OLD.requester_id,OLD.request_session,OLD.request_version,OLD.purpose,OLD.expires_at)
 OR (OLD.approved_by IS NOT NULL AND ROW(NEW.approved_by,NEW.approval_session,NEW.approval_version) IS DISTINCT FROM ROW(OLD.approved_by,OLD.approval_session,OLD.approval_version))
 OR (OLD.revoked_at IS NOT NULL AND NEW.revoked_at IS DISTINCT FROM OLD.revoked_at)
 OR (OLD.consumed_at IS NOT NULL AND NEW.consumed_at IS DISTINCT FROM OLD.consumed_at)
 THEN RAISE EXCEPTION 'Evidence scope and consumed or revoked approvals cannot be restored';END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER social_evidence_request_scope BEFORE UPDATE ON social_attribution_requests FOR EACH ROW EXECUTE FUNCTION protect_social_evidence_request();
-- Role re-grant must never resurrect an earlier authenticated session or exceptional grant.
CREATE FUNCTION revoke_sessions_after_role_removal() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 PERFORM id FROM users WHERE id=OLD.user_id FOR UPDATE;
 UPDATE user_sessions SET revoked_at=CURRENT_TIMESTAMP,token_version=token_version+1 WHERE user_id=OLD.user_id AND revoked_at IS NULL;
 UPDATE social_attribution_requests SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE requester_id=OLD.user_id OR approved_by=OLD.user_id;
 UPDATE clinic_record_access_requests SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP),revision=revision+1 WHERE operator_id=OLD.user_id;
 UPDATE clinic_record_access_grants SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE operator_id=OLD.user_id OR approved_by=OLD.user_id;
 UPDATE trust_coverage SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE operator_id=OLD.user_id OR approved_by=OLD.user_id;
 INSERT INTO audit_logs(action,entity_type,entity_id) VALUES('PRIVILEGE_REVOKED','USER',OLD.user_id);
 RETURN OLD;
END $$;
CREATE TRIGGER role_loss_revokes_sessions AFTER DELETE ON user_roles FOR EACH ROW EXECUTE FUNCTION revoke_sessions_after_role_removal();
