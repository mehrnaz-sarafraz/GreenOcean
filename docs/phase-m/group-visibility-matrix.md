# Server visibility matrix

| Surface | PUBLIC/PRIVATE nonmember | Current member | HIDDEN outsider | Valid hidden invite recipient | Authorized manager |
|---|---|---|---|---|---|
| Ordinary directory/search/suggestions | Approved minimal card | Approved minimal card | Absent | Absent | Hidden still absent from ordinary directory |
| Own groups workspace | No membership entry | Own entry | Absent | Pending invite only | Own entry |
| Group posts/replies/events | Denied | Current audience/block/status restrictions | Same404 as missing | Denied until explicit acceptance | Same content rules, no anonymous attribution |
| Roster | Denied | Empty/redacted | Denied | Denied | Minimum management fields |
| Counts | Redacted/null | Redacted/null | No existence contribution | No public count contribution | Bounded management roster only |
| Rules and joining explanation | Approved rules/card | Current rules | Absent | Minimal decision-specific rules in inbox | Manage under global policy |

PUBLIC never means guest internet access. Group metadata and content use no-store. Approved UUID-based slugs do not depend on a hidden name. A member's own account archive is a separate owner-rights surface and never authorizes reading another member's discussion. Small groups, distinctive text, voluntary named participation and previously viewed/copied information remain inference risks.

## Critical privacy matrix: automated evidence

The product has no independent group recommender or public calendar. Their ordinary directory/discovery consumers use the same filtered queries; no new subsystem is fabricated solely for testing.

| Specification case | Executed automated assertion location |
|---|---|
|1 hidden discovery absent|PhaseM1PrivacyTest.hiddenExistenceAndRosterAreProtected; PhaseM3PrivacySurfaceTest.hiddenStoriesNeverEnterGlobalDiscoverySearchOrDirectoryEvenForMembers|
|2 hidden search absent|PhaseM3PrivacySurfaceTest.hiddenStoriesNeverEnterGlobalDiscoverySearchOrDirectoryEvenForMembers (serialized global search and directory query)|
|3 hidden suggestions/global activity absent|Same test, DISCOVER/FOLLOWING and legacy feed; no separate recommendation engine|
|4 hidden public counts absent|PhaseM3PrivacySurfaceTest.publicProfilesAndCountsDoNotAdvertiseNamedGroupParticipation; directory null-count projection|
|5 guessed IDs|PhaseM1PrivacyTest.hiddenExistenceAndRosterAreProtected; PhaseM3ActivitiesTest.hiddenEventAndReportingIdentifiersDoNotConfirmExistence|
|6 guessed slugs|PhaseM3PrivacySurfaceTest.guessedHiddenAndMissingReferencesHaveTheSameNondisclosingErrorAndNoStore|
|7 guessed event IDs|PhaseM3ActivitiesTest.hiddenEventAndReportingIdentifiersDoNotConfirmExistence|
|8 wrong invitation recipient|PhaseM2MembershipTest.hiddenInvitationIsExplicitTargetBoundAndIdempotent|
|9 expired invitation|PhaseM2MembershipTest.revokedExpiredOrConsumedInvitesCannotRestoreMembership|
|10 revoked invitation|Same test; PhaseM2ConcurrencyTest.revocationAndAcceptanceHaveAConsistentCommittedWinner|
|11 duplicate acceptance|PhaseM2ConcurrencyTest.concurrentInviteAcceptanceCreatesOneMembership|
|12 pending private content denial|PhaseM2MembershipTest.privatePendingCancellationAndOldApprovalsCannotGrantAccess|
|13 nonmember roster denial|Same test (details404); PhaseM1PrivacyTest.groupPublicDoesNotMakeDiscussionOrRosterPublic|
|14 ordinary roster redaction|PhaseM2MembershipTest.publicJoinRequiresRulesAndDoesNotExposeRosterOrCreateOtherRelationships|
|15 platform role is no implicit group access|PhaseM3ActivitiesTest.platformStatusRequiresRealMfaAndOrdinaryManagersCannotReadStaffEvidence; existing current member predicates; true-stack purpose/MFA review|
|16 manager cannot unmask|PhaseM2MembershipTest.groupModeratorCannotEscalateOrReadPlatformAttribution; exact minimized group report queue assertion|
|17 named permitted social identity|PhaseM3PrivacySurfaceTest.publicProfilesAndCountsDoNotAdvertiseNamedGroupParticipation (current group DTO identity, no email)|
|18 anonymous notifications|PhaseM3PrivacySurfaceTest.anonymousGroupReplyNotificationNeverIdentifiesItsAuthor|
|19 revocation including saved/direct access|PhaseM2MembershipTest.banRevokesSavedDirectAndAnonymousGroupAccess; M2 true-stack leave/replay journey|
|20 suspension|PhaseM3PrivacySurfaceTest.blockAndGroupSuspensionOverrideMembershipAndPreviouslySavedAccess; M3 races|
|21 block/moderation override|Same test; PhaseM3ActivitiesTest.independentGroupHideAndPlatformAppealDoNotOverrideGlobalBlocks|
|22 historical PUBLIC/FOLLOWERS retained|PhaseLMigrationTest; PhaseMMigrationTest and PhaseM3MigrationTest preserve stored audiences/identity|
|23 conservative populated upgrade|PhaseMMigrationTest; PhaseM3MigrationTest.populatedM2RetainsActivityMembershipAndAnonymousAudiences|
|24 no profile membership disclosure|PhaseM3PrivacySurfaceTest.publicProfilesAndCountsDoNotAdvertiseNamedGroupParticipation|
|25 PUBLIC does not publish group content universally|PhaseM1PrivacyTest.groupPublicDoesNotMakeDiscussionOrRosterPublic; M2 private/public authorization cases|

Tests inspect actual serialized responses and real PostgreSQL state. Browser fixture tests are separately labeled and are not substituted for integrated E2E. Final commit/run outcomes are recorded in test-evidence.
