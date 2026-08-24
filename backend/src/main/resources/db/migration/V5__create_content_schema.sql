CREATE TABLE posts (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    author_id UUID NOT NULL,
    community_id UUID NULL,
    body TEXT NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    content_warning VARCHAR(120) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL,

    CONSTRAINT chk_posts_body_not_blank CHECK (LENGTH(BTRIM(body)) > 0),
    CONSTRAINT chk_posts_visibility CHECK (visibility IN ('PUBLIC', 'FOLLOWERS', 'COMMUNITY')),
    CONSTRAINT chk_posts_status CHECK (status IN ('PUBLISHED', 'HIDDEN', 'UNDER_REVIEW', 'DELETED')),
    CONSTRAINT chk_posts_community_visibility CHECK (
        (visibility = 'COMMUNITY' AND community_id IS NOT NULL)
        OR (visibility IN ('PUBLIC', 'FOLLOWERS') AND community_id IS NULL)
    ),
    CONSTRAINT fk_posts_author
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_posts_community
        FOREIGN KEY (community_id)
        REFERENCES communities(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_posts_author_created_at
    ON posts (author_id, created_at DESC);

CREATE INDEX idx_posts_community_created_at
    ON posts (community_id, created_at DESC);

CREATE INDEX idx_posts_status_created_at
    ON posts (status, created_at DESC);

CREATE TRIGGER trg_posts_set_updated_at
    BEFORE UPDATE ON posts
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    post_id UUID NOT NULL,
    author_id UUID NOT NULL,
    parent_comment_id UUID NULL,
    body TEXT NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL,

    CONSTRAINT uq_comments_id_post_id UNIQUE (id, post_id),
    CONSTRAINT chk_comments_body_not_blank CHECK (LENGTH(BTRIM(body)) > 0),
    CONSTRAINT chk_comments_status CHECK (status IN ('PUBLISHED', 'HIDDEN', 'UNDER_REVIEW', 'DELETED')),
    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_comments_parent_same_post
        FOREIGN KEY (parent_comment_id, post_id)
        REFERENCES comments (id, post_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_comments_post_created_at
    ON comments (post_id, created_at ASC);

CREATE INDEX idx_comments_parent_created_at
    ON comments (parent_comment_id, created_at ASC);

CREATE TRIGGER trg_comments_set_updated_at
    BEFORE UPDATE ON comments
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TABLE media (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    post_id UUID NOT NULL,
    uploader_id UUID NOT NULL,
    storage_key TEXT NOT NULL,
    original_filename VARCHAR(255) NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    width INTEGER NULL,
    height INTEGER NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_media_size_bytes CHECK (size_bytes > 0),
    CONSTRAINT chk_media_dimensions CHECK (
        (width IS NULL OR width > 0)
        AND (height IS NULL OR height > 0)
    ),
    CONSTRAINT fk_media_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_media_uploader
        FOREIGN KEY (uploader_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(60) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_tags_slug UNIQUE (slug),
    CONSTRAINT chk_tags_slug CHECK (
        slug = LOWER(slug)
        AND slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'
    )
);

CREATE TABLE post_tags (
    post_id UUID NOT NULL,
    tag_id UUID NOT NULL,

    CONSTRAINT pk_post_tags PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag
        FOREIGN KEY (tag_id)
        REFERENCES tags(id)
        ON DELETE RESTRICT
);
