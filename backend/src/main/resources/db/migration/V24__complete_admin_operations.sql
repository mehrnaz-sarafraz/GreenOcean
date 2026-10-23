ALTER TABLE support_requests
    ADD COLUMN assigned_to UUID NULL,
    ADD COLUMN resolution_note VARCHAR(2000) NULL,
    ADD CONSTRAINT fk_support_requests_assignee FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE policy_documents
    ADD COLUMN content TEXT NULL,
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_by UUID NULL,
    ADD CONSTRAINT fk_policy_documents_updater FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE professional_application_documents
    ADD COLUMN scanned_at TIMESTAMPTZ NULL,
    ADD COLUMN reviewed_by UUID NULL,
    ADD CONSTRAINT fk_professional_application_documents_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE TRIGGER trg_policy_documents_set_updated_at
    BEFORE UPDATE ON policy_documents FOR EACH ROW EXECUTE FUNCTION set_updated_at();
