CREATE TABLE professional_availability_slots (
    id UUID PRIMARY KEY,
    professional_user_id UUID NOT NULL REFERENCES professional_profiles(user_id) ON DELETE CASCADE,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    consultation_mode VARCHAR(80) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_professional_slot_time CHECK (ends_at > starts_at),
    CONSTRAINT chk_professional_slot_status CHECK (status IN ('AVAILABLE', 'BOOKED', 'CANCELLED')),
    CONSTRAINT uq_professional_slot UNIQUE (professional_user_id, starts_at, consultation_mode)
);

CREATE INDEX idx_professional_slots_upcoming
    ON professional_availability_slots (professional_user_id, status, starts_at);

ALTER TABLE consultation_requests
    ADD COLUMN availability_slot_id UUID NULL,
    ADD CONSTRAINT fk_consultation_availability_slot
        FOREIGN KEY (availability_slot_id) REFERENCES professional_availability_slots(id) ON DELETE SET NULL;

CREATE UNIQUE INDEX uq_consultation_availability_slot
    ON consultation_requests (availability_slot_id) WHERE availability_slot_id IS NOT NULL;
