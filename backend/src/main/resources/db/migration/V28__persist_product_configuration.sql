-- Persist account-scoped product state and administrator-managed option catalogs.

ALTER TABLE user_preferences
    ADD COLUMN onboarding_complete BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN intended_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    ADD CONSTRAINT chk_user_preferences_intended_role
        CHECK (intended_role IN ('MEMBER', 'PROFESSIONAL'));

CREATE TABLE user_search_history (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    term VARCHAR(100) NOT NULL,
    searched_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_search_history_term CHECK (LENGTH(BTRIM(term)) BETWEEN 2 AND 100),
    CONSTRAINT fk_user_search_history_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX uq_user_search_history_term
    ON user_search_history (user_id, LOWER(term));

CREATE INDEX idx_user_search_history_recent
    ON user_search_history (user_id, searched_at DESC);

CREATE TABLE catalog_options (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    option_group VARCHAR(60) NOT NULL,
    option_key VARCHAR(80) NOT NULL,
    label VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    icon VARCHAR(60) NULL,
    accent VARCHAR(20) NULL,
    soft_accent VARCHAR(20) NULL,
    sort_order SMALLINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_catalog_options_group_key UNIQUE (option_group, option_key),
    CONSTRAINT chk_catalog_options_group CHECK (option_group ~ '^[A-Z][A-Z0-9_]*$'),
    CONSTRAINT chk_catalog_options_key CHECK (option_key ~ '^[A-Za-z0-9][A-Za-z0-9_+x-]*$'),
    CONSTRAINT chk_catalog_options_label CHECK (LENGTH(BTRIM(label)) > 0)
);

CREATE INDEX idx_catalog_options_group_order
    ON catalog_options (option_group, is_active, sort_order, label);

CREATE TRIGGER trg_catalog_options_set_updated_at
    BEFORE UPDATE ON catalog_options
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

INSERT INTO catalog_options (option_group, option_key, label, description, icon, accent, soft_accent, sort_order) VALUES
('CATEGORY_GROUP', 'EMOTION', 'An emotion', 'How I feel right now', 'mood', '#4E8CB8', '#EAF4FA', 10),
('CATEGORY_GROUP', 'CONDITION', 'A condition', 'Symptoms or a diagnosis', 'psychology', '#6B4F8D', '#F3EEFA', 20),
('CATEGORY_GROUP', 'LIFE_EXPERIENCE', 'A life experience', 'Something I lived through', 'auto_stories', '#D56C5C', '#FDEDEA', 30),
('POST_TYPE', 'EXPERIENCE', 'Lived experience', 'Share something you have lived through.', 'auto_stories', NULL, NULL, 10),
('POST_TYPE', 'QUESTION', 'Seeking support', 'Ask the community for support or perspective.', 'help', NULL, NULL, 20),
('POST_TYPE', 'REFLECTION', 'Reflection', 'Name something you have noticed or learned.', 'lightbulb', NULL, NULL, 30),
('POST_MOOD', 'CALM', 'Calm', NULL, 'sentiment_satisfied', NULL, NULL, 10),
('POST_MOOD', 'HOPEFUL', 'Hopeful', NULL, 'wb_sunny', NULL, NULL, 20),
('POST_MOOD', 'LOW', 'Low', NULL, 'cloud', NULL, NULL, 30),
('POST_MOOD', 'ANXIOUS', 'Anxious', NULL, 'air', NULL, NULL, 40),
('POST_MOOD', 'OVERWHELMED', 'Overwhelmed', NULL, 'bolt', NULL, NULL, 50),
('JOURNAL_MOOD', 'REFLECTIVE', 'Reflective', NULL, 'edit_note', NULL, NULL, 10),
('JOURNAL_MOOD', 'HEAVY', 'Heavy', NULL, 'cloud', NULL, NULL, 20),
('JOURNAL_MOOD', 'GRATEFUL', 'Grateful', NULL, 'favorite', NULL, NULL, 30),
('JOURNAL_MOOD', 'UNCLEAR', 'Unclear', NULL, 'blur_on', NULL, NULL, 40),
('SUPPORT_STYLE', 'SHARED_EXPERIENCES', 'Shared experiences', 'Stories from people who understand', 'forum', NULL, NULL, 10),
('SUPPORT_STYLE', 'SUPPORT_GROUPS', 'Support groups', 'Moderated conversations by topic', 'groups', NULL, NULL, 20),
('SUPPORT_STYLE', 'LISTENER', 'One-to-one listener', 'Private peer support when I need it', 'hearing', NULL, NULL, 30),
('SUPPORT_STYLE', 'PROFESSIONAL_KNOWLEDGE', 'Professional knowledge', 'Articles and verified expert perspectives', 'psychology', NULL, NULL, 40),
('LISTENER_TOPIC', 'ANXIETY', 'Anxiety', NULL, 'air', NULL, NULL, 10),
('LISTENER_TOPIC', 'WORK_STRESS', 'Work stress', NULL, 'work', NULL, NULL, 20),
('LISTENER_TOPIC', 'SLEEP', 'Sleep', NULL, 'bedtime', NULL, NULL, 30),
('LISTENER_TOPIC', 'LONELINESS', 'Loneliness', NULL, 'person', NULL, NULL, 40),
('LISTENER_TOPIC', 'GRIEF', 'Grief', NULL, 'spa', NULL, NULL, 50),
('LISTENER_TOPIC', 'RELATIONSHIPS', 'Relationships', NULL, 'favorite', NULL, NULL, 60),
('LISTENER_AVAILABILITY', 'HOURS_1_2', '1-2 hours/week', NULL, 'schedule', NULL, NULL, 10),
('LISTENER_AVAILABILITY', 'HOURS_2_4', '2-4 hours/week', NULL, 'schedule', NULL, NULL, 20),
('LISTENER_AVAILABILITY', 'HOURS_5_PLUS', '5+ hours/week', NULL, 'schedule', NULL, NULL, 30),
('REPORT_REASON', 'HARASSMENT', 'Harassment or bullying', NULL, 'report', NULL, NULL, 10),
('REPORT_REASON', 'MISINFORMATION', 'Dangerous or misleading advice', NULL, 'fact_check', NULL, NULL, 20),
('REPORT_REASON', 'HATE_SPEECH', 'Hate or discrimination', NULL, 'block', NULL, NULL, 30),
('REPORT_REASON', 'SPAM', 'Spam or scam', NULL, 'warning', NULL, NULL, 40),
('REPORT_REASON', 'SELF_HARM_CONTENT', 'Self-harm risk', NULL, 'health_and_safety', NULL, NULL, 50),
('REPORT_REASON', 'OTHER', 'Other safety concern', NULL, 'more_horiz', NULL, NULL, 60),
('GOAL_CADENCE', 'DAILY', 'Daily', NULL, 'today', NULL, NULL, 10),
('GOAL_CADENCE', 'THREE_TIMES_WEEKLY', '3x weekly', NULL, 'date_range', NULL, NULL, 20),
('GOAL_CADENCE', 'WEEKLY', 'Weekly', NULL, 'calendar_month', NULL, NULL, 30),
('ASSESSMENT_RESPONSE', 'NOT_AT_ALL', 'Not at all', NULL, NULL, NULL, NULL, 0),
('ASSESSMENT_RESPONSE', 'SOME_DAYS', 'Some days', NULL, NULL, NULL, NULL, 1),
('ASSESSMENT_RESPONSE', 'OFTEN', 'Often', NULL, NULL, NULL, NULL, 2),
('ASSESSMENT_RESPONSE', 'MOST_DAYS', 'Most days', NULL, NULL, NULL, NULL, 3),
('ASSESSMENT_RESPONSE', 'NEARLY_ALWAYS', 'Nearly always', NULL, NULL, NULL, NULL, 4),
('ASSESSMENT_QUESTION', 'PAUSE_UNDER_STRESS', 'I have felt able to pause when stress rises.', NULL, NULL, NULL, NULL, 10),
('ASSESSMENT_QUESTION', 'SOCIAL_CONNECTION', 'I have felt connected to at least one person.', NULL, NULL, NULL, NULL, 20),
('ASSESSMENT_QUESTION', 'RESTORATIVE_SLEEP', 'My sleep has felt restorative enough.', NULL, NULL, NULL, NULL, 30),
('ASSESSMENT_QUESTION', 'MANAGEABLE_RESPONSIBILITIES', 'Daily responsibilities have felt manageable.', NULL, NULL, NULL, NULL, 40),
('ASSESSMENT_QUESTION', 'INTEREST_OR_HOPE', 'I have had moments of interest or hope.', NULL, NULL, NULL, NULL, 50),
('REFLECTION_PROMPT', 'BEFORE_FEELING', 'What happened just before this feeling showed up?', NULL, NULL, NULL, NULL, 10),
('REFLECTION_PROMPT', 'FRIEND_RESPONSE', 'What would you say to a friend in the same moment?', NULL, NULL, NULL, NULL, 20),
('REFLECTION_PROMPT', 'UNDERLYING_NEED', 'What is one need underneath this reaction?', NULL, NULL, NULL, NULL, 30),
('REFLECTION_PROMPT', 'SMALL_NEXT_STEP', 'What is a small next step that does not require fixing everything?', NULL, NULL, NULL, NULL, 40),
('MEDIA_KIND', 'MOVIE', 'Movies', NULL, 'movie', NULL, NULL, 10),
('MEDIA_KIND', 'SERIES', 'Series', NULL, 'live_tv', NULL, NULL, 20),
('MEDIA_KIND', 'DOCUMENTARY', 'Documentaries', NULL, 'theaters', NULL, NULL, 30),
('PROFESSIONAL_TYPE', 'PSYCHOLOGIST', 'Psychologist', NULL, 'psychology', NULL, NULL, 10),
('PROFESSIONAL_TYPE', 'PSYCHIATRIST', 'Psychiatrist', NULL, 'medical_services', NULL, NULL, 20),
('PROFESSIONAL_TYPE', 'COUNSELOR', 'Counselor', NULL, 'support_agent', NULL, NULL, 30),
('PROFESSIONAL_TYPE', 'THERAPIST', 'Therapist', NULL, 'diversity_1', NULL, NULL, 40),
('PROFESSIONAL_TYPE', 'OTHER', 'Other licensed professional', NULL, 'badge', NULL, NULL, 50),
('GENDER', 'WOMAN', 'Woman', NULL, NULL, NULL, NULL, 10),
('GENDER', 'MAN', 'Man', NULL, NULL, NULL, NULL, 20),
('GENDER', 'NON_BINARY', 'Non-binary', NULL, NULL, NULL, NULL, 30),
('GENDER', 'PREFER_NOT_TO_SAY', 'Prefer not to say', NULL, NULL, NULL, NULL, 40),
('CONSULTATION_MODE', 'VIDEO', 'Video', NULL, 'videocam', NULL, NULL, 10),
('CONSULTATION_MODE', 'IN_PERSON', 'In-person', NULL, 'location_on', NULL, NULL, 20),
('CONSULTATION_MODE', 'PHONE', 'Phone', NULL, 'call', NULL, NULL, 30)
ON CONFLICT (option_group, option_key) DO NOTHING;

CREATE TABLE listener_training_modules (
    module_key VARCHAR(40) PRIMARY KEY,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(500) NOT NULL,
    sort_order SMALLINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_listener_training_modules_key CHECK (module_key ~ '^[a-z][a-z0-9-]*$')
);

INSERT INTO listener_training_modules (module_key, title, description, sort_order) VALUES
('boundaries', 'Listening without treating', 'Boundaries, scope, and consent', 10),
('reflection', 'Reflective listening', 'Questions, summaries, and silence', 20),
('safety', 'Safety escalation', 'Risk language and urgent routes', 30),
('culture', 'Inclusive support', 'Culture, identity, and bias', 40),
('practice', 'Supervised practice', 'Reviewed role-play conversation', 50)
ON CONFLICT (module_key) DO NOTHING;

ALTER TABLE listener_training_progress
    DROP CONSTRAINT chk_listener_training_progress_module,
    ADD CONSTRAINT fk_listener_training_progress_module FOREIGN KEY (module_key)
        REFERENCES listener_training_modules(module_key) ON DELETE RESTRICT;
