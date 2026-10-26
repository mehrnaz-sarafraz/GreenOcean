# Phase A test execution results

Execution date: 2026-10-26 (Asia/Tehran). [Machine-readable totals, persona groups and every test name](phase-a-results.json) are retained with this report.

- Backend real-PostgreSQL run: 55 passed, 0 failed, 0 skipped. `backend/mvnw.cmd -o -B verify` completed successfully, including executable JAR packaging. The test database URL was explicitly set to the disposable cluster.
- Frontend native tests: 17 passed, 0 failed, 0 skipped. Command: `node --experimental-strip-types --test frontend/tests/*.test.mjs`.
- Backend compilation: passed, Java 21, all 194 production source files.
- Local frontend typecheck initially exposed stale generated route types and missing upstream dependencies. Dependency installation hit repeated connection resets for official npm archives; it has not passed and is not counted as success.
- Local lint/web export: not completed while dependencies remain unavailable. GitHub Actions is configured to install cleanly, regenerate route types and run these checks.
- Browser E2E and Android/iOS smoke/E2E: not executed. These remain release blockers.
- Fake mail transport integration: passed, including payload purge and failure retry. Real production mail deliverability: pending provider configuration.

Earlier failing runs found stale target migrations from the previous local checkout, an ambiguous CORS bean injection, transaction-fixture state and connection-pool limits. Those issues were fixed and the complete backend suite rerun. Failed/blocked executions were never counted as passed. Detailed temporary logs live under the ignored .tmp directory; JUnit XML lives in backend/target/surefire-reports.
