# GreenOcean — Phase M
## Support Communities, Group Privacy, Membership, Governance and Safe Group Activities

### Complete Implementation Specification — M1 + M2 + M3
### Privacy-First Public, Private and Hidden Groups

---

# 1. MISSION

You are the principal full-stack engineer, privacy-by-design architect, community safety engineer, database architect, UX engineer, security engineer and quality assurance lead for GreenOcean.

Repository:

`mehrnaz-sarafraz/GreenOcean`

Technology stack:

- Java 21
- Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router
- TypeScript
- Web, Android and iOS

Your assignment is to implement **Phase M — Support Communities and Group Experiences**, the next major workstream in GreenOcean Phase 2.

GreenOcean is a non-clinical, privacy-first, support-oriented social platform.

Its groups exist to help members share experiences, support one another, develop meaningful relationships and participate in safe communities.

They are not intended to operate as unregulated treatment centers, emergency services or automatically supervised clinical environments.

Deliver complete, tested implementation work rather than only an architectural plan.

## Primary objectives

1. Introduce three correctly enforced group privacy modes.
2. Establish a safe group creation and activation approval process.
3. Complete membership requests, invitations and member management.
4. Preserve anonymous publishing within communities.
5. Strengthen group governance and roles.
6. Integrate group-specific content safety.
7. Complete useful text-based group activities and events.
8. Protect potentially sensitive group membership.
9. Reuse existing GreenOcean infrastructure.
10. Deliver three reviewable milestone PRs and final Phase M evidence.

Do not claim legal, clinical or operational approval that has not been granted.

---

# 2. APPROVED AND SELECTED PRODUCT DECISIONS

The owner previously approved three community types:

- PUBLIC
- PRIVATE
- HIDDEN

For this implementation, adopt the following product defaults and document them in the Phase M product contract.

These choices represent the selected implementation baseline. Do not silently change them during development.

## Group creation

An eligible active member may submit a group creation proposal.

The group cannot become active until an appropriately authorized GreenOcean platform reviewer approves it.

A submitted proposal must not create a publicly visible, usable group prematurely.

## Public group

- Discoverable by eligible GreenOcean members.
- Safe descriptive metadata may be visible before joining.
- Eligible members may join without moderator approval.
- Group posts and discussion remain accessible to current authorized members only.
- Nonmembers do not receive a member roster or private discussion contents.

PUBLIC describes discoverability and open membership, not universal public internet access.

## Private group

- Discoverable through a minimal, privacy-reviewed directory card.
- Joining requires approval by an authorized group owner or moderator.
- Posts, discussions, activities and membership details require membership.
- Rejected or pending applicants cannot access group content.

## Hidden group

- Absent from ordinary discovery and search.
- No ordinary public metadata or membership information is exposed.
- Membership requires a targeted invitation from an authorized group manager and explicit acceptance.
- A guessed slug or ID does not reveal the group's existence.
- A legitimately invited account may view only the minimal information necessary to decide whether to accept.
- No unrestricted public invite links are included in the initial implementation.

## Group roles

Use:

- OWNER
- MODERATOR
- MEMBER

Platform ADMIN/MODERATOR roles are separate from these group-specific roles.

## Privacy

- Member rosters are visible by default only to authorized group managers and purpose-authorized platform operators.
- Ordinary members may see the public social identities of authors who intentionally post under their social identity.
- Anonymous post authors remain anonymous to ordinary members and group managers.
- Group membership is not automatically disclosed through a member's public profile.
- Existing post audience and block restrictions continue to apply.

## Communication

Phase M uses text-based posts and replies for group discussion.

Do not add a new unrestricted real-time group chat, live audio, video or conferencing system without separate approval.

## Safety

Group management must reuse and extend the Phase L content safety system.

Groups do not bypass global community standards.

Group rules may be stricter than platform rules, but never weaker.

---

# 3. EXECUTION STRUCTURE

Implement Phase M through three dependent milestones.

## M1 — Group Privacy and Approval Foundation

- Group privacy contracts
- PUBLIC / PRIVATE / HIDDEN
- Group creation proposal workflow
- Platform review and activation
- Privacy-preserving discovery
- Database migration
- Security and API tests

## M2 — Membership and Governance

- Public joining
- Private join requests
- Hidden invitations
- Owner and moderator responsibilities
- Membership revocation
- Removal and bans
- Group rules
- Posts, anonymous publishing and notifications
- Membership security tests

## M3 — Activities, Moderation and Integration

- Text-based events and group activities
- Group moderation workflows
- Reporting, appeals and abuse prevention
- Accessibility and localization
- Full end-to-end validation
- Cross-phase regression
- Operational readiness
- Final Phase M delivery report

### Critical dependencies

M2 builds on M1.

M3 builds on M2.

Hidden groups must not be activated for real users until their invitation, discovery, roster and content-access protections have been verified.

Safety controls must exist from the initial implementation, not only after M3.

Create separate reviewable stacked Pull Requests.

Suggested branches:

- `feature/phase-m1-group-privacy-approval`
- `feature/phase-m2-membership-governance`
- `feature/phase-m3-activities-safety`

Do not automatically merge or deploy.

---

# 4. VERIFY THE LATEST SOURCE BASELINE

Before changing code:

1. Inspect the repository's current main branch.
2. Read the Phase K closure documentation.
3. Verify Phase L1, L2 and L3 branch and PR statuses.
4. Identify the actual latest integrated and validated Phase L baseline.
5. Review the latest Flyway migration.
6. Inspect CI results.
7. Read the latest approved privacy and social product contracts.
8. Identify pending operational or legal blockers.

Historical observations:

- Phase K was merged into main via PR #9.
- Phase L1 was subsequently opened as PR #10.
- L1 introduced an identity-first composer, drafts and preliminary moderation changes.
- L1 was a Draft PR during the last inspection.
- L2 and L3 had not yet been confirmed as completed.

These observations may be outdated.

Verify the real repository state.

### Do not assume L is complete

If L1–L3 have not passed their required implementation gates:

- Do not falsely report them as complete.
- Do not rebuild L inside M.
- Prepare compatible, independent M changes where safe.
- Record blocked integrations explicitly.
- Keep affected group publishing disabled until the required L content safety contract is available.

The intended implementation base is the final validated Phase L3 baseline.

Do not create M on stale main when the relevant L changes are available only in stacked branches.

---

# 5. REUSE THE EXISTING COMMUNITY ARCHITECTURE

Inspect before implementing.

Relevant backend files include:

- `CommunityController`
- `CommunityService`
- `CommunityRepository`
- `CreateCommunityRequest`
- `CommunityResponse`
- `CommunityDetailsResponse`
- `CommunityJoinRequestResponse`
- `CreateCommunityEventRequest`
- `UpdateCommunityRulesRequest`
- `PostService`
- `PostReadRepository`
- `SocialInteractionRepository`
- `MessagingService`
- `ReportService`
- `AdminService`
- `AccessPolicy`

Relevant frontend paths include:

- `(app)/communities.tsx`
- `(app)/community/[slug]/index.tsx`
- Existing community management tools
- Existing composer
- Existing post and comment components
- Existing notification and search screens
- Existing localization and design system

Previously observed functionality includes:

- Creating a group directly
- PUBLIC-like and PRIVATE-like group behavior
- Creating an OWNER membership
- Discovering groups
- Joining a public group
- Requesting private group access
- Approving and declining membership requests
- Leaving a group
- Assigning MEMBER or MODERATOR
- Removing members
- Editing rules
- Creating events
- Joining and leaving events
- Group-specific posts
- Platform moderation controls

These capabilities must be reused or safely extended.

### Known gap

The original create-group API directly created an active group and assigned OWNER.

It used a boolean `privateCommunity` rather than a complete three-mode privacy contract.

This must be migrated safely to a reviewed creation process.

Do not break existing groups or silently change their privacy.

---

# 6. REQUIREMENTS AND TRACEABILITY

Create:

`docs/phase-m/requirements-matrix.md`

For each requirement M-01 through M-24 record:

- Approved behavior
- Existing implementation
- Missing or defective behavior
- Backend changes
- Frontend changes
- Migration changes
- Privacy and security considerations
- Test cases
- Commit/PR evidence
- Final status

Use:

- VERIFIED_REUSED
- ENHANCEMENT_REQUIRED
- MISSING
- IMPLEMENTED_TESTED
- APPROVAL_REQUIRED
- OPERATIONAL_BLOCKER
- NOT_VERIFIED

Do not invent successful validations.

---

# PART I — M1: GROUP PRIVACY AND APPROVAL FOUNDATION

# 7. M-01 — Three group privacy types

Priority: CRITICAL P0

Replace or safely extend the boolean privacy model with an explicit enum:

- PUBLIC
- PRIVATE
- HIDDEN

Use a durable server-controlled representation.

## PUBLIC

Discoverable to eligible authenticated GreenOcean members.

Eligible members can view the approved name, description and applicable rules necessary to understand the group.

Membership is open to eligible users.

Posts, discussions, roster and member activities remain members-only unless a separate explicitly approved contract states otherwise.

## PRIVATE

Discoverable through a limited card.

Show only necessary non-sensitive introductory information.

Join requests require approval.

Nonmembers cannot access posts, private discussions, member rosters or events.

## HIDDEN

No listing in discovery, global search, recommendations or publicly accessible counts.

Unauthorized users must not distinguish a hidden group's existence from a nonexistent group using:

- Slug lookup
- ID lookup
- Search
- API errors
- Event URLs
- Post URLs
- Membership checks
- Notifications
- Client caches
- Pagination totals
- Member profile activity
- Timing-dependent obvious differences where practical
- Slug availability or creation errors

Use consistent non-disclosing API responses.

Authorized current members, invited recipients and specifically authorized operators may receive role-appropriate information.

### Historical migration

For existing groups:

- Preserve existing public groups as PUBLIC.
- Preserve existing private groups as PRIVATE.
- Do not convert historical groups to HIDDEN.
- Do not broaden current content access.
- Preserve existing membership and activity data.

Verify exact old semantics first.

If an old behavior conflicts with the new stricter policy, migrate conservatively and explain the user impact.

---

# 8. M-02 — Group creation proposal workflow

Priority: CRITICAL P0

Replace immediate member-created group activation with a reviewed proposal workflow.

Eligible users may propose:

- Group name
- Description
- Group purpose
- Privacy type
- Topic or category if voluntarily supplied
- Group rules
- Optional additional explanation for reviewers

Do not require:

- Real names
- Health diagnosis
- Medical documents
- Sensitive personal history
- Psychological assessment

## Proposal states

Use a durable, explicit state machine.

Suggested states:

- DRAFT
- SUBMITTED
- NEEDS_REVISION
- APPROVED
- REJECTED
- CANCELLED

Support transitions only according to authorized roles.

A proposal is not an active group.

### User experience

- Create proposal
- Save or revise where appropriate
- Submit for review
- See current status
- See requested corrections
- Withdraw a pending proposal
- Correct and resubmit
- View final outcome

Do not promise immediate approval.

### Safe defaults

Approved active groups must not be created merely by submitting an HTTP request.

Do not let a stale frontend invoke a legacy endpoint that bypasses approval.

Do not create a public placeholder containing potentially sensitive group details.

Do not show pending group proposals in discovery.

---

# 9. M-03 — Platform approval and group activation

Priority: CRITICAL P0

Only appropriately authorized GreenOcean platform staff may approve activation.

### Review criteria

- Clear non-clinical group purpose
- Suitable name
- Appropriate privacy mode
- Compliance with global community rules
- No prohibited threats, harassment or hateful framing
- No impersonation
- No misleading claim of professional treatment
- No false emergency service representation
- No obvious duplicative spam
- Appropriate ownership eligibility

Do not reject a group merely because its purpose concerns emotional distress or difficult life experiences.

### Staff requirements

Reuse E/F privileged administration security:

- Server-authorized staff role
- Strong MFA where required
- No public approval bypass
- Audit log
- Reviewer identity
- Decision reason
- Version of the reviewed proposal
- Review timestamp

Prevent self-review or conflicts of interest where the approved staff-governance contract requires independent review.

### Atomic activation

A successful approval must transactionally:

1. Verify the proposal remains eligible for approval.
2. Verify the exact reviewed revision.
3. Reserve the approved group slug.
4. Create or activate the group.
5. Assign the eligible requester OWNER.
6. Record the approval.
7. Mark the proposal APPROVED.
8. Generate only authorized notifications.

Retries must not create duplicate groups.

Concurrent approval must produce one consistent result.

If the proposed owner has become ineligible, suspended or deleted, activation must fail safely.

### Staff unavailable

Keep submitted proposals pending.

Do not automatically approve them.

Record an operational blocker instead of inventing a review team.

---

# 10. M-04 — Group discoverability and metadata

Priority: CRITICAL P0

Create privacy-specific server queries and DTO projections.

Do not fetch all group data then hide fields only in the frontend.

### Public group card

May contain approved:

- Name
- Description
- Group type
- Safe topic labels
- Group rules summary
- Join action

### Private group card

Use minimal non-sensitive information.

Do not expose the member roster or identifying membership data.

Membership count visibility should be conservative where it could reveal sensitive participation.

### Hidden groups

Excluded entirely from ordinary directory responses, search results, counts, suggestions and global activity feeds.

For unauthorized direct references, return the same nondisclosing class of response as a nonexistent group.

### Group name and slug safety

Handle globally unique slugs without disclosing the existence of hidden groups through error messages.

Prefer server-generated opaque or non-semantic routing identifiers for hidden groups where necessary.

Do not expose internal hidden group identifiers through analytics or URL previews.

Never render sensitive information in unauthenticated OpenGraph metadata.

---

# 11. M-05 — Group visibility transitions

Priority: CRITICAL P0

Do not allow arbitrary, instantaneous switching between PUBLIC, PRIVATE and HIDDEN.

Changing a group from more restrictive to less restrictive visibility can disclose its existence and members' participation.

### Safe initial policy

The group privacy type is fixed after activation unless a specifically reviewed transition workflow is implemented.

Support a request for change only when:

- Authorized OWNER initiates it.
- A platform reviewer approves it.
- The consequences are clearly explained.
- Necessary membership and content privacy considerations are resolved.
- Historical content audiences are not automatically broadened.

No bulk widening of post visibility.

No silent unhide operation.

When necessary, require a new group rather than an unsafe conversion.

---

# 12. M-06 — Group lifecycle and suspension

Priority: P0

Support practical group states:

- ACTIVE
- RESTRICTED
- SUSPENDED
- ARCHIVED

Reuse existing status contracts where compatible.

Specify allowed transitions.

### Restrictions

A suspended group cannot continue normal publishing, invitations or events.

An archived group remains inaccessible for ordinary interaction.

Restricted groups may have explicitly limited capabilities only when those restrictions are actually enforced server-side.

Do not invent an unrestricted platform operator override.

Provide authorized restoration and audit evidence.

---

# PART II — M2: MEMBERSHIP AND GOVERNANCE

# 13. M-07 — Public membership

Priority: P0

An eligible member can join an ACTIVE PUBLIC group directly.

Required rules:

- Account eligibility enforced
- Regional service permission enforced
- Existing blocks and bans respected
- Idempotent join
- Accurate membership status
- No duplicate memberships
- Safe concurrent requests
- No automatic follower relationship
- No automatic direct messaging permission
- Explicit acknowledgment of group rules before joining where appropriate

Group posts and activities become accessible only after membership is valid.

A group may be discoverable without being readable.

---

# 14. M-08 — Private membership requests

Priority: P0

For PRIVATE groups:

- Request access
- See pending state
- Cancel request
- Approve request
- Decline request
- Receive an appropriate status notification
- Re-request according to anti-abuse policy

Only authorized OWNER/MODERATOR may review private join requests.

### Privacy

A pending applicant must not:

- View private posts
- View private events
- View the member roster
- Search private content
- Receive group-content notifications
- Access group-scoped discussion threads

### Concurrency

Approval and cancellation must have deterministic, transactionally safe behavior.

A revoked or rejected request must not be accepted by replaying an old action.

An account that is blocked, banned or ineligible at approval time cannot silently become a member.

---

# 15. M-09 — Hidden invitations

Priority: CRITICAL P0

Hidden groups use invitation-only membership.

For the initial implementation, prefer **targeted in-app invitations to an existing eligible account**, not freely shareable public invite URLs.

This minimizes accidental existence leakage.

### Allowed inviters

- OWNER
- MODERATOR

Only while:

- The group is ACTIVE.
- The inviter retains the required permission.
- The target is eligible.
- The target is not banned or otherwise prohibited.

### Invite lifecycle

Suggested states:

- PENDING
- ACCEPTED
- DECLINED
- REVOKED
- EXPIRED

### Recommended safe defaults

- Explicit acceptance required.
- A configurable seven-day invitation expiry.
- Bounded outstanding invitations.
- Rate limits.
- Idempotent retry behavior.
- No automatic membership from merely opening an invitation.

### Privacy requirements

- Invitation available only to the intended recipient and authorized managers.
- A valid recipient may see the minimal group details necessary to decide.
- Other users cannot inspect the invitation.
- A declined or revoked invitation no longer grants access.
- Old links or identifiers cannot restore membership.
- No public recipient lists.
- No private group metadata in unrestricted email or push previews.
- Do not expose hidden group details in ordinary logs.

If invitation tokens are used:

- Use high-entropy values.
- Store verifiers rather than reusable raw secrets.
- Enforce expiry, single-use semantics and target binding.
- Avoid placing raw tokens in analytics, third-party URLs or logs.

Do not implement unrestricted shareable hidden-group links without separate approval.

---

# 16. M-10 — Membership revocation, leaving and removal

Priority: CRITICAL P0

Support:

- Member leaves voluntarily.
- Authorized manager removes a member.
- Authorized manager temporarily restricts or bans a member.
- Platform operator suspends inappropriate membership where policy allows.
- Removed member's access is revoked immediately.

### Revocation affects

- Group posts
- Group details
- Member roster
- Events
- Attendance
- Group search
- Group notifications
- Group-scoped saved views
- Group-scoped discussion resources
- Direct URLs
- Cached frontend data

### Ownership

The last valid owner must not leave or be removed without a safe ownership-resolution path.

Do not auto-transfer ownership to an arbitrary member.

### Removed member's own historical data

Preserve lawful owner access, export and erasure rights where applicable.

Do not use that owner-access exception as a way to inspect other members' group content after removal.

### Important distinction

Removal and ban are different.

A removed public-group member must not simply rejoin immediately if the authorized action intended to exclude them.

Implement a persistent and reviewable group-ban rule rather than relying on frontend state.

---

# 17. M-11 — Role-based group governance

Priority: CRITICAL P0

Define clear permissions.

| Capability | OWNER | MODERATOR | MEMBER |
|---|---|---|---|
| View permitted group content | Yes | Yes | Yes |
| Publish according to global rules | Yes | Yes | Yes |
| Invite eligible members | Yes | Yes | No |
| Approve private join requests | Yes | Yes | No |
| Review ordinary group reports | Authorized | Authorized | No |
| Remove ordinary members | Yes | Yes | No |
| Ban members | Yes | Authorized | No |
| Edit group rules | Yes | Authorized | No |
| Create text-based events | Yes | Yes | No |
| Assign moderators | Yes | No | No |
| Transfer ownership | Controlled | No | No |
| Change group visibility | Reviewed request | No | No |
| Reveal anonymous authors | No | No | No |

The table is the intended minimum authorization contract.

### Platform versus group privileges

A group MODERATOR is not a GreenOcean platform MODERATOR.

A group OWNER is not a platform ADMIN.

Group roles must not grant access to:

- Global staff tools
- Other groups
- Hidden author attribution
- Private listener conversations
- Account security records
- Professional credential documents

### Role changes

Use server-side authorization.

Prevent self-promotion and unauthorized role assignment.

Make demotion effective immediately.

Invalidate previously cached permissions.

Use concurrency-safe role changes.

---

# 18. M-12 — Ownership transfer and recovery

Priority: P0

A group needs a safe continuity mechanism.

### Owner transfer

Require:

1. Active current owner authorization.
2. Eligible intended new owner.
3. Explicit acceptance by the new owner.
4. Atomic transfer.
5. Audit record.
6. No interval with two inconsistent authoritative owners.

Do not transfer ownership merely because a user has moderator privileges.

### Owner unavailable

If the owner is deleted, suspended or otherwise unable to act:

- Restrict unsafe management actions.
- Use a separate platform-governed recovery process.
- Require authorized human review.
- Preserve privacy and member rights.
- Avoid granting ownership to the first member who requests it.

No automatic role-escalation endpoint.

---

# 19. M-13 — Member privacy

Priority: CRITICAL P0

Group membership can reveal private interests or potentially sensitive lived experiences.

Treat membership as privacy-sensitive.

### Required defaults

- Do not display group membership on public profiles automatically.
- Do not expose hidden group membership.
- Do not expose private group membership to nonmembers.
- Ordinary group members do not receive a complete membership roster by default.
- Group managers can access the minimum roster needed for authorized management.
- No anonymous-post author identity is exposed to a group OWNER or MODERATOR merely because of their role.

### Transparency

Before joining, tell members:

- What group type it is.
- Who can find the group.
- Who can read the posts.
- Who can see membership or participation.
- Who can see named posts.
- What anonymous posting does and does not conceal.
- Whether events expose attendance or host identity.

Do not promise total anonymity within a small group.

Members who publish with social identity may be recognizable to other authorized group members.

---

# 20. M-14 — Group posts and anonymous publishing

Priority: CRITICAL P0

Reuse Phase L post creation and moderation.

For group posts:

- Use the existing COMMUNITY audience.
- Enforce valid active membership.
- Respect user-selected publication identity.
- Preserve anonymous-author privacy.
- Respect group status.
- Respect group bans/restrictions.
- Require approved community rules.
- Apply global moderation policy.
- Apply approved additional group rules.
- Respect blocks and content warnings.

### No moderation bypass

Publishing into a group must not skip Phase L safety checks.

Editing an already approved group post must not publish an unchecked revised body.

### Privacy boundaries

A group post must not become PUBLIC or FOLLOWERS merely because someone shares its link.

Hidden group posts must not reveal their group name, slug or author through ordinary search.

A user's feed, bookmarks or comments must not expose content after membership revocation.

### Named and anonymous activity

Do not infer anonymous ownership from follower relations, member lists, notification actors or author-profile views.

If group-specific anonymous membership is not implemented, explain that posting anonymously does not make all group participation untraceable to other participants.

---

# 21. M-15 — Group rules and onboarding

Priority: P0

Every active group must have a clear ruleset.

### Global minimum

No group may permit:

- Targeted harassment
- Threats
- Hate speech
- Bullying
- Doxxing
- Sexual harassment
- Spam
- Dangerous encouragement
- Misleading claims that peer support constitutes medical treatment

### Group-specific rules

A group may additionally define appropriate behavioral boundaries.

Group rules cannot contradict the global policy.

### Required workflow

- Display rules before joining.
- Obtain required acknowledgment where appropriate.
- Allow owners/moderators to propose rule changes.
- Validate changes.
- Record necessary revisions.
- Notify current members of materially changed rules.
- Require new acknowledgment for changes that materially affect participation when appropriate.

Do not let a group moderator silently redefine platform policy.

---

# 22. M-16 — Group invitations and notifications

Priority: P1

Use existing notification architecture.

Support:

- Proposal submitted
- Group approved or rejected
- Revision requested
- Private membership request
- Membership accepted or declined
- Hidden invitation received
- Invitation accepted or declined
- Role assigned or removed
- Event created or cancelled
- Relevant moderation notice

### Strict privacy

Notification payloads must be recipient-scoped.

For hidden groups:

- Avoid sensitive group names in operating-system lock-screen previews.
- Do not include private group text in email subjects.
- Do not include invitation secrets in analytics.
- Do not reveal hidden membership through broad activity notifications.

Notifications must revalidate the recipient's current authorization before loading detailed content.

---

# PART III — M3: GROUP ACTIVITIES AND COMMUNITY SAFETY

# 23. M-17 — Text-based group activities

Priority: P0

Reuse existing `community_events` functionality.

Support:

- Scheduled text-based discussion circles
- Structured Q&A sessions
- Text-based peer reflection activities
- Group workshops that do not require unimplemented audio/video transport

### Clarify execution semantics

A scheduled event is not automatically a live chat service.

For the initial Phase M:

- Event scheduling is supported.
- Members can view applicable event details.
- Members can register to attend.
- Authorized hosts may publish an event discussion prompt.
- Participants can interact through approved group posts and replies.
- Host and participant permissions are enforced.

Do not label an event LIVE unless there is a functioning and authorized live interaction mechanism.

### Video and audio

Existing code may accept VIDEO and AUDIO event types.

Do not mistake event metadata for functional conferencing.

Keep unsupported transport formats disabled or clearly marked unavailable.

Do not add video calls, recording or audio streaming without separate approval.

---

# 24. M-18 — Event lifecycle and attendance

Priority: P0

Implement or complete:

- Create scheduled event
- Edit permitted event details
- Cancel event
- Join attendance
- Leave attendance
- View current event status
- Show capacity where appropriate
- Prevent overbooking
- Close invalid registrations
- Handle organizer removal
- Handle group suspension
- Handle membership revocation
- Prevent duplicate attendance

### Event states

Suggested:

- SCHEDULED
- CANCELLED
- COMPLETED

Only add ACTIVE/LIVE if the underlying functionality genuinely supports it.

### Concurrency

Capacity enforcement must be atomic across backend instances.

Two users competing for the last place cannot both succeed.

Retries must not duplicate attendees.

### Time

Store authoritative timestamps consistently.

Render in the user's applicable timezone.

Test daylight-saving transitions.

Do not fabricate event times.

### Privacy

Nonmembers cannot enumerate restricted event details.

Hidden events are absent from public calendars and search.

Attendance must not expose a hidden group's identity or member roster.

---

# 25. M-19 — Group moderation

Priority: CRITICAL P0

Integrate group management with the existing L moderation system.

### Group-level actions

Where authorized:

- Review group reports.
- Hide prohibited group content.
- Request edits to problematic group content.
- Warn members.
- Apply approved temporary restrictions.
- Remove members.
- Ban members.
- Report severe content to authorized platform moderators.
- Request group suspension.

### Platform-level actions

Only authorized platform roles may:

- Review group creation proposals.
- Suspend entire communities where authorized.
- Resolve platform-wide incidents.
- Conduct privileged anonymous-author attribution where justified.
- Review applicable escalated safety evidence.

### Privacy limitation

Group moderators must not gain unrestricted private-message access.

Group roles do not grant access to platform-wide moderation records.

### Global moderation integration

Group-specific review cannot override a higher-priority global safety restriction.

If content has been globally blocked, a group manager cannot approve it into publication.

---

# 26. M-20 — Reports, appeals and accountability

Priority: P0

Reuse existing report and appeal infrastructure.

Support reports about:

- Group
- Group post
- Group comment
- Group member's behavior
- Event
- Abuse by a group moderator
- Abuse by a group owner

### Conflict of interest

A report against a group OWNER or MODERATOR must not be resolved solely by the person being reported.

Escalate to appropriately authorized platform review.

### Reporting controls

- Authenticated owner of report
- Safe evidence handling
- Minimal necessary content
- No private author identity exposure
- Duplicate report protection
- Concurrent submission safety
- Review decision recording
- Appropriate notice
- Appeal where applicable

### Operational honesty

If human reviewers are unavailable, preserve restricted pending status.

Do not pretend a report or appeal has been human-reviewed.

---

# 27. M-21 — Abuse prevention

Priority: CRITICAL P0

Introduce appropriately bounded controls for:

- Mass group creation proposals
- Proposal spam
- Duplicate or near-duplicate group names
- Invite flooding
- Private membership request flooding
- Repeated join/leave abuse
- Group-ban evasion
- Moderator role abuse
- Mass member removals
- Group notification spam
- Event attendance abuse
- Creation of misleading treatment groups

### Suggested configurable limits

Use conservative, revisable initial defaults for proposal and invitation throughput.

Do not treat example values as universal product requirements.

Implement server-side limits with cross-instance correctness where material.

Avoid invasive personal profiling or hidden health-based scoring.

Do not infer a member's diagnosis from group participation.

Record important abuse-prevention decisions without leaking sensitive group contents to operational logs.

---

# 28. M-22 — Group closure and deletion

Priority: P0

Define lifecycle operations for:

- Owner-requested closure
- Platform suspension
- Archived group
- Deleted or erased group data, where appropriate
- Group restoration under authorized procedure

### Required safety properties

Closing a group must immediately stop new memberships, invitations, posts and events.

Old invitation tokens must not revive an archived group.

Archived hidden groups remain hidden.

Account deletion must not leave invalid owner permissions.

Group closure must not automatically expose old content elsewhere.

Apply approved retention, evidence preservation and user-rights rules.

No irreversible mass deletion without an authorized, tested lifecycle.

---

# 29. M-23 — UX, localization and accessibility

Priority: P0

Build a coherent group experience using the existing GreenOcean design system.

### Core screens

- Group discovery
- Public group overview
- Private group overview
- Hidden invitation acceptance
- Create group proposal
- Proposal status
- Group home
- Group posts
- Group events
- Membership management
- Group rules
- Group moderators
- Group safety/reporting
- Group settings
- Group closure/restriction status

### UX behavior

- Clear group type
- Clear membership state
- Clear rules
- Honest access restrictions
- Understandable pending review states
- No accidental posting to wrong group
- No hidden membership leak through loading states
- Correct cancellation and retry behavior
- No dark-pattern invitations
- No forced disclosure of sensitive personal details

### Localization

Support:

- Persian
- English
- RTL
- Mixed-script names
- Unicode-safe descriptions
- Proper time formatting
- Accessible error messages

### Accessibility

Test:

- Keyboard navigation
- Screen reader labels
- Focus management
- Dynamic text
- Status announcements
- Touch targets
- Color contrast
- Relevant VoiceOver/TalkBack device behavior when available

Do not claim actual native device testing from JavaScript exports.

---

# 30. M-24 — Final Phase M integration and testing

Priority: CRITICAL P0

Run complete milestone and cross-feature tests.

Use the established GreenOcean four-persona methodology.

## Happy User

1. Register and become eligible.
2. Submit a PUBLIC group proposal.
3. Authorized staff approves it.
4. Group becomes active.
5. Eligible member joins.
6. Member sees group rules.
7. Member publishes a named or anonymous post.
8. Other member replies.
9. Owner creates a text-based event.
10. Members attend.
11. Authorized manager resolves a normal membership issue.
12. A member leaves and loses restricted access.

Repeat applicable journeys for PRIVATE and HIDDEN.

## Creative User

- Persian/RTL.
- Unicode names and descriptions.
- Sensitive but non-clinical lived-experience topics.
- Group proposal revision.
- Multiple private membership requests.
- Hidden invitation decline and later re-invitation.
- Anonymous group posts.
- Unusual but legitimate group rules.
- Different timezones.
- DST event boundary.
- Intermittent connectivity.
- Owner transfer.
- Cancelled event.
- Previously saved group post becoming inaccessible.
- Group suspension while screen remains open.

## Confused User

- Tries to create a group but leaves the proposal unfinished.
- Doesn't understand PUBLIC versus PRIVATE.
- Expects PRIVATE membership without approval.
- Cannot find a HIDDEN group.
- Receives an invitation and is unsure whether joining reveals identity.
- Attempts to post before joining.
- Misunderstands event attendance.
- Attempts to leave as last owner.
- Receives a moderation notice.
- Doesn't understand group versus platform moderator roles.

Show clear, truthful messages.

## Malicious User

- Calls legacy immediate-create API.
- Attempts activation without staff approval.
- Forges a platform reviewer role.
- Uses an expired or revoked staff session.
- Tries to approve own proposal where policy forbids it.
- Finds a hidden group by guessed slug.
- Enumerates hidden group existence through errors.
- Guesses event IDs.
- Enumerates invitation recipients.
- Replays revoked invitations.
- Attempts private-group access while pending.
- Uses a stale membership after removal.
- Rejoins while banned.
- Escalates self to MODERATOR.
- Removes OWNER.
- Accepts a role transfer without authorization.
- Reads anonymous author IDs.
- Uses group reporting to reveal private data.
- Publishes without Phase L content review.
- Sends prohibited group notifications.
- Uses old cache data after authorization revocation.
- Attempts last-seat event race exploitation.
- Executes concurrent group proposal approval.
- Tries to convert HIDDEN to PUBLIC without approval.
- Tries to access suspended group resources.

Use isolated synthetic records.

Never run adversarial or destructive tests against production or real users.

---

# 31. CRITICAL PRIVACY TEST MATRIX

Create explicit automated tests for these conditions:

1. Hidden groups absent from discovery.
2. Hidden groups absent from search.
3. Hidden groups absent from recommendation lists.
4. Hidden groups absent from public count queries.
5. Hidden group guessed IDs fail without confirmation.
6. Hidden group guessed slugs fail without confirmation.
7. Hidden group event IDs do not disclose existence.
8. Hidden invitation cannot be redeemed by another user.
9. Expired invitation fails.
10. Revoked invitation fails.
11. Accepted invitation cannot create duplicate membership.
12. Private group applicant cannot access posts.
13. Nonmember cannot enumerate private roster.
14. Ordinary member cannot access protected management roster.
15. Platform reviewer cannot bypass restricted content access without an authorized audited purpose.
16. Group manager cannot reveal anonymous post author.
17. Named posts display only permitted social identity.
18. Anonymous posts remain anonymous in notifications.
19. Group content excluded after membership revocation.
20. Group content excluded after suspension.
21. Block and moderation restrictions override membership.
22. Old PUBLIC/FOLLOWERS post audiences remain preserved.
23. Existing group data migrates without visibility expansion.
24. User profile does not disclose hidden memberships.
25. Group-specific posts never become publicly readable merely because the group is PUBLIC.

Test serialized API responses and actual database state.

Do not rely only on frontend visibility.

---

# 32. DATABASE MIGRATIONS

Use new additive Flyway migrations.

Determine the actual current migration version from the latest source.

Never rewrite applied migrations.

### Expected schema areas

- Group privacy type
- Proposal records
- Proposal revisions
- Platform approval decisions
- Group invitation records
- Invitation expiry and revocation
- Group bans or restrictions
- Ownership transfer workflow
- Group status and review evidence
- Group rule revisions where needed
- Group event lifecycle extensions
- Safe audit references

Reuse existing:

- communities
- community_members
- community_join_requests
- community_rules
- community_events
- community_event_attendees
- posts
- comments
- reports
- notifications
- account lifecycle data

### Migration safety

Test both fresh and populated databases.

Preserve:

- Existing PUBLIC groups
- Existing PRIVATE groups
- Existing owners
- Existing members
- Existing private requests
- Existing rules
- Existing event attendees
- Existing group posts
- Existing anonymous posts
- Existing reports and appeals

Never default old groups to HIDDEN.

Never convert historical private content into public content.

No automatic staff approval of historical pending group proposals.

Do not add production seed groups or staff accounts without owner approval.

---

# 33. API DESIGN

Reuse `/api/v1/communities` conventions.

Extend rather than duplicate controllers where appropriate.

Illustrative endpoints may include:

### Group proposals

- `POST /api/v1/community-proposals`
- `GET /api/v1/community-proposals/mine`
- `GET /api/v1/community-proposals/{id}`
- `PATCH /api/v1/community-proposals/{id}`
- `POST /api/v1/community-proposals/{id}/submit`
- `POST /api/v1/community-proposals/{id}/cancel`

### Platform review

- `GET /api/v1/admin/community-proposals`
- `POST /api/v1/admin/community-proposals/{id}/approve`
- `POST /api/v1/admin/community-proposals/{id}/request-revision`
- `POST /api/v1/admin/community-proposals/{id}/reject`

### Membership

- Existing join/leave endpoints
- Existing private membership request endpoints
- `GET /api/v1/communities/{id}/invitations`
- `POST /api/v1/communities/{id}/invitations`
- `POST /api/v1/community-invitations/{id}/accept`
- `POST /api/v1/community-invitations/{id}/decline`
- `POST /api/v1/community-invitations/{id}/revoke`

### Governance

- Existing member-role endpoints
- Existing rules endpoints
- Scoped ban/restriction endpoints
- Ownership transfer endpoints
- Group lifecycle endpoints

### Events

- Existing event creation
- Existing attendance endpoints
- Scoped event updates/cancellation
- Authorized event discussion entry points

These paths are suggestions.

Follow actual project conventions and avoid redundant APIs.

### Universal API requirements

- Server-derived identity
- Validated DTOs
- State transition checks
- Correct role authorization
- Consistent nondisclosing errors
- Bounded pagination
- Rate limits
- Transactional mutations
- Idempotency where needed
- No leakage of hidden group information
- Appropriate cache headers

---

# 34. SECURITY AND CONCURRENCY

Use server-authoritative security checks.

Test:

- Proposal approval races
- Duplicate slug races
- Concurrent hidden invitation acceptance
- Invitation revocation versus acceptance
- Private request cancellation versus approval
- Membership removal versus group-post access
- Owner transfer versus suspension
- Member ban versus rejoin
- Group suspension versus event registration
- Concurrent last-seat event signup
- Moderation approval versus post editing

### Transactionality

Never use a check-then-act sequence as the sole protection against a concurrency-sensitive operation.

Use appropriate database constraints, transactions, locking or atomic transitions.

### Cache invalidation

Revalidate access when:

- A member leaves.
- A member is removed.
- A member is banned.
- Group privacy or status changes.
- A role is revoked.
- A hidden invitation expires.
- A post is moderated.
- An account is suspended.

### Operational logs

Do not log:

- Sensitive group discussion text
- Anonymous author identity in ordinary logs
- Invitation secrets
- Passwords
- Tokens
- Private member rosters
- Sensitive health narratives

Use purpose-limited audit records.

---

# 35. PRIVACY, LEGAL AND OPERATIONS HANDOFF

Reuse the Phase J governance model.

Assess newly introduced:

- Group creation proposals
- Group membership
- Hidden invitations
- Moderator decisions
- Attendance
- Community reports
- Group rules history
- Closure and deletion metadata

### Data rights

Verify:

- Owner export
- Account erasure
- Group membership deletion
- Invitation cleanup
- Appeal records
- Approved moderation retention
- Event attendance retention
- Backup limitations

### Privacy-sensitive participation

Do not automatically classify members as having a health condition based on joining a support group.

Do not begin sensitive behavioral analytics.

Do not expose membership to marketers, recommendation systems or unapproved processors.

### Legal and operational dependencies

Keep actual approval requirements separate from code implementation.

A software-complete invitation flow does not mean real staff review is available.

Do not enable a public social community until applicable content safety, moderator capacity and approved service/legal conditions are ready.

---

# 36. NON-GOALS

Do not implement these without separate approval:

- Video calling
- Voice rooms
- Recording
- Unrestricted live group chat
- Clinical group therapy
- Automatic mental-health diagnosis
- Hidden diagnosis-based group recommendations
- AI therapist personas
- External AI content processing
- Paid memberships
- Public internet group indexing
- Mass group invitation marketing
- Unrestricted shareable hidden-group links
- New unapproved countries
- New professional clinical services
- Phase N listener queues or scheduling
- Phase O journaling and self-care
- Phase P specialist services

Phase M must remain focused on safe, reliable group communities.

---

# 37. DELIVERY REQUIREMENTS BY MILESTONE

## M1 PR

Deliver:

- Group privacy enum and database contract
- Historical migration
- Proposal lifecycle
- Platform review
- Safe activation
- Public/private/hidden discovery
- Hidden group existence protection
- M1 unit and real PostgreSQL tests
- M1 full-stack user journeys

## M2 PR

Deliver:

- Public self-join
- Private approval
- Hidden targeted invitation
- Membership revocation
- Group roles
- Removal/ban
- Ownership transfer
- Group rules
- Anonymous group publishing integration
- Notifications
- M2 privacy and race-condition tests

## M3 PR

Deliver:

- Text-based events
- Attendance correctness
- Moderation
- Appeals
- Abuse prevention
- Group suspension/closure
- Accessibility and localization
- Full cross-phase regression
- Final Phase M report

All PRs must be reviewable and stacked on their correct dependencies.

No automatic merge.

No production deployment.

---

# 38. TEST COMMANDS AND EVIDENCE

Run the repository's actual build and test commands rather than assuming their syntax.

Required evidence categories:

- Backend unit tests
- PostgreSQL integration tests
- Migration tests
- Authorization tests
- Hidden privacy tests
- Group membership tests
- Invitation lifecycle tests
- Role escalation tests
- Content moderation tests
- Event capacity tests
- Frontend component tests
- TypeScript validation
- Lint
- Browser full-stack E2E
- RTL checks
- Web build/export
- Native JS export
- Native-device tests when available

### Existing regressions

Preserve validated behavior from:

- A–F account and security
- G/H privacy and onboarding
- I listener-session isolation
- J consent and data rights
- K integration and migration constraints
- L social anonymity and content moderation

Do not blindly repeat historical implementation work.

### Report

For each milestone include:

- Exact tested commit SHA
- Commands
- Environment
- Passed
- Failed
- Skipped
- Not executed
- Reproduced defects
- Fixes
- Remaining blockers

Do not fabricate results.

Do not call mocked API tests real full-stack E2E.

---

# 39. REQUIRED DOCUMENTATION

Create or update:

- `docs/phase-m/requirements-matrix.md`
- `docs/product/community-group-privacy-contract.md`
- `docs/product/group-creation-approval-contract.md`
- `docs/phase-m/group-visibility-matrix.md`
- `docs/phase-m/group-proposal-state-machine.md`
- `docs/phase-m/membership-and-invitation-state-machine.md`
- `docs/phase-m/role-permission-matrix.md`
- `docs/phase-m/hidden-group-threat-model.md`
- `docs/phase-m/group-safety-and-moderation.md`
- `docs/phase-m/event-lifecycle.md`
- `docs/phase-m/privacy-data-lifecycle.md`
- `docs/phase-m/owner-decisions.md`
- `docs/phase-m/operations.md`
- `docs/phase-m/test-evidence.md`
- `docs/phase-m/delivery-report.md`
- `docs/phase-m/phase-n-handoff.md`

Do not duplicate authoritative existing documents.

Update relevant existing product contracts when necessary.

---

# 40. FINAL ACCEPTANCE CRITERIA

Phase M may be marked technically complete only if:

1. Three group privacy modes are durable.
2. Historical groups migrate safely.
3. Eligible users can submit group proposals.
4. Ordinary users cannot activate their own unreviewed groups.
5. Staff approval is properly authenticated and audited.
6. Concurrent approvals cannot create duplicate active groups.
7. PUBLIC groups are discoverable to eligible members.
8. PRIVATE groups require membership approval.
9. HIDDEN groups are absent from unauthorized discovery.
10. Hidden existence is protected from guessed identifiers and error responses.
11. Hidden invitations are targeted and expire.
12. Hidden invitations require explicit acceptance.
13. Revoked invitations cannot grant access.
14. Group members have properly enforced roles.
15. Group moderators cannot obtain platform-staff privileges.
16. Group moderators cannot unmask anonymous authors.
17. Private member rosters remain restricted.
18. Group membership is not exposed automatically through public profiles.
19. Leaving or removal revokes current access.
20. Bans cannot be bypassed through immediate rejoining.
21. Last-owner continuity is safe.
22. Ownership transfer is explicit and audited.
23. Group rules cannot override platform safety rules.
24. Group posts inherit Phase L publication identity protection.
25. Group posts cannot bypass content moderation.
26. Group posts remain members-only under the approved contract.
27. Group events work without pretending unsupported video/audio transport exists.
28. Attendance capacity is concurrency-safe.
29. Group reports, moderation and appeals are authorized.
30. Groups can be restricted or suspended safely.
31. All four test personas are covered.
32. Actual PostgreSQL tests pass.
33. Actual integrated frontend/backend journeys pass.
34. English/Persian and RTL work correctly.
35. Existing A–L privacy and security contracts are preserved.
36. External operational blockers are honestly recorded.
37. Three reviewable milestone PRs exist.
38. Final Phase M documentation is complete.
39. No unapproved public launch occurs.
40. Phase N has a clear handoff without unnecessary group-system redevelopment.

---

# 41. FINAL EXECUTION DIRECTIVE

Implement the complete GreenOcean Phase M using three ordered milestones.

First verify the latest valid Phase L implementation baseline.

Reuse existing community creation, membership, roles, posts, events, reports, search and notifications.

Implement:

**M1:** PUBLIC, PRIVATE and HIDDEN group privacy, user proposals and platform-approved activation.

**M2:** Public membership, approved private membership, invitation-only hidden membership, secure role governance, member privacy, anonymous publishing and revocation.

**M3:** Text-based group activities, event management, moderation, anti-abuse protections, appeals and final integration.

Do not expose hidden groups through public APIs, search, events, notifications, counters, errors or caches.

Do not activate a user-submitted group without appropriately authorized review.

Do not allow group moderators to identify anonymous authors or bypass platform-level safeguards.

Do not invent video/audio transport, clinical treatment services, external AI providers or sensitive analytics.

Run real tests, fix proven defects, preserve earlier phase behavior and create three reviewable Pull Requests.

Complete all development that can safely be completed in the available environment.

Where a real human reviewer, legal approval or operational capability is unavailable, preserve a safe disabled or pending state and document the exact blocker.

Do not automatically merge or deploy.

**Expected outcome: GreenOcean has a technically complete, secure and privacy-first group community system supporting public, private and hidden communities, controlled group creation, consensual membership, anonymous-safe discussion, responsible governance and meaningful text-based group activities.**
