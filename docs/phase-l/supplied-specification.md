# GreenOcean — Phase L
## Privacy-First, Support-Oriented Social Community

### Full Implementation of L1 + L2 + L3
### Anonymous-First Publishing, Safe Content Moderation, Social Discovery, Relationships and End-to-End Testing

---

# 1. MISSION

You are the principal full-stack engineer, software architect, privacy-by-design specialist, community-safety engineer, database engineer, UX engineer and quality assurance lead for GreenOcean.

Repository:

`mehrnaz-sarafraz/GreenOcean`

Technology stack:

- Java 21
- Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router
- TypeScript
- Web / Android / iOS

Your assignment is to fully implement:

**Phase L — Support-Oriented Social Community**

This is the first social-experience development phase following the Phase 1 A–K technical implementation.

GreenOcean is a privacy-first, non-clinical peer-support community.

Its social experience must prioritize:

- Empathy
- Psychological safety
- Voluntary participation
- User autonomy
- Privacy
- Respectful communication
- Meaningful connections
- Honest service boundaries

It must not be optimized primarily for addictive engagement, public popularity or competitive follower metrics.

## Approved product decisions

The product owner has approved the following:

1. GreenOcean social publishing is identity-optional.
2. Members may publish a post anonymously or under their existing social display identity.
3. Anonymous means anonymous to other ordinary members, not untraceable to the authorized platform safety system.
4. Choosing publication identity must be the FIRST stage of creating a new post.
5. The user must consciously choose how the post will appear.
6. The writing stage comes after the identity decision.
7. A final review must show both identity mode and content audience before publishing.
8. The community must maintain clear standards for respectful language.
9. Content moderation must begin during Phase L, not be deferred entirely to later phases.
10. Harassment, threats, hate, deliberate humiliation, privacy violations and abusive interactions are prohibited.
11. Users must still be allowed to express pain, distress, anger and difficult personal experiences in respectful ways.
12. Safety must not be reduced to an indiscriminate list of forbidden words.
13. Existing A–K security, privacy, account, listener-support, governance and moderation protections must remain intact.

Do not reinterpret these approved choices without asking the owner.

---

# 2. EXECUTION STRATEGY — THREE DEPENDENT SUBPHASES

The overall Phase L is intentionally divided into three implementation milestones.

Implement all three, sequentially, under this single assignment.

## L1 — Safe publishing foundation

- Publication identity first
- Explicit anonymous versus named publishing
- Three-stage composer
- Durable privacy boundaries
- Optional topic selection
- Drafts and owner edits
- Pre-publication safety enforcement
- Minimum viable moderation lifecycle
- Relevant database migrations
- Focused security and privacy tests

## L2 — Social discovery and relationships

- Following feed
- Discover feed
- Saved posts
- Replies and comment management
- Follow-request UX
- Message request UX
- Notifications
- Search improvements
- Anonymous-author protection across social surfaces
- Focused integration tests

## L3 — Community safety hardening and final L integration

- Human moderation workflow completion
- Review decisions, notices and appeals
- Abuse prevention and rate limiting
- Bilingual moderation quality testing
- Anonymous-author security review
- Cross-feature integration
- Full Phase L regression
- Operational safety checklist
- Final Phase L delivery report

### Critical sequencing

L1 must contain an actually functioning minimum safety gate before new content publishing is enabled.

L3 strengthens and verifies that safety system. It must not be the first milestone where harmful content is controlled.

Do not activate the new public community experience before the necessary safety and moderation operations are ready.

### GitHub structure

Suggested branches:

- `feature/phase-l1-safe-publishing`
- `feature/phase-l2-social-interactions`
- `feature/phase-l3-community-safety`

Create reviewable stacked PRs.

L2 should build on the validated L1 baseline.

L3 should build on the validated L2 baseline.

Do not silently squash all changes into one enormous unreviewable commit.

Do not automatically merge or deploy.

---

# 3. VERIFY THE REAL REPOSITORY BASELINE

Before writing code:

1. Fetch the latest repository state.
2. Inspect all relevant Phase 1 A–K implementation branches.
3. Identify the final valid integrated baseline.
4. Inspect existing PRs and CI status.
5. Confirm the latest migration number.
6. Read existing social, privacy, consent, security and moderation contracts.
7. Review Phase K release blockers.
8. Determine what already works.

Previously observed:

- PR #9 contains the integrated Phase K work.
- Phase K reported 445 passing automated tests.
- The validated executable source was `4075561ccfd6585b00f7dd9b8b95b96e5000a2c4`.
- Phase K used additive migrations through V43.
- The stacked PR chain had not been automatically merged to main.
- Phase 1 technical work was assessed as complete within its approved scope.
- Production rollout remained unauthorized due to external legal, security, human-operations and platform dependencies.

These are historical observations.

Verify the latest status rather than relying on these values blindly.

Do not confuse a technically integrated branch with an approved production release.

---

# 4. MANDATORY TARGETED REUSE AUDIT

Inspect relevant existing systems:

### Backend

- PostController
- PostService
- PostRepository
- PostReadRepository
- Post and Comment entities
- PostVisibility
- ContentStatus
- CreatePostRequest
- CreateCommentRequest
- SocialController
- SocialService
- SocialInteractionRepository
- SearchService
- NotificationService
- MessagingService
- UserPreferences
- ReportService
- ReportRepository
- AdminService
- AdminController
- AccessPolicy
- Existing account export/deletion
- Existing privacy and governance infrastructure

### Frontend

- `(app)/index.tsx`
- `(app)/create.tsx`
- `(app)/post/[id].tsx`
- `(app)/profile.tsx`
- `(app)/search.tsx`
- `(app)/messages.tsx`
- `(app)/notifications.tsx`
- `features/content/post-card.tsx`
- Existing report and blocking controls
- Existing localization and design system

### Previously verified capabilities

GreenOcean already has:

- Text-based posts
- Experience, question and reflection types
- Anonymous post storage
- Optional anonymous comments in backend DTOs
- PUBLIC/FOLLOWERS/COMMUNITY post audiences
- Server-enforced audience restrictions
- Likes
- Bookmarks
- Basic comments
- Follow relationships
- Private follow requests
- Blocking
- Direct messaging
- Search
- Notifications
- Reporting
- Moderation administration
- Account data export and deletion

Reuse these systems.

Do not create duplicate post, follow, notification or messaging platforms.

---

# 5. KNOWN BASELINE ISSUES TO VERIFY

The last inspected source indicated:

1. The composer treated anonymity as a late-stage switch rather than the first product decision.
2. The composer required a topic/category.
3. `PostService.create()` published newly created posts directly.
4. `Comment` creation also began in PUBLISHED state.
5. `ContentStatus` contained PUBLISHED, HIDDEN, UNDER_REVIEW and DELETED.
6. A dedicated pre-publication safety decision was not integrated into post/comment creation.
7. The Following frontend tab did not apply a genuine following-author filter.
8. The main feed query returned accessible posts chronologically.
9. `parentCommentId` existed, but the comment frontend did not provide a complete nested reply experience.
10. A saved-post collection was not available as a complete user-facing feed.
11. Existing reporting and appeals were reactive, not a complete pre-publication moderation process.
12. Anonymous posts already had some protections in DTO projections and author-profile visibility.

Confirm each issue against the latest code.

Fix actual gaps.

Preserve verified privacy protections.

---

# 6. REQUIREMENT MATRIX

Create:

`docs/phase-l/requirements-matrix.md`

The matrix must cover L-01 through L-20.

For each requirement record:

- Existing implementation
- Approved expected behavior
- Actual gap
- Affected files
- Backend work
- Frontend work
- Database work
- Privacy impact
- Security impact
- Tests
- Final status
- Related PR/commit evidence

Use:

- VERIFIED_REUSED
- TARGETED_ENHANCEMENT
- DEFECTIVE
- MISSING
- IMPLEMENTED_TESTED
- APPROVAL_REQUIRED
- EXTERNAL_BLOCKER

Do not classify old behavior as defective merely because a newer feature is desired.

---

# PART ONE — L1: SAFE PUBLISHING FOUNDATION

# 7. L-01 — Publication identity is the first decision

Priority: CRITICAL P0

Implement a new three-stage post creation experience.

### Stage 1 — Identity

The first meaningful question must be:

**How would you like this post to appear to other members?**

Provide two clearly explained choices.

### Option A — Anonymous

- Other ordinary members do not see the author's username.
- Other ordinary members do not see the author's profile identifier.
- The post does not link to the author's social profile.
- Author-derived recommendations and follower views must not expose ownership.
- Platform-authorized safety systems may retain attribution subject to approved access and retention rules.

### Option B — Social identity

- The existing social display name may appear.
- The existing social username may appear.
- Eligible readers may navigate to the user's profile according to privacy settings.
- No real name or email is automatically disclosed merely because the user chose social identity.

### UX rules

- Present anonymous publishing as a natural, prominent option.
- Do not hide anonymity in an advanced settings menu.
- Do not use a tiny late-stage switch.
- Explain the consequences in short, accessible language.
- Do not silently inherit the previous post's identity choice.
- Require a conscious selection for every new post.
- Provide a back action.
- Preserve the user's in-progress text if they navigate back.
- Support English, Persian and RTL.

The user must be able to choose either mode voluntarily.

### Anonymous default

The anonymous option may be visually recommended.

However, the new publishing flow must still ensure the member understands and makes the decision rather than silently publishing with an assumed identity.

### Backend enforcement

Require an explicit, validated publication identity contract.

Do not accept missing identity intent as authorization to publish under the member's visible identity.

Plan safe compatibility for older clients.

A legacy request must never result in an unintended named publication.

Document the upgrade strategy.

---

# 8. L-02 — Stage 2: Writing the post

Priority: P0

Once identity is selected, display the writing interface.

Support existing post types:

- EXPERIENCE
- QUESTION
- REFLECTION

### Required fields

- Text body
- Identity mode inherited from Stage 1
- Audience control before final publication

### Optional fields

- Topic/category
- Content warning
- Mood or emotional context
- Community destination, only for COMMUNITY audience

### Critical rule

**Do not require a mental-health condition, emotional classification, diagnosis or sensitive topic to create a post.**

A user must be able to publish a general personal experience without assigning themselves to a health category.

Do not automatically populate a topic from private onboarding data.

If an optional topic is used, validate it under the existing applicable privacy and catalog policies.

### Text limits

Reuse the existing bounded server-side text limit unless there is a demonstrated reason to change it.

Show a visible character count when useful.

Support Persian, Arabic and Unicode input correctly.

Do not corrupt zero-width non-joiners or legitimate script characters.

---

# 9. L-03 — Stage 3: Final publication review

Priority: CRITICAL P0

Before publishing, show a clear summary.

### Required elements

- Identity: ANONYMOUS or SOCIAL_IDENTITY
- Audience: FOLLOWERS, COMMUNITY or PUBLIC
- Preview of the post
- Selected optional topic, if any
- Content-warning state
- Community name, if relevant
- Edit/back actions
- Final Publish action

### Identity and audience are independent

Anonymous publishing does not mean public publishing.

Existing audience meanings remain authoritative:

- PUBLIC: Eligible authenticated members
- FOLLOWERS: Approved current followers
- COMMUNITY: Current authorized community members

Do not broaden audience automatically.

Preserve Phase G/H's conservative historical visibility protections.

### Additional confirmation

Broader PUBLIC publication still requires the existing explicit broader-audience confirmation.

If an anonymous post uses a narrow follower/community audience, explain that small audience size or details in the text may still allow someone to infer the author.

Do not promise perfect anonymity.

### Publication response

The UI must confirm only what the backend actually committed.

Show appropriate status:

- Published
- Awaiting review
- Needs changes
- Unable to publish

Never claim publication succeeded merely because a request was submitted.

---

# 10. L-04 — Durable anonymous privacy invariants

Priority: CRITICAL P0

Anonymous-author protection must be enforced server-side across every relevant surface.

### Anonymous posts must not reveal

- Author ID
- Username
- Display name
- Avatar
- Social profile URL
- Email
- Real-world identity
- Relationship-derived author information
- Hidden ownership through notification metadata

### Protect

- Feed
- Post detail
- Search
- Author profile listings
- Likes
- Comments
- Replies
- Saved-post views
- Notifications
- Direct links
- Sharing
- Activity history
- Follower feeds
- Public API projections
- Client caches
- Unread counters and aggregates where identifying

### Following and anonymity

A special privacy rule is required:

Do not place anonymous posts into a named-author Following feed in a way that allows readers to infer the author through the follow relationship.

Anonymous posts may remain discoverable through neutral authorized discovery mechanisms, subject to their audience and content restrictions.

Do not widen an anonymous post's audience merely to make it discoverable.

Document any unavoidable inference risks arising from small audiences, distinctive text or timing.

### Owner access

The actual author may view and manage their own posts privately.

Owner-only APIs may identify the post as owned by that user.

Other ordinary members must not receive owner attribution.

### Platform safety

Authorized moderation investigations may identify authors where necessary.

Such access requires:

- Verified privileged authorization
- Purpose limitation
- Appropriate staff MFA
- Audit logging
- No unrestricted bulk identity lookup
- No public identity exposure

### Immutable publication identity

For the initial Phase L, treat published anonymity mode as immutable.

An edit to an anonymous post must not silently turn it into a named post, or vice versa.

If the user wants a different identity mode, use an explicitly approved new publication workflow.

Do not retroactively relabel historical posts.

---

# 11. L-05 — Drafts, editing and deletion

Priority: P0

Complete a usable author workflow.

### Drafts

Support:

- Start a draft
- Return to a draft
- Edit draft
- Discard draft
- Publish draft
- Recover safely after navigation/reload where approved

Drafts must be owner-scoped.

Never expose unpublished drafts in feed/search/notifications.

Treat potentially sensitive draft content as private data.

Before implementing durable server-side draft storage, verify Phase J's applicable purpose, access, retention, export and erasure policy.

If storage policy is unresolved, present a minimal decision to the owner and keep unsafe persistent storage disabled until resolved.

### Edit published posts

The author should be able to edit the body of their own published post.

Important requirements:

- Enforce server-side ownership.
- Do not permit editing another user's post.
- Prevent moderation bypass through editing.
- Re-run safety review on modified content.
- Do not show an unreviewed new body as if already approved.
- Preserve or safely withdraw the previously approved visible version according to moderation status.
- Do not widen audience automatically.
- Preserve published identity mode.
- Record necessary revision metadata under approved retention rules.

### Delete

Reuse soft deletion or existing authorized deletion mechanisms.

Ensure removal from:

- Feed
- Search
- Direct reads
- Saved-post lists where unauthorized
- Relevant notification previews
- Community feed

Do not expose deleted content through stale frontend state.

Preserve only legally or operationally authorized evidence.

---

# 12. L-06 — Mandatory pre-publication moderation

Priority: CRITICAL P0

Implement an enforceable server-side content safety pipeline for:

- New posts
- Edited posts
- New comments
- Edited comments, if supported
- Relevant future replies

The existing publication path must not bypass moderation.

### Core principle

GreenOcean is a support-oriented community.

Members may express difficult emotions, anger and distress.

However, targeted abuse and prohibited harmful behavior must not be published as ordinary supportive content.

### Minimum policy categories

- Targeted harassment
- Bullying and humiliation
- Threats
- Hate speech
- Sexual harassment
- Doxxing and privacy violations
- Spam and unsolicited promotion
- Dangerous encouragement of harm
- Manipulative or abusive messaging
- Clearly prohibited content under approved community rules

### Important distinction

Do not treat these as equivalent:

1. Expressing distress.
2. Quoting an abusive statement to describe victimization.
3. Using emotionally intense language.
4. Directing abuse or threats at another person.

The content's context matters.

Do not implement a blanket ban on descriptions of mental distress, victimization or suicidal thoughts.

Do not use an unqualified profanity wordlist as the sole decision system.

### Processing outcomes

Every submission must result in a safe authoritative outcome.

Recommended outcomes:

- ALLOW
- REQUEST_REVISION
- HOLD_FOR_REVIEW
- BLOCK_PROHIBITED

Map these decisions onto the existing persistence model with the smallest safe extensions necessary.

Do not conflate a moderation outcome with the permanent content lifecycle.

### ALLOW

Publish normally under the selected audience.

### REQUEST_REVISION

Keep the content unpublished.

Explain respectfully that the wording needs revision.

Preserve the user's draft safely.

Allow resubmission.

### HOLD_FOR_REVIEW

Do not publish while review is pending.

Provide a truthful status.

Do not promise an immediate human response if staffing is unavailable.

### BLOCK_PROHIBITED

Prevent public publication.

Use a proportionate policy-grounded explanation.

Maintain an appropriate appeal/review path.

Do not reveal sensitive anti-abuse detection internals.

---

# 13. L-07 — Practical moderation architecture

Priority: CRITICAL P0

Implement a layered, auditable architecture.

### Layer 1: Deterministic validation

Use established server-side rules for:

- Content length
- Invalid payloads
- Empty content
- Known prohibited links or patterns where approved
- Duplicate flooding
- Excessive repeated posting
- Bounded rate limits
- Structural input abuse

### Layer 2: Context-aware classification

Design a moderation interface that can support policy-approved classifiers.

Requirements:

- Versioned policy/rules
- Bounded processing
- Stable decision categories
- Confidence/uncertainty handling
- Structured reason codes
- No sensitive body text in ordinary logs
- Deterministic test fixtures
- Explicit model/provider configuration where relevant

Do not introduce an external AI moderation provider without owner/privacy approval.

Do not transmit health-related narratives to unapproved third parties.

A rules-only implementation must not claim comprehensive semantic detection.

### Layer 3: Human review

Ambiguous or severe cases must have a restricted review state.

Reuse privileged moderation infrastructure.

### Fail-safe behavior

If the moderation service cannot make an authorized publication decision:

- Do not silently publish unchecked content.
- Preserve the user submission safely.
- Return a recoverable pending or unavailable state.
- Do not indefinitely retry without bounds.
- Do not fabricate human review completion.

### Multilingual robustness

Test:

- Persian formal language
- Persian colloquial language
- Arabic variants of letters
- Zero-width non-joiners
- Mixed Persian/English
- Unicode combining characters
- Word spacing tricks
- Repeated characters
- Emoji
- Quoted insults
- Negation
- Discussion of being harassed
- Genuine threats versus figurative phrases

Normalize for matching without corrupting the displayed original text.

Prevent catastrophic regex behavior.

Do not claim complete profanity or harassment detection from limited keyword tests.

---

# 14. L-08 — Minimum operational safety before rollout

Priority: CRITICAL P0

The technical moderation pipeline is not enough without operational controls.

At minimum verify:

- Authorized moderator access
- Staff MFA gate
- Review queue
- Content takedown
- Report handling
- Audit trail
- Appropriate escalation
- Ability to disable publishing
- Ability to stop an abusive account
- Ability to preserve relevant evidence under approved policy

### No operational review staff

If necessary review staff or privileged infrastructure are unavailable:

- Keep risky content pending and inaccessible.
- Keep unsupported review actions unavailable.
- Document the operational blocker.
- Keep broad production publishing disabled until the necessary operational approval exists.

Do not enable a community solely because a moderation unit test passes.

---

# PART TWO — L2: SOCIAL DISCOVERY AND INTERACTIONS

# 15. L-09 — Real Following feed

Priority: P0

Replace the existing misleading Following presentation with a genuinely distinct server-side feed.

### Requirements

Return only eligible content consistent with current approved follow relationships.

Enforce:

- Original post audience
- Approved follows
- Community membership
- Blocks
- Muted content
- Stronger safety filters
- Moderation state
- Deletions
- Account eligibility

### Anonymous safeguard

Do not use an anonymous post's hidden author relationship to reveal who created it.

If anonymous posts are excluded from the named Following feed, document that clearly.

### Sorting

Use straightforward chronological ordering initially.

Do not use sensitive mental-health topics to rank the feed.

### API

Add or extend a dedicated authorized feed query.

Do not fake Following by filtering an incomplete first page of a generic feed on the client.

### Empty states

Clearly distinguish:

- Following nobody
- No recent permitted posts
- Network failure
- Restricted content

Do not expose why a particular private user's post is unavailable.

---

# 16. L-10 — Discover feed

Priority: P0

Create a truthful Discover experience using permitted content.

### Requirements

- Server-side pagination
- Stable ordering
- Optional category filtering
- Relevant search
- Muted-term controls
- Existing warning filters
- Account eligibility
- Post audience restrictions
- Block restrictions
- Moderation enforcement

### Privacy

PUBLIC means eligible authenticated members, not unauthenticated internet guests.

Never show a FOLLOWERS or COMMUNITY post to an unauthorized member.

Never broaden previously restricted historical content.

### Ranking

Start with chronological discovery.

No diagnosis inference, psychological targeting or sensitive-topic-based ranking.

Do not introduce hidden personalized feeds.

If personalization becomes desirable, require an independently approved privacy contract.

---

# 17. L-11 — Saved posts and personal content library

Priority: P1

Reuse the existing bookmark relation.

Implement:

- View saved posts
- Remove saved posts
- Stable pagination
- Empty state
- Loading/error state
- Server-side authorization checks

A bookmark is not permanent permission to access the post.

When a post becomes inaccessible because of a block, deletion, audience restriction or moderation action, the saved collection must not expose it.

Do not cache inaccessible body text indefinitely.

Allow the owner to remove stale saved entries appropriately.

---

# 18. L-12 — Comments, replies and author management

Priority: P0

Complete the conversation experience without making it chaotic.

### Requirements

- Reply to a post
- Reply to an existing comment
- Bounded thread depth
- Correct parent association
- Chronological or otherwise transparent ordering
- Author-owned deletion
- Author-owned editing, if approved and implemented
- Comment moderation
- Report a comment
- Correct like handling
- No duplicate comment submission
- Accessible thread navigation
- Persian/RTL support

### Suggested UI

Display up to two primary levels of nesting clearly.

Deeper conversations may be flattened with an explicit reply-context reference.

Avoid infinitely nested mobile indentation.

### Anonymous comments

The backend already supports anonymous comments.

Preserve existing anonymous protections.

Do not identify the writer of an anonymous post through a later comment's metadata, special "author" badge, reaction, notification or threading behavior.

The product owner approved optional identity for posts, not an unrestricted right to infer identity from comments.

Before changing the general comment identity policy, document its privacy implications and obtain approval if the change is consequential.

Safe interim behavior:

- No automatic identity disclosure in replies to anonymous posts.
- No hidden author-to-comment linkage visible to ordinary readers.
- No cross-surface metadata leak.
- No use of private author ID as a recommendation signal.

### Anti-harassment

Replies must be subject to the same baseline safety pipeline as posts.

No edited reply may bypass review.

---

# 19. L-13 — Follow relationships and consent

Priority: P0

Reuse existing follow infrastructure.

### Requirements

- Follow an eligible public profile.
- Request follow access to a private profile.
- Accept or decline incoming requests.
- Cancel outgoing requests.
- Unfollow.
- Block and unblock.
- Accurate counts and states.
- No relationship created without an authorized action.

### Privacy safeguards

- Blocked users cannot obtain private relationship data.
- Private profiles reveal only approved fields.
- Anonymous posts do not link back to a hidden author.
- Following another account does not automatically authorize unsolicited messaging.
- Follow notifications do not expose anonymous content attribution.

### UX

Avoid nudging users to collect followers for status or rewards.

Do not introduce popularity leaderboards.

---

# 20. L-14 — Private messaging and message requests

Priority: P0

Reuse the existing direct-message infrastructure and Phase G privacy controls.

Do not build a second messaging system.

### Required improvements

- Truthful new-message permissions
- Understandable request states
- Accept/decline workflow where not already implemented
- Block controls
- Report controls
- Safe unread counts
- Controlled previews
- Correct account switching
- Correct revocation behavior
- No unsolicited message access by URL forging

### Separation from listener support

Ordinary member messaging is distinct from Phase I's live listener sessions.

Do not allow ordinary direct-message access to bypass listener acceptance rules.

Do not silently turn social messaging into a clinically supervised service.

### Anonymous posts

Do not create a direct-message shortcut to the hidden author of an anonymous post.

If users want to contact an anonymous author, that requires a separate approved privacy-preserving product model; do not invent it in Phase L.

---

# 21. L-15 — Notifications and search

Priority: P1

Complete relevant existing social workflows.

### Notifications

Support permitted events such as:

- Follow request
- Follow accepted
- Named post interaction
- Replies
- Allowed comments
- Message request
- Moderation notice

### Privacy

- No anonymous author disclosure.
- No private post body in unrestricted notifications.
- No sensitive story text in lock-screen previews.
- No notifications for inaccessible content.
- No hidden link from anonymous posts to identity.
- Respect user preferences.
- Do not send social pressure reminders for inactivity.

### Search

Reuse existing backend search.

Improve:

- Stable pagination
- Appropriate result ordering
- Empty states
- Block enforcement
- Privacy-aware profile results
- Moderation exclusions
- Content warning handling
- Anonymous attribution protection

Do not index private drafts or pending content for ordinary members.

---

# 22. L-16 — Low-pressure, supportive engagement

Priority: P1

The community's core objective is meaningful support, not attention extraction.

### Design principles

- No popularity leaderboards.
- No public ranking by mental-health disclosure.
- No manipulative daily-post streaks.
- No mandatory check-in to read the feed.
- No automatic emotional profiling.
- No pressure to respond to strangers.
- No misleading claims about "people just like you."
- No recommendation engine driven by diagnosis inference.
- No automatic promotion of distressed content merely because it generates engagement.

Existing likes and bookmarks may remain.

Do not implement expensive engagement features that undermine support-oriented principles without owner approval.

Allow users to participate by reading only.

---

# PART THREE — L3: MODERATION HARDENING AND FINAL INTEGRATION

# 23. L-17 — Complete human moderation workflow

Priority: CRITICAL P0

Complete a reviewable safety process using existing staff authorization.

### Moderator capabilities

- View permitted review queue.
- View minimized necessary evidence.
- Review flagged posts and comments.
- Approve appropriate content.
- Reject prohibited content.
- Request revision.
- Hide published abusive content.
- Record decision reasons.
- Record reviewer identity.
- Preserve auditability.
- Handle repeated abuse proportionately.
- Receive authorized appeals.

### Privacy rules

Moderators must not receive unrestricted access to all private personal content.

Access to anonymous author attribution must be separate, privileged, purpose-limited and audited.

Do not weaken Phase E/F staff MFA.

Do not implement an invisible staff backdoor.

### Decision state safety

Once content is rejected or awaiting review:

- It cannot appear in public/member feeds until valid approval.
- Search cannot reveal it.
- Cached clients must revalidate.
- Old public URLs must respect the new status.
- Concurrent moderator actions must have deterministic outcomes.
- A pending edit cannot inherit approval for an older version.
- A stale approval cannot authorize a newly edited body.

Bind moderation decisions to the exact reviewed content revision.

---

# 24. L-18 — Notices, corrections and appeals

Priority: P0

Members should understand relevant content decisions.

### Required author experiences

- Post published
- Publication pending review
- Changes requested
- Post rejected
- Published post hidden
- Appeal submitted
- Appeal under review
- Appeal resolved

### Notice quality

Give concise and respectful reasons.

Do not expose confidential safety-detection details.

Do not characterize a user as mentally unwell.

Do not present an automated screening result as a clinical assessment.

### Appeals

Reuse Phase J report/appeal foundations where suitable.

Validate:

- Ownership
- Authorization
- No duplicate abusive appeals
- Reviewer independence where applicable
- Auditability
- Correct reinstatement behavior
- Retention under approved policy

Do not fabricate a staffed appeals service.

If staffing is unavailable, record the operational blocker and avoid a false resolution-time promise.

---

# 25. L-19 — Abuse, spam and anonymous misuse prevention

Priority: CRITICAL P0

Anonymous publishing must not mean consequence-free abuse.

Implement effective server-side controls for:

- Excessive posting
- Duplicate submissions
- Spam
- Mass commenting
- Targeted harassment
- Repeated blocked interactions
- Follow-request flooding
- Message-request flooding
- Repeated prohibited content
- Invalid input
- Oversized requests
- Automated abuse
- Attempts to exploit anonymous posting to contact blocked users

### Requirements

- Per-account bounded rate limits
- Appropriate per-action budgets
- Safe idempotency behavior
- No unrestricted public identity exposure
- No global permanent lockout caused by ordinary mistakes
- Documented abuse decisions
- Consistent behavior across backend instances

### Anti-abuse signals

Do not create sensitive health profiles.

Avoid invasive device fingerprinting, precise location tracking or broad behavioral surveillance without approved purpose and privacy review.

Use the least invasive available controls that meaningfully address abuse.

---

# 26. L-20 — Full integration, privacy and quality

Priority: CRITICAL P0

Complete Phase L with comprehensive real execution evidence.

## Four GreenOcean personas

### Happy user

- Logs in.
- Opens social community.
- Selects anonymous identity first.
- Writes a respectful post.
- Chooses eligible audience.
- Reviews identity/audience.
- Publishes.
- Finds the post in a permitted feed.
- Adds and receives comments.
- Saves a post.
- Follows another member.
- Uses an authorized direct message.
- Returns and edits/deletes owned content.

Repeat the publication journey using social identity.

### Creative user

- Uses Persian/RTL.
- Uses English/Persian mixed text.
- Changes identity selection before publishing.
- Creates drafts.
- Navigates backward and forward.
- Uses zero category selections.
- Writes long text within bounds.
- Adds a content warning.
- Quotes an insult while discussing their own experience.
- Uses legitimate angry language without targeting another user.
- Publishes within a private community.
- Uses anonymous comments safely.
- Changes profile privacy after posting.
- Uses multiple devices.
- Reloads during composition.
- Experiences intermittent connectivity.

### Confused user

- Does not understand anonymous versus social identity.
- Attempts publishing without choosing identity.
- Does not know who can see PUBLIC content.
- Starts a post and leaves.
- Tries submitting a post during moderation failure.
- Receives REQUEST_REVISION.
- Sees HOLD_FOR_REVIEW.
- Revises prohibited wording.
- Confuses profile visibility and post audience.
- Attempts to comment on inaccessible content.
- Has no followed users.
- Has no saved posts.
- Attempts a message when the recipient does not allow requests.

### Malicious user

- Forges the identity mode.
- Omits explicit named-publication confirmation.
- Calls legacy publishing APIs.
- Accesses another user's draft.
- Edits another user's post.
- Tries publishing unreviewed edits.
- Replays a previously approved moderation decision.
- Retrieves anonymous author IDs.
- Infers anonymous authors from Following filters.
- Retrieves anonymous authors through notifications.
- Attempts cross-account export leakage.
- Tries to access a private community post.
- Bypasses a block.
- Sends unsolicited direct messages.
- Floods the report system.
- Submits duplicate posts concurrently.
- Uses homoglyphs and mixed scripts to evade simple filters.
- Performs concurrent conflicting moderation actions.
- Uses stale staff privileges.
- Attempts to publish while moderation is unavailable.
- Attempts to infer reviewer-only attribution through public APIs.

Use synthetic isolated test accounts.

Do not run destructive or adversarial tests against real users.

---

# 27. Specific privacy regression tests

Write explicit tests proving:

1. Anonymous author ID absent from post DTO.
2. Anonymous author name absent from post DTO.
3. Anonymous profile link absent.
4. Anonymous author not exposed through direct post lookup.
5. Anonymous author not exposed through search.
6. Anonymous author not exposed through Following.
7. Anonymous author not exposed through notification actors.
8. Anonymous author not exposed through saved posts.
9. Anonymous author not exposed through comment threading.
10. Anonymous author not exposed through activity counts.
11. Anonymous author not exposed through API errors.
12. Anonymous author not exposed through unauthorized exports.
13. Anonymous author not exposed through logging or telemetry.
14. Authorized owner can manage owned content.
15. Privileged identity review is auditable.
16. Block overrides ordinary access.
17. Historical audience restrictions remain intact.
18. Private community membership remains enforced.
19. Revoked access invalidates cached content views.
20. Previously published anonymous posts never silently become named.

Test the exact serialized API responses, not just internal Java objects.

Also test inference risks caused by follower relationships, small audiences and correlation between post and author timestamps.

Where absolute prevention is impossible, document residual risk and provide truthful user guidance.

---

# 28. Moderation security tests

Create a dedicated suite covering:

- Respectful emotional distress
- Anger without targeted harassment
- Quoted abusive words
- Direct personal harassment
- Threats
- Hate speech
- Sexual harassment
- Spam
- Private information exposure
- Harmful encouragement
- Non-clinical peer support boundaries
- Persian idioms
- Unicode obfuscation
- Malicious HTML-like text
- Null and invalid input
- Very long submissions
- Regex attack attempts
- Moderation provider or classifier outage
- Review queue unavailable
- Concurrent content revision
- Stale approval
- Forged staff role
- Illegal status transition
- Unauthorized publication from UNDER_REVIEW
- Appeal ownership bypass

### False positives and false negatives

The final report must evaluate both.

Do not claim 100% moderation accuracy.

Use explicit test categories and policy examples.

If automatic classification cannot confidently resolve a case, use the documented proportionate fallback.

Do not disguise unknown classifications as safe.

---

# 29. Database and Flyway

Use only additive, forward migrations.

Verify the current migration number from the actual integrated code.

### Possible new data structures

Implement only those necessary:

- Explicit publication identity contract
- Owner-scoped drafts
- Post revision/version records
- Moderation assessment and decision records
- Moderation queue references
- Decision notices
- Appeal associations
- Bounded abuse-prevention records
- Indexes for real Following feed
- Indexes for saved collections
- Relevant owner-management metadata

### Preservation requirements

Never overwrite:

- Historical anonymous state
- Historical post audiences
- Private profiles
- Existing follows
- Existing blocks
- Existing reports
- Moderation history
- Account consent records
- Session history
- Existing group membership
- Live listener sessions

### Migration tests

- Empty database
- Populated prior schema
- Anonymous legacy posts
- Named legacy posts
- Existing comments
- Existing bookmarks
- Existing follow requests
- Private community posts
- Deleted posts
- Moderated posts
- Account export/deletion
- Fresh and legacy authentication sessions

Do not rewrite existing Flyway migrations.

Do not silently reclassify historical named posts as anonymous or historical anonymous posts as named.

---

# 30. Access control and API security

All social operations require the applicable existing eligibility and service authorization.

### Verify

- Authenticated identity
- Active account
- Region/service eligibility
- Display-name review where applicable
- Object ownership
- Post audience
- Community membership
- Block restrictions
- Moderation status
- Non-deleted content
- Current relationship authorization
- Correct staff privileges

Do not trust client-supplied author IDs.

A request with `anonymous=true` must never allow forging another author's account.

Client-supplied moderation status is never authoritative.

Only authorized staff can approve or reject restricted content.

Do not reuse a member's ordinary bearer credential as privileged staff authorization.

### API quality

Use:

- Validated DTOs
- Bounded pagination
- Consistent error codes
- Idempotent critical retries where appropriate
- Server-controlled timestamps
- Transactional state changes
- Safe error messages
- No secret leakage

---

# 31. Integration with Phase J and privacy rights

All new Phase L data must be assessed under the existing data-governance model.

### Check

- Purpose limitation
- Sensitive-text handling
- Export
- Correction
- Deletion
- Draft retention
- Moderation records
- Appeals
- Security retention exceptions
- Backups
- Third-party processing
- Staff access logs
- User-facing notices

Do not enable external AI text analysis without approved privacy and processing arrangements.

Do not silently copy support or private journal content into moderation analytics.

Do not claim that removal from a public feed erases content already viewed or copied by another user.

Any consequential unanswered retention, processing or operational decision must be presented to the owner before affected functionality is enabled.

Do not reopen and rewrite Phase J wholesale.

Make targeted compliant changes.

---

# 32. Frontend requirements

The UI must remain consistent with GreenOcean's existing design system.

### Publication

- Separate identity-selection first stage.
- Writing stage.
- Final preview and approval.
- Clear back navigation.
- Draft preservation under the approved storage contract.
- Honest moderation states.
- No accidental named publishing.

### Discovery

- Following
- Discover
- Saved
- Clear empty states
- Stable pagination
- Accurate loading and error feedback

### Social interaction

- Like
- Bookmark
- Reply
- Follow
- Unfollow
- Follow request
- Block
- Report
- Message request

### Accessibility

- English/Persian
- RTL
- Keyboard controls
- VoiceOver/TalkBack readiness
- Dynamic text
- Meaningful labels
- Focus management
- Accessible status and moderation messages

Do not describe JS export tests as proof of actual native screen-reader behavior.

---

# 33. Test execution and evidence

Run real tests after every milestone.

### L1

- Backend unit tests
- PostgreSQL integration tests
- Composer component tests
- Identity decision tests
- Privacy DTO tests
- Draft lifecycle tests
- Moderation decision tests
- Database migration tests
- Full-stack publication test

### L2

- Following feed tests
- Discover feed tests
- Bookmark tests
- Comment/reply tests
- Follow request tests
- Message permissions tests
- Notification tests
- Search privacy tests
- Full-stack social journeys

### L3

- Abuse and spam tests
- Privileged moderation tests
- Review queue tests
- Appeal tests
- Anonymous inference tests
- Cross-account access tests
- Concurrent edit/approval tests
- Regression tests
- Final full-stack A–L compatibility checks

### Run existing regressions

Especially preserve:

- E/F authentication
- G/H content audience
- I live listener-session isolation
- J consent and rights workflows
- K verified integration invariants

Do not conduct a pointless full A–K redevelopment audit.

### True full-stack evidence

Use:

- Real frontend
- Running backend
- Disposable PostgreSQL
- Controlled synthetic users
- Real permission logic

Mock-only tests are not sufficient evidence for complete user journeys.

### Reporting

Report actual:

- Passed
- Failed
- Skipped
- Not executed

Do not fabricate native/device tests.

Use final tested commit hashes.

---

# 34. Product-owner decisions

Do not silently invent new consequential product policies.

The following items are outside current explicit approval or may require clarification:

1. Media attachments and videos.
2. Durable draft retention duration.
3. Changing identity mode after publication.
4. General anonymous-comment default and identity-switching policy.
5. External AI moderation provider.
6. Human review staffing and operational response commitments.
7. Specific moderation policy wording and appeal obligations.
8. Automatic suspension thresholds.
9. User-facing notification of moderator identity.
10. New sensitive analytics or personalization.
11. Publication to unrestricted public internet audiences.
12. Changes to historical content visibility.

### Safe default

Until approved:

- Keep posts text-only.
- Never broaden post audiences.
- Do not change published anonymity mode.
- Do not add unapproved third-party data processing.
- Do not enable sensitive analytics.
- Do not bypass staff MFA.
- Do not activate unsupported public moderation operations.
- Continue unrelated safe development.

When a decision is required, ask the owner in Persian using concise grouped alternatives.

Do not ask again about the already-approved identity-optional model, identity-first composer or support-oriented community principles.

---

# 35. Required documentation

Create or update:

- `docs/phase-l/requirements-matrix.md`
- `docs/product/support-oriented-social-contract.md`
- `docs/product/publication-identity-contract.md`
- `docs/phase-l/anonymous-privacy-invariants.md`
- `docs/phase-l/social-feed-contract.md`
- `docs/phase-l/content-moderation-policy-contract.md`
- `docs/phase-l/moderation-state-machine.md`
- `docs/phase-l/abuse-and-threat-model.md`
- `docs/phase-l/owner-decisions.md`
- `docs/phase-l/operations.md`
- `docs/phase-l/test-evidence.md`
- `docs/phase-l/delivery-report.md`

Use the existing repo documentation structure.

Avoid duplicating authoritative earlier contracts.

---

# 36. Pull Request deliverables

## PR L1 — Safe Publishing

Include:

- First-stage identity choice
- Three-stage composer
- Anonymous guarantees
- Optional categories
- Draft/edit/delete workflow
- Minimum moderation enforcement
- Database migrations
- Focused tests
- Operational safety gating

## PR L2 — Social Interactions

Include:

- Real Following feed
- Discover
- Saved posts
- Comment replies
- Follow requests
- Private message-request improvements
- Notifications
- Search
- Anonymous privacy regression tests

## PR L3 — Community Safety and Final Integration

Include:

- Moderation review and appeals
- Abuse prevention
- Human safety workflow
- Cross-feature regressions
- Database/permission tests
- Full-stack E2E
- Phase L delivery report
- Remaining external blockers

### PR standards

- Correct stacked dependency
- Focused commits
- Reviewable diffs
- No force push
- No silent merge
- No production deployment
- Real test results
- Correct source revision
- Clear remaining risks

---

# 37. Definition of Done

Phase L is technically complete only when:

1. Identity choice is the first publishing step.
2. Both anonymous and social-identity publication function.
3. Identity choice is not silently inherited from an earlier post.
4. A final publication review confirms identity and audience.
5. Anonymous authors are protected across ordinary-member surfaces.
6. Existing audience restrictions remain server-enforced.
7. Optional topic selection does not force sensitive self-disclosure.
8. Drafts, editing and deletion follow the approved data contract.
9. New or edited content cannot bypass pre-publication safety enforcement.
10. Moderation has truthful ALLOW/REVISION/REVIEW/BLOCK outcomes.
11. Human moderation access is properly privileged and auditable.
12. Existing reporting and appeal infrastructure is reused.
13. Abuse prevention has server-side enforcement.
14. Following truly reflects named followed authors' eligible posts.
15. Anonymous posts cannot be deanonymized through Following.
16. Discover enforces audience, moderation and block policies.
17. Saved posts revalidate current viewing permission.
18. Comments and replies operate correctly.
19. Follow requests work accurately.
20. Direct message permissions remain intact.
21. Phase I listener conversations remain isolated.
22. Search and notifications preserve anonymity.
23. User-facing interactions remain voluntary and low-pressure.
24. English/Persian and RTL behaviors are verified.
25. All four persona categories have actual test evidence.
26. Relevant PostgreSQL concurrency tests pass.
27. Full-stack user journeys pass.
28. Existing A–K security and privacy regressions remain intact.
29. Legal/operational blockers are documented rather than fabricated.
30. All three milestone PRs and a final Phase L report are delivered.

Do not claim production readiness merely because the code passes automated tests.

A new social community must not be exposed publicly until the approved content policy, authorized moderation access and necessary operational controls are actually available.

---

# 38. Final delivery report

Provide:

## A. Verified baseline

Latest A–K code and dependency chain.

## B. Completed L1

Identity-first composer, author privacy and minimum content safety.

## C. Completed L2

Feeds, replies, follow relationships, saved posts and private messages.

## D. Completed L3

Moderation, abuse prevention and full integration.

## E. Requirements

L-01 through L-20 with evidence.

## F. Architecture

New services, schema, endpoints, state machines and permissions.

## G. Privacy and security

Anonymous-author protections and residual inference risks.

## H. Content safety

Policy categories, review lifecycle, false-positive/negative analysis and staffing requirements.

## I. Four-persona testing

Happy, Creative, Confused and Malicious results.

## J. CI and test evidence

Exact commands and actual outcomes.

## K. Remaining owner/external blockers

Legal, staff, infrastructure, native and operational items.

## L. GitHub

Branch names, PR links, commits, dependency chain and review status.

## M. Phase M handoff

Only genuinely remaining community/group features for the next phase.

---

# FINAL EXECUTION DIRECTIVE

Implement the full Phase L as three sequential, reviewable subphases.

Start from the latest valid Phase K integrated GreenOcean baseline.

Reuse existing posts, comments, follows, feeds, messaging, privacy, notifications, reporting and legal governance.

Implement anonymous-first, identity-optional publishing exactly as approved.

Make publication identity the first explicit user decision.

Use the second stage for writing and the final stage for preview, audience confirmation and publication.

Ensure anonymous identity is hidden from other ordinary members throughout all social interactions.

Do not equate anonymity with immunity from authorized safety accountability.

Implement basic content safety enforcement in L1 before broad publication.

Complete the remaining social features in L2.

Harden moderation, abuse prevention and full integration in L3.

Avoid aggressive censorship of legitimate personal distress.

Prevent harassment, threats, hate speech, bullying and privacy abuse through proportionate safeguards, genuine review processes and enforceable server-side rules.

Do not introduce unapproved AI providers, clinical profiling, media processing or sensitive analytics.

Maintain all A–K security, privacy, legal and service authorization contracts.

Run real PostgreSQL, frontend, full-stack, privacy, security, concurrent and four-persona tests.

Fix confirmed defects, provide truthful evidence and produce three reviewable PRs plus the comprehensive final Phase L delivery report.

**Expected outcome: GreenOcean provides a trustworthy, support-oriented social community in which members can intentionally publish anonymously or under their social identity, interact respectfully, discover permitted content, build consensual connections and remain protected by enforceable privacy and moderation safeguards.**
