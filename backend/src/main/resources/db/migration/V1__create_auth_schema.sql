-- =========================================================
-- GreenOcean V1
-- AUTH schema
-- =========================================================


-- =========================================================
-- 1. USERS
-- Account / Authentication Identity
-- =========================================================

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuidv7(),

                       email VARCHAR(320) NOT NULL,

                       password_hash VARCHAR(255) NOT NULL,

                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                       email_verified BOOLEAN NOT NULL DEFAULT FALSE,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       last_login_at TIMESTAMPTZ NULL,

                       deleted_at TIMESTAMPTZ NULL,

                       CONSTRAINT chk_users_status
                           CHECK (
                               status IN (
                                          'ACTIVE',
                                          'SUSPENDED',
                                          'PENDING',
                                          'DELETED'
                                   )
                               )
);


-- Case-insensitive unique email
CREATE UNIQUE INDEX uq_users_email_lower
    ON users (LOWER(email));


-- Query support for status
CREATE INDEX idx_users_status
    ON users (status);


-- =========================================================
-- 2. PROFILES
-- =========================================================

CREATE TABLE profiles (
                          user_id UUID PRIMARY KEY,

                          username VARCHAR(30) NOT NULL,

                          display_name VARCHAR(80) NOT NULL,

                          bio VARCHAR(500) NULL,

                          avatar_url TEXT NULL,

                          country_code CHAR(2) NULL,

                          city VARCHAR(100) NULL,

                          birth_year SMALLINT NULL,

                          is_profile_private BOOLEAN NOT NULL DEFAULT FALSE,

                          show_location BOOLEAN NOT NULL DEFAULT FALSE,

                          show_birth_year BOOLEAN NOT NULL DEFAULT FALSE,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_profiles_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_profiles_country_code
                              CHECK (
                                  country_code IS NULL
                                      OR country_code ~ '^[A-Z]{2}$'
),

    CONSTRAINT chk_profiles_birth_year
        CHECK (
            birth_year IS NULL
            OR birth_year BETWEEN 1900 AND 2100
        )
);


-- Case-insensitive unique username
CREATE UNIQUE INDEX uq_profiles_username_lower
    ON profiles (LOWER(username));


-- =========================================================
-- 3. USER SESSIONS
-- =========================================================

CREATE TABLE user_sessions (
                               id UUID PRIMARY KEY DEFAULT uuidv7(),

                               user_id UUID NOT NULL,

                               refresh_token_hash VARCHAR(255) NOT NULL,

                               device_name VARCHAR(150) NULL,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               expires_at TIMESTAMPTZ NOT NULL,

                               last_used_at TIMESTAMPTZ NULL,

                               revoked_at TIMESTAMPTZ NULL,

                               CONSTRAINT fk_user_sessions_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE
);


CREATE INDEX idx_user_sessions_user_id
    ON user_sessions (user_id);


CREATE INDEX idx_user_sessions_refresh_token_hash
    ON user_sessions (refresh_token_hash);


-- =========================================================
-- 4. ROLES
-- =========================================================

CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT uuidv7(),

                       name VARCHAR(30) NOT NULL,

                       CONSTRAINT uq_roles_name
                           UNIQUE (name)
);


-- =========================================================
-- 5. USER ROLES
-- =========================================================

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,

                            role_id UUID NOT NULL,

                            CONSTRAINT pk_user_roles
                                PRIMARY KEY (user_id, role_id),

                            CONSTRAINT fk_user_roles_user
                                FOREIGN KEY (user_id)
                                    REFERENCES users(id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_user_roles_role
                                FOREIGN KEY (role_id)
                                    REFERENCES roles(id)
                                    ON DELETE RESTRICT
);