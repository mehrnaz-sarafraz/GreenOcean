# Phase P validation evidence

This replaces intermediate in-progress notes. All accounts, documents, provider receipts and clinical narratives used in QA are synthetic.

| Snapshot / scope | Observed result |
|---|---|
| P1 exact-head CI 40768695429 | All four jobs succeeded |
| P2 exact-head CI 40828662757 | All four succeeded; local complete backend 577 passed; four component cases and one real journey passed |
| P3 exact-head CI 40836174883 | All four succeeded; 30 P3 targeted cases, four component cases and real P1–P3 journeys passed |
| P4 exact-head CI 40846917934 | All four succeeded; complete backend 628 passed; 21 P4 migration/concurrency cases; five component cases and one real journey passed |
| Initial P5 complete backend | 650 passed, zero failures/errors/skips; later policy-binding/deduplication refinements are verified separately and by final CI |
| Final P5 targeted | 26 passed, including independent-connection races, forward migration/rollback, no-show evidence, quiet-hour deduplication and subject rights |
| P5 interface | Four component browser cases passed, including Persian RTL at 360/1280 widths |
| Frontend shared logic / lint | 69 unit cases passed; zero lint errors and eleven existing warnings |
| Final shared P2/P3/P5 source regression | 80 passed, zero failures/errors/skips; final P5 presence refinement separately passed all 26 P5 cases |
| Final P5 real API/browser journey | Passed (38.4s test; 1.4m orchestration): optional follow-up, explicit acceptance/withdrawal, no ongoing messaging, protected rights review and independent incident authority |
| Final P5 CI | Exact published head and all job results maintained in the P5 PR |
| Physical Android/iOS, native push/call permission/disconnect behavior | NOT_EXECUTED; no attached adb/Xcode/device runtime |
| Real bank/media/clinical approval and production capacity | NOT_EXECUTED / APPROVAL_REQUIRED |

Reproducible commands (repository root unless stated):

- PostgreSQL18 disposable database, Java21: `cd backend; ./mvnw -B clean verify` with GREENOCEAN_TEST_DB_URL pointing to a disposable `_qa` database and its independent test credentials.
- Focused final domain run: `./mvnw -B -Dtest=PhaseP2*,PhaseP3*,PhaseP5* clean test` (quote the test argument in PowerShell). Local orchestration logs are `.tmp/phase-p5-final-regression.log`; full earlier run `.tmp/phase-p5-full-backend.log`.
- Frontend: `npm run typecheck`, `npm run lint`, `npm test`, `npx expo export --platform all`, `npm run test:e2e`.
- Component scope: `npx playwright test phase-p5.spec.mjs`; local `.tmp/phase-p5-browser.log`.
- Isolated real journeys: compile `PhaseCLiveServer` test sources/classpath and run the established CI registration-live setup with GREENOCEAN_LIVE_BROWSER_QA, GREENOCEAN_LIVE_SUPPORT_QA and GREENOCEAN_LIVE_CLINICAL_QA set to `isolated-only`; `npm run test:live` executes all real-stack cases. Test-only loopback fixtures require their generated QA secret and synthetic account/database restrictions. No production bean fabricates provider success.

Four personas: Happy User covers verified scheduling, free real-stack delivery, explicit follow-up and synthetic-provider paid accounting. Confused User covers exact IRR/IRT display, refund obligation versus receipt, separate consent, optional reminders and no invented small-sample score. Malicious User covers role/IDOR failures, callback replay/mismatch, clinical/social separation, independent moderation/incident/hold review and concurrency. Creative User covers DST/locale/RTL, cohort changes, repeated feedback, changed methodology inputs and recovery retries. Existing A–O suites run in the full backend and CI frontend/live gates.

Exact implementation SHAs and CI run IDs are in delivery-report.md and each PR body. Final PR metadata records the exact final tested head without embedding a self-referential commit SHA into its own tree. No synthetic success is an operational approval.
