CREATE TABLE clinic_delivery_evidence(id UUID PRIMARY KEY DEFAULT uuidv7(),encounter_id UUID NOT NULL UNIQUE REFERENCES clinic_encounters(id),
 kind TEXT NOT NULL CHECK(kind IN('TEXT_PARTICIPATION','MEDIA_PROVIDER_CONFIRMED')),observed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp());
CREATE TRIGGER clinic_delivery_evidence_immutable BEFORE UPDATE OR DELETE ON clinic_delivery_evidence FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
