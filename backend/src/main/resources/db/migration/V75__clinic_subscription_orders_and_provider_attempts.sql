INSERT INTO roles(name) VALUES('CLINIC_FINANCE') ON CONFLICT DO NOTHING;
ALTER TABLE clinic_subscription_plans ADD COLUMN period_days INTEGER CHECK(period_days BETWEEN 1 AND 366);
CREATE TABLE clinic_subscription_orders(id UUID PRIMARY KEY DEFAULT uuidv7(),professional_id UUID NOT NULL REFERENCES professional_profiles(user_id),
 plan_id UUID NOT NULL REFERENCES clinic_subscription_plans(id),retry_key UUID NOT NULL,state TEXT NOT NULL DEFAULT 'PENDING' CHECK(state IN('PENDING','ACTIVE','MANUAL_REVIEW_REQUIRED')),
 provider TEXT,provider_reference TEXT UNIQUE,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(professional_id,retry_key));
ALTER TABLE clinic_professional_subscriptions ADD COLUMN order_id UUID UNIQUE REFERENCES clinic_subscription_orders(id);
CREATE TABLE clinic_provider_attempts(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID REFERENCES consultation_requests(id),
 operation TEXT NOT NULL CHECK(operation IN('INTENT','REFUND','SETTLEMENT')),state TEXT NOT NULL CHECK(state IN('PROVIDER_REFERENCE_RETURNED','MANUAL_REVIEW_REQUIRED')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TRIGGER clinic_provider_attempts_immutable BEFORE UPDATE OR DELETE ON clinic_provider_attempts FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TABLE clinic_settlement_requests(id UUID PRIMARY KEY DEFAULT uuidv7(),appointment_id UUID NOT NULL UNIQUE REFERENCES consultation_requests(id),
 amount_units BIGINT NOT NULL CHECK(amount_units>0),state TEXT NOT NULL DEFAULT 'PENDING' CHECK(state IN('PENDING','PROCESSING','CONFIRMED','MANUAL_REVIEW_REQUIRED')),
 provider_reference TEXT UNIQUE,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
