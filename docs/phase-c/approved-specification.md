# GreenOcean — Phase C: Registration Completion, Security & User Experience

## ROLE

You are the principal full-stack engineer, software architect, application security engineer, QA lead, and product implementation owner for GreenOcean.

Your mission is to fully audit, complete, improve, implement, and test **Phase C — Account Creation and Information Collection**, building directly on the already implemented Phase A and Phase B.

**Repository:** `mehrnaz-sarafraz/GreenOcean`

**Technology stack:**
- Java 21 / Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router / TypeScript
- Web, Android, and iOS

This is an existing production-oriented project, NOT a greenfield application.

Your primary responsibility is to preserve existing functionality and implement only what is genuinely missing, incomplete, defective, insecure, or inadequately tested in Phase C.

Do not stop after writing recommendations. Complete the executable work, run tests, fix failures, and prepare a reviewable Pull Request.

---

# 1. CRITICAL: Do not repeat completed Phase A or Phase B work

Phase A and Phase B have already been worked on.

Treat them as established implementation baselines.

**Do not conduct another full A/B audit.**

**Do not rewrite or redesign completed A/B functionality.**

**Do not recreate existing APIs, database tables, authentication services, geographic infrastructure, account states, policy management, or Welcome pages.**

### First, establish a verified baseline

Inspect:

- Current repository HEAD
- Current working branch
- Relevant merged commits
- Existing Phase A and Phase B pull requests, if available
- Existing implementation reports
- Existing test evidence
- Relevant API contracts
- Current migration version
- Existing CI status

Check whether the actual code contains the completed A/B implementation.

### Skip rule

If an A/B requirement:

1. Is already implemented;
2. Has valid test evidence;
3. Matches the current commit or relevant unchanged implementation;
4. Has no interface or behavioral conflict with Phase C;

Then mark it:

`VERIFIED_EXISTING_DEPENDENCY`

Do not reimplement it.

Do not rerun its entire original test suite solely to audit that feature again.

Only perform the limited compatibility and regression checks necessary for Phase C.

### When previous evidence is insufficient

If test evidence is missing, outdated, or does not match the relevant code:

- Do a targeted verification of the specific dependency.
- Avoid repeating a broad A/B architectural audit.
- Record what was checked and why.
- Do not assume a feature is correct merely because previous documentation claims it was implemented.

If a Phase C change affects an A/B contract, run the specific affected regression tests.

### Important distinction

Skipping repeated A/B implementation does NOT mean skipping relevant security or regression verification.

Preserve the previously approved architecture.

---

# 2. Approved product decisions inherited from Phase A

These decisions are binding and must not be changed in Phase C.

## Age eligibility

- Initial interactive social access is limited to eligible adults aged 18+.
- Public, curated educational and crisis resources remain available without registration.
- The future interactive teen experience for ages 13–17 is out of scope.
- The server is authoritative for adult eligibility.
- Self-declaration must not be misrepresented as independent age verification.
- Any jurisdiction-specific stronger age-assurance requirement must remain enforceable.

## Geographic eligibility

- Use the geographic catalog and service-region architecture established in Phase A.
- Do not implement another country database or duplicate country enums.
- Registration must comply with the actual supported-market policy.
- A country existing in the catalog does not automatically mean interactive services are available there.
- Do not invent supported countries.
- Do not require the user's city for general registration.

## Account roles

- Every newly registered user receives the base USER/MEMBER role.
- Professional privileges require a separate professional application and verification workflow.
- Listener privileges require a separate approval workflow.
- Admin privileges cannot be requested through public registration.
- No user-supplied registration field may grant elevated permissions.

## Account lifecycle

Respect the current Phase A account state machine, including relevant states such as:

- PENDING
- ACTIVE
- SUSPENDED
- DELETED

Do not invent another parallel account-state system.

New users must not obtain protected social access before satisfying required activation conditions.

## Progressive profiling

Only collect information required for account creation, authentication, eligibility, and necessary agreement acceptance.

Defer optional profile information.

## Privacy

Preserve the privacy-first defaults implemented in Phase A.

Do not weaken them to make registration easier.

## Policy acceptance

Use the current versioned policy and acceptance infrastructure.

Do not create a duplicate acceptance table or a second versioning system.

## Phase B

Preserve:

- Welcome
- Sign In entry point
- Create Account entry point
- Public urgent help
- Public informational pages
- Trust and service-boundary messaging

Do not redesign Welcome unless a specific navigation defect prevents registration.

---

# 3. Phase C requirements — authoritative checklist

You must inspect and resolve ALL nineteen requirements.

For every requirement, classify its actual state and take the corresponding action.

Use the following statuses:

- COMPLETE_VERIFIED — already implemented and adequately tested
- IMPLEMENTED_NEEDS_TESTS — implementation exists but coverage is incomplete
- PARTIALLY_IMPLEMENTED — implementation exists but behavior is incomplete
- DEFECTIVE — implementation exists but fails the intended contract
- MISSING — no implementation exists
- BLOCKED_EXTERNAL — requires a documented external dependency

The requirements are:

## C-01 — Email input and normalization

Priority: P0

Inspect the existing email registration implementation.

Ensure:

- Valid email input is accepted.
- Leading and trailing whitespace is handled.
- Normalization follows the approved account identity policy.
- Unnecessary destructive transformations are avoided.
- Email validation is consistent between frontend and backend.
- Case-insensitive account uniqueness is enforced according to the existing database contract.
- Invalid, oversized, or malformed input fails safely.
- Duplicate account creation is prevented, including under concurrency.
- Account enumeration is minimized.

Do not introduce arbitrary normalization that could change the meaning of valid email addresses.

Backend and database enforcement are authoritative.

Reuse Phase A validation if already correct.

---

## C-02 — Password creation and usability

Priority: P0

Inspect the password implementation from Phase A.

Ensure:

- Frontend and backend enforce the same password policy.
- The implemented policy follows the approved security design.
- Long passphrases and password-manager-generated passwords are supported.
- Users can paste passwords.
- Password visibility can be toggled accessibly.
- Password fields use appropriate autofill and password-manager attributes.
- Weak or compromised passwords are handled according to the implemented security policy.
- Raw passwords are not logged or persisted.
- Existing valid accounts remain compatible with hashing upgrades.

Do not introduce contradictory password requirements.

Do not require arbitrary character-class combinations if prohibited by the adopted policy.

Do not automatically replace the hashing implementation if Phase A already implemented an appropriate solution.

---

## C-03 — Password confirmation

Priority: P1

Evaluate whether the current confirmation field contributes to usability.

If retained:

- Confirm the password locally.
- Provide clear mismatch feedback.
- Do not send the confirmation field to the backend.
- Do not store or log confirmation values.
- Support pasted passwords and password managers.
- Provide an accessible show/hide option.

If Phase A or existing UX evidence supports removing the confirmation field in favor of a visibility toggle, preserve that decision and document the rationale.

Do not add redundant steps merely to satisfy an old UI checklist.

---

## C-04 — Server-side age eligibility

Priority: P0

Reuse Phase A age eligibility enforcement.

Verify that:

- Direct API requests cannot bypass eligibility rules.
- Modified request bodies cannot grant adult eligibility.
- Unsupported age-assurance states are not accepted.
- Underage users cannot obtain interactive social access.
- Public curated resources remain accessible without an account.
- The frontend displays understandable eligibility messaging.

Do not build another age-verification system.

If jurisdiction-specific verification remains externally blocked, preserve safe production defaults and document the limitation.

---

## C-05 — Geographic eligibility

Priority: P0

Reuse the Phase A geographic and service-availability system.

Ensure:

- Country selection is sourced from the existing catalog where applicable.
- The frontend uses correct country codes.
- Unsupported markets do not accidentally receive interactive-service access.
- Geographic eligibility is independently enforced by the backend.
- The country is not inferred exclusively from an IP address.
- Invalid or inconsistent geographic inputs are handled safely.
- City selection is not mandatory during general registration.

Do not hardcode all countries or cities in the registration component.

---

## C-06 — Optional public username

Priority: P1

The registration process must not require the user to invent a public username immediately.

If the database requires one:

- Generate a safe temporary identifier.
- Ensure uniqueness.
- Avoid deriving it from email.
- Avoid exposing sensitive data.
- Allow later customization.
- Handle collisions atomically and correctly.

Existing username-update functionality must be preserved.

Do not change public usernames of existing users unnecessarily.

---

## C-07 — Optional display name

Priority: P1

Do not require a real name or manually chosen display name during account registration.

Use an appropriate temporary display name when required by existing schema constraints.

Ensure:

- It does not expose the email address.
- It does not imply the user is verified.
- It can be changed later.
- Unicode and Persian names are handled appropriately.
- Existing display names remain unchanged during migration.

---

## C-08 — Remove city from mandatory registration

Priority: P0

The user must be able to register without providing a city.

City may be requested later for genuinely location-dependent services.

Inspect:

- Frontend registration fields
- Registration DTO
- Service validation
- Entity constraints
- Database constraints
- Profile responses
- Existing integration tests

Make only the necessary compatible changes.

Do not introduce a dummy city value such as "Unknown" merely to satisfy outdated validation.

---

## C-09 — Age data minimization

Priority: P0

Use the Phase A eligibility model instead of requiring birthYear solely because the original registration form did so.

Requirements:

- Do not collect an unnecessary full birthdate.
- Do not preserve outdated mandatory birth-year requirements.
- Respect the approved age-assurance method.
- Preserve necessary eligibility audit evidence.
- Do not confuse age self-attestation with verified age.
- Protect age-related information from public exposure.
- Preserve legitimate existing records during migration.

Do not invent an independent age-verification requirement without reference to the approved product policy and launch jurisdiction.

---

## C-10 — Remove initial role selection

Priority: P1

Registration should not ask:

- Are you a Member?
- Are you a Professional?
- Do you want Admin access?

All eligible users register as the base account role.

The professional application journey remains available after account activation.

Ensure that forged `intendedRole`, `roles`, `isAdmin`, or similar fields cannot escalate privileges.

Preserve Phase A authorization design.

---

## C-11 — Terms acceptance

Priority: P0

Reuse Phase A server-side Terms acceptance functionality.

Verify:

- User action is explicit.
- The valid current Terms version is checked by the backend.
- Registration cannot bypass required acceptance.
- The interaction is accessible.
- The Terms document can be opened before accepting.
- Privacy information is presented without wrongly bundling all optional consent.
- Errors are understandable.

Do not create another Terms acceptance system.

---

## C-12 — Policy version acceptance evidence

Priority: P0

Verify the existing Phase A policy versioning and acceptance mechanism.

Ensure it records appropriate evidence of:

- User identity
- Document identity
- Accepted version
- Server-side timestamp
- Relevant audit metadata

Do not trust a client-provided policy version without server-side verification.

If the policy changes between opening registration and submission, show a recoverable explanation and allow the user to review the new version.

Reuse existing database structures.

---

## C-13 — Field-level form validation and API errors

Priority: P0

This is a major Phase C frontend responsibility.

Audit and complete validation behavior for every registration input.

Requirements:

- Clear field-specific feedback.
- Consistent validation between frontend and backend.
- Structured error codes where available.
- No generic "malformed request" for ordinary correctable input mistakes.
- Appropriate errors for eligibility and policy-version problems.
- No leakage of account existence or security internals.
- Correct focus handling after validation fails.
- Screen-reader announcements.
- Error state clears or updates when the user corrects input.
- No silent submission failure.

Distinguish safe, actionable validation errors from security-sensitive authentication and enumeration errors.

Do not reveal whether another person's email address exists merely to provide a more specific error.

---

## C-14 — Duplicate account protection

Priority: P0

Inspect existing uniqueness constraints and registration behavior.

Ensure:

- A second account cannot be created using the same normalized account email.
- Concurrent requests cannot bypass uniqueness.
- Frontend duplicate submission is controlled.
- The backend handles uniqueness conflicts gracefully.
- Public responses do not unnecessarily reveal whether an email is registered.
- Existing legitimate accounts remain unaffected.

Do not remove the database uniqueness constraint.

---

## C-15 — Registration rate limiting and abuse prevention

Priority: P0

Reuse Phase A abuse-prevention infrastructure.

Verify registration-specific protections against:

- Excessive signup attempts
- Automated account creation
- Email abuse
- Repeated failed requests
- Enumeration
- Application-layer resource exhaustion

Ensure:

- The backend is protected even if the frontend is bypassed.
- Limits can be configured.
- Responses are safe and consistent.
- Retry behavior is understandable.
- Legitimate shared-network users are not unnecessarily blocked.
- Security logging excludes passwords and sensitive payloads.

Do not introduce CAPTCHA without considering usability, accessibility, and demonstrated need.

---

## C-16 — Password storage security

Priority: P0

Inspect the actual password hashing implementation.

Preserve secure hashing and existing-password compatibility.

Verify:

- No plaintext passwords in storage.
- No password leakage into logs.
- Appropriate hashing configuration.
- Compatibility with approved password length policy.
- Old users can still authenticate where appropriate.
- Migration strategy exists for any necessary hashing upgrade.
- Sensitive temporary values are not persisted.

Do not unnecessarily force all existing users to reset passwords.

Do not make unsupported security claims.

---

## C-17 — Pending account creation and activation

Priority: P0

Reuse the existing Phase A account lifecycle.

Ensure:

- A newly registered account enters the correct pending state.
- Verification is required before protected social access.
- The user receives a clear next action.
- Email verification activates the account only when all necessary eligibility conditions are met.
- Unverified accounts cannot access restricted APIs.
- Invalid, expired, or replayed verification credentials fail safely.
- Resend verification remains usable and abuse-resistant.
- Temporary accounts follow the approved retention policy.

Do not create a second account-lifecycle service.

Do not enforce a verification step that is impossible to complete because email delivery is not configured.

If delivery is not operational in a particular environment, use an explicit secure test adapter for testing and fail closed in production.

---

## C-18 — Loading, retry, interruption, and recovery UX

Priority: P1

This is a primary Phase C responsibility.

Implement or complete:

### Loading states

- Clear pending state after submitting registration.
- Accessible progress feedback.
- Disable accidental duplicate submissions.
- Avoid indefinite spinner states.

### Network errors

- Offline handling.
- Request timeout handling.
- Safe retry messaging.
- Avoid losing all entered non-sensitive form data unnecessarily.
- Do not persist passwords in insecure long-term storage.

### Duplicate clicks

- Prevent multiple submissions from one screen.
- Account creation must remain safe if duplicate requests reach the backend.
- Handle the case where an account was created but the response was lost.

### Interrupted registration

- Reopen verification after navigating away.
- Explain what happens if the user closes the app.
- Recover appropriately after restarting the app.
- Preserve required security boundaries.

### Submission outcomes

Clearly distinguish:

- Validation failure
- Eligibility failure
- Verification required
- Rate limit
- Network failure
- Unexpected server failure

Avoid revealing account existence where security policy requires generic responses.

---

## C-19 — Minimal registration end-to-end contract

Priority: P0

Ensure the entire registration workflow follows one consistent Phase A contract.

This includes:

- Frontend form
- TypeScript request types
- API client
- Registration controller
- DTO validation
- Registration service
- Account entity
- Profile creation
- Preferences initialization
- Policy acceptance
- Age eligibility
- Geographic eligibility
- Database constraints
- Pending account state
- Email verification transition

The flow must work without unnecessary profile fields.

Preserve existing users and profiles.

Do not implement inconsistent fallback values merely to avoid modifying obsolete schema assumptions.

---

# 4. Expected final user journey

The intended user journey is:

WELCOME
→ CREATE ACCOUNT
→ ELIGIBILITY AND REQUIRED INFORMATION
→ SUBMIT REGISTRATION
→ PENDING EMAIL VERIFICATION
→ VERIFY EMAIL
→ ACCOUNT ACTIVATION
→ OPTIONAL ONBOARDING
→ PERMITTED SUPPORT EXPERIENCE

This is a conceptual flow, not a requirement to create a separate UI screen for every step.

Prefer the simplest usable design.

Do not introduce unnecessary friction.

Users should not be forced to provide mental health history or profile details to create an account.

All protected actions must remain subject to backend authorization.

---

# 5. Mandatory four-persona testing model

Every Phase C feature must have coverage across the following behavioral classes when applicable.

## Persona 1 — HAPPY PATH

A typical eligible adult follows the intended process.

Test:

1. Visit Welcome.
2. Open registration.
3. Provide minimal valid information.
4. Satisfy eligibility requirements.
5. Accept valid Terms.
6. Submit registration.
7. Receive the appropriate pending state.
8. Complete email verification.
9. Activate the account.
10. Sign in or continue through the authorized activation flow.
11. Start or skip optional onboarding.
12. Access permitted social features.
13. Sign out and sign in again.

Verify frontend navigation, API behavior, persisted database state, and session permissions.

---

## Persona 2 — CREATIVE USER

A legitimate but unconventional user.

Test:

- Very long valid password.
- Password manager autofill.
- Pasted password.
- Unicode and Persian display names.
- Mixed RTL and LTR content.
- International email input within the supported email policy.
- Empty optional profile fields.
- No city.
- No manually chosen username.
- Browser reload during registration.
- Mobile app restart during verification.
- Multiple device usage.
- Change of country when permitted.
- Later customization of username and profile.
- Professional application after basic account activation.

Ensure valid unconventional behavior is supported rather than incorrectly rejected.

---

## Persona 3 — CONFUSED USER

A user who makes mistakes or loses context.

Test:

- Empty required field.
- Invalid email format.
- Password that fails policy.
- Mistyped password confirmation, if the field exists.
- Accidental repeated submission.
- Closing the verification screen.
- Using an expired verification credential.
- Reopening the app after several hours.
- Slow network.
- Network unavailable.
- Server timeout.
- Request succeeds but response is lost.
- Terms version changes during registration.
- Unsupported country selection.
- Misunderstanding the age eligibility question.
- Repeatedly pressing Back and Continue.
- Entering a previously used email.
- Returning to login before verification is complete.

Expected behavior:

The user should receive understandable, safe, recoverable feedback.

Do not create duplicate accounts or disclose sensitive information.

---

## Persona 4 — MALICIOUS USER

An attacker attempting to violate the security model.

Test:

- Forged adult eligibility.
- Attempts to bypass age checks through direct API calls.
- Invalid age-assurance state.
- Unsupported country.
- Forged `ADMIN` or `PROFESSIONAL` role.
- Extra unexpected JSON fields.
- Missing Terms acceptance.
- Forged policy version.
- Email enumeration.
- Repeated registration requests.
- Excessive verification requests.
- Malformed JSON.
- Oversized inputs.
- SQL injection attempts using safely scoped test data.
- XSS-like payloads in permitted public fields.
- Duplicate concurrent account creation.
- Attempted access to social APIs while PENDING.
- Reuse of expired or consumed verification credentials.
- Attempted use of another user's verification token.
- Attempted privilege escalation through registration parameters.

All tests must use isolated test accounts and non-production environments.

Verify that rejected operations did not cause unauthorized database mutations.

---

# 6. Required automated tests

## Unit tests

Create or update tests for:

- Registration validation
- Email normalization
- Password policy
- Age eligibility
- Geographic eligibility
- Account-role assignment
- Terms acceptance
- Account state transitions
- Temporary profile identifiers
- Privacy defaults
- Safe error mapping

## Backend integration tests

Use isolated PostgreSQL test infrastructure.

Cover:

- Successful registration
- Minimal payload
- Pending account
- Email verification
- Account activation
- Duplicate email
- Concurrent duplicate requests
- Missing required agreements
- Invalid policy version
- Underage access rejection
- Unsupported geographic access
- Forged roles
- Password storage
- Existing-user compatibility
- Pending-user authorization
- Malformed and oversized input

Verify both HTTP responses and database effects.

## Frontend component tests

Cover:

- Input validation
- Password visibility
- Password confirmation behavior
- Policy links
- Consent controls
- Country selection when required
- Eligibility feedback
- Loading indicators
- Duplicate submission prevention
- Structured API error handling
- Offline and timeout states
- Verification navigation
- RTL and accessibility behavior

## Web E2E tests

Use the existing framework or adopt a suitable maintainable one where necessary.

Test the complete registration flow using controlled development/test email delivery.

Include representative Happy, Creative, Confused, and Malicious flows.

Do not use real external email accounts or production data.

## Native validation

Run available automated native tests.

If native device testing is unavailable, provide explicit Android and iOS smoke-test procedures and mark them NOT EXECUTED.

## Regression tests

Run only the relevant existing A/B tests affected by Phase C.

Verify:

- Welcome still routes correctly.
- Public urgent help remains accessible.
- Existing policy pages still work.
- Phase A eligibility remains enforced.
- Existing professional and listener privileges remain protected.
- Existing account sessions remain secure.
- No private resources become public.

Avoid rerunning the entire A/B audit.

---

# 7. Database safety

Follow these mandatory rules:

- Do not change previously applied Flyway migration files.
- Use new additive migrations only where needed.
- Never reset production or user databases.
- Never delete legitimate user accounts during testing.
- Do not overwrite existing usernames or display names without justification.
- Preserve professional applications, roles, preferences, and social data.
- Test schema changes on fresh and populated test databases.
- Add database constraints where they are needed.
- Handle concurrent registration safely.
- Verify backward compatibility with existing users.
- Document migration risk and forward-recovery strategy.

Do not create new tables if an existing Phase A structure already satisfies the requirement.

---

# 8. Frontend quality requirements

Ensure the registration experience is:

- Minimal
- Trustworthy
- Accessible
- Responsive
- Understandable
- Recoverable
- Consistent with GreenOcean design tokens
- Compatible with Expo Web, Android, and iOS

Test representative small and large screens.

Follow supported localization conventions.

Support RTL where implemented.

Ensure email and password fields behave correctly in RTL environments.

Test screen-reader labels, keyboard behavior, text scaling, focus handling, contrast, and validation announcements.

Do not build a second design system.

---

# 9. Engineering workflow

## Step 1 — Identify the verified baseline

Find the latest correct code containing Phase A and Phase B.

Do not assume default main contains every completed change.

Avoid starting from an obsolete commit.

## Step 2 — Build the Phase C gap matrix

For every C-01 through C-19, report:

- Requirement ID
- Actual status
- Existing implementation
- Evidence and tests
- Missing or defective behavior
- Files needing changes
- Required backend changes
- Required frontend changes
- Database changes
- Security considerations

Do not produce a full A/B gap matrix again.

## Step 3 — Plan only the missing changes

Group missing work into small implementation packages.

Prioritize P0 issues.

Avoid code changes for already verified requirements.

## Step 4 — Implement

Perform the actual code changes.

Reuse and extend existing services.

Do not create redundant implementations.

## Step 5 — Test

Write and execute all necessary new Phase C tests.

Fix discovered regressions.

Use all four persona categories.

## Step 6 — Verify compatibility

Perform narrow, relevant A/B regression checks.

Do not weaken previous requirements.

## Step 7 — Deliver

Commit work to a dedicated branch and open a reviewable Pull Request.

Do not merge into main automatically.

---

# 10. Git workflow

Create a new feature branch based on the latest correct Phase A/B baseline.

Suggested name:

`feature/phase-c-registration-completion`

Before branching, check whether Phase A or B changes are in main or an existing feature branch.

Do not overwrite previous work.

Do not force-push.

Use focused commits.

Do not commit secrets, API keys, tokens, personal records, or database dumps.

Keep changes within the Phase C scope.

---

# 11. Required final report

Provide the following outputs.

## A. Complete Phase C implementation matrix

Include all nineteen requirements:

C-01 through C-19.

For each, report:

- Initial status
- Work performed
- Final status
- Changed files
- Relevant tests
- Remaining limitations

Final statuses:

- COMPLETE_AND_TESTED
- IMPLEMENTED_NOT_FULLY_TESTED
- BLOCKED_EXTERNAL_DEPENDENCY
- NOT_IMPLEMENTED

Do not mark a requirement complete if required tests were not executed successfully.

## B. Reused Phase A/B functionality

List only the relevant dependencies reused.

Explain what was preserved.

Do not repeat the complete Phase A/B audits.

## C. Frontend deliverables

List actual registration, validation, recovery, and navigation improvements.

## D. Backend deliverables

List the actual service, DTO, validation, security, and database changes.

## E. Automated test results

Report:

- Commands executed
- Unit tests
- Backend integration tests
- Frontend component tests
- E2E tests
- Migration tests
- Relevant security tests
- Passed/failed/skipped results

Clearly identify anything NOT EXECUTED.

## F. Four-persona coverage matrix

For each persona, list:

- Test cases implemented
- Relevant requirement IDs
- Results
- Remaining gaps

## G. Security verification

Confirm the implementation did not introduce:

- Age eligibility bypass
- Privilege escalation
- Duplicate account creation
- Account enumeration
- Authentication bypass
- Terms acceptance bypass
- Password leakage
- Unauthorized access to protected social resources
- Unsafe handling of pending accounts

Base these conclusions on actual tests or inspection evidence.

Do not claim an absolute guarantee of security.

## H. Compatibility and deployment

Document:

- Schema migrations
- Existing-user handling
- Environment configuration
- Email dependencies
- Rollout order
- Known deployment risks

## I. Pull Request

Provide:

- Branch name
- PR URL
- Summary
- Commit references
- Test evidence
- CI status
- Outstanding blockers

---

# 12. Definition of Done

Phase C is complete when:

1. All nineteen C requirements have been evaluated against actual code.
2. Already completed Phase A/B implementations have been reused.
3. No unnecessary duplicate systems have been introduced.
4. Registration collects only justified necessary information.
5. Email and password behavior matches the existing secure account contract.
6. Age and geographic eligibility remain server-enforced.
7. Public registration cannot grant elevated roles.
8. Terms acceptance is valid and recorded.
9. New accounts follow the correct pending/activation lifecycle.
10. Optional profile fields are no longer mandatory.
11. Validation errors are understandable and safe.
12. Loading, retries, interruptions, and duplicate submissions are handled correctly.
13. Existing account data and roles remain intact.
14. Happy, Creative, Confused, and Malicious test cases are implemented and executed wherever technically possible.
15. Actual test evidence is documented.
16. The registration experience is usable across supported platforms.
17. Relevant A/B regression tests pass.
18. A reviewable Pull Request exists.

---

# FINAL EXECUTION DIRECTIVE

**Your mission is to complete Phase C, not to repeat Phase A or Phase B.**

Begin by establishing which Phase C requirements are already satisfied by the existing implementation.

Do not reimplement completed functionality.

Do not conduct redundant full audits of previous phases.

For requirements that are missing, defective, partially implemented, or inadequately tested, perform the necessary implementation and testing.

If a requirement is blocked by an external dependency, document it, preserve secure defaults, and continue independent work.

Do not stop at the audit or implementation plan.

Proceed to actual code changes, automated tests, relevant regression verification, documentation, commits, and a Pull Request.

**The final outcome must be a secure, minimal, accessible, reliable, and fully tested registration experience that builds upon the existing Phase A and Phase B architecture.**
