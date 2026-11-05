CREATE EXTENSION IF NOT EXISTS btree_gist WITH SCHEMA public;
CREATE FUNCTION listener_support_now() RETURNS TIMESTAMPTZ LANGUAGE SQL VOLATILE AS $$ SELECT clock_timestamp() $$;
CREATE TABLE listener_support_policy(
 id BOOLEAN PRIMARY KEY DEFAULT TRUE CHECK(id),
 planned_seconds INTEGER NOT NULL DEFAULT 1800 CHECK(planned_seconds=1800),
 extension_seconds INTEGER NOT NULL DEFAULT 600 CHECK(extension_seconds=600),
 queue_seconds INTEGER NOT NULL DEFAULT 1200 CHECK(queue_seconds=1200),
 upcoming_limit INTEGER NOT NULL DEFAULT 2 CHECK(upcoming_limit BETWEEN 1 AND 2),
 horizon_days INTEGER NOT NULL DEFAULT 14 CHECK(horizon_days BETWEEN 1 AND 90),
 lead_seconds INTEGER NOT NULL DEFAULT 1800 CHECK(lead_seconds BETWEEN 1800 AND 86400),
 checkin_seconds INTEGER NOT NULL DEFAULT 300 CHECK(checkin_seconds=300),
 grace_seconds INTEGER NOT NULL DEFAULT 600 CHECK(grace_seconds=600),
 match_seconds INTEGER NOT NULL DEFAULT 60 CHECK(match_seconds BETWEEN 15 AND 90),
 buffer_seconds INTEGER NOT NULL DEFAULT 300 CHECK(buffer_seconds BETWEEN 0 AND 1800),
 handoff_seconds INTEGER NOT NULL DEFAULT 150 CHECK(handoff_seconds BETWEEN 45 AND 210),
 queue_capacity INTEGER NOT NULL DEFAULT 100 CHECK(queue_capacity BETWEEN 1 AND 500),
 max_rules INTEGER NOT NULL DEFAULT 28 CHECK(max_rules BETWEEN 1 AND 50),
 max_exceptions INTEGER NOT NULL DEFAULT 100 CHECK(max_exceptions BETWEEN 1 AND 200));
INSERT INTO listener_support_policy(id) VALUES(TRUE);
CREATE TABLE listener_calendar_settings(
 listener_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
 timezone VARCHAR(80) NOT NULL DEFAULT 'UTC',paused BOOLEAN NOT NULL DEFAULT FALSE,revision BIGINT NOT NULL DEFAULT 1);
CREATE TABLE listener_calendar_rules(
 id UUID PRIMARY KEY DEFAULT uuidv7(),listener_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 timezone VARCHAR(80) NOT NULL,weekday SMALLINT NOT NULL CHECK(weekday BETWEEN 1 AND 7),
 starts_local TIME NOT NULL,ends_local TIME NOT NULL,valid_from DATE NOT NULL,valid_until DATE NOT NULL,
 overlap_policy VARCHAR(10) NOT NULL CHECK(overlap_policy IN('EARLIER','LATER','BOTH')),
 active BOOLEAN NOT NULL DEFAULT TRUE,revision BIGINT NOT NULL DEFAULT 1,
 CHECK(valid_until>=valid_from AND valid_until<=valid_from+366));
CREATE INDEX listener_rules_owner ON listener_calendar_rules(listener_id,active);
CREATE TABLE listener_calendar_exceptions(
 id UUID PRIMARY KEY DEFAULT uuidv7(),listener_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 timezone VARCHAR(80) NOT NULL,starts_at TIMESTAMPTZ NOT NULL,ends_at TIMESTAMPTZ NOT NULL,
 available BOOLEAN NOT NULL,active BOOLEAN NOT NULL DEFAULT TRUE,revision BIGINT NOT NULL DEFAULT 1,
 CHECK(ends_at>starts_at AND ends_at<=starts_at+INTERVAL '7 days'));
CREATE INDEX listener_exceptions_owner ON listener_calendar_exceptions(listener_id,starts_at);
CREATE TABLE listener_bookings(
 id UUID PRIMARY KEY DEFAULT uuidv7(),member_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 listener_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,client_key UUID NOT NULL,
 rule_id UUID REFERENCES listener_calendar_rules(id),exception_id UUID REFERENCES listener_calendar_exceptions(id),
 timezone VARCHAR(80) NOT NULL,starts_at TIMESTAMPTZ NOT NULL,ends_at TIMESTAMPTZ NOT NULL,reserved_until TIMESTAMPTZ NOT NULL,
 status VARCHAR(28) NOT NULL DEFAULT 'CONFIRMED' CHECK(status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED','ACTIVE','COMPLETED','CANCELLED_BY_MEMBER','CANCELLED_BY_LISTENER','CANCELLED_BY_SERVICE','MEMBER_NO_SHOW','LISTENER_NO_SHOW','EXPIRED')),
 checked_in_at TIMESTAMPTZ,live_request_id UUID UNIQUE REFERENCES live_support_requests(id) ON DELETE SET NULL,
 reminders BOOLEAN NOT NULL DEFAULT FALSE,reason_code VARCHAR(50),created_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now(),revision BIGINT NOT NULL DEFAULT 1,
 UNIQUE(member_id,client_key),CHECK(member_id<>listener_id AND ends_at=starts_at+INTERVAL '30 minutes' AND reserved_until>=ends_at),
 EXCLUDE USING gist(listener_id WITH =,tstzrange(starts_at,reserved_until,'[)') WITH &&) WHERE(status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED','ACTIVE')),
 EXCLUDE USING gist(member_id WITH =,tstzrange(starts_at,reserved_until,'[)') WITH &&) WHERE(status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED','ACTIVE')));
ALTER TABLE listener_bookings ADD COLUMN rescheduled_from UUID REFERENCES listener_bookings(id) ON DELETE SET NULL;
CREATE INDEX listener_booking_member ON listener_bookings(member_id,starts_at,id);
CREATE INDEX listener_booking_listener ON listener_bookings(listener_id,starts_at,id);
CREATE TABLE listener_support_notices(
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,entity_id UUID NOT NULL,kind VARCHAR(50) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now(),PRIMARY KEY(user_id,entity_id,kind));
ALTER TABLE live_support_requests ADD COLUMN source_booking_id UUID UNIQUE REFERENCES listener_bookings(id) ON DELETE SET NULL;
ALTER TABLE live_support_requests ADD COLUMN planned_seconds INTEGER CHECK(planned_seconds=1800);
ALTER TABLE live_support_requests ADD COLUMN planning_buffer_seconds INTEGER;
ALTER TABLE live_support_requests ADD COLUMN planned_ends_at TIMESTAMPTZ;
ALTER TABLE live_support_requests ADD COLUMN admission_until TIMESTAMPTZ;
CREATE FUNCTION listener_lock_pair(first_user UUID,second_user UUID) RETURNS VOID LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(hashtextextended('listener-capacity:'||LEAST(first_user,second_user)::text,0));
 IF first_user<>second_user THEN PERFORM pg_advisory_xact_lock(hashtextextended('listener-capacity:'||GREATEST(first_user,second_user)::text,0));END IF;
END $$;
CREATE FUNCTION listener_booking_free(a UUID,b UUID,start_time TIMESTAMPTZ,end_time TIMESTAMPTZ,excluded UUID DEFAULT NULL) RETURNS BOOLEAN LANGUAGE SQL VOLATILE AS $$
 SELECT NOT EXISTS(SELECT 1 FROM listener_bookings r WHERE r.id IS DISTINCT FROM excluded
 AND r.status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED','ACTIVE')
 AND (r.member_id IN(a,b) OR r.listener_id IN(a,b))
 AND tstzrange(r.starts_at,r.reserved_until,'[)')&&tstzrange(start_time,end_time,'[)'))
 AND NOT EXISTS(SELECT 1 FROM live_support_requests r WHERE r.status IN('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED')
 AND (r.member_id IN(a,b) OR r.listener_id IN(a,b))
 AND tstzrange(listener_support_now(),GREATEST(listener_support_now()+INTERVAL '1 microsecond',COALESCE(r.planned_ends_at+(r.planning_buffer_seconds*INTERVAL '1 second'),r.admission_until,'infinity'::timestamptz)),'[)')&&tstzrange(start_time,end_time,'[)')) $$;
CREATE FUNCTION listener_capacity_available(a UUID,b UUID,origin UUID DEFAULT NULL) RETURNS BOOLEAN LANGUAGE SQL VOLATILE AS $$
 SELECT NOT EXISTS(SELECT 1 FROM listener_bookings r CROSS JOIN listener_support_policy p
 WHERE r.id IS DISTINCT FROM origin AND r.status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED','ACTIVE')
 AND (r.member_id IN(a,b) OR r.listener_id IN(a,b))
 AND tstzrange(r.starts_at,r.reserved_until,'[)')&&tstzrange(listener_support_now(),listener_support_now()+((p.planned_seconds+p.handoff_seconds+p.buffer_seconds)*INTERVAL '1 second'),'[)')) $$;
CREATE FUNCTION guard_listener_booking() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 PERFORM listener_lock_pair(NEW.member_id,NEW.listener_id);
 IF TG_OP='UPDATE' AND (NEW.member_id IS DISTINCT FROM OLD.member_id OR NEW.listener_id IS DISTINCT FROM OLD.listener_id OR NEW.starts_at IS DISTINCT FROM OLD.starts_at OR NEW.ends_at IS DISTINCT FROM OLD.ends_at OR NEW.reserved_until IS DISTINCT FROM OLD.reserved_until OR NEW.client_key IS DISTINCT FROM OLD.client_key) THEN
 RAISE EXCEPTION 'Reservation identity and instants are immutable' USING ERRCODE='23514';END IF;
 IF TG_OP='INSERT' AND NOT listener_booking_free(NEW.member_id,NEW.listener_id,NEW.starts_at,NEW.reserved_until,NULL) THEN RAISE EXCEPTION 'Reservation capacity changed' USING ERRCODE='23P01';END IF;
 RETURN NEW;END $$;
CREATE TRIGGER listener_booking_guard BEFORE INSERT OR UPDATE ON listener_bookings FOR EACH ROW EXECUTE FUNCTION guard_listener_booking();
CREATE FUNCTION guard_listener_live_capacity() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE p listener_support_policy%ROWTYPE; required_until TIMESTAMPTZ;
BEGIN
 PERFORM pg_advisory_xact_lock(72491001);PERFORM listener_lock_pair(NEW.member_id,NEW.listener_id);
 SELECT * INTO p FROM listener_support_policy;
 IF TG_OP='INSERT' OR (TG_OP='UPDATE' AND NEW.status IN('ACCEPTED','ACTIVE') AND OLD.status IS DISTINCT FROM NEW.status AND OLD.status<>'INTERRUPTED') THEN
  required_until=listener_support_now()+((p.planned_seconds+p.handoff_seconds+p.buffer_seconds)*INTERVAL '1 second');
  IF EXISTS(SELECT 1 FROM listener_bookings b WHERE b.id IS DISTINCT FROM NEW.source_booking_id AND b.status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED','ACTIVE') AND(b.member_id IN(NEW.member_id,NEW.listener_id) OR b.listener_id IN(NEW.member_id,NEW.listener_id)) AND tstzrange(b.starts_at,b.reserved_until,'[)')&&tstzrange(listener_support_now(),required_until,'[)')) THEN RAISE EXCEPTION 'Upcoming reservation conflicts with this live handoff' USING ERRCODE='23P01';END IF;
 END IF;
 IF TG_OP='INSERT' AND NEW.source_booking_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM listener_bookings b WHERE b.id=NEW.source_booking_id AND b.member_id=NEW.member_id AND b.listener_id=NEW.listener_id AND b.status='CHECKED_IN' AND b.starts_at<=listener_support_now() AND b.starts_at+(p.grace_seconds*INTERVAL '1 second')>listener_support_now()) THEN RAISE EXCEPTION 'Scheduled handoff is unavailable' USING ERRCODE='23514';END IF;
 IF TG_OP='INSERT' AND NEW.planned_seconds IS NOT NULL THEN NEW.planned_seconds=p.planned_seconds;NEW.planning_buffer_seconds=p.buffer_seconds;NEW.admission_until=required_until;END IF;
 IF TG_OP='UPDATE' AND NEW.status='ACTIVE' AND OLD.status='ACCEPTED' AND NEW.planned_seconds IS NOT NULL THEN NEW.planned_ends_at=listener_support_now()+(NEW.planned_seconds*INTERVAL '1 second');END IF;
 RETURN NEW;END $$;
CREATE TRIGGER listener_live_capacity_guard BEFORE INSERT OR UPDATE OF status ON live_support_requests FOR EACH ROW EXECUTE FUNCTION guard_listener_live_capacity();
CREATE FUNCTION reconcile_listener_planning() RETURNS VOID LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(72491001);
 UPDATE live_support_requests SET status='COMPLETED',reason_code='PLANNED_DURATION_ENDED',ended_at=clock_timestamp()
 WHERE status IN('ACTIVE','INTERRUPTED') AND planned_ends_at<=listener_support_now();
 UPDATE listener_bookings b SET status='CANCELLED_BY_SERVICE',reason_code='AUTHORIZATION_CHANGED',revision=revision+1
 WHERE b.status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED') AND(NOT live_account_eligible(b.member_id) OR NOT live_listener_qualified(b.listener_id) OR live_pair_blocked(b.member_id,b.listener_id));
 UPDATE listener_bookings b SET status=CASE WHEN checked_in_at IS NULL THEN 'MEMBER_NO_SHOW' ELSE 'LISTENER_NO_SHOW' END,reason_code=CASE WHEN checked_in_at IS NULL THEN 'MEMBER_NOT_CHECKED_IN' ELSE 'LISTENER_UNAVAILABLE' END,revision=revision+1
 FROM listener_support_policy p WHERE b.status IN('CONFIRMED','CHECKED_IN') AND b.starts_at+(p.grace_seconds*INTERVAL '1 second')<=listener_support_now();
 UPDATE listener_bookings b SET status=CASE WHEN r.status IN('ACTIVE','INTERRUPTED') THEN 'ACTIVE' WHEN r.status='COMPLETED' THEN 'COMPLETED' WHEN r.status='ENDED_SAFETY' THEN 'CANCELLED_BY_SERVICE' ELSE 'EXPIRED' END,reason_code=r.reason_code,revision=b.revision+1
 FROM live_support_requests r WHERE r.id=b.live_request_id AND b.status IN('HANDOFF_REQUESTED','ACTIVE') AND r.status IN('ACTIVE','INTERRUPTED','COMPLETED','ENDED_SAFETY','EXPIRED','DECLINED','CANCELLED')
 AND b.status IS DISTINCT FROM CASE WHEN r.status IN('ACTIVE','INTERRUPTED') THEN 'ACTIVE' WHEN r.status='COMPLETED' THEN 'COMPLETED' WHEN r.status='ENDED_SAFETY' THEN 'CANCELLED_BY_SERVICE' ELSE 'EXPIRED' END;
END $$;
ALTER FUNCTION expire_live_support() RENAME TO expire_live_support_before_planning;
CREATE FUNCTION expire_live_support() RETURNS VOID LANGUAGE plpgsql AS $$ BEGIN
 PERFORM reconcile_listener_planning();PERFORM expire_live_support_before_planning();PERFORM reconcile_listener_planning();END $$;
