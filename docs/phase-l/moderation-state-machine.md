# Current-revision moderation state machine

| Event | Current state | New content state | Evidence |
|---|---|---|---|
| Local ALLOW | New/revised submission | PUBLISHED | Versioned assessment, original identity/audience |
| REQUEST_REVISION | New/revised submission | UNDER_REVIEW | Private editable submission and outcome |
| HOLD_FOR_REVIEW or classifier outage | New/revised submission | UNDER_REVIEW | Private current revision, safe reason code |
| BLOCK_PROHIBITED | New/revised submission | UNDER_REVIEW | Unpublished, appeal available |
| Staff ALLOW | Current nondeleted revision | PUBLISHED | Reviewer, author-visible reason, audit |
| Staff revision/block | Current nondeleted revision | HIDDEN | Reviewer, reason, audit |
| Report REMOVE | Published/restricted nondeleted content | HIDDEN | Existing report resolution, assessment notice, audit |
| Author edit | Owned nondeleted revision | Reclassified state | Revision increments; old approval cannot authorize new body |
| Author delete | Owned content | DELETED | Existing soft delete; cannot restore through review |
| Independent reviewer begins appeal | OPEN appeal, current revision | REVIEWING appeal; content stays restricted | Role/MFA/revision and independence checks, audit |
| Independent appeal ALLOW | Current appealed revision | PUBLISHED, appeal OVERTURNED | Existing appeal row and same assessment |
| Independent appeal reject/revise | Current appealed revision | HIDDEN, appeal UPHELD | Reason and independent reviewer |

Original classifier outcome/reason are retained. Human decisions append immutable decision-history rows under the content lifecycle, including author-visible notes; current assessment fields are a projection. Owner exports include these rows without reviewer identity. Assessment row locking plus content row locking serializes decisions. Requests include the exact displayed revision. Stale version, deleted content, self-review, repeated decision and same-reviewer appeal decision are rejected. One appeal per assessment is enforced by a unique index. Initial appeal state reuses OPEN, not a new parallel state machine. Old generic appeal actions reject linked social appeals so they cannot close a case without applying the revision-aware content decision.

No unreviewed edited body stays visible under an older approval. Identity/audience never change on review. Existing reports can dismiss a complaint without publishing restricted content. A different identity requires a conscious new publication, not changing history.
