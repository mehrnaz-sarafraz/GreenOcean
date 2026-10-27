# GreenOcean — Phase K
## Final Integration, Security Verification, Regression Testing and Phase 1 Closure

### Complete End-to-End Validation, Targeted Fixes, Operational Readiness and Go/No-Go Assessment

---

# 1. MISSION

You are the principal release engineer, full-stack integration engineer, software architect, application security engineer, QA lead, database migration engineer, and technical release-readiness owner for GreenOcean.

Repository:

`mehrnaz-sarafraz/GreenOcean`

Technology stack:

- Java 21 / Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router / TypeScript
- Web / Android / iOS

Your mission is to complete:

**Phase K — Final Integration, Testing and Closure of GreenOcean Phase 1.**

Phase 1 consists of:

- A — Identity, Eligibility and Privacy Foundations
- B — Public Welcome and Trust
- C — Minimal Registration
- D — Email Verification
- E — Authentication and Account Recovery
- F — Session and Device Security
- G — Initial Profile and Privacy
- H — Progressive Onboarding
- I — First Real Support
- J — Legal Transparency, Consent and Data Governance
- K — Final Integration and Release Readiness

The approved A–J phases should already have their own implementations and relevant tests.

**Do not rebuild the previous phases.**

This phase must:

1. Establish the actual integrated baseline.
2. Verify compatibility across A–J.
3. Run the final applicable automated and full-stack tests.
4. Find real integration and release-blocking defects.
5. Fix only demonstrated defects.
6. Rerun the affected and full relevant regression suites.
7. Verify migration and operational readiness.
8. Record external blockers honestly.
9. Deliver a reviewable Phase K Pull Request.
10. Produce a defensible Phase 1 closure decision.

### Important distinction

Finishing Phase K code does not automatically authorize production deployment.

Technical completion and production launch authorization must be separately evaluated.

---

# 2. Mandatory initial baseline verification

The original A–J implementations may reside in a chain of stacked branches and Pull Requests.

### First inspect

- Current main
- All active Phase A–J branches
- Merged and unmerged PRs
- Their actual commit SHAs
- Dependency relationships
- CI results
- Delivery reports
- Migration history
- Open blocking defects
- Owner decisions
- Legal approvals and pending reviews
- Operational dependencies

### Critical requirement

Find the latest correct integration baseline containing all intended A–J implementations.

Do not assume the code is integrated merely because every phase has a Pull Request.

If Phase I or J is incomplete, identify the dependency.

Do not invent missing implementations.

Do not silently expand K into a complete unrelated product-development phase.

Continue independent K validation work where safe.

Request a product-owner decision if the incomplete prerequisite requires a change of approved scope.

### Proposed branch

`feature/phase-k-final-integration-readiness`

Do not automatically merge earlier PRs into main.

Review stacked branch dependencies and merge-readiness explicitly.

---

# 3. Scope: validate and repair, do not rebuild

### In scope

- Source integration
- Cross-module contracts
- Authorization consistency
- End-to-end user journeys
- Database migrations
- Test automation
- Integration defects
- Deployment compatibility
- Security regressions
- Privacy regressions
- Native platform validation
- Performance and reliability verification
- Operational readiness documentation
- Phase 1 closure reporting

### Out of scope

- Rebuilding completed product workflows
- Adding unrelated Phase 2 features
- New AI-based mental-health profiling
- New unsupported clinical features
- New persistent listener queues
- New voice/video support
- New countries without approval
- New identity providers without need
- New marketing analytics
- Automatic production deployment
- Unapproved data migrations
- Fabricated staff, legal, or service approvals

### The no-duplication principle

For every previously implemented requirement:

- Reuse working code.
- Reuse existing tests.
- Add tests only when a necessary verification gap exists.
- Fix actual failures.
- Do not rewrite good code for stylistic reasons.

---

# 4. Mandatory Phase 1 integration matrix

Create:

`docs/phase-k/integration-matrix.md`

For every phase A–J, identify:

- Latest implementation commit
- PR/dependency
- Relevant feature entry points
- Backend API dependencies
- Database dependencies
- Security dependencies
- Previously executed tests
- Actual current integration tests
- Outstanding defects
- Operational blockers
- Final state

### Allowed classifications

- VERIFIED_INTEGRATED
- VERIFIED_WITH_EXTERNAL_BLOCKER
- NEEDS_TARGETED_TEST
- INTEGRATION_DEFECT
- INCOMPLETE_PREREQUISITE
- OWNER_DECISION_REQUIRED
- BLOCKED_OPERATIONAL
- NOT_VERIFIED

Do not mark an integration complete solely because an old phase-specific test passed.

---

# 5. K-01 — Full account journey

Priority: CRITICAL P0

Test the complete flow:

PUBLIC WELCOME
→ REGISTRATION
→ AGE/REGION ELIGIBILITY
→ TERMS ACCEPTANCE
→ PENDING ACCOUNT
→ EMAIL VERIFICATION
→ ACTIVE ACCOUNT
→ LOGIN
→ ONBOARDING
→ PROFILE PRIVACY
→ SUPPORT DISCOVERY
→ FIRST PERMITTED SUPPORT ACTION
→ LOGOUT

### Verify

- The correct user account is used throughout.
- Eligibility gates cannot be bypassed.
- Terms version and acceptance are valid.
- Verification activates the correct account.
- Login behavior respects account status.
- Session tokens are valid and correctly scoped.
- Onboarding can be skipped.
- The app does not enter redirect loops.
- Privacy defaults remain active.
- Support routing matches permitted services.
- No unsupported region becomes enabled.

Execute as a true full-stack journey with a real backend and disposable PostgreSQL.

Controlled test email delivery is appropriate for automated tests.

Do not claim real-provider delivery unless separately exercised.

---

# 6. K-02 — Authentication, recovery and device security

Priority: CRITICAL P0

Verify the approved E/F security policy.

### Passwords

- Approved minimum and maximum lengths
- Unicode handling
- Legacy password hash compatibility
- Password change
- Password recovery
- One-use recovery tokens
- Correct session invalidation
- Abuse controls

### Sessions

- Access token expiration
- Refresh token rotation
- Refresh replay detection
- Server-side session revocation
- Logout current session
- Logout all devices
- Absolute expiration
- Idle expiration
- Web cookie protection
- Mobile secure storage
- Cross-tab behavior
- Cross-device behavior
- Account switching
- Sensitive cache wiping
- Recent authentication requirements

### Privileged access

- Server-enforced strong MFA
- Correct WebAuthn origin/RP policy
- Correct authenticator ownership
- No password-only privileged bypass
- Safe loss/recovery procedures
- Fail-closed behavior when external production prerequisites are unavailable

### External blockers

Real hardware, origin, trusted operational provisioning, and recovery approval must be separately verified before claiming live privileged readiness.

Do not weaken privileged controls to make staff workflows pass.

---

# 7. K-03 — Privacy, content visibility and messaging

Priority: CRITICAL P0

Revalidate Phase G privacy under the final integrated code.

### Profile

- Private default
- Public identity boundaries
- Hidden email
- Hidden location
- Hidden birth year
- Hidden private support preferences
- Correct follow access
- Correct block restrictions

### Posts

- Explicit audience
- Historical audience preservation
- Public/private profile transitions
- Community membership enforcement
- Search
- Direct post links
- Notifications
- Comments
- Anonymous posting
- Pagination and cached results

### Messaging

- Ordinary direct messages
- Existing conversations
- Listener sessions
- Professional conversations
- Community/group conversations
- Blocking after conversation creation
- Authorization after account suspension
- Session completion effects
- Access via forged conversation IDs

### Critical regression

Changing a profile from private to public must never unexpectedly expose historical restricted posts.

A closed listener session must not silently become a reusable normal direct-message channel.

The backend must enforce these rules even when the frontend is bypassed.

---

# 8. K-04 — First real support journey

Priority: CRITICAL P0

Test the actual Phase I model.

### Approved Model A

- Live text support
- Real listener acceptance
- No persistent waiting queue
- Explicit availability
- Server-authoritative expiration
- Transactionally enforced capacity
- No unauthorized conversation creation

### Full positive journey

1. Eligible member chooses listener support.
2. Available approved listener is discovered.
3. Member submits a request.
4. Listener receives it.
5. Listener accepts before expiration.
6. Member activates/enters the accepted session.
7. Both participants exchange messages.
8. Member or listener ends the session.
9. Further messages are rejected.
10. Capacity is released.
11. Previous session history follows approved privacy rules.

### Negative journeys

- No listener available
- Listener offline
- Listener busy
- Listener declines
- Member cancels
- Request expires
- Simultaneous requests
- Account becomes suspended
- Listener privilege revoked
- User blocks listener
- Connection drops
- Browser reloads
- Client retries acceptance
- Old client invokes legacy matching API
- Stale client attempts to send after completion

### Human operations

Do not claim that automated matching tests prove real trained listeners, real supervisors, or 24/7 coverage.

Verify actual operational preparedness separately.

---

# 9. K-05 — Legal, privacy and consent consistency

Priority: P0

Verify Phase J's implemented mechanisms.

### Required checks

- Correct published Terms version
- Safe unavailable-policy behavior
- Accurate Privacy Notice
- Correct consent separation
- Optional authorization defaults
- Consent withdrawal where applicable
- Sensitive-topic collection remains gated unless approved
- Data export authorization
- Data deletion
- Retention controls
- Report handling
- Moderation appeals
- Third-party data disclosures
- Appropriate user-facing support limitations
- No privacy-dark-pattern regression

### Compliance boundary

A technical test can establish correct application behavior against an approved contract.

It cannot independently prove that the contract or lawfully selected processing basis is legally sufficient.

Keep legal and technical evidence separately identified.

Do not silently authorize a geographic or regulated service.

---

# 10. K-06 — Database migration integration

Priority: CRITICAL P0

Validate the entire actual Flyway chain.

### Environments

1. Empty fresh PostgreSQL database.
2. Database upgraded from a representative legacy schema.
3. Database containing representative existing users and content.
4. Database containing historical posts/privacy settings.
5. Database containing sessions and recovery records.
6. Database containing onboarding progress.
7. Database containing existing listener conversations.
8. Database containing moderation reports and account deletion state.

Use only synthetic or appropriately sanitized isolated data.

### Requirements

- No checksum conflicts.
- No migration-order conflicts.
- No modified applied migrations.
- No unsafe default broadened access.
- No duplicate role grants.
- No lost password hashes.
- No restored revoked sessions.
- No unexpected data deletion.
- No invalid listener session state.
- No invalid foreign keys.
- No unbounded migration locking without review.
- No accidental activation of production services.

### Rollback/forward recovery

Document whether older application binaries remain compatible with new schemas.

Do not assume rolling back old application code is safe after a migration.

Prefer reviewed forward recovery for irreversible changes.

Test backup restoration and recovery procedures where environment access permits.

Do not mutate production data during tests.

---

# 11. K-07 — Four-persona full journey coverage

Priority: P0

Execute the established GreenOcean persona methodology across the integrated application.

## Happy Path

- New eligible user.
- Registers and verifies email.
- Logs in.
- Completes onboarding.
- Adjusts privacy.
- Uses an authorized support pathway.
- Completes a live listener session where a synthetic available listener exists.
- Reports or blocks when needed.
- Changes password.
- Exports data.
- Deletes account through the authorized workflow.

## Creative User

- Persian and RTL.
- Unicode input.
- Multiple devices.
- Multiple concurrent browser tabs.
- Empty onboarding interests.
- Changing support style.
- Switching profile visibility.
- Multiple post audiences.
- Interrupted listener request.
- Session restoration.
- Large-text accessibility.
- Network transitions.

## Confused User

- Incorrect registration field.
- Duplicate email.
- Expired OTP.
- Lost session.
- Password recovery.
- Skipped onboarding.
- Confusing privacy choices.
- No available listener.
- Expired support request.
- Mistaken support/treatment assumption.
- Partial data export.
- Cancellation during loading.
- Account deletion error recovery.

## Malicious User

- Underage/ineligible access attempts.
- Forged role.
- Unauthorized country/service access.
- Account enumeration.
- Recovery token replay.
- Refresh token replay.
- MFA bypass.
- Foreign profile update.
- Private post disclosure.
- Cross-account support request.
- Listener spoofing.
- Listener double-acceptance.
- Forged conversation ID.
- Message sending after session end.
- Block evasion.
- Report and appeal ownership bypass.
- Sensitive preference extraction.
- Consent forgery.
- Privileged policy publication.
- Unauthorized export/deletion.
- Malicious oversized API inputs.

Use only synthetic data in isolated environments.

Never run destructive security tests against real users or production systems.

---

# 12. K-08 — Automated test pipeline

Priority: CRITICAL P0

Run the actual applicable full regression suite on the integrated final commit.

### Backend

- Java unit tests
- Spring Boot integration tests
- PostgreSQL integration tests
- Migration tests
- Authorization tests
- Authentication tests
- MFA tests
- Listener race-condition tests
- Moderation tests
- Data export and deletion tests
- Retention and cleanup tests
- Policy and consent tests

### Frontend

- TypeScript
- ESLint
- Relevant unit tests
- Actual-component tests
- Navigation tests
- Privacy tests
- Onboarding tests
- Listener session tests
- Responsive layout tests
- Persian/RTL checks

### Web full-stack E2E

Run a real integrated browser/backend/PostgreSQL test environment.

Include successful and failure journeys.

Do not classify mocked frontend transport as real full-stack evidence.

### Failures

If tests fail:

1. Identify the actual defect.
2. Fix the smallest relevant component.
3. Add or update a genuine regression test.
4. Rerun targeted tests.
5. Rerun applicable full integration tests.

Do not:

- Disable failing tests.
- Weaken assertions.
- Hide failure output.
- Replace live components with mocks solely to make final evidence pass.
- Count skipped or unexecuted tests as successes.

Record the final tested commit SHA.

---

# 13. K-09 — Real Android and iOS validation

Priority: P0 for native release readiness.

Web tests and native JavaScript exports do not prove device behavior.

### Validate where devices/builds are available

- Installation and first launch
- Registration and activation
- Secure token storage
- Password autofill
- App background lock
- Foreground restoration
- Expired authentication
- Logout and account switching
- Listener readiness while app is backgrounded
- Listener request expiration
- Live conversation messaging
- Network disconnect/reconnect
- Screen reader labels
- VoiceOver
- TalkBack
- Dynamic text
- RTL layout
- Native keyboard and focus
- Push notification privacy where applicable
- Sensitive previews
- Offline behavior

### Required honesty

If an actual device or signed native build is unavailable, mark the item:

`NOT_EXECUTED_DEVICE_REQUIRED`

Provide a detailed manual checklist.

Do not claim native release readiness based on an export command.

---

# 14. K-10 — Performance and reliability

Priority: P0 for critical service performance, otherwise P1.

Measure the actual application under controlled synthetic load.

### Critical scenarios

- Concurrent registration
- OTP resend limits
- Token refresh concurrency
- Profile/feed pagination
- Privacy-filtered post queries
- Listener presence heartbeat traffic
- Multiple simultaneous support requests
- Listener acceptance contention
- Message sending and polling
- Moderation report creation
- Data export and account deletion
- Scheduled cleanup
- Connection pool saturation

### Goals

- Identify obvious bottlenecks.
- Avoid uncontrolled resource consumption.
- Verify transaction timeouts.
- Verify safe database lock contention.
- Verify bounded pagination.
- Verify bounded workers and retries.
- Verify graceful failure under overload.

Do not invent latency or throughput measurements.

Propose acceptance thresholds based on a declared initial rollout capacity and environment, then seek owner approval for material service commitments.

No stress tests against uncontrolled production endpoints.

---

# 15. K-11 — Security verification

Priority: CRITICAL P0

Use the existing approved security framework.

References:

- NIST SP 800-63B-4
- OWASP ASVS 5.0.0
- OWASP MASVS
- Current appropriate OWASP testing guidance

Do not claim full certification without an independent assessment and evidence.

### Review areas

- Authentication
- Access control
- Session management
- Input validation
- Output encoding
- CSRF
- CORS
- Security headers
- Token storage
- Secret management
- Rate limiting
- Logging
- Privacy controls
- Sensitive-data access
- File/document access
- Abuse prevention
- Role escalation
- Secure errors
- Dependency vulnerabilities
- Account deletion
- Data export
- Staff MFA
- Support-session authorization

### Adversarial checks

Exercise direct backend APIs without relying on frontend restrictions.

Verify negative tests and resulting database state.

### Dependency and secret scanning

Run configured dependency/security scans where available.

Do not disclose secret values in reports.

Identify severity and exploitability before proposing changes.

Fix confirmed P0/security release blockers.

Do not perform a broad version upgrade without compatibility evidence.

---

# 16. K-12 — Observability and operational monitoring

Priority: P0

Review current operational signals.

### Essential categories

- API error rate
- Authentication failures
- Refresh replay
- Privileged denial
- Verification delivery failures
- OTP delivery queue
- Database health
- Migration failure
- Listener request failures
- Listener availability
- Active session limits
- Support session technical failures
- Moderation report backlog
- Account erasure worker failures
- Storage failures

### Privacy rules

Do not log:

- Passwords
- Tokens
- OTP codes
- Private messages
- Support topics
- Clinical inferences
- Sensitive medical narratives

### Operational evidence

Where supported, test alerts with controlled synthetic incidents.

Document:

- Alert destination
- Responsible operator
- Trigger
- Expected response
- Result of exercise

If external alert infrastructure or staff is unavailable, record the blocker.

Do not claim monitoring operational readiness based solely on log statements.

---

# 17. K-13 — External service validation

Priority: P0

Create an external-dependency verification ledger.

Cover:

### Email

- Provider credentials
- Sender/domain verification
- SPF/DKIM/DMARC
- Templates
- Authorized test inbox
- Delivery outcome
- Bounce handling
- Secret management
- Alerts

### Privileged MFA

- Approved RP ID
- Exact trusted origins
- Real authenticators
- Approved staff enrollment
- Backup authenticator
- Recovery procedure
- MFA-protected administration

### Geographic services

- Approved pilot country or countries
- Authorized service types
- Published scope
- Professional-care approvals if offered
- No unsupported service activation

### Human listener operations

- Approved real listeners
- Training evidence
- Actual staff/supervisor capacity
- Availability processes
- Incident handling
- Reporting responsibility
- Service suspension controls

### Urgent resources

- Country-specific listings
- Verification provenance
- Scheduled review owner
- No fabricated 24/7 availability

### Legal/privacy

- Approved Terms
- Privacy Notice
- Applicable data-processing decisions
- Sensitive-data processing review
- Appropriate DPIA status
- Third-party contracts and transfer decisions where required
- Retention and backup policy approvals

### Native release

- Platform signing and provisioning
- Real devices
- Store disclosure requirements
- Release permission where applicable

Never invent completion of external checks.

---

# 18. K-14 — Deployment and rollout plan

Priority: P0

Prepare a release plan, but do not deploy automatically.

### Environments

- Development
- Automated QA
- Staging
- Production

### Required preparation

- Environment configuration matrix
- Database migration order
- Schema compatibility
- Backup procedure
- Deployment secrets
- Service feature flags
- Health checks
- User eligibility
- Published policies
- Rollout monitoring
- Production access restrictions
- Incident contacts
- Forward recovery plan
- Rollback feasibility
- User communication for breaking changes
- Data privacy and retention controls

### Feature gates

Preserve disabled states for unapproved:

- New countries
- Professional clinical care
- Sensitive support-topic collection
- Nonessential analytics
- Privileged administration
- Real listener support where required staffing or operational controls are absent

Do not enable a feature solely because unit tests pass.

### Deployment authorization

A successful CI run is not approval to deploy.

Do not merge or deploy without explicit authorization.

---

# 19. K-15 — Recovery and disaster scenarios

Priority: P0

Validate recovery planning for:

- Database migration failure
- Database outage
- Email provider outage
- Compromised credential/key
- Lost old mail encryption key
- Authentication outage
- Session revocation failure
- Listener matching outage
- Listener support shutdown
- Moderator unavailability
- Bad client rollout
- Unexpected private-content exposure
- Backup restoration
- Account deletion restoration implications

### Required evidence

Use safe staging or synthetic fault-injection exercises when possible.

Document:

- Detection
- Containment
- User impact
- Recovery procedure
- Data preservation
- Safe rollback or forward-fix
- Operator responsibility
- Required follow-up

Do not trigger real production outages.

Do not promise a recovery-time or recovery-point objective without measured evidence and operational approval.

---

# 20. K-16 — Accessibility and localization acceptance

Priority: P0 for core user journeys.

Review:

- Welcome
- Registration
- Verification
- Login and recovery
- Onboarding
- Profile privacy
- Support discovery
- Listener request and acceptance
- Live messaging
- Safety reporting
- Policy pages
- Export/deletion

### Requirements

- English
- Persian
- RTL
- Appropriate bidirectional text rendering
- Dynamic text
- Keyboard operation
- Screen reader labels
- Focus management
- Error announcements
- Loading/status announcements
- Mobile target sizes
- Color contrast
- Clear service boundaries

Record both automated checks and manual assistive-technology evidence.

Do not claim complete native accessibility without device review.

---

# 21. K-17 — Final release blocker triage

Priority: CRITICAL P0

Create a single release blocker register.

For every issue record:

- ID
- Affected phase
- Severity
- Reproduction evidence
- User/security/privacy impact
- Code defect or external dependency
- Required owner
- Recommended action
- Whether deployment must remain blocked
- Verification evidence after resolution

### Severity definitions

P0 — Must be resolved or explicitly keep the affected feature disabled before the intended release.

P1 — Important defect requiring a documented decision and rollout plan.

P2 — Non-blocking issue with a bounded follow-up plan.

### Important rule

Do not mark P0 issues as accepted merely to make the final report green.

An affected feature may remain disabled when this is an approved safe alternative.

A disabled high-risk feature is not the same as a fully available feature.

Do not claim market launch completeness while its core intended service is disabled.

---

# 22. K-18 — Phase 1 completion decision

Priority: CRITICAL P0

Produce a final evidence-based Phase 1 assessment.

Use separate readiness dimensions:

## A. Development completeness

Has all approved A–J software functionality been implemented or explicitly deferred?

## B. Functional integration

Do the relevant full-stack journeys work together?

## C. Security and privacy

Are all applicable P0 risks adequately mitigated?

## D. Data and migration safety

Can the application safely run and upgrade without unexpected data loss or exposure?

## E. Cross-platform quality

Have the intended web/native platforms been sufficiently tested?

## F. Legal readiness

Are necessary product/legal decisions and reviews complete for the intended scope?

## G. Operational readiness

Are external services, trained staff, monitoring, incident processes and recovery procedures actually prepared?

## H. Geographic/service readiness

Are the intended countries and service types explicitly approved?

### Final decision values

- TECHNICALLY_COMPLETE
- TECHNICALLY_INCOMPLETE
- INTEGRATION_VERIFIED
- INTEGRATION_BLOCKED
- READY_FOR_LIMITED_STAGING
- STAGING_BLOCKED
- READY_FOR_APPROVED_PILOT
- PILOT_BLOCKED
- PRODUCTION_GO
- PRODUCTION_NO_GO

Do not issue PRODUCTION_GO unless the applicable legal, human operational, security, geographic and deployment approvals are verified.

The owner must explicitly authorize deployment.

### Honest completion standard

If A–J software is implemented and tested but real email/MFA/human operations/legal review remain blocked, report:

**Phase 1 technical implementation complete, operational launch not yet authorized.**

Do not falsely call all blockers solved.

---

# 23. Owner-decision protocol

Request product-owner approval when an unresolved issue requires a consequential choice.

Examples:

- Selecting initial approved pilot markets.
- Deciding which optional services are included in launch.
- Establishing a binding human-listener availability commitment.
- Approving operational monitoring or incident staffing.
- Setting new sensitive-data retention policies.
- Approving legal publication.
- Accepting material residual safety risk.
- Approving a breaking migration.
- Choosing an initial rollout capacity.
- Authorizing production deployment.

### Decision request format

Present:

1. Exact issue.
2. Applicable existing approved policy.
3. Evidence and consequences.
4. Options.
5. Recommended option.
6. Safe behavior while unresolved.

Ask in Persian where suitable.

Batch related decisions.

Do not ask for already approved policy choices.

Continue independent testing and fixes while owner decisions are pending.

Never interpret silence as approval.

---

# 24. Execution sequence

Use a controlled, efficient process.

## K-00 — Baseline and dependency verification

Identify latest A–J code and PR chain.

## K-01 — Integrated smoke tests

Confirm the application starts and core journeys work.

## K-02 — Automated regression

Run existing suites against the integrated code.

## K-03 — Targeted defect remediation

Fix demonstrated issues only.

## K-04 — Full-stack integration

Run browser/backend/PostgreSQL journeys.

## K-05 — Security, privacy and migrations

Validate critical invariants and transitions.

## K-06 — Native and accessibility

Execute available checks and identify unavailable device evidence.

## K-07 — Reliability and operations

Verify staging, infrastructure and external dependency contracts.

## K-08 — Re-run final evidence

Re-run affected tests and full applicable suites after fixes.

## K-09 — Release evaluation

Publish blocker register, readiness verdict, deployment plan and PR.

Do not stop after producing a plan.

Execute all possible work in the available environment.

Do not fabricate inaccessible infrastructure or external approvals.

---

# 25. Required final documentation

Create:

- `docs/phase-k/integration-matrix.md`
- `docs/phase-k/end-to-end-test-matrix.md`
- `docs/phase-k/test-evidence.md`
- `docs/phase-k/security-privacy-review.md`
- `docs/phase-k/migration-compatibility.md`
- `docs/phase-k/native-accessibility-checklist.md`
- `docs/phase-k/performance-reliability.md`
- `docs/phase-k/external-dependency-ledger.md`
- `docs/phase-k/release-blockers.md`
- `docs/phase-k/rollout-and-recovery-plan.md`
- `docs/phase-k/phase-1-closure-report.md`

Reuse authoritative existing documentation where possible.

Do not duplicate hundreds of pages of previous phase reports.

Link to evidence.

---

# 26. GitHub and review

Create a separate Phase K branch from the correct integrated baseline.

Use focused commits for confirmed defects.

Preserve existing implementation history.

Do not force-push.

Do not merge automatically.

Do not deploy automatically.

### Pull Request

Include:

- Integrated commit baseline
- Dependency chain
- Test execution evidence
- Defects fixed
- Migration verification
- Security/privacy findings
- Native verification status
- Operational readiness
- Outstanding owner decisions
- External blockers
- Release recommendation

Provide a clear reviewer checklist.

The final tested SHA must be identifiable.

---

# 27. Final deliverable format

## Executive Summary

Describe whether Phase 1 technical implementation is complete.

## Integration Evidence

Summarize A–J compatibility.

## Test Results

Report actual counts of:

- Passed
- Failed
- Skipped
- Not executed

By test category.

## Fixed Defects

For each:

- Reproduction
- Root cause
- Source change
- Regression test
- Result

## Database Safety

Migration chain and recovery evidence.

## Security/Privacy

Verified controls and residual risks.

## Native Readiness

Executed device results versus remaining manual tests.

## Operations

External dependencies, actual evidence and blockers.

## Legal

Required approvals completed versus outstanding.

## Go/No-Go

Use a clear decision with specific supporting reasons.

## GitHub

Branch, PR, commits and CI.

## Remaining Phase 2 Opportunities

List future enhancements separately.

Do not implement Phase 2 inside Phase K.

---

# 28. Definition of Done

Phase K technical work is complete when:

1. The correct integrated A–J baseline is identified.
2. All prerequisite implementation statuses are accurately established.
3. The application starts successfully on the tested environments.
4. Core user journeys are validated end to end.
5. Existing regression tests run on the final integrated revision.
6. Confirmed critical bugs are corrected.
7. Authentication and session protections remain enforced.
8. Profile and content privacy remain enforced.
9. Support requests require true listener acceptance.
10. Unauthorized or expired listener sessions cannot send messages.
11. Reporting and blocking work as specified.
12. Legal/consent controls behave according to approved technical contracts.
13. Flyway migrations are compatible and preserve data.
14. No prohibited sensitive analytics or profiling is introduced.
15. Web accessibility and RTL are reviewed.
16. Native evidence is recorded accurately.
17. Relevant security verification is completed.
18. Operational services are checked where accessible.
19. Unavailable external requirements are explicitly classified.
20. All relevant P0 release blockers are resolved or the affected scope remains explicitly blocked.
21. Recovery procedures are documented.
22. A final Phase 1 closure report exists.
23. A reviewable Phase K PR exists.
24. Technical completion is not confused with production approval.
25. The owner receives a clear, evidence-based recommendation for the next step.

---

# FINAL EXECUTION DIRECTIVE

Perform the final GreenOcean Phase 1 integration and release-readiness assignment.

Use the latest correct A–J implementation baseline.

Preserve previous verified work.

Do not repeat earlier implementation phases.

Run comprehensive integration, regression, security, privacy, migration, accessibility and reliability tests.

Find and fix only confirmed defects.

Re-run tests after each necessary fix and execute the final complete applicable regression suite.

Provide genuine test evidence.

Document operational, legal and geographic blockers without inventing approvals.

Prepare an explicit deployment and recovery plan, but do not deploy or merge automatically.

Deliver a focused Pull Request and final Phase 1 closure report.

**The expected outcome is an evidence-based determination of whether GreenOcean Phase 1 is technically complete, integrated and safe for its intended deployment scope, with any remaining approval or operational conditions precisely identified.**

**Do not declare production readiness unless it is actually demonstrated and authorized.**
