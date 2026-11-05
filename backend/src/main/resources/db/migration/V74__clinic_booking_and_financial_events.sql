-- Existing appointments remain legacy; no historical status becomes evidence of service/payment.
ALTER TABLE consultation_requests DROP CONSTRAINT chk_consultation_requests_status;
ALTER TABLE consultation_requests ALTER COLUMN status TYPE VARCHAR(40);
ALTER TABLE consultation_requests ADD CONSTRAINT chk_consultation_requests_status CHECK(status IN('REQUESTED','CONFIRMED','DECLINED','COMPLETED','CANCELLED','PAYMENT_PENDING','SESSION_ACTIVE','CANCELLED_BY_PATIENT','CANCELLED_BY_PROFESSIONAL','CANCELLED_BY_PLATFORM','PROFESSIONAL_NO_SHOW','PATIENT_NO_SHOW','TECHNICAL_FAILURE','DISPUTED'));
ALTER TABLE consultation_requests ADD COLUMN clinic_governed BOOLEAN NOT NULL DEFAULT FALSE,
 ADD COLUMN booking_key UUID, ADD COLUMN booking_revision BIGINT NOT NULL DEFAULT 1,
 ADD COLUMN ends_at TIMESTAMPTZ, ADD COLUMN price_units BIGINT CHECK(price_units>=0),
 ADD COLUMN commission_units BIGINT CHECK(commission_units>=0 AND commission_units<=price_units),
 ADD COLUMN provider_fee_units BIGINT CHECK(provider_fee_units>=0), ADD COLUMN currency CHAR(3) CHECK(currency='IRR'),
 ADD COLUMN policy_id UUID, ADD COLUMN terms_snapshot TEXT, ADD COLUMN confirmed_at TIMESTAMPTZ,
 ADD COLUMN payment_deadline TIMESTAMPTZ, ADD COLUMN approval_required BOOLEAN NOT NULL DEFAULT FALSE,
 ADD CONSTRAINT clinic_booking_retry UNIQUE(requester_user_id,booking_key),
 ADD CONSTRAINT clinic_booking_snapshot CHECK(NOT clinic_governed OR(booking_key IS NOT NULL AND ends_at>scheduled_at AND price_units IS NOT NULL AND commission_units IS NOT NULL AND provider_fee_units IS NOT NULL AND currency IS NOT NULL AND policy_id IS NOT NULL AND terms_snapshot IS NOT NULL));
ALTER TABLE consultation_requests ADD CONSTRAINT clinic_patient_overlap EXCLUDE USING gist(requester_user_id WITH =,tstzrange(scheduled_at,ends_at,'[)') WITH &&) WHERE(clinic_governed AND status IN('REQUESTED','PAYMENT_PENDING','CONFIRMED','SESSION_ACTIVE'));
CREATE UNIQUE INDEX clinic_reserved_slot ON consultation_requests(availability_slot_id) WHERE clinic_governed AND status IN('REQUESTED','PAYMENT_PENDING','CONFIRMED','SESSION_ACTIVE');
CREATE TABLE clinic_commercial_policies(
 id UUID PRIMARY KEY DEFAULT uuidv7(),version TEXT NOT NULL UNIQUE,commission_bps INTEGER NOT NULL CHECK(commission_bps BETWEEN 0 AND 10000),
 provider_fee_units BIGINT NOT NULL CHECK(provider_fee_units>=0),terms TEXT NOT NULL CHECK(length(terms) BETWEEN 10 AND 5000),
 refund_policy TEXT NOT NULL CHECK(length(refund_policy) BETWEEN 10 AND 5000),approved_by UUID NOT NULL REFERENCES users(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
ALTER TABLE consultation_requests ADD FOREIGN KEY(policy_id) REFERENCES clinic_commercial_policies(id);
CREATE TABLE clinic_slot_offers(slot_id UUID PRIMARY KEY REFERENCES professional_availability_slots(id) ON DELETE CASCADE,
 policy_id UUID NOT NULL REFERENCES clinic_commercial_policies(id),approval_required BOOLEAN NOT NULL DEFAULT FALSE,accepted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_appointment_events(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL REFERENCES consultation_requests(id),
 revision BIGINT NOT NULL,actor_id UUID REFERENCES users(id),state TEXT NOT NULL,reason VARCHAR(2000) NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(appointment_id,revision));
CREATE TABLE clinic_invoices(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL UNIQUE REFERENCES consultation_requests(id),amount_units BIGINT NOT NULL CHECK(amount_units>=0),currency CHAR(3) NOT NULL CHECK(currency='IRR'),issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_payment_intents(id UUID PRIMARY KEY DEFAULT uuidv7(),invoice_id UUID NOT NULL UNIQUE REFERENCES clinic_invoices(id),provider TEXT NOT NULL,
 state TEXT NOT NULL DEFAULT 'CREATED' CHECK(state IN('CREATED','PENDING','CAPTURED','FAILED','TIMED_OUT','RECONCILIATION_REQUIRED')),provider_reference TEXT UNIQUE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_provider_events(id UUID PRIMARY KEY DEFAULT uuidv7(),provider TEXT NOT NULL,event_key TEXT NOT NULL,reference TEXT NOT NULL,kind TEXT NOT NULL CHECK(kind IN('CAPTURE','REFUND','SETTLEMENT')),amount_units BIGINT NOT NULL CHECK(amount_units>0),currency CHAR(3) NOT NULL CHECK(currency='IRR'),verified_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(provider,event_key));
CREATE TABLE clinic_financial_events(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL REFERENCES consultation_requests(id),
 kind TEXT NOT NULL CHECK(kind IN('CAPTURE','PLATFORM_FEE','PROFESSIONAL_PAYABLE','REFUND_OBLIGATION','REFUND_CONFIRMED','COMMISSION_REVERSAL','PAYABLE_REVERSAL','SETTLEMENT_CONFIRMED')),
 amount_units BIGINT NOT NULL CHECK(amount_units>=0),currency CHAR(3) NOT NULL CHECK(currency='IRR'),provider_event_id UUID REFERENCES clinic_provider_events(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(appointment_id,kind));
CREATE TABLE clinic_refunds(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL UNIQUE REFERENCES consultation_requests(id),amount_units BIGINT NOT NULL CHECK(amount_units>0),
 state TEXT NOT NULL CHECK(state IN('REFUND_ELIGIBLE','REFUND_REQUESTED','REFUND_PROCESSING','REFUND_CONFIRMED','REFUND_FAILED','MANUAL_REVIEW_REQUIRED')),
 reason VARCHAR(2000) NOT NULL,confirmed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,initiate_by TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP+INTERVAL '48 hours',provider_reference TEXT UNIQUE,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_disputes(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL UNIQUE REFERENCES consultation_requests(id),patient_id UUID NOT NULL REFERENCES users(id),reason VARCHAR(2000) NOT NULL,state TEXT NOT NULL DEFAULT 'OPEN' CHECK(state IN('OPEN','RESOLVED')),resolution VARCHAR(2000),resolved_by UUID REFERENCES users(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_subscription_plans(id UUID PRIMARY KEY DEFAULT uuidv7(),version TEXT NOT NULL UNIQUE,entitlements TEXT[] NOT NULL,
 price_units BIGINT NOT NULL CHECK(price_units>=0),currency CHAR(3) NOT NULL CHECK(currency='IRR'),approved_by UUID NOT NULL REFERENCES users(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(entitlements<@ARRAY['CALENDAR_TOOLS','ADMIN_REPORTS','PRACTICE_TOOLS','EDITORIAL_WORKSPACE']::TEXT[]));
CREATE TABLE clinic_professional_subscriptions(id UUID PRIMARY KEY DEFAULT uuidv7(),professional_id UUID NOT NULL REFERENCES professional_profiles(user_id),plan_id UUID NOT NULL REFERENCES clinic_subscription_plans(id),starts_at TIMESTAMPTZ NOT NULL,ends_at TIMESTAMPTZ NOT NULL,provider_event_id UUID REFERENCES clinic_provider_events(id),CHECK(ends_at>starts_at));
CREATE TABLE clinic_readiness_evidence(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL REFERENCES consultation_requests(id),actor_id UUID NOT NULL REFERENCES users(id),session_id UUID NOT NULL,observed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE FUNCTION clinic_append_only() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'Clinic audit records are append only'; END $$;
CREATE TRIGGER clinic_policy_immutable BEFORE UPDATE OR DELETE ON clinic_commercial_policies FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_plan_immutable BEFORE UPDATE OR DELETE ON clinic_subscription_plans FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_ledger_immutable BEFORE UPDATE OR DELETE ON clinic_financial_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_provider_immutable BEFORE UPDATE OR DELETE ON clinic_provider_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_appointment_history_immutable BEFORE UPDATE OR DELETE ON clinic_appointment_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_invoice_immutable BEFORE UPDATE OR DELETE ON clinic_invoices FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_readiness_immutable BEFORE UPDATE OR DELETE ON clinic_readiness_evidence FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE FUNCTION clinic_readiness_capture() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF EXISTS(SELECT 1 FROM consultation_requests WHERE id=NEW.appointment_id AND clinic_governed) THEN INSERT INTO clinic_readiness_evidence(appointment_id,actor_id,session_id) VALUES(NEW.appointment_id,NEW.actor_id,NEW.session_id); END IF; RETURN NEW; END $$;
CREATE TRIGGER clinic_readiness_history AFTER INSERT OR UPDATE ON professional_appointment_readiness FOR EACH ROW EXECUTE FUNCTION clinic_readiness_capture();
CREATE FUNCTION clinic_booking_immutable() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF OLD.clinic_governed AND(ROW(NEW.requester_user_id,NEW.professional_user_id,NEW.availability_slot_id,NEW.scheduled_at,NEW.ends_at,NEW.consultation_mode,NEW.price_units,NEW.commission_units,NEW.provider_fee_units,NEW.currency,NEW.policy_id,NEW.terms_snapshot,NEW.booking_key,NEW.clinic_governed) IS DISTINCT FROM ROW(OLD.requester_user_id,OLD.professional_user_id,OLD.availability_slot_id,OLD.scheduled_at,OLD.ends_at,OLD.consultation_mode,OLD.price_units,OLD.commission_units,OLD.provider_fee_units,OLD.currency,OLD.policy_id,OLD.terms_snapshot,OLD.booking_key,OLD.clinic_governed)) THEN RAISE EXCEPTION 'Agreed clinic booking is immutable'; END IF;RETURN NEW; END $$;
CREATE TRIGGER clinic_booking_snapshot_immutable BEFORE UPDATE ON consultation_requests FOR EACH ROW EXECUTE FUNCTION clinic_booking_immutable();
