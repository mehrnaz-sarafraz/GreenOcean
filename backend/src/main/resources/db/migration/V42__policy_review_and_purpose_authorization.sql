ALTER TABLE policy_documents
 ADD COLUMN locale VARCHAR(8) NOT NULL DEFAULT 'en' CHECK(locale IN ('en','fa')),
 ADD COLUMN effective_at TIMESTAMPTZ,
 ADD COLUMN review_status VARCHAR(24) NOT NULL DEFAULT 'LEGACY_UNREVIEWED'
   CHECK(review_status IN ('LEGACY_UNREVIEWED','DRAFT','APPROVED')),
 ADD COLUMN reviewed_by UUID REFERENCES users(id),
 ADD COLUMN reviewed_at TIMESTAMPTZ,
 ADD COLUMN review_reference VARCHAR(100);
UPDATE policy_documents SET effective_at=published_at;

CREATE FUNCTION preserve_published_policy() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF OLD.status IN ('PUBLISHED','RETIRED') AND
    ROW(NEW.policy_key,NEW.version,NEW.title,NEW.summary,NEW.content,NEW.locale,NEW.published_at,NEW.effective_at)
    IS DISTINCT FROM ROW(OLD.policy_key,OLD.version,OLD.title,OLD.summary,OLD.content,OLD.locale,OLD.published_at,OLD.effective_at)
 THEN RAISE EXCEPTION 'Published policy evidence is immutable'; END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER policy_publication_immutable BEFORE UPDATE ON policy_documents
 FOR EACH ROW EXECUTE FUNCTION preserve_published_policy();
CREATE FUNCTION preserve_acceptance_timestamp() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN RAISE EXCEPTION 'Policy acceptance evidence is immutable'; END $$;
CREATE TRIGGER policy_acceptance_immutable BEFORE UPDATE ON policy_acceptances
 FOR EACH ROW EXECUTE FUNCTION preserve_acceptance_timestamp();

CREATE TABLE processing_authorizations (
 id UUID PRIMARY KEY DEFAULT uuidv7(),
 event_order BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 purpose VARCHAR(40) NOT NULL CHECK(purpose IN ('SUPPORT_TOPICS','OPTIONAL_ANALYTICS','OPTIONAL_COMMUNICATIONS')),
 decision VARCHAR(12) NOT NULL CHECK(decision IN ('GRANTED','DENIED','WITHDRAWN')),
 notice_id UUID REFERENCES policy_documents(id),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(decision<>'GRANTED' OR notice_id IS NOT NULL)
);
CREATE INDEX processing_authorizations_owner_recent ON processing_authorizations(user_id,purpose,event_order DESC);
CREATE TABLE privacy_notice_receipts (
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 notice_id UUID NOT NULL REFERENCES policy_documents(id),
 received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(user_id,notice_id)
);
CREATE TABLE data_rights_requests (
 id UUID PRIMARY KEY DEFAULT uuidv7(), user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 kind VARCHAR(24) NOT NULL CHECK(kind IN ('ACCESS','RECTIFICATION','ERASURE','RESTRICTION','OBJECTION','PORTABILITY','HUMAN_REVIEW')),
 status VARCHAR(24) NOT NULL DEFAULT 'RECEIVED' CHECK(status IN ('RECEIVED','IN_REVIEW','NEEDS_INFORMATION','RESOLVED','DECLINED')),
 idempotency_key UUID NOT NULL, resolution_reference VARCHAR(100),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(user_id,idempotency_key)
);
CREATE TRIGGER authorization_event_immutable BEFORE UPDATE ON processing_authorizations
 FOR EACH ROW EXECUTE FUNCTION preserve_acceptance_timestamp();
CREATE TRIGGER notice_receipt_immutable BEFORE UPDATE ON privacy_notice_receipts
 FOR EACH ROW EXECUTE FUNCTION preserve_acceptance_timestamp();
CREATE INDEX rights_owner_recent ON data_rights_requests(user_id,created_at DESC);
CREATE UNIQUE INDEX rights_one_open_kind ON data_rights_requests(user_id,kind)
 WHERE status IN ('RECEIVED','IN_REVIEW','NEEDS_INFORMATION');
-- No backfilled grants/receipts, published drafts, new legal text or new retention period.
