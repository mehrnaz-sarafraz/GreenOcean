-- Align the persisted model with the GreenOcean mobile application.

CREATE TABLE support_categories (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    slug VARCHAR(80) NOT NULL UNIQUE,
    category_group VARCHAR(30) NOT NULL,
    name VARCHAR(80) NOT NULL,
    description VARCHAR(240) NOT NULL,
    icon VARCHAR(60) NOT NULL,
    color VARCHAR(20) NOT NULL,
    soft_color VARCHAR(20) NOT NULL,
    sort_order SMALLINT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_support_categories_group
        CHECK (category_group IN ('EMOTION', 'CONDITION', 'LIFE_EXPERIENCE')),
    CONSTRAINT chk_support_categories_slug
        CHECK (slug = LOWER(slug) AND slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$')
);

ALTER TABLE posts
    ADD COLUMN category_id UUID NULL,
    ADD COLUMN post_type VARCHAR(20) NOT NULL DEFAULT 'EXPERIENCE',
    ADD COLUMN mood VARCHAR(30) NULL,
    ADD CONSTRAINT fk_posts_category
        FOREIGN KEY (category_id) REFERENCES support_categories(id) ON DELETE RESTRICT,
    ADD CONSTRAINT chk_posts_type
        CHECK (post_type IN ('EXPERIENCE', 'QUESTION', 'REFLECTION'));

CREATE INDEX idx_posts_category_created_at
    ON posts (category_id, created_at DESC)
    WHERE status = 'PUBLISHED';

ALTER TABLE professional_profiles
    ADD COLUMN specialties TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN rating NUMERIC(3,2) NOT NULL DEFAULT 0,
    ADD COLUMN review_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN green_ocean_score SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN languages TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN gender VARCHAR(40) NULL,
    ADD COLUMN country VARCHAR(100) NULL,
    ADD COLUMN city VARCHAR(100) NULL,
    ADD COLUMN workplace VARCHAR(180) NULL,
    ADD COLUMN clinic_name VARCHAR(180) NULL,
    ADD COLUMN clinic_address VARCHAR(300) NULL,
    ADD COLUMN education TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN license_number VARCHAR(100) NULL,
    ADD COLUMN consultation_modes TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN accepting_new_clients BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN promoted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN promoted_reason VARCHAR(240) NULL,
    ADD CONSTRAINT chk_professional_profiles_rating CHECK (rating BETWEEN 0 AND 5),
    ADD CONSTRAINT chk_professional_profiles_review_count CHECK (review_count >= 0),
    ADD CONSTRAINT chk_professional_profiles_score CHECK (green_ocean_score BETWEEN 0 AND 100);

CREATE TABLE professional_answers (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    post_id UUID NOT NULL,
    professional_user_id UUID NOT NULL,
    body TEXT NOT NULL,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_professional_answers_post UNIQUE (post_id),
    CONSTRAINT chk_professional_answers_body CHECK (LENGTH(BTRIM(body)) > 0),
    CONSTRAINT chk_professional_answers_helpful CHECK (helpful_count >= 0),
    CONSTRAINT fk_professional_answers_post
        FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_professional_answers_professional
        FOREIGN KEY (professional_user_id) REFERENCES professional_profiles(user_id) ON DELETE RESTRICT
);

CREATE TABLE professional_articles (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    author_id UUID NOT NULL,
    title VARCHAR(220) NOT NULL,
    summary VARCHAR(600) NOT NULL,
    topic VARCHAR(80) NOT NULL,
    read_time_minutes SMALLINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_REVIEW',
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    evidence_level VARCHAR(80) NOT NULL,
    sections JSONB NOT NULL DEFAULT '[]'::jsonb,
    takeaways JSONB NOT NULL DEFAULT '[]'::jsonb,
    reference_list JSONB NOT NULL DEFAULT '[]'::jsonb,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    published_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_professional_articles_status
        CHECK (status IN ('DRAFT', 'IN_REVIEW', 'PUBLISHED', 'REJECTED')),
    CONSTRAINT chk_professional_articles_read_time CHECK (read_time_minutes BETWEEN 1 AND 180),
    CONSTRAINT chk_professional_articles_helpful CHECK (helpful_count >= 0),
    CONSTRAINT chk_professional_articles_published
        CHECK ((status = 'PUBLISHED' AND published_at IS NOT NULL) OR status <> 'PUBLISHED'),
    CONSTRAINT chk_professional_articles_sections_array CHECK (jsonb_typeof(sections) = 'array'),
    CONSTRAINT chk_professional_articles_takeaways_array CHECK (jsonb_typeof(takeaways) = 'array'),
    CONSTRAINT chk_professional_articles_references_array CHECK (jsonb_typeof(reference_list) = 'array'),
    CONSTRAINT fk_professional_articles_author
        FOREIGN KEY (author_id) REFERENCES professional_profiles(user_id) ON DELETE RESTRICT
);

CREATE INDEX idx_professional_articles_published
    ON professional_articles (pinned DESC, published_at DESC)
    WHERE status = 'PUBLISHED';

CREATE TRIGGER trg_professional_articles_set_updated_at
    BEFORE UPDATE ON professional_articles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE article_helpful_reactions (
    article_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_article_helpful_reactions PRIMARY KEY (article_id, user_id),
    CONSTRAINT fk_article_helpful_article
        FOREIGN KEY (article_id) REFERENCES professional_articles(id) ON DELETE CASCADE,
    CONSTRAINT fk_article_helpful_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE media_recommendations (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    title VARCHAR(180) NOT NULL,
    media_kind VARCHAR(20) NOT NULL,
    release_year SMALLINT NOT NULL,
    duration_label VARCHAR(40) NOT NULL,
    theme VARCHAR(180) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    discussion_prompt VARCHAR(500) NOT NULL,
    content_notes TEXT[] NOT NULL DEFAULT '{}',
    recommended_by VARCHAR(180) NOT NULL,
    accent VARCHAR(20) NOT NULL,
    soft_accent VARCHAR(20) NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_media_recommendations_kind CHECK (media_kind IN ('MOVIE', 'SERIES', 'DOCUMENTARY')),
    CONSTRAINT chk_media_recommendations_year CHECK (release_year BETWEEN 1888 AND 2200)
);

CREATE TABLE media_saves (
    media_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_media_saves PRIMARY KEY (media_id, user_id),
    CONSTRAINT fk_media_saves_media
        FOREIGN KEY (media_id) REFERENCES media_recommendations(id) ON DELETE CASCADE,
    CONSTRAINT fk_media_saves_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_preferences (
    user_id UUID PRIMARY KEY,
    support_topics TEXT[] NOT NULL DEFAULT '{}',
    support_style VARCHAR(40) NULL,
    stronger_content_controls BOOLEAN NOT NULL DEFAULT TRUE,
    private_feed BOOLEAN NOT NULL DEFAULT TRUE,
    blur_sensitive_content BOOLEAN NOT NULL DEFAULT TRUE,
    reduce_medication_content BOOLEAN NOT NULL DEFAULT FALSE,
    allow_message_requests BOOLEAN NOT NULL DEFAULT TRUE,
    professionals_only_messages BOOLEAN NOT NULL DEFAULT FALSE,
    muted_terms TEXT[] NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_preferences_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_user_preferences_set_updated_at
    BEFORE UPDATE ON user_preferences
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

INSERT INTO user_preferences (user_id)
SELECT id FROM users
ON CONFLICT (user_id) DO NOTHING;

CREATE OR REPLACE FUNCTION create_default_user_preferences()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO user_preferences (user_id) VALUES (NEW.id) ON CONFLICT DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_create_default_preferences
    AFTER INSERT ON users
    FOR EACH ROW EXECUTE FUNCTION create_default_user_preferences();

CREATE TABLE support_channels (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(600) NOT NULL,
    category VARCHAR(100) NOT NULL,
    icon VARCHAR(60) NOT NULL,
    channel_type VARCHAR(20) NOT NULL,
    moderated BOOLEAN NOT NULL DEFAULT TRUE,
    next_event VARCHAR(180) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_support_channels_type CHECK (channel_type IN ('GROUP', 'ANNOUNCEMENT')),
    CONSTRAINT chk_support_channels_slug
        CHECK (slug = LOWER(slug) AND slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$')
);

CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    conversation_kind VARCHAR(20) NOT NULL,
    title VARCHAR(120) NOT NULL,
    subtitle VARCHAR(180) NULL,
    channel_id UUID NULL UNIQUE,
    professional_user_id UUID NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_conversations_kind CHECK (conversation_kind IN ('DIRECT', 'PROFESSIONAL', 'GROUP')),
    CONSTRAINT chk_conversations_channel CHECK (
        (conversation_kind = 'GROUP' AND channel_id IS NOT NULL)
        OR (conversation_kind <> 'GROUP' AND channel_id IS NULL)
    ),
    CONSTRAINT fk_conversations_channel
        FOREIGN KEY (channel_id) REFERENCES support_channels(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversations_professional
        FOREIGN KEY (professional_user_id) REFERENCES professional_profiles(user_id) ON DELETE SET NULL
);

CREATE TRIGGER trg_conversations_set_updated_at
    BEFORE UPDATE ON conversations
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE conversation_members (
    conversation_id UUID NOT NULL,
    user_id UUID NOT NULL,
    member_role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMPTZ NULL,
    CONSTRAINT pk_conversation_members PRIMARY KEY (conversation_id, user_id),
    CONSTRAINT chk_conversation_members_role CHECK (member_role IN ('MEMBER', 'MODERATOR', 'OWNER')),
    CONSTRAINT fk_conversation_members_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_members_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_conversation_members_user ON conversation_members (user_id, joined_at DESC);

CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    conversation_id UUID NOT NULL,
    sender_id UUID NULL,
    message_kind VARCHAR(20) NOT NULL DEFAULT 'USER',
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL,

    CONSTRAINT chk_messages_kind CHECK (message_kind IN ('USER', 'SYSTEM')),
    CONSTRAINT chk_messages_body CHECK (LENGTH(BTRIM(body)) > 0),
    CONSTRAINT chk_messages_sender CHECK (
        (message_kind = 'SYSTEM' AND sender_id IS NULL)
        OR (message_kind = 'USER' AND sender_id IS NOT NULL)
    ),
    CONSTRAINT fk_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_messages_conversation_created_at
    ON messages (conversation_id, created_at ASC)
    WHERE deleted_at IS NULL;

ALTER TABLE reports
    ADD COLUMN severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    ADD COLUMN signals TEXT[] NOT NULL DEFAULT '{}',
    ADD CONSTRAINT chk_reports_severity CHECK (severity IN ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW'));

ALTER TABLE professional_verifications
    ADD COLUMN profession VARCHAR(100) NULL,
    ADD COLUMN country VARCHAR(100) NULL,
    ADD COLUMN document_names TEXT[] NOT NULL DEFAULT '{}';
