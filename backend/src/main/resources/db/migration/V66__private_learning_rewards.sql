-- Private educational milestones only. No legacy percentage or sensitive action backfill.
CREATE TABLE growth_reward_rules(version INTEGER PRIMARY KEY CHECK(version>0),lesson_points INTEGER NOT NULL CHECK(lesson_points BETWEEN 0 AND 1000),exercise_points INTEGER NOT NULL CHECK(exercise_points BETWEEN 0 AND 1000),course_points INTEGER NOT NULL CHECK(course_points BETWEEN 0 AND 1000),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
INSERT INTO growth_reward_rules VALUES(1,10,5,30,CURRENT_TIMESTAMP);
CREATE FUNCTION growth_reward_source(kind TEXT,completion UUID,who UUID)
RETURNS TABLE(course_key UUID,activity_key UUID,course_id UUID,revision INTEGER) LANGUAGE SQL STABLE AS $$
 SELECT source_course_key,lesson_key,course_id,course_revision FROM learning_lesson_completions WHERE kind='LESSON' AND id=completion AND owner_id=who
 UNION ALL SELECT source_course_key,lesson_key,course_id,course_revision FROM learning_exercise_completions WHERE kind='EXERCISE' AND id=completion AND owner_id=who
 UNION ALL SELECT source_course_key,source_course_key,course_id,course_revision FROM learning_course_completions WHERE kind='COURSE' AND id=completion AND owner_id=who
$$;
CREATE TABLE growth_reward_ledger(
 id UUID PRIMARY KEY DEFAULT uuidv7(),owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 source_type TEXT NOT NULL CHECK(source_type IN('LESSON','EXERCISE','COURSE')),completion_id UUID NOT NULL,
 course_key UUID NOT NULL,activity_key UUID NOT NULL,rule_version INTEGER NOT NULL REFERENCES growth_reward_rules(version),
 point_delta INTEGER NOT NULL CHECK(point_delta BETWEEN -1000 AND 1000),reversal_of UUID UNIQUE REFERENCES growth_reward_ledger(id) ON DELETE CASCADE,
 actor_id UUID REFERENCES users(id) ON DELETE SET NULL,reason VARCHAR(1000),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX growth_reward_once ON growth_reward_ledger(owner_id,source_type,course_key,activity_key) WHERE reversal_of IS NULL;
CREATE INDEX growth_reward_owner ON growth_reward_ledger(owner_id,created_at DESC,id);
CREATE FUNCTION growth_reward_guard() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE source RECORD; original RECORD; amount INTEGER;
BEGIN
 IF TG_OP='UPDATE' THEN
  -- Account erasure may redact the operational actor, never rewrite a reward.
  IF NEW.actor_id IS NULL AND OLD.actor_id IS NOT NULL AND (to_jsonb(NEW)-'actor_id')=(to_jsonb(OLD)-'actor_id') THEN RETURN NEW; END IF;
  RAISE EXCEPTION 'Reward events are immutable; append a reversal';
 END IF;
 IF NEW.reversal_of IS NOT NULL THEN
  SELECT * INTO original FROM growth_reward_ledger WHERE id=NEW.reversal_of FOR UPDATE;
  IF original.id IS NULL OR original.reversal_of IS NOT NULL OR NEW.owner_id<>original.owner_id OR NEW.source_type<>original.source_type OR NEW.completion_id<>original.completion_id OR NEW.course_key<>original.course_key OR NEW.activity_key<>original.activity_key OR NEW.rule_version<>original.rule_version OR NEW.point_delta<>-original.point_delta OR length(trim(COALESCE(NEW.reason,'')))<3 THEN RAISE EXCEPTION 'Invalid reward reversal'; END IF;
 ELSE
  SELECT * INTO source FROM growth_reward_source(NEW.source_type,NEW.completion_id,NEW.owner_id);
  IF source.course_key IS NULL OR source.course_key<>NEW.course_key OR source.activity_key<>NEW.activity_key OR NOT learning_actor_eligible(NEW.owner_id) OR NOT growth_resource_available(source.course_id) OR NOT EXISTS(SELECT 1 FROM growth_resources WHERE id=source.course_id AND published_revision=source.revision) THEN RAISE EXCEPTION 'A current owned eligible learning completion is required'; END IF;
  SELECT CASE NEW.source_type WHEN 'LESSON' THEN lesson_points WHEN 'EXERCISE' THEN exercise_points ELSE course_points END INTO amount FROM growth_reward_rules WHERE version=NEW.rule_version;
  IF NEW.point_delta IS DISTINCT FROM amount OR NEW.actor_id IS NOT NULL THEN RAISE EXCEPTION 'Reward amount must follow its server rule'; END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER growth_reward_guard BEFORE INSERT OR UPDATE ON growth_reward_ledger FOR EACH ROW EXECUTE FUNCTION growth_reward_guard();
CREATE FUNCTION growth_reward_rule_immutable() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'Create a new reward rule version'; END $$;
CREATE TRIGGER growth_reward_rule_immutable BEFORE UPDATE OR DELETE ON growth_reward_rules FOR EACH ROW EXECUTE FUNCTION growth_reward_rule_immutable();
CREATE TABLE growth_learning_badges(owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,code TEXT NOT NULL CHECK(code IN('FIRST_LESSON','FIRST_COURSE','FIVE_LESSONS')),earned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,revoked_at TIMESTAMPTZ,PRIMARY KEY(owner_id,code));
CREATE FUNCTION growth_refresh_badges(who UUID) RETURNS VOID LANGUAGE plpgsql AS $$
DECLARE lessons INTEGER; courses INTEGER; badge TEXT; valid BOOLEAN;
BEGIN
 PERFORM pg_advisory_xact_lock(hashtextextended('growth-rewards:'||who::text,0));
 SELECT count(*) FILTER(WHERE source_type='LESSON'),count(*) FILTER(WHERE source_type='COURSE') INTO lessons,courses FROM growth_reward_ledger e WHERE owner_id=who AND reversal_of IS NULL AND NOT EXISTS(SELECT 1 FROM growth_reward_ledger r WHERE r.reversal_of=e.id);
 FOREACH badge IN ARRAY ARRAY['FIRST_LESSON','FIRST_COURSE','FIVE_LESSONS'] LOOP
  valid:=CASE badge WHEN 'FIRST_LESSON' THEN lessons>=1 WHEN 'FIRST_COURSE' THEN courses>=1 ELSE lessons>=5 END;
  IF valid THEN INSERT INTO growth_learning_badges(owner_id,code) VALUES(who,badge) ON CONFLICT(owner_id,code) DO UPDATE SET revoked_at=NULL;
  ELSE UPDATE growth_learning_badges SET revoked_at=COALESCE(revoked_at,CURRENT_TIMESTAMP) WHERE owner_id=who AND code=badge; END IF;
 END LOOP;
END $$;
CREATE FUNCTION growth_completion_removed() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
 PERFORM pg_advisory_xact_lock(hashtextextended('growth-rewards:'||OLD.owner_id::text,0));
 INSERT INTO growth_reward_ledger(owner_id,source_type,completion_id,course_key,activity_key,rule_version,point_delta,reversal_of,reason)
 SELECT owner_id,source_type,completion_id,course_key,activity_key,rule_version,-point_delta,id,'Source completion removed' FROM growth_reward_ledger e WHERE completion_id=OLD.id AND owner_id=OLD.owner_id AND reversal_of IS NULL AND NOT EXISTS(SELECT 1 FROM growth_reward_ledger r WHERE r.reversal_of=e.id) ON CONFLICT DO NOTHING;
 PERFORM growth_refresh_badges(OLD.owner_id);RETURN OLD;
END $$;
CREATE TRIGGER growth_lesson_removed AFTER DELETE ON learning_lesson_completions FOR EACH ROW EXECUTE FUNCTION growth_completion_removed();
CREATE TRIGGER growth_exercise_removed AFTER DELETE ON learning_exercise_completions FOR EACH ROW EXECUTE FUNCTION growth_completion_removed();
CREATE TRIGGER growth_course_removed AFTER DELETE ON learning_course_completions FOR EACH ROW EXECUTE FUNCTION growth_completion_removed();
