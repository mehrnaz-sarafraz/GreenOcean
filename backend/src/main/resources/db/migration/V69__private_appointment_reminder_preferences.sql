CREATE TABLE professional_appointment_reminder_preferences(
 appointment_id UUID NOT NULL REFERENCES consultation_requests(id) ON DELETE CASCADE,
 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,enabled BOOLEAN NOT NULL DEFAULT FALSE,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(appointment_id,user_id));
