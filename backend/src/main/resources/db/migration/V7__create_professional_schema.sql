CREATE TABLE professional_profiles (
    user_id UUID PRIMARY KEY,
    professional_type VARCHAR(30) NOT NULL,
    title VARCHAR(100) NULL,
    specialization VARCHAR(200) NULL,
    bio TEXT NULL,
    years_of_experience SMALLINT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_professional_profiles_type CHECK (
        professional_type IN ('PSYCHOLOGIST', 'PSYCHIATRIST', 'COUNSELOR', 'THERAPIST', 'OTHER')
    ),
    CONSTRAINT chk_professional_profiles_verification_status CHECK (
        verification_status IN ('PENDING', 'VERIFIED', 'REJECTED', 'SUSPENDED')
    ),
    CONSTRAINT chk_professional_profiles_experience CHECK (
        years_of_experience IS NULL OR years_of_experience >= 0
    ),
    CONSTRAINT chk_professional_profiles_verified_at CHECK (
        (verification_status = 'VERIFIED' AND verified_at IS NOT NULL)
        OR (verification_status <> 'VERIFIED' AND verified_at IS NULL)
    ),
    CONSTRAINT fk_professional_profiles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TRIGGER trg_professional_profiles_set_updated_at
    BEFORE UPDATE ON professional_profiles
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TABLE professional_verifications (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    professional_user_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by UUID NULL,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMPTZ NULL,
    notes VARCHAR(1000) NULL,

    CONSTRAINT chk_professional_verifications_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT chk_professional_verifications_reviewed_at CHECK (
        (status = 'PENDING' AND reviewed_at IS NULL)
        OR (status IN ('APPROVED', 'REJECTED') AND reviewed_at IS NOT NULL)
    ),
    CONSTRAINT fk_professional_verifications_professional
        FOREIGN KEY (professional_user_id)
        REFERENCES professional_profiles(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_professional_verifications_reviewer
        FOREIGN KEY (reviewed_by)
        REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_professional_verifications_professional_submitted_at
    ON professional_verifications (professional_user_id, submitted_at DESC);
