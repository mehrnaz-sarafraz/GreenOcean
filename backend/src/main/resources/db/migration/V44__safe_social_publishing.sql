-- No historical identity, audience or moderation state is rewritten.
ALTER TABLE posts ADD COLUMN revision BIGINT NOT NULL DEFAULT 1;
ALTER TABLE posts ADD COLUMN safety_outcome VARCHAR(30);
ALTER TABLE posts ADD COLUMN safety_reason VARCHAR(60);
ALTER TABLE comments ADD COLUMN revision BIGINT NOT NULL DEFAULT 1;
ALTER TABLE comments ADD COLUMN safety_outcome VARCHAR(30);
ALTER TABLE comments ADD COLUMN safety_reason VARCHAR(60);

CREATE TABLE social_drafts (
 id UUID PRIMARY KEY DEFAULT uuidv7(),
 owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 payload JSONB NOT NULL CHECK (octet_length(payload::text) <= 70000),
 revision BIGINT NOT NULL DEFAULT 1,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 expires_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP + INTERVAL '7 days'
);
CREATE INDEX social_drafts_owner ON social_drafts(owner_id,updated_at DESC);
CREATE INDEX social_drafts_expiry ON social_drafts(expires_at);

-- Current revision evidence only: no second copy of sensitive narrative text.
CREATE TABLE social_assessments (
 id UUID PRIMARY KEY DEFAULT uuidv7(),
 post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
 comment_id UUID REFERENCES comments(id) ON DELETE CASCADE,
 revision BIGINT NOT NULL,
 policy_version VARCHAR(50) NOT NULL,
 outcome VARCHAR(30) NOT NULL CHECK(outcome IN ('ALLOW','REQUEST_REVISION','HOLD_FOR_REVIEW','BLOCK_PROHIBITED')),
 reason_code VARCHAR(60) NOT NULL,
 reviewer_id UUID REFERENCES users(id),
 assessed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK ((post_id IS NULL) <> (comment_id IS NULL))
);
CREATE UNIQUE INDEX social_assessment_post_revision ON social_assessments(post_id,revision);
CREATE UNIQUE INDEX social_assessment_comment_revision ON social_assessments(comment_id,revision);
CREATE INDEX social_assessment_queue ON social_assessments(assessed_at,id) WHERE outcome='HOLD_FOR_REVIEW';

CREATE FUNCTION preserve_social_identity() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF NEW.is_anonymous IS DISTINCT FROM OLD.is_anonymous OR NEW.author_id IS DISTINCT FROM OLD.author_id THEN
  RAISE EXCEPTION 'Publication attribution is immutable';
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER posts_identity_immutable BEFORE UPDATE ON posts FOR EACH ROW EXECUTE FUNCTION preserve_social_identity();
CREATE TRIGGER comments_identity_immutable BEFORE UPDATE ON comments FOR EACH ROW EXECUTE FUNCTION preserve_social_identity();
