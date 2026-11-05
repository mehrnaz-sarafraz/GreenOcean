# Phase L operations and deployment controls

Default-off flags: GREENOCEAN_SOCIAL_PUBLISHING_ENABLED and GREENOCEAN_SOCIAL_POLICY_APPROVED. Existing admin-enabled/recent WebAuthn and Phase A regional/account gates remain required. Tests explicitly opt in using isolated synthetic users. No production deployment or activation performed.

Before enabling: approve community policy wording and scope; appoint review/appeal staff and an independent escalation owner; provision actual security keys, recovery and access audit review; approve evidence/notes/access-log retention and sensitive-data processing; verify notification/privacy notices, deletion and backup handling; rehearse pending queue, takedown, appeal and outage recovery. Carry forward every unresolved docs/phase-k/release-blockers.md item.

Review path: authorized staff open /community-review, inspect minimized current-revision text, enter a respectful author-visible reason and decide. Role/MFA denial is an error, not an empty queue. ADMIN may separately record a necessary investigation purpose and inspect one attribution. Do not put story text, identifiers or detection secrets in free-text audit purposes. No bulk identity lookup. Queue pages are bounded to20. Assessments preserve original machine outcomes; append-only human decision rows retain each explanation without duplicating narrative bodies. Decisions cascade with assessment erasure and appear in the owner archive without reviewer identity.

Author path: /my-posts → /content-decisions; see current/historical outcomes and appeal status, supply a reason once, or revise owned text. Decision notices and appeals are paginated. No human response time is promised. Staff appeal resolution requires a different reviewer. Generic appeals cannot bypass this path.

Report takedown immediately hides content from new direct/feed/search/saved reads. Cached external copies cannot be recalled. Client foreground/navigation revalidation is eventual; it is not a server push revocation guarantee. New writes and edits are rate bounded; denied/rolled-back attempts do not persist private text in limiter records. Existing security retention is authoritative; unanswered retention/hold decisions block rollout.

V46 is additive. Apply through Flyway after the existing V44/V45. Do not modify applied migrations. Rollback is feature disable + forward repair, not destructive schema downgrade. Rehearse migrations and restore with approved infrastructure; local PostgreSQL16 uses V0 UUID compatibility, CI uses PostgreSQL18. Neither is production performance evidence.

Native binaries/devices, TalkBack/VoiceOver, actual key custody, staffing, legal approval, production provider delivery and external processing arrangements remain NOT EXECUTED / EXTERNAL BLOCKER as appropriate. JS exports do not substitute for them.
