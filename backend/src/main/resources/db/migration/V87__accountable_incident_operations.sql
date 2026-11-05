-- Operational references only: no report, journal, message or clinical narrative copies.
CREATE TABLE operation_incidents(
 id UUID PRIMARY KEY DEFAULT uuidv7(),domain TEXT NOT NULL CHECK(domain IN('SOCIAL','GROUP','LISTENER','CLINIC','FINANCE','ACCOUNT','EDUCATION','PLATFORM')),
 kind TEXT NOT NULL CHECK(kind IN('DATA_EXPOSURE','CREDENTIAL_COMPROMISE','DATABASE_OUTAGE','EMAIL_OUTAGE','PAYMENT_CALLBACK','REFUND_DELAY','STAFFING_GAP','MEDIA_INTERRUPTION','VISIBILITY_REGRESSION','KEY_CONFIGURATION','SERVICE_FAILURE','BACKUP_FAILURE')),
 severity TEXT NOT NULL CHECK(severity IN('SEV1','SEV2','SEV3','SEV4')),
 state TEXT NOT NULL DEFAULT 'DETECTED' CHECK(state IN('DETECTED','ACKNOWLEDGED','TRIAGED','CONTAINMENT','INVESTIGATING','RECOVERY_PENDING','RECOVERING','RESOLVED','POST_INCIDENT_REVIEW','CLOSED')),
 revision BIGINT NOT NULL DEFAULT 1,created_by UUID REFERENCES users(id),owner_id UUID REFERENCES users(id),evidence_reference VARCHAR(160) NOT NULL,
 dedup_key UUID NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 recovery_author UUID REFERENCES users(id),recovery_session UUID,recovery_version INTEGER,recovery_expires_at TIMESTAMPTZ,recovery_reviewer UUID REFERENCES users(id),UNIQUE(domain,dedup_key)
);
CREATE INDEX operation_incident_queue ON operation_incidents(domain,state,created_at,id);
CREATE TABLE operation_incident_events(
 id UUID PRIMARY KEY DEFAULT uuidv7(),incident_id UUID NOT NULL REFERENCES operation_incidents(id),revision BIGINT NOT NULL,actor_id UUID REFERENCES users(id),
 from_state TEXT,to_state TEXT NOT NULL,evidence_reference VARCHAR(160) NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(incident_id,revision)
);
CREATE TABLE operation_containment(
 id UUID PRIMARY KEY DEFAULT uuidv7(),incident_id UUID NOT NULL REFERENCES operation_incidents(id),domain TEXT NOT NULL CHECK(domain IN('SOCIAL','GROUP','LISTENER','CLINIC','FINANCE','ACCOUNT','EDUCATION','PLATFORM')),
 control TEXT NOT NULL CHECK(control IN('LISTENER_NEW','LISTENER_QUEUE','CLINIC_BOOKING','CLINIC_MEDIA','PAYMENT_CAPTURE','EDUCATION_PUBLICATION','ACCOUNT_RESTRICTION')),
 target_user UUID REFERENCES users(id),action TEXT NOT NULL CHECK(action IN('PAUSE','RESUME')),author_id UUID NOT NULL REFERENCES users(id),author_session UUID NOT NULL,author_version INTEGER NOT NULL,
 incident_revision BIGINT NOT NULL,evidence_reference VARCHAR(160) NOT NULL,expires_at TIMESTAMPTZ NOT NULL DEFAULT(CURRENT_TIMESTAMP+INTERVAL '15 minutes'),
 reviewer_id UUID REFERENCES users(id),approved_at TIMESTAMPTZ,revoked_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK((control='ACCOUNT_RESTRICTION')=(target_user IS NOT NULL)),CHECK(author_id IS DISTINCT FROM reviewer_id)
);
CREATE INDEX operation_containment_active ON operation_containment(control,target_user,approved_at) WHERE approved_at IS NOT NULL;
CREATE TABLE operation_alert_routes(
 id UUID PRIMARY KEY DEFAULT uuidv7(),domain TEXT NOT NULL CHECK(domain IN('SOCIAL','GROUP','LISTENER','CLINIC','FINANCE','ACCOUNT','EDUCATION','PLATFORM')),recipient_id UUID NOT NULL REFERENCES users(id),destination_key TEXT NOT NULL CHECK(destination_key IN('PRIMARY','ESCALATION')),
 escalation_level INTEGER NOT NULL CHECK(escalation_level IN(0,1)),author_id UUID NOT NULL REFERENCES users(id),author_session UUID NOT NULL,author_version INTEGER NOT NULL,approved_by UUID REFERENCES users(id),
 expires_at TIMESTAMPTZ NOT NULL,acknowledged_at TIMESTAMPTZ,revoked_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(author_id IS DISTINCT FROM approved_by),CHECK(expires_at>created_at AND expires_at<=created_at+INTERVAL '30 days')
);
CREATE TABLE operation_alerts(
 id UUID PRIMARY KEY DEFAULT uuidv7(),incident_id UUID NOT NULL REFERENCES operation_incidents(id),route_id UUID NOT NULL REFERENCES operation_alert_routes(id),
 state TEXT NOT NULL DEFAULT 'QUEUED' CHECK(state IN('QUEUED','CLAIMED','DELIVERED','FAILED','ACKNOWLEDGED','CANCELLED')),
 attempts INTEGER NOT NULL DEFAULT 0,next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,claim_id UUID,claimed_at TIMESTAMPTZ,
 delivered_at TIMESTAMPTZ,acknowledged_at TIMESTAMPTZ,acknowledged_by UUID REFERENCES users(id),last_status INTEGER,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(incident_id,route_id)
);
CREATE INDEX operation_alert_due ON operation_alerts(next_attempt_at) WHERE state IN('QUEUED','CLAIMED');
CREATE TABLE operation_communications(
 id UUID PRIMARY KEY DEFAULT uuidv7(),incident_id UUID NOT NULL REFERENCES operation_incidents(id),incident_revision BIGINT NOT NULL,
 template TEXT NOT NULL CHECK(template IN('SERVICE_INTERRUPTION','SERVICE_RESTORED','CONTACT_SUPPORT')),locale TEXT NOT NULL CHECK(locale IN('en','fa')),
 public_status BOOLEAN NOT NULL DEFAULT FALSE,author_id UUID NOT NULL REFERENCES users(id),author_session UUID NOT NULL,author_version INTEGER NOT NULL,
 reviewer_id UUID REFERENCES users(id),approved_at TIMESTAMPTZ,revoked_at TIMESTAMPTZ,expires_at TIMESTAMPTZ NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(author_id IS DISTINCT FROM reviewer_id)
);
CREATE TABLE operation_communication_targets(communication_id UUID NOT NULL REFERENCES operation_communications(id),user_id UUID NOT NULL REFERENCES users(id),notification_id UUID REFERENCES notifications(id) ON DELETE SET NULL,PRIMARY KEY(communication_id,user_id));
CREATE TABLE operation_ranking_audits(
 id UUID PRIMARY KEY DEFAULT uuidv7(),policy_id UUID NOT NULL REFERENCES clinic_ranking_policies(id),profession TEXT NOT NULL CHECK(profession IN('PSYCHOLOGIST','COUNSELOR','PSYCHIATRIST')),mode TEXT NOT NULL CHECK(mode IN('TEXT','AUDIO','VIDEO')),
 author_id UUID NOT NULL REFERENCES users(id),reviewer_id UUID REFERENCES users(id),evidence_reference VARCHAR(160) NOT NULL,
 review_interval_days INTEGER NOT NULL CHECK(review_interval_days BETWEEN 1 AND 90),approved_at TIMESTAMPTZ,next_review_at TIMESTAMPTZ,
 revoked_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,CHECK(author_id IS DISTINCT FROM reviewer_id)
);
CREATE FUNCTION protect_operation_timeline() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'Operational timeline is append-only'; END $$;
CREATE TRIGGER operation_timeline_immutable BEFORE UPDATE OR DELETE ON operation_incident_events FOR EACH ROW EXECUTE FUNCTION protect_operation_timeline();
-- Extend the existing Q2 revocation transaction; never lift an already approved safety hold.
CREATE OR REPLACE FUNCTION revoke_sessions_after_role_removal() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 PERFORM id FROM users WHERE id=OLD.user_id FOR UPDATE;
 UPDATE user_sessions SET revoked_at=CURRENT_TIMESTAMP,token_version=token_version+1 WHERE user_id=OLD.user_id AND revoked_at IS NULL;
 UPDATE social_attribution_requests SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE requester_id=OLD.user_id OR approved_by=OLD.user_id;
 UPDATE clinic_record_access_requests SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP),revision=revision+1 WHERE operator_id=OLD.user_id;
 UPDATE clinic_record_access_grants SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE operator_id=OLD.user_id OR approved_by=OLD.user_id;
 UPDATE trust_coverage SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE operator_id=OLD.user_id OR approved_by=OLD.user_id;
 UPDATE operation_alert_routes SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE recipient_id=OLD.user_id OR author_id=OLD.user_id OR approved_by=OLD.user_id;
 UPDATE operation_alerts SET state='CANCELLED',claim_id=NULL WHERE state IN('QUEUED','CLAIMED') AND route_id IN(SELECT id FROM operation_alert_routes WHERE recipient_id=OLD.user_id OR author_id=OLD.user_id OR approved_by=OLD.user_id);
 UPDATE operation_ranking_audits SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE author_id=OLD.user_id OR reviewer_id=OLD.user_id;
 UPDATE operation_containment SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE author_id=OLD.user_id AND approved_at IS NULL;
 UPDATE operation_communications SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE author_id=OLD.user_id OR reviewer_id=OLD.user_id;
 INSERT INTO audit_logs(action,entity_type,entity_id) VALUES('PRIVILEGE_REVOKED','USER',OLD.user_id);
 RETURN OLD;
END $$;
-- No operator, route destination, approval, audit or staffed period is seeded.
