# Phase J executed evidence

Recorded 2026-10-27. Final executable db2bfb1abf5f296a2cbac48827b7afb0471eec92 includes I beb5b78. [CI40537181467](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40537181467) completed successfully in backend, frontend and registration-live. Job IDs:122306364666,122306365191,122306364580 respectively. This is actual remote execution; it is not represented as a local Windows backend/browser run.

| Command / CI conditions | Actual result |
|---|---|
| backend: chmod +x mvnw && ./mvnw -B clean verify; Java21, disposable PostgreSQL18 | 259 tests,0 failures/errors/skips; BUILD SUCCESS |
| J backend classes included above | PhaseJGovernanceIntegrationTest20, PhaseJConcurrencyTest1, PhaseJDisabledGovernanceTest3, PhaseJMigrationTest1:25 passes |
| frontend: npm ci --no-audit --no-fund; npm run typecheck | Passed |
| npm run lint | 0 errors,11 existing warnings |
| npm test | 68 passed,0 failures/skips |
| npx expo export --platform all | Web/Android/iOS JS/assets exported successfully; not native runtime evidence |
| npm run test:e2e | 92 passed,0 failures/skips;8 J fixture browser cases |
| npx playwright test --config playwright.live.config.mjs | 22 passed,0 failures/skips;2 J true fullstack cases |

Total441 passes. Against final I's406, J adds25 backend+8 fixture+2 fullstack cases. All earlier tests, including the final I delayed foreground response case, are retained. Local Windows command node --experimental-strip-types --test frontend/tests/*.test.mjs separately passed68 helpers,0 failures/skips. Local full dependency installation/backend/native tests were not claimed.

## Test boundaries and scenarios

Backend uses genuine PG locking/constraints/transactions. Migration exercises populated V41 → latest V43 and existing fresh-schema suite; retains hash/profile/acceptance history and fabricates no grants/receipts. Concurrent withdrawal/save uses independent transactions and a synchronized start; either initial write outcome must end with empty current/legacy topics and disabled processing. Governance tests cover exact notice, missing/legacy grants, receipt/Terms separation, decline idempotency/core access, unavailable analytics/communications, self-review, public version/locale/history, older-version rollback, revoked reviewer, edited-draft reapproval, immutable publication/review/acceptance records, owner-only rights retries/status and complete archive vs UI100 cap/erasure. Report tests prove private post404, readable post201, duplicate409 and cross-owner appeal404.

Fixture tests mount the actual Expo components with controlled transport: saved/failed withdrawal, no preselected grants, unavailable manual intake, FA/RTL360px, script rendered as literal text, lost rights response preserving retry key, exact notice-before-grant and stale historical grant disclosure. They supplement rather than replace database and server checks. True fullstack tests run actual frontend/Spring/PG and controlled mail: separate receipt/Terms/purpose history and archive; forged grants/topics/owner IDs/admin publication fail or remain authenticated-owner-bound. Prior I two-party support and E/F genuine virtual WebAuthn tests remain in the full run.

Happy/Creative/Confused/Malicious evidence is mapped in delivery-report.md and exact test names/source hashes in test-evidence.json. Every actor/address/policy/review in tests is synthetic. Synthetic approval is not permission to publish legal documents or authorize production listeners. QA fixtures remain test-classpath-only, isolated flags/loopback/key protected and absent from production jar.

## Continuation results and limits

No failing test in the final source run; earlier408eff5 checks were superseded by the final report-target correction. No passing assertion was removed or relaxed during this continuation. Original J809258e CI success is historical, not substituted for final db2bfb1 evidence. Subsequent report commit changes docs only; it does not change tested executable source.

Physical Android/iOS, TalkBack/VoiceOver, actual legal review/staff ceremony, real provider delivery/contracts, alerts, load/DDL/backup restore/replay remain NOT_EXECUTED or external. Native manual cases are in phase-k-handoff.md; JS export is not proof of operating-system behavior. No adb/emulator/xcrun available in this Windows workspace. No production data/migration/deployment or legal-compliance certification.
