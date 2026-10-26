# Phase A test execution results

Execution date: 2026-10-26 (Asia/Tehran). [Machine-readable totals, persona groups and every test name](phase-a-results.json) are retained with this report.

- Backend real-PostgreSQL run: 56 passed, 0 failed, 0 skipped. `backend/mvnw.cmd -o -B verify` completed successfully, including executable JAR packaging. The test database URL was explicitly set to the disposable cluster. Fresh/V30 upgrade now validates V31–V35, including localized JSON/Persian search and reviewed care scopes.
- Frontend native tests: 17 passed, 0 failed, 0 skipped. Command: `node --experimental-strip-types --test frontend/tests/*.test.mjs`.
- Backend compilation: passed, Java 21, all 194 production source files.
- Local frontend typecheck initially exposed stale generated route types and missing upstream dependencies. Dependency installation hit repeated connection resets for official npm archives; it has not passed and is not counted as success.
- Clean frontend install, route-type generation, TypeScript checking, lint and static web export: passed in GitHub Actions.
- Chromium UI: 12 passed, 0 failed, 0 skipped (three per persona), running the actual Expo app against controlled API responses. Full-stack browser journeys and Android/iOS smoke/E2E remain unrun.
- Fake mail transport integration: passed, including payload purge and failure retry. Real production mail deliverability: pending provider configuration.

Earlier failing runs found stale target migrations from the previous local checkout, an ambiguous CORS bean injection, transaction-fixture state and connection-pool limits. Those issues were fixed and the complete backend suite rerun. Failed/blocked executions were never counted as passed. Detailed temporary logs live under the ignored .tmp directory; JUnit XML lives in backend/target/surefire-reports.

## CI evidence

[Successful runtime-source run](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40311371746), source commit `21ce213eb87c3b088743cbfa67e2bc4f8b6cef1b`. Backend job 121533099752: 56 passed, verify/build succeeded. Frontend job 121533099987: npm ci, route generation, typecheck, lint, web export, 17 helper tests and 12 Chromium UI tests passed. HTML browser reports/traces and JUnit XML are attached as workflow artifacts. Earlier failing browser run correctly exposed a test expecting a literal type attribute instead of the DOM default text property; the assertion was corrected and all 12 rerun successfully.

| Persona | Backend | Frontend helpers | Chromium UI |
|---|---:|---:|---:|
| Happy | 11 | 2 | 3 |
| Creative | 9 | 4 | 3 |
| Confused | 7 | 7 | 3 |
| Malicious | 15 | 4 | 3 |
| Preserved regression | 14 | — | — |
