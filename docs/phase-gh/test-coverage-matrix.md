# G/H test and four-persona traceability

Method references below are in `backend/src/test/java/com/greenocean/backend`; UI references are in `frontend`. Counts and executed outcomes are recorded separately in [test-evidence.json](test-evidence.json). All records are synthetic. Browser fixture tests are explicitly distinct from the real-server suite.

| Persona | Requirements | Implemented scenarios and tests | Layer |
|---|---|---|---|
| Happy | G-01/G-02/G-05/G-12; H-02/H-03/H-04/H-05/H-08/H-09/H-12 | `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`; `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`; `enabledRequestsPermitContactAndDeletionErasesPreferencesProgressAndAvatar`; Happy GH UI/live completion | API + browser fixtures + real server |
| Creative | G-01/G-02/G-03/G-04/G-06/G-11; H-01/H-04/H-06/H-07 | `PhaseGHConcurrencyTest`; `PhaseGHMigrationTest`; `PhaseGHTopicGateTest`; `persianNamesDuplicateDisplayAndAvatarClearPreserveAccountIdentity`; RTL at 360/1280; stable checkbox slug/clear; resumed steps; Persian live edit | PostgreSQL + migration + browser fixtures + real server |
| Confused | G-08/G-09; H-03/H-05/H-06/H-07/H-08/H-09 | failed registration rollback; legacy boolean dismissal/null preservation; content discovery/direct-link distinction; failed privacy save rollback; failed onboarding save/retry; skip/reload/reentry live journey | API + concurrency + helper + browser fixtures + real server |
| Malicious | G-03/G-04/G-08/G-10/G-11/G-12; H-01/H-02/H-04/H-12/H-13 | rejected reserved/taken/unsafe/forged owner fields; audience transition matrix; community/follow separation; anonymous author/list/notification redaction; disabled and professional-only messaging; private preferences bounds/isolation; route injection; three saved-export revocation regressions | PostgreSQL API + helper + browser fixtures + real server |

## Per-requirement links

| ID | Tests (exact API method or browser title prefix) |
|---|---|
| G-01 | `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`<br>`failedRegistrationTransactionLeavesNoPartialProfileEligibilityOrEmail`<br>`simultaneousDistinctRegistrationsKeepUniqueUsernamesAndAllowDuplicateDisplayNames` |
| G-02 | `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`<br>`simultaneousDistinctRegistrationsKeepUniqueUsernamesAndAllowDuplicateDisplayNames` |
| G-03 | `persianNamesDuplicateDisplayAndAvatarClearPreserveAccountIdentity`<br>`rejectsReservedTakenUnsafeIdentityAndAvatarWithoutPersisting`<br>`Creative GH live` |
| G-04 | `persianNamesDuplicateDisplayAndAvatarClearPreserveAccountIdentity`<br>`rejectsReservedTakenUnsafeIdentityAndAvatarWithoutPersisting`<br>`Creative GH live` |
| G-05 | `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`<br>`populatedEfDatabasePreservesChoicesAndConservativelyFreezesHistoricalAudiences`<br>`Creative GH live` |
| G-06 | `privateFieldsAreRedactedInProfileAndSearchButVisibleToApprovedFollower`<br>`persianNamesDuplicateDisplayAndAvatarClearPreserveAccountIdentity` |
| G-07 | `privateFieldsAreRedactedInProfileAndSearchButVisibleToApprovedFollower`<br>`minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity` |
| G-08 | `messageRequestsFollowApprovalAndEstablishedBlockAreEnforced`<br>`enabledRequestsPermitContactAndDeletionErasesPreferencesProgressAndAvatar`<br>`professionalOnlyRequestsRequireVerifiedSenderAndRemainSeparateFromCare`<br>`concurrentIndependentPreferencePatchesPreserveBothConfirmedChanges`<br>`Confused GH UI: privacy saving failure` |
| G-09 | `contentControlsFilterDiscoveryIndependentlyOfAuthorizedDirectLinks`<br>`Confused GH UI: privacy saving failure` |
| G-10 | `privateFieldsAreRedactedInProfileAndSearchButVisibleToApprovedFollower`<br>`anonymousPostsDoNotAppearInOtherMembersAuthorListingsOrCounts`<br>`revokedAudienceNotificationsAndAnonymousRepliesDoNotDiscloseProtectedInformation`<br>`Malicious GH live` |
| G-11 | `audienceMatrixSurvivesPrivacyTransitionsAndAlternateApiRequests`<br>`communityMembershipCannotBeReplacedByFollowAndLeavingRevokesAccess`<br>`broaderAudienceRequiresExplicitPerPostConfirmation`<br>`paginatedDiscoveryNeverReturnsFollowersOnlyPostsToStrangers`<br>`savedExportPreviewsRespectBlocksInBothDirectionsWithoutErasingBookmarks`<br>`savedExportPreviewsDisappearAfterCommunityMembershipIsRevoked`<br>`savedExportPreviewsRespectModerationAndInactiveAuthors`<br>`populatedEfDatabasePreservesChoicesAndConservativelyFreezesHistoricalAudiences`<br>`Malicious GH live` |
| G-12 | `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`<br>`enabledRequestsPermitContactAndDeletionErasesPreferencesProgressAndAvatar`<br>`audienceMatrixSurvivesPrivacyTransitionsAndAlternateApiRequests`<br>`savedExportPreviewsRespectBlocksInBothDirectionsWithoutErasingBookmarks`<br>`savedExportPreviewsDisappearAfterCommunityMembershipIsRevoked`<br>`savedExportPreviewsRespectModerationAndInactiveAuthors` |
| H-01 | `reviewedTopicGateUsesStableActiveSlugsAndAllowsExplicitClear`<br>`ownerOnlyPreferencesRejectForgedTopicsStylesProgressAndLargePayloads`<br>`populatedEfDatabasePreservesChoicesAndConservativelyFreezesHistoricalAudiences`<br>`Creative GH UI: topic checkbox` |
| H-02 | `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`<br>`ownerOnlyPreferencesRejectForgedTopicsStylesProgressAndLargePayloads`<br>`Happy GH: all support styles`<br>`Happy GH live` |
| H-03 | `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`<br>`concurrentIndependentPreferencePatchesPreserveBothConfirmedChanges`<br>`Confused GH UI: privacy saving failure`<br>`Happy GH live` |
| H-04 | `concurrentIndependentPreferencePatchesPreserveBothConfirmedChanges`<br>`ownerOnlyPreferencesRejectForgedTopicsStylesProgressAndLargePayloads`<br>`legacyBooleanDismissalDoesNotFabricateCompletionAndNullDoesNotClear`<br>`reviewedTopicGateUsesStableActiveSlugsAndAllowsExplicitClear` |
| H-05 | `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`<br>`legacyBooleanDismissalDoesNotFabricateCompletionAndNullDoesNotClear`<br>`Confused GH: skip and legacy unknown`<br>`Confused GH UI: Skip`<br>`Confused GH live` |
| H-06 | `onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`<br>`Creative GH: durable confirmed progress`<br>`Creative GH UI: confirmed step resumes`<br>`Confused GH UI: failed save`<br>`Confused GH live` |
| H-07 | `Creative GH UI: topic checkbox`<br>`Confused GH live`<br>`onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately` |
| H-08 | `minimalRegistrationHasOnePrivateProfileAndNonEmailIdentity`<br>`onboardingProgressSkipCompleteAndEmptyClearArePersistedSeparately`<br>`Happy GH UI: optional zero-input`<br>`Happy GH live`<br>`Confused GH live` |
| H-09 | `Malicious GH: unknown styles`<br>`Happy GH UI: optional zero-input`<br>`Confused GH UI: Skip`<br>`Happy GH live` |
| H-10 | `Happy GH: all support styles`<br>`Happy GH live`<br>`Confused GH live` |
| H-11 | `Creative GH UI: Persian RTL`<br>`Creative GH UI: topic checkbox`<br>`Happy GH live` |
| H-12 | `Happy GH: all support styles`<br>`Malicious GH: unknown styles`<br>`Creative GH UI: confirmed step resumes`<br>`Happy GH live` |
| H-13 | `ownerOnlyPreferencesRejectForgedTopicsStylesProgressAndLargePayloads`<br>`professionalOnlyRequestsRequireVerifiedSenderAndRemainSeparateFromCare`<br>`Malicious GH UI: legacy professional preference` |
| H-14 | Telemetry intentionally disabled; policy/source review only |

## Evidence boundaries

The persona matrix is traceability, not a claim that every possible adversarial case is exhausted. Concurrent independent-field patches are tested; true multi-device same-field conflict resolution is not. Native TalkBack/VoiceOver, physical force-stop/network conditions, text scaling and production migration lock measurements remain NOT RUN. See [native matrix](native-validation.md). No formal accessibility/security certification or production/legal approval is claimed.

## Migration suite ordering regression

The standalone historical tests execute V0 in their temporary schema, but V0 also creates `public.uuidv7()`. If these tests run before Spring initializes public, Flyway previously rejected the nonempty public schema without history. `MigrationTestSupport` now performs normal current migrations in the isolated application database before each historical schema test. Historical migration targets, assertions and cleanup remain unchanged; no applied migration or production setting is edited, no baseline is fabricated and no real database is touched. A clean-database full-suite execution verifies this ordering.

The pre-existing Phase D pending-account cleanup suite additionally uses a unique temporary application schema, removed after the class. Cleanup intentionally scans all eligible pending accounts; it must not count or erase committed historical synthetic accounts from other test classes. The observed boundary failure was instead caused by calendar-day versus exact-duration arithmetic across the autumn DST offset change; exact-second fixtures and elapsed-second assertions now match the unchanged production cutoff. Schema isolation prevents this suite from counting or erasing another suite's fixtures.
