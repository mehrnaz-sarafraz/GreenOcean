# Phase L audit follow-up — 2026-10-28

Continues published L3 `9ed49986ce0c2f4b2facd275684c04c3c036a3bb`, PR12 against L2. The read-only audit identified four actionable code/delivery gaps; this continuation addresses them without changing approved identity, audience, publication rollout or retention decisions.

## Corrections

1. **Comment reaction authorization:** `PostService.setCommentLike` now requires the existing visible-comment projection as well as parent-post access. A known comment ID on an accessible third-party post cannot bypass either block direction or an inactive comment author. Rejected writes return the same safe404 and create no reaction.
2. **Following empty states:** the server supplies an owner-scoped nullable `followingAnyone` flag. The UI separates no active unblocked follows, no permitted named stories, loading and network failure. The flag is not inferred from an incomplete feed page and never contains another account's identity. Anonymous content remains excluded from Following.
3. **English/Persian accessibility:** search headings, input/clear labels, results/loading/error copy, social-card actions, warning controls, audience labels and feed copy are localized. Search input and post body direction follow the selected language; anonymous cards announce reading the anonymous post instead of a nonexistent author-profile action. Server-authored user content/catalog labels are not fabricated translations.
4. **Published evidence:** delivery report, requirement matrix, owner decisions and machine evidence now acknowledge uploaded PR12 and successful published CI. The historical upload rejection and unavailable local-only source hash are not presented as current evidence.

No database migration is needed. The response change is additive; old clients may ignore it, and clients receiving no flag use the generic empty message. Existing post audience, staff MFA, listener isolation, optional-processing and default-off publishing controls remain authoritative.

## Regression evidence

New database-backed tests in `PhaseL2SocialTest`:

- `commentReactionsRespectBothBlockDirectionsOnThirdPartyPosts`: three actors, both block directions, both PUT/DELETE reaction routes, no reaction inserted.
- `reactionsToInactiveCommentAuthorsAreUnavailable`: published parent remains accessible while the inactive author's reaction route returns404.
- `followingEmptyStateUsesOwnRelationshipsWithoutExposingAnonymousContent`: false→true relationship state with anonymous-only content still excluded and no author attribution.

New browser journeys in `phase-l2.spec.mjs` cover no follows versus no eligible stories versus503, and Persian RTL search/input/clear/result headings plus anonymous post and reaction accessible names. They supplement the existing distinct-server-query test.

Local Windows verification on Java21 / disposable PostgreSQL18: **327 backend passed, zero failures/errors/skips** using `scripts/run-phase-a-qa.ps1 -Offline`. The initial frontend logic attempt failed the existing K dependency assertion because node_modules still contained the older J dependency; it is not counted as passing. Dependencies are restored from the checked-in lockfile before final execution. TypeScript passed after Expo regenerated its stale J-era route cache. No production database was used. Browser and final published CI outcomes are recorded below after execution; baseline results must not be counted as new execution.

Local re-execution after `npm ci --no-audit --no-fund`: **69 logic tests passed, zero failures/skips**, and `PLAYWRIGHT_CHANNEL=chrome EXPO_OFFLINE=1 npx playwright test e2e/phase-l2.spec.mjs` passed **all3 tests**, including both new browser journeys. No dependency files were changed; this restored the checked-in L lockfile's installed versions. The original failed attempts remain recorded above.

## External release dependencies

Actual moderation staffing, approved policy wording, legal/evidence retention decisions, real staff key custody and physical Android/iOS/TalkBack/VoiceOver remain unverified external dependencies. These cannot be completed by relabeling code tests. Local classifier coverage remains limited; no external AI, new sensitive analytics, production activation, merge or deployment is introduced here.

## Final execution

Tested executable head: `8843a613a52ac5120c97b3eeddba6a6da75c25ca`. [GitHub Actions run 40640710127](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40640710127) completed successfully in all four jobs. Direct logs confirm **327 backend +69 logic +101 browser +27 real-stack =524 passing tests**, no final failures/skips. TypeScript, lint (zero errors;11 inherited warnings), Web/Android/iOS JavaScript/assets export and release-scans passed. Browser/full-stack evidence is from Chromium; it is not physical-device evidence.

Windows local backend verification also passed327 tests on disposable PostgreSQL18. The initial local69-test logic attempt was68pass/1failure due to stale J node_modules; lockfile installation corrected the local environment. The initial new Persian browser case selected two equivalent clear-search controls; its selector was corrected without removing the behavioral assertion. Final CI used a clean dependency install and passed every case. See the separate local outcome below if recorded; failed attempts are not included in the524 total.

No executable changes follow this tested head in the evidence-only documentation commit. PR12 remains stacked on L2; no merge or activation occurred.
