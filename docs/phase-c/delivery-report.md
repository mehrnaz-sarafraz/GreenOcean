# Phase C — Registration completion

## Baseline and scope

Branch `feature/phase-c-registration-completion` starts at Phase B `b370ea00b72abb04bd9e6d85b2b2e126413d8214`, which descends from Phase A `745c24d`. Main is `ab44bc1` and does not contain A/B. Phase B PR #2 is open, draft and based on A. This PR is stacked on B; it must not be merged into main independently of that history.

Reviewed existing registration DTO/service, password encoder/policy, identity gate, policy publication/acceptance, geography, assistance/outbox, migrations through V33, Phase A delivery evidence and Phase B delivery report/test evidence. The exact Expo SDK 57 reference was read before code changes: https://docs.expo.dev/versions/v57.0.0/ . No full A/B audit was repeated.

`VERIFIED_EXISTING_DEPENDENCY`: minimal profile schema, USER role assignment, private defaults, self-attested adult eligibility, server-controlled region availability, versioned policy acceptance with server timestamp, PBKDF2 with BCrypt compatibility, encrypted email outbox, durable assistance limits, pending account/session gate and public pages. Prior evidence concerns the unchanged dependencies only; affected registration/session tests are retained as compatibility checks.

## C-01 through C-19 matrix

Paths below are relative to the repository. Backend test abbreviations: C = `PhaseCRegistrationIntegrationTest`; CC = `PhaseCConcurrencyTest`; CV = `PhaseCValidationTest`; A = affected `PhaseAIntegrationTest` compatibility tests. Frontend: U = `tests/phase-c.test.mjs`, E = `e2e/phase-c.spec.mjs` (real components mounted in Expo web, controlled transport).

Final statuses are provisional until CI evidence is recorded. A requirement is not marked complete merely because a test was written.

| ID | Initial status and actual gap | Work and changed files | Tests | Final status / limitations |
|---|---|---|---|---|
| C-01 | DEFECTIVE: validation preceded trimming; frontend/backend email rules differed | `EmailIdentity`, `RegisterRequest`, `registration-validation.ts`; explicit ASCII dot-atom/DNS/punycode policy, preserve dots/plus, trim/fold case per existing unique index | CV, C normalization, U email cases | IMPLEMENTED_NOT_FULLY_TESTED; SMTPUTF8 not supported; existing identities unchanged |
| C-02 | PARTIALLY_IMPLEMENTED: common-password checks only server-side; DTO used UTF-16 minimum | `RegisterRequest`, `registration-validation.ts`, `register.tsx`, `app-field.tsx`; shared length/common rules, autofill, paste, visibility, LTR | CV maximum Unicode, C login, U, existing A visibility E2E | IMPLEMENTED_NOT_FULLY_TESTED; compromised corpus stays authoritative on server, native autofill NOT EXECUTED |
| C-03 | COMPLETE_VERIFIED: no confirmation, existing visibility control | Preserve existing decision: password-manager/paste + visibility avoid a redundant confirmation step | existing A visibility E2E | IMPLEMENTED_NOT_FULLY_TESTED pending regression |
| C-04 | COMPLETE_VERIFIED: server self-attestation and independent-decision guard exist | Reuse Phase A eligibility; field feedback and no new assurance service | C forged fields/no mutations, A direct underage/independent checks | IMPLEMENTED_NOT_FULLY_TESTED pending regression; self-attestation does not prove age |
| C-05 | COMPLETE_VERIFIED: catalog and independent service gating exist | Preserve country catalog and region checks; add clear no-service-guarantee copy | C invalid country / region authorization, E country choice | IMPLEMENTED_NOT_FULLY_TESTED; approved markets remain operator dependency |
| C-06 | IMPLEMENTED_NEEDS_TESTS: generated username, no explicit collision loop | `RegistrationService`: transaction locks, retry generated identifier collision; retain unique index and later profile changes | C private minimal profile, CC atomic duplicate | IMPLEMENTED_NOT_FULLY_TESTED; collision loop inspected, deterministic forced-collision test not yet added |
| C-07 | COMPLETE_VERIFIED: neutral temporary display name exists | Preserve `New member`, ignore public registration profile claims; no existing names changed | C optional/malicious display; existing profile compatibility | IMPLEMENTED_NOT_FULLY_TESTED; Persian later profile editing retained |
| C-08 | COMPLETE_VERIFIED: DTO/schema/profile city already optional | No migration; preserve null city and no city form input | C minimal payload and null database assertion | IMPLEMENTED_NOT_FULLY_TESTED pending integration |
| C-09 | COMPLETE_VERIFIED: optional legacy birthYear, private age evidence exists | Preserve no birthdate collection, null new profile birthYear, SELF_ATTESTED evidence | C database state, A minor/independent tests | IMPLEMENTED_NOT_FULLY_TESTED pending integration |
| C-10 | COMPLETE_VERIFIED: no form role selector; USER assigned server-side | Preserve role gate, ignore extra authority fields, reject legacy elevated intendedRole | C forged roles and no mutation, A professional/listener regressions | IMPLEMENTED_NOT_FULLY_TESTED pending integration |
| C-11 | PARTIALLY_IMPLEMENTED: accessible explicit acceptance existed, stale acceptance recovery missing | `register.tsx`: recheck on focus, clear acceptance after version change, existing policy links retained | C stale version, E changed Terms, A policy link | IMPLEMENTED_NOT_FULLY_TESTED pending browser |
| C-12 | PARTIALLY_IMPLEMENTED: evidence existed; verification ignored changed/missing Terms | `AssistanceRepository.verifyUser`: require eligibility, catalog country and latest published Terms acceptance before PENDING→ACTIVE; existing activation route recovers | C Terms change between registration and verification, acceptance timestamp assertion | IMPLEMENTED_NOT_FULLY_TESTED pending integration |
| C-13 | PARTIALLY_IMPLEMENTED: summary errors lost API field errors and stayed stale | `RegistrationValidationException`, handler, client, field, register, error mapper; adjacent feedback, correction clearing, focus/announcements, safe unknown errors | C safe field errors; U mapping/localization; E focus/server error | IMPLEMENTED_NOT_FULLY_TESTED; native reader behavior NOT EXECUTED |
| C-14 | DEFECTIVE: precheck raced with unique insert; existing/new HTTP outcomes distinguishable | `RegistrationService`, `RegisterResponse`: same 201/message on new and existing, hash on both paths, advisory transaction lock; no duplicate mail/password overwrite, unique index retained | C duplicate body equality/hash/code unchanged, CC two HTTP callers/one account/profile/acceptance/outbox | IMPLEMENTED_NOT_FULLY_TESTED; timing is reduced, not constant-time guaranteed |
| C-15 | PARTIALLY_IMPLEMENTED: durable IP and delivery limits existed, signup budget fixed and body unbounded | `AuthAbuseFilter`, application properties: configurable registration budget, Retry-After, 8KiB byte cap including absent Content-Length before parse/hash | C rate limit/malformed/oversized/no mutation; U/E retry feedback | IMPLEMENTED_NOT_FULLY_TESTED; proxy boundary and shared-NAT tuning operational |
| C-16 | COMPLETE_VERIFIED: modern salted hash and legacy upgrade existed | Preserve encoder and no secret logging; no hashing migration/reset | CV/A password tests; C stored-hash and long-password login | IMPLEMENTED_NOT_FULLY_TESTED pending regression |
| C-17 | PARTIALLY_IMPLEMENTED: lifecycle exists; activation navigation and Terms prerequisites incomplete | `AssistanceRepository`, `activate.tsx`, auth context; explicit pending next action, resumable verification, same existing lifecycle | C activation/stale Terms/wrong user's code; A replay/expiry/pending authorization/mail; E verify/signin/skip/signout/signin | IMPLEMENTED_NOT_FULLY_TESTED; real mail, retention operations, native restart remain external/manual |
| C-18 | PARTIALLY_IMPLEMENTED: timeout helper exists; same-tick duplicate taps, stale-policy retry and interrupted flow incomplete | synchronous refs in register/activate; loading announcements, actionable errors, retain form memory, re-enter email after restart; pending email in memory only, no personal route params/storage | U outcomes; E pending, lost response, restart, 429/503, data retention | IMPLEMENTED_NOT_FULLY_TESTED pending browser/native |
| C-19 | IMPLEMENTED_NEEDS_TESTS: existing minimal vertical contract needs C evidence | Reuse entity/schema/profile/preferences/policy/eligibility; only changed response removes public identity data. Existing test helpers look up IDs in isolated DB | C/CC/CV, affected integration fixtures, U/E | IMPLEMENTED_NOT_FULLY_TESTED; browser fixture and PostgreSQL tests are separate, no claim of live browser→server email test |

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
| Happy | minimal private pending account/evidence, permitted activation, Welcome→signup→verify→signin→optional onboarding skip→signout→signin | 01–12,16–19 | Helpers pass; integration/browser pending CI |
| Creative | whitespace/plus/punycode, maximum Unicode password/login, no profile requirements, restart/email reentry, 320/1440 screens, Persian LTR credentials | 01–09,13,18,19 | Helpers pass; native password managers/multiple device/manual reader checks unexecuted |
| Confused | common password, stale Terms at submission/verification, duplicate/lost response, focus/error clearing, timeout/offline, 429/503, reopening verification | 02,11–15,17–19 | Helpers pass; browser/integration pending CI |
| Malicious | forged role/status/age method, invalid eligibility/country/Terms, malformed/oversized input, injection-shaped email, concurrent duplicate HTTP, code bound to wrong email, rate limit/no mutations | 01,04,05,10–17,19 | Helpers pass; PostgreSQL pending CI; absolute security not claimed |

## Security and compatibility

Public signup now returns only generic instructions, on both fresh and duplicate addresses (201 retained for existing clients). Clients must not expect userId/email/username in signup responses. No password/profile mutation, extra outbox delivery or existing account reset occurs for duplicate signup. Hashing occurs on both paths to reduce the obvious timing shortcut; SQL queries remain parameterized. The transaction lock works across application instances; the original case-insensitive unique index is retained. The temporary username uses random data, never email. Existing profile updates remain available.

No authorization matcher or access policy weakened. Unknown extra JSON fields remain ignored under the inherited contract; authoritative values are server-derived. A declared underage birthYear is still rejected for legacy callers; registration does not collect it. Self-attestation cannot independently establish real age: stronger jurisdictional assurance stays an external prerequisite. Regions without AVAILABLE services still cannot access those services after email verification.

No claim of email, password or authorization security should exceed the actual CI/test/inspection evidence. Passwords remain component memory only; pending email is auth-context memory and disappears on restart/signout. Browser URL/storage no longer gets the registration email. Raw form bodies are not logged. Verification codes retain encrypted delivery, hashed account binding, limits, expiry and single-use behavior.

## Deployment and remaining dependencies

- No migration/production data operation. Roll out backend first (generic body compatible with current client, which ignores it), then frontend. Forward recovery: fix application code; no database restore required for this phase.
- Registration IP budget: `GREENOCEAN_REGISTRATION_ATTEMPTS` (default 30) and `GREENOCEAN_REGISTRATION_WINDOW_SECONDS` (60). Tune using observed abuse/shared-network traffic; do not trust arbitrary forwarded addresses.
- Production continues to require `prod` profile, configured delivery gateway, separate outbox key, explicit HTTPS origins/cookies, compromised-password corpus and explicit legacy deadline. Existing production validator fails startup if these are absent. Local tests use encrypted synthetic outbox/decrypted codes only in test classes, never a public code endpoint.
- Approved market/clinical policies and real operational mail remain owner dependencies. Existing retention policy publication and pending-account cleanup operations require product/legal/operations decisions; no arbitrary expiration or deletion of legitimate records added.
- Android/iOS JavaScript exports are bundle checks, not native builds or device validation. Native binary/emulator, TalkBack/VoiceOver, password-manager/autofill and multiple-device checks NOT EXECUTED; see native checklist.
- Full browser→running backend→controlled delivered email E2E is not yet executed; UI transport fixtures and real PostgreSQL integration independently cover the two sides.
- This is a reviewable draft; no automatic main merge or deployment.

## Pull request

Branch: `feature/phase-c-registration-completion`; base: `feature/phase-b-welcome-trust-experience`. URL, commits and CI results will be added after creation.
