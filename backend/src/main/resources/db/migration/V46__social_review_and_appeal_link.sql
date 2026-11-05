-- Additive links reuse existing appeal lifecycle; historical content remains unchanged.
ALTER TABLE moderation_appeals ADD COLUMN assessment_id UUID REFERENCES social_assessments(id) ON DELETE SET NULL;
CREATE UNIQUE INDEX social_one_appeal ON moderation_appeals(assessment_id) WHERE assessment_id IS NOT NULL;
ALTER TABLE social_assessments ADD COLUMN decision_note VARCHAR(1000);
ALTER TABLE social_assessments ADD COLUMN reviewed_at TIMESTAMPTZ;
CREATE INDEX social_pending_review ON social_assessments(assessed_at,id) WHERE reviewed_at IS NULL AND outcome <> 'ALLOW';

ALTER TABLE social_assessments ADD COLUMN initial_outcome VARCHAR(30);
ALTER TABLE social_assessments ADD COLUMN initial_reason_code VARCHAR(60);
UPDATE social_assessments SET initial_outcome=outcome,initial_reason_code=reason_code;
CREATE TABLE social_review_decisions (
 id UUID PRIMARY KEY DEFAULT uuidv7(),
 assessment_id UUID NOT NULL REFERENCES social_assessments(id) ON DELETE CASCADE,
 revision BIGINT NOT NULL,
 outcome VARCHAR(30) NOT NULL CHECK(outcome IN ('ALLOW','REQUEST_REVISION','BLOCK_PROHIBITED')),
 reviewer_id UUID REFERENCES users(id) ON DELETE SET NULL,
 note VARCHAR(1000) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX social_review_decision_history ON social_review_decisions(assessment_id,created_at,id);
