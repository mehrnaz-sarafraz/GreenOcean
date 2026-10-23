CREATE TABLE guided_growth_paths (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    slug VARCHAR(100) NOT NULL UNIQUE,
    title VARCHAR(160) NOT NULL,
    summary VARCHAR(1000) NOT NULL,
    lesson_count SMALLINT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    author_name VARCHAR(120) NOT NULL,
    evidence_note VARCHAR(300) NOT NULL,
    reference_list TEXT[] NOT NULL DEFAULT '{}',
    reviewed_at DATE NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_guided_growth_paths_slug CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
    CONSTRAINT chk_guided_growth_paths_lessons CHECK (lesson_count BETWEEN 1 AND 100),
    CONSTRAINT chk_guided_growth_paths_duration CHECK (duration_minutes BETWEEN 1 AND 10000)
);

CREATE TABLE user_growth_path_progress (
    user_id UUID NOT NULL,
    path_id UUID NOT NULL,
    progress SMALLINT NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_user_growth_path_progress PRIMARY KEY (user_id, path_id),
    CONSTRAINT chk_user_growth_path_progress CHECK (progress BETWEEN 0 AND 100),
    CONSTRAINT fk_user_growth_path_progress_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_growth_path_progress_path
        FOREIGN KEY (path_id) REFERENCES guided_growth_paths(id) ON DELETE CASCADE
);

CREATE TRIGGER trg_guided_growth_paths_set_updated_at
    BEFORE UPDATE ON guided_growth_paths
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_user_growth_path_progress_set_updated_at
    BEFORE UPDATE ON user_growth_path_progress
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

INSERT INTO guided_growth_paths
    (id, slug, title, summary, lesson_count, duration_minutes, author_name, evidence_note, reference_list, reviewed_at)
VALUES
('0191e200-0000-7000-8000-000000000001', 'grounding-anxious-moments', 'Grounding through anxious moments',
 'Notice escalation, orient to the present, and choose one manageable action without treating anxiety as a personal failure.',
 5, 28, 'Dr. Maya Bennett', 'Clinician reviewed; educational, not diagnostic',
 ARRAY['NHS: Breathing exercises for stress', 'WHO: Doing What Matters in Times of Stress'], CURRENT_DATE),
('0191e200-0000-7000-8000-000000000002', 'gentler-sleep-rhythm', 'Build a gentler sleep rhythm',
 'Strengthen morning anchors, reduce bedtime struggle, and track consistency without demanding perfect sleep.',
 7, 42, 'Dr. Thomas Reed', 'Evidence informed; persistent insomnia needs individual assessment',
 ARRAY['American Academy of Sleep Medicine clinical guidance', 'NHS insomnia self-help guidance'], CURRENT_DATE),
('0191e200-0000-7000-8000-000000000003', 'boundaries-without-guilt', 'Boundaries without guilt',
 'Name needs, make specific requests, and practice repair while keeping safety and consent central.',
 6, 35, 'Sarah Kim', 'Professional editorial review completed',
 ARRAY['AAMFT consumer guidance', 'Gottman Institute resources on repair attempts'], CURRENT_DATE)
ON CONFLICT (slug) DO NOTHING;
