-- Immutable reviewed curriculum extends the existing growth-path catalog.
ALTER TABLE guided_growth_paths ADD COLUMN resource_id UUID UNIQUE REFERENCES growth_resources(id) ON DELETE SET NULL;
ALTER TABLE user_growth_path_progress ADD COLUMN legacy_progress SMALLINT;
UPDATE user_growth_path_progress SET legacy_progress=progress;
CREATE TABLE learning_lesson_identities(
 lesson_key UUID PRIMARY KEY DEFAULT uuidv7(),course_id UUID NOT NULL REFERENCES growth_resources(id) ON DELETE CASCADE,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE learning_lesson_views(
 owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,course_id UUID NOT NULL REFERENCES growth_resources(id) ON DELETE CASCADE,
 lesson_key UUID NOT NULL,revision INTEGER NOT NULL,content_digest CHAR(64) NOT NULL,opened_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(owner_id,course_id,lesson_key)
);
CREATE TABLE learning_lesson_completions(
 id UUID PRIMARY KEY DEFAULT uuidv7(),owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 course_id UUID REFERENCES growth_resources(id) ON DELETE SET NULL,source_course_key UUID NOT NULL,course_revision INTEGER NOT NULL,
 lesson_key UUID NOT NULL,content_digest CHAR(64) NOT NULL,lesson_title VARCHAR(300) NOT NULL,
 completed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(owner_id,source_course_key,lesson_key,content_digest)
);
CREATE TABLE learning_exercise_completions(
 id UUID PRIMARY KEY DEFAULT uuidv7(),owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 course_id UUID REFERENCES growth_resources(id) ON DELETE SET NULL,source_course_key UUID NOT NULL,course_revision INTEGER NOT NULL,
 lesson_key UUID NOT NULL,content_digest CHAR(64) NOT NULL,exercise_digest CHAR(64) NOT NULL,
 completed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(owner_id,source_course_key,lesson_key,exercise_digest)
);
CREATE TABLE learning_course_completions(
 id UUID PRIMARY KEY DEFAULT uuidv7(),owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 course_id UUID REFERENCES growth_resources(id) ON DELETE SET NULL,source_course_key UUID NOT NULL,course_revision INTEGER NOT NULL,
 course_title VARCHAR(220) NOT NULL,completed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(owner_id,source_course_key,course_revision)
);
CREATE INDEX learning_completed_owner ON learning_lesson_completions(owner_id,completed_at DESC,id);
CREATE FUNCTION learning_lessons(item UUID,version INTEGER)
RETURNS TABLE(lesson_key UUID,module_position INTEGER,module_title TEXT,lesson_position INTEGER,title TEXT,body TEXT,objectives TEXT,minutes INTEGER,required BOOLEAN,exercise JSONB,content_digest TEXT)
LANGUAGE SQL STABLE AS $$
 SELECT (lesson->>'key')::uuid,m.ordinality::integer,module->>'title',l.ordinality::integer,
 lesson->>'title',lesson->>'body',lesson->>'objectives',(lesson->>'minutes')::integer,
 COALESCE((lesson->>'required')::boolean,TRUE),lesson->'exercise',encode(sha256(convert_to(lesson::text,'UTF8')),'hex')
 FROM growth_resource_versions v
 CROSS JOIN LATERAL jsonb_array_elements(v.document->'modules') WITH ORDINALITY m(module,ordinality)
 CROSS JOIN LATERAL jsonb_array_elements(module->'lessons') WITH ORDINALITY l(lesson,ordinality)
 WHERE v.resource_id=item AND v.revision=version AND v.document->>'kind'='COURSE'
$$;
CREATE FUNCTION learning_actor_eligible(who UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM users u JOIN account_eligibility e ON e.user_id=u.id
 WHERE u.id=who AND u.status='ACTIVE' AND u.email_verified AND e.age_result='ADULT'
 AND EXISTS(SELECT 1 FROM regional_services r WHERE r.country_code=e.residence_country AND r.service='COMMUNITY' AND r.status='AVAILABLE')
 AND(e.current_country IS NULL OR EXISTS(SELECT 1 FROM regional_services r WHERE r.country_code=e.current_country AND r.service='COMMUNITY' AND r.status='AVAILABLE')))
$$;
CREATE FUNCTION learning_progress(who UUID,item UUID,version INTEGER) RETURNS INTEGER LANGUAGE SQL STABLE AS $$
 SELECT COALESCE(floor(100.0*count(*) FILTER(WHERE EXISTS(
 SELECT 1 FROM learning_lesson_completions c WHERE c.owner_id=who AND c.source_course_key=item
 AND c.lesson_key=l.lesson_key AND c.content_digest=l.content_digest))/NULLIF(count(*),0)),0)::integer
 FROM learning_lessons(item,version) l WHERE l.required
$$;
CREATE FUNCTION learning_completion_guard() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF NOT learning_actor_eligible(NEW.owner_id) OR NEW.course_id IS DISTINCT FROM NEW.source_course_key
 OR NOT growth_resource_available(NEW.course_id) OR NOT EXISTS(
 SELECT 1 FROM growth_resources r WHERE r.id=NEW.course_id AND r.published_revision=NEW.course_revision)
 THEN RAISE EXCEPTION 'Current eligible reviewed learning source is required'; END IF;
 IF TG_TABLE_NAME='learning_course_completions' THEN
  IF learning_progress(NEW.owner_id,NEW.course_id,NEW.course_revision)<>100
   OR NOT EXISTS(SELECT 1 FROM learning_lessons(NEW.course_id,NEW.course_revision) WHERE required)
  THEN RAISE EXCEPTION 'Actual required lesson completions are required'; END IF;
 ELSE
  IF NOT EXISTS(SELECT 1 FROM learning_lessons(NEW.course_id,NEW.course_revision) l
    JOIN learning_lesson_views v ON v.owner_id=NEW.owner_id AND v.course_id=NEW.course_id
    AND v.lesson_key=l.lesson_key AND v.revision=NEW.course_revision AND v.content_digest=l.content_digest
    WHERE l.lesson_key=NEW.lesson_key AND l.content_digest=NEW.content_digest)
  THEN RAISE EXCEPTION 'An owned current lesson opening is required'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER learning_lesson_completion_guard BEFORE INSERT ON learning_lesson_completions FOR EACH ROW EXECUTE FUNCTION learning_completion_guard();
CREATE TRIGGER learning_exercise_completion_guard BEFORE INSERT ON learning_exercise_completions FOR EACH ROW EXECUTE FUNCTION learning_completion_guard();
CREATE TRIGGER learning_course_completion_guard BEFORE INSERT ON learning_course_completions FOR EACH ROW EXECUTE FUNCTION learning_completion_guard();
ALTER TABLE wellbeing_prompt_versions ADD COLUMN review_state TEXT NOT NULL DEFAULT 'DRAFT' CHECK(review_state IN('DRAFT','SUBMITTED','NEEDS_REVISION','APPROVED','PUBLISHED','RETIRED','REJECTED'));
ALTER TABLE wellbeing_prompt_versions ADD COLUMN sources_checked BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE wellbeing_prompt_versions ADD COLUMN rights_checked BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE wellbeing_prompt_versions ADD COLUMN etag BIGINT NOT NULL DEFAULT 0;
ALTER TABLE wellbeing_prompt_versions ADD COLUMN sources TEXT[] NOT NULL DEFAULT '{}';
ALTER TABLE wellbeing_prompt_versions ADD COLUMN rights_note VARCHAR(1000);
CREATE FUNCTION wellbeing_prompt_available(item UUID) RETURNS BOOLEAN LANGUAGE SQL STABLE AS $$
 SELECT EXISTS(SELECT 1 FROM wellbeing_prompt_versions p WHERE p.id=item AND p.status='PUBLISHED' AND p.review_state='PUBLISHED' AND p.sources_checked AND p.rights_checked
 AND p.author_id<>p.reviewer_id AND p.reviewed_at IS NOT NULL AND COALESCE(length(btrim(p.rights_note)),0)>0
 AND educational_permission_live(p.author_id,'SUBMIT','GENERAL')
 AND educational_permission_live(p.reviewer_id,'REVIEW','GENERAL')
 AND educational_permission_live(p.reviewer_id,'APPROVE','GENERAL'))
$$;
CREATE OR REPLACE FUNCTION wellbeing_prompt_immutable() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF OLD.review_state<>'DRAFT' AND
 (NEW.body,NEW.language,NEW.prompt_key,NEW.revision) IS DISTINCT FROM
 (OLD.body,OLD.language,OLD.prompt_key,OLD.revision) THEN RAISE EXCEPTION 'Published question versions are immutable'; END IF;
 IF OLD.status IN('PUBLISHED','RETIRED') AND
 (NEW.author_id,NEW.reviewer_id,NEW.review_note,NEW.reviewed_at,NEW.rights_note,NEW.sources)
 IS DISTINCT FROM(OLD.author_id,OLD.reviewer_id,OLD.review_note,OLD.reviewed_at,OLD.rights_note,OLD.sources)
 AND NOT(NEW.status='RETIRED' AND NEW.review_state='RETIRED' AND
 (NEW.author_id IS NULL OR NEW.reviewer_id IS NULL) AND
 NEW.rights_note IS NOT DISTINCT FROM OLD.rights_note AND NEW.sources IS NOT DISTINCT FROM OLD.sources
 AND NEW.reviewed_at IS NOT DISTINCT FROM OLD.reviewed_at)
 THEN RAISE EXCEPTION 'Historical review evidence cannot be silently replaced'; END IF;
 RETURN NEW;
END $$;
