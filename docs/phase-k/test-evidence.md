# Final Phase K executed evidence

Date2026-10-27. Final executable **4075561ccfd6585b00f7dd9b8b95b96e5000a2c4**, based on J ab16a953511dcc075cb0bd4c12c6380893561d7d including final I. [CI40541904020](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40541904020) completed successfully in all four jobs. The following final delivery commit changes documentation only and preserves tested executable source. These backend/browser/build/scan results were actually run in GitHub QA, not claimed as local Windows execution.

| Category / actual command | Passed | Failed | Skipped / not executed |
|---|---:|---:|---|
| Java21/PG18: backend ./mvnw -B clean verify | 260 | 0 | 0; includes unit/integration/migration/concurrency/security/cleanup/rights |
| Frontend npm test | 69 | 0 | 0; includes shell-quote rejection regression |
| Actual-component browser npm run test:e2e | 92 | 0 | 0; synthetic transport, not fullstack |
| Browser→Spring→PG/controlled mail: npx playwright test --config playwright.live.config.mjs | 24 | 0 | 0; includes both new K journeys |
| Total automated test cases | **445** | **0** | **0** |
| TypeScript / npm run typecheck | Passed | 0 | Not counted as test cases |
| ESLint / npm run lint | Passed | 0 errors | 11 existing warnings; not silently discarded |
| npx expo export --platform all | Web/Android/iOS JS/assets succeeded | 0 | Physical OS/device execution NOT_EXECUTED_DEVICE_REQUIRED |
| Synthetic PG18 pg_dump/pg_restore with selected-table/Flyway checksum comparison | Passed | 0 | Production volume/physical backup/deletion replay NOT_VERIFIED |
| Current tracked source four-pattern credential scan | 554 files; no matching candidate locations | 0 candidates | No full-history/entropy claim |
| Original migration source checks | Main31 unchanged; integrated J44 unchanged | 0 changed | Full44 hashes/ancestry in JSON |
| Public dependency scans | 113 Maven runtime coordinates and complete npm audit processed | No scan failure | **31 Maven advisory pairs /12 npm package records remain**; npm critical0 after shell-quote fix; not dependency clearance |
| Native/assistive/staff/provider/legal/actual staging | — | — | Explicitly unexecuted/external; not counted as passed or skipped automated tests |

Jobs: backend122323079637; frontend122323079656; registration-live122323079005; release-scans122323079568. Final scan artifact12751097967; actual-server artifact12752350087 contains performance-results JSON; original initial scan artifact12750512227 preserves pre-fix evidence. Full safe structured source/test/advisory/performance references are committed alongside this report. Database dumps and secret values are not included in the delivery evidence.

## Failures reproduced and fixed

Initial executable bb6d3bf / run40540741385 passed the new full account journey but failed the unchanged four-way report contention assertion: actual201,201,201,409 vs expected201,409,409,409. Report check/insert was not serialized. Fix4075561 takes a DB advisory transaction lock per authenticated reporter/type/target before the duplicate check. The new PhaseKReportConcurrencyTest uses four real independent transaction threads/pool3 and verifies exactly one stored open record. The original real HTTP assertion now passes. No test was disabled or expectation weakened; existing history was not deduplicated destructively.

Initial advisory scan identified shell-quote1.10.0 as affected by GHSA-pqg4-j6r4-53mv. Official registry metadata/integrity resolved compatible1.11.0 in package-lock only. A real dependency regression verifies quote rejects LF/CR/U+2028/U+2029 following a comment token without invoking any shell. Final clean npm installation, helper suite and all exports/browser tests prove compatibility for this source. Initial npm critical1 becomes0; the other advisory records remain visible and block pilot security clearance.

## Scope and local checks

All A–J suites remain included, including I real acceptance/terminal/race checks and J policy/consent/owner controls. K adds one backend regression, one dependency helper and two fullstack cases; it does not fabricate previous implementations. Its72 bounded HTTP measurements across17 scenarios exercise real database/service behavior and exposed the actual report race. See end-to-end-test-matrix.md for all18 K requirements and four-persona mapping.

Local Windows validation executed the existing68 helper tests before adding the dependency regression, original44 migration/ancestry comparisons and the current-source credential patterns. Local full dependencies/backend/native were unavailable, so final69-helper/backend/browser/build counts above are CI evidence. Real mail/provider/hardware, full-history secrets/independent pentest, production load/timeout/HA, actual backup deletion-ledger replay, staffing and legal approvals remain open. No production endpoint stress test, merge or deployment occurred.
