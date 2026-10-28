ALTER TABLE community_members ADD COLUMN rules_version_ack BIGINT NOT NULL DEFAULT 0;
ALTER TABLE community_members ADD COLUMN restricted_until TIMESTAMPTZ;
ALTER TABLE community_join_requests ADD COLUMN request_version BIGINT NOT NULL DEFAULT 1;
ALTER TABLE community_join_requests ADD COLUMN rules_version_ack BIGINT NOT NULL DEFAULT 0;
CREATE TABLE community_bans(
 community_id UUID NOT NULL REFERENCES communities(id) ON DELETE CASCADE,user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 actor_id UUID REFERENCES users(id),reason VARCHAR(1000) NOT NULL,expires_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(community_id,user_id));
CREATE TABLE community_invitations(
 id UUID PRIMARY KEY DEFAULT uuidv7(),community_id UUID NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
 inviter_id UUID NOT NULL REFERENCES users(id),target_id UUID NOT NULL REFERENCES users(id),retry_key UUID NOT NULL,
 status VARCHAR(12) NOT NULL DEFAULT 'PENDING' CHECK(status IN('PENDING','ACCEPTED','DECLINED','REVOKED','EXPIRED')),
 expires_at TIMESTAMPTZ NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(inviter_id,retry_key));
CREATE UNIQUE INDEX group_pending_target ON community_invitations(community_id,target_id) WHERE status='PENDING';
CREATE INDEX group_invite_inbox ON community_invitations(target_id,status,expires_at);
CREATE TABLE community_ownership_transfers(
 id UUID PRIMARY KEY DEFAULT uuidv7(),community_id UUID NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
 from_owner UUID REFERENCES users(id),target_id UUID NOT NULL REFERENCES users(id),
 status VARCHAR(12) NOT NULL DEFAULT 'PENDING' CHECK(status IN('PENDING','ACCEPTED','DECLINED','CANCELLED','EXPIRED')),
 expires_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP+INTERVAL '7 days',created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE UNIQUE INDEX group_pending_transfer ON community_ownership_transfers(community_id) WHERE status='PENDING';
CREATE TABLE community_rule_revisions(
 community_id UUID NOT NULL REFERENCES communities(id) ON DELETE CASCADE,version BIGINT NOT NULL,
 rules JSONB NOT NULL,actor_id UUID REFERENCES users(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(community_id,version));
CREATE TABLE community_member_actions(
 id UUID PRIMARY KEY DEFAULT uuidv7(),community_id UUID NOT NULL REFERENCES communities(id) ON DELETE CASCADE,
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,actor_id UUID REFERENCES users(id),
 action VARCHAR(30) NOT NULL,reason VARCHAR(1000) NOT NULL,expires_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
INSERT INTO community_rule_revisions(community_id,version,rules)
 SELECT c.id,c.rules_version,COALESCE(jsonb_agg(r.rule_text ORDER BY r.position) FILTER(WHERE r.id IS NOT NULL),'[]'::jsonb)
 FROM communities c LEFT JOIN community_rules r ON r.community_id=c.id GROUP BY c.id;
CREATE OR REPLACE FUNCTION active_group_member(group_id UUID,viewer UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM communities g JOIN community_members m ON m.community_id=g.id JOIN users u ON u.id=m.user_id
 WHERE g.id=group_id AND m.user_id=viewer AND u.status='ACTIVE' AND u.deleted_at IS NULL AND g.status IN('ACTIVE','RESTRICTED')
 AND NOT EXISTS(SELECT 1 FROM community_bans b WHERE b.community_id=g.id AND b.user_id=viewer AND (b.expires_at IS NULL OR b.expires_at>CURRENT_TIMESTAMP))) $$;
