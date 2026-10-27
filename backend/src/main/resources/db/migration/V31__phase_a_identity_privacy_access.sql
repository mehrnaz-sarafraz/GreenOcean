-- Additive Phase A contract. Existing accounts retain access during a configurable grace period.
ALTER TABLE users ADD COLUMN phase_a_enrolled BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ALTER COLUMN status SET DEFAULT 'PENDING';
ALTER TABLE profiles ALTER COLUMN is_profile_private SET DEFAULT TRUE;
ALTER TABLE user_preferences ALTER COLUMN allow_message_requests SET DEFAULT FALSE;

CREATE TABLE geography_countries (
 code CHAR(2) PRIMARY KEY CHECK (code ~ '^[A-Z]{2}$'),
 name VARCHAR(160) NOT NULL, local_names JSONB NOT NULL DEFAULT '{}',
 catalog_version VARCHAR(80) NOT NULL
);
CREATE TABLE geography_subdivisions (
 code VARCHAR(16) PRIMARY KEY, country_code CHAR(2) NOT NULL REFERENCES geography_countries(code),
 name VARCHAR(160) NOT NULL, local_names JSONB NOT NULL DEFAULT '{}',
 catalog_version VARCHAR(80) NOT NULL, UNIQUE(code, country_code)
);
CREATE TABLE geography_cities (
 geonames_id BIGINT PRIMARY KEY CHECK(geonames_id > 0), country_code CHAR(2) NOT NULL REFERENCES geography_countries(code),
 subdivision_code VARCHAR(16), name VARCHAR(200) NOT NULL, local_names JSONB NOT NULL DEFAULT '{}',
 catalog_version VARCHAR(80) NOT NULL,
 FOREIGN KEY(subdivision_code,country_code) REFERENCES geography_subdivisions(code,country_code),
 UNIQUE(geonames_id,country_code)
);
CREATE INDEX idx_geography_cities_country ON geography_cities(country_code, subdivision_code);
CREATE TABLE regional_services (
 country_code CHAR(2) NOT NULL REFERENCES geography_countries(code),
 service VARCHAR(40) NOT NULL CHECK(service IN ('COMMUNITY','LISTENER','PROFESSIONAL_DIRECTORY','PROFESSIONAL_CARE','URGENT_RESOURCES')),
 status VARCHAR(20) NOT NULL CHECK(status IN ('AVAILABLE','UNAVAILABLE','LIMITED','UNDER_REVIEW')),
 reason VARCHAR(500), PRIMARY KEY(country_code,service)
);
CREATE TABLE account_eligibility (
 user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 age_result VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN' CHECK(age_result IN ('UNKNOWN','ADULT','INELIGIBLE')),
 age_method VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN' CHECK(age_method IN ('UNKNOWN','SELF_ATTESTED','INDEPENDENT')),
 checked_at TIMESTAMPTZ,
 residence_country CHAR(2) REFERENCES geography_countries(code),
 current_country CHAR(2) REFERENCES geography_countries(code),
 preferred_language VARCHAR(16), city_id BIGINT REFERENCES geography_cities(geonames_id),
 display_reviewed_at TIMESTAMPTZ,
 CHECK((age_result = 'UNKNOWN' AND age_method = 'UNKNOWN') OR (age_result <> 'UNKNOWN' AND age_method <> 'UNKNOWN'))
);
CREATE TABLE policy_acceptances (
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 policy_key VARCHAR(80) NOT NULL, version INTEGER NOT NULL,
 accepted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(user_id,policy_key,version),
 FOREIGN KEY(policy_key,version) REFERENCES policy_documents(policy_key,version)
);
CREATE TABLE account_deletions (
 user_id UUID PRIMARY KEY REFERENCES users(id),
 status VARCHAR(30) NOT NULL CHECK(status IN ('REQUESTED','PROCESSING','COMPLETED','FAILED_RETRYABLE','ON_HOLD')),
 requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, completed_at TIMESTAMPTZ
);
CREATE TABLE retired_refresh_tokens (
 token_hash VARCHAR(128) PRIMARY KEY, session_id UUID NOT NULL REFERENCES user_sessions(id) ON DELETE CASCADE,
 expires_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_retired_refresh_expiry ON retired_refresh_tokens(expires_at);
CREATE TABLE auth_rate_windows (
 key_hash VARCHAR(128) PRIMARY KEY, attempts INTEGER NOT NULL, expires_at TIMESTAMPTZ NOT NULL
);
ALTER TABLE email_outbox ADD COLUMN expires_at TIMESTAMPTZ NOT NULL DEFAULT (CURRENT_TIMESTAMP + INTERVAL '30 minutes');
CREATE TABLE account_file_erasures (
 user_id UUID NOT NULL REFERENCES account_deletions(user_id), storage_key TEXT NOT NULL,
 completed_at TIMESTAMPTZ, attempts INTEGER NOT NULL DEFAULT 0,
 PRIMARY KEY(user_id,storage_key)
);
-- The delivery worker encrypts all new secrets and purges payloads after delivery/expiry.
