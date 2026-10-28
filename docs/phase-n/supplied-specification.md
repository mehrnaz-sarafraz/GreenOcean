# GreenOcean — Phase N
## Listener Scheduling, Bounded Waiting Queue, Scheduled Peer Support and Voluntary Follow-Up

### Comprehensive Implementation Specification
### N1 + N2 + N3 — Three Sequential, Independently Reviewable Pull Requests

---

# 1. MISSION

You are the principal full-stack engineer, Java/Spring Boot architect, PostgreSQL concurrency specialist, React Native engineer, privacy-by-design architect, service-safety engineer and quality assurance lead for GreenOcean.

Repository:

`mehrnaz-sarafraz/GreenOcean`

Technology stack:

- Java 21
- Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router
- TypeScript
- Web / Android / iOS

Your assignment is to implement the complete:

**Phase N — Extended Listener Support Services**

GreenOcean is a privacy-first, non-clinical, peer-support platform.

Its listeners are trained and independently authorized peers, not automatically licensed healthcare professionals.

The purpose of Phase N is to extend the existing Phase I real-time text-support system with:

1. Real listener schedules.
2. Genuine bookable appointment slots.
3. Optional, time-bounded waiting queues.
4. Safe queue-to-live-session handoff.
5. Scheduled-session check-in and acceptance.
6. Bounded session durations and consensual extensions.
7. Cancellation and no-show management.
8. Optional post-session feedback.
9. Optional repeat booking.
10. Operational controls, privacy protection and complete testing.

This is an implementation assignment, not merely a planning or documentation exercise.

Preserve all existing security, privacy and listener qualification requirements.

Do not turn GreenOcean into a clinical, diagnostic, emergency-response or continuously monitored service.

---

# 2. OWNER-APPROVED PRODUCT DECISIONS

These decisions are already approved and must not be questioned again.

## N-D01 — Three support pathways

Members must be able to choose among:

- Live Now
- Join Waiting Queue
- Book a Future Session

Each option has different availability and timing requirements.

The user must explicitly choose a pathway.

Opening the Support screen must not automatically create a request, queue entry or booking.

## N-D02 — Twenty-minute waiting limit

The waiting queue has a maximum duration of:

**20 minutes from server-confirmed admission.**

After expiry:

- The queue entry becomes terminal.
- The member is informed.
- No automatic renewed queue entry is created.
- No conversation is created.
- Any temporary allocation is released.
- Other support alternatives remain accessible.

The twenty-minute duration does not promise that support will be available.

## N-D03 — Booking from genuine listener calendars

Members select bookable time slots published by currently qualified, eligible listeners.

The schedule must be real server-side availability, not a static interface calendar.

A booked slot is a reservation of a support opportunity, not a guarantee that a real conversation will start.

Actual start requires genuine member and listener readiness, acceptance and member activation.

## N-D04 — Thirty-minute planned sessions

The planned duration of a support session is:

**30 minutes.**

Support controlled, limited extensions when:

- Both participants explicitly agree.
- The listener remains qualified and available.
- Capacity permits it.
- There is no conflict with another booking.
- The session is still authorized.
- The approved extension policy allows it.

Never silently extend a session.

Never promise an indefinite conversation.

Provide clear advance notice before an authorized time boundary.

## N-D05 — Optional follow-up

After a completed session, a member may voluntarily:

- Provide lightweight feedback.
- Review permitted session metadata.
- Request another session.
- Rebook the same listener when eligible and available.

Do not automatically message users for follow-up.

Do not automatically schedule recurring sessions.

Do not infer treatment needs from the conversation.

---

# 3. SELECTED ADDITIONAL IMPLEMENTATION DEFAULTS

The owner has also accepted the previously recommended operational defaults.

Use these as initial documented product parameters, not hidden assumptions.

| Parameter | Initial policy |
|---|---|
| Queue maximum | 20 minutes |
| Queue participation | Explicit opt-in |
| Queue concurrency | One active queue entry per member |
| Queue ordering | FIFO among currently eligible requests |
| Queue activation | Only when real, approved listener coverage exists |
| Queue result | Explicit member readiness followed by Phase I acceptance |
| Queue duration extension | None automatically |
| Bookable session duration | 30 minutes |
| Session extension | One additional 10-minute extension at most |
| Extension consent | Both participants |
| Extension constraint | No conflict with next commitment |
| Maximum upcoming bookings | Two per member |
| Booking lookahead | Initially 14 days, configurable |
| Minimum booking lead time | Initially 30 minutes, configurable |
| Member check-in opens | Five minutes before appointment |
| No-show grace period | Ten minutes after scheduled start |
| Session reminders | Optional and privacy-preserving |
| Cancellation penalty | None initially |
| Same-listener rebooking | Optional, with current qualification and availability |
| Clinical topic collection | Never required |
| External AI analysis | Disabled unless separately approved |

All timing and limit parameters must be defined centrally and validated.

Do not scatter hardcoded values throughout frontend components.

The existing Phase I offer timeout, activation deadline, presence heartbeat and reconnect deadline remain authoritative unless a separately reviewed integration change is necessary.

An extension must not override participant revocation, session interruption or safety termination.

---

# 4. PHASE N EXECUTION PLAN

Implement Phase N as three sequential milestones.

## N1 — Listener Scheduling and Booking

Deliver:

- Listener weekly calendars.
- Timezone-safe availability rules.
- Exceptions and unavailable periods.
- Real bookable slots.
- Reservation lifecycle.
- Cancellation and rescheduling.
- Capacity control.
- Scheduling UI for listeners.
- Booking UI for members.
- Scheduled-session check-in.
- Safe integration with Phase I acceptance.
- Focused PostgreSQL and full-stack tests.

## N2 — Waiting Queue and Live Handoff

Deliver:

- Queue admission conditions.
- Twenty-minute server-enforced expiry.
- FIFO assignment.
- Explicit user readiness.
- Real qualified-listener matching.
- Phase I request creation.
- Atomic capacity protection.
- Cancellation, expiry and recovery.
- Low-disclosure notifications.
- Queue position/status UI.
- Concurrency and fairness tests.

## N3 — Duration, Follow-Up, Safety and Integration

Deliver:

- Thirty-minute planned session policy.
- Controlled extension.
- No-show handling.
- Appropriate session end states.
- Optional feedback.
- Optional repeat booking.
- Privacy/data-rights integration.
- Operational dashboards and alerts.
- Failure and recovery exercises.
- Full regression against A–M.
- Final Phase N report.

### Pull Request structure

Suggested branches:

- `feature/phase-n1-listener-scheduling`
- `feature/phase-n2-bounded-waiting-queue`
- `feature/phase-n3-followup-integration`

N2 must build on the validated N1 baseline.

N3 must build on the validated N2 baseline.

Create three separate, reviewable PRs.

Do not automatically merge, deploy or enable services in production.

---

# 5. VERIFY CURRENT REPOSITORY STATUS

Before implementation:

1. Fetch the latest repository state.
2. Inspect main and the existing PR stack.
3. Confirm Phase L1, L2 and L3 status.
4. Confirm Phase M1, M2 and M3 status.
5. Find the latest validated integrated baseline.
6. Inspect the current Flyway migration number.
7. Inspect Phase I live-support code and tests.
8. Inspect Phase J data-governance contracts.
9. Inspect Phase K readiness blockers.
10. Inspect subsequent privacy and moderation changes.

Previously observed:

- PR #9 merged Phase K into main.
- PR #10 implemented L1 as a Draft.
- PR #11 implemented L2 as a Draft.
- PR #12 implemented L3 as a Draft.
- PR #13 implemented M1 as a Draft.
- PR #14 implemented M2 as a Draft.
- M3 had not yet been confirmed as complete.

This is historical context only.

Verify current state independently.

### Required starting point

The intended Phase N baseline is the latest validated Phase M3 integration.

If M3 is not available:

- Do not claim it is complete.
- Do not silently rebuild M3 in Phase N.
- Identify whether safe independent N preparation can proceed.
- Avoid opening a PR against an incorrect dependency.
- Keep blocked work clearly documented.
- Do not bypass the expected L/M integration tests.

Never interpret synthetic tests as production authorization.

---

# 6. PRESERVE THE EXISTING PHASE I CONTRACT

This is a critical requirement.

Phase I already provides real listener acceptance and authorization.

Read:

`docs/product/live-peer-support-contract.md`

Inspect:

- `LiveSupportService`
- `LiveSupportController`
- `LiveSupportSettings`
- `ListenerAvailability`
- `LiveSupportView`
- `LiveAuthorizationController`
- `ListenerService`
- `ListenerRepository`
- `MessagingService`
- Listener availability UI
- Live member UI
- Live listener workspace
- Phase I migrations
- Phase I integration tests

### Existing Phase I behavior

A current member can explicitly request a genuinely ready listener.

The listener must actively accept.

The member must explicitly activate the accepted conversation.

Only then is a LISTENER service conversation created.

Existing default timing includes:

- 90-second live request offer.
- 60-second member activation opportunity.
- 15-second heartbeat interval.
- 45-second presence freshness.
- 120-second bounded reconnection after a stale deadline.

Exact current values must be verified in source.

Existing Phase I constraints include:

- Independent listener qualification.
- Current practice/conduct approval.
- Authenticated session-bound readiness.
- One open participant role capacity.
- Database-backed state transitions.
- Conversation ownership checks.
- Block and suspension enforcement.
- Regional eligibility.
- Safe interruption/reconnection.
- Idempotent message retries.
- No chat before genuine activation.
- No automatic reopening of terminal sessions.

### Non-negotiable

Phase N must extend this system rather than replace it.

Do not create a second listener messaging service.

Do not reuse ordinary DIRECT messaging to represent a listener session.

Do not treat a future calendar entry as a valid listener readiness lease.

Do not create a Phase I live request days in advance.

Do not change Phase I's 90-second live request into a twenty-minute request.

Queue and booking objects must be separate durable entities with separate state machines.

The Phase I request is created only during the actual authorized live handoff.

---

# 7. ARCHITECTURE REQUIREMENT MATRIX

Create:

`docs/phase-n/requirements-matrix.md`

Track N-01 through N-30.

Each requirement must record:

- Approved behavior.
- Relevant existing implementation.
- Identified gap.
- Backend modifications.
- Frontend modifications.
- Database modifications.
- Access control.
- Privacy impact.
- Failure states.
- Test cases.
- Exact completion evidence.

Use status values such as:

- VERIFIED_REUSED
- TARGETED_ENHANCEMENT
- MISSING
- IMPLEMENTED_TESTED
- APPROVAL_REQUIRED
- OPERATIONAL_BLOCKER
- NOT_EXECUTED

Do not use successful compilation as a substitute for runtime integration tests.

---

# PART I — N1: LISTENER SCHEDULING AND BOOKING

# 8. N-01 — Genuine listener scheduling

Priority: P0

Provide a listener-managed availability calendar.

Only listeners who currently meet all applicable qualification, account, region and operational authorization requirements can publish genuinely bookable support windows.

### Required capabilities

- Set recurring weekly availability.
- View upcoming scheduled windows.
- Add one-time availability exceptions.
- Mark holidays or unavailable periods.
- Remove future availability.
- Edit future availability safely.
- Pause booking availability.
- View confirmed bookings.
- See upcoming commitments.
- See booking conflicts.
- See cancellation and rescheduling states.

### Critical distinction

A published calendar slot is not proof that a listener is online.

A listener remains subject to Phase I's session-bound readiness lease at the actual handoff.

Do not set `listener_presence.ready=true` merely because the clock reaches the start of a scheduled window.

### Listener qualification

A schedule from a revoked or unqualified listener cannot produce new bookings.

Qualifying evidence must be evaluated again at booking, check-in and live handoff.

Past bookings must not preserve an expired listener authorization.

### Safe behavior

If a listener loses qualification:

- Stop offering their future slots.
- Restrict their upcoming bookings.
- Notify affected members without disclosing privileged details.
- Offer cancellation or an approved alternative.
- Do not silently assign an unqualified substitute.

---

# 9. N-02 — Timezone-safe calendar architecture

Priority: CRITICAL P0

Use proper timezone handling.

### Required

- Store instants in an authoritative UTC-compatible representation.
- Preserve the listener's IANA timezone identifier.
- Generate recurring local calendar rules correctly.
- Display slots in each viewer's applicable timezone.
- Handle daylight-saving transitions.
- Reject or explicitly resolve nonexistent local times.
- Disambiguate repeated local times.
- Never rely on the device clock for booking validity.
- Use PostgreSQL or a comparable server-authoritative time source.

### Test

Include:

- DST forward transition.
- DST backward transition.
- Cross-timezone booking.
- Listener timezone change.
- User timezone change.
- Browser clock skew.
- Duplicate-looking local times.
- Overnight availability.
- Week boundary.
- Long-lived recurring schedule.

Do not silently shift a booked session to a different instant when the viewer changes timezone.

---

# 10. N-03 — Actual bookable slots

Priority: P0

Generate bookable slots from approved listener availability.

Initial slot duration:

**30 minutes.**

Account for:

- Existing confirmed bookings.
- Required breaks and configurable buffers.
- Listener authorization.
- Service region eligibility.
- Existing current and upcoming commitments.
- Listener suspension.
- Approved coverage restrictions.
- Session extension capacity.
- Appointment cancellation.
- Scheduling horizon.

### Slot inventory

A slot must not remain bookable after it has been reserved.

Do not rely on frontend state to prevent duplicate booking.

Use the database as the authority.

### Concurrency

Two simultaneous users attempting to reserve the same slot must not both succeed.

Use appropriate PostgreSQL transaction serialization and uniqueness/exclusion constraints for overlapping listener intervals.

Test across multiple application instances.

Avoid introducing broad locks that unnecessarily serialize unrelated listeners.

---

# 11. N-04 — Reservation lifecycle

Priority: P0

Create a durable booking model distinct from live sessions.

Define a complete, documented state machine.

Suggested states:

- CONFIRMED
- CHECKIN_OPEN
- MEMBER_CHECKED_IN
- HANDOFF_PENDING
- HANDOFF_REQUESTED
- ACTIVE
- COMPLETED
- CANCELLED_BY_MEMBER
- CANCELLED_BY_LISTENER
- CANCELLED_BY_SERVICE
- MEMBER_NO_SHOW
- LISTENER_NO_SHOW
- EXPIRED

Use only the states actually required after reviewing the architecture.

Do not create redundant or contradictory status fields.

### Creation rules

- Booking action is explicit.
- Booking requires a valid slot.
- The member is eligible.
- The listener is currently qualified.
- The slot is actually available.
- The member does not exceed the permitted booking count.
- No conflicting commitment is present.
- Client retries are idempotent.
- A confirmed booking never creates an immediate conversation.

### Booking limits

Initially allow a maximum of two upcoming confirmed bookings per member.

Make the limit configurable.

Do not create a hidden permanent waiting list for appointment availability.

### Confirmation

Return a server-confirmed booking record.

The user should see:

- Listener social display identity.
- Date and time.
- Timezone.
- Planned duration.
- Cancellation options.
- Check-in expectations.
- Non-clinical service boundaries.

The wording must not promise that a session has already started.

---

# 12. N-05 — Booking cancellation and rescheduling

Priority: P0

Support member and listener cancellation.

### Member cancellation

- Explicit authorized action.
- Idempotent retries.
- Immediate release of future slot capacity where safe.
- Appropriate notification.
- No cancellation fee.
- No automatic replacement booking.

### Listener cancellation

- Requires authorized listener action or platform safety action.
- Notifies the affected member.
- Makes the original appointment unavailable.
- Provides alternatives where genuinely available.
- Does not silently transfer the conversation to another listener.

### Rescheduling

Implement as a safe atomic transaction or clearly defined cancel/rebook workflow.

Do not release an existing reservation and falsely claim a replacement unless the replacement is confirmed.

Do not double-book a listener under concurrent rescheduling.

### Abuse prevention

Repeated malicious bookings and cancellations may trigger proportionate rate controls.

Do not impose automatic punitive penalties on legitimate distressed users.

---

# 13. N-06 — Appointment check-in

Priority: CRITICAL P0

The scheduling system must connect to the existing Phase I acceptance lifecycle.

### Initial check-in window

Allow member check-in beginning five minutes before the booked start.

Use a ten-minute post-start grace period.

### Required conditions

Before handoff:

- The booking remains valid.
- The member is eligible.
- The listener is eligible.
- The service is enabled.
- The current region is approved.
- Neither party is blocked from the other.
- The listener has a valid live readiness lease.
- Both participants have available current capacity.
- No conflicting session exists.
- Required actual human coverage is present.

### Important

A booking must not automatically generate a conversation when the scheduled time arrives.

A checked-in member alone does not create a conversation.

### Scheduled handoff

1. The member checks in.
2. The listener explicitly becomes ready on an authenticated device.
3. The server confirms the reservation can proceed.
4. The system creates the appropriate current Phase I live request.
5. The targeted listener explicitly accepts.
6. The member explicitly activates.
7. Exactly one LISTENER conversation becomes active.
8. The booking becomes linked to that live session.

### Existing Phase I timing

Preserve the short-lived offer and activation deadlines.

Do not convert calendar check-in into a permanent accepted live request.

### Failed handoff

When either participant is unavailable:

- Return truthful status.
- Do not create fake conversations.
- Observe the approved grace period.
- Record a correct no-show or service-unavailable reason.
- Release unused capacity.
- Offer optional rescheduling or other paths.

Do not describe a listener who has lost authorization as merely temporarily offline when that distinction affects user expectations.

---

# 14. N-07 — Live-session and calendar capacity coordination

Priority: CRITICAL P0

The existing Phase I live support system enforces real-time participant capacity.

Phase N adds future commitments.

These must be coordinated centrally.

### Requirements

- One live conversation per participant under the existing role constraints.
- No overlapping confirmed listener bookings.
- No conflicting active queue allocations.
- No booking beyond approved schedule capacity.
- No double allocation when queue and calendar operations race.
- No new immediate assignment that predictably conflicts with an imminent booked commitment.
- No forced interruption of an active listener session merely to fulfill a newly arrived booking.

### Important design distinction

A future booking need not occupy the listener's immediate live capacity for the entire period before the appointment.

However, it must prevent conflicting booking commitments and must be accounted for in live admission near appointment time.

### Overrun handling

If a live conversation threatens a later confirmed booking:

- Provide early, transparent warnings.
- Avoid silently overbooking.
- Apply the approved duration and extension policy.
- Do not abruptly interrupt without the documented session ending procedure.
- Do not promise an upcoming full thirty-minute session if current constraints make that impossible.

Document any necessary transition policy between live support and scheduled reservations.

### Implementation

Use shared transactional capacity guards.

Review the existing `live_support_requests` constraints, advisory locks, database functions and authorization triggers.

Do not use only process-local mutexes.

---

# 15. N-08 — Listener and member scheduling interfaces

Priority: P0

Implement genuine frontend experiences.

### Listener calendar

- Weekly availability.
- Exception dates.
- Current bookings.
- Upcoming appointments.
- Current qualification state.
- Booking cancellations.
- Readiness control.
- No-show status.
- Clearly separated scheduling versus online readiness.

### Member booking experience

- Browse eligible listeners.
- View genuinely bookable slots.
- Choose a specific listener/time.
- Review planned thirty-minute session.
- Confirm booking.
- View upcoming bookings.
- Cancel or reschedule.
- Check in at the permitted time.
- Open the genuine authorized conversation after activation.

### Design

Reuse existing GreenOcean design tokens and navigation.

Support English, Persian and RTL.

Do not use decorative calendars that display availability which the backend does not confirm.

---

# PART II — N2: BOUNDED WAITING QUEUE AND LIVE HANDOFF

# 16. N-09 — Queue entry eligibility

Priority: CRITICAL P0

Implement a genuine server-backed waiting queue.

Queue admission must be opt-in.

### Queue admission requires

- Eligible member.
- Approved region.
- Enabled listener service.
- Enabled queue feature.
- Real eligible listener coverage.
- No conflicting active session.
- No duplicate queue membership.
- No prohibited account or block situation.
- Bounded queue capacity.
- Appropriate operational staffing conditions.

### Real coverage

Do not open a queue merely because an old listener profile exists.

A listener listed on a future calendar without actual coverage does not prove immediate queue capacity.

Use a defensible coverage rule based on currently qualified, operationally available listeners and approved staffing windows.

### No coverage

Return an honest unavailable state.

Offer:

- Check Live Now again.
- Browse actually available future booking slots.
- Return to other permitted GreenOcean features.
- Use independently available regional urgent resources where relevant.

Do not manufacture waiting entries when no reasonable coverage exists.

---

# 17. N-10 — Twenty-minute server-enforced queue expiry

Priority: CRITICAL P0

The queue expires twenty minutes after its authoritative server creation time.

### Required invariants

- The expiry deadline is immutable for the queue entry.
- The client clock cannot extend it.
- Refresh does not extend it.
- Reconnect does not extend it.
- Notification delivery does not extend it.
- A delayed scheduler does not extend it.
- Backend restart does not extend it.
- A stale worker cannot revive it.

### Terminal states

Suggested:

- CANCELLED
- EXPIRED
- HANDOFF_COMPLETED
- SERVICE_UNAVAILABLE
- SAFETY_TERMINATED

Use a precise state machine and document it.

### Failure behavior

At expiry:

- Stop the queue attempt.
- Release any temporary queue allocation.
- Invalidate pending proposals that have not entered an independent valid Phase I state.
- Inform the member.
- Do not automatically requeue.
- Preserve only approved bounded metadata.

### Expiry versus live request

Once the member explicitly confirms a proposed match and a valid Phase I live request is created, that live request follows its own existing short-lived deadlines.

Do not allow expired queue entries to create new Phase I requests afterward.

A successfully transferred request must not be silently destroyed merely because its parent queue deadline subsequently passes.

Document the exact atomic transfer point.

---

# 18. N-11 — Fair queue assignment

Priority: P0

Use FIFO ordering among eligible active entries.

### Requirements

- Server-authoritative ordering.
- Stable sequence or equivalent ordering key.
- Bounded pagination.
- No duplicate entries.
- No phantom positions.
- No hidden diagnosis-based priority.
- No preferential ranking based on inferred distress.
- No monetized priority.
- No assignment to a blocked or ineligible listener.

### Suitability

Use only approved, necessary eligibility criteria.

Examples may include:

- Qualified listener availability.
- Language support if explicitly selected.
- Current region.
- Existing block rules.
- Current participant capacity.

Do not infer a member's mental-health condition from previous messages, journal entries or group memberships.

### Queue position

You may display an approximate position only when it does not mislead the user.

Do not invent an estimated wait duration.

Do not reveal other queued members.

### Fairness

A cancelled or expired earlier entry must not block later members.

An ineligible request may be skipped without exposing another person's private information.

Use bounded selection and recovery behavior.

---

# 19. N-12 — Queue-to-live handoff

Priority: CRITICAL P0

When a genuinely ready, eligible listener becomes available:

1. Select an eligible queue member.
2. Create a short-lived allocation proposal.
3. Notify the selected member through permitted channels.
4. Ask the member to confirm that they are ready now.
5. Revalidate both parties and all permissions.
6. Create the actual Phase I live request using the existing authorization rules.
7. Require targeted listener acceptance.
8. Require explicit member activation.
9. Open exactly one authorized LISTENER conversation.

### No automatic conversation

Queue admission is not consent to start a chat at an arbitrary future moment.

A member must explicitly confirm readiness for the handoff.

### Member response window

Use a configurable short confirmation interval.

Suggested initial value:

60 seconds.

If the member does not respond:

- Release the temporary allocation.
- Mark the proposal expired.
- Continue the queue according to documented fairness rules.
- Do not force a conversation.
- Do not automatically create an indefinite second queue entry.

### Listener response

The listener must retain Phase I's real acceptance and readiness requirements.

If the listener declines or becomes unavailable, do not silently create an alternate live request without renewed appropriate member authorization.

### Idempotency

Client retries cannot create two Phase I requests or two conversations.

Link the queue, proposal, Phase I live request and conversation through auditable references.

---

# 20. N-13 — Queue cancellation and recovery

Priority: P0

Support cancellation at any permitted stage.

### Member actions

- Join queue.
- View queue status.
- Cancel.
- Confirm readiness.
- Decline a proposed match.
- Return to Support alternatives.
- Review expired outcome.

### Recovery requirements

- Browser reload.
- App backgrounding.
- Network reconnection.
- Backend restart.
- Session refresh.
- Multi-device login.
- Delayed response arrival.

The server must always be authoritative.

A stale client response cannot revive an expired queue entry.

After logout, remove protected queue data from local visible state.

Do not keep pretending that a member is actively ready merely because their app was previously open.

---

# 21. N-14 — Queue capacity and contention

Priority: CRITICAL P0

Test high-contention situations.

Examples:

- Two members competing for one free listener.
- One member entering the queue on two devices.
- A listener becoming ready during queue cancellation.
- A queue entry expiring during match creation.
- A booking check-in racing with a queue assignment.
- A direct Live Now request racing with queue allocation.
- Listener qualification revoked during selection.
- Both participants becoming blocked before acceptance.
- Two workers assigning the same listener.
- A server crash after temporary allocation but before request creation.

### Requirements

- No duplicate active assignment.
- No lost reservation capacity.
- No indefinite stuck queue state.
- Correct timeout recovery.
- No stale eligibility bypass.
- No unintended participant overlap.
- Deterministic terminal states.

Use PostgreSQL transactions, constraints and atomic transitions.

Do not rely on a single in-memory queue in a multi-instance backend.

---

# 22. N-15 — Queue notifications

Priority: P1

Reuse existing GreenOcean notification infrastructure.

Provide optional notifications when:

- Queue admission succeeds.
- A potential match is ready.
- User readiness confirmation is requested.
- Queue expires.
- Queue is cancelled due to service unavailability.

### Privacy

Avoid displaying sensitive support details in unrestricted push notifications.

Use generic, low-disclosure text.

Examples:

- "An update is available in GreenOcean."
- "Open GreenOcean to review your support request."

Do not include:

- Health conditions.
- Message text.
- Private topic labels.
- Session transcripts.
- Diagnostic inferences.

Respect consent and user preferences.

Notification delivery is not proof that the member received or accepted an offer.

Always require server-side confirmation.

---

# 23. N-16 — Preserve the direct Live Now pathway

Priority: CRITICAL P0

The existing immediate listener path must continue functioning.

Do not force every member through the queue.

### Live Now

- Explicit member request.
- Real currently ready listener.
- Genuine listener acceptance.
- Explicit member activation.
- Existing expiration and participant-capacity enforcement.

### Queue

- Explicit opt-in.
- Bounded wait.
- Optional readiness confirmation when matched.
- Handoff into Phase I.

### Scheduled booking

- Confirmed future slot.
- Explicit check-in.
- Real listener readiness.
- Handoff into Phase I.

All three pathways eventually use the same authorized live conversation lifecycle.

Do not create three incompatible messaging stacks.

---

# PART III — N3: SESSION DURATION, FOLLOW-UP AND SAFETY

# 24. N-17 — Thirty-minute planned support sessions

Priority: P0

Introduce a consistent server-authoritative planned session duration.

Initial value:

30 minutes.

### Scope

Apply the new duration policy to newly activated sessions governed by the approved Phase N service contract.

Coordinate the policy across Live Now, queue-origin and booked sessions.

Do not retroactively terminate historical Phase I sessions that were created under a different contract.

### User experience

Show:

- Actual server-confirmed session start.
- Planned duration.
- Clear remaining time.
- Advance ending notices.
- Availability of extension, if permitted.
- Optional voluntary early ending.

### Advance warning

Provide meaningful notice before the planned time ends.

Suggested initial warnings:

- Five minutes remaining.
- One minute remaining.

A countdown must be presentational; server time is authoritative.

### Session completion

End according to a documented, safe lifecycle.

Do not allow indefinite capacity occupation.

Do not silently cut the conversation without prior understandable notice.

Do not allow a session to continue sending after its final authorized deadline.

### Emergency distinction

A time limit is a service-capacity policy, not a clinical determination.

Do not infer crisis risk from a user's remaining session time.

Keep independently available urgent resources accessible.

---

# 25. N-18 — Limited consensual extension

Priority: CRITICAL P0

Provide one optional ten-minute extension.

Both participants must explicitly agree.

### Preconditions

- The original session is active and authorized.
- Neither participant is blocked or suspended.
- Listener qualification remains valid.
- The regional service remains available.
- Neither participant has conflicting commitments.
- The listener is not due to fulfill another confirmed reservation.
- The current session has not already used its extension allowance.
- Both approvals refer to the same session and extension request.

### Concurrency

Two simultaneous extension approvals must not produce twenty minutes of extension.

A repeated approval is idempotent.

An expired extension offer cannot revive an ended session.

### If extension is unavailable

Explain it respectfully.

Do not promise extra time.

Offer permitted future booking alternatives if genuinely available.

### No automatic continuation

There is no implicit consent based on silence or continued typing.

---

# 26. N-19 — No-show and delayed-start handling

Priority: P0

Use a ten-minute scheduled appointment grace period.

### Member no-show

If the member does not complete the required check-in by the deadline:

- Mark the correct no-show state.
- Release reserved current capacity.
- Do not create a conversation.
- Provide a truthful notice.
- Allow an optional new booking if eligible.

### Listener no-show

If the listener does not become genuinely available:

- Do not fabricate a ready presence lease.
- Do not start a conversation.
- Record the correct listener-unavailable/no-show state.
- Inform the member.
- Offer another genuine slot or eligible queue path.

### Delay

Do not automatically promise a full thirty-minute session if the start is delayed and doing so would overlap another commitment.

Show the feasible time window and require any necessary explicit consent to an adjusted start/end plan.

If the full planned service cannot reasonably be honored, provide rescheduling or cancellation.

### No financial penalty

Do not introduce fees or monetary penalties.

Repeated deliberate abuse may be handled through appropriate bounded access restrictions, with review.

---

# 27. N-20 — Optional post-session feedback

Priority: P1

After a completed session, show an optional lightweight feedback screen.

### Suggested fields

- Was the support experience helpful?
- Did you feel respected?
- Would you like to request support again?
- Would you like to report a concern?

Avoid requiring free-text disclosure.

### Important

Feedback is not a clinical outcome measure.

Do not label the member as recovered, improved or diagnosed.

Do not automatically treat dissatisfaction as evidence of a safety violation.

### Separation from reports

Maintain a clear independent route for reporting:

- Harassment.
- Unsafe conduct.
- Threats.
- Privacy violations.
- Inappropriate listener behavior.

Reuse existing reporting infrastructure.

### Privacy

Do not attach full transcripts to ordinary feedback.

Do not generate automated sentiment scores from conversation text.

Do not use responses for sensitive advertising or profiling.

Apply Phase J retention and data-rights requirements.

---

# 28. N-21 — Optional repeat booking

Priority: P1

A member may voluntarily request another session.

### Same listener

Allow selecting the same listener only if:

- Currently qualified.
- Account eligible.
- Regionally eligible.
- Not blocked.
- Future slot available.
- Current scheduling rules allow it.

### Different listener

Allow choosing a different available qualified listener.

### No automatic continuity

Do not:

- Automatically create weekly appointments.
- Automatically contact the member.
- Grant permanent direct-message access.
- Create a treatment relationship.
- Promise that the same listener will always be available.

### Context sharing

Do not automatically transfer previous message transcripts or sensitive journal material to the next listener.

Any future handoff-summary feature requires a separate approved privacy and purpose contract.

---

# 29. N-22 — Private follow-up notes

Priority: P2, conditional on privacy approval

The member may eventually benefit from private personal notes after a conversation.

However, such notes may contain highly sensitive information.

### Safe initial policy

Do not introduce a new durable sensitive note store without verifying the approved purpose, retention, export, deletion and access rules.

If those decisions are not approved:

- Keep personal-note storage disabled.
- Complete optional feedback and repeat booking independently.
- Document the pending decision.
- Do not block unrelated Phase N features.

Never let a listener view a member's private note by default.

Do not copy private notes into analytics or matching algorithms.

---

# 30. N-23 — Listener scheduling dashboard

Priority: P1

Provide a genuinely useful listener workspace.

### Display

- Current qualification.
- Current readiness.
- Current live session.
- Upcoming booked sessions.
- Approved weekly schedule.
- Availability exceptions.
- Pending check-ins.
- Cancellations.
- Required action to accept a scheduled request.
- Session ending notices.

### Important distinctions

Show clearly:

- Scheduled to be available.
- Currently online.
- Explicitly ready to accept a request.
- Busy.
- Booked for a future time.
- Temporarily unavailable.
- Authorization revoked.

These must not be represented as the same state.

### Safety

A listener cannot mark themselves eligible through a UI toggle if the backend qualification evidence is missing.

Do not let another device's stale readiness lease remain authoritative.

Maintain Phase I multi-device protections.

---

# 31. N-24 — Member Support dashboard

Priority: P1

The Support entry screen must provide three distinct pathways.

### Live Now

Displays genuinely currently available listeners.

### Waiting Queue

Displays:

- Whether queue service is currently available.
- Maximum waiting period.
- Current queue status.
- Expiry time based on server state.
- Cancellation action.
- Readiness confirmation when selected.

### Scheduled Support

Displays:

- Genuine future availability.
- Listener/time selection.
- Booking confirmation.
- Upcoming appointments.
- Cancellation/rescheduling.
- Check-in.

### History and follow-up

Shows permitted:

- Prior session status.
- Basic appointment metadata.
- Optional feedback state.
- Repeat booking actions.

Do not expose another participant's private session information.

### Service unavailable

Display realistic alternatives.

Do not promise 24/7 support.

Do not use emergency-service language for a non-emergency peer-support queue.

---

# 32. N-25 — Regional and operational authorization

Priority: CRITICAL P0

All support modes are subject to existing server-enforced regional eligibility.

### Required checks

- Account status.
- Age and eligibility policy.
- Residence/current region.
- Approved listener service scope.
- Actual listener qualification.
- Independent practice authorization.
- Approved conduct policy.
- Staff operations.
- Session authorization.
- Block and reporting restrictions.

### Operational readiness

A schedule cannot substitute for:

- Actual human listeners.
- Supervision.
- Incident response.
- Staff MFA.
- Approved launch region.
- Approved service policy.
- Documented escalation responsibility.

### Safe rollout

Use separate feature gates where appropriate:

- Scheduling enabled.
- Booking enabled.
- Queue enabled.
- Follow-up enabled.

These gates must remain dependent on the underlying Phase I live-support enablement and required approvals.

Default to disabled for unapproved production contexts.

Do not activate a market by merely applying a migration.

---

# 33. N-26 — Privacy and data governance

Priority: CRITICAL P0

Integrate with existing Phase J governance.

New records may include:

- Listener scheduling rules.
- Availability exceptions.
- Bookable slots.
- Reservations.
- Booking cancellations.
- Waiting queue entries.
- Temporary allocations.
- Appointment check-ins.
- Session duration metadata.
- Extension approvals.
- No-show records.
- Feedback.
- Authorized operational audit records.

### Requirements

Each data category must have:

- Clearly defined purpose.
- Owner/controller mapping.
- Appropriate access.
- Bounded retention.
- Export behavior.
- Deletion behavior.
- Backup limitations.
- Operational log restrictions.
- Incident handling.

### Never automatically collect

- Diagnosis.
- Medication details.
- Inferred mental-health conditions.
- Private onboarding support topics.
- Full conversation transcripts for scheduling.
- Personal journal contents.
- Sensitive engagement profiles.

### Export

A user may export their own applicable scheduling, queue and feedback data according to existing rights.

Do not disclose another listener/member's private information beyond the authorized participation record.

### Deletion

Deleting an account must invalidate:

- Future bookings.
- Active queue membership.
- Outstanding queue allocations.
- Associated access rights.

Preserve only records lawfully required under an actually approved policy.

Do not invent indefinite retention.

---

# 34. N-27 — Notifications and reminders

Priority: P1

Reuse existing notification infrastructure.

Support privacy-conscious reminders for:

- Booking confirmed.
- Booking cancelled.
- Booking changed.
- Appointment approaching.
- Check-in available.
- Listener unavailable.
- Queue match available.
- Queue expired.
- Session extension request.
- Optional follow-up action.

### Notification controls

- Respect preferences.
- Use low-disclosure text.
- Never include support narratives.
- Do not include full conversation contents.
- Avoid sensitive details on lock screens.
- Prevent duplicate reminder floods.
- Handle delayed notification delivery safely.

### Critical principle

A notification cannot independently authorize:

- Queue activation.
- Booking check-in.
- Live request acceptance.
- Session start.
- Session extension.

The user must complete the appropriate authenticated action.

---

# 35. N-28 — Safety, reporting and incident handling

Priority: CRITICAL P0

Reuse Phase I reporting and interruption controls.

### Preserve

- Listener acceptance.
- Block restrictions.
- Session end.
- Safety termination.
- Staff qualification revocation.
- Account suspension.
- Regional revocation.
- Current authenticated session validation.
- Message ownership checks.

### Do not create clinical claims

The scheduling system must not:

- Diagnose users.
- Assess suicide risk using unapproved automated inference.
- Promise crisis response.
- Promise continuous monitoring.
- Present a waiting queue as emergency care.
- Claim that a support session replaces professional treatment.

### Distress handling

Keep approved urgent resources available independently of booking and queue status.

Do not infer that a member who leaves a queue is safe.

Do not infer that a member who fails to check in needs an emergency intervention.

### Operational procedures

Document:

- Listener unexpectedly disconnects.
- Member disconnects.
- Listener loses qualification.
- Service is disabled.
- Emergency shutdown.
- Complaint during session.
- Booking cancellation due to safety.
- Queue failure.
- Supervisor unavailable.

Do not create false emergency escalation capabilities.

---

# 36. N-29 — Security and concurrency threat model

Priority: CRITICAL P0

Threat-model the complete system.

## Abuse scenarios

- Forged booking owner.
- Forged listener ID.
- Forged availability.
- Unauthorized scheduling another listener.
- Double booking.
- Duplicate queue creation.
- Queue jumping.
- Waiting time reset through retries.
- Reservation theft.
- Replay of expired slot.
- Reuse of cancelled booking.
- Booking after qualification revocation.
- Cross-role capacity overlap.
- Direct Live Now versus queue race.
- Queue versus scheduled booking race.
- Expired queue allocation accepted late.
- Extension after final deadline.
- Concurrent double extension.
- Fake check-in.
- Forged no-show status.
- Stale session token.
- Session ID enumeration.
- Cross-account booking history.
- Unauthorized feedback access.
- Booking cancellation by a different member.
- Malicious reminder generation.
- Multi-device duplicate activation.
- Backend restart during handoff.
- Queue scheduler running twice.
- Delayed worker committing a stale assignment.
- Timezone manipulation.
- Client clock manipulation.
- Old client bypassing new constraints.

## Technical controls

- Transactional state machines.
- Database uniqueness/exclusion constraints.
- Appropriate advisory/row locks.
- Server-issued timestamps.
- Validated idempotency keys.
- Owner-scoped queries.
- State revision/version checks.
- Authenticated role checks.
- Bounded API payloads.
- Rate limiting.
- Safe retry semantics.
- Consistent terminal states.
- Delayed-worker revalidation.
- Feature gates.
- Audit logging.

No system correctness may depend exclusively on one application instance's memory.

---

# 37. N-30 — Final integration and release verification

Priority: CRITICAL P0

Run complete Phase N integration tests.

Include all three entry paths.

### Direct Live Now

Member requests a real listener, receives explicit acceptance and activates exactly one conversation.

### Waiting Queue

Member joins queue, receives a genuine proposed match, confirms readiness and enters the existing Phase I handoff.

### Scheduled Booking

Member selects a real slot, books, checks in, waits for actual listener readiness, receives acceptance and explicitly activates the session.

### Duration

Session displays the thirty-minute plan.

Both participants can approve one bounded ten-minute extension when permissible.

Unauthorized or conflicting extensions fail.

### Follow-up

Member can give optional feedback and initiate an authorized new booking.

No automatic follow-up messages or treatment relationship are created.

### Regression

The work must not break:

- A–F account/security.
- G/H privacy.
- I live-support authorization and messaging.
- J data rights and consent.
- K integration baseline.
- L anonymity and content safety.
- M communities and group membership.

Do not broaden Phase N into a rewrite of these systems.

---

# 38. REQUIRED STATE MACHINES

Define and document at least four coordinated state machines.

## A. Listener schedule and slot

Suggested states:

- AVAILABLE
- RESERVED
- UNAVAILABLE
- CANCELLED
- COMPLETED

Actual state selection must avoid redundant derived state.

## B. Booking

Suggested lifecycle:

CONFIRMED
→ CHECKIN_OPEN
→ MEMBER_CHECKED_IN
→ HANDOFF_PENDING
→ HANDOFF_REQUESTED
→ ACTIVE
→ COMPLETED

With valid terminal cancellation, no-show and service-unavailable branches.

## C. Queue

QUEUED
→ MATCH_PROPOSED
→ MEMBER_CONFIRMED
→ HANDOFF_REQUESTED
→ TRANSFERRED

With EXPIRED, CANCELLED and SERVICE_UNAVAILABLE terminal branches.

## D. Live session

Preserve the existing Phase I lifecycle:

REQUESTED
→ ACCEPTED
→ ACTIVE

With existing interrupted/recovered/ended and safety termination behavior.

Attach duration and bounded extension metadata without creating a competing conversation lifecycle.

### State machine requirements

- Explicit permitted transitions.
- Authorized actor for each transition.
- Authoritative deadline.
- Idempotency behavior.
- Current qualification checks.
- Current session/capacity checks.
- Failure reason.
- Audit event where necessary.
- Safe restart reconciliation.

Produce sequence diagrams for the three support pathways.

---

# 39. DATABASE MIGRATIONS

Use additive Flyway migrations only.

Determine the latest version dynamically.

Do not rewrite historical Phase I or Phase J migrations.

### Suggested tables, subject to architecture review

- `listener_schedule_rules`
- `listener_schedule_exceptions`
- `listener_bookings`
- `listener_booking_events`
- `listener_wait_queue`
- `listener_queue_allocations`
- `listener_session_extensions`
- `listener_session_feedback`

Use existing entities wherever possible.

Avoid redundant storage of derivable time slots.

### Required database constraints

- No overlapping confirmed listener reservations.
- Bounded participant commitments.
- Unique active queue membership.
- Unique active queue allocation.
- Unique source-to-live-request handoff.
- Unique booking-to-live-session association.
- Valid appointment intervals.
- Valid terminal state transitions.
- Appropriate foreign keys.
- No invalid staff or user cross-reference.
- No unsafe orphaned capacity reservations.

### Migrations must preserve

- Existing listener profiles.
- Historical qualifications.
- Existing live requests.
- Existing conversations.
- Existing messages.
- Reports.
- Account export and deletion behavior.
- Current blocks.
- Existing schedules from unrelated professional services.

Do not conflate professional appointment calendars with listener peer-support scheduling.

Test fresh and populated database upgrades.

---

# 40. API CONTRACT

Reuse existing `/api/v1/listener-support` and `/api/v1/listeners` conventions.

Suggested additions, to be adapted to the actual repository:

## Listener calendar

- `GET /api/v1/listeners/me/schedule`
- `PUT /api/v1/listeners/me/schedule`
- `POST /api/v1/listeners/me/schedule/exceptions`
- `DELETE /api/v1/listeners/me/schedule/exceptions/{id}`
- `GET /api/v1/listeners/me/bookings`

## Member booking

- `GET /api/v1/listener-support/bookable-slots`
- `POST /api/v1/listener-support/bookings`
- `GET /api/v1/listener-support/bookings/me`
- `GET /api/v1/listener-support/bookings/{id}`
- `POST /api/v1/listener-support/bookings/{id}/cancel`
- `POST /api/v1/listener-support/bookings/{id}/reschedule`
- `POST /api/v1/listener-support/bookings/{id}/check-in`
- `POST /api/v1/listener-support/bookings/{id}/handoff`

## Queue

- `POST /api/v1/listener-support/queue`
- `GET /api/v1/listener-support/queue/me`
- `POST /api/v1/listener-support/queue/{id}/cancel`
- `POST /api/v1/listener-support/queue/{id}/confirm-match`
- `POST /api/v1/listener-support/queue/{id}/decline-match`

## Duration

- `POST /api/v1/listener-support/sessions/{id}/extension-request`
- `POST /api/v1/listener-support/sessions/{id}/extension-approve`
- `POST /api/v1/listener-support/sessions/{id}/extension-decline`

## Feedback

- `POST /api/v1/listener-support/sessions/{id}/feedback`
- `GET /api/v1/listener-support/sessions/{id}/feedback/me`

These are illustrative endpoint shapes, not mandatory duplicates.

Implement the smallest consistent API surface.

### API security

Every endpoint must enforce current authorization.

Use:

- Server-derived member identity.
- Real authenticated listener identity.
- Validated DTOs.
- Bounded pagination.
- Explicit state transitions.
- Safe error codes.
- Version/idempotency protection.
- Current service eligibility.
- Consistent clock handling.

Avoid leaking another member's reservation or queue information.

---

# 41. FRONTEND SPECIFICATION

Provide a coherent GreenOcean support experience.

## Main Support screen

Present three cards:

### Live Now

"Talk to an available listener."

Display true current availability.

### Waiting Queue

"Wait for an available listener."

Show:

- Optional participation.
- Twenty-minute limit.
- Current status.
- Cancel option.
- No guaranteed wait time.

### Schedule Support

"Choose a time for a future conversation."

Show:

- Genuine bookable availability.
- Listener selection.
- Timezone.
- Planned duration.
- Booking confirmation.

## Listener workspace

Show:

- Calendar.
- Upcoming appointments.
- Availability toggle.
- Current active conversation.
- Scheduled check-in.
- Pending offers.
- Explicit acceptance.
- Relevant extension prompts.

## Follow-up

After a completed session:

- Optional feedback.
- Optional repeat booking.
- Report a problem.
- Return to other GreenOcean areas.

### UX quality

Use:

- Existing design tokens.
- Persian and English translations.
- RTL support.
- Accessible status announcements.
- Screen-reader labels.
- Explicit loading, empty and error states.
- Honest server-confirmed actions.

Do not use fake or seeded listeners in normal production builds.

---

# 42. FOUR-PERSONA TEST PLAN

Use the established GreenOcean personas.

## Happy User

- Finds a genuinely available listener.
- Completes Live Now.
- Joins queue while eligible.
- Receives a match.
- Confirms readiness.
- Completes listener acceptance.
- Books future slot.
- Receives confirmation.
- Checks in.
- Completes a thirty-minute planned session.
- Uses a permitted extension.
- Gives optional feedback.
- Books another appointment.

## Creative User

- Persian and English.
- RTL and mixed scripts.
- Different timezones.
- DST transition.
- Starts a queue attempt then backgrounds the app.
- Changes devices.
- Cancels a booking.
- Reschedules.
- Declines queue match.
- Returns after connection interruption.
- Requests same listener again.
- Declines follow-up.
- Uses keyboard and accessibility controls.

## Confused User

- Cannot find an available listener.
- Mistakes queue for a confirmed booking.
- Expects the queue to remain indefinitely.
- Does not confirm readiness.
- Does not understand check-in.
- Arrives late.
- Does not receive a push notification.
- Assumes scheduling means the listener is online.
- Thinks acceptance automatically opens chat.
- Confuses a peer listener with a medical professional.
- Expects guaranteed crisis support.

The interface must explain outcomes truthfully without unnecessary complexity.

## Malicious User

- Double books the same listener.
- Submits duplicate idempotency keys.
- Joins multiple queues.
- Forges queue position.
- Replays an expired match.
- Uses another account's booking ID.
- Attempts unauthorized listener scheduling.
- Changes device clock.
- Attempts extension without consent.
- Attempts activation after revocation.
- Forges a check-in.
- Uses a stolen expired session.
- Creates races between queue and booking.
- Attempts cross-account feedback access.
- Exploits backend restart timing.
- Attempts to bypass staff qualification.
- Tries to access another participant's transcripts through export.

Use isolated test accounts and disposable data.

---

# 43. REAL CONCURRENCY TEST MATRIX

Run actual PostgreSQL-backed concurrency tests for at least:

1. Two members booking the same final slot.
2. Two members matching the same listener.
3. Immediate request versus queue assignment.
4. Scheduled check-in versus queue assignment.
5. Two queue workers selecting the same entry.
6. Queue cancellation versus match proposal.
7. Queue expiry versus readiness confirmation.
8. Member check-in versus booking cancellation.
9. Listener revocation versus scheduled handoff.
10. Booking cancellation versus rescheduling.
11. Same member queueing from two devices.
12. Same member booking from two devices.
13. Extension approval from both participants concurrently.
14. Duplicate extension after approval.
15. Extension versus upcoming reserved slot.
16. Session termination versus extension.
17. Reconnect versus duration deadline.
18. Booking no-show versus late check-in.
19. Listener schedule removal versus active reservation.
20. Backend restart with pending queue allocations.

Assertions must verify:

- One authoritative winner where required.
- No duplicate conversations.
- No capacity leaks.
- No invalid reactivation.
- No unauthorized access.
- Correct terminal states.

Use real transactions, not mock-only tests.

---

# 44. FULL-STACK JOURNEYS

Build and run actual frontend/backend/browser journeys with disposable PostgreSQL.

## Journey A — Immediate Support

Real readiness
→ explicit member request
→ listener acceptance
→ explicit member activation
→ text conversation
→ bounded session completion.

## Journey B — Queue Support

Queue admission
→ bounded waiting
→ genuine listener availability
→ match proposal
→ member readiness
→ Phase I live request
→ listener acceptance
→ member activation
→ text conversation.

## Journey C — Scheduled Support

Listener publishes schedule
→ member sees slot
→ member books
→ slot becomes unavailable
→ check-in
→ actual listener readiness
→ listener acceptance
→ member activation
→ conversation
→ duration completion.

## Journey D — Failure

Listener cancelled
→ member notified
→ no false session
→ slot released
→ appropriate rescheduling option.

## Journey E — Follow-Up

Completed session
→ optional feedback
→ optional same-listener booking
→ independently authorized new session.

## Journey F — Revocation

A listener loses approval between booking and check-in.

Verify:

- No unauthorized conversation.
- Booking status changes safely.
- Member receives a truthful outcome.
- No unauthorized transcript access.
- No stale capacity lock remains.

---

# 45. END-TO-END REGRESSION

Run the repository's actual build, test and validation commands.

Required categories:

- Backend unit tests.
- Real PostgreSQL integration tests.
- Flyway migration tests.
- State-machine tests.
- Capacity tests.
- Scheduling tests.
- Queue tests.
- Duration and extension tests.
- Authorization tests.
- React Native helper tests.
- Component/browser tests.
- True frontend/backend integration tests.
- English/Persian/RTL tests.
- TypeScript.
- Lint.
- Web export.
- Android/iOS JavaScript exports.
- Native device tests where actually available.

### Native validation

Do not claim physical Android/iOS tests passed unless they were performed.

Record:

- Device model.
- OS version.
- Build identifier.
- Accessibility settings.
- Network conditions.
- Result.
- Evidence.

### Preserve old tests

Especially preserve the complete Phase I listener acceptance and reconnection suite.

Do not modify or delete old assertions merely to make the new behavior appear correct.

---

# 46. OPERATIONAL READINESS AND FAILURE RECOVERY

Create operator-facing procedures.

### Required scenarios

- Queue service unavailable.
- No trained listeners.
- Schedule database unavailable.
- Notification provider unavailable.
- Booking handoff interrupted.
- Listener session unexpectedly terminated.
- Listener qualification revoked.
- Listener misses appointment.
- Member misses appointment.
- Backend restart.
- Queue worker restart.
- Timezone configuration error.
- Duplicate reminder delivery.
- Scheduling feature emergency shutdown.
- Underlying Phase I service disabled.

### Feature shutdown

Ensure queue and booking functionality can be disabled without invalidly reopening Phase I sessions or leaking protected information.

Prevent new submissions when disabled.

Reconcile existing entries according to the documented safe policy.

Do not silently delete historical records.

### Support alternatives

The user must retain access to permitted non-listener services and independently available urgent resources.

### Observability

Use approved bounded operational metrics such as:

- Queue admission success/failure.
- Booking conflict counts.
- Capacity reservation failures.
- No-show counts.
- Queue expirations.
- Stale worker recoveries.
- Listener qualification failures.

Do not introduce sensitive behavioral analytics or detailed conversation-text logs.

No production alert may be claimed as configured without a real recipient and delivery test.

---

# 47. RELEASE GATES

Maintain independent readiness dimensions.

### Technical implementation

Code, migrations and tests are complete.

### Privacy and security

New data categories and access controls are reviewed and verified.

### Human service operations

Actual listener roster and supervision exist.

### Geography and legal

The applicable jurisdiction and service scope have been approved.

### Infrastructure

Real notification delivery, monitoring, backups and capacity are verified.

### Native behavior

Required mobile device and assistive-technology checks are completed.

### Production authorization

Only the product owner and designated operators can authorize actual launch.

Do not equate technical completion with a public service release.

Keep unapproved production features disabled.

---

# 48. REQUIRED DOCUMENTATION

Create or update:

- `docs/phase-n/requirements-matrix.md`
- `docs/product/listener-scheduling-contract.md`
- `docs/product/listener-waiting-queue-contract.md`
- `docs/product/peer-support-session-duration-contract.md`
- `docs/phase-n/scheduling-state-machine.md`
- `docs/phase-n/queue-state-machine.md`
- `docs/phase-n/live-handoff-sequence.md`
- `docs/phase-n/capacity-and-concurrency-contract.md`
- `docs/phase-n/timezone-and-calendar-rules.md`
- `docs/phase-n/session-extension-contract.md`
- `docs/phase-n/booking-no-show-and-cancellation.md`
- `docs/phase-n/follow-up-and-feedback.md`
- `docs/phase-n/privacy-and-data-lifecycle.md`
- `docs/phase-n/security-threat-model.md`
- `docs/phase-n/operational-readiness.md`
- `docs/phase-n/external-dependencies.md`
- `docs/phase-n/test-evidence.md`
- `docs/phase-n/delivery-report.md`
- `docs/phase-n/phase-o-handoff.md`

Update the existing Phase I live support product contract as necessary.

Do not duplicate or contradict the authoritative Phase I contract.

---

# 49. PULL REQUEST DELIVERABLES

## PR N1

Title:

**Phase N1 — Listener Scheduling and Bookable Support**

Include:

- Listener schedules.
- Timezone support.
- Recurrence and exceptions.
- Real bookable slots.
- Member booking.
- Cancellation/rescheduling.
- Capacity safeguards.
- Scheduled check-in.
- Phase I handoff.
- PostgreSQL concurrency tests.
- Frontend workflows.
- Real integration evidence.

## PR N2

Title:

**Phase N2 — Twenty-Minute Queue and Safe Live Handoff**

Include:

- Queue states.
- Admission rules.
- Twenty-minute expiry.
- FIFO ordering.
- Temporary allocations.
- Explicit member readiness.
- Phase I request handoff.
- Notifications.
- Cancellation/recovery.
- Queue-versus-booking concurrency.
- Real PostgreSQL and full-stack tests.

## PR N3

Title:

**Phase N3 — Duration, Voluntary Follow-Up and Full Integration**

Include:

- Thirty-minute session planning.
- Bounded ten-minute extension.
- No-show handling.
- Optional feedback.
- Optional repeat booking.
- Account export/erasure integration.
- Security hardening.
- Operational controls.
- Four-persona tests.
- Complete A–M regression.
- Phase N delivery report.

### PR rules

- Correct dependency base.
- Focused implementation.
- Reviewable commits.
- No fabricated test outcomes.
- No automatic merge.
- No automatic deployment.
- No unauthorized service enablement.

---

# 50. DEFINITION OF DONE

Phase N is technically complete only when:

1. Live Now remains functional.
2. Waiting Queue is a separate explicit pathway.
3. Scheduled Booking is a separate explicit pathway.
4. Listener schedules are real, not simulated.
5. Listener readiness is never inferred from calendar data alone.
6. Bookable slots use correct timezones.
7. DST behavior is tested.
8. Slots cannot be double booked.
9. Each member respects the two-future-booking limit.
10. Booking cancellation and rescheduling work.
11. Scheduled check-in requires genuine authorization.
12. Booking does not bypass listener acceptance.
13. Booking does not create a premature conversation.
14. Queue entry is voluntary.
15. Queue admission requires legitimate service coverage.
16. Queue expires after twenty minutes.
17. Queue entries cannot be revived by stale retries.
18. FIFO assignment is correctly implemented.
19. No diagnosis-based queue ranking is used.
20. The member explicitly confirms readiness for queue handoff.
21. Queue handoff reuses Phase I.
22. Queue, booking and Live Now share capacity correctly.
23. No duplicate live conversations are possible.
24. New sessions follow the thirty-minute duration policy.
25. Expiration and end warnings are truthful.
26. Only one approved ten-minute extension is possible.
27. Extension requires both participants.
28. Extension cannot conflict with booked capacity.
29. No-show states are correct.
30. Feedback is optional.
31. Repeat booking is optional.
32. No automatic continuing-care relationship is created.
33. Existing reports and blocking remain effective.
34. Regional restrictions remain enforced.
35. Listener qualification remains mandatory.
36. User data export and deletion cover new records.
37. Sensitive transcripts are not copied into analytics.
38. English/Persian/RTL workflows are tested.
39. Real PostgreSQL concurrency tests pass.
40. True full-stack journeys pass.
41. Existing Phase I tests remain intact.
42. A–M regression remains successful.
43. All four personas have evidence.
44. Three independent reviewable PRs exist.
45. Phase N documentation is complete.
46. External blockers are truthfully documented.
47. Production is not enabled without actual authorization.

---

# 51. FINAL DELIVERY REPORT

Provide a final report containing:

## A. Actual baseline

Verified current repository state, branches and dependency chain.

## B. N1 delivery

Scheduling, booking, timezone and calendar evidence.

## C. N2 delivery

Queue, expiry, assignment and live handoff evidence.

## D. N3 delivery

Duration, extension, follow-up, safety and integration evidence.

## E. Requirement status

N-01 through N-30.

## F. Technical architecture

Services, tables, endpoints, state machines, locks and permissions.

## G. Security and privacy

Threats, mitigations and residual risks.

## H. Capacity correctness

Concurrency, double-booking, queue fairness and real-time admission evidence.

## I. Four personas

Happy, Creative, Confused and Malicious.

## J. Test evidence

Exact commands, commit SHAs, passed/failed/skipped/not-executed results.

## K. GitHub deliverables

PR links, branch names, commits and current review status.

## L. Operational blockers

Real staffing, regional approvals, legal decisions, notifications, monitoring and physical devices.

## M. Phase O handoff

Only remaining self-care and personal-growth capabilities.

Do not expand Phase O into a mandatory prerequisite for listener scheduling.

---

# FINAL EXECUTION DIRECTIVE

Implement the full GreenOcean Phase N in three sequential milestones:

**N1 — Real Scheduling and Booking**

**N2 — Bounded Waiting Queue and Live Handoff**

**N3 — Session Duration, Optional Follow-Up and Final Integration**

Begin from the latest actually validated Phase M3 baseline.

Reuse Phase I live authorization, listener readiness, acceptance, activation, conversations, capacity constraints, interruption handling and safety termination.

Do not replace Phase I with a new chat system.

Implement three explicit user pathways:

- Live Now
- Waiting Queue
- Future Booking

Enforce a maximum twenty-minute queue period.

Allow reservations from actual approved listener calendar slots.

Introduce a planned thirty-minute support session with one optional ten-minute extension requiring both participants' agreement and sufficient capacity.

Provide optional feedback and voluntary repeat booking.

Do not automatically open conversations, diagnose members, initiate treatment relationships or promise emergency response.

Preserve Phase J data rights and the existing A–M privacy/security contracts.

Use database-authoritative timing and transactions.

Test actual concurrency, timezone boundaries, capacity, service revocation, queue expiry, booking conflicts, three complete user journeys and the established four personas.

Deliver three independently reviewable Pull Requests with real CI and integration evidence.

Do not merge, deploy or activate production services without authorization.

**Expected outcome: GreenOcean provides a reliable, privacy-first, non-clinical listener-support system with immediate conversations, bounded waiting, genuinely scheduled appointments, safe duration management and optional follow-up — all grounded in real listener availability and explicit human consent.**
