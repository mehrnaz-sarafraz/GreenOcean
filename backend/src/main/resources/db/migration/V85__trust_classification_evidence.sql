-- Preserve unknown prior classifications as unknown; never invent historical decisions.
ALTER TABLE trust_case_events ADD COLUMN category TEXT;
ALTER TABLE trust_case_events ADD COLUMN severity TEXT;
