# GreenOcean — Phase D: Email Verification, Delivery & Secure Account Activation

## ROLE

You are the principal backend engineer, full-stack architect, application security engineer, reliability engineer, and QA lead for GreenOcean.

Your task is to complete **Phase D — Email Verification and Account Activation** in the existing GreenOcean repository.

Repository: `mehrnaz-sarafraz/GreenOcean`

Technology:
- Java 21 / Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router / TypeScript
- Web, Android, and iOS

**Primary objective:**

Deliver a secure, reliable, user-friendly, operationally realistic email verification and account activation workflow.

The work must cover D-01 through D-12.

This is NOT a greenfield implementation.

Reuse working code, extend incomplete features, fix relevant defects, implement necessary tests, and create a reviewable Pull Request.

Do not stop after producing an audit report or implementation plan.

---

# 1. Establish the correct repository baseline

IMPORTANT: Phase A, Phase B, and potentially Phase C may exist on different branches.

Previously observed:

- Phase A: PR #1, `feature/phase-a-identity-privacy-access`
- Phase B: PR #2, `feature/phase-b-welcome-trust-experience`
- Phase B is stacked on Phase A.
- Both PRs were open drafts during the last inspection.
- Phase C delivery may be on another branch or workspace.

These observations may have changed.

Before any code modification:

1. Inspect the current main HEAD.
2. Inspect relevant active branches and PRs.
3. Find the latest valid implementation of Phase A, B, and C.
4. Examine available test evidence and CI results.
5. Determine the correct implementation baseline.
6. Do not branch from outdated main if doing so would lose A/B/C changes.
7. Do not recreate previous work.

If Phase C is only present in a local workspace, determine whether that workspace is available.

If it cannot be found, preserve the best verified A/B baseline and document any unresolved Phase C dependency.

Never silently overwrite another branch.

---

# 2. Strict non-duplication rule

Phase D overlaps with previous phases.

You MUST NOT reimplement functionality that is already correct and verified.

For any existing feature, use:

- REUSE_VERIFIED
- REUSE_BUT_ADD_TESTS
- MODIFY_DEFECTIVE
- COMPLETE_PARTIAL
- IMPLEMENT_MISSING
- BLOCKED_EXTERNAL

A previously implemented feature can be REUSE_VERIFIED only if the actual code and valid test evidence support that conclusion.

If a dependency has changed since its tests ran, perform targeted verification.

Do not repeat the full Phase A, B, or C audit.

Do not rerun every unrelated test solely as another architectural review.

Run relevant regression tests and the new Phase D tests.

---

# 3. Mandatory targeted code inspection

Inspect the actual latest files corresponding to these previously observed components.

## Backend

- `AssistanceService`
- `AssistanceRepository`
- `AssistanceController`
- `EmailOutboxWorker`
- `MailTransport`
- `WebhookMailTransport`
- `OutboxCipher`
- `AuthRateLimit`
- `AuthAbuseFilter`
- `RegistrationService`
- `AuthenticationService`
- `AccessPolicy`
- `User`
- `UserStatus`
- `UserRepository`
- Security configuration
- Account eligibility and policy acceptance services
- Relevant database migrations
- Relevant existing integration tests

## Frontend

- Activation and verification screens
- Registration screen
- Login screen
- Authentication provider
- API client
- Account state and eligibility navigation
- Localization
- Existing form components
- Existing Phase C improvements
- Existing UI and E2E tests

## Operations

- Application configuration
- Mail environment variables
- Current CI workflows
- Mail gateway adapter contract
- Outbox retry and cleanup behavior
- Logging and monitoring
- Database cleanup scheduling
- Deployment documentation

Use actual current paths when files have moved.

---

# 4. Known previous implementation — verify before changing

The previous Phase A/B implementation included:

- SecureRandom-based six-digit verification codes
- A 15-minute verification-code validity period
- Email-bound HMAC hashing
- AES-GCM encrypted Outbox secrets
- Transactional single-use verification-code consumption
- Initial durable rate limits
- A database-backed email outbox
- A scheduled outbox worker
- A MailTransport interface
- An HTTPS webhook mail adapter
- Initial PENDING account enrollment
- A conditional PENDING-to-ACTIVE transition
- Backend access restrictions through AccessPolicy
- An activation screen with code submission and resend
- Controlled fake-mail integration tests

These are reference observations, not permission to assume they are still correct.

Preserve them when valid.

Investigate and resolve actual defects, incomplete edge cases, and remaining operational dependencies.

Known unresolved concerns from Phase A include:

- Real email gateway integration and delivery verification
- Separate stable production encryption key
- Operational delivery monitoring
- Account cleanup/retention completion
- Full-stack browser and native-device journeys
- Legacy-user activation rollout
- Production geographic availability and other independent release gates

Phase D must not falsely claim to resolve unrelated legal, MFA, or geographic launch approvals.

---

# 5. D-01 — Verification code generation

Priority: P0

Inspect the existing code-generation implementation.

### Requirements

- Use cryptographically secure randomness.
- Preserve a fixed-length numeric code if this remains the approved product design.
- Support leading zeros.
- Keep the code associated with one intended account and email address.
- Avoid exposing code values in API responses.
- Avoid logging or analytics capture of code values.
- Avoid creating inconsistent multiple active challenges.
- Ensure code generation and challenge persistence are transactionally consistent.

Do not create a new token-generation service if the existing implementation is correct.

### Tests

Happy:
- Valid code is generated and accepted.

Creative:
- Leading-zero codes are handled correctly.

Confused:
- Blank, incomplete, or malformed codes fail understandably.

Malicious:
- Repeated generation cannot bypass abuse controls or create unlimited useful challenges.

---

# 6. D-02 — Secure code storage

Priority: P0

Inspect OutboxCipher and the verification-code repository.

### Requirements

- Never store a six-digit verification code as plaintext in verification tables.
- Do not rely on unkeyed SHA-256 alone for low-entropy codes.
- Use a protected keyed construction or an equally secure approved approach.
- Keep encryption keys outside source control.
- Separate operational mail encryption keys from JWT signing keys in production.
- Assess whether encryption and verification-HMAC keys should be independently derived or configured.
- Avoid secret exposure in exceptions, logs, debug tools, metrics, and database reports.
- Restrict database and mail-worker access using least privilege.
- Ensure queued plaintext secrets are protected at rest until delivery or expiry.
- Purge sensitive Outbox payloads after successful acceptance, expiry, or terminal failure according to policy.
- Design a safe key-rotation procedure.

Do not change existing encrypted formats without a migration or an explicit strategy for pending Outbox entries.

### Tests

- Database records do not expose plaintext codes.
- Incorrect codes fail.
- Correct codes work with the correct key.
- Invalid or corrupted ciphertext fails safely.
- Key changes cannot silently make pending deliveries unrecoverable.
- Debug logs do not contain codes.

---

# 7. D-03 — Expiration policy

Priority: P0

The previously implemented verification validity period was 15 minutes.

Preserve this value unless the current approved policy specifies otherwise.

Do not assume a universal NIST MFA OTP expiration limit automatically applies to email ownership confirmation.

### Requirements

- Expiration must be enforced server-side.
- Use reliable UTC timestamps.
- Avoid relying on client-side timers for validity.
- Expired codes cannot activate accounts.
- Delayed delivery must have a recoverable resend path.
- UI must correctly explain expiry.
- Resending must produce consistent challenge validity.

Prefer injectable clock/time abstractions for deterministic tests where practical.

### Tests

- Just before expiration
- Exactly at expiration
- Just after expiration
- Client clock incorrect
- Delayed email delivery
- Expired challenge resent
- Expired code replay

---

# 8. D-04 — Atomic single-use consumption

Priority: P0

Inspect the current SQL-based consume operation.

### Requirements

- A verification challenge is consumable only once.
- Consumption must be atomic.
- Concurrent requests cannot both succeed.
- An expired challenge cannot be consumed.
- A challenge for one email cannot verify another account.
- Resending must follow a defined invalidation policy.
- A failed or incomplete activation transaction must not leave contradictory account states.

If activation and code consumption are in separate operations, review whether transactional boundaries permit partial success.

Do not create unnecessary parallel token tables.

### Tests

- One successful confirmation
- Immediate reuse
- Ten concurrent confirmations of one code
- Code for another user
- Expired code
- Resend followed by old-code attempt
- Database rollback/failure during activation

Verify database state, not only HTTP status.

---

# 9. D-05 — Real email delivery and reliable Outbox processing

Priority: P0

This is a major Phase D objective.

Inspect the existing:

- EmailOutboxWorker
- MailTransport
- WebhookMailTransport
- Email outbox schema
- Mail configuration
- Scheduled job configuration
- Existing controlled test transport

### Step 1 — Preserve existing architecture

Do not replace the transactional Outbox unnecessarily.

Do not create multiple competing mail-delivery workers.

Reuse MailTransport unless a concrete incompatibility requires a justified change.

### Step 2 — Complete the production integration boundary

Provide a reliable production-configurable mail gateway.

If existing WebhookMailTransport is sufficient, preserve it and complete the missing operational integration.

Do not hardcode a specific commercial email provider without reason.

The integration must support:

- Authenticated HTTPS delivery
- Stable delivery identifiers
- Idempotency keys
- Secure configuration via environment/secrets management
- Explicit sender identity
- Verified templates
- Safe timeout behavior
- Structured delivery outcomes
- Retryable versus permanent errors
- Provider acceptance tracking
- Delivery/bounce webhook handling where supported
- Monitoring and alerting

Provider acceptance must not be misrepresented as guaranteed Inbox delivery.

### Step 3 — Harden Outbox reliability

Evaluate and improve:

- Worker concurrency
- Multi-instance processing
- Row locking and leases
- Network calls inside database transactions
- Retrying after worker crashes
- Backoff and jitter
- Retry exhaustion
- Dead-letter or terminal-failure states
- Recovery of stuck SENDING entries
- Idempotent provider submission
- Duplicate-send prevention
- Expired message suppression
- Safe handling of resend replacing old challenges
- Queue visibility and health metrics
- Cleanup of sensitive payloads

Avoid holding database transactions open across slow network calls where a safer state-machine/lease pattern is appropriate.

If delivery outcome is uncertain after a timeout, use idempotency and reconciliation instead of blindly sending duplicates.

### Step 4 — Delivery templates

Use appropriate templates for:

- Email verification
- Resend verification
- Confirmation of important account changes where necessary

Preserve existing password-reset templates.

Templates must not expose private mental health data.

Support existing application languages where practical.

Avoid unsupported marketing or privacy claims.

### Step 5 — Sender-domain readiness

Document operational prerequisites such as:

- Verified sending domain
- SPF
- DKIM
- DMARC
- Sender address
- Provider credentials
- Bounce handling
- Delivery reputation and monitoring

Do not fabricate credentials or assume these have been configured.

### Step 6 — Test environments

Provide:

- Safe fake/in-memory MailTransport for unit tests
- Controlled local mail capture for integration/E2E
- A staging configuration that can send to explicitly authorized test mailboxes
- Production fail-closed configuration when required delivery is unavailable

Never send test verification codes to random real addresses.

Never log real tokens to provide a workaround.

### Critical acceptance rule

If real provider credentials and approved sender-domain configuration are unavailable, complete the implementation and local/staging tests that are possible, but mark:

`D-05 = BLOCKED_EXTERNAL_DEPENDENCY`

Do not claim production email delivery works merely because a mocked transport passed.

---

# 10. D-06 — Verification attempts and resend limits

Priority: P0

Reuse existing AuthRateLimit and AuthAbuseFilter components where applicable.

### Required protection

- Limit verification attempts per challenge/account.
- Limit resend requests.
- Protect against distributed guessing using additional abuse signals where appropriate.
- Avoid relying solely on client-side countdowns.
- Keep failed-attempt accounting durable where needed.
- Prevent generating new codes from trivially resetting abuse counters.
- Avoid account enumeration.
- Avoid easy intentional account denial-of-service through excessive lockouts.
- Protect against excessive outgoing email.
- Return understandable retry feedback without sensitive disclosures.

Define configurable limits, with tests and rationale.

Do not hardcode limits only in the frontend.

### Tests

- Repeated invalid codes
- Rapid resend requests
- Cross-IP attempts in controlled simulations
- Attempts from one IP against many accounts
- Attempts against one account through multiple clients
- Expiration of rate windows
- Normal user recovery after a limit
- New code after multiple failures
- Shared-network legitimate-user scenarios

Do not perform actual denial-of-service testing on production systems.

---

# 11. D-07 — Email verification and activation UX

Priority: P0

Inspect and improve the existing activation screen.

Previously observed:

`frontend/src/app/(auth)/activate.tsx`

Do not discard it or introduce another competing verification page.

### Required experience

The user should understand:

- Their account has been created.
- Email verification is still needed.
- Where the code was sent, using appropriately masked information if displayed.
- Where to enter the code.
- How long it remains valid.
- How to request another code.
- How to fix an incorrect email.
- What to do if no email arrives.
- What happens after verification.
- Which public resources remain accessible.

### UI requirements

- Clear heading and short instructions
- Six-digit code entry
- Paste support
- Appropriate one-time-code autofill where available
- Accessible focus handling
- Loading state
- Incorrect-code feedback
- Expired-code feedback
- Rate-limit feedback
- Resend confirmation
- Network failure recovery
- Clear success state
- Navigation to the next permitted step
- Ability to reopen the process safely
- Support for existing locales and RTL where applicable
- No unnecessary disclosure of personal information

Avoid an endless loading state.

Avoid sending the user through registration again after every failed verification.

Separate email ownership verification from additional legacy-account eligibility reviews where needed, without creating duplicate account-state logic.

Do not automatically issue a fully privileged session as a consequence of code entry alone.

If the existing system uses a limited PENDING session, preserve this secure design and verify all permissions.

---

# 12. D-08 — Resend verification code

Priority: P0

Reuse the existing resend endpoint.

### Requirements

- Prevent repeated uncontrolled sending.
- Show accurate request/submission feedback.
- Apply backend resend limits.
- Clearly define whether older codes are invalidated.
- Avoid confusing users who receive multiple emails out of order.
- Avoid resending codes to an unverified new address without a secure account-ownership process.
- Preserve account enumeration protection.
- Prevent multiple pending Outbox entries from creating contradictory activation instructions.

### Tests

- Normal resend
- Rapid repeated taps
- Previous code used after resend
- Delayed previous email arriving after new email
- Backend rate limit
- Provider unavailable
- Browser refresh
- App restart
- Account already verified
- Suspended/deleted account

---

# 13. D-09 — Correct account activation

Priority: P0

Reuse the existing PENDING-to-ACTIVE lifecycle and eligibility rules from Phase A.

### Requirements

- Only valid verification credentials establish email ownership.
- Do not activate an account using a code associated with another email.
- Do not activate suspended or deleted accounts.
- Preserve adult eligibility requirements.
- Preserve Terms acceptance requirements.
- Preserve the geographic availability model.
- Do not mistake email verification for independent age verification.
- Do not mistake account ACTIVE status for universal service availability.
- Make activation idempotent where appropriate.
- Ensure concurrent verification does not result in contradictory state.

### Important distinction

Email ownership verification and authorization for a particular service are different decisions.

An email can be verified while a service remains unavailable in the user's region.

Do not automatically grant access to listener, community, or professional services just because email verification succeeded.

### Tests

- New PENDING account
- Correct code
- Incorrect code
- Expired code
- Concurrent verification
- Missing adult eligibility
- Missing/stale Terms acceptance where required
- Unsupported regional service
- Suspended account
- Deleted account
- Already verified account
- Legacy account activation

---

# 14. D-10 — Protect social services before activation

Priority: P0

Reuse the existing AccessPolicy and Spring Security controls.

Do not build a second global authorization system.

### Required validation

A PENDING account must not access protected social features merely by obtaining a limited activation session.

Audit relevant endpoint categories:

- Post creation
- Comment creation
- Reactions where applicable
- Private messages
- Conversation initiation
- Community participation
- Listener matching
- Professional-only actions
- Administrative actions
- Private member resources

### Permitted pending-account operations

Maintain an explicit allowlist for limited actions needed to complete activation, such as:

- Verify email
- Resend verification
- Review required policies
- Complete allowed account eligibility steps
- Manage appropriate own-account settings
- Request account deletion
- Access public resources and urgent help

Do not open general social endpoints simply because a user possesses a valid JWT.

### Tests

For each protected endpoint category, execute a negative authorization test using a valid but limited PENDING account.

Verify both HTTP denial and absence of unauthorized database changes.

---

# 15. D-11 — Correcting an incorrectly entered email address

Priority: P1

The previous implementation did not establish a complete verified workflow for correcting the email address of a pending account.

Implement only if missing.

### Primary requirements

Allow an account owner who entered the wrong email to request a secure correction without creating an unnecessary duplicate account.

Design a safe proof-of-ownership process for the pending account.

Depending on the existing Phase A architecture, this may use:

- Authenticated limited pending session plus password reauthentication
- A secure enrollment continuation credential
- Another reviewed proof already supported by the account system

Do not assume control of an unverified email address.

### Security requirements

- Reauthenticate the pending account owner appropriately.
- Do not permit anonymous arbitrary email reassignment.
- Validate the new email.
- Enforce uniqueness safely.
- Do not expose whether the target email belongs to another user.
- Invalidate obsolete verification challenges.
- Bind new challenges to the new address.
- Require verification of the new address.
- Avoid silent privilege changes.
- Record minimal account-change audit evidence.
- Preserve role and eligibility restrictions.
- Apply rate limits.
- Prevent cross-account email changes.

If the account is already verified/active, use a stronger separately reviewed email-change process rather than automatically reusing pending-account logic.

### User experience

Provide:

- Edit email action
- Clear explanation
- Secure confirmation
- New verification instruction
- Correct success/error behavior
- Navigation back to verification

### Tests

- Correcting a mistyped email
- Changing to an existing email
- Expired pending session
- Wrong password or invalid proof
- Multiple changes
- Concurrent changes
- Cross-account attempts
- Old verification code after change
- Successful verification of new address

---

# 16. D-12 — Cleanup of abandoned pending accounts

Priority: P0

Implement or complete a safe configurable retention and cleanup workflow.

Do not confuse Outbox cleanup with abandoned-account cleanup.

### Design requirements

- Define an explicit retention policy.
- Make the pending-account age threshold configurable.
- Do not assume a universal legal retention period.
- A seven-day period may be used as a provisional product configuration only if documented and approved.
- Do not automatically delete old ACTIVE accounts merely because email_verified is false.
- Preserve the Phase A legacy-account transition and grace policy.
- Exclude suspended accounts from generic pending-account cleanup.
- Handle delivery outages fairly, including extending or pausing cleanup where appropriate.
- Do not delete accounts while an authorized activation or recovery operation is actively underway.
- Keep deletion idempotent.
- Process accounts in bounded batches.
- Support multi-instance safety.
- Ensure failures can be retried safely.
- Respect foreign keys and existing data-retention rules.
- Avoid storing unnecessary deleted-account personal information.

### Related data

Assess safe cleanup of:

- Verification challenges
- Outbox entries
- Temporary activation data
- Pending accounts
- Associated temporary profiles
- Sessions
- Rate-limit records where appropriate
- Other account-owned records created during pending status

Do not blindly cascade-delete historical data without reviewing the current schema.

### Operational requirements

- Scheduled cleanup
- Configurable interval
- Bounded batch size
- Dry-run/reporting mode where appropriate
- Safe logging without sensitive personal data
- Failure monitoring
- Clear retention documentation

### Tests

- Old abandoned PENDING account
- New PENDING account
- ACTIVE account
- Legacy ACTIVE unverified account
- SUSPENDED account
- Account with a recent resend
- Delayed mail delivery
- Concurrent verification and cleanup
- Cleanup retry
- Worker restart
- Associated data integrity
- Database transaction failure

---

# 17. Four mandatory test personas

All relevant requirements must be evaluated with four persona groups.

## HAPPY PATH

A normal eligible adult:

1. Registers.
2. Receives a verification email.
3. Enters the correct code.
4. Becomes email verified.
5. Completes necessary account activation.
6. Gains only permitted service access.
7. Signs in successfully.
8. Continues to onboarding.

Use actual backend and database state, not only mocked UI responses.

## CREATIVE USER

A legitimate unconventional user:

- Uses Persian and RTL.
- Pastes a six-digit code.
- Receives a code with leading zeroes.
- Opens email on one device and the app on another.
- Resends after the permitted interval.
- Changes network.
- Reopens the app.
- Uses a delayed verification email.
- Switches between web and mobile.
- Has valid account eligibility but no locally available social service.

Verify consistent, comprehensible behavior.

## CONFUSED USER

A user who makes mistakes:

- Enters the wrong code.
- Enters an incomplete code.
- Types the wrong email during registration.
- Requests several codes.
- Uses the old code after resending.
- Opens the app after the code has expired.
- Believes the email was never sent.
- Checks Spam.
- Has intermittent network access.
- Tries logging in before verification.
- Closes the app mid-process.
- Receives contradictory delayed email messages.
- Encounters a temporary mail provider outage.

Ensure clear recovery without exposing account existence.

## MALICIOUS USER

An attacker:

- Guesses verification codes repeatedly.
- Requests many verification emails.
- Attempts to verify someone else's account.
- Reuses consumed credentials.
- Submits expired credentials.
- Manipulates account IDs or email fields.
- Attempts to bypass PENDING restrictions.
- Tries to activate suspended/deleted accounts.
- Attempts to change another account's email.
- Attempts parallel confirmations.
- Attempts to exploit Outbox retries.
- Attempts to obtain raw codes from APIs/logs.
- Tries to exploit rate-limit resets.
- Uses manipulated client-side eligibility values.

Verify security at the backend and persisted-state level.

All adversarial tests must run against isolated synthetic test accounts.

---

# 18. Mandatory automated testing

## Unit tests

Test:

- Code generation
- Hashing and encryption
- Expiration calculations
- Verification state transitions
- Rate-limit policies
- Resend behavior
- Email correction validation
- Retention eligibility

## Backend integration tests

Use an isolated PostgreSQL test database.

Test:

- Full registration-to-verification lifecycle
- Correct activation
- Single-use verification
- Concurrent confirmation
- Wrong user/email
- Rate limiting
- Resend invalidation
- Mail queue insertion
- Worker execution
- Worker crash/restart
- Retry and idempotency
- Expired Outbox cleanup
- Pending-account cleanup
- Regional access restrictions
- Legacy-account compatibility

## Mail integration tests

Use controlled local/staging mail capture.

Verify:

- Correct recipient
- Correct template
- Correct code/link
- No plaintext secret in logs
- Provider success behavior
- Temporary provider failure
- Permanent provider failure
- Timeout after provider acceptance
- Duplicate delivery request
- Retry with the same delivery ID
- Expired message not delivered
- Sensitive payload purged

Do not claim production mail delivery is tested without actual authorized delivery evidence.

## Frontend component tests

Cover:

- Activation screen
- Code entry
- Paste/autofill behavior
- Resend
- Countdown/cooldown presentation
- Wrong email correction
- Loading
- Success
- Failure
- Expiration
- Offline state
- Accessibility
- RTL
- Navigation

## Web E2E tests

Execute full-stack tests where environment permits:

- Register
- Capture verification email through controlled mail receiver
- Read generated code
- Submit code through real UI
- Verify persisted account state
- Complete authorized activation
- Enter permitted app routes

Add representative tests for all four personas.

A browser test with mocked API responses is valuable but must be reported separately from genuine full-stack E2E.

## Mobile tests

Run Android/iOS automated tests where supported.

Otherwise produce explicit device smoke-test instructions and mark device execution as NOT_EXECUTED.

## Regression tests

Run targeted relevant A/B/C tests:

- Registration
- Account lifecycle
- Age eligibility
- Geographic access
- Terms acceptance
- Login
- Session refresh
- Welcome registration navigation
- Public urgent help
- Account deletion

Do not repeat the full earlier audits.

---

# 19. Security review

Perform targeted threat modeling for Phase D.

Consider:

- OTP brute force
- Offline code guessing
- Account enumeration
- Verification replay
- Account takeover through email correction
- Resend abuse
- Mail bombing
- Stolen pending-session tokens
- Race conditions
- Queue poisoning
- Duplicate mail dispatch
- Key compromise
- Exposed delivery secrets
- Expired-code acceptance
- Privilege escalation after activation
- Improper pending-account cleanup
- Cross-account access

Follow relevant OWASP guidance:

https://cheatsheetseries.owasp.org/cheatsheets/Email_Validation_and_Verification_Cheat_Sheet.html

https://cheatsheetseries.owasp.org/cheatsheets/Forgot_Password_Cheat_Sheet.html

https://pages.nist.gov/800-63-4/sp800-63b.html

Treat email ownership verification as distinct from MFA and identity proofing.

Do not claim independent age verification from email verification.

Do not claim absolute security.

---

# 20. Database safety and migration

Do not edit previously applied Flyway migrations.

Add new versioned migrations only when necessary.

Preserve:

- Existing users
- Existing sessions where valid
- Existing verified emails
- Existing Terms acceptance evidence
- Existing roles
- Existing professional/listener applications
- Existing social content
- Legacy activation/grace policy

Never delete real accounts as part of automated tests.

Test both:

1. Fresh database migration.
2. Upgrade of a populated compatible legacy database.

If the migration history differs from the supported baseline, stop destructive migration attempts and report the incompatibility.

Do not use Flyway repair as an automatic workaround.

Provide forward-recovery instructions when rollback is unsafe.

---

# 21. Deployment and external dependencies

Separate code completion from operational readiness.

Create an environment readiness matrix covering:

- Development
- Automated tests
- Staging
- Production

For each, report:

- Mail transport mode
- Sender identity
- Gateway configuration
- Encryption key source
- Retry policy
- Worker enabled/disabled state
- Rate-limit configuration
- Monitoring
- Cleanup retention
- Security restrictions

### Production release blockers

Do not declare Phase D production-ready if any required item remains absent:

- Functional real email delivery
- Secure sender-domain configuration
- Stable separate secret management
- Correct verification/activation logic
- Abuse prevention
- Operational delivery monitoring
- Approved pending-account retention policy
- Successful relevant integration tests
- Valid rollout procedure for existing users

If actual provider credentials are unavailable, build and test the integration boundary, but explicitly report the missing external prerequisite.

Do not invent a successful production email delivery.

---

# 22. Git implementation workflow

1. Determine the correct latest A/B/C baseline.
2. Create a new branch for Phase D.
3. Inspect existing implementation.
4. Produce a D-01 to D-12 gap matrix.
5. Preserve verified working features.
6. Implement only missing/defective functionality.
7. Add unit and integration tests.
8. Add frontend and E2E tests.
9. Run relevant regressions.
10. Document environment and deployment setup.
11. Commit focused changes.
12. Open a reviewable Pull Request.

Suggested branch:

`feature/phase-d-email-verification-activation`

Do not commit secrets.

Do not merge into main automatically.

Do not modify unrelated functionality.

Do not introduce a second mail worker, token service, or account lifecycle system without clear technical justification.

---

# 23. Mandatory final delivery report

Produce a complete table covering D-01 through D-12.

For every requirement, include:

- Requirement ID
- Initial implementation status
- Existing code reused
- Changes performed
- Changed files
- Database changes
- Tests added
- Test execution results
- Final status
- Remaining blockers

Final status values:

- COMPLETE_AND_TESTED
- IMPLEMENTED_NOT_FULLY_TESTED
- BLOCKED_EXTERNAL_DEPENDENCY
- NOT_IMPLEMENTED

Do not mark a requirement COMPLETE_AND_TESTED if mandatory tests were not executed successfully.

Also report:

### Code deliverables

- Backend changes
- Frontend changes
- Database migrations
- Environment configuration
- Mail transport improvements
- Retention/cleanup jobs

### Testing evidence

- Exact commands executed
- Test counts
- Passed / failed / skipped
- Full-stack E2E results
- Local mail capture results
- Real staging delivery results if available
- Native test status
- Security test evidence

### Operational readiness

- What is usable in development?
- What is verified in staging?
- What still blocks production?
- Which credentials/configuration must the owner supply?
- How should the operator monitor failed delivery?
- What happens when the mail gateway is unavailable?
- What happens to long-abandoned pending accounts?

### Pull Request

Provide:

- Branch
- PR URL
- Commit references
- CI status
- Migration notes
- Risk register
- Reviewer checklist

---

# 24. Definition of Done

Phase D is complete only when:

1. All twelve requirements have been inspected.
2. Correct A/B/C functionality has been reused.
3. No duplicate verification infrastructure exists.
4. Codes are generated securely.
5. Codes are protected at rest.
6. Expiration is server-enforced.
7. Code consumption is atomic and single-use.
8. The email delivery worker is reliable and testable.
9. Resend and guessing abuse are limited.
10. The activation UI is clear and accessible.
11. Valid verification completes the correct account transition.
12. Pending accounts cannot access protected social features.
13. Incorrect email addresses can be corrected through a secure workflow.
14. Abandoned pending accounts follow a safe documented cleanup policy.
15. All four test personas have executable coverage.
16. Relevant backend/frontend/integration tests have run.
17. Real-provider dependencies are clearly distinguished from mocks.
18. Previous functionality and existing users remain protected.
19. The code is documented and reviewable.
20. A Pull Request exists without automatic merge.

---

# FINAL EXECUTION DIRECTIVE

Do not stop after the audit.

First identify exactly which D-01 to D-12 requirements are already implemented and tested in the latest code.

Preserve those implementations.

For missing, incomplete, unsafe, or insufficiently tested requirements, implement the required changes and execute the appropriate tests.

Respect all Phase A, B, and C contracts.

Avoid unnecessary rewrites.

Do not claim email delivery works in production unless a real authorized test demonstrates it.

If external mail-provider configuration or operational approval is unavailable, implement all independent functionality, create safe configuration boundaries, and document the blocker.

**The target is a reliable, secure, testable, production-oriented email verification and account activation workflow, with truthful delivery reporting and no regressions to the existing GreenOcean system.**
