# Retention and erasure matrix

Only existing rules run. Proposed candidate periods below are discussion inputs, NOT APPROVED and NOT IMPLEMENTED. Category-specific owner/counsel decisions and preservation exceptions must precede changes. J runs no production purge and invents no statutory deadline or universal hold.

| ID/category | Current implemented rule | Erasure/access limit and pending owner decision |
|---|---|---|
| R1 PENDING | Approved7days after protected verification activity, D outage/recovery/legacy safeguards; disabled/dry-run until operational enablement | Reuse transactional erasure, never simple creation-date deletion; identity/delivery owner |
| R2 ACTIVE/profile/tombstone/export metadata | Account-controlled erasure, profile/email/hash replaced, eligibility/preferences/session rows removed | Pseudonymous user/profile reference stubs remain; tombstone lifetime unresolved; account/legal owner |
| R3 Sessions/recovery/verification/outbox | Fixed absolute/idle lifetimes, one-use expiry/consumption, encrypted outbox secret purge; reset/erase revokes | Offline client cannot promise instant server revocation; preserve auth/mail policy; security/delivery owner |
| R4 Social/anonymous mapping/community | Existing audiences; owned posts/comments redacted, relations/memberships removed, owned group archived | UUID/stub and received copies can remain; evidence duration unresolved; social/moderation owner |
| R5 Preferences/topics/onboarding/wellbeing | Withdrawal transactionally clears current and legacy topics; account erasure removes own preference/wellbeing rows | Revised/missing grant blocks future topic writes; no unrelated post/message deletion implied; preference owner |
| R6 Listener/professional evidence | Owned qualification records erased; credential binaries handled by retry worker | Reviewer/audit links may remain; category legal evidence schedule/hold needs approval; service/legal owner |
| R7 Presence/live requests/session metadata | Readiness expires; I terminal deadlines enforced; request records erased for either deleted participant | Candidate terminal metadata minimization after30days except approved safety holds; NOT APPROVED; listener/legal owner |
| R8 Message/history | Authorized participant history; authored message bodies redacted on erasure | Recipient copies/backups not recalled; no approved universal history period; messaging/legal owner |
| R9 Reports/appeals/evidence | Existing restricted review records preserved, no new transcript copying | Candidate review/minimization90days after closure except lawful holds; NOT APPROVED; moderation/legal owner |
| R10 Policy/consent/receipt/rights | Published text/review and acceptance/decision/receipt updates immutable; own governance records erased through account deletion | Policy history/staff references persist; evidence/case lifetime needs approval; governance/legal owner |
| R11 Security/staff audit | SECURITY default30days, bounded1000-row purge,200 events/actor/hour; own recent50 | Non-SECURITY audit does not inherit30days; separate staff-evidence decision required; security owner |
| R12 Files/backups/media/caches | Binary erasure20jobs/pass, retry/idempotent delete; completed file-job refs purge after1day; auth epoch isolates private caches | Candidate backup window30days plus tested deletion replay, NOT APPROVED; external copies/physical backup erasure unverified; infrastructure owner |
| R13 Analytics/aggregate metrics | No approved collection/SDK, grants unavailable | Approve purpose/access/aggregation/retention before pipeline; privacy owner |

## Restore and deletion replay

Required operator procedure: isolate restored backup before serving traffic; disable outbound/new processing; reconcile a restricted erasure/tombstone ledger newer than the backup; replay existing account erasure and external-file jobs; verify session revocation, post audiences, regional gates and synthetic export/deletion before reopening. Keep replay evidence separately protected from restored backups under an approved schedule. This is a runbook, NOT a deployed backup integration or successful rehearsal: provider, retention inventory and independent replay ledger were not supplied. If erasure replay cannot be verified, do not expose restored data. Immediate erasure from all backups is not claimed.

Unresolved periods do not authorize permanent storage. Restrict access and secondary use, keep new sensitive/optional processing disabled, appoint a named owner and require a time-bound legal/operational review before launch. Do not run arbitrary historical purges or override a specifically approved preservation obligation.
