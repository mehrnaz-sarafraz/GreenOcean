# Phase D delivery report

## Scope and branch lineage

Phase D continues Phase C on `feature/phase-d-email-verification-activation`. Draft PR: https://github.com/mehrnaz-sarafraz/GreenOcean/pull/4, base `feature/phase-c-registration-completion`. C `9aaacc1` includes B `b370ea0` and A `745c24d`. Main remains `ab44bc1`; no merge, production migration, deployment, real-account cleanup or real-provider email was performed.

The initial inspection is [gap-matrix.md](gap-matrix.md); the supplied prompt is preserved in [approved-specification.md](approved-specification.md). Reused registration/auth/JWT sessions, account state/eligibility, policy acceptance, geography, AccessPolicy, assistance tables, mail worker and existing erasure. There is one verification flow and one outbox, one additive V36 migration. Public B pages and C onboarding were retained.

## Requirement evidence matrix

Paths below are relative to the repository. Backend package prefix is backend/src/main/java/com/greenocean/backend; tests are backend/src/test/java/com/greenocean/backend. Final classifications are deliberately limited by the evidence and external dependencies.

| ID | Before / action | Changed components and DB | Executable evidence | Final classification / remaining limit |
|---|---|---|---|---|
| D01 | Partial secure generation reused; serialize and preserve numeric formatting | AssistanceService, VerificationPolicy; existing challenges + V36 single-current index | PhaseDVerificationIntegrationTest leading-zero/malformed/resend; PhaseDConcurrencyTest competing resends; helper/browser pasted digits | COMPLETE_AND_TESTED; deterministic leading-zero test, no claim to prove randomness statistically |
| D02 | Existing protection strengthened with separate derived keys and ring | OutboxCipher, DeliveryKeyReadiness; V36 terminal payload purge | PhaseDProtectionTest rotation/v1/corruption/key separation; PhaseDOutboxIntegrationTest purge/readiness; PhaseDMigrationTest queued ciphertext | COMPLETE_AND_TESTED; operators must preserve real deployment keys |
| D03 | Existing server expiry retained; dispatch expiry added | AssistanceRepository, EmailOutboxWorker, WebhookMailTransport; server expires_at remains authoritative | PhaseDVerificationIntegrationTest before/exact/after boundary + ignored clientTime; PhaseDOutboxIntegrationTest expired dispatch | COMPLETE_AND_TESTED; provider must honor envelope expiresAt |
| D04 | Existing consume transaction reused, user locks/status checks added | AssistanceService/UserRepository; consume and activation same transaction | PhaseDConcurrencyTest ten confirms/one winner + rollback/retry; replay and suspended/deleted tests | COMPLETE_AND_TESTED |
| D05 | Existing outbox repaired; HTTP outside transaction, leases/backoff/idempotent IDs/outcomes | EmailOutboxWorker, MailTransport, WebhookMailTransport, MailEventController, MailDeploymentValidator, DeliveryKeyReadiness; V36 lease/status/event metadata | 11 outbox tests, 5 real loopback HTTPS gateway tests, authenticated event test, controlled-mail fullstack tests | BLOCKED_EXTERNAL_DEPENDENCY: real provider/sender/DNS/templates/alerts/staging inbox not supplied or verified |
| D06 | Existing durable hashed rate/IP controls reused; configurable stable account budgets and cooldown added | VerificationPolicy, AssistanceService, AuthRateLimit, TooManyRequestsException/handler; existing auth_rate_windows | five-guess cross-IP test, window expiry/recovery, resend cannot reset budget, dedicated 60s cooldown and unknown-account response test; existing A IP tests | COMPLETE_AND_TESTED for automated cases; production abuse tuning/alerting remains operational |
| D07 | Existing activation page extended | frontend activation screen, auth-provider, verification helpers; no new screen/schema | 7 D browser UI cases + 7 helper tests; real Persian paste/correction flow; previous A/B/C browser suites | IMPLEMENTED_NOT_FULLY_TESTED: native Android/iOS, VoiceOver/TalkBack and manual assistive-technology checklist not executed |
| D08 | Unsafe concurrent resend/verified issuance repaired | AssistanceService, AssistanceRepository; single-current index/cancelled old outbox | resend old/new code, two competing resends, cooldown, verified-account no-new-code, delayed old controlled mail | COMPLETE_AND_TESTED; cannot recall already in-flight obsolete email, but its code is invalid |
| D09 | C eligibility transition retained; suspended/deleted false success repaired | AssistanceService, UserRepository, IdentityController; no new states/roles | verification/region and suspended/deleted integration tests; C eligibility/terms suite; creative fullstack unsupported region | COMPLETE_AND_TESTED; email ownership and adult self-attestation are distinct; regional service access separately enforced |
| D10 | Existing AccessPolicy/interceptor reused without replacement | Protected endpoint behavior unchanged; tests expanded | negative HTTP requests for posting/comments/messaging/listener/professional/admin/likes/follows/community/other profile, no DB writes; own identity/profile/preferences and public resources allowed | COMPLETE_AND_TESTED for exercised categories; existing A/B regression suites retained |
| D11 | Missing workflow implemented | PendingEmailRequest, AssistanceService, User.correctPendingEmail, assistance endpoint, activation UI/auth context; existing audit_logs/session revocation + V36 owner links | own-session/password, malicious supplied userId ignored, collision generic/no change, old-code/session invalidation, concurrent changes, UI and true backend correction | COMPLETE_AND_TESTED for automated web/API flows; native UI under D07 remains untested |
| D12 | Missing scheduler implemented using existing transactional erasure | PendingAccountCleanup, AccountRepository; V36 verification_activity_at; no blind historical cascade | old/new/active/legacy/suspended/history exclusions, undelivered/recent resend, dry-run/pause/batch/restart, lock/activation race, rollback/retry, bounce/live recovery, configurable exact 7day boundary | COMPLETE_AND_TESTED for automated cases; owner approved 7days since last verification activity on2026-10-26. Disabled + dry-run by default; operational activation not executed |

## Validation ledger

Executable history: `3edab57` (implementation), `a533d79` (security/outbox/retention tests), `2909997` (correction recovery and true D fullstack), `ee4ca2c3e4ab0ed45bdaede8dd22db8913593004` (final retention boundary/activation-race evidence). Final results are recorded below. Earlier diagnostics are not represented as passing evidence: initial D CI caught render-time clock lint, a test-fixture lifecycle error, wrong HTTP method in a negative test, and real correction-message loss across auth subtree remount. Each was corrected before final evidence. No failed test was disabled or loosened to hide a production failure.

Final tested executable SHA: `ee4ca2c3e4ab0ed45bdaede8dd22db8913593004`. [CI40372115704](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40372115704): all three jobs succeeded. **254 passed, 0 failed, 0 skipped.** The subsequent documentation-only commit records this evidence without changing executable code.

| Gate | Result | Evidence |
|---|---|---|
| Java21 / PostgreSQL18 / real loopback HTTPS / migrations | 137 passed, 0 failed/errors/skipped; includes48 D tests and89 existing tests | [backend-junit](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40372115704/artifacts/12673790733) |
| Frontend helper tests | 49 passed, 0 failed/skipped | frontend job log |
| Browser UI fixtures | 60 passed, 0 failed/skipped; includes7 D cases | [browser-e2e-report](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40372115704/artifacts/12673097567), including generated Persian screenshot |
| True fullstack browser / controlled delivered mail | 8 passed, 0 failed/skipped; includes4 D personas and4 retained C cases | [registration-live-evidence](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40372115704/artifacts/12673521054) |
| TypeScript / ESLint | both pass | frontend job log |
| Expo web / Android / iOS export | all pass; device runtime NOT EXECUTED | [platform bundles](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40372115704/artifacts/12672229173) |
| Real staging/provider inbox and DNS | NOT EXECUTED / external blocker | no credentials or verified sender supplied |
| Native devices / manual assistive technology | NOT EXECUTED | operations checklist remains open |

Artifact links are retained under the repository's Actions retention policy, not permanent archives. Screenshots/traces were generated by the browser suite; downloading the screenshot for separate local manual inspection timed out, so no separate visual/assistive-technology audit is claimed.

Commands executed by the existing quality workflow:

```sh
# backend, Java21 and disposable PostgreSQL18
./mvnw -B clean verify
# frontend, Node24
npm ci --no-audit --no-fund
npx expo customize tsconfig.json
npm run typecheck
npm run lint
npm test
npx expo export --platform all
npm run test:e2e
# true fullstack job, separate disposable database/test-only mailbox
./mvnw -B test-compile dependency:build-classpath -Dmdep.outputFile=target/live-classpath
java -cp "target/test-classes:target/classes:$(cat target/live-classpath)" com.greenocean.backend.PhaseCLiveServer
npx playwright test --config playwright.live.config.mjs
```

Local: `node --experimental-strip-types --test tests/*.test.mjs` passed49, failed0, skipped0; `git diff --check` passed. Full dependency install/test execution was delegated to repository CI because this Windows network repeatedly stalled downloads. Production code is tested there, not assumed to work from static review. Native bundles are export/build evidence only, not device execution.

Four personas have executable backend, helper and UI coverage. Fullstack D cases use a real browser, real Spring backend, PostgreSQL and controlled transport, without mocked account-assistance responses: Happy corrects an address and activates the same account; Creative pastes Persian digits and stays region-restricted; Confused recovers with the newest resend while old mail fails; Malicious exhausts guessing and cannot reset the budget with resend. The four existing C fullstack cases remain. Controlled mailbox runs loopback, requires a per-run key, is compiled only in test sources and is absent from the production artifact. The HTTPS gateway test generates its own temporary certificate/key and trusts it only in the test client; production TLS validation is unchanged.

## Threat model and residual risk register

| Threat | Implemented control / evidence | Residual risk / operator action |
|---|---|---|
| Online six-digit guessing, distributed clients | keyed email-bound hash, account UUID budget, durable DB counters, existing IP throttle, expiry, one-use consume | finite entropy; limits can cause temporary denial of service; monitor distributed abuse and tune budgets |
| Database read compromise / payload disclosure | HMAC with external master; separately derived AES-GCM delivery key; purge after terminal/accepted/cancelled/expired | DB plus master compromise exposes live secrets; secret manager/access controls/rotation required; recipient metadata retained per existing account lifecycle |
| Replay / concurrent confirmation / resend races | user lock + atomic SQL consume + transaction activation + unique current challenge; 10-way/rollback tests | gateway cannot recall already in-flight email; server rejects old codes |
| Mail bombing and account enumeration | generic resend/collision response, account-stable delivery/correction caps + IP controls; staging exact allowlist | timing and valid login inherently reveal information to an authenticated owner; no claim of constant-time global non-enumeration |
| Owner email correction takeover | valid own pending session + password reauthentication, no caller-selected userId, unique normalized email, code retirement and session revocation, minimal audit | stolen session plus password can change a pending account; MFA is outside approved scope; generic collision response can require original-address recovery |
| Provider accepted then crashed / timeout | stable ID, lease CAS, expiry guard, bounded retries/backoff; uncertain state explicit | delivery exactly once depends on gateway deduplication; real provider delivery/inbox/DNS remains blocked |
| Forged callbacks / stale outcomes | distinct trusted key, timestamp window, reference correlation when available, idempotent later-event update; no authority transition | gateway credential compromise can falsify delivery states; ingress/signature checks and credential rotation required externally |
| Losing encryption keys on deployment | key IDs, overlap, v1 compatibility, startup readiness | no automatic key recovery; backup/ring deployment procedure required; keep old key for live hashes even after queue drains |
| Pending-account access / privilege escalation | existing server access policy and service geography, eligibility and current policy checks, no client-assigned roles | self-attestation is not independently verified age; mail ownership is not MFA or service authorization |
| Unfair/destructive retention | approved threshold, recent-activity protection, delivery/history/legacy exclusions, live recovery and row locks, bounded transactions, dry-run/pause | ACCEPTED does not prove inbox placement; pause during incidents; existing tombstones remain for FK integrity; no legal compliance conclusion |
| Deployment misconfiguration | production/staging mail startup validators, explicit recipients/templates/mode, existing prod session/origin/key/password-corpus guards | startup checks cannot verify SPF/DKIM/DMARC, inbox placement, actual gateway template content or alert routing |

See [operations.md](operations.md) for environment matrix, gateway/template contract, key rotation, migration/recovery, monitoring SQL, outage runbook, retention rollout and native checklist. Real mail delivery remains the release blocker; independent code work is reviewable in PR4.

## Reviewer checklist

- [ ] Review D delta against C; confirm A/B/C ancestry and unchanged main.
- [ ] Inspect V36 additive migration/deduplication, V35 upgrade fixture and old key overlap before deploying.
- [ ] Review separate key derivations, single-use transaction, resend serialization and durable budgets.
- [ ] Review worker claims, no network transaction, timeout/lease relationship, idempotency contract and expiry/purge states.
- [ ] Review authenticated pending correction, uniqueness/collision behavior, old challenge/session revocation and minimal audit.
- [ ] Review 7day approved policy, conservative exclusions, real activation/cleanup locking and default disabled/dry-run.
- [ ] Read final CI evidence and distinguish controlled HTTPS/mailbox from real staging/provider proof.
- [ ] Execute native/assistive-technology checklist before claiming full cross-platform UX readiness.
- [ ] Supply real sender/provider configuration, review templates and DNS, connect alerts and authorize staging tests before release.
- [ ] Merge only after human review; this task does not merge main or enable production cleanup.
