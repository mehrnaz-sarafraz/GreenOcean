CREATE FUNCTION guard_listener_queue_transfer_link() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF OLD.status='TRANSFERRED' AND NEW.live_request_id IS DISTINCT FROM OLD.live_request_id AND NEW.live_request_id IS NOT NULL THEN RAISE EXCEPTION 'Transferred request identity is immutable' USING ERRCODE='23514';END IF;
 IF NEW.status='TRANSFERRED' AND OLD.status<>'TRANSFERRED' THEN
  IF NEW.expires_at<=listener_support_now() OR NEW.allocation_expires_at<=listener_support_now() OR NEW.live_request_id IS NULL
   OR NOT EXISTS(SELECT 1 FROM live_support_requests r WHERE r.id=NEW.live_request_id AND r.source_queue_id=NEW.id AND r.member_id=NEW.member_id AND r.listener_id=NEW.listener_id AND r.status='REQUESTED')
  THEN RAISE EXCEPTION 'Queue transfer must atomically bind its current Phase I request' USING ERRCODE='23514';END IF;
 ELSIF NEW.status<>'TRANSFERRED' AND NEW.live_request_id IS NOT NULL THEN RAISE EXCEPTION 'Only transferred entries can own a live request' USING ERRCODE='23514';END IF;
 RETURN NEW;END $$;
CREATE TRIGGER listener_queue_transfer_guard BEFORE UPDATE ON listener_wait_queue FOR EACH ROW EXECUTE FUNCTION guard_listener_queue_transfer_link();
