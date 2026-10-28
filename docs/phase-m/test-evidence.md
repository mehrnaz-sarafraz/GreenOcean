# Phase M evidence ledger

Baseline: L3 `d09fbd4`, executable `8843a61`, all four jobs in run40640710127 passed;524 tests. M1 adds V47 and reuses existing group/post/privacy/MFA paths.

M1 local attempts: the first complete332-test run passed the five new M1 privacy tests but failed an inherited assertion expecting an ordinary member's roster count. The new contract redacts counts as null. The next run exposed the inherited403 expectation; unauthorized group feeds intentionally use nondisclosing404. Both assertions were updated to the explicit stricter contract, retaining the underlying access-denial checks. Neither failed attempt is counted as a successful milestone.

M1 TypeScript passed; lint zero errors and11 inherited warnings. Both M1 browser tests passed (Persian/RTL private draft and failed-save text preservation). Additional default-off, populated-L migration and concurrent activation tests are included. Clean CI and real-backend MFA journey results will be recorded against their actual commit after execution. No native-device, actual staffing or production migration execution is claimed.
