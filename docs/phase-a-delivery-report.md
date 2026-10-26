# Phase A delivery report

## A. Current state audit

See [requirements/audit](product/phase-a-requirements.md). Audit and implementation use fetched main `ab44bc1`. The initial local checkout was older and has a different migration history; it was not used as the implementation base. Existing role-review, listener training, policies, session ownership, reporting, post deletion and account erasure were reused.

## B. Changes implemented

Backend auth/identity: mandatory adult self-attestation, pending enrollment, current Terms validation/server acceptance, random profile identity, private defaults, field validation, protected verification/recovery, durable throttling, compatible password hashing and production guards.

Backend access/lifecycle: centralized eligibility/region policy, live role/session validation, refresh replay/family revocation and concurrency locking, separate web HttpOnly-cookie API, location APIs, separate public resources, erasure extensions and retrying credential-file cleanup. No production region is enabled.

Frontend: minimal signup and country search, Terms/Privacy separation, activation and public resources screens, first-contribution identity review, account-dependent app gate, password visibility, network timeout/retry handling, memory-only web access tokens, cookie refresh, secure mobile storage preservation and per-user data-provider isolation.

Tests/operations: four-persona API/security/helper tests, isolated PostgreSQL migration/concurrency tests, reproducible QA script, CI quality gates and documentation. See the Git changed-file list for exact paths; every changed file is part of these domains.

## C. Database migrations

- V31__phase_a_identity_privacy_access.sql: additive eligibility/acceptance/geography/service/deletion/file/rate/refresh schema, safer defaults and outbox expiry.
- V32__reference_country_catalog.sql: 249 reference countries with localized names; zero enabled services.
- V33__phase_a_conversation_boundaries_and_token_rollout.sql: explicit conversation service kind, old-token retirement and plaintext-payload removal.

All V0–V30 sources are unchanged. Fresh migration and upgrade from V30 with a synthetic legacy account passed against PostgreSQL 18.6. Divergent historical deployed databases require a separate migration-reconciliation review.

## D. API contracts

- POST /auth/register: profile fields are optional/ignored for new identity; adultSelfAttested=true, a valid countryCode and current termsVersion required; USER/PENDING only.
- Existing /account-assistance email verification/reset endpoints: protected delivery, expiry/single use, durable rate limits; verification can activate an eligible pending account.
- GET /identity; POST /identity/activate; POST /identity/display-reviewed: current readiness, self-attested eligibility/Terms/geography and display review. No independent trust escalation.
- GET /geography/{countries,subdivisions,cities,services}: bounded public reference/service queries.
- GET /public/resources: curated all-age resources separate from user content.
- POST /auth/web/{login,refresh,logout}: browser-only Origin/header protection and HttpOnly-cookie refresh. Existing mobile bearer endpoints remain.
- DELETE /account: subject-derived ownership/password check retained; extended data cleanup and retryable files.
- Social endpoints reject ineligible/pending/unapproved-region access. Owner activation/settings/export/deletion remain reachable. Default admin routes are disabled pending strong authentication.

## E. Frontend changes

See B and the UI files in the diff. Registration no longer logs in automatically before verification. Pending/region-unavailable users receive an activation/public-resource path. New profiles have no email-derived names, stored birth year, city or signup professional selection. Privacy notice is informational; only Terms acceptance is required and recorded. Sensitive optional preferences remain outside public profile data.

## F. Security review

See [threat model](security/phase-a-threat-model.md). No complete-security or legal-compliance claim is made. Production startup blocks unsafe browser settings, absent protected delivery, an unconfigured breach corpus, an unspecified legacy rollout deadline and administrative access without an MFA integration. Legacy community grace and self-declared age/location remain explicit residual risks.

## G. Persona testing

Backend: 55 tests passed, 0 failed, 0 skipped. Frontend: 17 passed, 0 failed, 0 skipped. The exact per-persona counts, test names and preserved untagged regression tests are in [results JSON](testing/phase-a-results.json). No cosmetic persona-only classes substitute for scenarios.

## H. Test execution evidence

See [results](testing/phase-a-results.md) and [journey matrix](testing/phase-a-test-matrix.md). Backend verify includes real Flyway, repository, MockMvc/API, service, security, negative and independent-transaction concurrency checks. Fake mail transport delivery/purge and failure/retry passed. Frontend native form/network/refresh tests passed. Local full frontend installation suffered network resets; typecheck/lint/export and UI/device E2E are not reported as passed. CI is configured to run a clean installation and checks.

## I. Remaining blockers

- Approve pilot markets/services and professional-care scope; import reviewed local reference data if local features are enabled.
- Configure a real idempotent HTTPS mail gateway, separate encryption key, maintained offline compromised-password corpus, delivery monitoring and a legacy-user notification/grace schedule.
- Integrate privileged MFA before enabling production admin/review workflows.
- Complete browser UI E2E, Android/iOS device smoke and multi-user session-switch verification.
- Finalize/report-retention expiry, backup retention and restore-with-erasure replay operations.
- Independent age assurance is an extensibility boundary; no provider is connected, and self-attestation can be false.

## J. Git deliverables

Branch: feature/phase-a-identity-privacy-access. Base: ab44bc1. A draft pull request is intended for review; no merge or deployment is authorized. Commit/PR and CI links are reported in the chat and final result update.

## K. Final readiness

| Requirement | Status | Evidence / outstanding work |
|---|---|---|
| A-01 | Implemented, Awaiting Runtime Verification | Backend age/access and public separation tested; UI/device paths and legacy rollout approval pending; independent provider intentionally absent |
| A-02 | Partially Complete | Country/service schema/search/default-deny tested; real subdivision/city reference import and market manifest pending |
| A-03 | Partially Complete | Base role, applicant-without-authority and live-role security tested; production MFA remains blocked |
| A-04 | Partially Complete | Verification, recovery, suspension, refresh races and owner erasure tested; actual delivery, backup/report-retention operations pending |
| A-05 | Implemented, Awaiting Runtime Verification | Minimal/private signup, Terms, password migration and cookie API tested; full UI/mobile and maintained deployment breach corpus pending |
| A-06 | Implemented, Awaiting Runtime Verification | Regional/conversation boundaries and resources present; professional scope and real UI/operational assessment pending |

Phase A is **not Complete** under the supplied Definition of Done. This is a tested backend and frontend implementation candidate with explicit remaining release and verification work.
