# Phase B delivery report

## A. Implementation coverage

Baseline: Phase A commit `745c24d` on `feature/phase-a-identity-privacy-access`. Main does not yet contain Phase A. Phase B is a stacked PR, with no merge or deployment performed.

| Requirement | Status | Main files | Tests/evidence | Notes |
|---|---|---|---|---|
| B-01 | COMPLETE_AND_TESTED | welcome.tsx, public-copy.ts | Happy unit + browser mission | Plain product purpose, no treatment promise |
| B-02 | COMPLETE_AND_TESTED | welcome.tsx, public-information.tsx | Three pathways + public explainers | Account requirements and clinical distinctions explicit |
| B-03 | COMPLETE_AND_TESTED | welcome/register/login, public-page.tsx | Navigation/double-tap + all A signup browser regressions | Existing minimal signup/Terms/adult requirements retained |
| B-04 | COMPLETE_AND_TESTED | auth-provider, token-storage, API client, app layout | Loading, cookie restore, pending activation, expired session | Invalidation updates auth UI; old requests cannot overwrite newer session |
| B-05 | COMPLETE_AND_TESTED | safety/urgent-help.tsx, CatalogRepository | Public/manual/no-local/offline/unsafe URL; PostgreSQL | No GPS, invented numbers or default country |
| B-06 | COMPLETE_AND_TESTED | public-copy.ts, copy-audit.md | Copy assertions + UI boundaries | Public posts, staff review, privacy and clinical limits explained |
| B-07 | COMPLETE_AND_TESTED | public-information, PolicyService/Repository | Published/draft/future/missing/error/stale/HTML | Missing legal documents fail honestly; publishing remains external |
| B-08 | COMPLETE_AND_TESTED | public-copy.ts, welcome.tsx | Copy checks + guest request isolation | No fake counts or new availability service |
| B-09 | IMPLEMENTED_NOT_FULLY_TESTED | public-page, controls, localization | Six browser widths, RTL, keyboard, contrast, enlarged text, all-platform bundles | Android/iOS runtime and assistive technology validation remain unexecuted |
| B-10 | COMPLETE_AND_TESTED | public-information, assistance-form, PublicReadAbuseFilter | FAQ/navigation/route allowlist/shared rate limit | No guest health disclosure, member feed, or duplicate public API |

Statuses describe implemented behavior covered by the evidence below. They do not assert production operational, legal, or native-device readiness.

## B. Changed code and reasons

Paths are repository-relative; the PR Files changed view is the complete authoritative diff.

Frontend:

- `src/app/(auth)/welcome.tsx`: existing Welcome rebuilt with mission, three independent paths, process, boundaries, public links and session-aware entry.
- `src/features/public/public-page.tsx`: shared public shell, safe area, headings/text, language controls, navigation and duplicate-tap protection.
- `src/features/public/contracts.ts`: finite public destinations, authentication entry classification and strict HTTPS/tel validation.
- `src/features/public/public-information.tsx`: current published documents, version/date, explicit unavailable/retry states, safe literal rendering, stale-response cancellation and public explanations.
- `src/features/public/assistance-form.tsx` and `src/app/(auth)/info/[page].tsx`: reuse the existing information route and account-assistance endpoints; preserve recovery/contact/verification forms.
- `src/app/(auth)/resources.tsx`: existing public resources with loading/empty/error/retry, publisher-language notice and clinical boundaries.
- `src/app/(auth)/urgent-help.tsx`, `src/features/safety/urgent-help.tsx`: public/manual country directory, source/verification dates, explicit missing coverage, safe external actions and network recovery.
- `src/app/(auth)/login.tsx`, `register.tsx`, `src/app/(app)/_layout.tsx`: authenticated/loading entry, accessible back/recovery controls, localized validation and the existing eligibility gate.
- `src/features/auth/auth-provider.tsx`, `src/lib/storage/token-storage.ts`, `src/lib/api/client.ts`: session-clear subscriptions and generation-aware invalidation prevent stuck guest restoration and expired-session navigation loops. Web tokens remain memory/cookie based.
- `src/localization/public-copy.ts`, `language-provider.tsx`, `translations.ts`: complete English/Persian public copy, persistent language selection and web/native RTL direction. Published documents retain their publisher's language.
- `src/components/app-button.tsx`, `app-icon.tsx`, `src/app/_layout.tsx`: visible keyboard focus, readable button text, hidden decorative icons and public content available even before optional fonts load.
- `tests/phase-b.test.mjs`, `e2e/phase-b.spec.mjs`, `e2e/fixtures/public-api.mjs`, `e2e/phase-a.spec.mjs`: persona tests and retained A regression scenarios; shared synthetic API fixture, not a replacement for server authorization tests.
- `playwright.config.mjs`: reproducible browser configuration with optional installed Chrome channel.

Backend:

- `catalog/repository/CatalogRepository.java`: crisis entries must be active and verified on a non-future date.
- `preference/repository/PolicyRepository.java`: only genuinely published, non-future policies returned.
- `preference/service/PolicyService.java`: bounded canonical policy keys; malformed keys rejected rather than silently transformed.
- `identity/PublicCatalogController.java`: bound the public city subdivision parameter.
- `identity/PublicReadAbuseFilter.java`: shared durable 120/minute public GET budget using the existing rate-limit store and server-observed address; changing locale/query/route does not evade it.
- `common/exception/GlobalExceptionHandler.java`: unknown resources return a generic 404.
- `PhaseBPublicAccessIntegrationTest.java`: fourteen PostgreSQL-backed public/privacy/abuse scenarios, alongside all A security regressions.

Infrastructure and documentation:

- `.github/workflows/phase-a.yml`: reuse existing quality gates, include B branch, deduplicate runs, export web/Android/iOS bundles, preserve test artifacts, use installed Chrome where available.
- `docs/phase-b/approved-specification.md`, `audit.md`, `copy-audit.md`, `mobile-smoke-checklist.md`, this report: approved scope, before-change audit, evidence-based copy decisions and explicit outstanding device checks.

No database migration or schema change. Existing migration files and real user records were not modified. Local backend QA creates and stops a separate synthetic PostgreSQL cluster.

## C. Actual validation

Evidence and final CI results are recorded in `test-evidence.json`. Browser tests execute the real Expo web application against a controlled API fixture; backend integration tests independently exercise Spring Security and PostgreSQL. Passing UI fixtures alone is not evidence of backend authorization.

Commands used:

```powershell
& .\scripts\run-phase-a-qa.ps1 -Offline
node --experimental-strip-types --test frontend/tests/*.test.mjs
```

CI, from `frontend/`:

```text
npm ci --no-audit --no-fund
npx expo customize tsconfig.json
npm run typecheck
npm run lint
npm test
npx expo export --platform all
PLAYWRIGHT_CHANNEL=chrome npm run test:e2e
```

CI, from `backend/`, against disposable PostgreSQL 18:

```text
./mvnw -B clean verify
```

| Check | Passed | Failed | Skipped | Scope |
|---|---:|---:|---:|---|
| Backend unit | 10 | 0 | 0 | Password/encryption/storage tests |
| Backend integration | 60 | 0 | 0 | Spring/PostgreSQL, migration, concurrency, public access and protected API regressions |
| Frontend helper/unit | 32 | 0 | 0 | 17 A + 15 B pure TypeScript contract/copy/request checks |
| Browser E2E | 40 | 0 | 0 | 12 A + 28 B against actual Expo UI and synthetic API contracts |
| Total | 142 | 0 | 0 | 85 retained + 57 added |

Local backend verification passed all 70; CI independently repeats the backend tests against a fresh PostgreSQL service. Local frontend helper tests passed all 32. TypeScript, ESLint and web/Android/iOS JavaScript/asset exports passed in CI. Browser tests passed with no retries or skips. Exact tested revision, CI links and every test name are in `test-evidence.json`.

| Added B persona | Backend | Helper/unit | Browser | Total |
|---|---:|---:|---:|---:|
| Happy | 3 | 5 | 5 | 13 |
| Creative | 3 | 4 | 10 | 17 |
| Confused | 3 | 3 | 8 | 14 |
| Malicious | 5 | 3 | 5 | 13 |

An earlier browser run had 37 passes and two test-selector failures: an ambiguous duplicate label and a check that counted hidden previous-route text. These were corrected to select the intended action and verify visible current-document content; the subsequent complete run passed all 40 scenarios. Nothing was skipped or disabled to obtain the pass.

Accessibility checks cover body/button/error/focus contrast, semantic headings, decorative icon suppression, keyboard focus and order, six widths (320/375/390/768/1024/1440), Persian persistence/RTL, enlarged browser text, landscape and reduced-motion functionality. This is not a full WCAG conformance audit. Screen-reader and native device results are not claimed.

[Visual review and retained screenshots](visual-review.md) document the actual desktop, narrow mobile, Persian and error-state inspection. ScrollView screenshots represent individual scroll positions rather than a stitched page.

Android/iOS JavaScript/asset exports are build checks, not native binary builds, simulator runs, store release checks or device tests. The exact unexecuted device steps are in `mobile-smoke-checklist.md`.

## D. Security review

- No authorization route matcher, Phase A adult/email/region requirement, live session/role check, credential review or professional-care scope was relaxed.
- Guest authorization tests exercise protected feed, messages, listener/professional data, private profile, posts, articles and media with changing locale/region and require 401.
- Public response assertions exclude private user fields. No social API or membership/capacity count becomes public.
- Policy drafts/future documents are hidden; invalid destinations fail closed; published text is rendered as text, not HTML. Late responses cannot replace a different requested policy.
- Country selection is explicit and does not use GPS. Resource URLs reject scripts, embedded credentials, controls and unsafe telephone strings. Directory dates do not establish universal/current coverage.
- Shared durable public rate limiting uses the existing hashed-key store; no raw address logging, new analytics or health profiling was introduced.
- No fabricated real-time service capacity, absolute confidentiality, guaranteed safety/outcome, continuous monitoring or immediate support claim was added.
- Browser QA uses synthetic people, addresses, tokens and resource URLs. No real health histories or production user database were used.

## E. Product review

Visitors can understand the product, compare community/listener/professional support, inspect policies, read FAQs and reach urgent/public resources before creating an account. Account creation is an explicit choice after the limits and eligibility are explained. Loading, unavailable coverage and unpublished policies remain visible as uncertainty rather than implied guarantees. English/Persian controls and progressive explanation support visitors who are unfamiliar with the service.

## F. Remaining limitations and dependencies

- Android/iOS device, TalkBack/VoiceOver, external-app return, notch/keyboard and system text scaling checks remain unexecuted; see checklist. Browser checks and bundle exports cannot establish these results.
- Publishing approved age-eligibility, service-boundaries and data-retention documents remains a legal/product owner dependency. The UI deliberately displays an unavailable state if no current published document exists. No legal content was auto-published or a draft represented as current.
- The directory only covers the existing curated entries. Verification/maintenance, local emergency operations, country/market approval, staffing and clinical availability are operational dependencies. No phone number was invented.
- Policy/resource text may not be translated; the UI identifies the publisher-language limitation. Private app translation completeness is outside B.
- Phase A's documented production prerequisites (including real mail delivery, MFA/re-authentication, market/clinical approvals and retention operations) remain applicable.
- Proxy deployments must configure a trusted address boundary deliberately; the public abuse filter uses the observed remote address and does not trust arbitrary forwarded headers. Shared NAT users may share its budget.
- No release to production or merge is authorized by completion of these code checks.

## G. Pull Request

Branch: `feature/phase-b-welcome-trust-experience`.

Draft PR: https://github.com/mehrnaz-sarafraz/GreenOcean/pull/2

Base: `feature/phase-a-identity-privacy-access` (stacked on PR #1). Merge Phase A through review first, then retarget B as appropriate. No force push, automatic merge or deployment performed. Final tested revision and CI links are in `test-evidence.json`.
