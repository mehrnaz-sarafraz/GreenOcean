# Phase G/H delivery and continuation report

Date: 2026-10-27. Repository: `mehrnaz-sarafraz/GreenOcean`.

## Baseline and scope

The continuation fetched the real remote G/H head `42ca3642e564c68fcf01ebae82de9ecc143dce62`, rather than reproducing the conversational progress log. E/F is `9086d3a86e611ad559b2d1362463ff9039f43526`. Existing [PR #6](https://github.com/mehrnaz-sarafraz/GreenOcean/pull/6) is stacked on `feature/phase-ef-account-access-security` (#5); main is not the implementation baseline. No merge or production deployment is part of this delivery.

The last remote commit already rechecks the post audience for bookmarked previews in account archives. Its existing audience test covers losing a follow. This continuation preserves that fix and adds direct API regressions for both block directions, revoked community membership despite an approved follow, hidden/under-review/deleted posts, and suspended authors. Tests check both the absence of the post ID/body from future exports and retention of the original bookmark. Restoring permission restores the preview; an anonymous saved preview does not acquire author attribution.

A clean full-suite run also reproduced a pre-existing test-order defect: historical Flyway tests execute V0 in a temporary schema, but V0 creates `public.uuidv7()`. When those tests run before application initialization, the public schema has a function but no Flyway history, so Spring refuses migration. The test-only `MigrationTestSupport` initializes normal application migration history first. A/D/E-F/G-H historical schema tests inherit it; their old target versions, populated fixtures and migration assertions remain intact. No applied migration, application authorization rule, production security limit or production database is changed.

## Requirement-by-requirement delivery

All G-01–G-12 and H-01–H-14 are evaluated individually in [requirements-matrix.md](requirements-matrix.md) and [requirements-matrix.json](requirements-matrix.json). Each entry includes initial status, reused code, changes/files, exact tests, executed-evidence references, database/privacy impact, final status and limits. The [initial gap matrix](gap-matrix.md) is retained as the historical audit, not rewritten to imply tests had already passed.

The software implementation is not a declaration of complete native/operational readiness. Applicable requirements remain `IMPLEMENTED_NOT_FULLY_TESTED` because real Android/iOS and assistive-technology checks are outstanding. H-14 remains `BLOCKED_EXTERNAL_DEPENDENCY`: no approved aggregate telemetry pipeline, retention design or lawful processing decision is provided. Topic collection is implemented behind the default-off gate; enabling it also requires external legal/product approval. These explicit limits follow the supplied specification rather than inventing approvals.

## Executed evidence

Actual commands, counts, failures/retries, platform exports and CI SHA boundaries are recorded in [test-evidence.json](test-evidence.json). Four-persona and per-requirement test links are in [test-coverage-matrix.md](test-coverage-matrix.md). These are the evidence sources for the matrix, rather than counts quoted in the earlier conversation.

The pre-continuation remote head has a successful [CI run 40475942112](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40475942112): backend, frontend and registration-live jobs all succeeded. This proves that remote commit's checks, not any unpublished continuation commit. New commits require their own CI result before the PR can be declared green at its new head.

Final executed local results:

| Check | Actual result |
|---|---|
| Backend, PostgreSQL integration/concurrency and historical migrations | 200 passed; 0 failures, 0 errors, 0 skipped; `clean verify` BUILD SUCCESS |
| Frontend logic/helpers | 59 passed; 0 failures, 0 skipped |
| G/H browser UI with API fixtures | 9 passed; 0 failures, 0 skipped |
| G/H real-server browser journeys | 4 passed; 0 failures, 0 skipped |
| TypeScript and ESLint | Passed |
| Expo web / Android / iOS JS and asset export | Passed; command exit code 0 |
| Physical native devices / TalkBack / VoiceOver | NOT RUN |

## Privacy validation

1. **Can a private profile expose biography or sensitive account data?** Nonfollowers get the documented minimal discoverable identity, including optional public avatar; private biography/location/year are redacted in profile and user search. Self and approved followers can receive permitted profile-detail fields. Email, eligibility evidence and private support preferences are excluded from social DTOs. Evidence: `privateFieldsAreRedactedInProfileAndSearchButVisibleToApprovedFollower` and the malicious live journey.
2. **Can private posts be retrieved through alternate APIs?** Feed, direct, search, author/community listing, notifications and archive saved-post previews use current authorization. Comments/likes/bookmarks authorize the parent first. Evidence: the audience matrix, community revocation, notification revocation, pagination and three saved-export regressions. Own authored content remains part of the owner's data-rights archive. This is not an assertion that previously downloaded copies disappear.
3. **Can making an account public reveal restricted historical posts?** V40 restricts legacy PUBLIC rows to FOLLOWERS without deleting them. Future explicit post audiences are independent of profile settings. Evidence: populated V39→V40 migration and repeated privacy-transition tests. The operational cost is narrower historical discoverability and a required owner-facing release notice.
4. **Can a blocked member bypass visibility or message restrictions?** Current blocks override ordinary post reads/previews and established-conversation listing/read/send/start. Both directions are tested for saved exports; direct-message block tests exercise existing conversation reuse. No claim is made about erasing previously delivered copies or authorized staff processes.
5. **Can another member retrieve private preferences?** Preferences are authenticated `/me` data; forged owner fields do not change the principal. Invalid styles/topics/progress and excess payloads are rejected without changing other accounts. Evidence: owner-only preferences and forged-profile tests, topic-gate tests and concurrent patches.
6. **Can anonymous posts leak attribution to ordinary members?** Author DTOs, author-profile listings/counts and anonymous reply notification actors suppress attribution. Saved previews contain no author field. Evidence: anonymous posts/counts, anonymous reply notifications and the anonymous bookmarked-post block test. Internal safety attribution is retained; timing/recognition attacks and previously shared identifying text are outside a guarantee of absolute anonymity.
7. **Do private preferences appear in logs or analytics?** The implemented G/H flow has no third-party trackers or general analytics events, and validation errors avoid echoing submitted values. Support choices are not clinical inference inputs. This conclusion is a source review, not an exhaustive production log/hosting audit. Owner progress persistence is not represented as anonymous measurement. H-14 stays disabled.
8. **Can onboarding be skipped without losing permitted access?** Yes: SKIPPED is saved and routes to the actual Support discovery page without a redirect loop. The legacy boolean is dismissal compatibility, while `onboardingStatus` distinguishes skipping/completion. Existing eligibility/service restrictions still apply. Evidence: API, helper, fixture-browser and real-server skip/resume journeys.
9. **Can saved preferences be revoked/deleted?** Empty topic arrays and empty support style explicitly clear; unmapped legacy labels have a clear control. Owner export includes progress and legacy values; password-confirmed deletion removes the preference row and invalidates sessions. Evidence: preference clear/progress tests, topic-gate/browser clear tests and account deletion test. External avatar copies and backups retain the documented limits.
10. **Does support style lead to an actual authorized destination?** Fixed styles resolve to feed, communities, Support or professional educational articles; missing/invalid style falls back to Support, and unavailable community eligibility routes to activation. No preference grants professional roles, creates a conversation, joins a group or promises immediate listener capacity. Evidence: helper allowlist/injection tests, professional preference browser regression and the articles real-server journey.

## Migration, compatibility and release gates

The only G/H runtime migration is [V40](../../backend/src/main/resources/db/migration/V40__durable_audience_and_progressive_onboarding.sql). This continuation adds no migration. V40 narrows historical PUBLIC content, adds explicit progress and private legacy-topic storage, and creates a shared read predicate. Old `onboardingComplete=true` is migrated to LEGACY_UNKNOWN, not fabricated completion. Existing intentional profile choices, social/account/session data and professional applications remain intact. Old clients cannot publish broad posts without per-post confirmation. Rollout and forward recovery are specified in [operations.md](operations.md); never roll back to the audience-widening predicate or edit an applied migration.

Native checks are all explicitly NOT RUN in [native-validation.md](native-validation.md). Exporting web/Android/iOS JavaScript is not a device launch. Production-volume migration timing, retention/backups, real mail/provider operations, staff assurance and legal review remain external release gates. The [privacy contract](../product/profile-and-content-privacy-contract.md), [onboarding contract](../product/progressive-onboarding-contract.md), [API notes](api.md) and [threat model](../security/phase-gh-threat-model.md) remain authoritative.

Phase I must reuse authorized real support routes and apply the shared audience/block/moderation checks before any ranking. No clinical prediction or personalization ranking is delivered here. Topics do not currently personalize feed/search, and the UI must not claim otherwise.

## Continuation commits and publication

Branch: `feature/phase-gh-profile-privacy-onboarding`. Source changes are in `e112bbc` (saved-export revocation regressions) and `e16cfa9` (migration initialization, isolated cleanup fixtures and exact-second DST-safe boundaries). The documentation/evidence commit containing this report completes the local continuation.

GitHub installation was confirmed after the user enabled it. However, the current operation registry still exposes no GitHub repository/credential/PR-write tools, and HTTPS git has no credential helper or account credential; its push dry-run failed with `could not read Username`. Local commits are therefore **not published** and PR #6 is **not updated by this continuation**. Its earlier green CI must not be attributed to these commits. A prepared PR description and an applyable continuation patch accompany the local handoff. This is an integration availability limitation, not a merge approval request.

## Reviewer checklist

- Review the 26 individual matrix entries and their evidence limits.
- Verify the new PR head's CI rather than attributing an older successful run to it.
- Confirm saved-preview revocation across follow/community/block/moderation boundaries.
- Confirm clean-database and populated-history migrations with the ordering fix.
- Review historical PUBLIC narrowing, owner release notice and forward recovery.
- Keep topic collection and telemetry disabled pending their external decisions.
- Complete native device/assistive-technology checks before native release.
- Do not merge the stacked PR out of order or deploy from this task.

The cleanup boundary failure was traced to daylight-saving time: in the local Europe/Paris database timezone on 2026-10-27, calendar `INTERVAL '7 days'` spans the autumn offset change, while the service correctly uses `Duration.ofDays(7)` (604800 seconds). The fixture now uses exact-second intervals and asserts the elapsed seconds at and one second before the cutoff. Production retention behavior is unchanged. The cleanup class also uses a unique temporary schema, removed after the class, so its global candidate/erasure scan cannot affect other suites' fixtures. Schema isolation alone did not resolve the DST failure. The restricted local runtime starts Mockito as a Java agent; that is not a production JVM option.
