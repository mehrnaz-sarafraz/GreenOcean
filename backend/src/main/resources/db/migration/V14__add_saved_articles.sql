CREATE TABLE article_saves (
    article_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_article_saves PRIMARY KEY (article_id, user_id),
    CONSTRAINT fk_article_saves_article FOREIGN KEY (article_id)
        REFERENCES professional_articles(id) ON DELETE CASCADE,
    CONSTRAINT fk_article_saves_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_article_saves_user_created ON article_saves (user_id, created_at DESC);
