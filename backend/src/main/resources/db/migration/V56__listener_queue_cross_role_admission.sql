CREATE OR REPLACE FUNCTION listener_queue_has_coverage(member UUID,selected_language TEXT) RETURNS BOOLEAN LANGUAGE SQL VOLATILE AS $$
 SELECT EXISTS(SELECT 1 FROM listener_presence p JOIN listener_profiles l ON l.user_id=p.listener_id
 CROSS JOIN listener_support_policy policy
 LEFT JOIN live_support_requests r ON(r.member_id=p.listener_id OR r.listener_id=p.listener_id) AND r.status IN('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED')
 LEFT JOIN listener_bookings origin ON origin.id=r.source_booking_id
 CROSS JOIN LATERAL(SELECT GREATEST(listener_support_now(),COALESCE(r.planned_ends_at+(COALESCE(r.planning_buffer_seconds,0)*INTERVAL '1 second'),listener_support_now()),COALESCE(origin.reserved_until,listener_support_now())) AS free_at) ready
 WHERE p.listener_id<>member AND NOT EXISTS(SELECT 1 FROM listener_wait_queue q WHERE q.member_id=p.listener_id AND q.status IN('WAITING','OFFERED')) AND live_listener_ready(p.listener_id) AND NOT live_pair_blocked(member,p.listener_id)
 AND(selected_language IS NULL OR selected_language=ANY(l.languages))
 AND(r.id IS NULL OR(r.status='ACTIVE' AND r.planned_ends_at IS NOT NULL AND ready.free_at<listener_support_now()+(policy.queue_seconds*INTERVAL '1 second')))
 AND listener_queue_staffed(p.listener_id,listener_support_now()+((policy.queue_seconds+policy.planned_seconds+policy.handoff_seconds+policy.buffer_seconds)*INTERVAL '1 second'))
 AND NOT EXISTS(SELECT 1 FROM listener_bookings b WHERE b.id IS DISTINCT FROM r.source_booking_id AND b.status IN('CONFIRMED','CHECKED_IN','HANDOFF_REQUESTED','ACTIVE') AND(b.member_id IN(member,p.listener_id) OR b.listener_id IN(member,p.listener_id)) AND tstzrange(b.starts_at,b.reserved_until,'[)')&&tstzrange(ready.free_at,ready.free_at+((policy.planned_seconds+policy.handoff_seconds+policy.buffer_seconds+policy.match_seconds)*INTERVAL '1 second'),'[)'))) $$;
CREATE FUNCTION guard_listener_queue_admission_capacity() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 PERFORM pg_advisory_xact_lock(72491001);PERFORM listener_lock_pair(NEW.member_id,NEW.member_id);
 IF NOT listener_all_capacity_available(NEW.member_id,NEW.member_id,NULL,NULL) OR EXISTS(SELECT 1 FROM live_support_requests r WHERE(r.member_id=NEW.member_id OR r.listener_id=NEW.member_id) AND r.status IN('REQUESTED','ACCEPTED','ACTIVE','INTERRUPTED')) THEN RAISE EXCEPTION 'Participant already holds support capacity' USING ERRCODE='23P01';END IF;
 RETURN NEW;END $$;
CREATE TRIGGER listener_queue_admission_guard BEFORE INSERT ON listener_wait_queue FOR EACH ROW EXECUTE FUNCTION guard_listener_queue_admission_capacity();
