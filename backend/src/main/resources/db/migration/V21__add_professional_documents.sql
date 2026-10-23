CREATE TABLE professional_application_documents (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    application_id UUID NOT NULL,
    original_name VARCHAR(240) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    byte_size BIGINT NOT NULL,
    sha256 CHAR(64) NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    scan_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_professional_application_documents_storage UNIQUE (storage_key),
    CONSTRAINT chk_professional_application_documents_size CHECK (byte_size > 0 AND byte_size <= 10485760),
    CONSTRAINT chk_professional_application_documents_scan CHECK (scan_status IN ('PENDING', 'CLEAN', 'REJECTED')),
    CONSTRAINT fk_professional_application_documents_application FOREIGN KEY (application_id)
        REFERENCES professional_verifications(id) ON DELETE CASCADE
);

CREATE INDEX idx_professional_application_documents_application
    ON professional_application_documents (application_id, uploaded_at);
