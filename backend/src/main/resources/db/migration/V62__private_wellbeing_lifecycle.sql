-- Additive private data; no historical writing or progress is reclassified.
ALTER TABLE wellbeing_journal_entries ADD COLUMN revision BIGINT NOT NULL DEFAULT 0;
ALTER TABLE wellbeing_journal_entries ADD COLUMN tags TEXT[] NOT NULL DEFAULT '{}';
ALTER TABLE wellbeing_journal_entries ADD COLUMN client_key UUID;
CREATE UNIQUE INDEX wellbeing_journal_retry ON wellbeing_journal_entries(user_id,client_key) WHERE client_key IS NOT NULL;
ALTER TABLE wellbeing_check_ins ADD COLUMN revision BIGINT NOT NULL DEFAULT 0;
ALTER TABLE wellbeing_check_ins ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE wellbeing_check_ins ADD COLUMN client_key UUID;
CREATE UNIQUE INDEX wellbeing_mood_retry ON wellbeing_check_ins(user_id,client_key) WHERE client_key IS NOT NULL;
ALTER TABLE wellbeing_goals ADD COLUMN revision BIGINT NOT NULL DEFAULT 0;
ALTER TABLE wellbeing_goals ADD COLUMN lifecycle TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(lifecycle IN('ACTIVE','PAUSED','COMPLETED','ARCHIVED'));
ALTER TABLE wellbeing_goals ADD COLUMN legacy_progress SMALLINT;
UPDATE wellbeing_goals SET legacy_progress=progress,lifecycle=CASE WHEN completed THEN 'COMPLETED' ELSE 'ACTIVE' END;
CREATE TABLE wellbeing_goal_steps (
 id UUID PRIMARY KEY DEFAULT uuidv7(),goal_id UUID NOT NULL REFERENCES wellbeing_goals(id) ON DELETE CASCADE,
 title VARCHAR(300) NOT NULL CHECK(length(btrim(title))>0),completed BOOLEAN NOT NULL DEFAULT FALSE,
 position INTEGER NOT NULL CHECK(position BETWEEN 0 AND 49)
);
CREATE INDEX wellbeing_steps_goal ON wellbeing_goal_steps(goal_id,position,id);
CREATE TABLE wellbeing_prompt_versions (
 id UUID PRIMARY KEY DEFAULT uuidv7(),prompt_key UUID NOT NULL DEFAULT uuidv7(),revision INTEGER NOT NULL CHECK(revision>0),
 language TEXT NOT NULL CHECK(language IN('en','fa')),body VARCHAR(1000) NOT NULL,
 status TEXT NOT NULL DEFAULT 'DRAFT' CHECK(status IN('DRAFT','PUBLISHED','RETIRED')),
 author_id UUID REFERENCES users(id) ON DELETE SET NULL,reviewer_id UUID REFERENCES users(id) ON DELETE SET NULL,
 review_note TEXT,reviewed_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(prompt_key,revision),CHECK(status<>'PUBLISHED' OR (author_id IS NOT NULL AND reviewer_id IS NOT NULL AND author_id<>reviewer_id AND reviewed_at IS NOT NULL AND length(btrim(review_note))>0))
);
CREATE TABLE wellbeing_daily_sets (
 id UUID PRIMARY KEY DEFAULT uuidv7(),user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 local_date DATE NOT NULL,timezone TEXT NOT NULL,language TEXT NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 UNIQUE(user_id,local_date)
);
CREATE TABLE wellbeing_daily_questions (
 set_id UUID NOT NULL REFERENCES wellbeing_daily_sets(id) ON DELETE CASCADE,
 position SMALLINT NOT NULL CHECK(position BETWEEN 1 AND 3),prompt_id UUID NOT NULL REFERENCES wellbeing_prompt_versions(id),
 PRIMARY KEY(set_id,position),UNIQUE(set_id,prompt_id)
);
CREATE TABLE wellbeing_daily_answers (
 id UUID PRIMARY KEY DEFAULT uuidv7(),set_id UUID NOT NULL,position SMALLINT NOT NULL,
 body VARCHAR(20000) NOT NULL CHECK(length(btrim(body))>0),revision BIGINT NOT NULL DEFAULT 0,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(set_id,position) REFERENCES wellbeing_daily_questions(set_id,position) ON DELETE CASCADE,UNIQUE(set_id,position)
);
CREATE TABLE wellbeing_journal_drafts (
 user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 title VARCHAR(160) NOT NULL,body VARCHAR(20000) NOT NULL,mood VARCHAR(40),revision BIGINT NOT NULL DEFAULT 0,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,expires_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP+INTERVAL '7 days'
);
CREATE INDEX wellbeing_draft_expiry ON wellbeing_journal_drafts(expires_at);
CREATE FUNCTION wellbeing_prompt_immutable() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 IF OLD.status IN('PUBLISHED','RETIRED') AND (NEW.body,NEW.language,NEW.prompt_key,NEW.revision,NEW.author_id,NEW.review_note,NEW.reviewed_at) IS DISTINCT FROM (OLD.body,OLD.language,OLD.prompt_key,OLD.revision,OLD.author_id,OLD.review_note,OLD.reviewed_at) THEN
  RAISE EXCEPTION 'Published question versions are immutable';
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER wellbeing_prompt_immutable BEFORE UPDATE ON wellbeing_prompt_versions FOR EACH ROW EXECUTE FUNCTION wellbeing_prompt_immutable();
