-- No clinical/financial retention duration or production authorization is inferred.
INSERT INTO roles(name) VALUES('CLINIC_OPERATIONS'),('CLINIC_PRIVACY_OPERATOR') ON CONFLICT(name) DO NOTHING;
CREATE TABLE clinic_followups(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL REFERENCES consultation_requests(id),professional_id UUID NOT NULL REFERENCES users(id),patient_id UUID NOT NULL REFERENCES users(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_followup_versions(followup_id UUID NOT NULL REFERENCES clinic_followups(id),revision BIGINT NOT NULL,author_id UUID NOT NULL REFERENCES users(id),body_cipher BYTEA NOT NULL,suggested_after TIMESTAMPTZ NOT NULL,expires_at TIMESTAMPTZ NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(followup_id,revision),CHECK(expires_at>suggested_after));
CREATE TABLE clinic_followup_choices(followup_id UUID NOT NULL REFERENCES clinic_followups(id),revision BIGINT NOT NULL,patient_id UUID NOT NULL REFERENCES users(id),choice TEXT NOT NULL CHECK(choice IN('ACCEPT','DECLINE','WITHDRAW')),remind_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,ordinal BIGINT GENERATED ALWAYS AS IDENTITY,FOREIGN KEY(followup_id,revision) REFERENCES clinic_followup_versions(followup_id,revision));
CREATE TABLE clinic_booking_changes(old_appointment_id UUID PRIMARY KEY REFERENCES consultation_requests(id),new_appointment_id UUID NOT NULL UNIQUE REFERENCES consultation_requests(id),patient_id UUID NOT NULL REFERENCES users(id),retry_key UUID NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(patient_id,retry_key),CHECK(old_appointment_id<>new_appointment_id));
CREATE TABLE clinic_operational_incidents(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL REFERENCES consultation_requests(id),reporter_id UUID NOT NULL REFERENCES users(id),kind TEXT NOT NULL CHECK(kind IN('CONNECTION','PAYMENT','ATTENDANCE','PRIVACY','OTHER')),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_incident_reviews(id UUID PRIMARY KEY DEFAULT uuidv7(),incident_id UUID NOT NULL REFERENCES clinic_operational_incidents(id),author_id UUID NOT NULL REFERENCES users(id),evidence_reference VARCHAR(1000) NOT NULL,classification TEXT NOT NULL CHECK(classification IN('NO_OUTAGE','TECHNICAL_FAILURE','UNRESOLVED')),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_incident_decisions(id UUID PRIMARY KEY DEFAULT uuidv7(),review_id UUID NOT NULL UNIQUE REFERENCES clinic_incident_reviews(id),reviewer_id UUID NOT NULL REFERENCES users(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_rights_requests(id UUID PRIMARY KEY DEFAULT uuidv7(),requester_id UUID NOT NULL REFERENCES users(id),appointment_id UUID REFERENCES consultation_requests(id),category TEXT NOT NULL,request_kind TEXT NOT NULL CHECK(request_kind IN('ACCESS','EXPORT','CORRECTION','ERASURE','RESTRICTION','APPEAL')),retry_key UUID NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(requester_id,retry_key));
CREATE TABLE clinic_rights_events(id UUID PRIMARY KEY DEFAULT uuidv7(),request_id UUID NOT NULL REFERENCES clinic_rights_requests(id),actor_id UUID NOT NULL REFERENCES users(id),state TEXT NOT NULL CHECK(state IN('RECEIVED','UNDER_REVIEW','AUTHORIZED_PROCESSOR_REQUIRED','ACTION_EVIDENCED','EXCEPTION_REVIEW','APPEALED')),evidence_reference VARCHAR(1000) NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_data_classes(category TEXT PRIMARY KEY,purpose TEXT NOT NULL,access_scope TEXT NOT NULL,rights_route TEXT NOT NULL,retention TEXT NOT NULL,backup_handling TEXT NOT NULL,incident_handling TEXT NOT NULL,provider_transfer TEXT NOT NULL);
INSERT INTO clinic_data_classes SELECT category,purpose,access_scope,'Authenticated subject request; independent category/purpose review; original history preserved for corrections; erasure requires lawful-policy/hold review','UNAPPROVED: manual review; automatic purge disabled','Encrypted restricted backups; approved expiry and restore reapplication required before production','Purpose-restricted incident review; no narrative in business telemetry','No transfer until independently approved processor, location, purpose and retention terms exist'
FROM (VALUES
('PROFESSIONAL_IDENTITY','Verified professional identity','Subject and credential reviewer'),
('CREDENTIALS','Independent qualification evidence','Subject and credential reviewer'),
('PATIENT_IDENTITY','Authenticated care access','Subject and identity operations'),
('BOOKING','Explicit scheduling and attendance','Participants and purpose-scoped operations'),
('FINANCE','Exact receipts, obligations and reconciliation','Subject and finance operations'),
('CONSENT','Versioned clinical choices','Subject, assigned professional and authorized reviewer'),
('CLINICAL_MESSAGES','Bounded encounter communication','Participants; separately authorized exceptional grant'),
('CLINICAL_NOTES','Versioned professional records and follow-up','Assigned current professional; patient access policy; exceptional grant'),
('CLINICAL_ATTACHMENTS','Protected clinical evidence','Participants under category policy and exceptional grant'),
('MEDIA_METADATA','Ephemeral admission and delivery proof','Participants and approved technical operations; no recordings'),
('REVIEWS','Optional service experience','Author and independent reputation reviewer; redacted chosen public projection'),
('RANKING_EVIDENCE','Auditable limited methodology','Public nonidentifying basis; purpose-scoped reviewers'),
('COMPLAINTS','Independent concern and appeal handling','Requester and independently authorized reviewer'),
('REFUND_DECISIONS','Liability, dispute and provider reconciliation','Subject and finance operations')) AS c(category,purpose,access_scope);
ALTER TABLE clinic_rights_requests ADD FOREIGN KEY(category) REFERENCES clinic_data_classes(category);
CREATE TABLE clinic_legal_holds(id UUID PRIMARY KEY DEFAULT uuidv7(),user_id UUID NOT NULL REFERENCES users(id),category TEXT NOT NULL REFERENCES clinic_data_classes(category),author_id UUID NOT NULL REFERENCES users(id),evidence_reference VARCHAR(1000) NOT NULL,expires_at TIMESTAMPTZ NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_hold_reviews(hold_id UUID PRIMARY KEY REFERENCES clinic_legal_holds(id),reviewer_id UUID NOT NULL REFERENCES users(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_notification_preferences(user_id UUID PRIMARY KEY REFERENCES users(id),optional_enabled BOOLEAN NOT NULL DEFAULT FALSE,zone_id TEXT NOT NULL DEFAULT 'UTC',quiet_start INTEGER NOT NULL DEFAULT 22 CHECK(quiet_start BETWEEN 0 AND 23),quiet_end INTEGER NOT NULL DEFAULT 8 CHECK(quiet_end BETWEEN 0 AND 23));
CREATE TABLE clinic_notice_deliveries(user_id UUID NOT NULL REFERENCES users(id),dedup_key TEXT NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(user_id,dedup_key));
CREATE INDEX clinic_followup_patient ON clinic_followups(patient_id,created_at);
CREATE INDEX clinic_incident_appointment ON clinic_operational_incidents(appointment_id);
CREATE INDEX clinic_rights_subject ON clinic_rights_requests(requester_id,created_at);
CREATE INDEX clinic_rights_queue ON clinic_rights_events(request_id,created_at,id);
DO $$ DECLARE tab TEXT; BEGIN FOREACH tab IN ARRAY ARRAY['clinic_followups','clinic_followup_versions','clinic_followup_choices','clinic_booking_changes','clinic_operational_incidents','clinic_incident_reviews','clinic_incident_decisions','clinic_rights_requests','clinic_rights_events','clinic_legal_holds','clinic_hold_reviews'] LOOP EXECUTE format('CREATE TRIGGER %I BEFORE UPDATE OR DELETE ON %I FOR EACH ROW EXECUTE FUNCTION clinic_append_only()',tab||'_immutable',tab); END LOOP; END $$;

-- These are authenticated in-app state notices, never evidence of delivery or push permission.
CREATE FUNCTION clinic_state_notice() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE patient UUID; professional UUID; appointment UUID;
BEGIN
 IF TG_TABLE_NAME='consultation_requests' THEN
  IF NOT NEW.clinic_governed THEN RETURN NEW; END IF;
  IF TG_OP='UPDATE' AND NEW.status=OLD.status THEN RETURN NEW; END IF;
  patient:=NEW.requester_user_id;professional:=NEW.professional_user_id;appointment:=NEW.id;
 ELSE
  SELECT requester_user_id,professional_user_id INTO patient,professional FROM consultation_requests WHERE id=NEW.appointment_id;
  appointment:=NEW.appointment_id;
 END IF;
 INSERT INTO notifications(user_id,type,title,body) VALUES(patient,'SYSTEM_NOTICE','Account update','Your authenticated account has an updated service status. Open the app to review it.');
 INSERT INTO notifications(user_id,type,title,body) VALUES(professional,'SYSTEM_NOTICE','Account update','Your authenticated professional workspace has an updated service status.');
 RETURN NEW;
END $$;
CREATE TRIGGER clinic_booking_notice AFTER INSERT OR UPDATE OF status ON consultation_requests FOR EACH ROW EXECUTE FUNCTION clinic_state_notice();
CREATE TRIGGER clinic_ledger_notice AFTER INSERT ON clinic_financial_events FOR EACH ROW EXECUTE FUNCTION clinic_state_notice();
CREATE TRIGGER clinic_readiness_notice AFTER INSERT ON clinic_readiness_evidence FOR EACH ROW EXECUTE FUNCTION clinic_state_notice();
