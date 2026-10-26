-- Step-up issues a fresh token version, so pre-MFA bearer tokens do not inherit staff assurance.
ALTER TABLE user_sessions ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;
