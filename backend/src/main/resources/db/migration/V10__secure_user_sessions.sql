ALTER TABLE user_sessions
    ADD CONSTRAINT uq_user_sessions_refresh_token_hash UNIQUE (refresh_token_hash);

DROP INDEX idx_user_sessions_refresh_token_hash;

CREATE INDEX idx_user_sessions_active_expiry
    ON user_sessions (expires_at)
    WHERE revoked_at IS NULL;
