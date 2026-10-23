-- Complete the product workflows used by the GreenOcean application.

ALTER TABLE user_preferences
    ADD COLUMN notify_messages BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notify_replies BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notify_follows BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notify_groups BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN notify_articles BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN quiet_hours_enabled BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE professional_reviews (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    professional_user_id UUID NOT NULL,
    reviewer_user_id UUID NOT NULL,
    rating SMALLINT NOT NULL,
    body VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_professional_reviews_reviewer UNIQUE (professional_user_id, reviewer_user_id),
    CONSTRAINT chk_professional_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_professional_reviews_body CHECK (LENGTH(BTRIM(body)) >= 20),
    CONSTRAINT chk_professional_reviews_status CHECK (status IN ('PENDING', 'PUBLISHED', 'REJECTED')),
    CONSTRAINT chk_professional_reviews_self CHECK (professional_user_id <> reviewer_user_id),
    CONSTRAINT fk_professional_reviews_professional FOREIGN KEY (professional_user_id)
        REFERENCES professional_profiles(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_professional_reviews_reviewer FOREIGN KEY (reviewer_user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_professional_reviews_professional_created
    ON professional_reviews (professional_user_id, created_at DESC)
    WHERE status = 'PUBLISHED';

CREATE TRIGGER trg_professional_reviews_set_updated_at
    BEFORE UPDATE ON professional_reviews
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE consultation_requests (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    requester_user_id UUID NOT NULL,
    professional_user_id UUID NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    consultation_mode VARCHAR(80) NOT NULL,
    note VARCHAR(1000) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_consultation_requests_status CHECK (
        status IN ('REQUESTED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'DECLINED')
    ),
    CONSTRAINT chk_consultation_requests_mode CHECK (LENGTH(BTRIM(consultation_mode)) > 0),
    CONSTRAINT chk_consultation_requests_self CHECK (requester_user_id <> professional_user_id),
    CONSTRAINT fk_consultation_requests_requester FOREIGN KEY (requester_user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_consultation_requests_professional FOREIGN KEY (professional_user_id)
        REFERENCES professional_profiles(user_id) ON DELETE RESTRICT
);

CREATE INDEX idx_consultation_requests_requester_created
    ON consultation_requests (requester_user_id, created_at DESC);

CREATE INDEX idx_consultation_requests_professional_schedule
    ON consultation_requests (professional_user_id, scheduled_at ASC);

CREATE TRIGGER trg_consultation_requests_set_updated_at
    BEFORE UPDATE ON consultation_requests
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE wellbeing_check_ins (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    mood VARCHAR(40) NOT NULL,
    note VARCHAR(1000) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_wellbeing_check_ins_mood CHECK (LENGTH(BTRIM(mood)) > 0),
    CONSTRAINT fk_wellbeing_check_ins_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_wellbeing_check_ins_user_created
    ON wellbeing_check_ins (user_id, created_at DESC);

CREATE TABLE wellbeing_journal_entries (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    body TEXT NOT NULL,
    mood VARCHAR(40) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_wellbeing_journal_title CHECK (LENGTH(BTRIM(title)) > 0),
    CONSTRAINT chk_wellbeing_journal_body CHECK (LENGTH(BTRIM(body)) > 0),
    CONSTRAINT fk_wellbeing_journal_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_wellbeing_journal_user_created
    ON wellbeing_journal_entries (user_id, created_at DESC);

CREATE TRIGGER trg_wellbeing_journal_set_updated_at
    BEFORE UPDATE ON wellbeing_journal_entries
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE wellbeing_goals (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    cadence VARCHAR(40) NOT NULL,
    progress SMALLINT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_wellbeing_goals_title CHECK (LENGTH(BTRIM(title)) > 0),
    CONSTRAINT chk_wellbeing_goals_progress CHECK (progress BETWEEN 0 AND 100),
    CONSTRAINT fk_wellbeing_goals_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_wellbeing_goals_user_created
    ON wellbeing_goals (user_id, created_at DESC);

CREATE TRIGGER trg_wellbeing_goals_set_updated_at
    BEFORE UPDATE ON wellbeing_goals
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE listener_profiles (
    user_id UUID PRIMARY KEY,
    display_title VARCHAR(100) NOT NULL DEFAULT 'Trained listener',
    listening_style VARCHAR(100) NOT NULL,
    languages TEXT[] NOT NULL DEFAULT '{}',
    topics TEXT[] NOT NULL DEFAULT '{}',
    completed_sessions INTEGER NOT NULL DEFAULT 0,
    safety_score SMALLINT NOT NULL DEFAULT 100,
    online BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_listener_profiles_sessions CHECK (completed_sessions >= 0),
    CONSTRAINT chk_listener_profiles_score CHECK (safety_score BETWEEN 0 AND 100),
    CONSTRAINT chk_listener_profiles_status CHECK (status IN ('ACTIVE', 'PAUSED', 'SUSPENDED')),
    CONSTRAINT fk_listener_profiles_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_listener_profiles_set_updated_at
    BEFORE UPDATE ON listener_profiles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE listener_applications (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    motivation VARCHAR(2000) NOT NULL,
    experience VARCHAR(2000) NOT NULL,
    weekly_availability VARCHAR(80) NOT NULL,
    accepted_code BOOLEAN NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMPTZ NULL,
    reviewed_by UUID NULL,
    review_note VARCHAR(1000) NULL,
    CONSTRAINT chk_listener_applications_code CHECK (accepted_code),
    CONSTRAINT chk_listener_applications_status CHECK (
        status IN ('SUBMITTED', 'IN_REVIEW', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT fk_listener_applications_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_listener_applications_reviewer FOREIGN KEY (reviewed_by)
        REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_listener_applications_status_submitted
    ON listener_applications (status, submitted_at ASC);

ALTER TABLE reports
    ADD COLUMN conversation_id UUID NULL,
    ADD COLUMN community_id UUID NULL,
    DROP CONSTRAINT chk_reports_exactly_one_target,
    ADD CONSTRAINT fk_reports_conversation FOREIGN KEY (conversation_id)
        REFERENCES conversations(id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_reports_community FOREIGN KEY (community_id)
        REFERENCES communities(id) ON DELETE RESTRICT,
    ADD CONSTRAINT chk_reports_exactly_one_target CHECK (
        num_nonnulls(target_user_id, post_id, comment_id, conversation_id, community_id) = 1
    );

CREATE TABLE moderation_appeals (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    appellant_user_id UUID NOT NULL,
    report_id UUID NULL,
    reason VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    decision_note VARCHAR(1000) NULL,
    decided_by UUID NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_moderation_appeals_status CHECK (status IN ('OPEN', 'REVIEWING', 'UPHELD', 'OVERTURNED')),
    CONSTRAINT fk_moderation_appeals_user FOREIGN KEY (appellant_user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_moderation_appeals_report FOREIGN KEY (report_id)
        REFERENCES reports(id) ON DELETE SET NULL,
    CONSTRAINT fk_moderation_appeals_decider FOREIGN KEY (decided_by)
        REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE promotion_campaigns (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    professional_user_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    impressions BIGINT NOT NULL DEFAULT 0,
    clicks BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_promotion_campaigns_dates CHECK (ends_at > starts_at),
    CONSTRAINT chk_promotion_campaigns_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'ACTIVE', 'PAUSED', 'COMPLETED')),
    CONSTRAINT chk_promotion_campaigns_metrics CHECK (impressions >= 0 AND clicks >= 0 AND clicks <= impressions),
    CONSTRAINT fk_promotion_campaigns_professional FOREIGN KEY (professional_user_id)
        REFERENCES professional_profiles(user_id) ON DELETE CASCADE
);

CREATE TABLE policy_documents (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    policy_key VARCHAR(80) NOT NULL,
    version INTEGER NOT NULL,
    title VARCHAR(180) NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_policy_documents_key_version UNIQUE (policy_key, version),
    CONSTRAINT chk_policy_documents_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'RETIRED'))
);

CREATE TABLE export_jobs (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    requested_by UUID NOT NULL,
    export_type VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    row_count BIGINT NULL,
    expires_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ NULL,
    CONSTRAINT chk_export_jobs_status CHECK (status IN ('QUEUED', 'RUNNING', 'COMPLETED', 'FAILED', 'EXPIRED')),
    CONSTRAINT fk_export_jobs_requester FOREIGN KEY (requested_by)
        REFERENCES users(id) ON DELETE RESTRICT
);

INSERT INTO listener_profiles (user_id, listening_style, languages, topics, completed_sessions, safety_score, online)
VALUES
    ('01910000-0000-7000-8000-000000000001', 'Reflective and calm', ARRAY['English','Spanish'], ARRAY['Anxiety','Recovery','Work stress'], 426, 98, TRUE),
    ('01910000-0000-7000-8000-000000000002', 'Practical and validating', ARRAY['English'], ARRAY['Burnout','Relationships'], 311, 97, TRUE),
    ('01910000-0000-7000-8000-000000000003', 'Gentle and patient', ARRAY['English','Mandarin'], ARRAY['Sleep','Loneliness'], 284, 99, FALSE)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO promotion_campaigns (id, professional_user_id, title, starts_at, ends_at, status, impressions, clicks)
VALUES
    ('0191d000-0000-7000-8000-000000000001', '01910000-0000-7000-8000-000000000004', 'Anxiety support spotlight', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '5 days', 'ACTIVE', 18420, 632),
    ('0191d000-0000-7000-8000-000000000002', '01910000-0000-7000-8000-000000000005', 'Sleep education spotlight', CURRENT_TIMESTAMP + INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '9 days', 'SCHEDULED', 0, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO policy_documents (id, policy_key, version, title, summary, status, published_at)
VALUES
    ('0191e000-0000-7000-8000-000000000001', 'community-guidelines', 1, 'Community guidelines', 'Safety, privacy, respectful participation, and prohibited conduct.', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '30 days'),
    ('0191e000-0000-7000-8000-000000000002', 'professional-content', 1, 'Professional content policy', 'Evidence, disclosure, advertising, boundaries, and credential requirements.', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '20 days'),
    ('0191e000-0000-7000-8000-000000000003', 'listener-code', 1, 'Listener code of conduct', 'Training, scope, escalation, confidentiality, and safeguarding.', 'PUBLISHED', CURRENT_TIMESTAMP - INTERVAL '15 days')
ON CONFLICT (id) DO NOTHING;
