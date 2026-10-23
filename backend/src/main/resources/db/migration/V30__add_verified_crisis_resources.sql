-- Country-aware urgent-support directory. Review these records regularly; they are operational data.

CREATE TABLE crisis_resources (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    country_code VARCHAR(10) NOT NULL,
    region_name VARCHAR(100) NOT NULL,
    title VARCHAR(160) NOT NULL,
    detail VARCHAR(500) NOT NULL,
    action_url TEXT NOT NULL,
    source_url TEXT NOT NULL,
    last_verified_on DATE NOT NULL,
    sort_order SMALLINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_crisis_resources_country CHECK (country_code ~ '^[A-Z]{2,10}$'),
    CONSTRAINT uq_crisis_resources_country_title UNIQUE (country_code, title)
);

CREATE INDEX idx_crisis_resources_country_order
    ON crisis_resources (country_code, is_active, sort_order, title);

CREATE TRIGGER trg_crisis_resources_set_updated_at
    BEFORE UPDATE ON crisis_resources
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

INSERT INTO crisis_resources
    (country_code, region_name, title, detail, action_url, source_url, last_verified_on, sort_order)
VALUES
('US', 'United States', '988 Suicide & Crisis Lifeline', 'Call or text 988; free and confidential support is available 24/7.', 'tel:988', 'https://988lifeline.org/get-help/', DATE '2026-09-13', 10),
('US', 'United States', '988 Lifeline online', 'Official chat, accessibility, Spanish-language, and service information.', 'https://988lifeline.org/get-help/', 'https://988lifeline.org/get-help/', DATE '2026-09-13', 20),
('CA', 'Canada', '9-8-8 Suicide Crisis Helpline', 'Call or text 9-8-8; English and French support is available 24/7/365.', 'tel:988', 'https://988.ca/', DATE '2026-09-13', 10),
('CA', 'Canada', '9-8-8 Canada online', 'Official service information, accessibility options, and what to expect.', 'https://988.ca/', 'https://988.ca/', DATE '2026-09-13', 20),
('GB', 'United Kingdom', 'Samaritans', 'Call 116 123 to speak with a volunteer in the UK or Ireland.', 'tel:116123', 'https://www.samaritans.org/how-we-can-help/contact-samaritan/', DATE '2026-09-13', 10),
('GB', 'United Kingdom', 'NHS urgent mental-health help', 'Use NHS 111 online or call 111 and select the mental-health option.', 'https://www.nhs.uk/nhs-services/mental-health-services/where-to-get-urgent-help-for-mental-health/', 'https://www.nhs.uk/nhs-services/mental-health-services/where-to-get-urgent-help-for-mental-health/', DATE '2026-09-13', 20),
('GLOBAL', 'Other locations', 'Befrienders Worldwide', 'Find emotional-support centres and helplines available in your location.', 'https://befrienders.org/', 'https://befrienders.org/', DATE '2026-09-13', 10)
ON CONFLICT (country_code, title) DO NOTHING;
