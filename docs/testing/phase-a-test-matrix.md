# Phase A test matrix

All tests use synthetic identities and an explicitly isolated PostgreSQL instance. Backend coverage is JUnit/Spring MockMvc, database/repository and real transaction concurrency; frontend coverage is Node 24's native test runner over the actual validation/network/refresh helpers. These are not presented as device or browser end-to-end tests.

## Personas

- Happy: minimal registration/private defaults, verification/login/logout, anonymous resources, owner deletion, browser cookie rotation, recovery/session revocation, fake-mail delivery.
- Creative: long Unicode passwords and legacy BCrypt upgrade, lowercase geography, independent email-bound code hashes, legacy-account grace, catalog queries, simultaneous refresh.
- Confused: invalid fields, stale/missing Terms, expired codes/resend, unavailable regional services, failed delivery/retry, invalid city/country, timeout/network recovery.
- Malicious: direct-API underage attempts, role/status/trust-method injection, protected pending routes, code reuse/guessing, suspension, cross-origin requests, independent-decision downgrade, wrong-password/cross-account deletion, role removal and retired-token replay.

## Required journeys

| ID | Executable evidence | Status / limitation |
|---|---|---|
| E2E-01 Welcome/Register/Verify/Activate/Login | `verifyActivateLoginLogoutFlow`, `minimalRegistrationHasPendingUserAndPrivateDefaults` | Backend API chain tested; full UI journey pending |
| E2E-02 Underage/public alternative | `minorCannotRegisterByDirectApi`, `publicResourcesAreSeparateAndAnonymous`, frontend attestation test | Backend/form helpers tested; browser navigation pending |
| E2E-03 Skip optional preferences | existing `ProductConfigurationIntegrationTest`, minimal registration/empty topics tests | API tested; onboarding UI pending |
| E2E-04 Login/profile/logout/login | existing `ProfileAccountIntegrationTest`, `AuthFlowIntegrationTest` | API tested; UI pending |
| E2E-05 Invalid Terms | `missingOrStaleTermsAreRejected`, frontend Terms validation | API/helpers tested |
| E2E-06 Pending sensitive access | `pendingAccountCannotAccessSocialRoutes` | API tested |
| E2E-07 Professional applicant remains unverified | role-forgery coverage plus existing professional workflow checks | Full apply/upload/review UI journey pending |
| E2E-08 Suspension invalidates access/refresh | `suspendedAccountCannotRefreshOrUseExistingAccess` | API tested |
| E2E-09 Password recovery revokes sessions | `passwordRecoveryInvalidatesEveryOldSession` | API tested |
| E2E-10 Account deletion invalidates session/profile | `ownerDeletionRevokesSessionAndHidesProfile`, profile service exclusion | API tested; backup restoration operational verification pending |
| E2E-11 Switch accounts on one device | keyed data provider, session-generation checks | Implementation present; real browser/mobile multi-user E2E pending |
| E2E-12 Unavailable services | `unconfiguredCountryGivesAlternativeWithoutGrantingService` | API tested; UI presentation pending |
| E2E-13 Anonymous public/private separation | `publicResourcesAreSeparateAndAnonymous` | API tested |
| E2E-14 Concurrent refresh | `simultaneousRefreshHasOneWinnerAndNoTwoUsableTokens`, frontend single-flight test | Real independent backend transactions and frontend helper tested |
| E2E-15 Network failure/recovery | native timeout/network/retry tests | Helpers tested; browser form interruption pending |

Fixtures are generated per test: pending, active verified, suspended, deleted, applicant/role-injection, underage request, unknown country and synthetic city. Existing professional availability, content/social, community, profile and database-invariant tests are preserved and adapted explicitly to eligible adult fixtures.

No Android or iOS emulator/device tests have run. No production SMTP/gateway delivery has run. The fake MailTransport tests exercise protected queue delivery, payload purge and failed delivery retry without contacting a real recipient.
