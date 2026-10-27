-- Forward-only correction: preserve V42 and historical publication evidence.
CREATE OR REPLACE FUNCTION preserve_published_policy() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF OLD.status IN ('PUBLISHED','RETIRED') THEN
  IF NEW.status NOT IN ('PUBLISHED','RETIRED') OR
     ROW(NEW.policy_key,NEW.version,NEW.title,NEW.summary,NEW.content,NEW.locale,NEW.published_at,NEW.effective_at,
         NEW.review_status,NEW.reviewed_by,NEW.reviewed_at,NEW.review_reference,NEW.updated_by)
     IS DISTINCT FROM
     ROW(OLD.policy_key,OLD.version,OLD.title,OLD.summary,OLD.content,OLD.locale,OLD.published_at,OLD.effective_at,
         OLD.review_status,OLD.reviewed_by,OLD.reviewed_at,OLD.review_reference,OLD.updated_by)
  THEN RAISE EXCEPTION 'Published policy and review evidence are immutable'; END IF;
 ELSIF OLD.status='DRAFT' AND
     ROW(NEW.policy_key,NEW.version,NEW.title,NEW.summary,NEW.content,NEW.locale,NEW.effective_at,NEW.updated_by)
     IS DISTINCT FROM
     ROW(OLD.policy_key,OLD.version,OLD.title,OLD.summary,OLD.content,OLD.locale,OLD.effective_at,OLD.updated_by)
 THEN
  -- Editing a reviewed draft requires a new review of its actual content.
  NEW.review_status='DRAFT';NEW.reviewed_by=NULL;NEW.reviewed_at=NULL;NEW.review_reference=NULL;
 END IF;
 RETURN NEW;
END $$;
