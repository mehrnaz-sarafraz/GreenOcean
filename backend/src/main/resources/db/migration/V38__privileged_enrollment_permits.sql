-- Provisioned only through an independently authorized operator process, never a public endpoint.
CREATE TABLE webauthn_enrollment_permits (
 token_hash VARCHAR(128) PRIMARY KEY,user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 expires_at TIMESTAMPTZ NOT NULL,used_at TIMESTAMPTZ
);
