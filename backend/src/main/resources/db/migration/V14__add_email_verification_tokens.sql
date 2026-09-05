CREATE TABLE email_verification_tokens (
                                           id UUID PRIMARY KEY DEFAULT uuidv7(),

                                           user_id UUID NOT NULL,

                                           token_hash VARCHAR(64) NOT NULL,

                                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                           expires_at TIMESTAMPTZ NOT NULL,

                                           consumed_at TIMESTAMPTZ NULL,

                                           CONSTRAINT fk_email_verification_tokens_user
                                               FOREIGN KEY (user_id)
                                                   REFERENCES users(id)
                                                   ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_email_verification_tokens_token_hash
    ON email_verification_tokens(token_hash);

CREATE INDEX idx_email_verification_tokens_user_id
    ON email_verification_tokens(user_id);

CREATE INDEX idx_email_verification_tokens_expires_at
    ON email_verification_tokens(expires_at);