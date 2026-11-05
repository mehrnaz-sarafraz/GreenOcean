ALTER TABLE listener_support_notices ADD COLUMN notification_id UUID REFERENCES notifications(id) ON DELETE SET NULL;
CREATE TABLE listener_support_worker_state(name VARCHAR(50) PRIMARY KEY,last_success_at TIMESTAMPTZ NOT NULL);
