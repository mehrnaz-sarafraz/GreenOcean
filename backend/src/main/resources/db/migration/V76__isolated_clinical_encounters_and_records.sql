-- No clinical record is inferred from legacy community, listener, wellbeing or consultation rows.
INSERT INTO roles(name) VALUES('CLINICAL_RECORDS_OPERATOR') ON CONFLICT DO NOTHING;
CREATE TABLE clinic_record_policies(id UUID PRIMARY KEY DEFAULT uuidv7(),version TEXT NOT NULL UNIQUE,
 category TEXT NOT NULL CHECK(category IN('MESSAGES','SHARED_NOTES','PROFESSIONAL_NOTES','ATTACHMENTS')),
 patient_access TEXT NOT NULL CHECK(patient_access IN('DIRECT','REVIEW_REQUIRED')),
 professional_access_days INTEGER NOT NULL CHECK(professional_access_days BETWEEN 0 AND 3650),
 retention_days INTEGER CHECK(retention_days BETWEEN 1 AND 36500),retention_review_required BOOLEAN NOT NULL DEFAULT TRUE,
 processing_basis_reference VARCHAR(2000) NOT NULL,approved_by UUID NOT NULL REFERENCES users(id),
 expires_at TIMESTAMPTZ NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,CHECK(expires_at>created_at));
CREATE TABLE clinic_notice_drafts(id UUID PRIMARY KEY DEFAULT uuidv7(),author_id UUID NOT NULL REFERENCES users(id),revision BIGINT NOT NULL DEFAULT 1,
 kind TEXT NOT NULL,version TEXT NOT NULL,locale TEXT NOT NULL,mode TEXT NOT NULL,body TEXT NOT NULL,effective_at TIMESTAMPTZ NOT NULL,expires_at TIMESTAMPTZ NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_notices(id UUID PRIMARY KEY DEFAULT uuidv7(),draft_id UUID NOT NULL UNIQUE REFERENCES clinic_notice_drafts(id),kind TEXT NOT NULL CHECK(kind IN('PLATFORM_TERMS','CARE_CONSENT','DATA_NOTICE','MEDIA_DISCLOSURE')),
 version TEXT NOT NULL,locale TEXT NOT NULL CHECK(locale IN('en','fa')),mode TEXT NOT NULL CHECK(mode IN('TEXT','AUDIO','VIDEO')),country CHAR(2) NOT NULL CHECK(country='IR'),
 body TEXT NOT NULL CHECK(length(body) BETWEEN 20 AND 20000),author_id UUID NOT NULL REFERENCES users(id),reviewer_id UUID NOT NULL REFERENCES users(id),
 effective_at TIMESTAMPTZ NOT NULL,expires_at TIMESTAMPTZ NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(author_id<>reviewer_id AND expires_at>effective_at),UNIQUE(kind,version,locale,mode,country));
CREATE TABLE clinic_consent_events(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL REFERENCES consultation_requests(id),
 patient_id UUID NOT NULL REFERENCES users(id),notice_id UUID NOT NULL REFERENCES clinic_notices(id),decision TEXT NOT NULL CHECK(decision IN('ACCEPT','WITHDRAW')),
 retry_key UUID NOT NULL,ordinal BIGINT GENERATED ALWAYS AS IDENTITY,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(patient_id,retry_key));
CREATE TABLE clinic_care_relationships(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL UNIQUE REFERENCES consultation_requests(id),
 patient_id UUID NOT NULL REFERENCES users(id),professional_id UUID NOT NULL REFERENCES professional_profiles(user_id),revoked_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_encounters(id UUID PRIMARY KEY DEFAULT uuidv7(),relationship_id UUID NOT NULL UNIQUE REFERENCES clinic_care_relationships(id),
 state TEXT NOT NULL DEFAULT 'WAITING' CHECK(state IN('WAITING','ACTIVE','ENDED','TECHNICAL_FAILURE')),
 revision BIGINT NOT NULL DEFAULT 1,started_at TIMESTAMPTZ,ended_at TIMESTAMPTZ,patient_ack_at TIMESTAMPTZ,professional_ack_at TIMESTAMPTZ,
 completed_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(ended_at IS NULL OR started_at IS NULL OR ended_at>=started_at));
CREATE TABLE clinic_encounter_entries(encounter_id UUID NOT NULL REFERENCES clinic_encounters(id),actor_id UUID NOT NULL REFERENCES users(id),
 session_id UUID NOT NULL REFERENCES user_sessions(id),entered_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,expires_at TIMESTAMPTZ NOT NULL,
 PRIMARY KEY(encounter_id,actor_id));
CREATE TABLE clinic_encounter_events(id UUID PRIMARY KEY DEFAULT uuidv7(),encounter_id UUID NOT NULL REFERENCES clinic_encounters(id),actor_id UUID REFERENCES users(id),
 kind TEXT NOT NULL,revision BIGINT NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(encounter_id,revision));
CREATE TABLE clinic_messages(id UUID PRIMARY KEY DEFAULT uuidv7(),encounter_id UUID NOT NULL REFERENCES clinic_encounters(id),sender_id UUID NOT NULL REFERENCES users(id),
 retry_key UUID NOT NULL,body_cipher BYTEA NOT NULL,policy_id UUID NOT NULL REFERENCES clinic_record_policies(id),ordinal BIGINT GENERATED ALWAYS AS IDENTITY,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(encounter_id,sender_id,retry_key));
CREATE INDEX clinic_messages_page ON clinic_messages(encounter_id,ordinal);
CREATE TABLE clinic_notes(id UUID PRIMARY KEY DEFAULT uuidv7(),encounter_id UUID NOT NULL REFERENCES clinic_encounters(id),author_id UUID NOT NULL REFERENCES users(id),
 category TEXT NOT NULL CHECK(category IN('SHARED_NOTES','PROFESSIONAL_NOTES')),policy_id UUID NOT NULL REFERENCES clinic_record_policies(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_note_versions(id UUID PRIMARY KEY DEFAULT uuidv7(),note_id UUID NOT NULL REFERENCES clinic_notes(id),revision BIGINT NOT NULL,
 state TEXT NOT NULL CHECK(state IN('DRAFT','FINAL','AMENDMENT')),body_cipher BYTEA NOT NULL,author_id UUID NOT NULL REFERENCES users(id),retry_key UUID NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(note_id,revision),UNIQUE(author_id,retry_key));
CREATE TABLE clinic_attachments(id UUID PRIMARY KEY DEFAULT uuidv7(),encounter_id UUID NOT NULL REFERENCES clinic_encounters(id),owner_id UUID NOT NULL REFERENCES users(id),
 policy_id UUID NOT NULL REFERENCES clinic_record_policies(id),content_type TEXT NOT NULL CHECK(content_type IN('application/pdf','image/jpeg','image/png')),
 body_cipher BYTEA NOT NULL,byte_size INTEGER NOT NULL CHECK(byte_size BETWEEN 1 AND 2097152),scan_state TEXT NOT NULL CHECK(scan_state IN('PENDING','CLEAN','REJECTED')),
 scanner_reference TEXT,previous_id UUID REFERENCES clinic_attachments(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_access_audit(id UUID PRIMARY KEY DEFAULT uuidv7(),actor_id UUID NOT NULL REFERENCES users(id),session_id UUID NOT NULL,
 encounter_id UUID NOT NULL REFERENCES clinic_encounters(id),action TEXT NOT NULL,resource_id UUID,grant_id UUID,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_record_access_requests(id UUID PRIMARY KEY DEFAULT uuidv7(),encounter_id UUID NOT NULL REFERENCES clinic_encounters(id),operator_id UUID NOT NULL REFERENCES users(id),category TEXT NOT NULL,purpose_reference VARCHAR(2000) NOT NULL,expires_at TIMESTAMPTZ NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_record_access_grants(id UUID PRIMARY KEY DEFAULT uuidv7(),request_id UUID NOT NULL UNIQUE REFERENCES clinic_record_access_requests(id),encounter_id UUID NOT NULL REFERENCES clinic_encounters(id),operator_id UUID NOT NULL REFERENCES users(id),
 category TEXT NOT NULL CHECK(category IN('MESSAGES','SHARED_NOTES','PROFESSIONAL_NOTES','ATTACHMENTS')),purpose_reference VARCHAR(2000) NOT NULL,
 requested_by UUID NOT NULL REFERENCES users(id),approved_by UUID NOT NULL REFERENCES users(id),expires_at TIMESTAMPTZ NOT NULL,revoked_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,CHECK(approved_by<>requested_by AND approved_by<>operator_id AND expires_at>created_at AND expires_at<=created_at+INTERVAL '24 hours'));
CREATE TABLE clinic_media_admissions(id UUID PRIMARY KEY DEFAULT uuidv7(),encounter_id UUID NOT NULL REFERENCES clinic_encounters(id),actor_id UUID NOT NULL REFERENCES users(id),session_id UUID NOT NULL REFERENCES user_sessions(id),
 expires_at TIMESTAMPTZ NOT NULL,consumed_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_media_events(id UUID PRIMARY KEY DEFAULT uuidv7(),encounter_id UUID NOT NULL REFERENCES clinic_encounters(id),actor_id UUID REFERENCES users(id),
 kind TEXT NOT NULL CHECK(kind IN('PERMISSION_DENIED','NETWORK_FAILURE','PROVIDER_OUTAGE','DISCONNECTED','RECONNECT_TIMEOUT','SERVICE_DISABLED','PARTICIPANT_UNAVAILABLE')),
 source TEXT NOT NULL CHECK(source IN('PARTICIPANT_REPORT','PROVIDER_VERIFIED')),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TRIGGER clinic_record_policy_immutable BEFORE UPDATE OR DELETE ON clinic_record_policies FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_notice_immutable BEFORE UPDATE OR DELETE ON clinic_notices FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_consent_immutable BEFORE UPDATE OR DELETE ON clinic_consent_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_message_immutable BEFORE UPDATE OR DELETE ON clinic_messages FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_note_version_immutable BEFORE UPDATE OR DELETE ON clinic_note_versions FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_note_identity_immutable BEFORE UPDATE OR DELETE ON clinic_notes FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_encounter_events_immutable BEFORE UPDATE OR DELETE ON clinic_encounter_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_access_audit_immutable BEFORE UPDATE OR DELETE ON clinic_access_audit FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_media_events_immutable BEFORE UPDATE OR DELETE ON clinic_media_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE FUNCTION clinic_attachment_payload_immutable() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF ROW(NEW.encounter_id,NEW.owner_id,NEW.policy_id,NEW.content_type,NEW.body_cipher,NEW.byte_size,NEW.previous_id) IS DISTINCT FROM ROW(OLD.encounter_id,OLD.owner_id,OLD.policy_id,OLD.content_type,OLD.body_cipher,OLD.byte_size,OLD.previous_id) THEN RAISE EXCEPTION 'Clinical attachment corrections require a new linked version';END IF;RETURN NEW;END $$;
CREATE TRIGGER clinic_attachment_payload_guard BEFORE UPDATE ON clinic_attachments FOR EACH ROW EXECUTE FUNCTION clinic_attachment_payload_immutable();
