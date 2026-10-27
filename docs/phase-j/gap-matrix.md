# Phase J targeted gap matrix

Baseline: `1b267c8` on `feature/phase-i-first-real-support`, PR #7. J remote initially pointed to the same commit; no J implementation was published. Existing A–I is reused. Initial market: Iran per owner-provided continuation; this is not legal/service approval.

| ID | Requirement | Initial status | Existing evidence/location | Work |
|---|---|---|---|---|
| J-01 | Data inventory | MISSING | AccountService, AccountRepository, live support, preferences, moderation and security services | Document actual categories, owner access, recipients, erasure and unresolved legal basis |
| J-02 | Sensitive processing authorization | PARTIALLY_IMPLEMENTED | UserPreferencesRepository default-off topic gate; Phase I default-off live support | Add purpose-specific current authorization; preserve all existing disabled gates |
| J-03 | Policy publication/versioning | DEFECTIVE | PolicyRepository, AdminRepository.createPolicy, admin policy UI | Reject publish-on-create, independent review and explicit publication gate; locale/effective/review metadata and immutable evidence |
| J-04 | Optional authorization lifecycle | MISSING | Terms policy_acceptances exist; no optional authorization history | Separate receipts, purpose grants/denials/withdrawal, unknown legacy state; withdrawal atomically clears topics |
| J-05 | Support boundaries | IMPLEMENTED_NEEDS_TESTS | Phase I live UI, public pages, educational/care access policy | Targeted truthful-copy inspection; no emergency/staffing/privacy promises |
| J-06 | User rights | PARTIALLY_IMPLEMENTED | Owner archive, profile correction, password-confirmed erasure, Phase I own metadata/messages | Extend archive/erase with governance; authenticated manual intake/status gated by actual owner/SLA readiness |
| J-07 | Retention/backups | PARTIALLY_IMPLEMENTED | Seven-day pending cleanup,30-day security events, retryable file erasure | Preserve approved workers; map unresolved content/support/evidence/backup retention and restore replay |
| J-08 | Reports/appeals | IMPLEMENTED_NEEDS_TESTS | ReportService ownership, duplicate rejection, privileged admin review | Targeted ownership/MFA and confidentiality regression; staffing/DSA applicability unresolved |
| J-09 | Support privacy | IMPLEMENTED_NEEDS_TESTS | Phase I explicit request/accept/activate; owner-only export and lifecycle | Document actual listener disclosure/evidence; no hidden topics/transcript analytics; no new session subsystem |
| J-10 | Communications | IMPLEMENTED_NEEDS_TESTS | SecurityEvents, email outbox and notification preferences | Separate essential operations from optional communications authorization; no marketing activation |
| J-11 | Storage/analytics | PARTIALLY_IMPLEMENTED | E/F HttpOnly refresh cookie, secure native storage, cache epochs; no analytics pipeline | Keep optional analytics disabled and server-denied without approved purpose; accessible controls with real saved state |
| J-12 | Processors/transfers | MISSING | Configured webhook email, professional document storage, external avatar URLs | Inventory real integration categories, unknown hosting/contracts/transfers; no fabricated providers |
| J-13 | DPIA screening | LEGAL_REVIEW_REQUIRED | A–I threat models and operational gates | Technical screening/evidence and residual-risk register; no approved DPIA assertion |
| J-14 | Incident governance | PARTIALLY_IMPLEMENTED | Bounded security events, staff MFA, audit and session revocation | Incident/access/notification runbook and evidence references; actual staffing/alert delivery external |
| J-15 | Transparency/localization | PARTIALLY_IMPLEMENTED | Public EN/FA/RTL, policy viewer, account settings and Phase I UI | Accessible EN/FA governance panel, explicit unreviewed policy/translation status; no decorated consent banner |
| J-16 | Tests | MISSING | I baseline234 backend+64 helpers+83 component+20 fullstack, locally recorded | Add real database policy/consent/rights/migration/ownership tests, browser/component/fullstack and honest native gaps |

Database plan: additive V42 only, no applied migration edits; preserve old policy acceptance/version/history and do not fabricate optional consent or notice receipt. New purposes, legal publication and manual intake remain off unless their specific prerequisites are configured.

## Continuation and final evidence

This table preserves the initial audit at1b267c8. Final J includes I beb5b78 (including foreground-response invalidation) through a normal merge. No A–I audit is repeated. The final16-row matrix in [delivery-report.md](delivery-report.md) supplies implementation, principal files, database/security impact, actual tests, decisions and allowed final statuses. Tested executable db2bfb1 passes441 tests in CI40537181467. Continuation fixes older-version publication, revoked-reviewer use, edited-draft approval, immutable review evidence, stale grant transparency and report audience checks. Database scope is additive V42 plus corrective V43; no earlier migration is edited. Legal/operational/device status is separate from automated code results.
