ALTER TABLE listener_wait_queue ADD COLUMN revision BIGINT NOT NULL DEFAULT 1 CHECK(revision>0);
CREATE FUNCTION increment_listener_queue_revision() RETURNS TRIGGER LANGUAGE plpgsql AS $$ BEGIN
 NEW.revision=OLD.revision+1;RETURN NEW;END $$;
CREATE TRIGGER listener_queue_revision BEFORE UPDATE ON listener_wait_queue FOR EACH ROW EXECUTE FUNCTION increment_listener_queue_revision();
