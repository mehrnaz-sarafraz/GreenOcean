-- Additive E/F policy. Unknown historical clients receive the stricter web bounds.
ALTER TABLE user_sessions ADD COLUMN client_type VARCHAR(10) NOT NULL DEFAULT 'LEGACY' CHECK(client_type IN ('LEGACY','WEB','MOBILE'));
ALTER TABLE user_sessions ADD COLUMN session_scope VARCHAR(12) NOT NULL DEFAULT 'MEMBER' CHECK(session_scope IN ('MEMBER','PENDING','PRIVILEGED'));
ALTER TABLE user_sessions ADD COLUMN overall_expires_at TIMESTAMPTZ;
ALTER TABLE user_sessions ADD COLUMN last_activity_at TIMESTAMPTZ;
ALTER TABLE user_sessions ADD COLUMN idle_expires_at TIMESTAMPTZ;
ALTER TABLE user_sessions ADD COLUMN reauthenticated_at TIMESTAMPTZ;
ALTER TABLE user_sessions ADD COLUMN mfa_authenticated_at TIMESTAMPTZ;
UPDATE user_sessions s SET overall_expires_at=LEAST(s.expires_at,s.created_at+INTERVAL '24 hours'),
 last_activity_at=COALESCE(s.last_used_at,s.created_at),
 idle_expires_at=LEAST(s.expires_at,COALESCE(s.last_used_at,s.created_at)+INTERVAL '30 minutes'),
 session_scope=CASE WHEN u.status='PENDING' THEN 'PENDING' WHEN EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.name IN ('ADMIN','MODERATOR')) THEN 'PRIVILEGED' ELSE 'MEMBER' END
 FROM users u WHERE u.id=s.user_id;
UPDATE user_sessions SET overall_expires_at=LEAST(overall_expires_at,created_at+INTERVAL '12 hours') WHERE session_scope='PRIVILEGED';
UPDATE user_sessions SET overall_expires_at=LEAST(overall_expires_at,created_at+INTERVAL '1 hour'),idle_expires_at=LEAST(idle_expires_at,last_activity_at+INTERVAL '15 minutes') WHERE session_scope='PENDING';
UPDATE user_sessions SET expires_at=LEAST(expires_at,overall_expires_at),idle_expires_at=LEAST(idle_expires_at,overall_expires_at);
ALTER TABLE user_sessions ALTER COLUMN overall_expires_at SET NOT NULL;
ALTER TABLE user_sessions ALTER COLUMN last_activity_at SET NOT NULL;
ALTER TABLE user_sessions ALTER COLUMN idle_expires_at SET NOT NULL;
-- Defaults preserve compatibility for old direct insert clients, with strict bounded expiry.
ALTER TABLE user_sessions ALTER COLUMN overall_expires_at SET DEFAULT (CURRENT_TIMESTAMP+INTERVAL '24 hours');
ALTER TABLE user_sessions ALTER COLUMN last_activity_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE user_sessions ALTER COLUMN idle_expires_at SET DEFAULT (CURRENT_TIMESTAMP+INTERVAL '30 minutes');
CREATE INDEX idx_sessions_boundaries ON user_sessions(user_id,overall_expires_at,idle_expires_at) WHERE revoked_at IS NULL;
CREATE INDEX idx_security_audit_owner_time ON audit_logs(actor_user_id,created_at DESC) WHERE entity_type='SECURITY';
CREATE TABLE webauthn_credentials (
 credential_id TEXT PRIMARY KEY CHECK(length(credential_id)<=2048),user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 public_key_cose TEXT NOT NULL,signature_count BIGINT NOT NULL DEFAULT 0,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 label VARCHAR(80) NOT NULL,revoked_at TIMESTAMPTZ
);
CREATE INDEX idx_webauthn_owner ON webauthn_credentials(user_id);
CREATE TABLE webauthn_ceremonies (
 id UUID PRIMARY KEY DEFAULT uuidv7(),user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 session_id UUID NOT NULL REFERENCES user_sessions(id) ON DELETE CASCADE,purpose VARCHAR(15) NOT NULL CHECK(purpose IN ('REGISTER','AUTHENTICATE')),
 request_json TEXT NOT NULL,expires_at TIMESTAMPTZ NOT NULL,used_at TIMESTAMPTZ
);
CREATE INDEX idx_webauthn_ceremony_expiry ON webauthn_ceremonies(expires_at);
