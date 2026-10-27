# G/H requirement delivery matrix

Baseline E/F `9086d3a`; resumed existing G/H `42ca364`. This matrix distinguishes implemented software from full release readiness. Every feature remains `IMPLEMENTED_NOT_FULLY_TESTED` until applicable native/manual and operational evidence exists; H-14 is externally blocked. Passing browser helpers does not imply device execution. See [test evidence](test-evidence.json), [test coverage](test-coverage-matrix.md), [operations](operations.md), and [initial gap matrix](gap-matrix.md).

## G-01

- Initial status: `PARTIALLY_IMPLEMENTED`.
- Reused: `auth/service/RegistrationService.java; profile/entity/Profile.java`.
- Change: Preserved transactional minimal private-profile initialization; added retry, concurrency and rollback coverage.
- Changed files: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`; `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java` — `failedRegistrationTransactionLeavesNoPartialProfileEligibilityOrEmail`; `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java` — `simultaneousDistinctRegistrationsKeepUniqueUsernamesAndAllowDuplicateDisplayNames`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: No schema change to registration.
- Privacy/security: No real name or health disclosure.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Registration-level rollback is exercised with a synthetic transaction failure; production fault injection was not performed.

## G-02

- Initial status: `IMPLEMENTED_NEEDS_TESTS`.
- Reused: `auth/service/RegistrationService.java`.
- Change: Preserved random non-email usernames and duplicate display names; verified concurrent identities.
- Changed files: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`; `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java` — `simultaneousDistinctRegistrationsKeepUniqueUsernamesAndAllowDuplicateDisplayNames`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: Existing unique username index retained.
- Privacy/security: Email and sequential IDs are not public identity generators.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Random collision handling reuses registration retry/unique constraints; exhaustive collision injection is not covered by G/H tests.

## G-03

- Initial status: `PARTIALLY_IMPLEMENTED`.
- Reused: `profile/service/ProfileService.java`.
- Change: Added reserved-name and unsafe-input validation, safe Unicode presentation and owner update locking; retained account UUID relationships.
- Changed files: `backend/src/main/java/com/greenocean/backend/profile/service/ProfileService.java`; `backend/src/main/java/com/greenocean/backend/profile/repository/ProfileRepository.java`; `backend/src/main/java/com/greenocean/backend/profile/dto/UpdateProfileRequest.java`; `frontend/src/app/(app)/tools/[tool].tsx`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `persianNamesDuplicateDisplayAndAvatarClearPreserveAccountIdentity`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `rejectsReservedTakenUnsafeIdentityAndAvatarWithoutPersisting`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Creative GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: Existing unique index, no new schema.
- Privacy/security: Forged owner fields cannot edit a different user.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Native keyboard, scaling and screen-reader validation remains unexecuted.

## G-04

- Initial status: `DEFECTIVE`.
- Reused: `profile/entity/Profile.java; tools/[tool].tsx`.
- Change: Added optional HTTPS avatar reference editing and explicit removal; reject local paths, unsafe protocols and credential-bearing URLs.
- Changed files: `backend/src/main/java/com/greenocean/backend/profile/service/ProfileService.java`; `frontend/src/app/(app)/tools/[tool].tsx`; `backend/src/main/java/com/greenocean/backend/profile/dto/UpdateProfileRequest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `persianNamesDuplicateDisplayAndAvatarClearPreserveAccountIdentity`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `rejectsReservedTakenUnsafeIdentityAndAvatarWithoutPersisting`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Creative GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: Null existing avatar_url on explicit empty-string clear.
- Privacy/security: Avatar is a discoverable identity field, not private media.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: External image hosts and previously received copies cannot be erased or stripped of metadata by this reference-only architecture; native validation pending.

## G-05

- Initial status: `PARTIALLY_IMPLEMENTED`.
- Reused: `profile/entity/Profile.java; tools/[tool].tsx`.
- Change: Preserved private DB/entity defaults and intentional historical profile choices; removed unsafe public/loading fallbacks.
- Changed files: `frontend/src/app/(app)/tools/[tool].tsx`; `backend/src/main/java/com/greenocean/backend/profile/service/ProfileService.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHMigrationTest.java`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`; `backend/src/test/java/com/greenocean/backend/PhaseGHMigrationTest.java` — `populatedEfDatabasePreservesChoicesAndConservativelyFreezesHistoricalAudiences`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Creative GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: V40 leaves existing profile choices intact.
- Privacy/security: A loading preference is not treated as permission to disclose.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Native loading behavior remains a device release gate.

## G-06

- Initial status: `DEFECTIVE`.
- Reused: `profile/service/ProfileService.java`.
- Change: Applied profile-detail visibility to optional public location; service eligibility country stays independent.
- Changed files: `backend/src/main/java/com/greenocean/backend/profile/service/ProfileService.java`; `backend/src/main/java/com/greenocean/backend/search/service/SearchService.java`; `frontend/src/app/(app)/tools/[tool].tsx`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `privateFieldsAreRedactedInProfileAndSearchButVisibleToApprovedFollower`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `persianNamesDuplicateDisplayAndAvatarClearPreserveAccountIdentity`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: No eligibility or location data rewrite.
- Privacy/security: Private-profile nonfollowers do not receive location; edits do not change residence eligibility.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: A deliberately enabled social location can be disclosed to the documented allowed audience.

## G-07

- Initial status: `IMPLEMENTED_NEEDS_TESTS`.
- Reused: `profile/service/ProfileService.java`.
- Change: Kept age assurance out of social DTOs and new-user prompts; tested explicit legacy-year visibility and redaction.
- Changed files: `backend/src/main/java/com/greenocean/backend/profile/service/ProfileService.java`; `frontend/src/app/(app)/tools/[tool].tsx`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `privateFieldsAreRedactedInProfileAndSearchButVisibleToApprovedFollower`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: Legacy birth-year values preserved.
- Privacy/security: No public age is inferred from account eligibility.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Legacy explicit year display remains supported under the documented audience contract.

## G-08

- Initial status: `DEFECTIVE`.
- Reused: `messaging/repository/MessagingRepository.java; safety-settings.tsx`.
- Change: Corrected safe UI fallback and visible save rollback; enforced blocks for established conversations and verified-professional request rules.
- Changed files: `backend/src/main/java/com/greenocean/backend/messaging/service/MessagingService.java`; `backend/src/main/java/com/greenocean/backend/messaging/repository/MessagingRepository.java`; `frontend/src/app/(app)/safety-settings.tsx`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java`; `frontend/e2e/phase-gh.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `messageRequestsFollowApprovalAndEstablishedBlockAreEnforced`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `enabledRequestsPermitContactAndDeletionErasesPreferencesProgressAndAvatar`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `professionalOnlyRequestsRequireVerifiedSenderAndRemainSeparateFromCare`; `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java` — `concurrentIndependentPreferencePatchesPreserveBothConfirmedChanges`; `frontend/e2e/phase-gh.spec.mjs` — `Confused GH UI: privacy saving failure`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: Existing preferences/conversations reused.
- Privacy/security: Unsolicited requests and established consent are separate; blocks override both.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Physical-device optimistic rollback and assistive technology remain unexecuted.

## G-09

- Initial status: `PARTIALLY_IMPLEMENTED`.
- Reused: `features/content/post-card.tsx; post/repository/PostReadRepository.java`.
- Change: Separated blur from discovery reduction, connected muted terms and medication filtering, and retained mandatory moderation independently.
- Changed files: `backend/src/main/java/com/greenocean/backend/post/repository/PostReadRepository.java`; `frontend/src/features/content/post-card.tsx`; `frontend/src/app/(app)/safety-settings.tsx`; `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/app/(app)/post/[id].tsx`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `frontend/e2e/phase-gh.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `contentControlsFilterDiscoveryIndependentlyOfAuthorizedDirectLinks`; `frontend/e2e/phase-gh.spec.mjs` — `Confused GH UI: privacy saving failure`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: No moderation policy/schema weakened.
- Privacy/security: Blur is presentation; accessible direct links still disclose permitted body to the client.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Medication reduction is a literal keyword/category filter, not semantic or clinical detection; native rendering pending.

## G-10

- Initial status: `DEFECTIVE`.
- Reused: `profile/service/ProfileService.java; search/service/SearchService.java`.
- Change: Redacted private biography/location/year/counts across profile/search; removed anonymous-author listing/count and notification attribution.
- Changed files: `backend/src/main/java/com/greenocean/backend/profile/service/ProfileService.java`; `backend/src/main/java/com/greenocean/backend/search/service/SearchService.java`; `backend/src/main/java/com/greenocean/backend/post/repository/PostReadRepository.java`; `backend/src/main/java/com/greenocean/backend/notification/repository/NotificationRepository.java`; `backend/src/main/java/com/greenocean/backend/notification/service/NotificationService.java`; `backend/src/main/java/com/greenocean/backend/catalog/repository/CatalogRepository.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `privateFieldsAreRedactedInProfileAndSearchButVisibleToApprovedFollower`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `anonymousPostsDoNotAppearInOtherMembersAuthorListingsOrCounts`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `revokedAudienceNotificationsAndAnonymousRepliesDoNotDiscloseProtectedInformation`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Malicious GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: V40 shared read predicate; existing profile fields preserved.
- Privacy/security: Minimal discoverability is retained; private preferences never enter social DTOs.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Avatar/username/display name remain discoverable to eligible members; authorized platform processes retain internal attribution.

## G-11

- Initial status: `DEFECTIVE`.
- Reused: `post/repository/PostReadRepository.java; social/service/SocialInteractionRepository.java; create.tsx`.
- Change: Implemented durable server-enforced per-post audience, restrictive composer default and explicit broad confirmation; froze legacy PUBLIC conservatively.
- Changed files: `backend/src/main/java/com/greenocean/backend/post/repository/PostReadRepository.java`; `backend/src/main/java/com/greenocean/backend/post/service/PostService.java`; `backend/src/main/java/com/greenocean/backend/post/dto/CreatePostRequest.java`; `backend/src/main/java/com/greenocean/backend/social/service/SocialInteractionRepository.java`; `backend/src/main/resources/db/migration/V40__durable_audience_and_progressive_onboarding.sql`; `frontend/src/app/(app)/create.tsx`; `backend/src/main/java/com/greenocean/backend/account/repository/AccountRepository.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHMigrationTest.java`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `audienceMatrixSurvivesPrivacyTransitionsAndAlternateApiRequests`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `communityMembershipCannotBeReplacedByFollowAndLeavingRevokesAccess`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `broaderAudienceRequiresExplicitPerPostConfirmation`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `paginatedDiscoveryNeverReturnsFollowersOnlyPostsToStrangers`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `savedExportPreviewsRespectBlocksInBothDirectionsWithoutErasingBookmarks`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `savedExportPreviewsDisappearAfterCommunityMembershipIsRevoked`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `savedExportPreviewsRespectModerationAndInactiveAuthors`; `backend/src/test/java/com/greenocean/backend/PhaseGHMigrationTest.java` — `populatedEfDatabasePreservesChoicesAndConservativelyFreezesHistoricalAudiences`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Malicious GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: V40: historical PUBLIC → FOLLOWERS, shared authorization function.
- Privacy/security: No privacy toggle broadens historical content; follow cannot grant community rights.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Native audience controls and production-scale migration locking remain untested; copies already downloaded cannot be recalled.

## G-12

- Initial status: `IMPLEMENTED_NEEDS_TESTS`.
- Reused: `account/service/AccountService.java; account/repository/AccountRepository.java`.
- Change: Reused owner archive and password-confirmed deletion; exported new progress/preferences, erased their row and avatar reference, and reauthorized bookmark previews at export time. Isolated lifecycle regression fixtures and corrected duration-boundary test arithmetic across DST; production erasure/retention remains unchanged.
- Changed files: `backend/src/main/java/com/greenocean/backend/account/repository/AccountRepository.java`; `backend/src/main/java/com/greenocean/backend/preference/repository/UserPreferencesRepository.java`; `backend/src/main/java/com/greenocean/backend/preference/dto/UserPreferencesResponse.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `backend/src/test/java/com/greenocean/backend/MigrationTestSupport.java`; `backend/src/test/java/com/greenocean/backend/PhaseDPendingCleanupTest.java`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `enabledRequestsPermitContactAndDeletionErasesPreferencesProgressAndAvatar`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `audienceMatrixSurvivesPrivacyTransitionsAndAlternateApiRequests`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `savedExportPreviewsRespectBlocksInBothDirectionsWithoutErasingBookmarks`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `savedExportPreviewsDisappearAfterCommunityMembershipIsRevoked`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `savedExportPreviewsRespectModerationAndInactiveAuthors`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: V40 fields are in existing owner preferences row and existing erasure scope.
- Privacy/security: Bookmarks never grant permanent access to another member’s content.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Own authored posts/comments remain owner data-rights records; external avatar hosts, backups and received copies follow documented retention limits.


## H-01

- Initial status: `DEFECTIVE`.
- Reused: `preference/repository/UserPreferencesRepository.java; onboarding.tsx`.
- Change: Validated stable active catalog slugs and exact legacy mapping; optional topic collection is off by default, with clear/export/delete supported.
- Changed files: `backend/src/main/java/com/greenocean/backend/preference/repository/UserPreferencesRepository.java`; `backend/src/main/java/com/greenocean/backend/preference/dto/UpdateUserPreferencesRequest.java`; `frontend/src/app/(auth)/onboarding.tsx`; `backend/src/main/resources/db/migration/V40__durable_audience_and_progressive_onboarding.sql`; `backend/src/test/java/com/greenocean/backend/PhaseGHTopicGateTest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHMigrationTest.java`; `frontend/e2e/phase-gh.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHTopicGateTest.java` — `reviewedTopicGateUsesStableActiveSlugsAndAllowsExplicitClear`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `ownerOnlyPreferencesRejectForgedTopicsStylesProgressAndLargePayloads`; `backend/src/test/java/com/greenocean/backend/PhaseGHMigrationTest.java` — `populatedEfDatabasePreservesChoicesAndConservativelyFreezesHistoricalAudiences`; `frontend/e2e/phase-gh.spec.mjs` — `Creative GH UI: topic checkbox`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: V40 retains unmapped historical values owner-only in legacy_support_topics.
- Privacy/security: No diagnosis or recommendation inference; no new sensitive collection without external approval.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Enabling sensitive collection is BLOCKED_EXTERNAL_DEPENDENCY pending legal/product basis, notice and retention decisions; synthetic gate-on tests do not authorize rollout.

## H-02

- Initial status: `PARTIALLY_IMPLEMENTED`.
- Reused: `preference/dto/UpdateUserPreferencesRequest.java`.
- Change: Validated current support-style allowlist/catalog, explicit empty clear and safe destination mapping; topics remain optional.
- Changed files: `backend/src/main/java/com/greenocean/backend/preference/dto/UpdateUserPreferencesRequest.java`; `backend/src/main/java/com/greenocean/backend/preference/repository/UserPreferencesRepository.java`; `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/features/platform/onboarding-policy.ts`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `frontend/tests/phase-gh.test.mjs`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `ownerOnlyPreferencesRejectForgedTopicsStylesProgressAndLargePayloads`; `frontend/tests/phase-gh.test.mjs` — `Happy GH: all support styles`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Happy GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: Existing support_style reused.
- Privacy/security: No clinical inference or implicit conversation/group creation.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Native selection and text scaling remain unexecuted.

## H-03

- Initial status: `DEFECTIVE`.
- Reused: `onboarding.tsx`.
- Change: Explained conservative defaults and exposed independent saved safety controls with honest failure feedback.
- Changed files: `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/app/(app)/safety-settings.tsx`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java`; `frontend/e2e/phase-gh.spec.mjs`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`; `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java` — `concurrentIndependentPreferencePatchesPreserveBothConfirmedChanges`; `frontend/e2e/phase-gh.spec.mjs` — `Confused GH UI: privacy saving failure`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Happy GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: Existing independent preferences retained.
- Privacy/security: Disabling personal blur cannot turn off moderation/authorization.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Native toggles, focus and screen readers remain unexecuted.

## H-04

- Initial status: `DEFECTIVE`.
- Reused: `preference/repository/UserPreferencesRepository.java`.
- Change: Serialized partial preference updates under an owner row lock; validated payloads and distinguished omission/null from explicit clear.
- Changed files: `backend/src/main/java/com/greenocean/backend/preference/repository/UserPreferencesRepository.java`; `backend/src/main/java/com/greenocean/backend/preference/dto/UpdateUserPreferencesRequest.java`; `backend/src/main/java/com/greenocean/backend/preference/dto/UserPreferencesResponse.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `backend/src/test/java/com/greenocean/backend/PhaseGHTopicGateTest.java`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHConcurrencyTest.java` — `concurrentIndependentPreferencePatchesPreserveBothConfirmedChanges`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `ownerOnlyPreferencesRejectForgedTopicsStylesProgressAndLargePayloads`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `legacyBooleanDismissalDoesNotFabricateCompletionAndNullDoesNotClear`; `backend/src/test/java/com/greenocean/backend/PhaseGHTopicGateTest.java` — `reviewedTopicGateUsesStableActiveSlugsAndAllowsExplicitClear`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: V40 progress/legacy fields; existing owner primary key.
- Privacy/security: No cross-user preference update or disclosure; unrelated concurrent patches survive.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Simultaneous edits to the same field use last committed write; no cross-device conflict-resolution UI.

## H-05

- Initial status: `DEFECTIVE`.
- Reused: `onboarding.tsx; (app)/_layout.tsx`.
- Change: Persisted explicit SKIPPED versus COMPLETED versus legacy dismissal and used explicit state for navigation.
- Changed files: `backend/src/main/java/com/greenocean/backend/preference/repository/UserPreferencesRepository.java`; `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/features/platform/onboarding-policy.ts`; `frontend/src/app/(app)/_layout.tsx`; `backend/src/main/resources/db/migration/V40__durable_audience_and_progressive_onboarding.sql`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `frontend/tests/phase-gh.test.mjs`; `frontend/e2e/phase-gh.spec.mjs`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `legacyBooleanDismissalDoesNotFabricateCompletionAndNullDoesNotClear`; `frontend/tests/phase-gh.test.mjs` — `Confused GH: skip and legacy unknown`; `frontend/e2e/phase-gh.spec.mjs` — `Confused GH UI: Skip`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Confused GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: V40 maps old true to LEGACY_UNKNOWN; boolean retained as dismissal compatibility.
- Privacy/security: Skip is not fabricated completion; ordinary access is not disclosure-dependent.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Legacy boolean remains true for any dismissed flow; clients must use onboardingStatus to distinguish outcomes; native pending.

## H-06

- Initial status: `MISSING`.
- Reused: `onboarding.tsx`.
- Change: Persisted the last confirmed step, restored progress and retained recoverable save failures.
- Changed files: `backend/src/main/java/com/greenocean/backend/preference/repository/UserPreferencesRepository.java`; `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/features/platform/onboarding-policy.ts`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `frontend/tests/phase-gh.test.mjs`; `frontend/e2e/phase-gh.spec.mjs`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`; `frontend/tests/phase-gh.test.mjs` — `Creative GH: durable confirmed progress`; `frontend/e2e/phase-gh.spec.mjs` — `Creative GH UI: confirmed step resumes`; `frontend/e2e/phase-gh.spec.mjs` — `Confused GH UI: failed save`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Confused GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: V40 onboarding_step 0–3 with constraint.
- Privacy/security: Only confirmed owner-scoped progress is restored.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Physical force-stop and real multi-device sessions remain unexecuted.

## H-07

- Initial status: `PARTIALLY_IMPLEMENTED`.
- Reused: `profile.tsx; safety-settings.tsx`.
- Change: Added optional-preference reentry from profile/settings with explicit clear controls and no forced re-enrollment.
- Changed files: `frontend/src/app/(app)/profile.tsx`; `frontend/src/app/(app)/safety-settings.tsx`; `frontend/src/app/(auth)/onboarding.tsx`; `frontend/e2e/phase-gh.spec.mjs`; `frontend/live-e2e/phase-gh-journey.spec.mjs`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`.
- Tests: `frontend/e2e/phase-gh.spec.mjs` — `Creative GH UI: topic checkbox`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Confused GH live`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: Existing preference row remains editable.
- Privacy/security: A user can revoke choices without account deletion.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Native reentry and real multi-device conflict UX remain unexecuted.

## H-08

- Initial status: `IMPLEMENTED_NEEDS_TESTS`.
- Reused: `auth/service/RegistrationService.java; onboarding.tsx`.
- Change: Preserved minimal registration and zero-topic/zero-health-input onboarding and skip routes.
- Changed files: `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/features/platform/onboarding-policy.ts`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `frontend/e2e/phase-gh.spec.mjs`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`; `frontend/e2e/phase-gh.spec.mjs` — `Happy GH UI: optional zero-input`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Happy GH live`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Confused GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: No new clinical/account identity fields.
- Privacy/security: No health disclosure, diagnosis or disease label required.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Native zero-input journey remains unexecuted.

## H-09

- Initial status: `PARTIALLY_IMPLEMENTED`.
- Reused: `onboarding.tsx`.
- Change: Allowed no selections and mapped missing/invalid styles to a neutral real Support destination.
- Changed files: `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/features/platform/onboarding-policy.ts`; `frontend/tests/phase-gh.test.mjs`; `frontend/e2e/phase-gh.spec.mjs`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `frontend/tests/phase-gh.test.mjs` — `Malicious GH: unknown styles`; `frontend/e2e/phase-gh.spec.mjs` — `Happy GH UI: optional zero-input`; `frontend/e2e/phase-gh.spec.mjs` — `Confused GH UI: Skip`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Happy GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: Empty arrays persist without inferred defaults.
- Privacy/security: No interest is inferred from absence or redirect manipulation.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Fallback is discovery, not a promise of available listeners; native pending.

## H-10

- Initial status: `PARTIALLY_IMPLEMENTED`.
- Reused: `support.tsx; onboarding.tsx`.
- Change: Clarified listener versus professional versus emergency boundaries and actual service availability; never starts interactions automatically.
- Changed files: `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/app/(app)/support.tsx`; `frontend/tests/phase-gh.test.mjs`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `frontend/tests/phase-gh.test.mjs` — `Happy GH: all support styles`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Happy GH live`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Confused GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: No new care, booking or messaging schema.
- Privacy/security: First-support navigation remains subject to existing service/eligibility rules.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Service capacity and clinical/legal operations are external; native and comprehensive copy comprehension study not performed.

## H-11

- Initial status: `DEFECTIVE`.
- Reused: `onboarding.tsx`.
- Change: Replaced unsupported personalization claims with actual purpose, storage/audience and editing/deletion explanations.
- Changed files: `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/app/(app)/index.tsx`; `frontend/src/app/(app)/support.tsx`; `frontend/e2e/phase-gh.spec.mjs`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `frontend/e2e/phase-gh.spec.mjs` — `Creative GH UI: Persian RTL`; `frontend/e2e/phase-gh.spec.mjs` — `Creative GH UI: topic checkbox`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Happy GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: No analytics or ranking database added.
- Privacy/security: Preferences do not form a clinical profile or rank the existing feed.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Code/copy review and browser journeys are not a formal user comprehension or privacy certification study.

## H-12

- Initial status: `DEFECTIVE`.
- Reused: `onboarding.tsx`.
- Change: Replaced obsolete role routing with fixed actual support destinations and eligibility fallback.
- Changed files: `frontend/src/features/platform/onboarding-policy.ts`; `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/app/(app)/support.tsx`; `frontend/tests/phase-gh.test.mjs`; `frontend/e2e/phase-gh.spec.mjs`; `frontend/live-e2e/phase-gh-journey.spec.mjs`.
- Tests: `frontend/tests/phase-gh.test.mjs` — `Happy GH: all support styles`; `frontend/tests/phase-gh.test.mjs` — `Malicious GH: unknown styles`; `frontend/e2e/phase-gh.spec.mjs` — `Creative GH UI: confirmed step resumes`; `frontend/live-e2e/phase-gh-journey.spec.mjs` — `Happy GH live`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: No conversations or memberships created by onboarding.
- Privacy/security: No open redirects or authorization grants from preferences.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Matching/booking remains Phase I; native destinations remain unexecuted.

## H-13

- Initial status: `PARTIALLY_IMPLEMENTED`.
- Reused: `profile.tsx; onboarding.tsx`.
- Change: Separated professional application from ordinary onboarding; inert intendedRole compatibility cannot elevate roles or steer completion into verification.
- Changed files: `frontend/src/app/(app)/profile.tsx`; `frontend/src/app/(auth)/onboarding.tsx`; `frontend/src/features/platform/onboarding-policy.ts`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java`; `frontend/e2e/phase-gh.spec.mjs`.
- Tests: `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `ownerOnlyPreferencesRejectForgedTopicsStylesProgressAndLargePayloads`; `backend/src/test/java/com/greenocean/backend/PhaseGHPrivacyIntegrationTest.java` — `professionalOnlyRequestsRequireVerifiedSenderAndRemainSeparateFromCare`; `frontend/e2e/phase-gh.spec.mjs` — `Malicious GH UI: legacy professional preference`.
- Actual results: Final suite: 200 backend, 59 helper, 9 fixture-browser and 4 real-server G/H tests passed. Exact cases/layers: `test-evidence.json` and `test-coverage-matrix.md`. Native checks remain NOT RUN.
- Database: Existing role/application/verification data preserved.
- Privacy/security: Professional verification and care authorization remain server-enforced.
- Final status: `IMPLEMENTED_NOT_FULLY_TESTED`.
- Remaining limits: Full native professional application journey is outside this G/H workstream.

## H-14

- Initial status: `BLOCKED_EXTERNAL_DEPENDENCY`.
- Reused: `preference/repository/UserPreferencesRepository.java`.
- Change: Kept onboarding analytics disabled; documented lawful purpose, aggregation, retention and consent prerequisites.
- Changed files: `docs/phase-gh/operations.md`; `docs/product/progressive-onboarding-contract.md`; `docs/security/phase-gh-threat-model.md`.
- Tests: No analytics execution; implementation/policy inspection only..
- Actual results: Source/policy inspection only; no analytics runtime implemented or tested.
- Database: No telemetry schema, trackers or events introduced.
- Privacy/security: No topic payloads or clinical inference in general analytics; owner resume progress is not anonymous analytics.
- Final status: `BLOCKED_EXTERNAL_DEPENDENCY`.
- Remaining limits: BLOCKED_EXTERNAL_DEPENDENCY: approved analytics architecture and lawful processing/retention decisions are absent.
