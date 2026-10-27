# Integrated verification matrix

Existing tests are retained rather than duplicated. Backend class paths are backend/src/test/java/com/greenocean/backend; fixture suites are frontend/e2e; actual-server suites are frontend/live-e2e. Only the latter use a running Spring/PG/controlled-email environment. All identities, listener approvals and regions there are synthetic, not production approvals.

| K requirement | Exact executed suite or focused evidence | Boundary |
|---|---|---|
| K-01 account journey | registration.spec.mjs plus phase-k-integration.spec.mjs first test: public/signup/pending/verification/fresh login/skip/privacy/support/receipt/decline/archive/change password/erase | Correct same account, current Terms, default private profile; controlled mail only |
| K-02 auth/device | PhaseEFSessionIntegrationTest, PhaseEFConcurrencyTest, PhaseEFMfaBoundaryTest; phase-ef-security.spec.mjs | Password15–128 Unicode, legacy hash, one-use reset, expiry/replay/revoke, proof rotation, cross-tab cache and genuine virtual WebAuthn; physical key/storage not proven |
| K-03 visibility/messaging | PhaseGHPrivacyIntegrationTest/PhaseGHConcurrencyTest/PhaseGHMigrationTest; PhaseI/J tests; phase-gh-journey/phase-i-journey live | Historical audiences stay restricted; block/account/owner gates; LISTENER cannot become ordinary reusable DIRECT |
| K-04 real support | PhaseILiveSupportIntegrationTest/PhaseIConcurrencyTest; phase-i-journey live; K bounded reliability test | Real targeted acceptance and activation; stale/expired/end sends denied; no offline queue/fake readiness |
| K-05 legal/rights | PhaseJGovernanceIntegrationTest/PhaseJDisabledGovernanceTest/PhaseJConcurrencyTest/PhaseJMigrationTest; phase-j-privacy and K same-account live | Missing grant not consent, immutable evidence/versions, exact notice, current purpose, withdraw/erase/appeal ownership; legal sufficiency remains separate |
| K-06 migrations | All PhaseA/D/EF/GH/I/J migration suites;44 source hash comparisons; isolated PG18 dump/restore checksum step | Fresh and populated synthetic upgrades/failed migration recovery; no production-volume DDL or deletion-ledger replay claim |
| K-07 personas | Happy: K same-account + I two-party; Creative: GH/EF multi-tab/FA + I foreground; Confused: C/D recovery + J failed-save/retry; Malicious: all direct DB/API security tests | Four personas cross integrated code; no skipped test counted passed |
| K-08 pipeline | Backend verify, frontend types/lint/helpers/all-platform export/fixture, actual-server live; new release-scans job | Exact tested SHA/run and counts in test evidence |
| K-09 native | native-accessibility-checklist.md | NOT_EXECUTED_DEVICE_REQUIRED, no signed build/device supplied |
| K-10 reliability | K second real HTTP test: four workers, registration/verification/login/refresh/feed/profile/presence/last-slot/acceptance/messages/reports/export/erase; existing resend/cleanup/concurrency tests | Latencies measured on isolated runner, no production SLO or saturation capacity claim |
| K-11 security | Targeted NIST/ASVS/MASVS mapping plus release source/advisory scans, direct API negative tests | No full certification/history/DAST/physical mobile assessment |
| K-12 monitoring | Existing SecurityEvents, D delivery outcomes/queue, privileged moderation/erasure states; controlled fault tests | Actual alert destination/operator/drill unavailable and explicitly blocked |
| K-13 external services | external-dependency-ledger.md | Real delivery/RP/staffing/legal/geography/signing not fabricated |
| K-14 rollout | rollout-and-recovery-plan.md | Plan only; no merge/deploy/flag activation |
| K-15 recovery | D gateway/key/lease failure tests, I transaction rollback/restart service tests, EF refresh/reset races; isolated backup restore | Reconstructed service objects are not physical multi-host failover; RTO/RPO not promised |
| K-16 accessibility/locale | B/C/EF/GH/I/J component suites: keyboard/focus/contrast/text/width/FA RTL/errors/status; K journey | Manual assistive/native checks remain open |
| K-17 triage | release-blockers.md | P0 cannot be accepted merely to make report green; disabled service is unavailable service |
| K-18 closure | phase-1-closure-report.md | Technical/integration/staging/pilot/production decisions kept separate |

Adversarial coverage includes pending/ineligible/unsupported/suspended actors, forged roles/session versions/owners/IDs, reset/refresh/proof replay, private posts and report/appeal/rights isolation, stale lease/acceptance/message, forbidden publication/consent, unsafe policy script text, oversized auth requests and document file signatures. Existing I race mapping covers17 actual DB failure/concurrency cases. Missing physical device evidence and external operations are never replaced with mocked success.
