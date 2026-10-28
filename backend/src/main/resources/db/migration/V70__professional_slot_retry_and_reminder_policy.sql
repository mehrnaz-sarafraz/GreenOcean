ALTER TABLE professional_availability_slots DROP CONSTRAINT uq_professional_slot;
CREATE UNIQUE INDEX professional_current_slot_start ON professional_availability_slots(professional_user_id,starts_at,consultation_mode) WHERE status<>'CANCELLED';
CREATE TABLE professional_reminder_policy(id BOOLEAN PRIMARY KEY DEFAULT TRUE CHECK(id),day_minutes INTEGER NOT NULL DEFAULT 1440 CHECK(day_minutes BETWEEN 60 AND 2880),hour_minutes INTEGER NOT NULL DEFAULT 60 CHECK(hour_minutes BETWEEN 15 AND 120),quarter_minutes INTEGER NOT NULL DEFAULT 15 CHECK(quarter_minutes BETWEEN 1 AND 30),start_enabled BOOLEAN NOT NULL DEFAULT TRUE);
INSERT INTO professional_reminder_policy(id) VALUES(TRUE);
