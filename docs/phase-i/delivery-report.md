# Phase I delivery report

## A. Current state

Phase I continues the validated G/H source, local `785f45a`, published equivalent `38c114add7ce1d0c8ffcce8c68239b43ca004eaa`, on `feature/phase-gh-profile-privacy-onboarding` (PR #6). The targeted [initial gap matrix](gap-matrix.md) identifies the old instant conversation creation, offline fallback, unverified online Boolean and self-completable practice as defects. Existing accounts, roles, authentication/MFA, listener applications/coursework, messaging, reporting, blocks, stories, communities, articles and urgent directory are reused. No earlier migration is rewritten.

The implemented web/backend experience demonstrates real two-party request, acceptance, activation, Unicode messaging and termination using synthetic users against a real PostgreSQL-backed server. **Production activation remains blocked and disabled by default.** Native validation and external operational/legal approvals are not complete. This is an implementation delivery, not a claim that a staffed live service is launched.

## B. Requirements I-01–I-18

Paths below are repository-relative. `live/` means `backend/src/main/java/com/greenocean/backend/listener/live/`; frontend modules are under `frontend/src/features/live-support/`. Integration/concurrency class names resolve under `backend/src/test/java/com/greenocean/backend/`.

| ID | Change / principal files | Actual evidence | Final status |
|---|---|---|---|
| I-01 | Preserve H destination; intentional Support entry; `support.tsx`, `member.tsx` | Full prior helper/browser/live regressions; Phase I honest alternatives browser journey | VERIFIED_AND_REUSED |
| I-02 | Existing access policy plus current pair validation at every transition/send; `live/LiveSupportService.java`, V41 | Integration pending/ineligible/unsupported; suspension/block/revocation; existing A–H suite | COMPLETE_AND_TESTED (backend/web) |
| I-03 | Independent staff practice/current published code authorization; `live/LiveAuthorizationController.java`, V41 | Self-completed practice denied; reviewed synthetic listener works; revocation immediately closes session | IMPLEMENTED_WITH_EXTERNAL_RELEASE_GATE: actual full policy, reviewers and real listener evidence absent |
| I-04 | Current authenticated rotating lease and foreground heartbeat; `workspace.tsx`, `foreground.tsx`, V41 | Multi-device old lease and revoked-session tests; real UI readiness | PARTIALLY_VALIDATED: backend/web passed; native lifecycle matrix NOT_EXECUTED |
| I-05 | Explicit idempotent bounded request, no chat/no queue; `member.tsx`, `LiveSupportService` | Duplicate keys, one open request, rate limit, real browser no conversation before acceptance | COMPLETE_AND_TESTED (backend/web) |
| I-06 | Actual targeted listener accept/decline; `workspace.tsx`, controller/service | Wrong actor rejected; explicit two-party browser acceptance; concurrent accepts | COMPLETE_AND_TESTED (backend/web) |
| I-07 | Server deadline, cancel and scheduled/lazy expiry; V41/service | Exact expiry, cancel/decline terminal, bounded accepted activation; stale retries | COMPLETE_AND_TESTED (backend/web) |
| I-08 | Database advisory transaction lock and unique open capacities; V41/service | Last-slot race, distinct duplicate request, two service objects and real independent transactions | COMPLETE_AND_TESTED; production throughput unmeasured |
| I-09 | Separate accepted activation creates exactly one LISTENER conversation; messaging repository/V41 | No pre-activation chat; duplicate activation; ordinary DIRECT separation; rollback | COMPLETE_AND_TESTED (backend/web) |
| I-10 | Durable ACTIVE/INTERRUPTED/COMPLETED lifecycle; `chat.tsx`, V41/service | Reconnect/stale pulse/timeout, restart simulation, end/reload real browser | PARTIALLY_VALIDATED: real native/OS restart NOT_EXECUTED |
| I-11 | State/pair checks, DB insert guard and message retry keys; messaging service/repository | Real Unicode bidirectional messages; raw forged insert/participant rejection; late-send409 | COMPLETE_AND_TESTED (backend/web) |
| I-12 | Honest disabled/no listener/error status, explicit refresh; `member.tsx`, `ui.tsx` | Empty roster, save/activation error, no false navigation or hidden queue | COMPLETE_AND_TESTED (backend/web) |
| I-13 | Existing stories/groups/articles/urgent alternatives preserved; `ui.tsx`, support page | Existing full regression suites; no-listener live journey to education; disabled-service urgent API | VERIFIED_AND_REUSED |
| I-14 | Reuse report/block, immediate safety close, own export/erase; account repository, V41, `chat.tsx` | Actual report201/block journey, auth isolation; export excludes peer messages; erasure/races | COMPLETE_AND_TESTED (backend/web); privileged review MFA workflow reused |
| I-15 | English/Persian/RTL/status and accessible buttons, server-time counters; frontend modules/product header | FA at360/1280, helpers, browser keyboard focus, full real journey | PARTIALLY_VALIDATED: TalkBack/VoiceOver/physical device NOT_EXECUTED |
| I-16 | No sensitive topic collection/analytics; default-off gate, metadata threat model | DTO topic rejection, own export tests, source review | BLOCKED_EXTERNAL_DEPENDENCY: aggregate telemetry purpose/access/retention and legal approval |
| I-17 | 34 new PostgreSQL-backed tests,5 helpers,4 fixture browser,3 real-server browser; CI updated | [Test evidence](test-evidence.md), four personas and17 failure/race mappings | COMPLETE_AND_TESTED for automated scope; native matrix remains open |
| I-18 | Contract/API/state/privacy/operations and J/K decisions | This report and linked documents, reviewable stacked branch | IMPLEMENTED; external handoff items remain explicitly open |

## C. State machine and API

See [product contract](../product/live-peer-support-contract.md), [state machine](state-machine.md) and [API contract](api-contract.md). REQUESTED creates no chat. Target listener acceptance changes only to ACCEPTED. Explicit member activation creates one ACTIVE LISTENER conversation. Member/listener pulses can restore INTERRUPTED only inside the server reconnect deadline with both parties fresh. Terminal states never reopen. Discovery does not create requests; accepting does not silently activate; ordinary DIRECT conversation reuse excludes LISTENER.

## D. Database changes

Only additive `V41__live_listener_acceptance_and_sessions.sql`: independent live authorizations, authenticated presence, durable requests/session linkage, indexed unique open capacities and message idempotency. A deferred conversation FK and participant constraint permit atomic activation while preventing invalid LISTENER pair membership. Database checks/triggers supplement service checks for message insertion, safety revocation and terminal lease closure. A populated V40 migration test retains legacy messages/listener conversations and does not infer authorization or fabricate active sessions. Historical unlinked LISTENER reads fail closed. The deliberately failing test-only V42 proves transaction rollback and subsequent recovery; no failing production migration is shipped. See [operations](operations.md) for additive recovery, not destructive rollback.

## E. Security and privacy

See [threat model](privacy-and-threat-model.md). Access requires current adult/email/account/region eligibility and listener approval, complete current training, independent review and current published full code. The seed has no full listener code, so real authorization cannot be fabricated. Synthetic policy version900001 exists only in disposable tests. No new country, clinical inference, sensitive topic, transcript analytics or tracker is introduced. Own export includes own messages and limited own session metadata, not a copied peer transcript. Account erasure removes the new records and frees reservations under the same transaction lock.

The global advisory lock deliberately favors correctness over throughput; production load/latency is unmeasured. Existing stored histories/backup retention are not silently redefined. Metadata retention and aggregate observability need Phase J decisions. Staff-positive authorization is exercised through synthetic independently reviewed data; a real privileged reviewer MFA approval ceremony is not newly demonstrated end-to-end in Phase I (existing E/F boundary is retained).

## F. Four-persona results

Happy: two real browser accounts register/verify/login, reviewed listener explicitly becomes ready, member requests, listener accepts, member activates, both exchange Unicode text, reload, end, report and block. Creative: duplicate devices/keys/actions, restarted service, language/RTL and independent concurrent transactions converge. Confused: no available listeners, retryable API failures, bounded activation expiry, cancelled/declined offers and rollback recovery give honest outcomes. Malicious: forged actors/topics/messages, no independent approval, stale leases, pending/unsupported/suspended accounts, blocks/revocation, late sends and capacity/rate-limit bypass are rejected. All data is synthetic.

## G. Test results

See [test evidence](test-evidence.md) for commands, totals, failures corrected, runtime limitations and all17 required race/failure mappings. A browser bundle export is not a native runtime test.

## H. Operational blockers

Default `GREENOCEAN_LIVE_SUPPORT_ENABLED=false`. Before enablement: publish approved full listener code; independent actual reviewer/practice evidence; valid real geographic service approval; staffed coverage and escalation; legal notices/retention/access review; approved aggregate monitoring; real device/assistive technology and background validation; load and production deployment readiness. No deployment or merge is performed by this change.

## I. Phase J/K handoff

The [J/K decision register and exact NOT_EXECUTED native matrix](phase-j-k-handoff.md) are the handoff. Phase J owns unresolved policies/legal/product choices; K combines native/operational validation and regression. No compliance certification or clinical suitability is asserted.

## J. Pull request and review

Branch `feature/phase-i-first-real-support`, stacked on `feature/phase-gh-profile-privacy-onboarding`. GitHub publication details and CI state are recorded in the PR description and [publication record](publication.md). Review the default-off configuration, no legacy migration edits, independent reviewer requirement, SQL constraints, no pre-activation messaging, terminal-state guards, export/erase safety, and explicit unexecuted native/operational gates. CI results must correspond to the exact remote commit; local test results are not represented as GitHub Actions results.
