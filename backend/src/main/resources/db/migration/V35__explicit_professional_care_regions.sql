-- A verified title is not a license to provide care in every country.
-- Deliberately empty: approved scopes must come from an authorized human credential review.
CREATE TABLE professional_care_regions (
 professional_user_id UUID NOT NULL REFERENCES professional_profiles(user_id) ON DELETE CASCADE,
 country_code CHAR(2) NOT NULL REFERENCES geography_countries(code),
 approved_by UUID NOT NULL REFERENCES users(id),
 approved_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 expires_at TIMESTAMPTZ NOT NULL CHECK(expires_at > approved_at),
 PRIMARY KEY(professional_user_id,country_code)
);
