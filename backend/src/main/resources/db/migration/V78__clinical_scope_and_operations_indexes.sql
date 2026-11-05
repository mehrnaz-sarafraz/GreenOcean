CREATE INDEX clinic_consents_current ON clinic_consent_events(appointment_id,notice_id,ordinal DESC);
CREATE INDEX clinic_notices_current ON clinic_notices(mode,locale,kind,created_at DESC);
CREATE INDEX clinic_record_policies_current ON clinic_record_policies(category,created_at DESC);
CREATE INDEX clinic_notes_encounter_category ON clinic_notes(encounter_id,category,id);
CREATE INDEX clinic_grants_current ON clinic_record_access_grants(encounter_id,operator_id,category,expires_at) WHERE revoked_at IS NULL;
CREATE INDEX clinic_relationship_patient ON clinic_care_relationships(patient_id,created_at DESC);
CREATE INDEX clinic_relationship_professional ON clinic_care_relationships(professional_id,created_at DESC);
CREATE INDEX clinic_refund_operations ON clinic_refunds(state,initiate_by);
CREATE INDEX clinic_pending_reservations ON consultation_requests(payment_deadline,id) WHERE clinic_governed AND status IN('REQUESTED','PAYMENT_PENDING');
