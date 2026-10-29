-- Repeated readiness heartbeats must not create repeated account notices.
CREATE OR REPLACE FUNCTION clinic_state_notice() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE patient UUID; professional UUID; appointment UUID; event_key TEXT;
BEGIN
 IF TG_TABLE_NAME='consultation_requests' THEN
  IF NOT NEW.clinic_governed THEN RETURN NEW; END IF;
  IF TG_OP='UPDATE' AND NEW.status=OLD.status THEN RETURN NEW; END IF;
  patient:=NEW.requester_user_id;professional:=NEW.professional_user_id;appointment:=NEW.id;
  event_key:='appointment:'||NEW.id||':'||NEW.booking_revision;
 ELSE
  SELECT requester_user_id,professional_user_id INTO patient,professional FROM consultation_requests WHERE id=NEW.appointment_id;
  appointment:=NEW.appointment_id;
  IF TG_TABLE_NAME='clinic_readiness_evidence' THEN event_key:='readiness:'||appointment||':'||NEW.actor_id;
  ELSE event_key:='ledger:'||NEW.id; END IF;
 END IF;
 INSERT INTO clinic_notice_deliveries(user_id,dedup_key) VALUES(patient,event_key) ON CONFLICT DO NOTHING;
 IF FOUND THEN INSERT INTO notifications(user_id,type,title,body) VALUES(patient,'SYSTEM_NOTICE','Account update','Your authenticated account has an updated service status. Open the app to review it.'); END IF;
 INSERT INTO clinic_notice_deliveries(user_id,dedup_key) VALUES(professional,event_key) ON CONFLICT DO NOTHING;
 IF FOUND THEN INSERT INTO notifications(user_id,type,title,body) VALUES(professional,'SYSTEM_NOTICE','Account update','Your authenticated professional workspace has an updated service status.'); END IF;
 RETURN NEW;
END $$;
