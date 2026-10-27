# GreenOcean — Phase G + H
## Privacy-First Profiles, Progressive Onboarding & First Support Journey

### ROLE AND OBJECTIVE

You are the principal full-stack engineer, privacy-by-design architect, product UX engineer, application security engineer, database architect, and QA lead for GreenOcean.

Your mission is to fully implement, complete, harden, document, and test:

- **Phase G — Initial Profile and Privacy**
- **Phase H — Onboarding and Understanding User Preferences**

Implement these two phases as one coordinated workstream because their privacy, profile, preference, and navigation contracts are closely connected.

**Repository:** `mehrnaz-sarafraz/GreenOcean`

**Technology stack:**
- Java 21 / Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router / TypeScript
- Web, Android, and iOS

The goal is to deliver functioning code, database-safe changes, comprehensive tests, documentation, and a reviewable Pull Request.

This is an existing project, not a greenfield application.

**Do not recreate working systems. Do not stop after analysis. Implement the missing work.**

---

# 1. Establish the correct implementation baseline

Previous GreenOcean phases have been implemented incrementally, including on stacked GitHub branches.

Previously observed:

- Phase A: `feature/phase-a-identity-privacy-access`
- Phase B: `feature/phase-b-welcome-trust-experience`
- Phase C: `feature/phase-c-registration-completion`
- Phase D: `feature/phase-d-email-verification-activation`

Phase E/F may have been implemented after the previous repository inspection.

Do not assume this list is current.

### Mandatory initial inspection

1. Inspect main and relevant active branches.
2. Locate the latest Phase A–F implementations and Pull Requests.
3. Identify the correct code baseline for G/H.
4. Inspect relevant CI and implementation reports.
5. Confirm whether Phase E/F has added account-security, session, privacy, or cache-isolation changes.
6. Avoid branching from stale main if required changes remain in stacked PRs.
7. Preserve prior work and do not automatically merge unrelated PRs.

### No redundant audits

Do not repeat complete Phase A–F audits.

Inspect only the dependencies relevant to profiles, preferences, onboarding, public content visibility, account data, and navigation.

Classify each existing dependency as:

- VERIFIED_AND_REUSED
- REUSED_WITH_TARGETED_TESTS
- PARTIALLY_IMPLEMENTED
- DEFECTIVE
- MISSING
- BLOCKED_EXTERNAL_DEPENDENCY

Only modify previously implemented features when a demonstrated G/H requirement or regression requires it.

A prior implementation report without current compatible test evidence is not proof of correctness.

---

# 2. Approved product principles

The following decisions are binding.

## 2.1 Privacy by default

New GreenOcean accounts must start with conservative privacy settings.

Where supported by the current architecture:

- `profilePrivate = true`
- `showLocation = false`
- `showBirthYear = false`
- `allowMessageRequests = false`
- `blurSensitiveContent = true`
- `strongerContentControls = true`

Preserve these defaults if already implemented.

Do not silently reset existing users' intentional privacy settings.

Do not interpret an existing account's historical configuration as consent to broader visibility.

## 2.2 Minimal public identity

Creating an account must not require:

- Real name
- City
- Biography
- Avatar
- Mental health history
- Diagnosis
- Medication history
- Trauma disclosure
- Specific support interests
- Professional credentials

Preserve the minimal registration model implemented in Phase A/C.

A temporary username and display name may be generated to satisfy existing database requirements.

Do not derive public identifiers from email addresses.

## 2.3 Distinguish identity, privacy, and personal preferences

These are three different concepts:

1. **Public/social profile:** username, display name, optional avatar and biography.
2. **Privacy and interaction permissions:** audience visibility, follows, message requests, blocking.
3. **Private support preferences:** interests, preferred support style, content preferences.

Do not expose private support preferences through social profile APIs.

## 2.4 Onboarding is optional

Onboarding must not become another registration requirement.

Users must be able to:

- Skip individual optional choices.
- Skip the entire flow.
- Choose no interests.
- Change preferences later.
- Return to an incomplete flow.
- Access permitted services without disclosing sensitive information.

## 2.5 No clinical inference

Do not infer a diagnosis, mental health condition, clinical risk, or medical need from onboarding selections.

Do not classify a user as ill because they selected a category.

Do not use onboarding selections to promise treatment outcomes.

## 2.6 Informed control

Users should understand what information is collected, why, where it is stored, who can see it, and how they can change or delete it.

Avoid misleading privacy or safety claims.

## 2.7 Future Phase I boundary

Phase H should connect users to an appropriate first-support destination.

However, do not rebuild the full listener matching system, messaging architecture, professional booking system, or community system.

Reuse existing features and routes.

Do not automatically start conversations or join groups.

---

# 3. Mandatory targeted code inspection

Inspect actual current implementations corresponding to the following previously observed components.

## Backend

- `RegistrationService`
- `Profile`
- `ProfileService`
- `ProfileRepository`
- `ProfileController`
- `OwnProfileResponse`
- `PublicProfileResponse`
- `UpdateProfileRequest`
- `UserPreferencesController`
- `UserPreferencesService`
- `UserPreferencesRepository`
- `UpdateUserPreferencesRequest`
- `UserPreferencesResponse`
- `SocialInteractionRepository`
- `PostService`
- `PostReadRepository`
- `PostVisibility`
- `CreatePostRequest`
- `MessagingService`
- `MessagingRepository`
- `AccessPolicy`
- Account data export and deletion services
- Service-region availability
- Existing Flyway migrations
- Existing profile/privacy integration tests

## Frontend

- `frontend/src/app/(auth)/onboarding.tsx`
- `frontend/src/app/(app)/profile.tsx`
- `frontend/src/app/(app)/safety-settings.tsx`
- `frontend/src/app/(app)/create.tsx`
- `frontend/src/app/(app)/support.tsx`
- `frontend/src/app/(app)/_layout.tsx`
- User profile pages
- Profile editing components
- PlatformData / DataProvider
- UserPreferences types
- AuthProvider
- Support category catalog
- Localization and theme
- Existing onboarding/profile tests

These paths describe the previously observed structure. Find the current equivalents if files have moved.

### Known observations to verify

Previous inspection found:

1. New profiles were created with `profilePrivate = true`.
2. The Phase A migration changed the database default for profile privacy.
3. Temporary random usernames were generated during minimal registration.
4. `showLocation` and `showBirthYear` were false by default.
5. The database default for `allowMessageRequests` was false.
6. The safety settings frontend used a potentially inconsistent `true` fallback while preferences were loading.
7. Private profile responses could still include biography and avatar.
8. Posts supported `PUBLIC`, `FOLLOWERS`, and `COMMUNITY`.
9. Post visibility for `PUBLIC` content also depended on the current author's profile privacy setting.
10. Changing profile privacy could therefore change the effective audience of historical posts.
11. The post composer previously submitted `visibility: 'PUBLIC'` without a genuine audience picker.
12. Onboarding Skip called the same completion method as Finish.
13. Onboarding persisted `onboardingComplete = true` after skipping.
14. The current onboarding step was held only in frontend state.
15. Selected support topics were stored using display names rather than stable identifiers.
16. The final onboarding route depended on an obsolete `intendedRole` professional-registration preference.
17. The selected support style did not consistently determine the user's initial support destination.
18. The privacy onboarding step coupled stronger content controls and sensitive-content blur through one toggle.
19. Some frontend trust/safety text could describe protections more strongly than the demonstrated runtime behavior.

These are historical inspection findings, NOT a guarantee of the latest implementation.

Verify before changing anything.

---

# 4. First required deliverable: G/H gap matrix

Before implementation, create a concise evidence-based gap matrix for all twenty-six requirements.

For each requirement record:

- Requirement ID
- Product intent
- Current code location
- Existing behavior
- Relevant previous implementation
- Existing test evidence
- Missing or defective behavior
- Frontend changes
- Backend changes
- Database changes
- Privacy/security implications
- Required tests
- Implementation priority

Allowed initial statuses:

- COMPLETE_AND_VERIFIED
- IMPLEMENTED_NEEDS_TESTS
- PARTIALLY_IMPLEMENTED
- DEFECTIVE
- MISSING
- BLOCKED_EXTERNAL_DEPENDENCY

Only implement actual gaps.

Do not modify correctly implemented requirements solely to generate new commits.

---

# 5. Phase G — Product-level privacy contract

Before coding G requirements, create:

`docs/product/profile-and-content-privacy-contract.md`

The document must formally distinguish:

- Account identity
- Public social identity
- Profile discoverability
- Profile visibility
- Post audience
- Community membership
- Follow approval
- Message request permission
- Established conversations
- Blocking
- Anonymous posting
- Sensitive-content presentation
- Optional private support preferences
- Data export
- Account deletion

### Critical invariants

1. Sensitive account attributes are never automatically public.
2. A private profile must not expose private support preferences.
3. A blocked user must not obtain protected information through alternative API endpoints.
4. An explicit post audience must be enforced server-side.
5. Changing profile settings must not silently expand the audience of historical posts.
6. Community visibility must not be bypassed through follower relationships.
7. Anonymous posting must not accidentally reveal the author through secondary APIs or UI surfaces.
8. Public educational resources remain distinct from members' social posts.
9. A user cannot bypass privacy through crafted API requests.
10. Private account settings must not be confused with clinical confidentiality guarantees.

Define a consistent audience policy that works across feed, profile, search, direct links, comments, notifications, and related APIs.

---

# 6. G-01 — Profile creation after registration

Priority: P0

Inspect and preserve the existing automatic profile initialization.

Requirements:

- Every eligible created account receives an internally valid minimal profile.
- No real name is required.
- No city is required.
- No user-selected avatar is required.
- A temporary username/display name is allowed.
- Profile creation remains transactionally consistent with registration.
- Correct privacy defaults are applied.
- Account state restrictions remain enforced.
- No account enters an inconsistent partially created state.

Do not create a second profile-initialization service.

### Tests

- Minimal registration creates one profile.
- Correct defaults.
- Failure rollback.
- No duplicate profile on retried registration.
- Existing profiles remain intact.

---

# 7. G-02 — Temporary social identity without email disclosure

Priority: P1

Reuse the current temporary username mechanism when correct.

Requirements:

- Public username is unique.
- Username is not derived from the email address.
- Display name does not reveal the email.
- No real-name inference.
- No predictable sequential account identification.
- Unicode-safe presentation.
- Temporary identity can be customized later.
- Name collisions are handled safely.

Do not treat a non-unique display name as a unique account identifier.

Tests must include duplicate display names and concurrent account creation.

---

# 8. G-03 — Edit username and display name

Priority: Existing behavior, preserve and test.

Requirements:

- Users can update their own social identity.
- Server-side validation.
- Username uniqueness.
- Reserved or impersonation-sensitive identifiers protected appropriately.
- Safe normalization.
- Clear validation errors.
- Proper cache updates.
- No unwanted account-identity change when editing a profile.
- Existing social references continue to work or fail safely after a username change.
- No cross-account editing.

Do not arbitrarily require Latin characters for display names.

Review username character restrictions separately from display-name rules.

Use the existing profile API if adequate.

---

# 9. G-04 — Optional avatar

Priority: Preserve existing behavior.

Requirements:

- Avatar remains optional.
- A safe default avatar is shown.
- Users can replace or clear an avatar.
- Deleting an avatar works, not merely overwriting it.
- The frontend distinguishes omitted patch fields from explicit removal.
- No arbitrary local file paths become public.
- Validate any uploaded or externally referenced media according to existing media architecture.
- Avoid leaking image metadata unnecessarily.
- Respect image storage cleanup/retention.
- Do not expose other users' nonpublic media.
- Avatar-related API responses honor the approved profile visibility contract.

Do not build a new file-storage subsystem unless required.

---

# 10. G-05 — Private profile by default

Priority: P0

Preserve the Phase A privacy-first default when already correct.

Requirements:

- New accounts start private.
- New temporary profiles remain private until explicitly changed.
- The frontend must not display an incorrect public state while data is loading.
- The backend must enforce actual privacy.
- API requests cannot bypass the setting.
- The database default must agree with the application default.

### Legacy users

Do not silently overwrite intentional public/private choices of existing users.

A database default change does not imply all historical records became private.

Audit the legacy behavior and document a safe, privacy-conscious policy for existing accounts.

If historical settings cannot be distinguished from deliberate choices, do not fabricate certainty.

Use a reviewed migration or user-facing review path instead of blindly rewriting preferences.

---

# 11. G-06 — Hide location by default

Priority: Preserve and verify.

Requirements:

- Residence country needed for service eligibility remains private.
- Public profile location is independently controlled.
- `showLocation = false` is enforced.
- No city/GPS requirement is introduced by profile completion.
- Changing public location visibility does not accidentally change jurisdictional access.
- City/country values cannot leak through related public DTOs or search results.
- The user understands the difference between country used for services and location publicly displayed.

Test direct API responses, not only the visible profile page.

---

# 12. G-07 — Hide birth year and age-related information

Priority: Preserve and verify.

Requirements:

- Do not expose age assurance, birth year, full date of birth, or eligibility evidence by default.
- Do not derive visible age from eligibility fields.
- Preserve Phase A data minimization.
- If birth-year public display is still supported for legacy accounts, require explicit control and test it.
- Do not automatically present a birth-year disclosure option to all new users.
- No unauthorized public endpoint may expose age-related attributes.

Prefer removing unnecessary public-age prompts from the new-user experience without destructive changes to historical data.

---

# 13. G-08 — Message request controls

Priority: P0

Preserve the existing message request architecture.

Requirements:

- Unknown users cannot start unsolicited requests when message requests are disabled.
- Existing follow/consent relationships are respected.
- Blocked accounts cannot contact the blocker through alternate routes.
- Verified professional messaging remains subject to the professional service rules.
- User-initiated listener support must not require globally opening unsolicited DM requests.
- Established conversations and unsolicited new requests must be treated separately.
- Backend permission checks must enforce the policy.
- The frontend must show accurate saved values.
- Optimistic updates must roll back visibly when saving fails.

### Existing concern

A previous safety settings screen used:

`allowMessageRequests ?? true`

Review this against the secure database default.

Do not display an unsafe fallback while preferences are loading.

### Tests

- Disabled requests.
- Enabled requests.
- Blocked user.
- Unapproved follower.
- Approved follower.
- Verified professional under applicable policy.
- Cross-account forged API calls.
- Concurrent preference changes.

---

# 14. G-09 — Sensitive-content controls

Priority: P0

Separate mandatory platform safety enforcement from optional personal display preferences.

### Mandatory safety rules

Content prohibited under platform moderation policy must remain restricted irrespective of an individual user's blur preferences.

A user disabling a visual blur must not disable moderation, role checks, reporting, blocking, or access-control enforcement.

### User preferences

Support existing options where functional:

- Blur permitted sensitive posts.
- Stronger content controls.
- Reduce specified content categories.
- Muted terms.
- Other existing approved controls.

Clarify the exact effect of each setting.

### Important distinction

Visual blurring is not encryption or a server-side confidentiality boundary.

Do not claim blurred content is inaccessible at the API level if it is merely visually hidden.

### Required inspection

Verify the setting's real effect in:

- Feed
- Post cards
- Direct post views
- Search results
- Previews
- Notifications
- Related content recommendations where applicable

Ensure sensitive previews are not unintentionally displayed before the user reveals them.

Do not introduce clinical risk scoring or automated diagnosis.

---

# 15. G-10 — Public profile information contract

Priority: P0

Define exactly what a viewer sees.

### Recommended default for private accounts

A normal authenticated eligible member who is not approved to follow the target may see only minimal discoverable social identity where permitted:

- Username
- Display name
- Optional avatar only when allowed by the approved policy
- Minimal relationship controls

Do not expose by default:

- Biography
- Location
- Birth year
- Support topics
- Preferred support style
- Mental-health-related preferences
- Private activity
- Private communities
- Message metadata
- Email
- Eligibility information

An approved follower may see additional social information only under the explicitly defined visibility policy.

Following must not grant access to private communities or private support preferences.

### Backend requirements

Inspect PublicProfileResponse and all related query projections.

Do not rely on frontend hiding.

Audit profiles, search, follows, comments, feeds, counts, and notifications for indirect disclosure.

Avoid exposing sensitive private details through aggregate counts or unfiltered joins.

Document discoverability separately from visibility.

---

# 16. G-11 — Profile privacy versus post-level audience

Priority: CRITICAL P0.

This is one of the most important requirements in the entire workstream.

### Existing technical background

The previous code supported:

- PUBLIC
- FOLLOWERS
- COMMUNITY

The effective visibility of PUBLIC posts was also influenced by the author's current `profilePrivate` setting.

The previous post composer always sent:

`visibility: 'PUBLIC'`

These behaviors can cause misleading UI and historical audience expansion.

### Required product behavior

Every post must have a clearly defined effective audience.

The user must understand that audience before publishing.

The server must enforce the same audience consistently.

### Proposed audience choices

1. Approved followers.
2. Members of the selected community.
3. Broader eligible GreenOcean members, only through an explicit informed choice.

For private accounts, default to a restrictive audience.

Publishing to a broader audience from a private account must be an explicit per-post decision, not an accidental effect of a global toggle.

### Historical post invariant

**Changing a profile from private to public must not automatically broaden access to previously restricted posts.**

Do not silently rewrite old posts to be more visible.

### Implementation options

Inspect current schema and read queries first.

Choose the smallest maintainable solution, potentially including:

- An explicit effective audience field;
- A durable post visibility ceiling;
- An audience snapshot;
- A server-side migration-safe equivalent.

The design must preserve the audience originally intended to the extent evidence permits.

If historical intent cannot be reconstructed, use a conservative interpretation and document any reduction in discoverability.

Do not invent historical consent.

### Consistency tests

Test every relevant surface:

- Feed
- Profile posts
- Post direct link
- Search
- Community feed
- Comments
- Notifications
- Likes/bookmarks
- User recommendations
- Shared previews
- API pagination
- Cached content

### Privacy transition tests

- Private → Public
- Public → Private
- Private → Public → Private
- Multiple historical posts with different audiences
- Follow approval and revocation
- Community join and leave
- Blocking and unblocking
- Deleted content
- Anonymous content

Verify that unauthorized viewers do not receive content through raw API requests.

### Anonymous posting

Ensure anonymous posts do not reveal identifiable author information through auxiliary UI or API fields.

Explain anonymity accurately: it may hide identity from ordinary members without implying the platform has no internal ability to attribute content for safety or legal purposes.

### UX acceptance

Do not display a misleading PUBLIC badge for content that is actually visible only to followers.

Provide clear audience selection and confirmation where necessary.

Do not create a second unrelated post composer.

Extend the existing composer.

---

# 17. G-12 — Data export and account deletion

Priority: P0.

Reuse Phase A account deletion and data export services.

Do not rebuild their infrastructure.

Verify:

- Appropriate ownership authorization.
- Appropriate recent reauthentication where Phase E/F requires it.
- Export includes documented data categories.
- Private support preferences are handled correctly.
- Account deletion removes or anonymizes applicable profile and preference data.
- External avatar files follow the existing erasure workflow.
- Onboarding progress is addressed by retention rules.
- Security/audit exceptions are documented accurately.
- Backups are handled according to the approved retention policy.
- No claim of instant erasure from all external copies or backups.
- Public caches do not indefinitely display deleted profile information.
- Previously issued sessions are invalidated according to Phase A/E/F.

Use synthetic test data only.

---

# 18. Phase H — Formal onboarding contract

Before coding H, create:

`docs/product/progressive-onboarding-contract.md`

### Purpose

Help a newly activated eligible member choose how they want to use GreenOcean without demanding disclosure of mental health problems.

### Maximum initial structure

Prefer retaining the existing three-step flow:

1. Optional topics of interest.
2. Preferred type of support.
3. Privacy and service-boundary introduction.

Do not add extra mandatory questionnaires.

### Rules

- All personalization is optional.
- The user can skip.
- The user can return later.
- The user can change preferences.
- No diagnosis is requested.
- No health history is required.
- No inferred diagnosis is stored.
- The platform does not automatically start clinical care.
- No support pathway is guaranteed immediately available.
- Personalization must not bypass geographic eligibility or role authorization.

### Required user state model

Distinguish:

- NOT_STARTED
- IN_PROGRESS
- SKIPPED
- COMPLETED

The onboarding process and a navigation gate are different concerns.

Skipping must not falsely mean every step was completed.

Completion must not mean the user consented to clinical profiling.

Document compatibility with the previous `onboardingComplete` boolean.

---

# 19. H-01 — Optional support topics

Priority: P0.

Preserve the existing topic catalog where appropriate.

Requirements:

- Zero selections are valid.
- Multiple selections may be valid.
- No diagnosis is required.
- No question forces the user to reveal a medical condition.
- Clear explanation of personalization purpose.
- Selection can be changed or cleared later.
- Backend validates actual allowed category identifiers.
- Unsupported category IDs or freeform forged values are rejected safely.

### Stable storage

The previously observed frontend stored topic display names.

Migrate toward stable IDs or slugs.

Do not use localized display strings as authoritative identity keys.

Preserve historical selections safely.

Do not guess mappings when ambiguous.

### Sensitive data safeguards

Treat mental-health-related topic selections as potentially sensitive personal data.

Assess and document:

- Lawful basis and applicable special-category processing conditions where required.
- Necessity and proportionality.
- Retention.
- Access restrictions.
- User ability to delete preferences.
- Analytics limitations.

Do not equate acceptance of Terms with blanket consent to sensitive profiling.

If the required legal/product decision is unresolved, keep the sensitive collection or related processing behind a safe gate and allow non-sensitive onboarding to continue.

---

# 20. H-02 — Preferred support style

Priority: P0.

Preserve the existing support-style catalog where correct.

Previously observed values included:

- SHARED_EXPERIENCES
- SUPPORT_GROUPS
- LISTENER
- PROFESSIONAL_KNOWLEDGE

Requirements:

- Selection is optional.
- Empty selection is valid.
- Backend validates the option.
- Existing current option values remain compatible.
- User can change it later.
- The choice informs navigation, not permissions.
- No guarantee of human availability is inferred.
- Professional knowledge is distinguished from professional clinical care.

Do not automatically apply for listener/professional roles.

---

# 21. H-03 — Initial safety settings

Priority: P0.

The privacy defaults must already be active before onboarding.

Use the third onboarding step primarily to explain existing protections.

Do not treat it as the user's first opportunity to become safe.

### Requirements

- Explain profile privacy.
- Explain message request restrictions.
- Explain sensitive-content controls accurately.
- Explain optional adjustment.
- Keep mandatory moderation protections independent.
- Do not couple unrelated preference fields through one misleading toggle.
- Do not silently disable protected settings when the user skips onboarding.

Previously observed behavior coupled:

- strongerContentControls
- blurSensitiveContent

Review and separate these unless there is a clearly documented legitimate reason to couple them.

---

# 22. H-04 — Persist preferences correctly

Priority: Preserve existing functionality.

Reuse `UserPreferencesService` and existing storage.

Requirements:

- Authenticated owner-only access.
- Partial updates.
- Valid stable identifiers.
- Safe empty selection.
- No cross-account preference editing.
- No unexpected reset of unrelated fields.
- Correct null-versus-omitted semantics.
- Safe concurrent updates.
- Proper frontend refresh after saving.
- Minimal retention and no unnecessary duplicate copies.

Avoid introducing a second preferences database or API.

---

# 23. H-05 — Skip versus complete

Priority: CRITICAL P0.

The previous `Skip for now` action called the same finish function as completing onboarding.

Fix this behavior.

### Required outcomes

When the user completes all steps intentionally:

`onboardingStatus = COMPLETED`

When the user chooses Skip:

`onboardingStatus = SKIPPED`

When a user begins but has not finished:

`onboardingStatus = IN_PROGRESS`

For a new user who has not entered the experience:

`onboardingStatus = NOT_STARTED`

### Navigation behavior

A skipped user must be allowed to enter permitted app features.

Do not create a redirect loop that forces onboarding again.

Offer a nonintrusive way to resume or edit preferences later.

### Legacy migration

Existing `onboardingComplete = true` does not prove whether a user completed or skipped.

Do not invent historical completion evidence.

Use an explicit compatibility/legacy-unknown handling approach where necessary.

Document the migration semantics.

---

# 24. H-06 — Resume incomplete onboarding

Priority: P1.

Implement minimal durable progress tracking.

Requirements:

- Save the last meaningful completed step where necessary.
- Restore progress after app restart.
- Restore progress after browser reload.
- Prevent cross-account restoration.
- Do not persist raw unfinished sensitive input unnecessarily.
- Do not overwrite preferences after a failed save.
- Allow returning to earlier stages.
- Allow abandoning without penalty.
- Provide accurate loading and save states.
- Avoid storing every interaction as analytics.

### Implementation

Reuse existing preferences infrastructure where suitable.

Only create a dedicated onboarding-progress table if the current schema cannot safely represent the requirements.

Use additive Flyway migration if needed.

---

# 25. H-07 — Change preferences after onboarding

Priority: P0.

Users must be able to edit:

- Support topics
- Preferred support style
- Content preferences
- Permitted privacy settings

through the existing Profile / Privacy & Safety / Support experiences as appropriate.

Do not duplicate settings across unrelated screens with inconsistent meanings.

Changes must affect future relevant recommendations or navigation when supported.

Clear preferences must be reflected correctly.

Do not silently reenroll the user in onboarding.

---

# 26. H-08 — No mandatory mental health disclosure

Priority: P0.

Make this an explicit enforced product policy.

Do not require:

- Diagnosis
- Symptoms
- Therapy history
- Medication history
- Trauma disclosure
- Suicide-risk questionnaires
- Medical files
- Family history
- Clinical screening

for general onboarding.

Do not introduce a hidden mandatory backend field while removing the corresponding UI question.

Do not infer health conditions from choices.

Any future clinical assessment requires a separate reviewed scope, legal basis, consent/notice as applicable, qualified oversight, and safety design.

---

# 27. H-09 — No interests selected

Priority: P0.

A user selecting no support topics or support style must still have a meaningful experience.

Requirements:

- Save an empty preference set correctly.
- Do not assign a fictitious diagnosis.
- Do not silently select categories.
- Show a neutral starting experience.
- Offer a Support discovery page.
- Permit editing later.
- Do not show errors implying topic selection is mandatory.

Verify both frontend and backend behavior.

---

# 28. H-10 — Support versus treatment boundaries

Priority: Preserve and verify.

Explain, contextually:

- Community support is peer-to-peer and non-clinical.
- Listeners provide non-clinical emotional support.
- Educational professional content is not automatically treatment.
- Actual professional care depends on verified credentials, jurisdiction, capacity, and appropriate professional relationship.
- GreenOcean is not an emergency response service.

Preserve access to urgent help without account requirements where appropriate.

Do not overwhelm onboarding with repeated lengthy legal disclaimers.

Do not promise immediate listener or therapist availability.

---

# 29. H-11 — Explain why information is requested

Priority: P1.

For each optional personalization question, display concise truthful guidance.

Examples:

Support topics:

"Optional. Helps tailor the content you may see. You do not need to share a diagnosis."

Support style:

"Optional. Helps us suggest where to start. You can change it later."

Safety controls:

"These settings affect how content appears and who can contact you. Mandatory safety rules remain in place."

Do not claim that the platform uses a selection for recommendations if the current application does not actually use it.

If an intended capability is not yet connected, implement the documented behavior or adjust the copy.

---

# 30. H-12 — Navigate to the actual selected support pathway

Priority: CRITICAL P0.

This is a major Phase H outcome.

After onboarding, route the user to a real existing screen based on the chosen support style.

### Intended mapping

SHARED_EXPERIENCES
→ Existing permitted feed or relevant stories experience.

SUPPORT_GROUPS
→ Existing communities/groups discovery.

LISTENER
→ Existing Support / Listener discovery and matching entry point.

PROFESSIONAL_KNOWLEDGE
→ Existing verified educational articles/professional knowledge.

NO_SELECTION
→ Neutral Support discovery or main permitted home experience.

### Requirements

- Inspect actual Expo Router destinations.
- Reuse existing routes.
- Do not invent nonexistent destinations.
- Check account eligibility and geographic service availability.
- Avoid automatic social interactions.
- Do not automatically create a conversation.
- Do not automatically join a group.
- Do not automatically request an appointment.
- Do not assume a listener is currently available.
- Use understandable fallback navigation when service is unavailable.
- Allow the user to change their choice later.
- Respect skipped onboarding.
- Prevent navigation loops.

### Existing concern

Previously, the flow routed based on:

`preferences.intendedRole === 'PROFESSIONAL'`

This is a legacy registration concept.

Remove it from ordinary onboarding routing if still present.

Professional applications must use their dedicated workflow.

### First support boundary

Phase H should deliver the user to a valid first-support destination.

The deeper interaction, matching quality, and actual support engagement belong to Phase I and later feature phases.

Implement required navigation integration now, not the entire future support system.

---

# 31. H-13 — Separate professional onboarding

Priority: P1.

All public registrants start with the basic user/member identity under Phase A.

Do not ask the user to select PROFESSIONAL during ordinary onboarding.

Preserve the separate professional application workflow.

Requirements:

- Basic account activation first.
- Dedicated professional application entry point.
- Professional document collection only in the appropriate dedicated flow.
- Professional verification remains server-enforced.
- No public payload can grant elevated privileges.
- Existing member preferences remain distinct from professional application data.
- Listener training/application flows remain separate.

Do not rebuild professional verification.

---

# 32. H-14 — Privacy-preserving onboarding measurement

Priority: P1.

Implement minimal analytics only if the required infrastructure, policy, and lawful processing conditions support it.

### Allowed proposed events

- Onboarding started
- Step displayed
- Step continued
- Onboarding skipped
- Onboarding completed
- Technical save failure
- Support route navigation outcome

Prefer aggregation or anonymous operational counters.

### Prohibited by default

Do not send to general analytics:

- Selected mental health topics
- Diagnosis-related categories
- Raw support preference contents
- Private journal text
- Messages
- Clinical inferences
- Sensitive free text
- Real user identifiers unnecessary for aggregate measurements

### Requirements

- Avoid third-party trackers unless justified and approved.
- Respect existing consent/preferences and applicable law.
- Use bounded retention.
- Do not claim data is anonymous when identifiers or combinations permit reidentification.
- Ensure analytics failures do not block onboarding.
- Do not record a clinical profile from ordinary product usage.

If legal/technical prerequisites are absent, document the gap and keep sensitive instrumentation disabled.

---

# 33. Special requirements for recommendation and preference behavior

Inspect whether the existing feed, search, and discovery systems actually consume the stored onboarding preferences.

### Requirements

- Do not claim personalization works unless implemented.
- Prioritize valid accessible content, not sensitive profiling.
- Respect blocks and muted terms.
- Respect content moderation status.
- Respect post audience.
- Respect geographic service availability.
- Respect account authorization.
- Respect users with no chosen preferences.
- Prefer a transparent deterministic first version over an opaque algorithm.
- Do not create a mental health prediction engine.

If existing recommendation logic is outside Phase G/H, document the contract for Phase I rather than expanding into a new personalization platform.

---

# 34. Cross-cutting security and privacy review

Perform targeted threat modeling for:

- Private profile data exposure
- Post audience escalation
- Historical post visibility expansion
- User enumeration
- Username manipulation
- Avatar media misuse
- Cross-account profile updates
- Direct message permission bypass
- Block bypass
- Community membership bypass
- Anonymous author reidentification
- Sensitive topic inference
- Invalid preference injection
- Unapproved catalog options
- Unauthorized onboarding state updates
- Cross-account cached data exposure
- Unexpected app redirects
- Sensitive information in analytics
- Deletion/export failures
- Client/server privacy-state disagreement

Document mitigations and remaining risks.

No private social content may be exposed to unauthorized guests or underage visitors through the public-resource architecture.

---

# 35. Mandatory four-persona tests

Every applicable G/H requirement must be tested using the four agreed behavioral personas.

## HAPPY PATH

### Profile

- Create eligible account.
- Verify email.
- Confirm minimal private profile.
- Edit display name.
- Change username.
- Add/remove optional avatar.
- View own private information.
- Adjust privacy settings.
- Verify saved preferences.
- Export account data.
- Delete account through the authorized process.

### Onboarding

- Start onboarding.
- Select optional topics.
- Choose support style.
- Review privacy information.
- Complete.
- Confirm saved progress.
- Navigate to chosen support destination.
- Update preferences later.

## CREATIVE USER

### Profile

- Persian display name.
- Unicode text.
- Multiple display-name changes.
- No avatar.
- Empty biography.
- Switch public/private settings.
- Use multiple devices.
- Create posts with different audiences.
- Follow and unfollow users.
- Use existing anonymous posting.

### Onboarding

- Select many topics.
- Select none.
- Change language.
- Change support style.
- Restart after the second step.
- Return to a previous step.
- Revisit later.
- Switch between devices.
- Use a narrow mobile screen and text scaling.

## CONFUSED USER

### Profile

- Incorrect username format.
- Username already taken.
- Unclear avatar removal.
- Network failure while saving privacy settings.
- Misunderstanding public post visibility.
- Changing profile privacy after publishing.
- Mistaking visual blur for data confidentiality.
- Assuming a private profile prevents all discovery.

### Onboarding

- Skip first step.
- Skip entire flow.
- Close the app mid-onboarding.
- Start again later.
- Navigate back repeatedly.
- Attempt to continue with no selections.
- Lose network during save.
- Select an unavailable support service.
- Misunderstand a listener as a therapist.
- Ask how to change preferences later.

Expected behavior: clear, accurate, recoverable feedback.

## MALICIOUS USER

### Profile

- Retrieve another user's private profile fields.
- Bypass profile privacy using direct API calls.
- Access private posts through search.
- Access historical posts after a privacy transition.
- Forge update requests for another account.
- Enumerate private preferences.
- Bypass block restrictions.
- Open restricted community content.
- Start unauthorized direct messages.
- Reidentify anonymous authors through secondary APIs.
- Inject unsafe display or avatar values.

### Onboarding

- Submit invalid support styles.
- Submit forged topic identifiers.
- Modify another user's onboarding status.
- Submit very large preference payloads.
- Try unauthorized routes.
- Attempt to bypass account eligibility.
- Force privilege elevation using intendedRole.
- Trigger route manipulation/open redirects.
- Extract support preferences through analytics or API errors.

Rejected requests must leave database state secure and consistent.

All adversarial tests must use isolated synthetic test data.

---

# 36. Required automated test layers

## Unit tests

Cover:

- Profile defaults
- Profile field visibility
- Username normalization
- Preference validation
- Privacy transition decisions
- Effective post audience
- Message request policy
- Content preference behavior
- Onboarding state transitions
- Skip versus Complete
- Resume logic
- Support destination mapping
- Invalid or missing preferences

## Backend integration tests

Use an isolated real PostgreSQL test database.

Verify:

- Minimal profile creation
- Correct privacy defaults
- Existing-user compatibility
- Profile update authorization
- Public/private DTO differences
- Message request controls
- Follow approval
- Block restrictions
- Post audience enforcement
- Historical post privacy transitions
- Anonymous posting confidentiality from ordinary members
- Profile data export
- Account deletion
- Onboarding progress persistence
- Valid/invalid preferences
- Cross-user isolation
- Account eligibility restrictions

Check persisted state and actual query results, not only response status codes.

## Frontend component tests

Cover:

- Profile editing
- Privacy toggles
- Save failures
- Profile loading states
- Audience selector
- Private/public explanations
- Onboarding steps
- Skip versus Finish
- Resume flow
- Zero topic selection
- Support style selection
- Navigation fallback
- RTL and accessibility
- Empty/error states

## Full-stack Web E2E

Where environment permits, run with:

- Real backend
- Disposable PostgreSQL
- Controlled authentication fixture
- Actual frontend navigation

Cover the main profile and onboarding journeys.

Do not count mock-only frontend tests as true full-stack E2E.

## Native mobile

Run available automated Android/iOS checks.

Where unavailable, provide an explicit manual device test matrix.

Do not claim device tests passed without execution.

## Migration tests

Test:

1. Fresh database.
2. Existing populated database.
3. Historical public/private profiles.
4. Historical posts.
5. Existing onboardingComplete records.
6. Legacy preference records.
7. Existing roles and professional applications.

Do not reset real user data.

## Privacy regression suite

Create an automated audience/authorization matrix testing multiple viewers against posts with different visibility and profile settings.

This is mandatory for G-11.

---

# 37. Accessibility, localization, and usability

Use existing GreenOcean design tokens.

Do not introduce a competing design system.

Requirements:

- Screen reader labels
- Logical focus
- Accessible toggle state
- Clear form validation
- Adequate target sizes
- Text scaling
- No clipped text
- Keyboard navigation on web
- Mobile safe areas
- RTL support
- Bidirectional text correctness
- English and other existing locales
- Honest loading/saving feedback

Do not assume that a visually selected preference is saved until the backend confirms it.

Avoid manipulative prompts that pressure users to disclose sensitive experiences.

---

# 38. Database and migration safety

Never edit applied Flyway migrations.

Only add migrations when required.

### Existing data must be preserved

- Users
- Profiles
- Usernames
- Existing avatar references
- Existing privacy choices
- Posts and comments
- Followers and pending requests
- Community memberships
- Conversations
- Existing preferences
- Professional applications
- Account eligibility
- Session/security information
- Policy acceptance records

Do not automatically delete or publicize historical posts.

Do not reset onboarding for existing users without an explicit, reviewed migration decision.

Do not rewrite legacy preference labels without an auditable mapping.

Provide forward-recovery guidance for irreversible schema changes.

Use bounded migrations and appropriate indexes.

Tests must operate on synthetic records in disposable databases.

---

# 39. Implementation packages

Implement the work in reviewable increments.

## GH-00 — Targeted baseline audit

Find latest code and test evidence.

Create G-01–G-12 and H-01–H-14 gap matrix.

## GH-01 — Privacy and audience contract

Define exact audience behavior and public-profile field policy.

Document and validate historical visibility behavior before migration.

## GH-02 — Profile completeness and privacy

Implement remaining G-01 through G-10 and G-12 gaps.

Do not recreate working profile/account APIs.

## GH-03 — Historical post privacy protection

Complete G-11.

Update existing audience UI and backend predicates consistently.

Use safe migrations only when necessary.

## GH-04 — Privacy and content controls

Ensure personal settings function correctly across relevant UI/API surfaces.

Keep mandatory moderation separate.

## GH-05 — Onboarding state and persistence

Complete H-01 through H-11, focusing on:

- Stable preference identifiers
- Skip versus Complete
- Resume
- Optionality
- Privacy explanations
- Legacy compatibility

## GH-06 — First-support routing

Implement H-12 and H-13.

Reuse existing routes and permissions.

Do not implement the complete future Phase I system.

## GH-07 — Privacy-preserving instrumentation

Implement H-14 only to the extent supported by the approved privacy architecture.

## GH-08 — QA, regression, documentation

Run tests, fix defects, update documentation, and create the Pull Request.

Small focused commits are required.

---

# 40. GitHub workflow

Create a dedicated feature branch based on the latest correct implementation baseline.

Suggested branch:

`feature/phase-gh-profile-privacy-onboarding`

Do not overwrite other developers' work.

Do not force-push.

Do not merge into main automatically.

Do not commit secrets, real user data, medical information, identity documents, tokens, or production database dumps.

If Phase E/F is still being implemented, coordinate the base branch so G/H does not discard its changes.

Use focused commits and explain any dependency on stacked Pull Requests.

Open a reviewable Pull Request with accurate test evidence.

---

# 41. Required documentation

Create or update:

1. `docs/product/profile-and-content-privacy-contract.md`
2. `docs/product/progressive-onboarding-contract.md`
3. `docs/phase-gh/gap-matrix.md`
4. `docs/phase-gh/delivery-report.md`
5. Relevant API documentation
6. Relevant privacy and retention documentation
7. Migration and rollout notes
8. Privacy threat model or equivalent security review
9. Test coverage matrix

Do not duplicate existing authoritative documentation unnecessarily.

Cross-link existing policies and security contracts.

---

# 42. Final delivery requirements

The final report must contain every G and H requirement individually.

### Requirement matrix

For each ID, include:

- Initial status
- Existing implementation reused
- What was changed
- Files modified
- Tests implemented
- Actual executed results
- Database impact
- Privacy/security impact
- Final status
- Remaining limitations

Allowed final statuses:

- COMPLETE_AND_TESTED
- IMPLEMENTED_NOT_FULLY_TESTED
- BLOCKED_EXTERNAL_DEPENDENCY
- NOT_IMPLEMENTED

### Test evidence

Report actual:

- Commands executed
- Passed tests
- Failed tests
- Skipped tests
- Backend integration tests
- Web E2E
- Native validation
- Migration tests
- Privacy regression tests
- Accessibility checks

Do not fabricate results.

### Four-persona coverage

Provide a traceability matrix linking Happy, Creative, Confused, and Malicious scenarios to implemented tests and requirement IDs.

### Privacy validation

Explicitly answer:

1. Can a private profile expose biography or sensitive account data?
2. Can private posts be retrieved through alternate APIs?
3. Can making an account public reveal previously restricted posts?
4. Can a blocked user bypass visibility or message restrictions?
5. Can someone retrieve another user's private preferences?
6. Can anonymous posts leak author identity to ordinary users?
7. Does user preference data appear in logs or analytics?
8. Can a user skip onboarding without losing normal permitted access?
9. Can a user revoke or delete saved personal preferences?
10. Does selected onboarding support style lead to an actual authorized destination?

Answers must be supported by code and tests.

### Operational readiness

Document:

- Required migrations
- Backward compatibility
- Known privacy limitations
- External legal/product dependencies
- Native testing gaps
- Deployment risks
- Phase I integration handoff

### Pull Request

Provide:

- Branch
- PR URL
- Commits
- Test results
- CI status
- Migration list
- Reviewer checklist
- Residual risks

---

# 43. Definition of Done

Phase G/H is complete only when:

1. Every G-01 through G-12 requirement has been evaluated.
2. Every H-01 through H-14 requirement has been evaluated.
3. Verified existing A–F implementations have been reused.
4. Profiles are safely initialized from minimal registration.
5. New accounts have the intended private defaults.
6. Private account information is not exposed through public/social APIs.
7. Message request and blocking policies are enforced server-side.
8. Sensitive-content preferences have truthful effects.
9. Profile privacy and post audiences have a documented consistent contract.
10. Changing profile visibility cannot unintentionally broaden historical post visibility.
11. Data export/deletion remains compatible.
12. Onboarding is optional.
13. Skipped and completed onboarding are distinguishable.
14. Interrupted onboarding can be resumed.
15. User preferences are validated, changeable, and removable.
16. Health disclosure is not required or inferred.
17. No-interest users have a valid default route.
18. Chosen support pathways lead to real, allowed experiences.
19. Professional application remains separate.
20. Analytics do not introduce unauthorized sensitive profiling.
21. All four user-persona test classes are covered.
22. Relevant automated tests have actually executed.
23. Existing user data and migration history remain intact.
24. A reviewable Pull Request is available.

Do not mark a requirement complete merely because the UI displays a control or the source code compiles.

---

# FINAL EXECUTION DIRECTIVE

Start from the latest valid GreenOcean implementation baseline.

First inspect the actual existing G/H behavior and related A–F dependencies.

Preserve all correctly implemented functionality.

Identify genuine gaps.

Formally establish profile privacy, post audience, onboarding state, and first-support navigation contracts.

Then implement only the missing, incomplete, defective, or insufficiently tested requirements.

Pay particular attention to preventing historical private-post exposure, separating Skip from Completion, eliminating obsolete role-based onboarding routing, and avoiding collection or inference of unnecessary sensitive health data.

Implement backend enforcement and frontend experience together.

Use additive migrations where necessary.

Run comprehensive tests for Happy, Creative, Confused, and Malicious users.

Perform targeted A–F regression checks without repeating previous full audits.

Do not stop at a plan.

Proceed through code implementation, tests, fixes, documentation, focused commits, and a reviewable Pull Request.

**The expected outcome is a privacy-preserving, minimal, trustworthy profile and onboarding experience that takes an eligible GreenOcean member safely from account activation toward their chosen first form of support, without requiring disclosure of personal mental health information.**
