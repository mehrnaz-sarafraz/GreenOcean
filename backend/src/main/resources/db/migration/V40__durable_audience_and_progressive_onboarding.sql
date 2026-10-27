-- Historical PUBLIC was not an informed per-post audience; never infer consent from current privacy.
UPDATE posts SET visibility = 'FOLLOWERS' WHERE visibility = 'PUBLIC';

ALTER TABLE user_preferences
 ADD COLUMN onboarding_status VARCHAR(24) NOT NULL DEFAULT 'NOT_STARTED',
 ADD COLUMN onboarding_step SMALLINT NOT NULL DEFAULT 0,
 ADD COLUMN legacy_support_topics TEXT[] NOT NULL DEFAULT '{}';
UPDATE user_preferences SET onboarding_status = 'LEGACY_UNKNOWN' WHERE onboarding_complete;
ALTER TABLE user_preferences ADD CONSTRAINT ck_onboarding_status
 CHECK (onboarding_status IN ('NOT_STARTED','IN_PROGRESS','SKIPPED','COMPLETED','LEGACY_UNKNOWN'));
ALTER TABLE user_preferences ADD CONSTRAINT ck_onboarding_step CHECK (onboarding_step BETWEEN 0 AND 3);

-- Exact one-to-one mapping only. Preserve ambiguous/unmapped labels owner-only.
WITH mapping AS (
 SELECT up.user_id, value,
   (SELECT MIN(sc.slug) FROM support_categories sc
    WHERE sc.slug = value OR sc.name = value
    HAVING COUNT(*) = 1) AS slug
 FROM user_preferences up CROSS JOIN LATERAL unnest(up.support_topics) value
), resolved AS (
 SELECT user_id,
 COALESCE(array_agg(DISTINCT slug) FILTER (WHERE slug IS NOT NULL),'{}') AS topics,
 COALESCE(array_agg(DISTINCT value) FILTER (WHERE slug IS NULL),'{}') AS legacy
 FROM mapping GROUP BY user_id
)
UPDATE user_preferences up SET support_topics = r.topics, legacy_support_topics = r.legacy
 FROM resolved r WHERE up.user_id = r.user_id;

-- One authorization predicate for all social post reads, counts, notifications and actions.
CREATE FUNCTION can_read_social_post(post_id UUID, viewer UUID) RETURNS BOOLEAN
LANGUAGE SQL STABLE AS $$
 SELECT EXISTS (
 SELECT 1 FROM posts p JOIN users author ON author.id=p.author_id
 WHERE p.id=post_id AND p.status='PUBLISHED' AND author.status='ACTIVE'
 AND NOT EXISTS (SELECT 1 FROM blocks b
   WHERE (b.blocker_id=viewer AND b.blocked_id=p.author_id)
      OR (b.blocker_id=p.author_id AND b.blocked_id=viewer))
 AND (p.author_id=viewer OR p.visibility='PUBLIC'
   OR (p.visibility='FOLLOWERS' AND EXISTS (
     SELECT 1 FROM follows f WHERE f.follower_id=viewer AND f.following_id=p.author_id))
   OR (p.visibility='COMMUNITY' AND EXISTS (
     SELECT 1 FROM community_members cm WHERE cm.community_id=p.community_id AND cm.user_id=viewer)))
 )
$$;
