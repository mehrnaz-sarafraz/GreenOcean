-- Complete the remaining persisted contracts used by the mobile application.

CREATE TABLE article_bookmarks (
    article_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_article_bookmarks PRIMARY KEY (article_id, user_id),
    CONSTRAINT fk_article_bookmarks_article
        FOREIGN KEY (article_id) REFERENCES professional_articles(id) ON DELETE CASCADE,
    CONSTRAINT fk_article_bookmarks_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE mood_check_ins (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    mood VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mood_check_ins_mood CHECK (mood IN ('ROUGH', 'LOW', 'OKAY', 'GOOD', 'CALM')),
    CONSTRAINT fk_mood_check_ins_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_mood_check_ins_user_created
    ON mood_check_ins (user_id, created_at DESC);

CREATE TABLE listener_profiles (
    user_id UUID PRIMARY KEY,
    experience_summary VARCHAR(500) NOT NULL,
    languages TEXT[] NOT NULL DEFAULT '{}',
    support_topics TEXT[] NOT NULL DEFAULT '{}',
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    current_capacity SMALLINT NOT NULL DEFAULT 1,
    active_conversations SMALLINT NOT NULL DEFAULT 0,
    trained_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_listener_profiles_status CHECK (status IN ('AVAILABLE', 'BUSY', 'OFFLINE', 'SUSPENDED')),
    CONSTRAINT chk_listener_profiles_capacity CHECK (current_capacity BETWEEN 1 AND 20),
    CONSTRAINT chk_listener_profiles_active CHECK (active_conversations BETWEEN 0 AND current_capacity),
    CONSTRAINT fk_listener_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE conversations
    ADD COLUMN listener_user_id UUID NULL;

ALTER TABLE conversations
    DROP CONSTRAINT chk_conversations_kind;

ALTER TABLE conversations
    DROP CONSTRAINT fk_conversations_professional;

ALTER TABLE conversations
    ADD CONSTRAINT chk_conversations_kind
        CHECK (conversation_kind IN ('DIRECT', 'PROFESSIONAL', 'LISTENER', 'GROUP')),
    ADD CONSTRAINT chk_conversations_specialist CHECK (
        (conversation_kind = 'PROFESSIONAL' AND professional_user_id IS NOT NULL AND listener_user_id IS NULL)
        OR (conversation_kind = 'LISTENER' AND listener_user_id IS NOT NULL AND professional_user_id IS NULL)
        OR (conversation_kind IN ('DIRECT', 'GROUP') AND professional_user_id IS NULL AND listener_user_id IS NULL)
    ),
    ADD CONSTRAINT fk_conversations_listener
        FOREIGN KEY (listener_user_id) REFERENCES listener_profiles(user_id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_conversations_professional
        FOREIGN KEY (professional_user_id) REFERENCES professional_profiles(user_id) ON DELETE RESTRICT;

CREATE INDEX idx_conversations_listener
    ON conversations (listener_user_id) WHERE listener_user_id IS NOT NULL;

-- Jamie is fictional development content. The associated account stays suspended and cannot sign in.
INSERT INTO listener_profiles (
    user_id, experience_summary, languages, support_topics, status,
    current_capacity, active_conversations, trained_at
) VALUES (
    '01910000-0000-7000-8000-000000000001',
    'A trained peer listener focused on non-judgmental, non-clinical emotional support.',
    ARRAY['English', 'Spanish'], ARRAY['Anxiety', 'Grief', 'Relationships'],
    'AVAILABLE', 6, 0, CURRENT_TIMESTAMP - INTERVAL '1 year'
) ON CONFLICT (user_id) DO NOTHING;

-- Every published support channel has a real conversation, including read-only announcement spaces.
INSERT INTO conversations (id, conversation_kind, title, subtitle, channel_id, created_at, updated_at) VALUES
('01918000-0000-7000-8000-000000000004', 'GROUP', 'GreenOcean Safety Updates', 'Official safety announcements', '01917000-0000-7000-8000-000000000004', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('01918000-0000-7000-8000-000000000005', 'GROUP', 'Knowledge Hub Updates', 'New clinician-reviewed resources', '01917000-0000-7000-8000-000000000005', CURRENT_TIMESTAMP - INTERVAL '6 months', CURRENT_TIMESTAMP - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO conversation_members (conversation_id, user_id, member_role, joined_at) VALUES
('01918000-0000-7000-8000-000000000004', '01910000-0000-7000-8000-000000000007', 'MODERATOR', CURRENT_TIMESTAMP - INTERVAL '6 months'),
('01918000-0000-7000-8000-000000000005', '01910000-0000-7000-8000-000000000007', 'MODERATOR', CURRENT_TIMESTAMP - INTERVAL '6 months')
ON CONFLICT DO NOTHING;

INSERT INTO messages (id, conversation_id, sender_id, message_kind, body, created_at) VALUES
('01919000-0000-7000-8000-000000000006', '01918000-0000-7000-8000-000000000004', NULL, 'SYSTEM', 'GreenOcean safety guidance and policy updates will appear here.', CURRENT_TIMESTAMP - INTERVAL '2 days'),
('01919000-0000-7000-8000-000000000007', '01918000-0000-7000-8000-000000000005', NULL, 'SYSTEM', 'New clinician-reviewed learning resources will appear here.', CURRENT_TIMESTAMP - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

-- Community stories make joined community feeds useful on a fresh development database.
INSERT INTO posts (
    id, author_id, community_id, body, is_anonymous, visibility, status,
    category_id, post_type, mood, created_at, updated_at
) VALUES
('01913000-0000-7000-8000-000000000006', '01910000-0000-7000-8000-000000000003', '01912000-0000-7000-8000-000000000001',
 'Keeping a short wind-down note helped me notice progress that I would otherwise miss. Tonight I am writing down one thing that made rest a little easier.',
 FALSE, 'COMMUNITY', 'PUBLISHED', '01911000-0000-7000-8000-000000000011', 'EXPERIENCE', 'Hopeful', CURRENT_TIMESTAMP - INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
('01913000-0000-7000-8000-000000000007', '01910000-0000-7000-8000-000000000002', '01912000-0000-7000-8000-000000000003',
 'I practiced saying: I want to understand, and I need ten quiet minutes before we continue. The pause made the next conversation safer.',
 FALSE, 'COMMUNITY', 'PUBLISHED', '01911000-0000-7000-8000-000000000014', 'REFLECTION', 'Calm', CURRENT_TIMESTAMP - INTERVAL '9 hours', CURRENT_TIMESTAMP - INTERVAL '9 hours')
ON CONFLICT (id) DO NOTHING;
