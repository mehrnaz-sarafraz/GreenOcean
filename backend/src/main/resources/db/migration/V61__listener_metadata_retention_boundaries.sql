ALTER TABLE listener_calendar_settings ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT listener_support_now();
ALTER TABLE listener_calendar_rules ADD COLUMN retired_at TIMESTAMPTZ;
ALTER TABLE listener_calendar_exceptions ADD COLUMN retired_at TIMESTAMPTZ;
UPDATE listener_calendar_rules SET retired_at=listener_support_now() WHERE NOT active;
UPDATE listener_calendar_exceptions SET retired_at=listener_support_now() WHERE NOT active;
CREATE FUNCTION timestamp_listener_calendar_change() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 IF TG_TABLE_NAME='listener_calendar_settings' THEN NEW.updated_at=listener_support_now();
 ELSIF OLD.active AND NOT NEW.active THEN NEW.retired_at=listener_support_now();END IF;RETURN NEW;END $$;
CREATE TRIGGER listener_calendar_settings_updated BEFORE UPDATE ON listener_calendar_settings FOR EACH ROW EXECUTE FUNCTION timestamp_listener_calendar_change();
CREATE TRIGGER listener_calendar_rule_retired BEFORE UPDATE ON listener_calendar_rules FOR EACH ROW EXECUTE FUNCTION timestamp_listener_calendar_change();
CREATE TRIGGER listener_calendar_exception_retired BEFORE UPDATE ON listener_calendar_exceptions FOR EACH ROW EXECUTE FUNCTION timestamp_listener_calendar_change();
CREATE OR REPLACE FUNCTION guard_listener_session_duration() RETURNS TRIGGER LANGUAGE plpgsql AS $$ DECLARE retention INTEGER;BEGIN
 PERFORM pg_advisory_xact_lock(72491001);
 IF TG_OP='INSERT' THEN NEW.planned_ends_at=NULL;NEW.extension_seconds_used=0;RETURN NEW;END IF;
 IF OLD.planned_seconds IS NOT NULL AND NEW.planned_seconds IS NULL AND NEW.planning_buffer_seconds IS NULL AND NEW.admission_until IS NULL AND NEW.planned_ends_at IS NULL AND NEW.extension_seconds_used=0 THEN
  retention=NULLIF(current_setting('greenocean.listener_metadata_retention_days',TRUE),'')::INTEGER;
  IF retention BETWEEN 1 AND 3650 AND OLD.status IN('COMPLETED','ENDED_SAFETY','EXPIRED','CANCELLED','DECLINED') AND OLD.ended_at<listener_support_now()-(retention*INTERVAL '1 day')
   AND(to_jsonb(NEW)-ARRAY['planned_seconds','planning_buffer_seconds','admission_until','planned_ends_at','extension_seconds_used']) IS NOT DISTINCT FROM(to_jsonb(OLD)-ARRAY['planned_seconds','planning_buffer_seconds','admission_until','planned_ends_at','extension_seconds_used'])
  THEN RETURN NEW;END IF;
 END IF;
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
