# Phase Q execution evidence

Q1 is under final published-source validation. Q2–Q5 and Q0 have not yet been implemented. No claim of whole-Phase-Q completion is made.

Base P5: 1bb797d0edee82baad4da7a41c36284f3034379b, CI40865866689, all four jobs confirmed successful. Those predecessor counts are not Q results.

| Q1 check | Observed result |
|---|---|
| Java compilation | Passed on initial implementation |
| Final focused PostgreSQL scope |29 passed,0 failures/errors/skips:21 current-role/state/receipt/appeal cases,3 independent-connection races,1 populated-P5 migration and4 encrypted source-isolation cases |
| Frontend unit helpers |69 passed,0 failed/skipped |
| Actual rendered Q1 browser cases |7 passed, English/Persian/RTL at360/1280, exact revision and revoked privilege clearing |
| Final TypeScript |Passed |
| Lint |0 errors,11 existing warnings |
| Complete A–P and initial Q1 regression |675 passed,0 failures/errors/skips; later classification/source refinements separately passed all29 targeted cases; published-source full CI remains pending |
| Q1 independent-connection races |3 passed: duplicate import, conflicting revision transitions and one-slot human capacity |
| Real API/browser/PostgreSQL + virtual WebAuthn |1 passed locally: actual report, two independent virtual-key staff, acknowledged shifts, correct source decision, self-review denial and owner receipt isolation |
| Initial published source |22865fbdf5acaead977cc7beaf165ed6c3797f2c, PR28; run41639503760 passed backend/frontend/scans;51 live passed and1 new live fixture failed400 due an unsupported action tag. Correct existing APPROVE tag now passes locally; final-source CI pending |
| Genuine operators/key custody, independent security review, physical Android/iOS, actual coverage drills |NOT_EXECUTED / OPERATIONAL_BLOCKER / DEVICE_TEST_REQUIRED |

Commands: `scripts/run-phase-a-qa.ps1 -Offline -BackendTests 'PhaseQ1*'`; complete `scripts/run-phase-a-qa.ps1 -Offline`; frontend `npm ci --no-audit --no-fund`, `npm run typecheck`, `npm run lint`, `npm test`, `npx playwright test phase-q1.spec.mjs`; established live harness with disposable PostgreSQL and virtual WebAuthn. Java21/Node24/PostgreSQL18/Windows/Chrome locally; standard four-job Linux CI remains authoritative for published-source regression.

Initial test compilation exposed invalid compound `var` fixture declarations; corrected before execution. The first complete local run exhausted the disposable PostgreSQL40-connection limit as historical Spring contexts accumulated. QA now bounds context caching to8; product checks and assertions were not removed. The subsequent complete run passed675 tests. The first live attempt used unsupported DISMISS instead of the existing APPROVE action that dismisses a report; corrected without changing product assertions or the existing API. These failed attempts are not counted as passes. Private QA logs remain in ignored `.tmp`; they are not copied into ordinary telemetry.

No external review, production recovery, clinical or payment approval, real notification acknowledgement, pilot recruitment, merge or deployment has been performed.
