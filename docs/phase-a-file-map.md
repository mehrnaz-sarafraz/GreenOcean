# Phase A changed-file map

Base: origin/main at ab44bc1. Runtime-source validation commit: 21ce213. The following list includes every changed tracked path at report generation; report-only metadata can be added afterward.

| File | Requirements | Purpose |
|---|---|---|
| `.env.production.example` | A-01–A-06 | Document or verify the approved contract and rollout |
| `.github/workflows/phase-a.yml` | A-01–A-06 | Reproducible dependencies, isolated QA and build/test quality gates |
| `.gitignore` | A-01–A-06 | Document or verify the approved contract and rollout |
| `PROJECT_MEMORY.md` | A-01–A-06 | Document or verify the approved contract and rollout |
| `README.md` | A-01–A-06 | Document or verify the approved contract and rollout |
| `backend/pom.xml` | A-01–A-06 | Reproducible dependencies, isolated QA and build/test quality gates |
| `backend/src/main/java/com/greenocean/backend/BackendApplication.java` | A-01–A-06 | Document or verify the approved contract and rollout |
| `backend/src/main/java/com/greenocean/backend/account/repository/AccountRepository.java` | A-04 | Owner-authorized erasure, access revocation and retryable credential-file cleanup |
| `backend/src/main/java/com/greenocean/backend/account/service/AccountErasureWorker.java` | A-04 | Owner-authorized erasure, access revocation and retryable credential-file cleanup |
| `backend/src/main/java/com/greenocean/backend/account/service/AccountService.java` | A-04 | Owner-authorized erasure, access revocation and retryable credential-file cleanup |
| `backend/src/main/java/com/greenocean/backend/assistance/dto/ResetPasswordRequest.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/assistance/repository/AssistanceRepository.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/assistance/service/AssistanceService.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/assistance/service/EmailOutboxWorker.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/assistance/service/MailTransport.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/assistance/service/OutboxCipher.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/assistance/service/WebhookMailTransport.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/config/ActiveSessionJwtValidator.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/config/AuthAbuseFilter.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/config/CompatiblePasswordEncoder.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/config/PasswordConfig.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/config/SecurityConfig.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/controller/WebSessionController.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/dto/ChangePasswordRequest.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/dto/LoginRequest.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/dto/RegisterRequest.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/entity/User.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/repository/UserSessionRepository.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/service/AuthRateLimit.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/service/AuthenticationService.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/service/PasswordPolicy.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/auth/service/RegistrationService.java` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `backend/src/main/java/com/greenocean/backend/common/exception/GlobalExceptionHandler.java` | A-01–A-06 | Document or verify the approved contract and rollout |
| `backend/src/main/java/com/greenocean/backend/identity/AccessPolicy.java` | A-01–A-06 | Enforce age, account, regional, public-resource and professional-scope contracts |
| `backend/src/main/java/com/greenocean/backend/identity/IdentityController.java` | A-01–A-06 | Enforce age, account, regional, public-resource and professional-scope contracts |
| `backend/src/main/java/com/greenocean/backend/identity/PhaseAInterceptor.java` | A-01–A-06 | Enforce age, account, regional, public-resource and professional-scope contracts |
| `backend/src/main/java/com/greenocean/backend/identity/PhaseAWebConfig.java` | A-01–A-06 | Enforce age, account, regional, public-resource and professional-scope contracts |
| `backend/src/main/java/com/greenocean/backend/identity/ProductionPhaseAValidator.java` | A-01–A-06 | Enforce age, account, regional, public-resource and professional-scope contracts |
| `backend/src/main/java/com/greenocean/backend/identity/PublicCatalogController.java` | A-01–A-06 | Enforce age, account, regional, public-resource and professional-scope contracts |
| `backend/src/main/java/com/greenocean/backend/listener/service/ListenerService.java` | A-02, A-03, A-06 | Reuse verified workflows and enforce service/territory boundaries |
| `backend/src/main/java/com/greenocean/backend/messaging/repository/MessagingRepository.java` | A-02, A-03, A-06 | Reuse verified workflows and enforce service/territory boundaries |
| `backend/src/main/java/com/greenocean/backend/messaging/service/MessagingService.java` | A-02, A-03, A-06 | Reuse verified workflows and enforce service/territory boundaries |
| `backend/src/main/java/com/greenocean/backend/professional/service/ProfessionalWorkflowService.java` | A-02, A-03, A-06 | Reuse verified workflows and enforce service/territory boundaries |
| `backend/src/main/java/com/greenocean/backend/profile/entity/Profile.java` | A-04, A-05 | Private defaults, deleted-profile exclusion or secure platform token storage |
| `backend/src/main/java/com/greenocean/backend/profile/service/ProfileService.java` | A-04, A-05 | Private defaults, deleted-profile exclusion or secure platform token storage |
| `backend/src/main/resources/application.properties` | A-01–A-06 | Document or verify the approved contract and rollout |
| `backend/src/main/resources/db/migration/V31__phase_a_identity_privacy_access.sql` | A-01–A-06 | Additive schema/reference data; preserve V0–V30 |
| `backend/src/main/resources/db/migration/V32__reference_country_catalog.sql` | A-01–A-06 | Additive schema/reference data; preserve V0–V30 |
| `backend/src/main/resources/db/migration/V33__phase_a_conversation_boundaries_and_token_rollout.sql` | A-01–A-06 | Additive schema/reference data; preserve V0–V30 |
| `backend/src/main/resources/db/migration/V34__correct_localized_reference_country_names.sql` | A-01–A-06 | Additive schema/reference data; preserve V0–V30 |
| `backend/src/main/resources/db/migration/V35__explicit_professional_care_regions.sql` | A-01–A-06 | Additive schema/reference data; preserve V0–V30 |
| `backend/src/test/java/com/greenocean/backend/AuthFlowIntegrationTest.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/java/com/greenocean/backend/CommunityNotificationIntegrationTest.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/java/com/greenocean/backend/ContentSocialIntegrationTest.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/java/com/greenocean/backend/PhaseAConcurrencyTest.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/java/com/greenocean/backend/PhaseAFixtures.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/java/com/greenocean/backend/PhaseAIntegrationTest.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/java/com/greenocean/backend/PhaseAMigrationTest.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/java/com/greenocean/backend/PhaseASecurityUnitTest.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/java/com/greenocean/backend/ProductConfigurationIntegrationTest.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/java/com/greenocean/backend/ProfessionalAvailabilityIntegrationTest.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/java/com/greenocean/backend/ProfileAccountIntegrationTest.java` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `backend/src/test/resources/application-test.properties` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `docs/architecture/account-lifecycle.md` | A-01–A-06 | Document or verify the approved contract and rollout |
| `docs/architecture/geography-and-service-availability.md` | A-01–A-06 | Document or verify the approved contract and rollout |
| `docs/architecture/identity-and-permissions.md` | A-01–A-06 | Document or verify the approved contract and rollout |
| `docs/migrations/phase-a-rollout.md` | A-01–A-06 | Document or verify the approved contract and rollout |
| `docs/phase-a-delivery-report.md` | A-01–A-06 | Document or verify the approved contract and rollout |
| `docs/product/phase-a-approved-specification.md` | A-01–A-06 | Document or verify the approved contract and rollout |
| `docs/product/phase-a-requirements.md` | A-01–A-06 | Document or verify the approved contract and rollout |
| `docs/security/phase-a-threat-model.md` | A-01–A-06 | Document or verify the approved contract and rollout |
| `docs/testing/phase-a-results.json` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `docs/testing/phase-a-results.md` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `docs/testing/phase-a-test-matrix.md` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `frontend/e2e/phase-a.spec.mjs` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `frontend/package-lock.json` | A-01–A-06 | Reproducible dependencies, isolated QA and build/test quality gates |
| `frontend/package.json` | A-01–A-06 | Reproducible dependencies, isolated QA and build/test quality gates |
| `frontend/playwright.config.mjs` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `frontend/src/app/(app)/_layout.tsx` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/app/(app)/create.tsx` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/app/(auth)/activate.tsx` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/app/(auth)/info/[page].tsx` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/app/(auth)/register.tsx` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/app/(auth)/resources.tsx` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/app/(auth)/welcome.tsx` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/app/_layout.tsx` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/components/app-field.tsx` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/features/auth/auth-provider.tsx` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `frontend/src/features/auth/display-review.tsx` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `frontend/src/features/auth/registration-validation.ts` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `frontend/src/features/auth/types.ts` | A-01, A-04, A-05 | Minimal private enrollment, protected recovery, secure sessions and password compatibility |
| `frontend/src/lib/api/client.ts` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/lib/api/request.ts` | A-01–A-06 | Present minimal registration, activation, public resources and per-account UI state |
| `frontend/src/lib/storage/token-storage.ts` | A-04, A-05 | Private defaults, deleted-profile exclusion or secure platform token storage |
| `frontend/tests/phase-a.test.mjs` | A-01–A-06 | Executable synthetic-data persona, API, database, concurrency or browser coverage |
| `scripts/run-phase-a-qa.ps1` | A-01–A-06 | Reproducible dependencies, isolated QA and build/test quality gates |
| `docs/phase-a-file-map.md` | A-01–A-06 | Trace every changed path to its purpose and requirement |
