# Phase Q execution evidence

Q1 is under validation. Q2–Q5 and Q0 have not yet been implemented. No claim of whole-Phase-Q completion is made.

Base P5: 1bb797d0edee82baad4da7a41c36284f3034379b, CI40865866689, all four jobs confirmed successful. Those predecessor counts are not Q results.

| Q1 check | Observed result |
|---|---|
| Java compilation | Passed on initial implementation |
| Focused PostgreSQL integration/migration |18 passed,0 failures/errors/skips (`PhaseQ1Test`17 and `PhaseQ1MigrationTest`1) |
| Frontend unit helpers |69 passed,0 failed/skipped |
| Actual rendered Q1 browser cases |6 passed, English/Persian/RTL at360/1280, exact revision and revoked privilege clearing |
| Final TypeScript |Passed |
| Lint |0 errors,11 existing warnings |
| Complete A–P and Q1 regression |In progress; final counts pending |
| Q1 independent-connection races |Added; execution pending |
| Real API/browser/PostgreSQL + virtual WebAuthn |Added; execution pending |
| Exact published SHA and GitHub CI |Pending publication/verification |
| Genuine operators/key custody, independent security review, physical Android/iOS, actual coverage drills |NOT_EXECUTED / OPERATIONAL_BLOCKER / DEVICE_TEST_REQUIRED |

Commands: `scripts/run-phase-a-qa.ps1 -Offline -BackendTests 'PhaseQ1*'`; complete `scripts/run-phase-a-qa.ps1 -Offline`; frontend `npm ci --no-audit --no-fund`, `npm run typecheck`, `npm run lint`, `npm test`, `npx playwright test phase-q1.spec.mjs`; established live harness with disposable PostgreSQL and virtual WebAuthn. Java21/Node24/PostgreSQL18/Windows/Chrome locally; standard four-job Linux CI remains authoritative for published-source regression.

Initial test compilation exposed invalid compound `var` fixture declarations; corrected before execution. The first complete local run exhausted the disposable PostgreSQL40-connection limit as historical Spring contexts accumulated. QA now bounds context caching to8; product checks and assertions were not removed. A subsequent complete run is executing. These failed attempts are not counted as passes. Private QA logs remain in ignored `.tmp`; they are not copied into ordinary telemetry.

No external review, production recovery, clinical or payment approval, real notification acknowledgement, pilot recruitment, merge or deployment has been performed.
