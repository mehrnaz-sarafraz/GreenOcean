# Phase I executed test evidence

## Foreground continuation (2026-10-27)

Continuation executable source: `4e3bfb4bfcd3b0f080831ffa991e7dab0940174b`, based on original delivered head `1b267c85c85e61ab37c21c5ec257ec7a21fdc43c`. The newly attached specification matches `supplied-specification.md` exactly after newline normalization. Original evidence below remains the original snapshot, not new local execution.

Local command: `node --experimental-strip-types --test frontend/tests/*.test.mjs` from repository root: **68 passed, 0 failed, 0 skipped**. Four added tests in `phase-i-foreground.test.mjs` hold actual promises across hidden/visible transitions, return before resolution, close/unmount, overlapping polls and network failure. They verify that stale responses cannot renew readiness, update a resumed visit or revive a closed screen, and that new legitimate polling remains possible.

Added fixture browser test: `I-04 UI: a late availability response cannot renew a previous foreground visit`. It holds the listener availability HTTP response, dispatches hidden then visible lifecycle events on the actual rendered workspace, and requires a new availability read before any incoming-request lookup/heartbeat. It uses synthetic transport; it does not claim native OS behavior.

Full final-source CI: [40534723099](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40534723099). **Completed successfully on that exact executable SHA:** backend 234/234 (0 failures/errors/skips); frontend helpers 68/68; fixture browser 84/84; real-server browser 20/20. Total **406 passed**, no failed/skipped tests. TypeScript, lint (0 errors, 11 existing warnings) and web/Android/iOS JS/assets export passed. All three jobs succeeded. The duplicate push run was cancelled by configured concurrency; this is not a failed test. This report update changes documentation only; it preserves the tested executable source. Physical Android/iOS, TalkBack/VoiceOver, real staffing and launch approvals remain unexecuted or external. This Windows workspace has no `adb`, `emulator` or `xcrun` command available; the manual matrix remains in `phase-j-k-handoff.md`.

Recorded 2026-10-27. All users, listener approvals, code content and regional fixture permissions are synthetic in disposable local PostgreSQL18 databases. No production connection or data is used. The actual source files/test inventory are recorded in `test-evidence.json`. Release restrictions and unexecuted work are in [J/K handoff](phase-j-k-handoff.md).

## Commands and results

| Check | Command / conditions | Actual result |
|---|---|---|
| Backend entire suite/package | `cd backend && bash ./mvnw -B clean verify`, JDK21, explicit `GREENOCEAN_TEST_DB_URL/USER`, startup Mockito agent as described below | 234 tests,0 failures,0 errors,0 skipped; BUILD SUCCESS |
| New backend coverage | Included in the above: `PhaseILiveSupportIntegrationTest`18, `PhaseIConcurrencyTest`12, `PhaseIDisabledServiceTest`1, `PhaseILiveSupportSettingsTest`2, `PhaseIMigrationTest`1 | 34 passed; real PostgreSQL constraints/transactions |
| Frontend helpers | `cd frontend && npm test` | 64 passed,0 failures,0 skipped;5 new Phase I |
| Types | `npm run typecheck` | Passed |
| Lint | `npm run lint` | Passed;0 errors,11 existing warnings |
| Fixture browser full regression | `npx playwright test` | 83 passed,0 failures,0 skipped |
| Real-server full regression | `npx playwright test --config playwright.live.config.mjs` against test-only `PhaseCLiveServer`, explicit two isolated QA flags, real PostgreSQL18 | 20 passed,0 failures,0 skipped;3 new Phase I |
| Bundle generation | `npx expo export --platform all` | Web/Android/iOS JS/assets exported; no native runtime/device execution inferred |

Local runtime: root-only managed container cannot run stock PostgreSQL as root. A temporary, uncommitted UID/stat shim was used exclusively for disposable local tests; shared/dynamic memory used mmap and parallel workers0. Proxy/trust settings and Java21 runtime were local only. Mockito5.23.0 was started with `-DargLine=-javaagent:/root/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar` because dynamic attach is restricted. CI uses normal PostgreSQL18/Java21 and does not use these container adaptations. Exact Maven invocation also used a temporary proxy `-s` settings file; no proxy secrets are committed. The production database schema/configuration is not relaxed for testing.

Real browser server setup uses the existing QA opt-in `GREENOCEAN_LIVE_BROWSER_QA=isolated-only` plus new `GREENOCEAN_LIVE_SUPPORT_QA=isolated-only`; test-only sources are absent from the production jar. The loopback/key-protected fixture route may add independent qualification to a synthetic eligible @example.test account, but cannot fabricate a readiness lease. The listener actually logs in and explicitly becomes ready through the UI.

## Four-persona test ownership

Integration/concurrency Java methods carry happy/creative/confused/malicious tags. Happy is demonstrated by the full two-party real-server journey plus export/erase. Creative includes multi-device readiness rotation, retries, restart simulation and FA/RTL. Confused includes zero listeners, expiry, error recovery and rollback. Malicious includes forged actor/third party, role/self-approval, eligibility, blocks/suspension, raw inserts, retry payload conflict, rate/capacity attempts and late sends. Fixture browser tests supplement real-server evidence; they do not substitute for PostgreSQL locking or actual acceptance.

## All17 required race/failure cases

Abbreviations: C=`PhaseIConcurrencyTest`, I=`PhaseILiveSupportIntegrationTest`; all are real-database tests. Method names are exact.

| # | Required case | Executed evidence |
|---|---|---|
|1|Last listener slot, two members|C.twoServiceInstancesCompetingForOneListenerHaveOneCapacityWinner|
|2|Two accepts|C.memberDuplicateDevicesAndListenerDuplicateAcceptanceConvergeToOneRequest|
|3|Expiry vs acceptance|C.exactExpirationWinsAgainstConcurrentAcceptanceAndRestartedWorker; I.exactExpiredDeadlineCannotBeAcceptedWithoutScheduler|
|4|Cancel vs acceptance|C.acceptanceVersusCancellationAlwaysEndsCancelledWithoutConversation|
|5|Block vs acceptance|C.blockingVersusAcceptanceAndSuspensionVersusActivationCannotLeaveLiveAccess|
|6|Suspension vs activation|Same C method, second race|
|7|Logout vs heartbeat|C.logoutVersusHeartbeatAndAuthorizationRevocationVersusActiveSessionFailClosed|
|8|Requester deletion vs active session|C.requesterErasureDuringActiveSessionReleasesSlotAndDoesNotPreservePrivateRequest; distinctDuplicateRequestsAndRequesterDeletionVersusActivationHaveOneSafeOutcome|
|9|Listener privilege revoked vs active|C.logoutVersusHeartbeatAndAuthorizationRevocationVersusActiveSessionFailClosed; I.suspensionAndListenerRevocationInvalidateActiveCapacity|
|10|Restart after request|C.committedAcceptanceRetryAndRestartedPendingReadNeverDuplicateOffers|
|11|Restart after acceptance before chat|C.restartedServiceRestoresAcceptedStateAndDuplicateActivationAndEndAreIdempotent|
|12|Timeout after committed accept|C.committedAcceptanceRetryAndRestartedPendingReadNeverDuplicateOffers (discard first response, retry)|
|13|Duplicate activation|C.restartedServiceRestoresAcceptedStateAndDuplicateActivationAndEndAreIdempotent|
|14|Duplicate end|Same C method|
|15|Rollback, no capacity leak|C.databaseFailureRollsBackConversationAndAcceptanceReservationRemainsRecoverable; failedRequestCreationTransactionDoesNotLeakCapacity|
|16|Multiple backend instances|C.twoServiceInstancesCompetingForOneListenerHaveOneCapacityWinner (separate service objects and independent transactions on same real PostgreSQL)|
|17|Stale send after end|I.realAcceptanceActivationMessagingEndAndHistoryAreSeparateAndRetrySafe; real-server Phase I journey asserts409 after end and clears blocked peer data|

Restart cases reconstruct services against committed database state; they do not claim a physical host/process crash or deployment failover. Multi-instance cases are multiple service objects/real independent transactions, not multiple physical hosts. Production load, browser multi-host failover and device network emulation are not executed. Reconnect state/deadlines are verified against actual database timestamps.

## Failures found and corrected

Initial synthetic listener qualification failed because no published full code exists in seed. Test-only policy/version900001 fixes the fixture, without writing fictional production policy. A full-suite regional failure exposed concurrency fixture pollution; the concurrency class now has an isolated temporary schema with cleanup. A local overlapping test/runtime attempt hit a PostgreSQL filesystem EOF error and was interrupted; fresh disposable runtime with mmap then completed all234 successfully. These failed attempts are not counted as successes.

The first full fixture browser run had82 passes and one pre-existing keyboard-focus test failure during hydration. Its test now retries acquisition of focus with short individual assertions; it still requires actual focus,3px visible border and exact Tab order. No product focus assertion was removed. An isolated rerun passed before the final full rerun.

A subsequent full fixture run passed that focus test but exposed a race in the pre-existing offline-logout test: asserting an absent sign-out button could succeed before auth bootstrap, then the test restored networking before pending-logout reconciliation ran. The test now first waits for the explicit pending-logout retry UI after reload, preserving all logout-marker/token assertions. No authentication product code was changed for this fixture synchronization.

The first full real-server run had19 passes and one Phase I locator failure because Expo retained an inactive screen with the same status text. Chat status now has a localized accessible status label, and the assertion scopes to that actual chat status. The subsequent full20-test run passed. Real activation/message/block assertions were not weakened.

Export prints Expo's existing forced-exit notice after `Exported: dist`; exit status is checked separately. Native TalkBack/VoiceOver, physical Android/iOS, production staffing/reviewer ceremony, load/monitoring/retention/legal approval remain NOT_EXECUTED or BLOCKED as specified in the handoff. Local results must not be relabeled as remote CI results.
