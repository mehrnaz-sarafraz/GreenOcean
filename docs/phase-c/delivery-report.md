# Phase C — Registration completion

## Baseline and scope

Branch `feature/phase-c-registration-completion` starts at Phase B `b370ea00b72abb04bd9e6d85b2b2e126413d8214`, which descends from Phase A `745c24d`. Main is `ab44bc1` and does not contain A/B. Phase B PR #2 is open, draft and based on A. This PR is stacked on B; it must not be merged into main independently of that history.

Reviewed existing registration DTO/service, password encoder/policy, identity gate, policy publication/acceptance, geography, assistance/outbox, migrations through V33, Phase A delivery evidence and Phase B delivery report/test evidence. The exact Expo SDK 57 reference was read before code changes: https://docs.expo.dev/versions/v57.0.0/ . No full A/B audit was repeated.

`VERIFIED_EXISTING_DEPENDENCY`: minimal profile schema, USER role assignment, private defaults, self-attested adult eligibility, server-controlled region availability, versioned policy acceptance with server timestamp, PBKDF2 with BCrypt compatibility, encrypted email outbox, durable assistance limits, pending account/session gate and public pages. Prior evidence concerns the unchanged dependencies only; affected registration/session tests are retained as compatibility checks.

## C-01 through C-19 matrix

Paths below are relative to the repository. Backend test abbreviations: C = `PhaseCRegistrationIntegrationTest`; CC = `PhaseCConcurrencyTest`; CV = `PhaseCValidationTest`; A = affected `PhaseAIntegrationTest` compatibility tests. Frontend: U = `tests/phase-c.test.mjs`, E = `e2e/phase-c.spec.mjs` (real components mounted in Expo web, controlled transport), L = `live-e2e/registration.spec.mjs` (real browser, Spring HTTP server, PostgreSQL and delivered synthetic email).

Final statuses refer to executed CI tests at code commit `82b39198d57e712cf5c17978cbdb5f30a4f43e99`. COMPLETE_AND_TESTED describes the implemented contract and listed automated coverage, not production operational approval or unexecuted native-device behavior. Three requirements retain native-validation limits; C-17 also has an unresolved retention-policy dependency.

| ID | Initial status and actual gap | Work and changed files | Tests | Final status / limitations |
|---|---|---|---|---|
| C-01 | DEFECTIVE: validation preceded trimming; frontend/backend email rules differed | `EmailIdentity`, `RegisterRequest`, `registration-validation.ts`; explicit ASCII dot-atom/DNS/punycode policy, preserve dots/plus, trim/fold case per existing unique index | CV, C normalization, U email cases | COMPLETE_AND_TESTED; explicit supported email policy; SMTPUTF8 remains unsupported; existing identities unchanged |
| C-02 | PARTIALLY_IMPLEMENTED: common-password checks only server-side; DTO used UTF-16 minimum | `RegisterRequest`, `registration-validation.ts`, `register.tsx`, `app-field.tsx`; shared length/common rules, autofill, paste, visibility, LTR | CV maximum Unicode, C login, U, existing A visibility E2E | IMPLEMENTED_NOT_FULLY_TESTED; web/unit/backend pass; native password-manager/autofill NOT EXECUTED; compromised corpus remains server-authoritative |
| C-03 | COMPLETE_VERIFIED: no confirmation, existing visibility control | Preserve existing decision: password-manager/paste + visibility avoid a redundant confirmation step | existing A visibility E2E | COMPLETE_AND_TESTED; existing visibility/paste design preserved; confirmation intentionally absent |
| C-04 | COMPLETE_VERIFIED: server self-attestation and independent-decision guard exist | Reuse Phase A eligibility; field feedback and no new assurance service | C forged fields/no mutations, A direct underage/independent checks | COMPLETE_AND_TESTED; direct API and independent-decision regressions pass; self-attestation does not independently prove age |
| C-05 | COMPLETE_VERIFIED: catalog and independent service gating exist | Preserve country catalog and region checks; add clear no-service-guarantee copy | C invalid country / region authorization, E country choice | COMPLETE_AND_TESTED; invalid catalog inputs and unavailable-region access rejected; real market approval remains operational |
| C-06 | IMPLEMENTED_NEEDS_TESTS: generated username, no explicit collision loop | `RegistrationService`: transaction locks, retry generated identifier collision; retain unique index and later profile changes | C private minimal profile / forced collision, CC atomic duplicate, live later username update | COMPLETE_AND_TESTED; deterministic forced UUID collision retries and preserves existing username; concurrent signup and later customization pass |
| C-07 | COMPLETE_VERIFIED: neutral temporary display name exists | Preserve `New member`, ignore public registration profile claims; no existing names changed | C optional/malicious display; live Persian profile update; existing profile compatibility | COMPLETE_AND_TESTED; new account neutral display name and later Persian/Unicode profile editing verified through live backend |
| C-08 | COMPLETE_VERIFIED: DTO/schema/profile city already optional | No migration; preserve null city and no city form input | C minimal payload and null database assertion | COMPLETE_AND_TESTED; browser/API registration and persisted null city verified |
| C-09 | COMPLETE_VERIFIED: optional legacy birthYear, private age evidence exists | Preserve no birthdate collection, null new profile birthYear, SELF_ATTESTED evidence | C database state, A minor/independent tests | COMPLETE_AND_TESTED; no birthdate/year collected; null new birth year, private defaults and eligibility evidence verified |
| C-10 | COMPLETE_VERIFIED: no form role selector; USER assigned server-side | Preserve role gate, ignore extra authority fields, reject legacy elevated intendedRole | C forged roles and no mutation, A professional/listener regressions | COMPLETE_AND_TESTED; forged authority claims cannot elevate USER; professional/listener protections retained |
| C-11 | PARTIALLY_IMPLEMENTED: accessible explicit acceptance existed, stale acceptance recovery missing | `register.tsx`: recheck on focus, clear acceptance after version change, existing policy links retained | C stale version, E changed Terms, A policy link | COMPLETE_AND_TESTED; version refresh, accessible checked state and renewed explicit acceptance pass |
| C-12 | PARTIALLY_IMPLEMENTED: evidence existed; verification ignored changed/missing Terms | `AssistanceRepository.verifyUser`: require eligibility, catalog country and latest published Terms acceptance before PENDING→ACTIVE; existing activation route recovers | C Terms change between registration and verification, acceptance timestamp assertion | COMPLETE_AND_TESTED; server timestamp/version evidence and changed-Terms verification/activation recovery pass |
| C-13 | PARTIALLY_IMPLEMENTED: summary errors lost API field errors and stayed stale | `RegistrationValidationException`, handler, client, field, register, error mapper; adjacent feedback, correction clearing, focus/announcements, safe unknown errors | C safe field errors; U mapping/localization; E focus/server error | IMPLEMENTED_NOT_FULLY_TESTED; web field focus/clearing/semantics/localization pass; native screen-reader validation NOT EXECUTED |
| C-14 | DEFECTIVE: precheck raced with unique insert; existing/new HTTP outcomes distinguishable | `RegistrationService`, `RegisterResponse`: same 201/message on new and existing, hash on both paths, advisory transaction lock; no duplicate mail/password overwrite, unique index retained | C duplicate body equality/hash/code unchanged, CC two HTTP callers/one account/profile/acceptance/outbox | COMPLETE_AND_TESTED; equal public responses, no credential/mail overwrite, concurrent HTTP one-account results verified; constant-time equality not claimed |
| C-15 | PARTIALLY_IMPLEMENTED: durable IP and delivery limits existed, signup budget fixed and body unbounded | `AuthAbuseFilter`, application properties: configurable registration budget, Retry-After, 8KiB byte cap including absent Content-Length before parse/hash | C rate limit/malformed/oversized/no mutation; U/E retry feedback | COMPLETE_AND_TESTED; durable rate limit, Retry-After, body cap/no mutations verified; shared-NAT/proxy tuning operational |
| C-16 | COMPLETE_VERIFIED: modern salted hash and legacy upgrade existed | Preserve encoder and no secret logging; no hashing migration/reset | CV/A password tests; C stored-hash and long-password login | COMPLETE_AND_TESTED; modern long-password storage and legacy BCrypt upgrade compatibility pass; no hash implementation replaced |
| C-17 | PARTIALLY_IMPLEMENTED: lifecycle exists; activation navigation and Terms prerequisites incomplete | `AssistanceRepository`, `activate.tsx`, auth context; explicit pending next action, resumable verification, same existing lifecycle | C activation/stale Terms/wrong user's code; A replay/expiry/pending authorization/mail; E verify/signin/skip/signout/signin | BLOCKED_EXTERNAL_DEPENDENCY; pending/verification/activation/replay/expiry/resend and actual synthetic mail delivery tested; approved unverified-account retention period and cleanup operations remain unspecified |
| C-18 | PARTIALLY_IMPLEMENTED: timeout helper exists; same-tick duplicate taps, stale-policy retry and interrupted flow incomplete | synchronous refs in register/activate; loading announcements, actionable errors, retain form memory, re-enter email after restart; pending email in memory only, no personal route params/storage | U outcomes; E pending, lost response, restart, 429/503, data retention | IMPLEMENTED_NOT_FULLY_TESTED; web retry, loading, stale policy, duplicate taps, lost response, reload and re-entry pass; native restart/device checks NOT EXECUTED |
| C-19 | IMPLEMENTED_NEEDS_TESTS: existing minimal vertical contract needs C evidence | Reuse entity/schema/profile/preferences/policy/eligibility; only changed response removes public identity data. Existing test helpers look up IDs in isolated DB | C/CC/CV, affected integration fixtures, U/E | COMPLETE_AND_TESTED; minimal real browser→Spring→PostgreSQL→test delivery→activation→optional onboarding→logout/login passes; no new schema required |

No new tables, migrations, parallel state machine, geographic dataset, policy infrastructure, role selector or password confirmation. No changes to applied migrations.

## Implementation packages

1. Correct identity/password validation and safe field-error contract (C-01/02/13).
2. Serialize registration, generic duplicate response, bounded/configurable signup protection (C-06/14/15).
3. Complete verification prerequisites and recoverable accessible form behavior (C-11/12/17/18).
4. Add four-persona tests and narrow compatibility evidence.

## Execution evidence

Local: `node --experimental-strip-types --test frontend/tests/*.test.mjs`: **42 passed, 0 failed, 0 skipped** (32 existing helpers + 10 C helpers). `git diff --check` passed.

Local npm installation and Maven wrapper downloads encountered connection resets/timeouts from official registry hosts. Portable Java 21/PostgreSQL 18/tools were downloaded under ignored `.tmp` only; no installed application or user database was modified. Typecheck initially could not run because dependency installation was incomplete. CI is the independent complete toolchain and records authoritative integration/browser/build outcomes. Never count a blocked command as a pass.

CI commands: `./mvnw -B clean verify` against disposable PostgreSQL 18; `npm ci`, `npm run typecheck`, `npm run lint`, `npm test`, `npx expo export --platform all`, `npm run test:e2e`. The existing workflow includes C and uploads JUnit/browser/bundle artifacts. Running existing registration-dependent integration fixtures verifies the deliberately tightened public response contract; test helpers now obtain internal IDs through their isolated DB rather than leaking them in public signup responses.

## Four-persona coverage

| Persona | Added cases | Relevant requirements | Results / gaps |
|---|---|---|---|
| Happy | minimal private pending account/evidence, permitted activation, Welcome→signup→verify→signin→optional onboarding skip→signout→signin | 01–12,16–19 | All automated tests pass; production mail and retention operations remain external |
| Creative | whitespace/plus/punycode, maximum Unicode password/login, no profile requirements, restart/email reentry, 320/1440 screens, Persian LTR credentials | 01–09,13,18,19 | All automated tests pass; native password managers/multiple-device/manual reader checks unexecuted |
| Confused | common password, stale Terms at submission/verification, duplicate/lost response, focus/error clearing, timeout/offline, 429/503, reopening verification | 02,11–15,17–19 | All automated tests pass; native interruption checks unexecuted |
| Malicious | forged role/status/age method, invalid eligibility/country/Terms, malformed/oversized input, injection-shaped email, concurrent duplicate HTTP, code bound to wrong email, rate limit/no mutations | 01,04,05,10–17,19 | All automated tests pass; absolute security not claimed |

## Security and compatibility

Public signup now returns only generic instructions, on both fresh and duplicate addresses (201 retained for existing clients). Clients must not expect userId/email/username in signup responses. No password/profile mutation, extra outbox delivery or existing account reset occurs for duplicate signup. Hashing occurs on both paths to reduce the obvious timing shortcut; SQL queries remain parameterized. The transaction lock works across application instances; the original case-insensitive unique index is retained. The temporary username uses random data, never email. Existing profile updates remain available.

No authorization matcher or access policy weakened. Unknown extra JSON fields remain ignored under the inherited contract; authoritative values are server-derived. A declared underage birthYear is still rejected for legacy callers; registration does not collect it. Self-attestation cannot independently establish real age: stronger jurisdictional assurance stays an external prerequisite. Regions without AVAILABLE services still cannot access those services after email verification.

No claim of email, password or authorization security should exceed the actual CI/test/inspection evidence. Passwords remain component memory only; pending email is auth-context memory and disappears on restart/signout. Browser URL/storage no longer gets the registration email. Raw form bodies are not logged. Verification codes retain encrypted delivery, hashed account binding, limits, expiry and single-use behavior.

## Deployment and remaining dependencies

- No migration/production data operation. Roll out backend first (generic body compatible with current client, which ignores it), then frontend. Forward recovery: fix application code; no database restore required for this phase.
- Registration IP budget: `GREENOCEAN_REGISTRATION_ATTEMPTS` (default 30) and `GREENOCEAN_REGISTRATION_WINDOW_SECONDS` (60). Tune using observed abuse/shared-network traffic; do not trust arbitrary forwarded addresses.
- Production continues to require `prod` profile, configured delivery gateway, separate outbox key, explicit HTTPS origins/cookies, compromised-password corpus and explicit legacy deadline. Existing production validator fails startup if these are absent. JUnit tests use encrypted synthetic outbox/decrypted codes only in test classes. The live QA adapter is test-classpath-only, explicitly opted in, loopback-bound, random-key-protected and restricted to synthetic @example.test addresses; it is absent from the production jar.
- Approved market/clinical policies and real operational mail remain owner dependencies. Existing retention policy publication and pending-account cleanup operations require product/legal/operations decisions; no arbitrary expiration or deletion of legitimate records added.
- C-17 retention blocker was checked against `docs/phase-a-delivery-report.md` (remaining release gates), `docs/product/phase-a-requirements.md` (A-04) and `docs/migrations/phase-a-rollout.md`. None specifies an approved unverified-account retention period. Implementing a deletion deadline without that decision would change the inherited product policy; the existing lifecycle remains safe and access-restricted while pending.
- Android/iOS JavaScript exports are bundle checks, not native builds or device validation. Native binary/emulator, TalkBack/VoiceOver, password-manager/autofill and multiple-device checks NOT EXECUTED; see native checklist.
- Full browser→running Spring backend→PostgreSQL→synthetic delivered email E2E executed successfully for all four personas. This validates the configured secure test adapter, not external real-world mail deliverability.
- This is a reviewable draft; no automatic main merge or deployment.

## Pull request

Draft PR: https://github.com/mehrnaz-sarafraz/GreenOcean/pull/3 . Branch: `feature/phase-c-registration-completion`; base: `feature/phase-b-welcome-trust-experience`. Implementation `b5f609c`, live tests/accessibility `9e86aa8`, final code/security/collision evidence `82b3919`. CI: https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40362730975 — backend, frontend and registration-live all successful. Final documentation/screenshot-only commits preserve that tested code. No automatic merge or deployment.

## Final automated results

Authoritative code revision: `82b39198d57e712cf5c17978cbdb5f30a4f43e99`. All three jobs in [CI run 40362730975](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40362730975) passed. See `test-evidence.json` for artifact IDs, SHA-256 digests and exact commands.

| Executed check | Passed | Failed | Skipped |
|---|---:|---:|---:|
| Backend unit (password/storage/validation) | 15 | 0 | 0 |
| Backend integration (PostgreSQL, schema, migration, concurrency, affected A/B regression) | 74 | 0 | 0 |
| Frontend helpers | 42 | 0 | 0 |
| Browser component/transport E2E | 53 | 0 | 0 |
| Real browser/Spring/PostgreSQL/delivered-mail E2E | 4 | 0 | 0 |
| Total | **188** | **0** | **0** |

TypeScript, ESLint and web/Android/iOS JavaScript exports passed. Fresh Flyway application and legacy/populated schema upgrade are covered by the existing migration/schema tests; no applied migration changed. Native runtime/device/assistive-technology tests: NOT EXECUTED (not counted as automated passes or skips).

The 142 baseline automated cases are retained with 46 new C cases. One pre-existing emoji-only helper expectation was corrected to match the pre-existing server policy's minimum four distinct password code points. Internal-ID fixture lookups changed solely because public signup intentionally stopped returning account identifiers.

An initial run had 50/53 browser passes: web checked state was missing, a loading button lost its accessible name, and verification recovery pushed a screen over a still-mounted signup form. Explicit aria checked states, a stable accessible button label, and replacement navigation/password clearing fixed these defects. The controlled pending-response test now uses an explicit release gate rather than a timing-dependent sleep. Subsequent full browser runs pass; no failing case was skipped or removed.

| Added C persona | Backend | Helper | Browser | Live full stack | Total |
|---|---:|---:|---:|---:|---:|
| Happy | 3 | 1 | 1 | 1 | 6 |
| Creative | 6 | 2 | 4 | 1 | 13 |
| Confused | 3 | 5 | 7 | 1 | 16 |
| Malicious | 7 | 2 | 1 | 1 | 11 |

## Actual frontend and backend deliverables

Frontend: minimal existing form preserved; adjacent validation errors with clearing/focus, accessible password/autofill controls, stable loading button names and progress copy, explicit radio/checkbox state, LTR credentials in Persian, revalidated Terms on return, renewed consent after changes, synchronous duplicate guards, differentiated network/rate/server errors and verification recovery. Email is passed in auth-context memory rather than route/query storage. Activation can recover after restart with email re-entry, verify/resend safely and continue to permitted app/onboarding after sign-in.

Backend: bounded normalized email before validation, password code-point policy alignment, safe structured service errors, generic signup response, database transaction serialization and username collision retry, configurable signup limits/body bound, and current-policy/eligibility checks before pending activation. Existing profile/preference initialization, age/region authority, password encoder, policy evidence and account state machine are reused.

## Evidence-based security conclusions

| Property | Executed evidence and bounded conclusion |
|---|---|
| Adult eligibility | Direct false/underage payloads rejected; extra age-method/status claims do not grant authority; independent ineligible decision regression retained. Self-declaration cannot establish actual age independently. |
| Elevated roles | Forged roles/isAdmin/status ignored and elevated intendedRole rejected; DB role remains USER, live browser direct requests included. |
| Duplicate accounts / enumeration | Sequential and simultaneous case variants produce identical public success bodies and one account/profile/acceptance; existing hash/code unchanged. Timing reduction is not a constant-time guarantee. |
| Terms bypass | Missing/stale versions rejected without account mutation; changed Terms keep verification from activating until current acceptance is supplied. |
| Pending account access | Real pending session receives 403 for feed; inherited tests exercise community/conversation/listener routes and live session revocation. |
| Verification abuse | Code binding, replay, expiry, durable guessing and resend restrictions tested; secure synthetic transport delivers actual code in the live browser journey. |
| Password handling | PBKDF2 hashes persisted; long Unicode authenticates without truncation; old BCrypt remains compatible. Inspection finds no new raw-password logging/persistence. |
| Region and private resources | Unsupported catalog inputs fail; regions without approved service remain forbidden after verification; public resources remain anonymous and private routes remain authenticated. |

These are test/inspection results, not absolute guarantees or a complete penetration test.

## Visual review

Viewed the actual CI screenshots: `screenshots/phase-c-320.png`, `screenshots/phase-c-1440.png`, `screenshots/phase-c-persian.png`. The 320px bottom-of-form view fits controls and wrapping labels without horizontal overflow; desktop keeps readable centered width; Persian error/label copy, focused password input and LTR email remain readable. ScrollView screenshots record particular scroll positions, not a stitched capture of all content. Screenshots were captured at `9e86aa8`; final `82b3919` changes preserve these illustrated layouts, and final CI independently rechecks responsive/RTL cases. Native visual and screen-reader checks remain unexecuted.
