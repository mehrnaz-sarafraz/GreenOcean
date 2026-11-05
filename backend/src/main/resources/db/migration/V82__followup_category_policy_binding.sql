-- Unknown earlier follow-up policies stay restricted; never infer a historical authorization.
ALTER TABLE clinic_followup_versions ADD COLUMN policy_id UUID REFERENCES clinic_record_policies(id);
