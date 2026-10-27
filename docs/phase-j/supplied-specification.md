# GreenOcean — Phase J
## Legal Transparency, Consent Management, Sensitive Data Governance and User Rights

### Complete Implementation, Security, Privacy, Documentation and Testing

---

# 1. Mission

You are the principal full-stack engineer, privacy-by-design architect, security engineer, data governance engineer, product UX engineer, and technical compliance implementation lead for GreenOcean.

Repository: `mehrnaz-sarafraz/GreenOcean`

Technology stack:

- Java 21 / Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router / TypeScript
- Web, Android and iOS

Your assignment is to implement **Phase J — Legal Transparency, Consent and Data Governance** as the final substantive governance implementation workstream of GreenOcean Phase 1.

Build all missing or defective software mechanisms needed for the approved privacy and legal requirements, reuse existing capabilities, execute tests, and deliver a reviewable Pull Request.

**This is an implementation assignment, not permission to invent legal authorization or declare regulatory compliance.**

Distinguish:

1. Technically implemented privacy controls.
2. Product-owner decisions.
3. Legal professional review and approval.
4. Operational readiness.
5. Actual regulatory compliance in a defined jurisdiction.

Do not claim that software tests alone satisfy all five.

---

# 2. Establish the latest implementation baseline

GreenOcean has existing implementations covering:

- A — Identity, eligibility, access and privacy
- B — Public welcome and service transparency
- C — Minimal registration
- D — Email verification
- E/F — Authentication, device security and sessions
- G/H — Profile privacy and optional onboarding
- I — First real support, once its separate implementation has completed

Previously, A–H were developed using stacked Pull Requests. Phase G/H PR #6 was still Draft at the last inspection.

Do not assume those statuses remain current.

### Mandatory repository steps

1. Fetch the latest repository and branch information.
2. Find the latest correct Phase I baseline.
3. Locate the approved A–I specifications and delivery reports.
4. Read the current privacy, consent, account lifecycle, content moderation and support policies.
5. Inspect related code and previous test evidence.
6. Identify the actual latest commit and deployment compatibility requirements.

If Phase I is unfinished:

- Do not falsely classify its legal dependencies as implemented.
- Do not rebuild Phase I inside J.
- Continue the independent J work that can safely be completed.
- Record Phase I dependencies and request an owner decision only when scope or policy genuinely requires one.

Suggested branch:

`feature/phase-j-legal-consent-data-governance`

Base it on the latest correct implementation containing the relevant Phase I changes, rather than stale main.

No automatic PR merging or production deployment.

---

# 3. Reuse the existing architecture

Inspect existing capabilities before writing code.

Relevant systems include:

- PolicyService and PolicyRepository
- Published policy documents and versioning
- Registration Terms acceptance
- Account eligibility and identity
- UserPreferences
- Onboarding status and optional support preferences
- AccountService and AccountRepository
- Data export
- Account deletion and erasure worker
- Password reauthentication and device sessions
- Profile visibility and post audiences
- Direct messages and listener-support sessions
- Professional applications and credential files
- Moderation reports and appeals
- Admin operations and audit logs
- Security events and retention
- Notifications
- Public resources and urgent-help directory
- Geographic service availability
- Existing web cookie and native secure-storage practices

Historical migrations include V26 for initial published policy documents and subsequent identity/security/privacy migrations.

**Never modify applied Flyway migrations.**

Create only necessary additive migrations.

### Preserve existing functionality

Do not replace functioning:

- Registration
- Authentication
- Consent history
- Policy repository
- Preferences database
- Reports and appeals
- Account deletion
- Data export
- Email delivery
- Listener messaging
- Geographic authorization
- Security event tracking

Implement confirmed gaps using the existing domain boundaries.

---

# 4. Authoritative legal and policy approach

Use the applicable laws and current authoritative guidance for the intended launch jurisdictions.

Potentially relevant frameworks include:

- EU GDPR, especially Articles 5, 6, 7, 9, 12–22, 24, 25, 28, 30, 32–35 and related provisions, as applicable.
- Applicable EU/member-state electronic communications and cookie/storage rules.
- EU Digital Services Act, to the extent GreenOcean's services and provider classification fall within its scope.
- Applicable national consumer, health, professional-services and safety requirements.
- Other jurisdiction-specific law only where GreenOcean intends to operate.

### Critical limitation

Do not assume every legal obligation applies identically to every company or online service.

Document jurisdiction, service classification, organizational size and exemptions before asserting a specific legal duty.

Do not invent:

- A lawful basis for processing.
- An Article 9 exception.
- A registered DPO.
- A supervisory authority approval.
- A completed DPIA.
- A professional license.
- Approval to serve a country.
- Consent from an existing user.
- A valid international data-transfer mechanism.
- A mandatory legal retention period without supporting authority.

Where legal interpretation requires professional assessment, prepare evidence and technical alternatives and mark the decision:

`LEGAL_REVIEW_REQUIRED`

Continue independently actionable implementation.

---

# 5. Mandatory Phase J gap matrix and decision register

Create:

`docs/phase-j/gap-matrix.md`

Classify each requirement:

- COMPLETE_VERIFIED
- IMPLEMENTED_NEEDS_TESTS
- PARTIALLY_IMPLEMENTED
- DEFECTIVE
- MISSING
- REQUIRES_OWNER_DECISION
- LEGAL_REVIEW_REQUIRED
- EXTERNAL_OPERATIONAL_DEPENDENCY
- DEFERRED_BY_APPROVED_POLICY

For each requirement provide:

- Existing implementation
- Missing functionality
- Relevant privacy/legal issue
- Required code changes
- Database impact
- Relevant legal or owner decision
- Tests
- Final implementation status

Also create:

`docs/phase-j/owner-decisions.md`

### Owner decision protocol

Before implementing a consequential unresolved policy decision, present:

1. What needs approval.
2. Why existing requirements do not resolve it.
3. The available alternatives.
4. Privacy, legal, technical and UX implications.
5. Your recommended alternative.
6. The safe default until approval.

Ask these questions in Persian if the owner is communicating in Persian.

Batch related questions.

Do not ask for already approved decisions.

Do not halt unrelated safe development.

---

# 6. J-01 — Data inventory and processing register

Priority: P0

Create a technical data-processing inventory covering all Phase A–I systems.

Include at least:

- Account identifiers
- Email addresses
- Password hashes and recovery metadata
- Age eligibility evidence
- Residence and service-region attributes
- Profile and avatar data
- Posts, comments, reactions and followers
- Private posts and anonymous posting metadata
- Communities and memberships
- User preferences and support topics
- Onboarding state
- Listener applications and training evidence
- Live support requests and session metadata
- Support message bodies
- Reports and moderation evidence
- Professional credential documents
- Security events and device/session records
- Analytics and operational measurements
- Account export and deletion metadata
- External media references
- Backup data

For every category record:

- Purpose
- Whether processing is necessary or optional
- Data source
- Storage system
- API access roles
- Public/private classification
- Sensitive-data considerations
- Recipient and processor categories
- Cross-border transfer considerations
- Retention rule or unresolved decision
- Deletion/anonymization pathway
- Applicable legal-basis review status
- Owner or responsible role
- Existing tests

Create:

`docs/privacy/data-inventory.md`

Do not claim all user-supplied mental-health data is non-sensitive because GreenOcean is a peer-support platform rather than a clinic.

---

# 7. J-02 — Legal basis and special-category data controls

Priority: CRITICAL P0

Build a purpose-by-purpose lawful-processing decision matrix.

### General personal data

Identify the proposed Article 6 basis where GDPR applies, subject to owner/legal validation.

### Potential health-related data

Evaluate whether support topics, free-text experiences, messages, journals, community participation and other processing may reveal health information.

Where special-category data rules apply, identify the necessary separate legal condition.

Do not assume Terms acceptance is an Article 9 processing authorization.

Do not assume explicit consent is universally sufficient for every operation.

### Technical gates

Preserve the previous G/H default:

`APP_PRIVACY_SUPPORT_TOPICS_ENABLED=false`

Do not enable new sensitive topic collection without approved requirements.

Do not silently begin:

- Health profiling
- Clinical inference
- Sensitive topic analytics
- Sensitive recommendation ranking
- Listener matching based on hidden diagnostic assumptions

If a new optional processing purpose is approved, implement its documented notice and control requirements.

A withheld or withdrawn optional authorization must not remove access to unrelated core features unnecessarily.

### Evidence

Provide negative tests proving that unapproved sensitive collection or processing remains disabled server-side, even when a modified client sends forged values.

---

# 8. J-03 — Versioned policy publication

Priority: P0

Extend the existing policy infrastructure where necessary.

Support relevant documents such as:

- Terms of Use
- Privacy Notice
- Cookie and Storage Notice
- Community Guidelines
- Listener Code of Conduct
- Professional Service Boundaries
- Content Moderation and Appeals Policy
- Data Retention Summary
- Support Service Limitations

### Required behavior

- Draft documents remain unpublished.
- Only authorized reviewers can approve or publish.
- Current published versions are retrievable.
- Previous published versions remain auditable as appropriate.
- Activation and required reacceptance use the correct applicable Terms version.
- User acceptance records are immutable or tamper-evident within the existing data model.
- A changed privacy notice does not automatically masquerade as a new contractual acceptance.
- Optional consent is distinct from mandatory contractual agreement.
- Unavailable documents fail safely.
- Version, locale, effective date and publication status are explicit.
- No arbitrary HTML or script injection through document rendering.

### Multilingual support

Preserve current English/Persian infrastructure.

Do not label a translated legal document officially approved unless reviewed.

If a legally approved translation is unavailable, explain the limitation instead of fabricating legal equivalence.

### Legal review gate

Do not automatically publish newly AI-drafted legal documents as authoritative production policies.

Prepare drafts, implementation scaffolding and validation tests.

Publication requires the appropriate approved decision.

---

# 9. J-04 — Consent and optional authorization lifecycle

Priority: CRITICAL P0

Distinguish:

1. Terms acceptance.
2. Receipt of privacy information.
3. Optional personal-data processing consent, where consent is the applicable basis.
4. Nonessential analytics or storage permission.
5. Optional communications choices.
6. Any separate authorization required by an approved support-data purpose.

### Requirements

When consent is the applicable basis:

- An explicit eligible affirmative action is recorded.
- The purpose is specific.
- Consent is versioned where appropriate.
- The record identifies the relevant notice/purpose and timestamp.
- Withdrawal is accessible.
- Withdrawal is no harder than granting consent.
- Revocation affects future processing appropriately.
- Previously lawfully processed data is handled under the applicable approved rule.
- Missing consent is not interpreted as granted.
- Consent is not preselected.
- Consent is not bundled with unrelated mandatory terms.

### Backend

Enforce authorization by actual purpose.

Do not rely on frontend toggles only.

### Frontend

Use clear, concise language and accurate saved-state feedback.

No dark patterns or manipulative disclosure prompts.

### Existing records

Do not invent historical consent timestamps or retrofit consent=true for all legacy accounts.

Use explicit unknown/unverified states where appropriate.

---

# 10. J-05 — Truthful support and professional-service boundaries

Priority: P0

Audit all relevant text and actual behavior.

### Peer community

- Non-clinical community support.
- No diagnosis or treatment guarantees.
- Actual visibility and moderation limits stated accurately.

### Trained listeners

- Voluntary non-clinical support.
- Live acceptance required under Phase I.
- No guaranteed availability.
- No emergency-response promise.
- Training and supervision claims must match documented operational reality.

### Professionals

- Clearly distinguish educational professional content from professional clinical care.
- Verified credentials must be server-enforced.
- Regional permissions remain authoritative.
- No automatic license claims outside approved scope.
- No automatic clinical relationship merely through viewing content.

### Urgent help

Preserve publicly accessible country-aware urgent resources.

Clearly state that GreenOcean is not an emergency service.

Do not invent telephone numbers or claim that all resource listings are continuously verified without an operational process.

### Privacy language

Do not claim:

- Absolute confidentiality.
- End-to-end encryption unless implemented and verified.
- Universal anonymity.
- Guaranteed human monitoring.
- Guaranteed deletion from recipients' devices or external services.
- Guaranteed mental-health outcomes.

Compare copy with actual APIs and data flows.

---

# 11. J-06 — Data subject rights workflows

Priority: CRITICAL P0

Audit and complete the existing account-data rights implementation.

Potentially relevant rights under applicable law include:

- Access
- Rectification
- Erasure
- Restriction
- Objection
- Portability
- Information and transparency
- Human-review rights where applicable

Do not assume every right applies identically to every processing basis and data category.

### Data access/export

Review the existing AccountService archive.

Verify coverage for user-owned or user-related data, including newly introduced I support requests/session metadata where appropriate.

Distinguish export for portability from the full scope of an access response where legally necessary.

Avoid exposing another person's private data in exports.

### Rectification

Provide a documented way to correct appropriate account/profile information.

Do not silently rewrite immutable evidence such as historical moderation decisions.

### Erasure

Reuse existing deletion/erasure infrastructure.

Check:

- User records
- Profile
- Preferences
- Onboarding state
- Sensitive legacy topics
- Session tokens
- Owned social content
- Support session records
- Support messages
- External credential files
- External avatar limitations
- Search indexes
- Cached data
- Retention exceptions

### Requests requiring manual review

If an access or erasure request is legally or technically nontrivial:

- Use a secure authenticated intake/status process when necessary.
- Enforce identity verification proportionately.
- Provide bounded operational handling.
- Never give one user access to another person's report evidence.
- Do not claim automatic completion while outstanding operations remain.

### Authorization

Apply Phase E/F recent reauthentication where required.

Do not allow a client-supplied user ID to override the authenticated owner.

### Testing

Use synthetic data to verify both successful requests and forbidden cross-account access.

---

# 12. J-07 — Retention, deletion and backup governance

Priority: CRITICAL P0

Create:

`docs/privacy/retention-and-erasure-matrix.md`

Cover:

- PENDING accounts
- ACTIVE account data
- Deleted account tombstones
- Profile fields
- Posts/comments
- Anonymous author mappings
- Community data
- Support requests
- Live listener sessions
- Messages
- Reports and appeals
- Moderation/safety evidence
- Listener application evidence
- Professional documents
- Verification challenges and outbox records
- Security audit events
- Session/device data
- Analytics and aggregate metrics
- Backup copies
- External caches/media

### Preserve approved decisions

Pending unverified account retention was previously configured for seven days following the approved protected activity rules.

Do not silently change that policy.

Preserve existing bounded security-event retention where already approved.

### Missing retention decisions

Do not invent universal retention periods.

If a data category lacks approved retention:

- Record the issue.
- Propose a proportionate default.
- Explain legal and operational tradeoffs.
- Require approval for a consequential policy.
- Preserve safe access restrictions in the meantime.

### Cleanup

Where policy is approved, implement bounded, auditable, idempotent cleanup.

Do not purge evidence needed for an applicable lawful preservation obligation.

Do not retain everything indefinitely merely because legal review is outstanding.

For unresolved cases, isolate and restrict processing and document the operational blocker.

### Backups

Document backup retention, restore procedures and deletion-replay mechanisms.

Do not claim immediate physical erasure from all backups without verification.

---

# 13. J-08 — Moderation notices, appeals and reporting rights

Priority: P0

Reuse existing report and appeal systems.

Assess whether applicable content-hosting and online-platform rules require additional mechanisms.

When evaluating the EU Digital Services Act, distinguish obligations according to:

- Actual GreenOcean service classification
- Jurisdiction
- Organization size
- Applicable exceptions
- Specific type of content action

Do not blindly implement the complete compliance program of a very large online platform.

### Functional requirements where applicable

- Accessible reporting of potentially unlawful or prohibited content.
- Appropriate report reason categories.
- Report ownership and authorization.
- Clear reporting confirmation.
- Status visibility where required.
- Proper explanation for content restrictions where required.
- Appropriate internal complaint/appeal handling.
- Authorized staff review and decision evidence.
- Safe handling of malicious duplicate reports.
- Privacy controls for reporters and reported users.
- No unnecessary public exposure of allegations.

### Safety

Do not grant arbitrary staff unrestricted access to private support conversations.

Staff access must be purpose-limited and authorized.

Do not bypass privileged MFA.

### Operational readiness

If a reporting obligation depends on human review, document staffing, workflow and service availability requirements.

Software automation does not prove those operations exist.

---

# 14. J-09 — Listener support privacy and evidence handling

Priority: P0

Audit Phase I live-support implementation.

Document:

- What the member sees before starting support.
- What the listener sees before accepting.
- How the request is stored.
- What data the listener receives.
- What messages are retained.
- Who may inspect reported content.
- Whether session history remains visible after completion.
- How user blocking changes access.
- How deletion interacts with conversation evidence.
- Which information is shared with human moderators.
- What confidentiality limits are communicated.

### Mandatory safeguards

- No new clinical diagnosis collection.
- No unapproved health scoring.
- No copying private onboarding topics into live support requests without an approved basis.
- No indefinite automatic monitoring promise.
- No invisible retrospective disclosure of user messages.
- No automatic transfer of full chat transcripts to analytics services.

Record unresolved legal retention questions for owner review.

Do not rebuild Phase I session infrastructure unless a confirmed privacy defect requires a narrowly scoped correction.

---

# 15. J-10 — Communications and preference transparency

Priority: P1

Review:

- Essential account verification emails
- Password recovery
- Security alerts
- General notifications
- Optional product communications
- Listener-request notifications
- Marketing communications if any are actually implemented

### Requirements

- Distinguish service-essential communication from optional marketing.
- Respect applicable communication preferences.
- Do not accidentally disable necessary security operations through a marketing preference.
- Do not automatically opt existing users into optional campaigns.
- Do not expose sensitive topic choices in email subject lines or push notification previews.
- Keep public lock-screen notifications conservative.
- Ensure user preferences persist and are enforced server-side where relevant.

No new marketing platform is required.

---

# 16. J-11 — Cookies, local storage and analytics

Priority: P0

Audit all data storage surfaces:

- Web cookies
- Browser session state
- Local storage, if any remains
- IndexedDB or caches where applicable
- Native secure storage
- App telemetry
- Analytics SDKs
- Crash reporting
- Marketing/attribution technologies

### Requirements

- Essential authentication storage remains functional.
- Nonessential processing is gated under applicable policy.
- Optional analytics remain disabled by default absent approval.
- No consent banner is introduced merely as decoration.
- A declined optional choice must be enforced technically.
- Withdrawal must have a real effect.
- Third-party scripts must not access protected session secrets.
- Sensitive mental-health data must not enter general analytics.
- Logout/account switching must clear private client data as required by Phase E/F.

Do not alter approved HttpOnly refresh-cookie or secure native token-storage architecture without a demonstrated need.

---

# 17. J-12 — Third parties, processors and data transfers

Priority: P0

Identify actual external systems used by the current application, including where applicable:

- Email gateway
- Cloud hosting
- Object storage
- Crash/error monitoring
- Push notifications
- Image hosting
- Authentication infrastructure
- Professional document storage
- Backup services

For each, document:

- Data categories transferred
- Purpose
- Technical endpoint or integration
- Data processor/controller role requiring legal validation
- Hosting region where known
- Access permissions
- Retention/deletion responsibility
- International transfer review status
- Contractual/organizational prerequisites
- Operational owner

Do not fabricate signed data-processing agreements or adequacy findings.

Do not introduce new providers solely to complete documentation.

---

# 18. J-13 — DPIA and privacy threat modeling

Priority: CRITICAL P0

Create a DPIA screening and evidence package.

Include:

- Nature and purpose of processing
- Categories of affected users
- Potentially sensitive health information
- Public/private interactions
- Listener support and messaging
- Profiles and anonymity
- Third-party data flows
- Data minimization
- Access controls
- Retention
- User rights
- Security and privacy threats
- Necessity and proportionality
- Residual risks and mitigations

Create:

`docs/privacy/dpia-screening.md`

If a formal DPIA is required, prepare the technical evidence and draft assessment materials.

Do not declare it legally approved.

If unmitigated high risk requires regulatory consultation, record the blocker and keep the affected processing disabled until the proper legal process is completed.

---

# 19. J-14 — Security, privacy incidents and breach governance

Priority: P0

Reuse E/F security events and existing operational logging.

Define the technical incident lifecycle for:

- Unauthorized access
- Sensitive-data leakage
- Compromised credentials
- Excessive privileged access
- Mail delivery credential exposure
- Incorrect privacy visibility
- Support message disclosure
- Lost professional credential files
- Data processing mistakes
- Report evidence misuse

Implement practical technical controls where missing:

- Restricted audit access
- Bounded security logs
- Safe incident references
- Operational alert hooks
- Evidence preservation rules
- Revocation actions
- Audit trail for privileged access

Document incident roles and notification decision procedures.

Do not invent automatic legal breach notifications or statutory reporting conclusions without legal review.

Do not put private message content into ordinary application logs.

---

# 20. J-15 — Transparency, accessibility and localization

Priority: P1

Review actual UI across:

- Public Welcome
- Registration
- Terms and Privacy
- Activation
- Onboarding
- Profile settings
- Support discovery
- Listener conversation
- Reporting and appeals
- Account export and deletion

Requirements:

- Clear and truthful copy
- English and Persian where supported
- RTL
- Logical screen-reader order
- Accessible policy links
- User-friendly privacy explanations
- No dark patterns
- No misleading promises of anonymity or clinical support
- Clear status for unavailable or unapproved legal documents

Do not silently replace approved legal copy with an unreviewed translation.

---

# 21. J-16 — Test requirements

Implement the four GreenOcean personas.

## Happy Path

- Register and accept the correct Terms.
- Read current Privacy Notice.
- Set optional preferences.
- Decline optional processing.
- Withdraw previously granted optional processing permission where supported.
- Export account data.
- Correct permitted profile data.
- Delete account securely.
- Submit and follow an authorized report.
- View support confidentiality disclosures.

## Creative User

- Persian/RTL.
- Multiple devices.
- Multiple policy versions.
- No optional support topics.
- Changing privacy settings repeatedly.
- Interrupted export/deletion request.
- Legacy account migration.
- Multiple reports and appeals.
- Device logout during an account-data request.

## Confused User

- Attempts to continue with unavailable policy text.
- Mistakes Privacy Notice for Terms.
- Declines optional analytics.
- Withdraws consent.
- Doesn't understand support-message retention.
- Requests deletion of a shared conversation.
- Tries to export while offline.
- Misunderstands what a listener can see.

## Malicious User

- Forged policy acceptance.
- Forged consent record.
- Unauthorized policy publication.
- Another user's data export.
- Another user's deletion request.
- Sensitive preferences via API bypass.
- Moderator accessing unrelated private content.
- Report ownership bypass.
- Appeal ownership bypass.
- HTML/script injection through policy content.
- Identity enumeration through rights requests.
- Analytics collection despite denied permission.
- Cross-account data leakage.
- Unapproved regional or professional processing.

All tests must use synthetic, isolated records.

### Technical tests

Run:

- Java unit tests
- PostgreSQL integration tests
- Policy/versioning tests
- Authorization tests
- Consent state transition tests
- Preference-gate tests
- Retention worker tests
- Erasure/export tests
- Moderation tests
- Migration tests
- Frontend component tests
- Accessibility checks
- True full-stack Web E2E where supported
- Native checks where available

Execute relevant regressions for A–I.

Do not rerun unrelated historical development audits.

Do not claim native-device tests passed merely because JavaScript exports succeed.

---

# 22. Database safety

Create only necessary new Flyway migrations.

Preserve:

- Existing accounts
- Password hashes
- Roles
- Sessions
- Policy acceptance records
- Profile privacy choices
- Historical post audience restrictions
- Preferences
- Listener support conversations
- Reports and moderation records
- Existing deletion/retention evidence

Do not backfill new consent=true values for existing accounts.

Do not retroactively claim old users received a newly written notice.

Do not silently delete protected evidence.

Use forward migrations for corrections.

Test fresh and populated database upgrades.

Avoid irreversible user-data modifications without approved policy and recovery planning.

---

# 23. Implementation packages

## J-00 — Targeted legal/technical baseline

Inspect actual code and produce requirement matrix.

## J-01 — Data inventory and policy contract

Map personal data, sensitivity and processing purposes.

## J-02 — Versioning and consent controls

Complete missing backend/frontend policy and optional authorization mechanisms.

## J-03 — User rights and lifecycle

Complete only verified gaps in access, export, rectification, erasure and retention.

## J-04 — Support and moderation transparency

Align behavior, reports, appeals and wording with applicable approved rules.

## J-05 — Client privacy and analytics gates

Verify storage, preferences and optional processing enforcement.

## J-06 — DPIA, processor and incident documentation

Prepare technical evidence and operational dependencies.

## J-07 — Test and final documentation

Execute tests, fix defects, produce delivery report and PR.

Use focused commits.

Do not implement the complete future Phase K inside J.

---

# 24. Required deliverables

Create:

- `docs/phase-j/gap-matrix.md`
- `docs/phase-j/owner-decisions.md`
- `docs/phase-j/delivery-report.md`
- `docs/phase-j/test-evidence.md`
- `docs/phase-j/operations.md`
- `docs/privacy/data-inventory.md`
- `docs/privacy/processing-purpose-and-legal-review-matrix.md`
- `docs/privacy/retention-and-erasure-matrix.md`
- `docs/privacy/dpia-screening.md`
- `docs/privacy/third-party-data-flows.md`
- `docs/phase-j/phase-k-handoff.md`

Use actual evidence.

Avoid duplicate documentation when an authoritative existing file can be updated safely.

### Final report

For each requirement J-01–J-16, include:

- Before state
- Implementation performed
- Files changed
- Database impact
- Security/privacy impact
- Test evidence
- Owner/legal decisions pending
- Final status

Allowed statuses:

- COMPLETE_AND_TESTED
- IMPLEMENTED_AWAITING_APPROVAL
- IMPLEMENTED_NOT_FULLY_TESTED
- BLOCKED_EXTERNAL_DEPENDENCY
- LEGAL_REVIEW_REQUIRED
- NOT_IMPLEMENTED

Do not label LEGAL_REVIEW_REQUIRED as fully compliant.

---

# 25. GitHub requirements

Create a dedicated Phase J feature branch.

Open a reviewable Pull Request.

Include:

- Latest correct Phase I baseline
- Targeted code changes
- Migration list
- Test commands and results
- Data processing inventory
- Policy and consent changes
- Retention decisions
- Legal review blockers
- Security assessment
- Reviewer checklist
- Explicit Phase K prerequisites

Do not force-push.

Do not automatically merge.

Do not enable unapproved sensitive data processing.

Do not publish unreviewed legal documents.

Do not deploy to production.

---

# 26. Definition of Done

Phase J technical implementation is complete when:

1. Existing A–I privacy and governance mechanisms have been verified and reused.
2. Every J requirement has a documented implementation status.
3. Data categories and processing purposes are inventoried.
4. Sensitive-data processing is controlled by real authorization and approved gates.
5. Policy publication is version-safe.
6. Consent and mandatory contractual acceptance are separated.
7. Optional consent withdrawal is effective where applicable.
8. Existing user rights workflows are verified and their gaps fixed.
9. Retention and erasure behavior is documented and safe.
10. Listener privacy rules match the actual Phase I implementation.
11. Reporting, moderation and appeal handling use appropriate authorization.
12. Analytics and browser/native storage obey approved privacy controls.
13. Third-party data flows are documented.
14. DPIA screening and necessary review dependencies are recorded.
15. No legal, regional or clinical approval has been fabricated.
16. English/Persian transparency UX is accessible.
17. Relevant automated tests pass on the actual source revision.
18. External legal and operational sign-offs are explicitly separated from code completion.
19. A reviewable PR exists.
20. Phase K has a clear handoff and blocker register.

---

# FINAL EXECUTION DIRECTIVE

Complete the technical implementation of GreenOcean Phase J.

Start from the latest valid Phase I implementation.

Reuse current privacy, consent, account, moderation and policy systems.

Identify and fix only the actual missing or defective requirements.

Establish truthful legal transparency and privacy controls.

Protect potentially sensitive mental-health-related information.

Implement user-rights mechanisms, purpose-specific processing gates, retention enforcement where approved, and relevant reporting transparency.

Ask the owner for unresolved consequential decisions before the affected changes.

Keep unapproved sensitive processing disabled.

Produce tested code, safe migrations, clear documentation, actual evidence and a reviewable Pull Request.

**The expected result is a technically complete, privacy-conscious and legally reviewable GreenOcean Phase J, ready to enter Phase K without falsely claiming legal approval or production readiness.**