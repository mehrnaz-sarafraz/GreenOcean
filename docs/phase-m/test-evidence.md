# Phase M evidence ledger

Baseline: L3 `d09fbd4`, executable `8843a61`, all four jobs in run40640710127 passed;524 tests. M1 adds V47 and reuses existing group/post/privacy/MFA paths.

M1 local attempts: the first complete332-test run passed the five new M1 privacy tests but failed an inherited assertion expecting an ordinary member's roster count. The new contract redacts counts as null. The next run exposed the inherited403 expectation; unauthorized group feeds intentionally use nondisclosing404. Both assertions were updated to the explicit stricter contract, retaining the underlying access-denial checks. Neither failed attempt is counted as a successful milestone.

M1 TypeScript passed; lint zero errors and11 inherited warnings. Both M1 browser tests passed (Persian/RTL private draft and failed-save text preservation). Additional default-off, populated-L migration and concurrent activation tests are included. Clean CI and real-backend MFA journey results will be recorded against their actual commit after execution. No native-device, actual staffing or production migration execution is claimed.

## M1 validated checkpoint

Head `77c338cdd78dfa9d8abff5ebcb0f4d3bb60da9d0`, [run40644524881](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40644524881):335 backend +69 logic +104 browser +28 real-stack =536 passes; all four jobs successful. Includes exact-revision independent MFA activation and hidden-directory/slug checks. A committed concurrency fixture initially mutated the common IR catalog and left registration mail; replaced with isolated legacy-eligible raw synthetic records and explicit cleanup. A pre-existing profile initialization race was reproduced by the GH live journey and fixed by withholding editable fields until source initialization and protecting unsaved edits. The final run includes the new delayed-profile-source browser regression. Failed attempts are not included in536.

## M2 local checkpoint

347 backend tests passed with zero failures/errors/skips on disposable PostgreSQL18;69 logic tests passed. New service/MVC cases cover target binding, expiry/revocation/replay, current private-request revisions, rules acknowledgment, roster/anonymous isolation, bans, ownership acceptance and role escalation. Real PostgreSQL races cover simultaneous acceptance, revoke-versus-accept and cancel-versus-approve. Initial JDBC expiry casts failed because queryForMap returns Timestamp; conversion now handles the actual driver types and the repeat passed. Eight focused browser regressions passed, including M1/L1 flows, explicit invitation acknowledgment and removal of stale discussion after revoked access. Language controls are disabled until hydration/storage initialization, preserving the RTL assertion rather than accepting an English fallback. M2 clean CI and real-backend journeys are recorded after execution.

## Verified source refresh for M3 (2026-10-28, America/Los_Angeles)

Git fetch revealed the local checkout was stale. Actual remote L3 d09fbd4 is successful in run40641439385; its executable8843a61 has524 passes in run40640710127. M1 77c338c has536 passes (run40644524881). M2 e4a401a6b120d2f0f64649e632a9dc7c979d9076 has347 backend +69 logic +106 browser +30 true-stack =552 passes,0 final failures/skips; all four jobs success in run40648265433. M3 continues exactly from this M2 head, not stale main. PR13/14 remain unmerged stacked drafts.

M3 initial focused execution passed11 actual PostgreSQL tests (0 failures/errors/skips), including current activity revisions, no unsupported transport, hidden event/report denial, scoped hide/appeal, global-block preservation and closure. Existing M1/M2 component browser regressions passed5 tests on actual Expo/Chrome.

Failed local environment attempts are not successful validation: stale target/classes from an older local branch duplicated V46 after switching; clean compilation removed generated files. Offline clean plugin was not cached; normal Maven download resolved it. The initial full local run failed32 cases after exhausting the local cluster's20 connection limit, and one inherited EF expiry test used CURRENT_TIMESTAMP at an ambiguous cross-clock boundary. The repeat uses a new disposable synthetic database with100 server connections and an explicitly past expiry timestamp (one second); no security assertion was removed or skipped. Generated Expo route types were stale before browser startup, then regenerated from current files. Final suite outcomes follow after execution.
