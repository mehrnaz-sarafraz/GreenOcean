# GreenOcean — Phase I
## First Real Support: Safe Live Listener Acceptance, Session Lifecycle & Support Journey

### Complete Full-Stack Implementation, Privacy, Security, Integration Testing and Delivery

---

# 1. ROLE AND OBJECTIVE

You are the principal full-stack engineer, software architect, backend security engineer, database engineer, product UX engineer, privacy-by-design specialist, and quality assurance lead for GreenOcean.

Your assignment is to fully implement:

**Phase I — Receiving the First Real Support**

Repository:

`mehrnaz-sarafraz/GreenOcean`

Technology stack:

- Java 21
- Spring Boot / Spring Security
- PostgreSQL
- Flyway
- React Native
- Expo / Expo Router
- TypeScript
- Web / Android / iOS

This phase connects the authenticated, eligible GreenOcean member's chosen support pathway to an actual, safe, authorized support experience.

The primary focus is a trustworthy, live, text-based peer-listener experience.

The implementation must also preserve and integrate the existing community, shared-experience, educational-content, reporting, and urgent-help pathways.

**Implement working code, not merely an architectural proposal or UI mockup.**

Do not rewrite previously completed phases.

Do not introduce unnecessary duplicate infrastructure.

Do not claim actual human availability without evidence.

---

# 2. APPROVED PRODUCT MODEL

The owner has approved:

**Model A — Live conversation only after real listener acceptance, without a persistent waiting queue.**

This is binding.

## 2.1 Approved experience

A member can:

1. Open Support discovery.
2. Choose to speak with a trained peer listener.
3. Review listener availability and service boundaries.
4. Explicitly request a live conversation.
5. Wait briefly for an eligible listener to accept.
6. Cancel while waiting.
7. Enter the conversation only after real acceptance.
8. Exchange messages during the active session.
9. End the session voluntarily.
10. Report or block unsafe behavior.
11. Explore alternative support if nobody is available.

## 2.2 Prohibited behavior

Never:

- Create a listener conversation merely because the member selected a listener.
- Match a member to an offline listener as if the listener were available now.
- Assume an online flag proves real readiness.
- Claim a listener accepted before the backend records acceptance.
- Let a member force the acceptance operation.
- Open messaging before acceptance.
- Automatically enroll a member in an unrelated conversation.
- Preserve a timed-out request as a hidden asynchronous waiting queue.
- Automatically restart expired requests.
- Create fake listener availability for production users.
- Present peer support as therapy, emergency intervention, or clinical treatment.

## 2.3 Initial implementation parameters

Use the following proposed MVP configuration unless a previously approved product contract specifies otherwise:

| Parameter | Initial value |
|---|---|
| Communication | Live text chat |
| Persistent waiting queue | Disabled |
| Offer acceptance window | 90 seconds |
| Simultaneous active sessions per listener | 1 |
| Simultaneous pending requests per member | 1 |
| Simultaneous live listener sessions per member | 1 |
| Listener availability | Explicit readiness + recent authenticated heartbeat |
| Request expiration | Server-authoritative |
| Backgrounded listener | Not eligible for new requests |
| Automatic retry after expiration | Disabled |
| Conversation creation | Only after valid acceptance |
| Session end | Explicit and server-enforced |
| Alternatives | Existing community, stories, educational content and urgent help |

Make operational timing parameters configurable, bounded, and covered by tests.

The owner has approved the overall live-acceptance model. If a timing or lifecycle decision materially changes user expectations, medical-safety claims, retention, or service obligations, present the alternatives for owner approval rather than silently inventing policy.

Do not pause unrelated safe engineering work.

---

# 3. IMPORTANT: LATEST CODE BASELINE

Previous phases were implemented incrementally.

Previously observed:

- Phase A — Identity, privacy, eligibility and access
- Phase B — Welcome and trust
- Phase C — Minimal registration
- Phase D — Email verification
- Phase E/F — Authentication and sessions
- Phase G/H — Profile privacy and progressive onboarding

The previous GitHub branch for G/H was:

`feature/phase-gh-profile-privacy-onboarding`

PR:

`https://github.com/mehrnaz-sarafraz/GreenOcean/pull/6`

At the last inspection, PR #6 was open and in Draft status.

Its implementation included:

- Private profile handling
- Durable post audiences
- Optional onboarding state
- Stable preference identifiers
- Conservative sensitive-topic collection
- Initial support-destination routing

However, a completed Phase I listener acceptance lifecycle was not present.

### Mandatory first steps

1. Fetch the latest GitHub repository state.
2. Inspect the latest Phase G/H branch and implementation.
3. Confirm the latest valid source commit.
4. Inspect relevant PRs and CI results.
5. Confirm whether G/H validation has completed.
6. Select the correct feature branch baseline.
7. Avoid using stale main when necessary upstream changes remain on stacked branches.

Do not require unrelated PRs to be merged simply to start development.

Do not automatically merge them.

Create a separate Phase I implementation branch, suggested:

`feature/phase-i-first-real-support`

Keep the dependency chain clear.

---

# 4. MANDATORY TARGETED REUSE AUDIT

Before implementing Phase I, inspect existing code.

Focus on relevant components only.

### Backend

- `ListenerService`
- `ListenerRepository`
- `ListenerController`
- `ListenerResponse`
- `ListenerMatchResponse`
- `MessagingService`
- `MessagingRepository`
- `MessagingController`
- `ConversationResponse`
- `MessageResponse`
- `AccessPolicy`
- `CommunityService`
- `CommunityRepository`
- `NotificationService`
- `NotificationRepository`
- `ReportService`
- `ReportRepository`
- Listener application and training workflows
- Administrative listener approval workflow
- Professional service authorization
- User blocking
- UserPreferences
- Account erasure/export
- Relevant migrations

### Frontend

- `frontend/src/app/(app)/support.tsx`
- `frontend/src/app/(app)/listener/[action].tsx`
- `frontend/src/app/(app)/chat/[id].tsx`
- `frontend/src/app/(app)/messages.tsx`
- `frontend/src/app/(app)/notifications.tsx`
- Existing communities and feed routes
- Existing articles/resources routes
- Existing report/block controls
- AuthProvider and DataProvider
- Existing localization and theme

### Previously observed defects

Verify each issue against the latest code:

1. `ListenerService.match()` selected an online listener or fell back to an offline listener.
2. Matching directly created or reused a conversation.
3. No explicit listener acceptance was required.
4. `listener_profiles.online` was a stored Boolean, not a reliable proof of current availability.
5. The chat page could send messages based on conversation membership without a separately enforced live support-session lifecycle.
6. Listener training and admin approval existed, but independent practice-session approval evidence was not clearly established.
7. Some UI copy implied that a listener was ready before acceptance.
8. Support-channel online participant counts were not sourced from a genuine presence service.

These are baseline observations, not assumptions about the latest code.

Fix demonstrated defects rather than blindly rewriting current implementations.

---

# 5. REQUIRED GAP MATRIX

Create:

`docs/phase-i/gap-matrix.md`

Cover every requirement I-01 through I-18.

For each requirement document:

- Current behavior
- Existing components
- Relevant A–H dependencies
- Missing behavior
- Proposed smallest implementation
- Backend scope
- Frontend scope
- Database scope
- Privacy/security risks
- Required tests
- Final evidence

Classifications:

- VERIFIED_AND_REUSED
- REUSED_WITH_TARGETED_TESTS
- PARTIALLY_IMPLEMENTED
- DEFECTIVE
- MISSING
- BLOCKED_EXTERNAL_DEPENDENCY
- NEEDS_OWNER_DECISION

Do not rebuild working community, notification, authentication, professional, or moderation services.

---

# 6. PHASE I ARCHITECTURE CONTRACT

Before implementation, create:

`docs/product/live-peer-support-contract.md`

Define:

- Member support request
- Listener eligibility
- Listener readiness
- Listener presence
- Listener capacity
- Request assignment
- Request acceptance
- Request rejection
- Request expiration
- Cancellation
- Conversation activation
- Active support session
- Session ending
- Network interruption
- Reporting and blocking
- Session retention
- Operational availability
- Service unavailability

### Core principle

**Discoverability, online presence, readiness, request acceptance, and active conversation are five different concepts.**

Never collapse them into a single `online` Boolean.

### Core safety invariants

1. Only eligible members may request listener support.
2. Only approved, authorized listeners may receive requests.
3. Presence alone does not equal readiness.
4. An expired request cannot be accepted.
5. A cancelled request cannot be accepted.
6. A rejected request cannot create a conversation.
7. Acceptance is an authenticated listener action.
8. Only one accepted or active session may occupy the same listener's MVP capacity.
9. An active session must belong to exactly the authorized participants.
10. An ordinary direct message cannot masquerade as a live listener session.
11. Blocking or suspension must revoke applicable access.
12. Server-side checks control all lifecycle transitions.
13. No client-provided status is authoritative.
14. No user is automatically matched later through a persistent queue.
15. No sensitive onboarding data is required to obtain support.

---

# 7. REQUIRED REQUEST AND SESSION STATE MACHINE

Do not model the entire workflow using one generic `matched` Boolean.

Implement a server-authoritative lifecycle.

### Proposed request states

| State | Meaning |
|---|---|
| REQUESTED | Member submitted a live request to an eligible listener |
| ACCEPTED | Listener accepted before the deadline |
| ACTIVE | Both parties have entered an authorized active session |
| DECLINED | Listener explicitly rejected the request |
| CANCELLED | Member cancelled before activation |
| EXPIRED | Acceptance or activation deadline passed |
| COMPLETED | Active session ended normally |
| ENDED_SAFETY | Session ended because of blocking, suspension, or safety restriction |
| INTERRUPTED | Session temporarily unavailable under the approved reconnection policy |

Implement only states that have real server meaning.

A separate request state and session state may be cleaner than a single combined enum.

Document the chosen model.

### Allowed transitions

- REQUESTED → ACCEPTED
- REQUESTED → DECLINED
- REQUESTED → CANCELLED
- REQUESTED → EXPIRED
- ACCEPTED → ACTIVE
- ACCEPTED → CANCELLED
- ACCEPTED → EXPIRED
- ACTIVE → COMPLETED
- ACTIVE → ENDED_SAFETY
- ACTIVE → INTERRUPTED, if a documented reconnection policy requires it
- INTERRUPTED → ACTIVE or a terminal outcome, under strict server rules

Terminal requests may never return to REQUESTED or ACCEPTED.

A user must explicitly create a new request.

### Request timeouts

The 90-second acceptance deadline is measured using trusted server/database time.

Do not trust device clocks.

If a listener accepts exactly when the deadline expires, concurrent transactions must produce one deterministic outcome.

For accepted requests that never become active, use a short bounded activation deadline. Document and test the chosen value.

Do not reserve a listener indefinitely in ACCEPTED.

### No automatic clinical escalation

Session expiration or user inactivity must not be treated as evidence of a medical emergency.

Do not automatically classify the user's mental-health status.

---

# 8. I-01 — Entry from Onboarding and Support

Priority: P0

Reuse Phase H's existing support routing.

Expected pathways:

- SHARED_EXPERIENCES → permitted shared-experience feed
- SUPPORT_GROUPS → existing community discovery
- LISTENER → existing Support/listener discovery
- PROFESSIONAL_KNOWLEDGE → appropriate educational content
- NO_SELECTION → neutral Support discovery

### Requirements

- Preserve G/H optional onboarding.
- Do not create a request during navigation.
- No automatic matching.
- No auto-join to communities.
- No automatic professional appointment.
- No new sensitive-data collection.
- Preserve regional eligibility and account-state checks.
- Explain unavailable paths accurately.

The listener journey begins only after an intentional user interaction.

---

# 9. I-02 — Account and regional authorization

Priority: P0

Reuse `AccessPolicy`.

Before requesting, receiving, accepting, activating, reading, or sending where relevant, verify:

- Account exists and is ACTIVE.
- Email verification and adult eligibility rules apply.
- Geographic service is authorized.
- Account is not suspended or deleted.
- Member and listener are allowed to participate.
- Blocking rules permit the interaction.
- Listener-specific service permission is valid.
- The request is not stale or already terminal.

### Critical restrictions

Do not create new geographic authorization tables when the existing regional-services model is sufficient.

Do not enable any country by default.

Do not allow a PENDING or ineligible account to request a live listener through a direct API call.

Public urgent resources remain independently accessible.

---

# 10. I-03 — Listener eligibility and authorization

Priority: P0

A listener must be more than a user who has a listener profile.

Inspect the existing training, application and review workflow.

Requirements:

- Approved listener application
- Required training completion
- Valid authorized listener status
- Active eligible user account
- Applicable regional permission
- No suspension
- No conflicting block relationship
- Any required supervised practice sign-off under the approved listener policy
- No revoked listener authorization
- Available capacity

### Previously observed concern

The existing administrator flow checked completion of training modules before approving a listener.

Verify whether supervised practice is a required policy condition and whether the backend records its approval.

If policy requires independent supervised-practice approval and this is not implemented, do not invent an implicit approval.

Implement the approved missing control, or keep listener activation unavailable pending the required owner/operations decision.

Do not grant listener status based only on a self-selected role or frontend field.

### Legacy accounts

Do not arbitrarily remove existing legitimate roles.

Review migrations and legacy listener records carefully.

Where authorization evidence is uncertain, preserve data and fail closed for new live matches until a safe approved transition exists.

---

# 11. I-04 — Genuine listener presence and readiness

Priority: CRITICAL P0

The existing `listener_profiles.online` field must not be treated as conclusive proof of availability.

Implement an explicit server-authoritative readiness mechanism.

### Listener controls

The listener must be able to:

- Set themselves available for new support requests.
- Set themselves unavailable.
- See whether they are currently available.
- View an incoming live request.
- Accept or decline.
- End an active session.
- Recover safely after a temporary connection interruption.

### Presence

Use an authenticated, bounded heartbeat or equivalent real connection-presence mechanism.

Suggested technical configuration:

- Foreground heartbeat approximately every 15 seconds.
- Presence expires after approximately 45 seconds without valid renewal.
- All values configurable and tested.
- Backgrounded clients stop claiming active availability.
- Expired authentication or revoked listener role invalidates readiness.
- New requests are not offered on stale presence.
- Multiple devices cannot accidentally bypass one-listener capacity.

The exact transport may reuse the existing architecture.

A full WebSocket infrastructure is not mandatory if bounded polling meets the MVP requirements.

### Distinguish states

- OFFLINE
- ONLINE_NOT_AVAILABLE
- AVAILABLE_FOR_REQUESTS
- BUSY
- UNAVAILABLE

These can be derived states rather than a new independently mutable state column.

Do not make `ONLINE` imply `AVAILABLE_FOR_REQUESTS`.

### Anti-spoofing

A client may renew only its own authorized readiness.

A client cannot submit another listener's user ID to control presence.

Use server time and short expiration leases.

Stale device leases must not override newer authenticated availability changes.

### Accurate user-facing copy

Only display availability supported by real server evidence.

When availability is uncertain, use language such as:

"Check current availability."

Do not fabricate live numbers or claim continuous online presence.

---

# 12. I-05 — Create a live request without creating a conversation

Priority: CRITICAL P0

The user explicitly presses a button requesting a live conversation.

The backend:

1. Validates the requester.
2. Validates the target listener or selects an eligible listener.
3. Checks real readiness.
4. Checks capacity.
5. Checks blocking and service authorization.
6. Creates a short-lived request record.
7. Records a server-authoritative expiration deadline.
8. Exposes the pending request to that listener.
9. Returns the request identifier and current status.

**At this point, no active conversation exists.**

### Automatic listener selection

When no listener is explicitly chosen:

- Select only actually available eligible listeners.
- Apply a simple documented fair selection policy.
- Do not fall back to offline listeners.
- Respect language when genuinely provided and supported.
- Do not infer diagnoses or use sensitive onboarding topics while their processing gate is disabled.
- Do not guarantee a match.

### Topic handling

Support topics may be sensitive personal data.

Do not automatically copy onboarding topics into session requests.

Do not require a clinical or mental-health category to request support.

If optional matching topics are retained from the old listener UI, review their lawful processing status and use stable identifiers.

Keep sensitive preference collection disabled unless its previously defined approval gate is satisfied.

### Abuse prevention

Implement bounded controls against:

- Rapid repeated requests
- Duplicate taps
- Request spam
- Listener-targeted harassment
- Forged listener identifiers
- Multiple concurrent requests from one member

Reuse existing rate-limit infrastructure where appropriate.

---

# 13. I-06 — Real listener acceptance or rejection

Priority: CRITICAL P0

The listener must receive the live offer through an authorized listener interface.

The listener may:

- Accept
- Decline
- Become unavailable, causing the offer to close safely

### Accept

Acceptance must:

- Require the authenticated targeted listener.
- Verify current role and authorization.
- Verify the request is still REQUESTED.
- Verify the server deadline.
- Verify the requester is still eligible.
- Verify blocking has not occurred.
- Verify the listener still has capacity.
- Atomically reserve the listener for the accepted session.
- Record acceptance once.

### Decline

Declining must:

- Move the request to DECLINED.
- Release provisional capacity.
- Notify the requester through the approved in-app mechanism.
- Never create a conversation.

### Concurrency

Two devices belonging to the listener must not both accept independently.

Two members competing for one free listener must not both succeed.

Acceptance should have a single transactional winner.

No frontend-only lock is sufficient.

---

# 14. I-07 — Expiration and cancellation

Priority: P0

### Acceptance deadline

Default: 90 seconds.

Expired requests are permanently terminal.

A delayed background worker cannot recreate an expired request.

The backend must reject acceptance after expiration, even if cleanup has not yet run.

### Member cancellation

The member may cancel while REQUESTED or ACCEPTED-before-activation.

Cancellation must be idempotent.

A simultaneous listener acceptance/member cancellation race must have a deterministic transactional result.

Do not open a conversation after cancellation wins.

### Background expiration

Use existing scheduling conventions.

Prefer database-enforced expiration semantics plus bounded cleanup.

A scheduler delay must not make expired requests valid.

### No queue

Expired, rejected, or cancelled requests must never become persistent waiting items automatically.

The user may initiate a fresh request after returning to the discovery screen.

---

# 15. I-08 — Atomic capacity enforcement

Priority: CRITICAL P0

MVP capacity:

- One active or accepted-but-not-yet-activated session per listener.
- One pending or active listener request/session per member.

Use database transactions and constraints/locking to enforce the rules across multiple application instances.

### Required scenarios

- Two members select one listener simultaneously.
- One listener accepts two offers simultaneously.
- Member requests using two devices.
- Listener accepts using two devices.
- Acceptance races expiration.
- Acceptance races cancellation.
- Acceptance races suspension.
- Acceptance races blocking.
- Listener disconnects after accepting.
- Server restarts during acceptance.
- Database transaction fails halfway.

No scenario may create duplicate active sessions, unauthorized participants or permanently occupied capacity.

Use indexes, constraints and transaction ordering consistent with PostgreSQL.

Do not depend on a process-local mutex for cross-instance correctness.

---

# 16. I-09 — Conversation creation only after acceptance

Priority: CRITICAL P0

Reuse the existing messaging infrastructure.

Do not build a second independent chat application.

### Required behavior

A live listener conversation must be linked to one accepted support session.

Use a durable session/request identifier.

Only the approved request's member and listener can participate.

### Existing concern

The previous listener match path directly created a DIRECT conversation.

The existing direct-conversation reuse query may also find a DIRECT conversation between the same two people.

This must not allow an ordinary personal conversation to bypass a new listener acceptance requirement.

### Required technical boundary

- Preserve the distinct LISTENER service kind.
- Differentiate listener sessions from ordinary DIRECT messages.
- Do not reuse ordinary direct conversations as authorized listener sessions.
- Do not let a client forge the LISTENER service kind.
- Ensure there is a valid accepted session before creating or activating listener messaging.
- Make conversation creation atomic and idempotent.
- Create at most one conversation per accepted session.
- Preserve any existing historical listener conversations without silently converting them to active new sessions.

### Legacy endpoint compatibility

Inspect:

`POST /api/v1/listeners/match`

Do not leave this endpoint able to create conversations without acceptance.

Retire, replace, or safely adapt the endpoint.

If old client versions call it, fail closed with a documented upgrade response instead of bypassing security.

### Activation

After listener acceptance, the requester explicitly enters or starts the accepted session.

Do not create an active conversation solely due to navigation.

Use a bounded activation deadline to avoid indefinite capacity reservation.

---

# 17. I-10 — Active session lifecycle

Priority: P0

A live session requires clear state and ownership.

### Member interface

- Listener identity
- Support boundaries
- Active conversation status
- Message area
- End conversation
- Report
- Block
- Appropriate network/error feedback
- No false guarantee of guaranteed privacy or immediate emergency monitoring

### Listener interface

- Current member session
- Session status
- Messaging
- End session
- Report
- Relevant safety guidance
- Clear indication of workload/capacity

### Completion

Either participant can end the session.

Ending must:

- Mark the session terminal.
- Stop new outgoing messages.
- Release capacity.
- Prevent reuse of the old session as a new live support session.
- Prevent background reconnect from reopening a completed session.
- Preserve or remove historical data according to existing approved retention policy.

### Disconnection

A disconnected device does not automatically mean the user intentionally ended the conversation.

Implement a conservative bounded reconnection strategy.

However:

- Do not keep a listener marked available for new sessions simply because an old device is disconnected.
- Do not silently allow an interrupted session to remain falsely active forever.
- Do not infer a crisis merely from inactivity.
- Do not fabricate a successful reconnection.

Document how server restarts, network failures and app backgrounding affect sessions.

---

# 18. I-11 — Messaging permissions during active support

Priority: CRITICAL P0

Update existing message authorization.

For LISTENER conversations, sending a message requires:

1. Authenticated participant.
2. Active eligible account.
3. Authorized live listener session.
4. Appropriate session state.
5. No applicable block.
6. No relevant suspension.
7. Appropriate regional permission.
8. Non-terminal session.
9. No revoked listener authorization where continuing support is prohibited.

### Security requirements

- Reject sending before acceptance.
- Reject sending before activation.
- Reject sending after completion.
- Reject sending after safety termination.
- Reject sending through forged conversation IDs.
- Reject unauthorized third parties.
- Reject ordinary-DM bypass.
- Restrict message payload sizes.
- Reuse approved moderation and reporting mechanisms.
- Handle duplicate client submissions safely.
- Use existing server-enforced identity for sender attribution.

### Reading previous messages

Reading and exporting previously authorized session history may have different rules than sending.

Do not delete history simply because the live session ended.

Preserve existing approved owner access, deletion and safety-record policies.

Do not allow a blocked participant to continue receiving new private data through polling.

Do not invent end-to-end encryption or absolute confidentiality claims.

---

# 19. I-12 — No-listener-available experience

Priority: P0

No listener availability must be treated as a normal operational state, not as a crash.

### Required UI

If there are no eligible available listeners:

- State that no live listener is currently available.
- Do not create a conversation.
- Do not automatically add the user to a waiting list.
- Permit checking availability again through an explicit action.
- Offer existing alternative support pathways.
- Keep urgent-help resources accessible.
- Do not recommend unavailable professional care as though it were ready.

### Alternatives

Reuse existing:

- Community discovery
- Shared experiences/feed
- Approved educational content
- Country-aware public urgent resources

These are choices, not automatic substitutions.

### Availability wording

Avoid:

- "Someone is ready now" without evidence.
- "You are matched" before acceptance.
- "Guaranteed support".
- "24/7 listener coverage" without actual operational support.

A lack of listener availability must not create false hope or an endless spinner.

---

# 20. I-13 — Integrate existing non-listener support paths

Priority: P0

Phase I includes the ability to take a meaningful first action through existing non-clinical alternatives.

### Shared experiences

Reuse:

- Post/feed read APIs
- Explicit audience
- Privacy and blocking
- Moderation
- Optional reaction/comment/posting where already supported

Do not bypass Phase G historical post visibility.

### Support communities

Reuse:

- Community discovery
- Membership controls
- Private-community approval
- Existing conversation channels
- Existing reporting and block controls

Do not auto-join a user to a group.

### Professional knowledge

Reuse:

- Published educational articles
- Professional verification indicators
- Existing region restrictions

Educational content is not the same as a professional consultation.

### Urgent resources

Reuse the existing public country-aware urgent-resource directory.

Do not invent phone numbers, providers, or local coverage.

No clinical triage engine is part of this phase.

---

# 21. I-14 — Safety, reporting and blocking

Priority: CRITICAL P0

Safety controls are part of the support experience, not hidden secondary features.

### Requirements

- Member can end the session at any time.
- Listener can end the session at any time.
- Relevant users can report unsafe conversation behavior.
- Existing report authorization rules are reused.
- Blocking prevents further prohibited contact.
- Block changes affect already-active listener conversations.
- A blocked pair cannot restart a live session without the applicable server-side policy permitting it.
- A user cannot evade a block by requesting a new listener session or using an old conversation.
- Suspended listeners cannot accept new requests.
- Suspended members cannot maintain unauthorized active support.

### Reports

Reuse existing moderation report infrastructure.

Extend the existing report target model only if genuinely required.

Avoid duplicating reports or collecting unnecessary raw conversation transcripts.

If access to reported conversation evidence is needed for authorized safety investigation, define purpose-limited access and the applicable preservation policy.

Do not create unrestricted administrator access to private conversations.

### Operational safety

The existing staff moderation interface may depend on privileged MFA and operational enablement.

Do not bypass that protection for Phase I.

If no approved staff/on-call process is available, clearly report the release blocker for live human support rather than claiming continuous supervision.

### Immediate danger

Provide visible access to urgent help resources.

Explain that peer listeners do not provide emergency intervention.

Do not promise response times from external emergency services.

---

# 22. I-15 — Frontend UX, accessibility and localization

Priority: P1

Reuse GreenOcean's existing design system.

Support:

- Web
- Android
- iOS
- English
- Persian
- RTL
- Large text
- Screen readers
- Keyboard navigation on web
- Mobile safe areas
- Accessible loading states
- Accurate status announcements

### Member screens

Minimum screens or states:

1. Support discovery.
2. Listener availability.
3. Request confirmation.
4. Waiting for acceptance.
5. Accepted and ready to enter.
6. Active conversation.
7. Request rejected.
8. Request expired.
9. Request cancelled.
10. No listener available.
11. Conversation ended.
12. Recoverable network failure.

Extend existing screens rather than adding unnecessary navigation layers.

### Listener screens

Minimum states:

1. Listener available/unavailable control.
2. Incoming request.
3. Request deadline.
4. Accept/decline.
5. Busy/active session.
6. End conversation.
7. Expired offer.
8. Network interruption.

### Important timer behavior

Display a countdown based on the backend-provided expiration timestamp.

The client timer is presentational.

The backend remains authoritative.

Do not imply acceptance succeeded until the server confirms it.

### Refresh and navigation

- Recover session state after app reload.
- Prevent stale accepted screens after expiration.
- Do not navigate into a forbidden chat.
- Avoid duplicate request submission.
- Respect logout/account switching.
- Wipe sensitive session views and caches as required by E/F.

---

# 23. I-16 — Privacy-preserving operational measurement

Priority: P1

The system needs operational visibility without constructing mental-health profiles.

### Appropriate minimum operational measures

Where supported by approved policy:

- Number of live requests
- Request acceptance rate
- Request expiration rate
- Request cancellation rate
- Concurrent active sessions
- Listener capacity utilization
- Technical delivery failures
- Time to acceptance in aggregated form
- Ended sessions by operational status

### Do not put into general analytics

- Chat message bodies
- Health topics
- Diagnoses
- Onboarding interests
- Journal text
- Private community details
- Identifiable clinical inferences
- Unnecessary individual behavioral profiles

Use existing operational logging where adequate.

Do not add third-party tracking.

### Retention

Separate:

- Transient live presence
- Request lifecycle metadata
- User messages
- User-accessible history
- Safety report evidence
- Security audit events
- Aggregate metrics

These may have different retention and access rules.

Do not invent indefinite retention.

If a material retention decision is missing, document the dependency for Phase J.

Use privacy-minimizing implementation defaults without deleting historical content unexpectedly.

---

# 24. I-17 — Comprehensive testing

Priority: CRITICAL P0

All new workflows must be covered by four required GreenOcean personas.

## Persona 1 — Happy Path

Test:

- Eligible member logs in.
- Member selects live listener support.
- Available approved listener appears.
- Member requests a conversation.
- Listener receives the request.
- Listener accepts within the deadline.
- Member enters the accepted session.
- Both participants exchange messages.
- Member ends the conversation.
- Capacity is released.
- Old session cannot be reopened as active.
- The user can return to Support discovery.

## Persona 2 — Creative User

Test:

- Persian and RTL interface.
- Unicode profile names.
- Zero selected support topics.
- Changing support style after onboarding.
- Listener using multiple devices.
- Member using multiple devices.
- Simultaneous client refreshes.
- Backgrounding and foregrounding the application.
- Slow network.
- Duplicate button presses.
- Switching accounts.
- Session recovery after browser reload.
- Alternate community/stories pathway.

## Persona 3 — Confused User

Test:

- Requesting support without choosing a topic.
- Trying to match when nobody is available.
- Cancelling while waiting.
- Leaving the waiting screen.
- Returning after expiration.
- Reconnecting after internet loss.
- Mistaking pending request for accepted request.
- Assuming peer support is clinical treatment.
- Listener goes offline before accepting.
- Listener declines.
- User attempts to enter an expired chat.
- User reports a problem.
- User returns to alternative support without losing navigation state.

## Persona 4 — Malicious User

Test:

- PENDING account requests support.
- Ineligible or suspended account requests support.
- Unsupported region attempts service access.
- Unapproved user impersonates a listener.
- Forged listener availability update.
- Forged heartbeat for another listener.
- Member accepts their own request.
- Member forges listener acceptance.
- Another listener accepts an unrelated request.
- Expired request accepted through direct API.
- Cancelled request accepted through direct API.
- Duplicate request race.
- Two members compete for one listener.
- Two devices accept concurrently.
- Cross-account request status disclosure.
- Forged conversation ID.
- Message sent before acceptance.
- Message sent after completion.
- Old direct conversation bypass.
- Block bypass.
- Suspension race.
- Rate-limit evasion.
- Stale session token reuse.
- Reconnect after revocation.
- Unauthorized access to reports or session metadata.
- Sensitive data leakage through logs or analytics.

All adversarial testing must use synthetic accounts and isolated environments.

---

# 25. Required concurrency and failure tests

Create dedicated deterministic tests for:

1. Two members requesting the last free listener slot.
2. Two acceptance transactions for the same request.
3. Expiration versus acceptance.
4. Cancellation versus acceptance.
5. Blocking versus acceptance.
6. Suspension versus activation.
7. Logout versus listener heartbeat.
8. Requester deletion versus active session.
9. Listener privilege revocation versus active session.
10. Server restart after request creation.
11. Restart after listener acceptance but before conversation creation.
12. Network timeout after successful committed acceptance.
13. Duplicate idempotent activation.
14. Duplicate end-session calls.
15. DB transaction rollback with no leaked capacity.
16. Multiple backend instances operating concurrently.
17. Stale frontend state attempting to send after termination.

Use real PostgreSQL isolation, locking and constraints where relevant.

Mock-only tests are insufficient to prove these cases.

Every race must converge to a consistent authorized state.

---

# 26. Database and Flyway requirements

Do not modify applied migrations.

The previous G/H implementation introduced:

`V40__durable_audience_and_progressive_onboarding.sql`

Verify the current latest migration number before creating a new one.

Use the next available migration number.

### Potential minimal schema additions

Depending on the verified architecture:

- Listener presence/readiness lease
- Live support request
- Request expiration
- Request status transitions
- Accepted session association
- Listener capacity control
- Requester capacity control
- Session termination reason
- Optional minimal operational timestamps

Use clear foreign keys.

Use appropriate unique or partial unique constraints where feasible.

### Mandatory guarantees

- No duplicate active session per listener.
- No duplicate pending request per member.
- Only authorized participants.
- No active conversation for an unaccepted request.
- Idempotent retry behavior.
- Expiration remains effective even without immediate cleanup.
- Capacity released on terminal states.
- Historical messages and conversations preserved.
- Existing professional conversations unaffected.
- Existing ordinary direct messaging unaffected.
- Existing groups unaffected.
- Existing listener application records unaffected.

### Migrations

Test:

- Fresh schema.
- Upgrade from latest G/H schema.
- Populated listener profiles.
- Legacy conversations.
- Existing blocks and reports.
- Existing account deletion/export behavior.
- Existing roles and approvals.
- Failed migration recovery.

Do not run destructive migrations on production data.

Do not rewrite historical conversation participants or message bodies.

---

# 27. Suggested API contracts

These are illustrative routes.

Follow existing REST and naming conventions when choosing actual endpoints.

### Listener readiness

- `GET /api/v1/listeners/me/availability`
- `PUT /api/v1/listeners/me/availability`
- `POST /api/v1/listeners/me/heartbeat`

### Live requests

- `POST /api/v1/listener-support/requests`
- `GET /api/v1/listener-support/requests/{id}`
- `POST /api/v1/listener-support/requests/{id}/cancel`

### Listener incoming offers

- `GET /api/v1/listener-support/incoming`
- `POST /api/v1/listener-support/requests/{id}/accept`
- `POST /api/v1/listener-support/requests/{id}/decline`

### Conversation lifecycle

- `POST /api/v1/listener-support/requests/{id}/activate`
- `POST /api/v1/listener-support/sessions/{id}/end`
- `GET /api/v1/listener-support/sessions/{id}`

Do not expose arbitrary participant IDs as authority.

Use authenticated identity.

### DTO requirements

Responses should expose only what is necessary:

- Request ID
- Current status
- Server-defined deadlines
- Safe listener display information
- Authorized conversation/session ID when valid
- Available user actions
- Clear reason code for non-sensitive terminal outcomes

Avoid exposing other members' requests or identifying health information.

### API errors

Use stable response codes for:

- No available listener
- Request already open
- Listener busy
- Request expired
- Request cancelled
- Listener unavailable
- Access forbidden
- Service unavailable in region
- Invalid transition
- Rate limited

Avoid leaking sensitive details about other users.

---

# 28. Delivery transport and real-time behavior

Do not introduce complex infrastructure without evidence that it is necessary.

The existing app already uses periodic message fetching.

A bounded, well-tested polling-based MVP is acceptable if it provides:

- Timely incoming offers
- Accurate server-based deadlines
- Safe acceptance
- Recoverable connection failures
- No silent duplicate requests
- No false availability
- Correct listener busy state
- Clear user feedback

If WebSocket or Server-Sent Events infrastructure already exists and is production-supported, reuse it when beneficial.

Do not create both polling and a new messaging platform unnecessarily.

Transport delivery is never a substitute for database-transaction authorization.

The server must independently verify every state change.

---

# 29. Account lifecycle and session security

Integrate with Phases A, E and F.

### Requirements

- Suspended accounts lose live service authorization.
- Deleted accounts cannot initiate or continue live service access.
- Revoked authentication sessions cannot operate on requests.
- Password reset/change follows the approved session invalidation policy.
- Account switching invalidates old local support data.
- Background local privacy locking continues to work.
- Concurrent requests from multiple devices are handled safely.
- Private message content is not retained in global stale frontend caches after logout.
- Previously approved HttpOnly cookie and mobile secure token storage designs remain unchanged.

Do not weaken MFA or session policy to simplify listener workflows.

Do not introduce a second token model.

---

# 30. Training, supervision and operational safety readiness

Live peer support is a human service.

Code alone does not prove that trained listeners, supervisors, or incident responders are operationally available.

### Required verification

Review:

- Listener Code of Conduct
- Training modules
- Listener application approval
- Supervisor/practice review where required
- Role revocation
- Safety reports
- Escalation procedures
- Reporting access
- Moderator availability
- Regional service permission
- Staff MFA requirements

### Production gating

If genuine operational supervision or essential listener approval controls are unavailable, keep live service disabled in production.

Do not fabricate approved listeners.

Do not bypass staff MFA.

Do not claim 24/7 support.

Keep existing public resources accessible.

### Operational documentation

Provide a launch checklist covering:

- Approved listener roster
- Listener training evidence
- Staff coverage
- Handling abandoned requests
- Handling interrupted active conversations
- Responding to reports
- Revocation during active sessions
- Service shutdown
- Incident logging
- Privacy and retention responsibilities

---

# 31. I-18 — Handoff to Phase J and Phase K

Priority: P0

Phase I must produce evidence for the remaining Phase 1 workstreams.

### Phase J — Legal and privacy handoff

Document open product/legal decisions concerning:

- Support request metadata
- Sensitive topics
- Chat content processing
- User-facing confidentiality language
- Session history
- Report evidence retention
- Service boundaries
- Listener training and supervision claims
- Consent and notice where applicable
- Data subject access/deletion
- Real market availability

Do not independently declare GDPR compliance.

Do not introduce new sensitive-data processing without its required approval.

### Phase K — Final validation handoff

Provide an integration test plan for:

REGISTRATION
→ EMAIL VERIFICATION
→ LOGIN
→ OPTIONAL ONBOARDING
→ SUPPORT DISCOVERY
→ LIVE REQUEST
→ LISTENER ACCEPTANCE
→ ACTIVE SUPPORT
→ SESSION END
→ REPORT/BLOCK
→ LOGOUT
→ ACCOUNT EXPORT/DELETION

Also cover zero listener availability, unsupported regions and urgent public resources.

Phase K will later validate the combined A–J product.

Phase I must not claim complete production readiness for the whole platform.

---

# 32. Implementation packages

Use reviewable, focused increments.

## I-00 — Latest baseline and gap matrix

Inspect actual repository state and existing capabilities.

Do not repeat complete A–H audits.

## I-01 — Approved live support lifecycle contract

Create state diagram, security invariants, owner-decision register and API design.

## I-02 — Presence, readiness and capacity

Implement authenticated readiness and safe availability claims.

## I-03 — Request creation and acceptance

Implement short-lived requests, listener acceptance, decline, cancellation and expiration.

## I-04 — Transactional session activation

Create active conversations only after valid acceptance.

Close the legacy direct-match bypass.

## I-05 — Active session security

Enforce participant checks, message permissions, termination and safe recovery.

## I-06 — Member and listener UX

Implement real request and response states, including unavailable alternatives.

## I-07 — Safety and account integration

Connect blocking, reporting, regional service policy, listener authorization and lifecycle restrictions.

## I-08 — Tests and documentation

Run all new tests, relevant A–H regressions, migration tests and full-stack journeys.

Do not implement unrelated new features.

---

# 33. Test execution requirements

Run actual verification against the final source revision.

### Backend

- Unit tests
- Spring integration tests
- PostgreSQL tests
- Flyway migration tests
- Authorization tests
- Multi-transaction concurrency tests
- Request expiration tests
- Listener presence tests
- Session state tests
- Messaging security tests
- Blocking/suspension tests
- Data export/deletion regressions

### Frontend

- TypeScript
- ESLint
- Component tests
- Status transition tests
- Responsive web testing
- Persian/RTL verification
- Error and loading states
- App background/resume behavior
- Request and session restoration
- Accessibility tests

### Full-stack E2E

Run with:

- Actual frontend
- Running backend
- Disposable PostgreSQL
- Synthetic member
- Synthetic approved listener
- Synthetic unapproved listener
- Controlled transport and test credentials

Test the complete real acceptance path.

Do not claim it works using only mocked API responses.

### Native

Run supported checks for Android and iOS.

If physical device testing cannot be executed, report NOT_EXECUTED and provide the exact manual test checklist.

Do not represent a native export as proof of operating-system presence/background behavior.

### Regression

Run relevant existing A–H tests.

Pay particular attention to:

- E/F session isolation
- G private profile and post audiences
- H onboarding navigation
- Existing direct messaging
- Professional messaging
- Group conversations
- Blocking
- Moderation
- Account deletion

Do not rerun unrelated full historical audits during each small change.

Run the complete applicable regression suite at final delivery.

---

# 34. Required documentation

Create or update:

1. `docs/phase-i/gap-matrix.md`
2. `docs/product/live-peer-support-contract.md`
3. `docs/phase-i/state-machine.md`
4. `docs/phase-i/api-contract.md`
5. `docs/phase-i/privacy-and-threat-model.md`
6. `docs/phase-i/operations.md`
7. `docs/phase-i/test-evidence.md`
8. `docs/phase-i/delivery-report.md`
9. `docs/phase-i/phase-j-k-handoff.md`

Use existing documentation conventions.

Avoid duplicating prior phase documents.

---

# 35. Owner decision protocol

The owner wants meaningful product decisions made before potentially costly or irreversible implementations.

Ask the owner only when necessary.

### Request approval before:

- Introducing a persistent waiting queue.
- Changing Model A to another service model.
- Adding voice/video calls.
- Collecting new sensitive support topics.
- Introducing clinical risk assessment.
- Changing legal consent or retention policy.
- Expanding supported countries.
- Granting unapproved listener or professional privileges.
- Making historical private messages more accessible.
- Enabling real production listener services without approved staffing/supervision.
- Changing a material listener training/qualification requirement.
- Changing privacy-sensitive operational logging.
- Changing the approved live-support acceptance model.

### Decision format

When approval is necessary, provide:

- Exact issue
- Existing behavior
- Affected requirement
- Options
- Risks
- Your recommended option
- Safe behavior until approval

Ask related questions together.

Do not repeatedly ask for already-approved decisions.

Continue independent work while awaiting a product decision.

---

# 36. GitHub delivery requirements

Create a dedicated Phase I branch.

Use focused commits.

Do not force-push.

Do not silently change previous phase architecture.

Do not automatically merge to main.

Do not deploy to production.

### Pull Request must include

- Correct baseline and dependency chain
- Complete requirement matrix I-01–I-18
- Architecture and state-machine documentation
- Endpoint changes
- Database migrations
- Frontend implementation
- Privacy/security checks
- Test execution evidence
- Four-persona scenarios
- Remaining external blockers
- Operational safety limitations
- Phase J/K handoff

Provide accurate commit references and CI status.

Never fabricate test outcomes.

---

# 37. Final acceptance criteria

Phase I is complete only when:

1. The current G/H implementation baseline has been correctly reused.
2. All eighteen I requirements have been evaluated.
3. The listener matching process no longer creates unauthorized instant conversations.
4. Offline listeners are never silently used as available live listeners.
5. Listener readiness is server-authoritative and expires safely.
6. Only approved eligible listeners can accept requests.
7. The member explicitly creates a live request.
8. The listener explicitly accepts or declines.
9. Requests expire automatically and cannot be revived.
10. Member cancellation is supported.
11. Concurrent acceptance cannot exceed listener capacity.
12. A conversation is created or activated only after valid acceptance.
13. Listener sessions are isolated from ordinary direct messages.
14. Sending messages requires an active authorized session.
15. Sessions can be ended by either participant.
16. Completed sessions cannot be reopened through stale client requests.
17. Blocking, suspension, account deletion and role revocation are enforced.
18. The user receives honest feedback when no listener is available.
19. Existing group, stories and educational pathways remain functional.
20. Public urgent resources remain available where authorized.
21. Sensitive personal data is not unnecessarily collected or logged.
22. Listener UX and member UX both work.
23. Persian, English, RTL and accessibility requirements are covered.
24. All four user-persona categories have test evidence.
25. Database race conditions are tested.
26. Actual relevant automated tests are executed.
27. Migration safety is demonstrated.
28. Operational and external release blockers are documented.
29. No unapproved production service is enabled.
30. A reviewable Pull Request is available.

Do not mark the phase complete merely because a listener card displays an Online badge or because a new endpoint returns a conversation ID.

**Successful listener acceptance and correctly authorized live interaction must be demonstrated.**

---

# 38. Final report format

Provide:

## A. Current State

Latest baseline, previous code reused, gaps identified.

## B. Implemented Requirements

I-01 through I-18 with:

- Change summary
- Files modified
- Relevant tests
- Final status

## C. State Machine and API

Actual transitions, endpoints and authorization rules.

## D. Database Changes

Migrations, indexes, constraints, preservation guarantees.

## E. Security and Privacy

Threats, mitigations, tests, remaining risks.

## F. Four-Persona Results

Happy, Creative, Confused and Malicious.

## G. Test Results

Exact commands, actual passes, failures, skipped and not executed.

## H. Operational Blockers

Staffing, verified listeners, geographic service approval, real devices, monitoring and legal review.

## I. Phase J/K Handoff

Remaining legal and release-readiness dependencies.

## J. Pull Request

Branch, URL, commits, CI and reviewer checklist.

---

# FINAL EXECUTION DIRECTIVE

Implement Phase I as a genuine, secure, text-based live peer-support experience.

Start from the latest valid GreenOcean G/H baseline.

Reuse the existing listener, messaging, authorization, profile, community, notification and reporting infrastructure.

Do not rebuild earlier phases.

Replace the unsafe direct listener-matching behavior with a durable, server-authoritative request/acceptance/session lifecycle.

Ensure listener readiness is genuine, acceptance is explicit, capacity is enforced transactionally, timeouts are authoritative, and messaging is impossible without proper session authorization.

Keep the experience voluntary, private by design, non-clinical, and honest about availability.

When no listener is available, offer real alternatives without false promises or a hidden waiting queue.

Preserve all existing roles, account security, data, migrations and privacy policies.

Test normal, unusual, confused and malicious user behaviors.

Document actual outcomes and remaining operational dependencies.

Complete the implementation, focused tests, full-stack verification, documentation and reviewable Pull Request.

**The expected result is that an eligible GreenOcean member can safely request, receive acceptance for, participate in, and end a real live text conversation with an authorized peer listener — while retaining full control over their participation and without being misled about support availability or clinical boundaries.**
