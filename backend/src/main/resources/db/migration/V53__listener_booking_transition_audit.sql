CREATE TABLE listener_booking_events(
 id UUID PRIMARY KEY DEFAULT uuidv7(),booking_id UUID NOT NULL REFERENCES listener_bookings(id) ON DELETE CASCADE,
 previous_status VARCHAR(28),next_status VARCHAR(28) NOT NULL,reason_code VARCHAR(50),
 created_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now());
CREATE INDEX listener_booking_events_owner ON listener_booking_events(booking_id,created_at,id);
CREATE FUNCTION guard_listener_booking_transition() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF TG_OP='UPDATE' AND NEW.status IS DISTINCT FROM OLD.status AND NOT(
  (OLD.status='CONFIRMED' AND NEW.status IN('CHECKED_IN','CANCELLED_BY_MEMBER','CANCELLED_BY_LISTENER','CANCELLED_BY_SERVICE','MEMBER_NO_SHOW')) OR
  (OLD.status='CHECKED_IN' AND NEW.status IN('HANDOFF_REQUESTED','CANCELLED_BY_MEMBER','CANCELLED_BY_LISTENER','CANCELLED_BY_SERVICE','LISTENER_NO_SHOW')) OR
  (OLD.status='HANDOFF_REQUESTED' AND NEW.status IN('ACTIVE','EXPIRED','CANCELLED_BY_MEMBER','CANCELLED_BY_LISTENER','CANCELLED_BY_SERVICE')) OR
  (OLD.status='ACTIVE' AND NEW.status IN('COMPLETED','CANCELLED_BY_SERVICE'))
 ) THEN RAISE EXCEPTION 'Invalid booking state transition' USING ERRCODE='23514';END IF;
 RETURN NEW;END $$;
CREATE TRIGGER listener_booking_state_guard BEFORE UPDATE ON listener_bookings FOR EACH ROW EXECUTE FUNCTION guard_listener_booking_transition();
CREATE FUNCTION audit_listener_booking_transition() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF TG_OP='INSERT' THEN INSERT INTO listener_booking_events(booking_id,next_status,reason_code) VALUES(NEW.id,NEW.status,NEW.reason_code);
 ELSIF OLD.status IS DISTINCT FROM NEW.status THEN INSERT INTO listener_booking_events(booking_id,previous_status,next_status,reason_code) VALUES(NEW.id,OLD.status,NEW.status,NEW.reason_code);END IF;
 RETURN NEW;END $$;
CREATE TRIGGER listener_booking_transition_event AFTER INSERT OR UPDATE ON listener_bookings FOR EACH ROW EXECUTE FUNCTION audit_listener_booking_transition();
