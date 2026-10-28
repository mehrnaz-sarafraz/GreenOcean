CREATE TABLE listener_queue_coverage_windows(
 id UUID PRIMARY KEY DEFAULT uuidv7(),listener_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 starts_at TIMESTAMPTZ NOT NULL,ends_at TIMESTAMPTZ NOT NULL,
 approved_by UUID REFERENCES users(id) ON DELETE SET NULL,revoked_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now(),
 CHECK(ends_at>starts_at AND ends_at<=starts_at+INTERVAL '24 hours'),CHECK(approved_by IS NULL OR approved_by<>listener_id));
CREATE INDEX listener_queue_coverage_owner ON listener_queue_coverage_windows(listener_id,ends_at);
CREATE TABLE listener_wait_queue(
 id UUID PRIMARY KEY DEFAULT uuidv7(),member_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 client_key UUID NOT NULL,sequence BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
 language VARCHAR(40),reminders BOOLEAN NOT NULL DEFAULT FALSE,
 admitted_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now(),expires_at TIMESTAMPTZ NOT NULL,
 status VARCHAR(24) NOT NULL DEFAULT 'WAITING' CHECK(status IN('WAITING','OFFERED','TRANSFERRED','CANCELLED','EXPIRED','SERVICE_UNAVAILABLE','SAFETY_TERMINATED')),
 listener_id UUID REFERENCES users(id) ON DELETE CASCADE,allocation_id UUID UNIQUE,allocation_expires_at TIMESTAMPTZ,
 live_request_id UUID UNIQUE REFERENCES live_support_requests(id) ON DELETE SET NULL,
 reason_code VARCHAR(50),updated_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now(),
 UNIQUE(member_id,client_key),CHECK(expires_at=admitted_at+INTERVAL '20 minutes'),
 CHECK(status<>'OFFERED' OR(listener_id IS NOT NULL AND allocation_id IS NOT NULL AND allocation_expires_at IS NOT NULL AND allocation_expires_at<=expires_at)),
 CHECK(member_id IS DISTINCT FROM listener_id));
CREATE UNIQUE INDEX listener_queue_member_active ON listener_wait_queue(member_id) WHERE status IN('WAITING','OFFERED');
CREATE UNIQUE INDEX listener_queue_listener_allocation ON listener_wait_queue(listener_id) WHERE status='OFFERED';
CREATE INDEX listener_queue_fifo ON listener_wait_queue(sequence) WHERE status='WAITING';
CREATE TABLE listener_queue_events(
 id UUID PRIMARY KEY DEFAULT uuidv7(),queue_id UUID NOT NULL REFERENCES listener_wait_queue(id) ON DELETE CASCADE,
 previous_status VARCHAR(24),next_status VARCHAR(24) NOT NULL,reason_code VARCHAR(50),
 created_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now());
ALTER TABLE live_support_requests ADD COLUMN source_queue_id UUID UNIQUE REFERENCES listener_wait_queue(id) ON DELETE SET NULL;
ALTER TABLE live_support_requests ADD CONSTRAINT live_support_single_origin CHECK(source_queue_id IS NULL OR source_booking_id IS NULL);
CREATE FUNCTION listener_queue_staffed(who UUID,until_time TIMESTAMPTZ) RETURNS BOOLEAN LANGUAGE SQL VOLATILE AS $$
 SELECT EXISTS(SELECT 1 FROM listener_queue_coverage_windows w JOIN users u ON u.id=w.approved_by
 WHERE w.listener_id=who AND w.revoked_at IS NULL AND w.starts_at<=listener_support_now() AND w.ends_at>=until_time
 AND u.status='ACTIVE' AND w.approved_by<>who
 AND EXISTS(SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=w.approved_by AND r.name IN('ADMIN','MODERATOR'))) $$;
ALTER FUNCTION listener_capacity_available(UUID,UUID,UUID) RENAME TO listener_calendar_capacity_available;
CREATE FUNCTION listener_all_capacity_available(a UUID,b UUID,booking UUID,queue_entry UUID) RETURNS BOOLEAN LANGUAGE SQL VOLATILE AS $$
 SELECT listener_calendar_capacity_available(a,b,booking) AND NOT EXISTS(SELECT 1 FROM listener_wait_queue q
 WHERE q.id IS DISTINCT FROM queue_entry AND
 ((q.status='WAITING' AND q.member_id IN(a,b)) OR(q.status='OFFERED' AND q.allocation_expires_at>listener_support_now() AND(q.member_id IN(a,b) OR q.listener_id IN(a,b))))) $$;
CREATE FUNCTION listener_capacity_available(a UUID,b UUID,origin UUID DEFAULT NULL) RETURNS BOOLEAN LANGUAGE SQL VOLATILE AS $$
 SELECT listener_all_capacity_available(a,b,origin,NULL) $$;
CREATE FUNCTION listener_queue_has_coverage(member UUID,selected_language TEXT) RETURNS BOOLEAN LANGUAGE SQL VOLATILE AS $$
 SELECT EXISTS(SELECT 1 FROM listener_presence p JOIN listener_profiles l ON l.user_id=p.listener_id
 CROSS JOIN listener_support_policy policy
 LEFT JOIN live_support_requests r ON(r.member_id=p.listener_id OR r.listener_id=p.listener_id) AND r.status IN('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED')
 LEFT JOIN listener_bookings origin ON origin.id=r.source_booking_id
 CROSS JOIN LATERAL(SELECT GREATEST(listener_support_now(),COALESCE(r.planned_ends_at+(COALESCE(r.planning_buffer_seconds,0)*INTERVAL '1 second'),listener_support_now()),COALESCE(origin.reserved_until,listener_support_now())) AS free_at) ready
 WHERE p.listener_id<>member AND live_listener_ready(p.listener_id) AND NOT live_pair_blocked(member,p.listener_id)
 AND(selected_language IS NULL OR selected_language=ANY(l.languages))
 AND(r.id IS NULL OR(r.status='ACTIVE' AND r.planned_ends_at IS NOT NULL AND ready.free_at<listener_support_now()+(policy.queue_seconds*INTERVAL '1 second')))
 AND listener_queue_staffed(p.listener_id,listener_support_now()+((policy.queue_seconds+policy.planned_seconds+policy.handoff_seconds+policy.buffer_seconds)*INTERVAL '1 second'))
 AND NOT EXISTS(SELECT 1 FROM listener_bookings b WHERE b.id IS DISTINCT FROM r.source_booking_id AND b.status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED','ACTIVE') AND(b.member_id IN(member,p.listener_id) OR b.listener_id IN(member,p.listener_id)) AND tstzrange(b.starts_at,b.reserved_until,'[)')&&tstzrange(ready.free_at,ready.free_at+((policy.planned_seconds+policy.handoff_seconds+policy.buffer_seconds+policy.match_seconds)*INTERVAL '1 second'),'[)'))) $$;
CREATE FUNCTION guard_listener_queue() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE p listener_support_policy%ROWTYPE;
BEGIN
 PERFORM pg_advisory_xact_lock(72491001);SELECT * INTO p FROM listener_support_policy;
 IF TG_OP='INSERT' THEN
  NEW.admitted_at=listener_support_now();NEW.expires_at=NEW.admitted_at+(p.queue_seconds*INTERVAL '1 second');
  NEW.status='WAITING';NEW.listener_id=NULL;NEW.allocation_id=NULL;NEW.allocation_expires_at=NULL;NEW.live_request_id=NULL;
 ELSE
  IF NEW.member_id IS DISTINCT FROM OLD.member_id OR NEW.client_key IS DISTINCT FROM OLD.client_key OR NEW.admitted_at IS DISTINCT FROM OLD.admitted_at OR NEW.expires_at IS DISTINCT FROM OLD.expires_at OR NEW.sequence IS DISTINCT FROM OLD.sequence OR NEW.language IS DISTINCT FROM OLD.language OR NEW.reminders IS DISTINCT FROM OLD.reminders THEN RAISE EXCEPTION 'Queue admission is immutable' USING ERRCODE='23514';END IF;
  IF NEW.status IS DISTINCT FROM OLD.status AND NOT((OLD.status='WAITING' AND NEW.status IN('OFFERED','CANCELLED','EXPIRED','SERVICE_UNAVAILABLE','SAFETY_TERMINATED')) OR(OLD.status='OFFERED' AND NEW.status IN('TRANSFERRED','CANCELLED','EXPIRED','SERVICE_UNAVAILABLE','SAFETY_TERMINATED'))) THEN RAISE EXCEPTION 'Invalid queue transition' USING ERRCODE='23514';END IF;
  IF OLD.status<>'WAITING' AND(NEW.listener_id IS DISTINCT FROM OLD.listener_id OR NEW.allocation_id IS DISTINCT FROM OLD.allocation_id OR NEW.allocation_expires_at IS DISTINCT FROM OLD.allocation_expires_at) THEN RAISE EXCEPTION 'Queue allocation is immutable' USING ERRCODE='23514';END IF;
 END IF;
 IF NEW.status='OFFERED' AND(TG_OP='INSERT' OR OLD.status='WAITING') THEN
  PERFORM listener_lock_pair(NEW.member_id,NEW.listener_id);
  IF NEW.expires_at<=listener_support_now() OR NEW.allocation_expires_at>LEAST(NEW.expires_at,listener_support_now()+(p.match_seconds*INTERVAL '1 second')) OR NEW.allocation_expires_at<=listener_support_now()
   OR NOT live_account_eligible(NEW.member_id) OR NOT live_listener_ready(NEW.listener_id) OR live_pair_blocked(NEW.member_id,NEW.listener_id)
   OR NOT listener_queue_staffed(NEW.listener_id,NEW.allocation_expires_at+((p.planned_seconds+p.handoff_seconds+p.buffer_seconds)*INTERVAL '1 second'))
   OR NOT listener_all_capacity_available(NEW.member_id,NEW.listener_id,NULL,NEW.id)
   OR NOT listener_booking_free(NEW.member_id,NEW.listener_id,listener_support_now(),NEW.allocation_expires_at+((p.planned_seconds+p.handoff_seconds+p.buffer_seconds)*INTERVAL '1 second'),NULL)
  THEN RAISE EXCEPTION 'Queue allocation capacity changed' USING ERRCODE='23P01';END IF;
 END IF;
 NEW.updated_at=listener_support_now();RETURN NEW;
END $$;
CREATE TRIGGER listener_queue_guard BEFORE INSERT OR UPDATE ON listener_wait_queue FOR EACH ROW EXECUTE FUNCTION guard_listener_queue();
CREATE FUNCTION audit_listener_queue() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF TG_OP='INSERT' THEN INSERT INTO listener_queue_events(queue_id,next_status,reason_code) VALUES(NEW.id,NEW.status,NEW.reason_code);
 ELSIF NEW.status IS DISTINCT FROM OLD.status THEN INSERT INTO listener_queue_events(queue_id,previous_status,next_status,reason_code) VALUES(NEW.id,OLD.status,NEW.status,NEW.reason_code);END IF;RETURN NEW;END $$;
CREATE INDEX listener_queue_events_entry ON listener_queue_events(queue_id,created_at,id);
CREATE TRIGGER listener_queue_event AFTER INSERT OR UPDATE ON listener_wait_queue FOR EACH ROW EXECUTE FUNCTION audit_listener_queue();
CREATE FUNCTION guard_listener_queue_live_capacity() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(72491001);PERFORM listener_lock_pair(NEW.member_id,NEW.listener_id);
 IF TG_OP='INSERT' OR(TG_OP='UPDATE' AND NEW.status IN('ACCEPTED','ACTIVE') AND NEW.status IS DISTINCT FROM OLD.status AND OLD.status<>'INTERRUPTED') THEN
  IF NOT listener_all_capacity_available(NEW.member_id,NEW.listener_id,NEW.source_booking_id,NEW.source_queue_id) THEN RAISE EXCEPTION 'Queue allocation occupies participant capacity' USING ERRCODE='23P01';END IF;
 END IF;
 IF TG_OP='INSERT' AND NEW.source_queue_id IS NOT NULL AND NOT EXISTS(SELECT 1 FROM listener_wait_queue q WHERE q.id=NEW.source_queue_id AND q.member_id=NEW.member_id AND q.listener_id=NEW.listener_id AND q.status='OFFERED' AND q.expires_at>listener_support_now() AND q.allocation_expires_at>listener_support_now() AND listener_queue_staffed(q.listener_id,listener_support_now()+(SELECT (planned_seconds+handoff_seconds+buffer_seconds)*INTERVAL '1 second' FROM listener_support_policy)) AND(q.language IS NULL OR EXISTS(SELECT 1 FROM listener_profiles l WHERE l.user_id=q.listener_id AND q.language=ANY(l.languages)))) THEN RAISE EXCEPTION 'Queue handoff expired' USING ERRCODE='23514';END IF;
 RETURN NEW;END $$;
CREATE TRIGGER listener_live_queue_guard BEFORE INSERT OR UPDATE OF status ON live_support_requests FOR EACH ROW EXECUTE FUNCTION guard_listener_queue_live_capacity();
CREATE FUNCTION guard_listener_booking_queue_capacity() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 PERFORM listener_lock_pair(NEW.member_id,NEW.listener_id);
 IF EXISTS(SELECT 1 FROM listener_wait_queue q CROSS JOIN listener_support_policy p WHERE q.status='OFFERED' AND q.allocation_expires_at>listener_support_now() AND(q.member_id IN(NEW.member_id,NEW.listener_id) OR q.listener_id IN(NEW.member_id,NEW.listener_id)) AND tstzrange(listener_support_now(),q.allocation_expires_at+((p.planned_seconds+p.handoff_seconds+p.buffer_seconds)*INTERVAL '1 second'),'[)')&&tstzrange(NEW.starts_at,NEW.reserved_until,'[)')) THEN RAISE EXCEPTION 'Queue proposal holds this capacity' USING ERRCODE='23P01';END IF;RETURN NEW;END $$;
CREATE TRIGGER listener_booking_queue_guard BEFORE INSERT ON listener_bookings FOR EACH ROW EXECUTE FUNCTION guard_listener_booking_queue_capacity();
CREATE FUNCTION reconcile_listener_queue() RETURNS VOID LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(72491001);
 UPDATE listener_wait_queue SET status='EXPIRED',reason_code=CASE WHEN expires_at<=listener_support_now() THEN 'WAIT_LIMIT_ENDED' ELSE 'READINESS_WINDOW_ENDED' END WHERE status IN('WAITING','OFFERED') AND(expires_at<=listener_support_now() OR(status='OFFERED' AND allocation_expires_at<=listener_support_now()));
 UPDATE listener_wait_queue SET status='SAFETY_TERMINATED',reason_code='AUTHORIZATION_CHANGED' WHERE status IN('WAITING','OFFERED') AND(NOT live_account_eligible(member_id) OR(status='OFFERED' AND(NOT live_listener_qualified(listener_id) OR live_pair_blocked(member_id,listener_id))));
 UPDATE listener_wait_queue q SET status='SERVICE_UNAVAILABLE',reason_code='LISTENER_UNAVAILABLE' FROM listener_support_policy p WHERE q.status='OFFERED' AND(NOT live_listener_ready(q.listener_id) OR NOT listener_queue_staffed(q.listener_id,q.allocation_expires_at+((p.planned_seconds+p.handoff_seconds+p.buffer_seconds)*INTERVAL '1 second')) OR(q.language IS NOT NULL AND NOT EXISTS(SELECT 1 FROM listener_profiles l WHERE l.user_id=q.listener_id AND q.language=ANY(l.languages))));
END $$;
ALTER FUNCTION expire_live_support() RENAME TO expire_live_support_before_queue;
CREATE FUNCTION expire_live_support() RETURNS VOID LANGUAGE plpgsql AS $$ BEGIN
 PERFORM expire_live_support_before_queue();PERFORM reconcile_listener_queue();END $$;
