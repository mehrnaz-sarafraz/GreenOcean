# Reviewed group activation

The legacy POST /communities route rejects immediate activation. Eligible verified members use owner-scoped proposals: DRAFT→SUBMITTED→APPROVED/NEEDS_REVISION/REJECTED; DRAFT/NEEDS_REVISION can be edited with revision checks; pending proposals may be withdrawn to CANCELLED. No proposal appears in discovery.

Only current platform ADMIN/MODERATOR behind existing recent strong MFA can review. Group roles are insufficient. Reviewer must differ from requester. Approval locks the exact submitted revision, rechecks requester eligibility and local safety, then creates one opaque-slug group, its OWNER membership, rules, decision record, audit and a content-free recipient notification in one transaction. Replays/stale/cancelled approvals conflict; failures roll back every activation effect.

Review criteria: non-clinical peer purpose; no impersonation or emergency/treatment claim; suitable name, approved minimal metadata and rules; no global-safety override; duplicate/spam scrutiny; appropriate privacy. Local pattern screening supplements human review and does not certify meaning. Reviewer identity is retained privately and never emitted to ordinary proposal responses.

`greenocean.groups.proposals-enabled=false` and `greenocean.groups.activation-approved=false` by default. Proposal processing/retention and actual independent reviewer operations must be approved before intake; actual safety/legal readiness is required before activation. Synthetic test flags are not production approval. No response time or arbitrary retention period is promised. Existing J/K/L operational dependencies remain.
