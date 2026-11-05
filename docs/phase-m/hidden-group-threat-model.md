# Hidden-group threat model

Protect existence, sensitive membership, invitation recipients, attendance and anonymous narratives from nonmembers, removed/banned users, stale clients and ordinary group managers seeking platform attribution.

Controls: server-filtered directory and counts; opaque UUID slugs; same missing-resource404; target-bound expiring inbox invitations; current role/member/status/ban checks; shared post-read predicate; no roster for ordinary members; no group role in platform authorization; no narrative/name/secret in notification previews or routine logs; no sensitive analytics; owner-specific export/erase.

Concurrency must serialize approval, membership/invitation/revocation/role/transfer and event operations with database row locks. Constraint/transaction failures never publish unchecked text or grant partial membership. Revocation affects every new API request. Foreground screens poll and clear protected state on blur/background/account change; network revocation cannot erase previously copied text. Bounded timing differences and small-audience inference cannot be eliminated absolutely and are not described as perfect anonymity.

No live/production adversarial testing. Synthetic-only PostgreSQL and browser fixtures are isolated. Actual staffing, incident response, policy and evidence/backup retention remain external gates, not claims derived from CI.
