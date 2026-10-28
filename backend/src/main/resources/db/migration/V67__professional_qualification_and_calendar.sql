-- Extend original applications/slots; never infer current authority from historical VERIFIED flags.
ALTER TABLE professional_verifications
 ADD COLUMN qualification_state TEXT NOT NULL DEFAULT 'SUBMITTED' CHECK(qualification_state IN('SUBMITTED','DOCUMENTS_PENDING','UNDER_VERIFICATION','NEEDS_CORRECTION','VERIFIED','REJECTED','SUSPENDED','EXPIRED','REVOKED')),
 ADD COLUMN qualification_revision BIGINT NOT NULL DEFAULT 1,
 ADD COLUMN professional_category TEXT CHECK(professional_category IN('PSYCHOLOGIST','COUNSELOR','PSYCHIATRIST')),
 ADD COLUMN license_issuer VARCHAR(300),ADD COLUMN verified_name VARCHAR(200),ADD COLUMN license_identifier VARCHAR(100),
 ADD COLUMN verification_method VARCHAR(80),ADD COLUMN verification_source VARCHAR(1000),ADD COLUMN evidence_reference VARCHAR(2000),
 ADD COLUMN valid_from TIMESTAMPTZ,ADD COLUMN valid_until TIMESTAMPTZ,ADD COLUMN recheck_at TIMESTAMPTZ,
 ADD CONSTRAINT professional_qualification_dates CHECK(valid_until IS NULL OR(valid_from IS NOT NULL AND valid_until>valid_from AND recheck_at>valid_from AND recheck_at<=valid_until));
CREATE TABLE professional_qualification_decisions(
 id UUID PRIMARY KEY DEFAULT uuidv7(),application_id UUID NOT NULL REFERENCES professional_verifications(id) ON DELETE CASCADE,
 revision BIGINT NOT NULL,actor_id UUID REFERENCES users(id) ON DELETE SET NULL,state TEXT NOT NULL,
 reason VARCHAR(2000) NOT NULL,evidence_reference VARCHAR(2000),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(application_id,revision));
CREATE TABLE professional_scope_authorizations(
 application_id UUID NOT NULL REFERENCES professional_verifications(id) ON DELETE CASCADE,
 country_code CHAR(2) NOT NULL REFERENCES geography_countries(code),mode TEXT NOT NULL CHECK(mode IN('TEXT','AUDIO','VIDEO')),
 activity TEXT NOT NULL DEFAULT 'CONSULTATION' CHECK(activity='CONSULTATION'),PRIMARY KEY(application_id,country_code,mode));
CREATE TABLE clinic_release_approvals(
 id UUID PRIMARY KEY DEFAULT uuidv7(),country_code CHAR(2) NOT NULL REFERENCES geography_countries(code),mode TEXT NOT NULL CHECK(mode IN('TEXT','AUDIO','VIDEO')),
 purpose TEXT NOT NULL CHECK(purpose IN('PLATFORM','PROFESSIONAL_SCOPE','PATIENT_IDENTITY','DATA_PROCESSING','TERMS','OPERATIONS','MEDIA_PROVIDER','PAYMENT_PROVIDER','SETTLEMENT','PROMOTION')),
 evidence_reference VARCHAR(2000) NOT NULL CHECK(length(trim(evidence_reference))>=10),approved_by UUID NOT NULL REFERENCES users(id),
 approved_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,expires_at TIMESTAMPTZ NOT NULL,revoked_at TIMESTAMPTZ,
 CHECK(expires_at>approved_at));
CREATE FUNCTION professional_qualification_live(who UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM professional_verifications v JOIN professional_profiles p ON p.user_id=v.professional_user_id JOIN users u ON u.id=p.user_id
 WHERE v.professional_user_id=who AND u.status='ACTIVE' AND p.verification_status='VERIFIED' AND v.qualification_state='VERIFIED' AND v.status='APPROVED'
 AND v.reviewed_by<>who AND v.reviewed_at IS NOT NULL AND v.valid_from<=CURRENT_TIMESTAMP AND v.valid_until>CURRENT_TIMESTAMP AND v.recheck_at>CURRENT_TIMESTAMP
 AND v.professional_category=p.professional_type AND v.license_identifier=p.license_number
 AND length(trim(v.evidence_reference))>=10 AND length(trim(v.verification_source))>=10
 AND EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id JOIN users reviewer ON reviewer.id=ur.user_id WHERE ur.user_id=v.reviewed_by AND reviewer.status='ACTIVE' AND r.name IN('ADMIN','MODERATOR')))
$$;
CREATE FUNCTION clinic_release_live(country CHAR(2),channel TEXT) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT country='IR' AND NOT EXISTS(SELECT 1 FROM unnest(ARRAY['PLATFORM','PROFESSIONAL_SCOPE','PATIENT_IDENTITY','DATA_PROCESSING','TERMS','OPERATIONS']||CASE WHEN channel IN('AUDIO','VIDEO') THEN ARRAY['MEDIA_PROVIDER'] ELSE ARRAY[]::TEXT[] END) required(purpose)
 WHERE NOT EXISTS(SELECT 1 FROM clinic_release_approvals a JOIN users u ON u.id=a.approved_by WHERE a.country_code=country AND a.mode=channel AND a.purpose=required.purpose AND a.revoked_at IS NULL AND a.expires_at>CURRENT_TIMESTAMP AND u.status='ACTIVE'
 AND EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=a.approved_by AND r.name='ADMIN')))
$$;
CREATE FUNCTION professional_scope_live(who UUID,country CHAR(2),channel TEXT) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT professional_qualification_live(who) AND clinic_release_live(country,channel) AND EXISTS(
 SELECT 1 FROM professional_scope_authorizations s JOIN professional_verifications v ON v.id=s.application_id
 WHERE v.professional_user_id=who AND v.status='APPROVED' AND v.reviewed_by<>who AND v.professional_category=(SELECT professional_type FROM professional_profiles WHERE user_id=who) AND v.license_identifier=(SELECT license_number FROM professional_profiles WHERE user_id=who) AND v.qualification_state='VERIFIED' AND v.valid_from<=CURRENT_TIMESTAMP AND v.valid_until>CURRENT_TIMESTAMP AND v.recheck_at>CURRENT_TIMESTAMP AND s.country_code=country AND s.mode=channel)
$$;
ALTER TABLE professional_application_documents ADD COLUMN encryption_version INTEGER NOT NULL DEFAULT 0,ADD COLUMN encryption_key_id VARCHAR(80),ADD COLUMN scanner_reference VARCHAR(300);
-- Old bytes are not magically encrypted/scanned. They remain quarantined for re-upload/migration.
UPDATE professional_application_documents SET scan_status='PENDING',scanned_at=NULL WHERE scan_status='CLEAN';
ALTER TABLE professional_availability_slots DROP CONSTRAINT chk_professional_slot_status;
ALTER TABLE professional_availability_slots ADD CONSTRAINT chk_professional_slot_status CHECK(status IN('DRAFT','AVAILABLE','BOOKED','CANCELLED')),
 ADD COLUMN governed BOOLEAN NOT NULL DEFAULT FALSE,ADD COLUMN price_units BIGINT NOT NULL DEFAULT 0 CHECK(price_units>=0),ADD COLUMN currency CHAR(3) NOT NULL DEFAULT 'IRR' CHECK(currency='IRR'),
 ADD COLUMN calendar_rule_id UUID,ADD COLUMN timezone VARCHAR(80) NOT NULL DEFAULT 'Asia/Tehran',ADD COLUMN revision BIGINT NOT NULL DEFAULT 1;
ALTER TABLE professional_availability_slots ADD CONSTRAINT professional_governed_slot_exclusive EXCLUDE USING gist(professional_user_id WITH =,tstzrange(starts_at,ends_at,'[)') WITH &&) WHERE(governed AND status IN('AVAILABLE','BOOKED'));
CREATE TABLE professional_calendar_rules(
 id UUID PRIMARY KEY DEFAULT uuidv7(),professional_id UUID NOT NULL REFERENCES professional_profiles(user_id) ON DELETE CASCADE,
 timezone VARCHAR(80) NOT NULL,weekday INTEGER NOT NULL CHECK(weekday BETWEEN 1 AND 7),starts_local TIME NOT NULL,ends_local TIME NOT NULL,
 valid_from DATE NOT NULL,valid_until DATE NOT NULL,slot_minutes INTEGER NOT NULL CHECK(slot_minutes BETWEEN 15 AND 240),
 mode TEXT NOT NULL CHECK(mode IN('TEXT','AUDIO','VIDEO')),price_units BIGINT NOT NULL CHECK(price_units>=0),
 overlap_policy TEXT NOT NULL CHECK(overlap_policy IN('EARLIER','LATER','REJECT')),active BOOLEAN NOT NULL DEFAULT TRUE,revision BIGINT NOT NULL DEFAULT 1,
 CHECK(ends_local>starts_local AND valid_until>=valid_from AND valid_until<=valid_from+366));
ALTER TABLE professional_availability_slots ADD CONSTRAINT professional_slot_rule FOREIGN KEY(calendar_rule_id) REFERENCES professional_calendar_rules(id) ON DELETE SET NULL;
CREATE TABLE professional_calendar_exceptions(
 id UUID PRIMARY KEY DEFAULT uuidv7(),professional_id UUID NOT NULL REFERENCES professional_profiles(user_id) ON DELETE CASCADE,
 starts_at TIMESTAMPTZ NOT NULL,ends_at TIMESTAMPTZ NOT NULL,reason VARCHAR(300) NOT NULL,active BOOLEAN NOT NULL DEFAULT TRUE,
 CHECK(ends_at>starts_at AND ends_at<=starts_at+INTERVAL '31 days'));
CREATE TABLE professional_appointment_readiness(
 appointment_id UUID NOT NULL REFERENCES consultation_requests(id) ON DELETE CASCADE,actor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 session_id UUID NOT NULL REFERENCES user_sessions(id) ON DELETE CASCADE,observed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 expires_at TIMESTAMPTZ NOT NULL,PRIMARY KEY(appointment_id,actor_id));
CREATE TABLE professional_reminder_events(
 id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL REFERENCES consultation_requests(id) ON DELETE CASCADE,
 recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,kind TEXT NOT NULL CHECK(kind IN('DAY','HOUR','QUARTER','START')),
 due_at TIMESTAMPTZ NOT NULL,state TEXT NOT NULL DEFAULT 'PENDING' CHECK(state IN('PENDING','IN_APP_CREATED','SUPPRESSED','CANCELLED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(appointment_id,recipient_id,kind));
CREATE INDEX professional_reminder_due ON professional_reminder_events(due_at) WHERE state='PENDING';
