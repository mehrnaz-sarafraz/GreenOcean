ALTER TABLE professional_verifications ADD COLUMN verified_specialties TEXT[] NOT NULL DEFAULT '{}';
ALTER TABLE professional_profiles ADD COLUMN profile_revision BIGINT NOT NULL DEFAULT 1,ADD COLUMN service_description VARCHAR(2000) NOT NULL DEFAULT '',ADD COLUMN conditions_of_service VARCHAR(2000) NOT NULL DEFAULT '';
CREATE TABLE professional_profile_history(id UUID PRIMARY KEY DEFAULT uuidv7(),professional_id UUID NOT NULL REFERENCES professional_profiles(user_id) ON DELETE CASCADE,revision BIGINT NOT NULL,title VARCHAR(100),bio TEXT,languages TEXT[] NOT NULL,service_description VARCHAR(2000),conditions_of_service VARCHAR(2000),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(professional_id,revision));

ALTER TABLE professional_qualification_decisions ADD COLUMN evidence_snapshot JSONB NOT NULL DEFAULT '{}';
