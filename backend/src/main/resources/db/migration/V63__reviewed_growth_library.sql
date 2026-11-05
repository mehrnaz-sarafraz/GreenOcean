-- New governance around existing catalogs; old records and saves are preserved.
CREATE TABLE educational_permissions (
 id UUID PRIMARY KEY DEFAULT uuidv7(),user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 action TEXT NOT NULL CHECK(action IN('DRAFT','SUBMIT','REVIEW','APPROVE','PUBLISH','FEATURE','RETIRE','ATTACH')),
 scope VARCHAR(80) NOT NULL,issued_by UUID REFERENCES users(id) ON DELETE SET NULL,
 evidence VARCHAR(2000) NOT NULL CHECK(length(btrim(evidence))>0),
 expires_at TIMESTAMPTZ NOT NULL,revoked_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(issued_by IS NULL OR issued_by<>user_id)
);
CREATE INDEX education_permissions_current ON educational_permissions(user_id,action,scope,expires_at) WHERE revoked_at IS NULL;
CREATE TABLE educational_credentials (
 id UUID PRIMARY KEY DEFAULT uuidv7(),user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 verification_id UUID NOT NULL REFERENCES professional_verifications(id) ON DELETE CASCADE,
 scope VARCHAR(80) NOT NULL,verified_by UUID REFERENCES users(id) ON DELETE SET NULL,
 reference VARCHAR(2000) NOT NULL CHECK(length(btrim(reference))>0),expires_at TIMESTAMPTZ NOT NULL,revoked_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,CHECK(verified_by IS NULL OR verified_by<>user_id)
);
CREATE TABLE growth_resources (
 id UUID PRIMARY KEY DEFAULT uuidv7(),author_id UUID REFERENCES users(id) ON DELETE SET NULL,
 legacy_article_id UUID UNIQUE REFERENCES professional_articles(id) ON DELETE SET NULL,
 legacy_media_id UUID UNIQUE REFERENCES media_recommendations(id) ON DELETE SET NULL,
 current_revision INTEGER NOT NULL DEFAULT 1,published_revision INTEGER,retired BOOLEAN NOT NULL DEFAULT FALSE,
 featured BOOLEAN NOT NULL DEFAULT FALSE,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE growth_resource_versions (
 resource_id UUID NOT NULL REFERENCES growth_resources(id) ON DELETE CASCADE,revision INTEGER NOT NULL CHECK(revision>0),
 document JSONB NOT NULL CHECK(jsonb_typeof(document)='object'),etag BIGINT NOT NULL DEFAULT 0,
 state TEXT NOT NULL DEFAULT 'DRAFT' CHECK(state IN('DRAFT','SUBMITTED','NEEDS_REVISION','APPROVED','PUBLISHED','RETIRED','REJECTED','LEGACY_UNREVIEWED')),
 reviewer_id UUID REFERENCES users(id) ON DELETE SET NULL,review_note VARCHAR(3000),reviewed_at TIMESTAMPTZ,
 sources_checked BOOLEAN NOT NULL DEFAULT FALSE,rights_checked BOOLEAN NOT NULL DEFAULT FALSE,
 published_by UUID REFERENCES users(id) ON DELETE SET NULL,published_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(resource_id,revision)
);
ALTER TABLE growth_resources ADD CONSTRAINT growth_current_version FOREIGN KEY(id,current_revision) REFERENCES growth_resource_versions(resource_id,revision) DEFERRABLE INITIALLY DEFERRED;
ALTER TABLE growth_resources ADD CONSTRAINT growth_published_version FOREIGN KEY(id,published_revision) REFERENCES growth_resource_versions(resource_id,revision) DEFERRABLE INITIALLY DEFERRED;
CREATE TABLE growth_editorial_events (
 id UUID PRIMARY KEY DEFAULT uuidv7(),resource_id UUID NOT NULL REFERENCES growth_resources(id) ON DELETE CASCADE,
 revision INTEGER NOT NULL,actor_id UUID REFERENCES users(id) ON DELETE SET NULL,action TEXT NOT NULL,
 note VARCHAR(3000) NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE growth_resource_saves(user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,resource_id UUID NOT NULL REFERENCES growth_resources(id) ON DELETE CASCADE,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(user_id,resource_id));
CREATE TABLE post_resource_preferences(post_id UUID PRIMARY KEY REFERENCES posts(id) ON DELETE CASCADE,allowed BOOLEAN NOT NULL DEFAULT FALSE,revision BIGINT NOT NULL DEFAULT 0);
CREATE TABLE post_resource_attachments (
 id UUID PRIMARY KEY DEFAULT uuidv7(),post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
 resource_id UUID NOT NULL REFERENCES growth_resources(id) ON DELETE CASCADE,resource_revision INTEGER NOT NULL,
 post_revision BIGINT NOT NULL,preference_revision BIGINT NOT NULL,
 proposed_by UUID REFERENCES users(id) ON DELETE SET NULL,reviewed_by UUID REFERENCES users(id) ON DELETE SET NULL,
 status TEXT NOT NULL DEFAULT 'PROPOSED' CHECK(status IN('PROPOSED','APPROVED','REMOVED','REJECTED')),
 reason VARCHAR(1000) NOT NULL,review_note VARCHAR(1000),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 reviewed_at TIMESTAMPTZ,UNIQUE(post_id,resource_id)
);
INSERT INTO growth_resources(id,author_id,legacy_article_id) SELECT id,author_id,id FROM professional_articles;
INSERT INTO growth_resource_versions(resource_id,revision,state,document)
 SELECT id,1,'LEGACY_UNREVIEWED',jsonb_build_object('kind','ARTICLE','title',title,'summary',summary,'topic',topic,'legacySections',sections,'legacyReferences',reference_list,'language','en','scope','GENERAL') FROM professional_articles;
INSERT INTO growth_resources(id,legacy_media_id) SELECT id,id FROM media_recommendations;
INSERT INTO growth_resource_versions(resource_id,revision,state,document)
 SELECT id,1,'LEGACY_UNREVIEWED',jsonb_build_object('kind',CASE WHEN media_kind='MOVIE' THEN 'FILM' ELSE media_kind END,'title',title,'summary',description,'topic',theme,'creator',recommended_by,'language','en','scope','GENERAL') FROM media_recommendations;
-- Preserve seed values; publication requires a real exact-revision review through the new catalog.
CREATE FUNCTION educational_permission_live(who UUID,capability TEXT,category TEXT) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM educational_permissions p JOIN users u ON u.id=p.user_id
 WHERE p.user_id=who AND p.action=capability AND p.scope=category AND u.status='ACTIVE' AND p.revoked_at IS NULL AND p.expires_at>CURRENT_TIMESTAMP
 AND EXISTS(SELECT 1 FROM users issuer JOIN user_roles ur ON ur.user_id=issuer.id JOIN roles r ON r.id=ur.role_id WHERE issuer.id=p.issued_by AND issuer.status='ACTIVE' AND r.name='ADMIN'))
$$;
CREATE FUNCTION educational_credential_live(who UUID,category TEXT) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM educational_credentials c JOIN professional_verifications v ON v.id=c.verification_id JOIN professional_profiles p ON p.user_id=c.user_id
 WHERE c.user_id=who AND c.scope=category AND c.revoked_at IS NULL AND c.expires_at>CURRENT_TIMESTAMP AND c.verified_by<>who
 AND p.verification_status='VERIFIED' AND v.status='APPROVED' AND v.reviewed_by<>who AND v.reviewed_at IS NOT NULL
 AND EXISTS(SELECT 1 FROM professional_application_documents d WHERE d.application_id=v.id AND d.scan_status='CLEAN' AND d.scanned_at IS NOT NULL AND d.reviewed_by<>who)
 AND EXISTS(SELECT 1 FROM users u JOIN user_roles ur ON ur.user_id=u.id JOIN roles r ON r.id=ur.role_id WHERE u.id=c.verified_by AND u.status='ACTIVE' AND r.name='ADMIN'))
$$;
CREATE FUNCTION growth_resource_available(item UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM growth_resources r JOIN growth_resource_versions v ON v.resource_id=r.id AND v.revision=r.published_revision
 WHERE r.id=item AND NOT r.retired AND v.state='PUBLISHED' AND v.sources_checked AND v.rights_checked
 AND v.reviewer_id<>r.author_id AND v.reviewed_at IS NOT NULL AND v.published_at IS NOT NULL
 AND educational_permission_live(r.author_id,'SUBMIT',v.document->>'scope')
 AND educational_permission_live(v.reviewer_id,'REVIEW',v.document->>'scope')
 AND educational_permission_live(v.reviewer_id,'APPROVE',v.document->>'scope')
 AND ((v.document->>'scope')='GENERAL' OR (educational_credential_live(r.author_id,v.document->>'scope') AND educational_credential_live(v.reviewer_id,v.document->>'scope'))))
$$;
CREATE FUNCTION growth_version_immutable() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF OLD.state<>'DRAFT' AND NEW.document IS DISTINCT FROM OLD.document THEN RAISE EXCEPTION 'Reviewed content revision is immutable'; END IF;
 IF NEW.state IN('APPROVED','PUBLISHED') AND (NEW.reviewer_id IS NULL OR NEW.reviewed_at IS NULL OR NOT NEW.sources_checked OR NOT NEW.rights_checked OR COALESCE(length(btrim(NEW.review_note)),0)=0 OR EXISTS(SELECT 1 FROM growth_resources r WHERE r.id=NEW.resource_id AND (r.author_id IS NULL OR r.author_id=NEW.reviewer_id))) THEN RAISE EXCEPTION 'Independent exact revision approval is required'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER growth_version_immutable BEFORE UPDATE ON growth_resource_versions FOR EACH ROW EXECUTE FUNCTION growth_version_immutable();
