CREATE TABLE follows (
    follower_id UUID NOT NULL,
    following_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_follows PRIMARY KEY (follower_id, following_id),
    CONSTRAINT chk_follows_not_self CHECK (follower_id <> following_id),
    CONSTRAINT fk_follows_follower
        FOREIGN KEY (follower_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_follows_following
        FOREIGN KEY (following_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_follows_following_id
    ON follows (following_id);

CREATE TABLE blocks (
    blocker_id UUID NOT NULL,
    blocked_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_blocks PRIMARY KEY (blocker_id, blocked_id),
    CONSTRAINT chk_blocks_not_self CHECK (blocker_id <> blocked_id),
    CONSTRAINT fk_blocks_blocker
        FOREIGN KEY (blocker_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_blocks_blocked
        FOREIGN KEY (blocked_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_blocks_blocked_id
    ON blocks (blocked_id);

CREATE TABLE bookmarks (
    user_id UUID NOT NULL,
    post_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_bookmarks PRIMARY KEY (user_id, post_id),
    CONSTRAINT fk_bookmarks_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bookmarks_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE
);
