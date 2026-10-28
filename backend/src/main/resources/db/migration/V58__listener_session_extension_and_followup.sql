ALTER TABLE listener_support_policy ADD COLUMN extension_offer_seconds INTEGER NOT NULL DEFAULT 60 CHECK(extension_offer_seconds BETWEEN 15 AND 120);
ALTER TABLE listener_support_policy ADD COLUMN warning_early_seconds INTEGER NOT NULL DEFAULT 300 CHECK(warning_early_seconds BETWEEN 120 AND 900);
ALTER TABLE listener_support_policy ADD COLUMN warning_final_seconds INTEGER NOT NULL DEFAULT 60 CHECK(warning_final_seconds BETWEEN 15 AND 120);
ALTER TABLE listener_support_policy ADD CONSTRAINT listener_warning_order CHECK(warning_early_seconds>warning_final_seconds);
ALTER TABLE live_support_requests ADD COLUMN extension_seconds_used INTEGER NOT NULL DEFAULT 0 CHECK(extension_seconds_used IN(0,600));
CREATE TABLE listener_session_extensions(
 id UUID PRIMARY KEY DEFAULT uuidv7(),session_id UUID NOT NULL UNIQUE REFERENCES live_support_requests(id) ON DELETE CASCADE,
 requester_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,client_key UUID NOT NULL,
 seconds INTEGER NOT NULL DEFAULT 600 CHECK(seconds=600),original_deadline TIMESTAMPTZ NOT NULL,expires_at TIMESTAMPTZ NOT NULL,
 member_approved_at TIMESTAMPTZ,listener_approved_at TIMESTAMPTZ,
 status VARCHAR(16) NOT NULL DEFAULT 'PENDING' CHECK(status IN('PENDING','APPLIED','DECLINED','EXPIRED','UNAVAILABLE')),
 reason_code VARCHAR(50),created_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now(),applied_at TIMESTAMPTZ,
 UNIQUE(session_id,client_key),CHECK(expires_at<=original_deadline));
CREATE TABLE listener_session_feedback(
 session_id UUID PRIMARY KEY REFERENCES live_support_requests(id) ON DELETE CASCADE,
 owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 helpful BOOLEAN,respected BOOLEAN,would_request_again BOOLEAN,
 revision BIGINT NOT NULL DEFAULT 1 CHECK(revision>0),created_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now(),updated_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now(),
 CHECK(helpful IS NOT NULL OR respected IS NOT NULL OR would_request_again IS NOT NULL));
CREATE TABLE listener_session_warning_notices(
 session_id UUID NOT NULL REFERENCES live_support_requests(id) ON DELETE CASCADE,
 kind VARCHAR(20) NOT NULL CHECK(kind IN('ENDING_EARLY','ENDING_FINAL')),deadline TIMESTAMPTZ NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now(),PRIMARY KEY(session_id,kind,deadline));
CREATE FUNCTION listener_extension_capacity(request UUID,until_time TIMESTAMPTZ) RETURNS BOOLEAN LANGUAGE SQL VOLATILE AS $$
 SELECT EXISTS(SELECT 1 FROM live_support_requests r WHERE r.id=request AND until_time>listener_support_now()
 AND NOT EXISTS(SELECT 1 FROM listener_bookings b WHERE b.id IS DISTINCT FROM r.source_booking_id AND b.status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED','ACTIVE') AND(b.member_id IN(r.member_id,r.listener_id) OR b.listener_id IN(r.member_id,r.listener_id)) AND tstzrange(b.starts_at,b.reserved_until,'[)')&&tstzrange(listener_support_now(),GREATEST(until_time+(r.planning_buffer_seconds*INTERVAL '1 second'),listener_support_now()+INTERVAL '1 microsecond'),'[)'))
 AND NOT EXISTS(SELECT 1 FROM live_support_requests other WHERE other.id<>r.id AND other.status IN('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED') AND(other.member_id IN(r.member_id,r.listener_id) OR other.listener_id IN(r.member_id,r.listener_id)))
 AND NOT EXISTS(SELECT 1 FROM listener_wait_queue q WHERE q.status='OFFERED' AND q.allocation_expires_at>listener_support_now() AND(q.member_id IN(r.member_id,r.listener_id) OR q.listener_id IN(r.member_id,r.listener_id)))
 AND(r.source_queue_id IS NULL OR listener_queue_staffed(r.listener_id,until_time+(r.planning_buffer_seconds*INTERVAL '1 second')))) $$;
CREATE FUNCTION listener_session_extendible(request UUID) RETURNS BOOLEAN LANGUAGE SQL VOLATILE AS $$
 SELECT EXISTS(SELECT 1 FROM live_support_requests r CROSS JOIN listener_support_policy p WHERE r.id=request AND r.status='ACTIVE' AND r.planned_seconds IS NOT NULL AND r.extension_seconds_used=0 AND r.planned_ends_at>listener_support_now()
 AND r.member_seen_at+(r.presence_seconds*INTERVAL '1 second')>clock_timestamp() AND r.listener_seen_at+(r.presence_seconds*INTERVAL '1 second')>clock_timestamp()
 AND live_account_eligible(r.member_id) AND live_listener_qualified(r.listener_id) AND NOT live_pair_blocked(r.member_id,r.listener_id)
 AND listener_extension_capacity(r.id,r.planned_ends_at+(p.extension_seconds*INTERVAL '1 second'))) $$;
CREATE FUNCTION guard_listener_extension() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE r live_support_requests%ROWTYPE;p listener_support_policy%ROWTYPE;
BEGIN
 PERFORM pg_advisory_xact_lock(72491001);SELECT * INTO r FROM live_support_requests WHERE id=NEW.session_id;
 PERFORM listener_lock_pair(r.member_id,r.listener_id);SELECT * INTO p FROM listener_support_policy;
 IF TG_OP='INSERT' THEN
  IF NEW.requester_id NOT IN(r.member_id,r.listener_id) OR NOT listener_session_extendible(r.id) THEN RAISE EXCEPTION 'Extension is not currently authorized' USING ERRCODE='23514';END IF;
  NEW.status='PENDING';NEW.seconds=p.extension_seconds;NEW.original_deadline=r.planned_ends_at;NEW.expires_at=LEAST(r.planned_ends_at,listener_support_now()+(p.extension_offer_seconds*INTERVAL '1 second'));
  NEW.member_approved_at=CASE WHEN NEW.requester_id=r.member_id THEN listener_support_now() END;
  NEW.listener_approved_at=CASE WHEN NEW.requester_id=r.listener_id THEN listener_support_now() END;
 ELSE
  IF NEW.session_id IS DISTINCT FROM OLD.session_id OR NEW.requester_id IS DISTINCT FROM OLD.requester_id OR NEW.client_key IS DISTINCT FROM OLD.client_key OR NEW.original_deadline IS DISTINCT FROM OLD.original_deadline OR NEW.expires_at IS DISTINCT FROM OLD.expires_at OR NEW.seconds IS DISTINCT FROM OLD.seconds THEN RAISE EXCEPTION 'Extension proposal is immutable' USING ERRCODE='23514';END IF;
  IF(OLD.member_approved_at IS NOT NULL AND NEW.member_approved_at IS DISTINCT FROM OLD.member_approved_at) OR(OLD.listener_approved_at IS NOT NULL AND NEW.listener_approved_at IS DISTINCT FROM OLD.listener_approved_at) THEN RAISE EXCEPTION 'Recorded extension consent is immutable' USING ERRCODE='23514';END IF;
  IF OLD.status<>'PENDING' AND(NEW.status IS DISTINCT FROM OLD.status OR NEW.member_approved_at IS DISTINCT FROM OLD.member_approved_at OR NEW.listener_approved_at IS DISTINCT FROM OLD.listener_approved_at) THEN RAISE EXCEPTION 'Extension proposal is terminal' USING ERRCODE='23514';END IF;
  IF NEW.status='APPLIED' AND OLD.status<>'APPLIED' AND(NEW.member_approved_at IS NULL OR NEW.listener_approved_at IS NULL OR NEW.expires_at<=listener_support_now() OR NOT listener_session_extendible(r.id) OR r.planned_ends_at IS DISTINCT FROM NEW.original_deadline) THEN RAISE EXCEPTION 'Both current participants and capacity must approve the same live extension' USING ERRCODE='23P01';END IF;
 END IF;RETURN NEW;
END $$;
CREATE TRIGGER listener_extension_guard BEFORE INSERT OR UPDATE ON listener_session_extensions FOR EACH ROW EXECUTE FUNCTION guard_listener_extension();
CREATE FUNCTION guard_listener_session_duration() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(72491001);
 IF TG_OP='INSERT' THEN NEW.planned_ends_at=NULL;NEW.extension_seconds_used=0;RETURN NEW;END IF;
 IF NEW.planned_seconds IS DISTINCT FROM OLD.planned_seconds OR NEW.planning_buffer_seconds IS DISTINCT FROM OLD.planning_buffer_seconds OR NEW.admission_until IS DISTINCT FROM OLD.admission_until THEN RAISE EXCEPTION 'Captured support duration policy is immutable' USING ERRCODE='23514';END IF;
 IF OLD.status='ACCEPTED' AND NEW.status='ACTIVE' AND NEW.planned_seconds IS NOT NULL THEN
  NEW.activated_at=listener_support_now();NEW.planned_ends_at=NEW.activated_at+(NEW.planned_seconds*INTERVAL '1 second');NEW.extension_seconds_used=0;
 ELSE
  IF NEW.planned_seconds IS NOT NULL AND NEW.activated_at IS DISTINCT FROM OLD.activated_at THEN RAISE EXCEPTION 'Activation does not restart the duration clock' USING ERRCODE='23514';END IF;
  IF NEW.planned_ends_at IS DISTINCT FROM OLD.planned_ends_at OR NEW.extension_seconds_used IS DISTINCT FROM OLD.extension_seconds_used THEN
   PERFORM listener_lock_pair(NEW.member_id,NEW.listener_id);
   IF OLD.extension_seconds_used<>0 OR NEW.extension_seconds_used<>600 OR NEW.status<>'ACTIVE' OR OLD.status<>'ACTIVE' OR NEW.planned_ends_at IS DISTINCT FROM OLD.planned_ends_at+INTERVAL '10 minutes'
    OR NOT listener_session_extendible(OLD.id)
    OR NOT EXISTS(SELECT 1 FROM listener_session_extensions e WHERE e.session_id=OLD.id AND e.status='APPLIED' AND e.member_approved_at IS NOT NULL AND e.listener_approved_at IS NOT NULL AND e.original_deadline=OLD.planned_ends_at AND e.expires_at>listener_support_now())
   THEN RAISE EXCEPTION 'Duration can change only through one authorized mutually approved extension' USING ERRCODE='23514';END IF;
  END IF;
 END IF;RETURN NEW;END $$;
CREATE TRIGGER listener_session_duration_guard BEFORE INSERT OR UPDATE ON live_support_requests FOR EACH ROW EXECUTE FUNCTION guard_listener_session_duration();
CREATE FUNCTION apply_listener_extension() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF NEW.status='APPLIED' AND OLD.status IS DISTINCT FROM NEW.status THEN
  UPDATE live_support_requests SET planned_ends_at=NEW.original_deadline+(NEW.seconds*INTERVAL '1 second'),extension_seconds_used=NEW.seconds WHERE id=NEW.session_id AND status='ACTIVE' AND planned_ends_at=NEW.original_deadline AND extension_seconds_used=0;
  IF NOT FOUND THEN RAISE EXCEPTION 'Live extension could not be applied' USING ERRCODE='23P01';END IF;
 END IF;RETURN NEW;END $$;
CREATE TRIGGER listener_extension_apply AFTER UPDATE ON listener_session_extensions FOR EACH ROW EXECUTE FUNCTION apply_listener_extension();
CREATE FUNCTION guard_listener_feedback_owner() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF NOT EXISTS(SELECT 1 FROM live_support_requests r WHERE r.id=NEW.session_id AND r.member_id=NEW.owner_id AND r.status='COMPLETED' AND r.planned_seconds IS NOT NULL) THEN RAISE EXCEPTION 'Feedback requires its owning member and a completed N session' USING ERRCODE='23514';END IF;
 IF TG_OP='UPDATE' AND(NEW.owner_id IS DISTINCT FROM OLD.owner_id OR NEW.session_id IS DISTINCT FROM OLD.session_id) THEN RAISE EXCEPTION 'Feedback ownership is immutable' USING ERRCODE='23514';END IF;RETURN NEW;END $$;
CREATE TRIGGER listener_feedback_guard BEFORE INSERT OR UPDATE ON listener_session_feedback FOR EACH ROW EXECUTE FUNCTION guard_listener_feedback_owner();
-- Reconciliation only releases capacity; it must not accumulate participant locks
-- across unrelated rows. Acquisition and extension retain ordered pair locks.
CREATE OR REPLACE FUNCTION guard_listener_booking() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF TG_OP='UPDATE' THEN
  IF NEW.member_id IS DISTINCT FROM OLD.member_id OR NEW.listener_id IS DISTINCT FROM OLD.listener_id OR NEW.starts_at IS DISTINCT FROM OLD.starts_at OR NEW.ends_at IS DISTINCT FROM OLD.ends_at OR NEW.reserved_until IS DISTINCT FROM OLD.reserved_until OR NEW.client_key IS DISTINCT FROM OLD.client_key THEN RAISE EXCEPTION 'Reservation identity and instants are immutable' USING ERRCODE='23514';END IF;
 ELSE
  PERFORM listener_lock_pair(NEW.member_id,NEW.listener_id);
  IF NOT listener_booking_free(NEW.member_id,NEW.listener_id,NEW.starts_at,NEW.reserved_until,NULL) THEN RAISE EXCEPTION 'Reservation capacity changed' USING ERRCODE='23P01';END IF;
 END IF;RETURN NEW;END $$;
CREATE OR REPLACE FUNCTION guard_listener_live_capacity() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE p listener_support_policy%ROWTYPE;required_until TIMESTAMPTZ;handoff INTERVAL;
BEGIN
 PERFORM pg_advisory_xact_lock(72491001);SELECT * INTO p FROM listener_support_policy;
 IF TG_OP='INSERT' OR(NEW.status IN('ACCEPTED','ACTIVE') AND OLD.status IS DISTINCT FROM NEW.status AND OLD.status<>'INTERRUPTED') THEN
  PERFORM listener_lock_pair(NEW.member_id,NEW.listener_id);
  handoff=CASE WHEN TG_OP='INSERT' THEN p.handoff_seconds*INTERVAL '1 second' WHEN NEW.status='ACCEPTED' THEN GREATEST(INTERVAL '0 seconds',NEW.activation_expires_at-listener_support_now()) ELSE INTERVAL '0 seconds' END;
  required_until=listener_support_now()+((p.planned_seconds+p.buffer_seconds)*INTERVAL '1 second')+handoff;
  IF EXISTS(SELECT 1 FROM listener_bookings b WHERE b.id IS DISTINCT FROM NEW.source_booking_id AND b.status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED','ACTIVE') AND(b.member_id IN(NEW.member_id,NEW.listener_id) OR b.listener_id IN(NEW.member_id,NEW.listener_id)) AND tstzrange(b.starts_at,b.reserved_until,'[)')&&tstzrange(listener_support_now(),required_until,'[)')) THEN RAISE EXCEPTION 'Upcoming reservation conflicts with this live handoff' USING ERRCODE='23P01';END IF;
 END IF;
 IF TG_OP='INSERT' AND NEW.source_booking_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM listener_bookings b WHERE b.id=NEW.source_booking_id AND b.member_id=NEW.member_id AND b.listener_id=NEW.listener_id AND b.status='CHECKED_IN' AND b.starts_at<=listener_support_now() AND b.starts_at+(p.grace_seconds*INTERVAL '1 second')>listener_support_now()) THEN RAISE EXCEPTION 'Scheduled handoff is unavailable' USING ERRCODE='23514';END IF;
 IF TG_OP='INSERT' AND NEW.planned_seconds IS NOT NULL THEN NEW.planned_seconds=p.planned_seconds;NEW.planning_buffer_seconds=p.buffer_seconds;NEW.admission_until=required_until;END IF;
 IF TG_OP='UPDATE' AND NEW.status='ACTIVE' AND OLD.status='ACCEPTED' AND NEW.planned_seconds IS NOT NULL THEN NEW.planned_ends_at=listener_support_now()+(NEW.planned_seconds*INTERVAL '1 second');END IF;
 RETURN NEW;END $$;
CREATE OR REPLACE FUNCTION guard_listener_queue_live_capacity() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(72491001);
 IF TG_OP='INSERT' OR(TG_OP='UPDATE' AND NEW.status IN('ACCEPTED','ACTIVE') AND NEW.status IS DISTINCT FROM OLD.status AND OLD.status<>'INTERRUPTED') THEN
  PERFORM listener_lock_pair(NEW.member_id,NEW.listener_id);
  IF EXISTS(SELECT 1 FROM listener_wait_queue q WHERE q.id IS DISTINCT FROM NEW.source_queue_id AND((q.status='WAITING' AND q.member_id IN(NEW.member_id,NEW.listener_id)) OR(q.status='OFFERED' AND q.allocation_expires_at>listener_support_now() AND(q.member_id IN(NEW.member_id,NEW.listener_id) OR q.listener_id IN(NEW.member_id,NEW.listener_id))))) THEN RAISE EXCEPTION 'Queue allocation occupies participant capacity' USING ERRCODE='23P01';END IF;
 END IF;
 IF TG_OP='INSERT' AND NEW.source_queue_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM listener_wait_queue q WHERE q.id=NEW.source_queue_id AND q.member_id=NEW.member_id AND q.listener_id=NEW.listener_id AND q.status='OFFERED' AND q.expires_at>listener_support_now() AND q.allocation_expires_at>listener_support_now() AND listener_queue_staffed(q.listener_id,listener_support_now()+(SELECT(planned_seconds+handoff_seconds+buffer_seconds)*INTERVAL '1 second' FROM listener_support_policy)) AND(q.language IS NULL OR EXISTS(SELECT 1 FROM listener_profiles l WHERE l.user_id=q.listener_id AND q.language=ANY(l.languages)))) THEN RAISE EXCEPTION 'Queue handoff expired' USING ERRCODE='23514';END IF;
 RETURN NEW;END $$;
CREATE OR REPLACE FUNCTION guard_listener_booking_transition() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF NEW.status IS DISTINCT FROM OLD.status AND NOT(
  (OLD.status='CONFIRMED' AND NEW.status IN('CHECKED_IN','CANCELLED_BY_MEMBER','CANCELLED_BY_LISTENER','CANCELLED_BY_SERVICE','MEMBER_NO_SHOW')) OR
  (OLD.status='CHECKED_IN' AND NEW.status IN('HANDOFF_REQUESTED','CANCELLED_BY_MEMBER','CANCELLED_BY_LISTENER','CANCELLED_BY_SERVICE','LISTENER_NO_SHOW','EXPIRED')) OR
  (OLD.status='HANDOFF_REQUESTED' AND NEW.status IN('ACTIVE','COMPLETED','EXPIRED','CANCELLED_BY_MEMBER','CANCELLED_BY_LISTENER','CANCELLED_BY_SERVICE')) OR
  (OLD.status='ACTIVE' AND NEW.status IN('COMPLETED','CANCELLED_BY_SERVICE'))
 ) THEN RAISE EXCEPTION 'Invalid booking state transition' USING ERRCODE='23514';END IF;RETURN NEW;END $$;
CREATE FUNCTION reconcile_listener_no_shows() RETURNS VOID LANGUAGE plpgsql AS $$ BEGIN
 UPDATE listener_bookings b SET status='EXPIRED',reason_code='HANDOFF_NOT_COMPLETED',revision=b.revision+1 FROM listener_support_policy p
 WHERE b.status='CHECKED_IN' AND b.starts_at+(p.grace_seconds*INTERVAL '1 second')<=listener_support_now() AND live_listener_ready(b.listener_id);
END $$;
ALTER FUNCTION expire_live_support() RENAME TO expire_live_support_before_followup;
CREATE FUNCTION expire_live_support() RETURNS VOID LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(72491001);PERFORM reconcile_listener_no_shows();PERFORM expire_live_support_before_followup();
 UPDATE listener_session_extensions e SET status='EXPIRED',reason_code=CASE WHEN e.expires_at<=listener_support_now() THEN 'CONSENT_WINDOW_ENDED' ELSE 'SESSION_NOT_ACTIVE' END FROM live_support_requests r WHERE e.session_id=r.id AND e.status='PENDING' AND(e.expires_at<=listener_support_now() OR r.status<>'ACTIVE');
END $$;
