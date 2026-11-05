INSERT INTO roles(name) VALUES('CLINIC_REPUTATION_OPERATOR') ON CONFLICT DO NOTHING;
CREATE TABLE clinic_feedback(id UUID PRIMARY KEY DEFAULT gen_random_uuid(),appointment_id UUID NOT NULL UNIQUE REFERENCES consultation_requests(id),encounter_id UUID REFERENCES clinic_encounters(id),
 patient_id UUID NOT NULL REFERENCES users(id),professional_id UUID NOT NULL REFERENCES professional_profiles(user_id),revision BIGINT NOT NULL DEFAULT 1,
 state TEXT NOT NULL DEFAULT 'SUBMITTED' CHECK(state IN('SUBMITTED','APPROVED','PUBLISHED','REJECTED','WITHDRAWN','HELD')),
 moderated_by UUID REFERENCES users(id),public_text VARCHAR(2000),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 CHECK(patient_id<>professional_id));
CREATE TABLE clinic_feedback_versions(feedback_id UUID NOT NULL REFERENCES clinic_feedback(id),revision BIGINT NOT NULL,rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5),
 body_cipher BYTEA NOT NULL,include_aggregate BOOLEAN NOT NULL,publish_text BOOLEAN NOT NULL,retry_key UUID NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(feedback_id,revision),UNIQUE(feedback_id,retry_key));
CREATE TABLE clinic_feedback_events(id UUID PRIMARY KEY DEFAULT uuidv7(),feedback_id UUID NOT NULL REFERENCES clinic_feedback(id),revision BIGINT NOT NULL,
 actor_id UUID NOT NULL REFERENCES users(id),action TEXT NOT NULL,reason_code TEXT NOT NULL,public_text VARCHAR(2000),ordinal BIGINT GENERATED ALWAYS AS IDENTITY,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_feedback_responses(id UUID PRIMARY KEY DEFAULT uuidv7(),feedback_id UUID NOT NULL REFERENCES clinic_feedback(id),author_id UUID NOT NULL REFERENCES users(id),
 body_cipher BYTEA NOT NULL,public_text VARCHAR(2000),state TEXT NOT NULL DEFAULT 'SUBMITTED' CHECK(state IN('SUBMITTED','PUBLISHED','REJECTED')),reviewer_id UUID REFERENCES users(id),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_feedback_response_events(id UUID PRIMARY KEY DEFAULT uuidv7(),response_id UUID NOT NULL REFERENCES clinic_feedback_responses(id),actor_id UUID NOT NULL REFERENCES users(id),action TEXT NOT NULL,public_text VARCHAR(2000),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_reputation_cases(id UUID PRIMARY KEY DEFAULT uuidv7(),requester_id UUID NOT NULL REFERENCES users(id),professional_id UUID NOT NULL REFERENCES professional_profiles(user_id),
 feedback_id UUID REFERENCES clinic_feedback(id),appointment_id UUID REFERENCES consultation_requests(id),kind TEXT NOT NULL CHECK(kind IN('REVIEW_CONCERN','RANKING_APPEAL','CONDUCT','PRIVACY')),
 body_cipher BYTEA NOT NULL,state TEXT NOT NULL DEFAULT 'OPEN' CHECK(state IN('OPEN','FINAL','REVERSED')),
 result TEXT CHECK(result IN('DISMISSED','NO_SHOW_CONFIRMED','FEATURE_HOLD')),reviewer_id UUID REFERENCES users(id),evidence_reference VARCHAR(2000),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_reputation_case_events(id UUID PRIMARY KEY DEFAULT uuidv7(),case_id UUID NOT NULL REFERENCES clinic_reputation_cases(id),actor_id UUID NOT NULL REFERENCES users(id),
 action TEXT NOT NULL,result TEXT,evidence_reference VARCHAR(2000),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_ranking_policies(id UUID PRIMARY KEY DEFAULT uuidv7(),version VARCHAR(80) NOT NULL UNIQUE,window_days INTEGER NOT NULL CHECK(window_days BETWEEN 30 AND 365),
 minimum_patients INTEGER NOT NULL CHECK(minimum_patients BETWEEN 5 AND 1000),prior_weight INTEGER NOT NULL CHECK(prior_weight BETWEEN 1 AND 1000),prior_positive_percent INTEGER NOT NULL CHECK(prior_positive_percent BETWEEN 40 AND 80),
 maximum_features INTEGER NOT NULL CHECK(maximum_features BETWEEN 1 AND 10),rotation_weeks INTEGER NOT NULL CHECK(rotation_weeks BETWEEN 1 AND 52),minimum_episode_seconds INTEGER NOT NULL CHECK(minimum_episode_seconds BETWEEN 30 AND 3600),
 author_id UUID NOT NULL REFERENCES users(id),reviewer_id UUID REFERENCES users(id),expires_at TIMESTAMPTZ NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,approved_at TIMESTAMPTZ,
 CHECK(reviewer_id IS NULL OR reviewer_id<>author_id));
CREATE TABLE clinic_ranking_policy_events(id UUID PRIMARY KEY DEFAULT uuidv7(),policy_id UUID NOT NULL REFERENCES clinic_ranking_policies(id),actor_id UUID NOT NULL REFERENCES users(id),action TEXT NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_ranking_snapshots(id UUID PRIMARY KEY DEFAULT uuidv7(),policy_id UUID NOT NULL REFERENCES clinic_ranking_policies(id),profession TEXT NOT NULL,mode TEXT NOT NULL,
 actor_id UUID NOT NULL REFERENCES users(id),inputs_digest CHAR(64) NOT NULL,public_inputs JSONB NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE clinic_weekly_features(id UUID PRIMARY KEY DEFAULT uuidv7(),week_start DATE NOT NULL,profession TEXT NOT NULL,mode TEXT NOT NULL,revision BIGINT NOT NULL,
 category TEXT NOT NULL CHECK(category IN('RELIABLE_APPOINTMENTS','EDUCATIONAL_CONTRIBUTION','NEW_DISCOVERY')),snapshot_id UUID NOT NULL REFERENCES clinic_ranking_snapshots(id),
 author_id UUID NOT NULL REFERENCES users(id),reviewer_id UUID REFERENCES users(id),state TEXT NOT NULL DEFAULT 'DRAFT' CHECK(state IN('DRAFT','PUBLISHED','WITHDRAWN')),
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,UNIQUE(week_start,profession,mode,category,revision),CHECK(reviewer_id IS NULL OR reviewer_id<>author_id));
CREATE UNIQUE INDEX clinic_one_live_weekly_feature ON clinic_weekly_features(week_start,profession,mode,category) WHERE state='PUBLISHED';
CREATE TABLE clinic_weekly_feature_entries(batch_id UUID NOT NULL REFERENCES clinic_weekly_features(id),professional_id UUID NOT NULL REFERENCES professional_profiles(user_id),
 position INTEGER NOT NULL,public_basis JSONB NOT NULL,PRIMARY KEY(batch_id,professional_id),UNIQUE(batch_id,position));
CREATE TABLE clinic_weekly_feature_events(id UUID PRIMARY KEY DEFAULT uuidv7(),batch_id UUID NOT NULL REFERENCES clinic_weekly_features(id),actor_id UUID NOT NULL REFERENCES users(id),action TEXT NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
-- Future paid placement has a distinct domain and no publication or purchase implementation in Phase P.
CREATE TABLE clinic_sponsored_placement_requests(id UUID PRIMARY KEY DEFAULT uuidv7(),professional_id UUID NOT NULL REFERENCES professional_profiles(user_id),
 state TEXT NOT NULL DEFAULT 'BLOCKED_POLICY_REQUIRED' CHECK(state='BLOCKED_POLICY_REQUIRED'),created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TRIGGER clinic_feedback_version_immutable BEFORE UPDATE OR DELETE ON clinic_feedback_versions FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_feedback_event_immutable BEFORE UPDATE OR DELETE ON clinic_feedback_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_feedback_response_event_immutable BEFORE UPDATE OR DELETE ON clinic_feedback_response_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_reputation_case_event_immutable BEFORE UPDATE OR DELETE ON clinic_reputation_case_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_ranking_policy_event_immutable BEFORE UPDATE OR DELETE ON clinic_ranking_policy_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_ranking_snapshot_immutable BEFORE UPDATE OR DELETE ON clinic_ranking_snapshots FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_weekly_feature_entries_immutable BEFORE UPDATE OR DELETE ON clinic_weekly_feature_entries FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE TRIGGER clinic_weekly_feature_event_immutable BEFORE UPDATE OR DELETE ON clinic_weekly_feature_events FOR EACH ROW EXECUTE FUNCTION clinic_append_only();
CREATE INDEX clinic_feedback_professional ON clinic_feedback(professional_id,state,created_at DESC);
CREATE INDEX clinic_reputation_case_queue ON clinic_reputation_cases(state,created_at);
CREATE FUNCTION clinic_ranking_policy_immutable() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF ROW(NEW.version,NEW.window_days,NEW.minimum_patients,NEW.prior_weight,NEW.prior_positive_percent,NEW.maximum_features,NEW.rotation_weeks,NEW.minimum_episode_seconds,NEW.author_id,NEW.expires_at) IS DISTINCT FROM ROW(OLD.version,OLD.window_days,OLD.minimum_patients,OLD.prior_weight,OLD.prior_positive_percent,OLD.maximum_features,OLD.rotation_weeks,OLD.minimum_episode_seconds,OLD.author_id,OLD.expires_at) OR OLD.approved_at IS NOT NULL THEN RAISE EXCEPTION 'Ranking methodology requires a new independently approved version';END IF;RETURN NEW;END $$;
CREATE TRIGGER clinic_ranking_policy_guard BEFORE UPDATE ON clinic_ranking_policies FOR EACH ROW EXECUTE FUNCTION clinic_ranking_policy_immutable();
