CREATE TABLE wellbeing_reflections (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    answers SMALLINT[] NOT NULL,
    score SMALLINT NOT NULL,
    summary_band VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_wellbeing_reflections_answers CHECK (cardinality(answers) = 5),
    CONSTRAINT chk_wellbeing_reflections_score CHECK (score BETWEEN 0 AND 20),
    CONSTRAINT chk_wellbeing_reflections_band CHECK (summary_band IN ('SUPPORTED', 'MIXED', 'STRAINED')),
    CONSTRAINT fk_wellbeing_reflections_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_wellbeing_reflections_user_created
    ON wellbeing_reflections (user_id, created_at DESC);
