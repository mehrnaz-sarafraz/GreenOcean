CREATE TABLE listener_training_progress (
    user_id UUID NOT NULL,
    module_key VARCHAR(40) NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_listener_training_progress PRIMARY KEY (user_id, module_key),
    CONSTRAINT chk_listener_training_progress_module CHECK (
        module_key IN ('boundaries', 'reflection', 'safety', 'culture', 'practice')
    ),
    CONSTRAINT fk_listener_training_progress_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);
