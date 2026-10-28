# Phase N evidence
## N1 local checks (2026-10-28)
- 30 existing Phase I integration and concurrency tests passed before scheduling UI changes.
- Ten new scheduling integration tests and eight genuine PostgreSQL multi-connection concurrency tests passed together, no failures/skips.
- Five new actual-component Chrome tests passed: explicit booking/cancellation, conflict retry, calendar creation/removal, Persian RTL at 360 and 1280 pixels.
- TypeScript passed; ESLint has zero errors and the unchanged 11 baseline warnings.
- Added populated M3-to-N migration test and two real HTTP/PostgreSQL/Chrome journeys. Their final CI results are not yet claimed here.

Synthetic test-only time fixtures are excluded from the production jar. Integration tests replace the N clock inside a rolled-back test transaction; the keyed loopback browser fixture moves only a synthetic future reservation to the present with the immutability trigger restored in the same transaction. This proves boundary behavior and the real scheduled handoff without claiming a real thirty-minute wall-clock staffing exercise.

N2 and N3 checks are pending. Full A–M regression and exact GitHub run evidence must be recorded before final Phase N completion.
