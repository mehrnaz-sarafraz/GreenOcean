# Phase N evidence
## N1 local checks (2026-10-28)
- 30 existing Phase I integration and concurrency tests passed before scheduling UI changes.
- Ten new scheduling integration tests and eight genuine PostgreSQL multi-connection concurrency tests passed together, no failures/skips.
- Five new actual-component Chrome tests passed: explicit booking/cancellation, conflict retry, calendar creation/removal, Persian RTL at 360 and 1280 pixels.
- TypeScript passed; ESLint has zero errors and the unchanged 11 baseline warnings.
- Added populated M3-to-N migration test and two real HTTP/PostgreSQL/Chrome journeys. Their final CI results are not yet claimed here.

Synthetic test-only time fixtures are excluded from the production jar. Integration tests replace the N clock inside a rolled-back test transaction; the keyed loopback browser fixture moves only a synthetic future reservation to the present with the immutability trigger restored in the same transaction. This proves boundary behavior and the real scheduled handoff without claiming a real thirty-minute wall-clock staffing exercise.

N2 and N3 checks are pending. Full A–M regression and exact GitHub run evidence must be recorded before final Phase N completion.

## Initial exact-head CI adf005f
GitHub run [40692653809](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40692653809): backend 391/391, synthetic backup restore, frontend and release scans passed. Real-stack browser tests: 33 passed, including the full new scheduled conversation; the new cancellation UI assertion failed because the rendered status also includes its reason code. The assertion now accepts that exact documented reason suffix while retaining the independent API status and zero-live-request checks. A new complete run is required before accepting N1.

## Validated N1 baseline 8de284f
GitHub run [40694008890](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40694008890) passed all four jobs for executable head 8de284f0546fedc0279c5003885971d30dbec084: 391 backend tests (zero failures/errors/skips), 34 real-stack journeys, full frontend checks, synthetic backup restore and release scans. Both N1 real-stack journeys and the five updated N1 component tests also passed locally against this head. N2 may now build on this validated baseline.

## N2 focused implementation checks
27 PostgreSQL queue integration/concurrency tests passed alongside the ten scheduling and eighteen I integration tests (55 total, zero failures/errors/skips). The 15 N2 race methods include direct-I/queue, booked check-in/queue, double workers, cancellation/transfer, expiry/readiness, two devices, restart recovery, qualification revocation and no-show/late check-in. Seven new actual-component Chrome tests passed, including stale revision rejection, late mutation after leaving, explicit readiness, no coverage and Persian RTL at 360/1280. TypeScript passed. Added staffing MFA/current-role tests, a populated N1 upgrade test and two true-stack queue journeys; final complete evidence is pending.

The synthetic busy-session-tail fixture moves only an owned synthetic bounded session to its last ten minutes; it does not claim a real twenty-minute wall-clock wait or genuine staffed production coverage.

N2 complete focused set: 33 tests passed including populated upgrade, four current-role/recent-MFA staffing checks and generic notification deduplication. Initial local real-stack queue revocation passed; the busy-to-ready journey exposed premature WAITING cancellation during Phase I's mandatory between-session readiness reset. The worker now preserves only the original bounded wait during that explicitly unavailable readiness gap, while refusing new admissions without actual coverage; a regression test was added. Revalidation is required.

## Validated N2 local milestone
Final focused N2 run: 35 tests passed, zero failures/errors/skips, including the readiness-gap regression and direct database rejection of renewed deadlines or an unbound transfer. Both real-stack queue journeys passed after the fix (48.2 seconds): busy coverage → explicit readiness → I acceptance/activation/message/end, and qualification revocation before live request creation. The seven component/browser tests passed; TypeScript and changed-file ESLint passed. Full exact-head CI remains required and will be recorded below.
