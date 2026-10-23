CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_user_created ON password_reset_tokens (user_id, created_at DESC);

CREATE TABLE email_verification_codes (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    code_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_email_verification_user_created ON email_verification_codes (user_id, created_at DESC);

CREATE TABLE email_outbox (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    recipient VARCHAR(320) NOT NULL,
    template_key VARCHAR(80) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_email_outbox_status CHECK (status IN ('PENDING','SENDING','SENT','FAILED')),
    CONSTRAINT chk_email_outbox_attempts CHECK (attempts >= 0)
);

CREATE INDEX idx_email_outbox_delivery ON email_outbox (status, created_at);

CREATE TABLE support_requests (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NULL,
    email VARCHAR(320) NOT NULL,
    message VARCHAR(5000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_support_requests_status CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CLOSED')),
    CONSTRAINT fk_support_requests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_support_requests_status_created ON support_requests (status, created_at);
