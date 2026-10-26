# GreenOcean — Phase B: Welcome & Pre-Registration Experience

## ROLE AND MISSION

You are acting as a senior product engineer, UX architect, accessibility specialist, application security engineer, and QA lead.

Your responsibility is to fully implement **Phase B — Welcome Screen and Pre-Registration Experience** in the existing GreenOcean application.

Repository: `mehrnaz-sarafraz/GreenOcean`

Technology stack:
- React Native / Expo / Expo Router / TypeScript
- Java 21 / Spring Boot / Spring Security
- PostgreSQL / Flyway
- Platforms: Web, Android, iOS

The objective is to create a trustworthy, accessible, polished, and fully functional pre-registration experience.

**This is not a greenfield project.**

You MUST inspect existing code first, preserve working functionality, reuse existing components and APIs, and extend or correct incomplete implementations.

Do not implement duplicated pages, endpoints, data models, or navigation flows if equivalent functionality already exists.

Do not modify unrelated product areas.

---

# 1. Critical prerequisite: Respect completed Phase A

Phase A is considered implemented by the project owner.

However, you MUST verify its actual state in the current repository before making changes.

Inspect the current branch, recent commits, relevant migrations, auth flows, and public access policies.

Phase A established the following product decisions:

1. Interactive social services are initially available to eligible adults aged 18+.
2. Teenagers may access separately curated, age-appropriate public educational and crisis resources without creating an interactive account.
3. The future 13–17 interactive community is not yet available.
4. Every registered user initially receives the base MEMBER/USER role.
5. Professional and listener privileges require separate verification.
6. Account status, age eligibility, professional verification, and onboarding status are separate concerns.
7. Registration follows progressive profiling and data minimization.
8. User privacy is conservative by default.
9. Geographic service availability must be based on actual configured availability rather than arbitrary assumptions.
10. Public resources must not expose private member content.
11. Terms, privacy information, and service boundaries must be represented truthfully.
12. Backend authorization is authoritative; frontend visibility does not grant access.

### Verification requirements

Before implementation:

- Identify where these rules are currently enforced.
- Confirm the latest Phase A API contracts.
- Identify how public resources are authorized.
- Verify registration and login route behavior.
- Identify the current email verification and pending-account experience.
- Confirm how geographic availability is represented.
- Determine whether the Phase A changes are merged into the active branch.

If Phase A is on an existing feature branch or Pull Request, locate the correct implementation baseline and avoid branching from an outdated main branch.

If Phase A implementation is incomplete, document the dependency and implement only compatible independent Phase B work.

**Never silently weaken Phase A security to make the Welcome screen function.**

---

# 2. Mandatory inspection of existing GreenOcean code

Before changing anything, inspect at least:

### Frontend

- `frontend/src/app/(auth)/welcome.tsx`
- `frontend/src/app/(auth)/_layout.tsx`
- `frontend/src/app/(auth)/register.tsx`
- `frontend/src/app/(auth)/login.tsx`
- `frontend/src/app/(auth)/info/[page].tsx`
- `frontend/src/app/(auth)/urgent-help.tsx`
- `frontend/src/app/_layout.tsx`
- `frontend/src/app/index.tsx`
- `frontend/src/features/auth/auth-provider.tsx`
- `frontend/src/features/safety/urgent-help.tsx`
- `frontend/src/components/`
- `frontend/src/lib/api/client.ts`
- `frontend/src/localization/`
- `frontend/src/theme/`
- Existing public resource pages
- Existing navigation guards
- Existing test configuration

### Backend

- Auth and registration controllers
- Public security route configuration
- Published policy controllers and services
- Crisis resource controllers and services
- Public educational resource APIs created during Phase A
- Geographic availability APIs
- Relevant public content authorization rules
- Existing integration and security tests

Inspect the files that actually exist in the latest repository. Paths listed above represent the previously known architecture and may have changed.

### Required audit output

Create a matrix containing:

- Requirement ID
- Current implementation
- Current file/component
- Missing behavior
- Required modification
- Frontend impact
- Backend impact
- Database impact
- Security impact
- Required tests

Use these statuses:

- IMPLEMENTED_AND_VERIFIED
- IMPLEMENTED_NEEDS_IMPROVEMENT
- PARTIALLY_IMPLEMENTED
- MISSING
- BLOCKED_BY_DEPENDENCY

**Do not rewrite existing functionality merely because it could be written differently.**

---

# 3. Overall Phase B product objective

A visitor arriving at GreenOcean must understand:

- What GreenOcean is.
- Who it is designed to help.
- What kinds of support exist.
- The difference between community support, trained listeners, and professional care.
- What GreenOcean cannot promise.
- How their privacy is protected.
- Which services require registration.
- Why interactive accounts are currently restricted to eligible adults.
- Where they can obtain appropriate public resources without registering.
- How to access urgent help.
- How to read the Terms and Privacy Notice.
- How to register or sign in safely.

The experience must not ask the visitor to disclose their diagnosis, mental health history, trauma, medication, symptoms, or personal problems before registration.

**First build trust and informed choice. Then offer account creation.**

---

# 4. Functional requirements

## B-01 — Clear GreenOcean mission and value proposition

Priority: P0

### Required behavior

The Welcome screen must communicate that GreenOcean is a digital environment for:

1. Supportive communities and shared experiences.
2. Human peer support through approved listeners.
3. Access to verified professionals and educational knowledge.
4. Personal growth and self-care, where relevant.

These must be presented as distinct but connected paths within one product.

### Copywriting rules

Use calm, respectful, clear language.

Avoid:
- Overly emotional marketing.
- Claims that GreenOcean can cure mental health conditions.
- Promises of guaranteed improvement.
- Claims of universal safety.
- Unsupported claims of clinical validation.
- Statements that imply every user can immediately receive human support.
- Claims of 24/7 human monitoring unless operationally proven.

Suggested positioning:

"Find understanding, meaningful connection, and the right next step."

Supporting copy should explain the actual services offered by the current product.

The wording must remain consistent across Welcome, How It Works, FAQ, and registration entry points.

### Acceptance criteria

A new visitor should be able to understand GreenOcean's primary purpose without reading long policy documents.

The mission must be accurate, not merely persuasive.

---

## B-02 — Clear presentation of support pathways

Priority: P0

Present three primary support pathways:

### Path 1 — Peer Community

Explain:
- Share experiences.
- Learn from other people's stories.
- Participate in communities.
- Receive non-clinical peer support.

Clarify that community discussions are not professional treatment.

### Path 2 — Trained Listener

Explain:
- One-to-one supportive listening.
- The listener's non-clinical role.
- Any relevant availability limitations.
- How matching works at a high level.

Do not imply listeners are available immediately unless reliable availability information supports that statement.

### Path 3 — Professional Support

Explain:
- Discover verified professionals.
- Review qualifications, expertise, and service availability.
- Understand that actual professional services depend on verification, jurisdiction, booking, and applicable terms.

Do not imply all listed professionals can serve every country or every user.

### UI requirements

Each pathway should include:

- Clear title
- Short description
- Suitable icon
- Distinct explanation
- "Learn more" or appropriate action

For unauthenticated visitors, an action must not expose private social data.

When a pathway requires an account, explain that registration is necessary rather than immediately navigating to protected content.

### Important

Do not create fake listener profiles, fake conversations, fake patient testimonials, or fake availability statistics.

---

## B-03 — Registration entry point

Priority: Required existing behavior

Preserve and improve the existing Create Account action.

Requirements:

- Route to the current Phase A registration flow.
- Respect eligibility rules.
- Do not bypass registration validation.
- Do not automatically request nonessential personal information.
- Prevent duplicate navigation from repeated taps.
- Ensure the action remains accessible on narrow screens and with a screen reader.
- Maintain correct navigation across Web, Android, and iOS.

If the user is already authenticated, do not show a misleading registration journey.

Respect the existing authentication state and route guards.

---

## B-04 — Sign-in entry point

Priority: Required existing behavior

Preserve the existing Sign In action.

Requirements:

- Navigate to the existing login flow.
- Support an authenticated user's return to their permitted destination.
- Respect pending-account and suspended-account restrictions from Phase A.
- Do not expose private data during authentication-state loading.
- Avoid confusing navigation loops between Welcome and protected routes.
- Test browser refresh, deep links, and session expiry.

Ensure navigation does not incorrectly treat an unknown or loading authentication state as fully unauthenticated.

---

## B-05 — Public urgent-help access

Priority: P0

An unauthenticated visitor must always have an identifiable route to public urgent-help resources.

### Requirements

- Display a clear urgent-help action.
- Keep it accessible without login or registration.
- Connect it to the approved public crisis resource service.
- Provide location-aware resources only when supported by verified data.
- Allow users to choose or change their country manually where applicable.
- Do not require GPS permission.
- Do not require an account.
- Do not require health-history disclosure.
- Do not expose sensitive member data.
- Do not falsely imply GreenOcean itself provides emergency intervention.

### Unavailable resources

If no verified resource exists for the selected region:

- Do not invent phone numbers.
- Do not claim universal coverage.
- Clearly explain the limitation.
- Provide general guidance to contact local emergency services through appropriate official channels.

Do not replace reliable crisis directories with unverified links.

### Backend requirements

Confirm the public crisis resource endpoint is actually accessible without authentication.

If Phase A introduced a replacement endpoint, use that endpoint.

Do not weaken global security rules to solve a routing issue.

### Acceptance criteria

A visitor with no account and no authentication token can reach appropriate public urgent-help information.

---

## B-06 — Transparent service limitations

Priority: P0

GreenOcean must distinguish:

- Peer support
- Trained listener support
- Professional services
- Emergency/crisis resources

### Presentation strategy

Use context-sensitive explanations.

On the main Welcome page, present a short, clear statement that GreenOcean's community and listener services are not substitutes for medical care or emergency intervention.

Provide links to detailed explanations.

Do not cover the screen with large legal notices that prevent normal interaction.

Avoid medical diagnoses, promises of clinical outcomes, or misleading professional endorsements.

### Trust claims

Audit all existing copy and UI elements containing phrases such as:

- Safe space
- Safety first
- Verified professionals
- Trained listeners
- Moderated support circles
- Private by default
- Immediate support
- Always available
- 24/7
- Guaranteed confidentiality

For each claim, determine whether the actual product and operational policies support it.

If the claim is not verifiable, replace it with narrower, truthful language.

Examples:

Do not say "Talk to someone anytime" when there may be no listeners.

Do not say "Professionally moderated 24/7" unless that service exists operationally.

Do not use a verified badge where the relevant verification status is unknown.

### Acceptance criteria

No unsupported medical, safety, availability, or professional-verification claim remains in the Phase B experience.

---

## B-07 — Public legal and trust information

Priority: P0

Preserve and improve public access to:

- Terms of Service
- Privacy Notice
- Community Guidelines
- Age & Eligibility Policy
- Peer Support & Professional Boundaries
- Data Retention and Deletion information
- How It Works
- FAQ

### Requirements

Use the existing Phase A published-policy infrastructure wherever available.

Do not create a duplicate legal-content database if the existing policy service already provides versioned documents.

Show the applicable current published version.

Prevent internal drafts or unpublished policies from being served publicly.

If a document is unavailable, display a safe, accurate fallback without claiming that a missing or outdated copy is the currently published policy.

Do not silently replace a failed legal-policy fetch with old hardcoded content while presenting it as authoritative.

### UI requirements

Public information pages must:

- Work without authentication.
- Have readable typography.
- Work with RTL languages.
- Be keyboard accessible on web.
- Support scrolling and text scaling.
- Provide clear navigation back.
- Preserve deep links.
- Avoid requiring consent merely to read policies.

Where policy version, publication date, and language are available, display them accurately.

### Acceptance criteria

Every public policy link opens the appropriate published document or an honest error state.

No private information or draft document is exposed.

---

## B-08 — Truthful service availability

Priority: P0

This is a critical trust requirement.

### Objective

Visitors must not believe that human support is guaranteed when actual capacity is unknown.

### Required behavior

Use carefully verified availability states.

Examples of acceptable messages:

- "Listener availability varies."
- "Explore how listener support works."
- "Availability depends on location and current capacity."
- "You can view available options after creating an eligible account."

Only display exact live counts or availability claims when an authoritative backend service provides suitable, current, non-sensitive data.

### Do not

- Hardcode "100 listeners online."
- Invent successful support sessions.
- Display fake response times.
- Claim instant matching.
- Promise availability in unsupported countries.
- Show professional availability not confirmed by the professional scheduling service.
- Expose identities of currently online users to unauthenticated visitors.
- Infer private service capacity from individual profiles.

### If availability is unknown

Use a neutral description and explain that availability is checked in the relevant authenticated service flow.

### Optional public summary

If the current architecture supports a safe public availability summary, it may expose broad service states without revealing personal information.

Do not build this endpoint unless it delivers genuine product value and can be supported operationally.

### Acceptance criteria

All capacity messaging corresponds to real supported product behavior.

The page must remain functional when availability cannot be retrieved.

---

## B-09 — Responsive, accessible, localized experience

Priority: P1, with essential accessibility treated as a release requirement

### Platforms

- Web desktop
- Web tablet
- Web mobile
- Android
- iOS

### Responsive design

Verify at representative viewport sizes, including:

- 320px
- 375px
- 390px
- 768px
- 1024px
- 1440px

These are testing representatives, not fixed layout breakpoints that must be hardcoded.

### Requirements

- No horizontal overflow.
- No clipped text.
- No overlapping CTAs.
- No unreachable buttons.
- Functional scrolling.
- Appropriate safe-area handling.
- Correct behavior when the keyboard appears.
- Suitable layouts on small phones and wider screens.
- Proper loading and error states.
- Visually consistent typography and spacing.

### Accessibility

Target WCAG 2.2 AA for relevant web experiences.

Implement:

- Semantic accessibility roles and labels
- Logical heading structure
- Keyboard navigation
- Visible focus indicators
- Adequate color contrast
- Touch targets with appropriate size and spacing
- Screen-reader-friendly navigation
- Reduced-motion support when practical
- Support for enlarged text
- Clear focus and error announcements
- Correct handling of decorative icons and images

### Localization

Reuse the existing localization architecture.

Preserve English and other languages already supported in the project.

Support Persian and RTL where existing localization supports them.

Do not introduce hardcoded English strings throughout newly implemented components.

Use logical layout directions and test bidirectional text.

Do not break left-to-right email addresses, URLs, or other Latin technical identifiers in RTL environments.

### Visual design

Follow the existing GreenOcean design system.

Aim for:

- Calm and welcoming design
- Clear hierarchy
- Professional presentation
- Consistent buttons and cards
- Readable content
- Strong contrast
- Trustworthy presentation

Do not introduce a second inconsistent design system.

Do not use fake endorsements, fake reviews, or stock imagery that implies real GreenOcean users.

---

## B-10 — Low-friction product explanation before registration

Priority: P1

A visitor must be able to understand essential product capabilities before deciding to create an account.

### Required sections or equivalent UX

Provide concise explanations of:

1. What GreenOcean does.
2. Who can use the interactive platform.
3. How community support works.
4. What trained listeners do.
5. How verified professional discovery works.
6. What happens after registration.
7. What account creation requires.
8. What remains available without an account.
9. Where to obtain urgent help.
10. How privacy and moderation work at a high level.

### Architecture

Reuse existing How It Works and FAQ pages where practical.

Do not build ten duplicate standalone screens if three clear sections and existing information pages can satisfy the requirements.

Use progressive disclosure:

- Short explanation on Welcome
- Additional details through How It Works / FAQ
- Context-specific details at relevant actions

### Guest experience

An unauthenticated visitor may access public information and approved public resources.

Do not expose:

- Private feed
- User profiles not approved for public access
- Messages
- Private groups
- Sensitive support preferences
- Personal account information
- Unapproved educational content
- Internal administrative data

### Acceptance criteria

A guest can understand the service without creating an account and without viewing private member information.

---

# 5. Proposed Welcome information architecture

Inspect the existing page first and refine its current structure.

Preferred information order:

### Section 1 — Brand and primary value proposition

- GreenOcean identity
- Clear headline
- Short supporting explanation
- Create Account CTA
- Sign In CTA

### Section 2 — Three ways to receive support

- Community
- Trained listener
- Professional support

Describe differences and limitations.

### Section 3 — How GreenOcean works

A concise explanation of:

- Join if eligible
- Choose how to participate
- Explore appropriate support
- Stay in control of privacy

Do not imply users must disclose sensitive information to begin.

### Section 4 — Trust and privacy

Explain:

- Private-by-default account behavior, if implemented
- User controls
- Reporting and blocking
- Verified versus unverified service roles
- Limits of confidentiality and moderation

Only make claims confirmed by the code and operational model.

### Section 5 — Public resources and urgent help

Show a clear pathway to separately approved public resources and urgent-help information.

### Section 6 — FAQ and policy links

Link to current public information pages and policies.

### Section 7 — Final invitation to join

Repeat registration and sign-in actions only when useful, without aggressive conversion design.

This information architecture is guidance, not permission to destroy a better existing layout.

Use the smallest number of components needed to achieve clarity.

---

# 6. Frontend/backend responsibilities

## Frontend-owned responsibilities

- Welcome layout
- Clear product copy
- Support pathway cards
- Navigation to registration and login
- Public-resource navigation
- Urgent-help access
- Policy navigation
- Accessibility
- Localization
- Responsive behavior
- Loading, empty, and error states
- Authentication-state-aware routing

## Backend-owned responsibilities

- Enforcement of guest/public access boundaries
- Public policy document retrieval
- Public crisis resource retrieval
- Public curated-resource retrieval
- Geographic service availability, where applicable
- Prevention of unpublished/private content exposure
- Server-side authorization
- Accurate data for any displayed live availability

## Database responsibilities

Only create or modify data structures when current Phase A infrastructure cannot satisfy a mandatory requirement.

Do not create a new database table simply to store static Welcome copy.

Do not create migrations for purely presentational changes.

Prefer existing published-policy and public-resource data structures.

---

# 7. Four mandatory test personas

All Phase B requirements must be evaluated using four behavioral personas.

## Persona 1 — Happy Path

A typical adult visitor.

Scenarios:

- Opens GreenOcean for the first time.
- Understands the product purpose.
- Reads the three support pathways.
- Opens How It Works.
- Opens Privacy Notice.
- Selects Create Account.
- Successfully reaches Phase A registration.
- Returns and signs in.
- Opens urgent-help resources without authentication.
- Revisits Welcome appropriately when signed out.

Expected outcome:

All actions work, content is truthful, and navigation has no dead ends.

## Persona 2 — Creative User

An unconventional but legitimate visitor.

Scenarios:

- Switches between existing supported languages.
- Uses Persian and RTL.
- Enlarges text substantially.
- Uses a narrow mobile browser.
- Navigates entirely by keyboard.
- Uses a screen reader.
- Opens a deep link directly to public policies.
- Uses browser Back and Forward.
- Opens several public links in different tabs.
- Changes country selection in public-resource discovery.
- Reads public content without creating an account.
- Rotates a tablet or phone.
- Uses reduced-motion settings.

Expected outcome:

The interface remains accessible, clear, and functional without demanding unnecessary personal information.

## Persona 3 — Confused User

A visitor who misunderstands or interrupts the experience.

Scenarios:

- Thinks a listener is a therapist.
- Expects immediate human help.
- Assumes the service is an emergency hotline.
- Tries to open a member-only community without registering.
- Clicks Create Account twice.
- Goes back from registration.
- Has an expired session.
- Experiences a slow or failed policy request.
- Has no network connection.
- Does not know which support pathway is appropriate.
- Is under 18 and wants guidance.
- Finds no local crisis resource.
- Navigates to a removed or invalid informational URL.

Expected outcome:

Provide truthful explanations, useful recovery paths, and safe alternatives without confusing redirect loops.

Never lead a distressed visitor to believe immediate help is guaranteed when unavailable.

## Persona 4 — Malicious User

An adversarial visitor.

Scenarios:

- Attempts to access private feeds as Guest.
- Calls protected APIs without tokens.
- Manipulates route parameters.
- Requests unpublished policy documents.
- Requests unapproved public educational material.
- Attempts to retrieve user information through public resource endpoints.
- Changes locale or region parameters to bypass authorization.
- Inserts unexpected values in public API query parameters.
- Attempts open redirect through navigation links.
- Uses malformed deep links.
- Attempts age-restricted social actions without authorization.
- Tries to enumerate private listener/professional data from public availability endpoints.
- Uses excessive public API requests.
- Attempts to exploit untrusted content rendered in the web experience.

Expected outcome:

Unauthorized data remains inaccessible, routes fail safely, and the frontend does not reveal sensitive information.

Do not run adversarial tests against production or real users.

---

# 8. Automated test requirements

## Frontend unit/component tests

Cover at minimum:

- B-01: Mission content displays correctly.
- B-02: Support pathways are distinct and accurate.
- B-03: Create Account navigation.
- B-04: Sign In navigation.
- B-05: Urgent Help navigation without authentication.
- B-06: Service boundary explanations.
- B-07: Legal and policy links.
- B-08: Availability fallback messaging.
- B-09: Accessibility semantics and responsive states.
- B-10: How It Works / FAQ navigation.
- Authenticated versus unauthenticated Welcome behavior.
- Policy loading and error states.
- Underage public resource navigation.

Use the repository's existing frontend testing tools where suitable.

If the repository has no component-testing framework, add a minimal maintainable setup consistent with the stack.

## Backend integration tests

Verify:

- Public policy endpoints work without tokens.
- Public crisis resources work without tokens.
- Public approved resources are accessible to guests.
- Unapproved resources cannot be retrieved.
- Protected social endpoints reject unauthenticated access.
- Public geographic data does not expose private member information.
- Incorrect or excessive parameters fail safely.
- Phase A account and role restrictions remain intact.

## Web end-to-end tests

Use Playwright or another suitable existing E2E framework.

Cover:

- Fresh visitor to Welcome.
- Welcome to registration.
- Welcome to login.
- Guest to urgent-help resources.
- Guest to legal policies.
- Guest to approved public resources.
- Guest blocked from private social features.
- Authenticated session navigation.
- Browser refresh and deep-link behavior.
- RTL and keyboard interactions.
- Error and offline states.

## Mobile validation

Where native automation exists, run it.

Otherwise provide a documented Android and iOS manual smoke-test checklist.

Do not claim mobile E2E tests passed if they were not executed.

## Visual regression tests

Where tooling permits, capture representative screenshots for:

- Desktop
- Tablet
- Mobile
- RTL
- Large text
- Error state

Review layout stability and major regressions.

Do not treat a screenshot alone as evidence of functional correctness.

---

# 9. Copywriting and trust audit

Before finalizing, perform a content audit of the entire unauthenticated experience.

Create a copy matrix:

- Existing statement
- Intended meaning
- Evidence supporting it
- Risk of misunderstanding
- Proposed wording
- Location in UI

Pay particular attention to:

- Safety claims
- Privacy claims
- Trained listener claims
- Moderation claims
- Professional verification claims
- Availability claims
- Medical and emergency disclaimers

Do not invent success rates, user counts, treatment effectiveness data, response times, or endorsements.

Provide final user-facing copy through the project's localization system.

Ensure terminology stays consistent between Welcome, How It Works, FAQ, and onboarding.

---

# 10. Privacy and analytics

The Welcome experience must not collect mental health information before registration.

Do not introduce behavioral profiling of guests.

If basic product analytics are implemented, minimize collection.

Permissible examples, subject to applicable policies:

- Anonymous page-load aggregate
- Registration CTA usage
- FAQ navigation aggregate
- General error counts

Do not include:

- Inferred mental health condition
- Private support topics
- Personal crisis details
- Message contents
- Clinical information
- Identification of vulnerable visitors through sensitive browsing behavior

Respect consent rules and current privacy configuration.

Do not add third-party trackers merely to measure conversion.

---

# 11. Engineering constraints

The implementation must:

- Reuse existing GreenOcean components.
- Preserve Phase A API contracts.
- Preserve current navigation and authentication security.
- Avoid unrelated architectural rewrites.
- Avoid unnecessary dependencies.
- Avoid unnecessary database migrations.
- Avoid exposing confidential data.
- Remain compatible with existing users.
- Work with the existing Expo design and localization systems.
- Avoid hardcoded production availability claims.
- Preserve the public urgent-help route.
- Preserve existing legitimate registration/login flows.
- Use backend authorization for all protected operations.

Do not move private social data into public endpoints merely to make the Welcome page appear more complete.

Do not create fake sample users or fake activity counters.

Do not alter existing applied Flyway migrations.

---

# 12. Implementation workflow

## Step 1 — Audit

Inspect the actual latest code.

Produce the B-01 through B-10 implementation matrix.

Identify existing reusable components and Phase A contracts.

## Step 2 — UX and information architecture

Refine the page structure and user-facing messages.

Choose the smallest clear interface that satisfies the requirements.

Do not introduce redundant steps.

## Step 3 — Implement Frontend

Update existing pages and components first.

Create new components only when needed.

Preserve mobile and web compatibility.

## Step 4 — Backend integration

Connect to existing public policies, resources, geographic configuration, and availability services.

Only implement missing backend functionality if required for the approved Phase B behavior.

## Step 5 — Copy and accessibility

Audit the entire unauthenticated experience.

Correct unsupported promises, accessibility problems, inconsistent translations, and responsive layout issues.

## Step 6 — Automated tests

Implement and execute tests for B-01 through B-10 across all four personas.

Include frontend, backend, E2E, and security tests where applicable.

## Step 7 — Regression verification

Confirm:

- Phase A registration still works.
- Phase A account eligibility still works.
- Login still works.
- Email verification is not bypassed.
- Public resources remain accessible.
- Private member data remains protected.
- Authenticated app navigation still works.
- No migrations or user records are damaged.

## Step 8 — Delivery

Commit changes on a dedicated branch.

Open a Pull Request.

Do not merge directly into main.

---

# 13. Git workflow

Create a dedicated branch based on the actual correct Phase A implementation baseline.

Suggested branch:

`feature/phase-b-welcome-trust-experience`

Before creating the branch, check whether another active branch or PR already contains unfinished Phase A changes.

Avoid duplicate work.

Do not force-push.

Do not commit secrets, tokens, private files, real user information, or test credentials.

Use clear, focused commits.

Do not automatically merge the Pull Request.

---

# 14. Mandatory delivery report

At completion, provide the following.

## A. Implementation coverage

| Requirement | Status | Files changed | Tests | Notes |
|---|---|---|---|---|
| B-01 | ... | ... | ... | ... |
| B-02 | ... | ... | ... | ... |
| B-03 | ... | ... | ... | ... |
| B-04 | ... | ... | ... | ... |
| B-05 | ... | ... | ... | ... |
| B-06 | ... | ... | ... | ... |
| B-07 | ... | ... | ... | ... |
| B-08 | ... | ... | ... | ... |
| B-09 | ... | ... | ... | ... |
| B-10 | ... | ... | ... | ... |

Possible final states:

- COMPLETE_AND_TESTED
- IMPLEMENTED_NOT_FULLY_TESTED
- BLOCKED_EXTERNAL_DEPENDENCY
- NOT_IMPLEMENTED

## B. Changed code

List modified frontend and backend files.

Explain each meaningful change.

## C. Tests

Report actual:

- Commands executed
- Unit tests passed/failed
- Integration tests passed/failed
- E2E tests passed/failed
- Accessibility checks
- Mobile checks
- Skipped or unexecuted tests

Never fabricate results.

## D. Security review

Confirm:

- No protected user information is public.
- No private social endpoint was exposed.
- No Phase A eligibility rule was weakened.
- No unsupported trust claim remains.
- No fake service capacity was introduced.

## E. Product review

Explain how the new Welcome experience builds trust and helps visitors decide whether GreenOcean is appropriate for them.

## F. Remaining limitations

List operational, legal, geographic, and technical dependencies.

Do not mark blocked features as complete.

## G. Pull Request

Provide the branch name, PR link, change summary, and CI status.

---

# 15. Definition of Done

Phase B is only complete when:

1. All B-01 through B-10 requirements have been assessed against the real codebase.
2. Existing correct implementations have been retained.
3. Missing features have been implemented or transparently identified as blocked.
4. Welcome accurately explains GreenOcean.
5. Community, listener, and professional pathways are distinguishable.
6. Registration and login navigation work.
7. Public urgent-help access works without an account.
8. Applicable public policies are accessible.
9. No unsupported service-availability promises remain.
10. The experience works on the required screen sizes and supported platforms.
11. Sensitive user information is not exposed to guests.
12. Existing Phase A access rules remain enforced.
13. Automated tests cover Happy, Creative, Confused, and Malicious personas.
14. Actual test evidence is available.
15. A reviewable Pull Request has been created.

**Do not stop at generating recommendations or a design document.**

Inspect the real current implementation and proceed to code changes, tests, regression checks, and a reviewable Pull Request.

If an external dependency blocks part of the work, implement the independent parts, preserve safe defaults, and clearly document the blocked requirement.

**The required outcome is a functioning, tested, privacy-preserving, trustworthy Welcome and pre-registration experience that builds on the completed Phase A foundation.**
