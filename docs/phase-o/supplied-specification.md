# GreenOcean — Phase O
## Personal Growth, Private Self-Care, Curated Knowledge, Educational Courses and Ethical Growth Rewards

### Comprehensive Implementation Specification — O1 + O2 + O3 + O4
### Four Sequential, Independently Reviewable Pull Requests

---

# 1. MISSION

You are the principal full-stack engineer, Spring Boot architect, PostgreSQL engineer, React Native UX engineer, privacy-by-design specialist, educational-content systems architect, content-governance specialist, security engineer and quality assurance lead for GreenOcean.

Repository:

`mehrnaz-sarafraz/GreenOcean`

Technology stack:

- Java 21
- Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router
- TypeScript
- Web / Android / iOS

Implement the complete:

**Phase O — Personal Growth and Self-Care**

GreenOcean is a privacy-first, non-clinical peer-support and personal-growth platform.

Phase O must provide a coherent personal-growth environment where members can:

1. Reflect privately.
2. Record their mood voluntarily.
3. Maintain a personal journal.
4. Set manageable personal goals.
5. Answer three optional daily self-reflection questions.
6. Explore reviewed educational content.
7. Discover relevant books, films, music and other learning resources.
8. Access educational articles and notes created by authorized contributors.
9. Explore relevant educational resources attached to eligible social posts.
10. Complete real learning modules and exercises.
11. Track genuine learning progress.
12. Earn private growth points and learning badges.
13. Control their personal data.

The product must encourage autonomy and meaningful learning rather than compulsive engagement.

Phase O must not become:

- A clinical diagnosis platform.
- An AI therapist.
- A compulsory mental-health diary.
- A public competition.
- A content-streaming service.
- An unregulated clinical education provider.
- A tool that rewards sensitive disclosure.
- An attention-maximizing engagement system.

Deliver fully implemented code, migrations, interfaces, tests, documentation and four reviewable Pull Requests.

Do not merely produce a design proposal.

---

# 2. APPROVED PRODUCT DECISIONS

These product decisions are approved.

Do not reopen them unnecessarily.

## O-D01 — Four implementation milestones

- O1: Private self-care environment.
- O2: Growth Library and reviewed specialist content.
- O3: Educational courses and daily self-discovery.
- O4: Private growth points, badges and final integration.

## O-D02 — Private wellbeing data

Personal journals, mood records, self-reflection responses, personal goals and private progress information are private account data.

They are not automatically published to social feeds, communities, listeners or professionals.

## O-D03 — Three optional daily questions

Provide three reviewed self-reflection prompts per day.

Members may answer:

- Zero questions.
- One question.
- Two questions.
- All three questions.

Skipping is always permitted.

The experience must not punish users for missing days.

## O-D04 — Curated Growth Library

The library may feature:

- Books.
- Films.
- Documentaries.
- Music.
- Educational articles.
- Short specialist notes.
- Practical guides.
- Guided reflection templates.
- Reviewed learning resources.

The purpose is education, reflection and personal growth.

## O-D05 — Authorized educational publishers

Ordinary members cannot directly publish official GreenOcean educational articles or courses.

Authorized editorial contributors may prepare general educational content.

Verified professionals may submit content within their verified scope.

Publication requires applicable content review and authorization.

## O-D06 — Educational resources linked to posts

Approved educational resources may appear in a section titled:

**For Further Reading / برای مطالعه بیشتر**

This feature must preserve post privacy, anonymous authorship, user autonomy and editorial accountability.

## O-D07 — Courses

Provide genuine educational lessons and exercises.

A progress indicator must represent actual learning activity, not arbitrary button presses.

## O-D08 — Adult-first scope

Current Phase O educational experiences serve adults eligible under the existing GreenOcean access policy.

Do not onboard minors or implement child-specific programs by merely introducing an age filter.

## O-D09 — Private growth points

Use **Model A: Personal Growth Rewards**.

Growth points are:

- Private.
- Non-monetary.
- Non-transferable.
- Not purchasable.
- Not exchangeable for priority support.
- Not a clinical wellbeing score.
- Not a measure of the user's worth.

## O-D10 — Ethical reward eligibility

Reward verified educational participation and genuine curriculum milestones.

Do not award points simply for:

- Writing private journal entries.
- Disclosing distress.
- Recording a particular mood.
- Revealing health information.
- Requesting listener support.
- Attending a support session.
- Posting or commenting repeatedly.
- Receiving likes.
- Collecting followers.
- Reporting abuse.
- Participating in crisis or urgent-help workflows.

## O-D11 — Text-first implementation

Use text-based educational materials and external references.

Do not build an audio/video streaming infrastructure or introduce third-party AI analysis without separate approval.

---

# 3. EXECUTION ORDER

Implement four dependent milestones.

## O1 — Private Reflection, Mood and Gentle Goals

- Personal wellbeing hub.
- Private journal CRUD.
- Private writing drafts where authorized.
- Mood check-ins.
- Mood history and descriptive trends.
- Gentle goal management.
- Goal steps and progress.
- Three optional daily questions.
- Privacy lifecycle and account data controls.

## O2 — Growth Library and Reviewed Content

- Curated book, film and music recommendations.
- Authorized contributor roles.
- Specialist articles.
- Short educational notes.
- Practical learning guides.
- Reusable blank reflection templates.
- Editorial workflow.
- Credential and source validation.
- For Further Reading attachments to posts.
- Appropriate content warnings and accessibility.

## O3 — Courses and Learning Journeys

- Educational curriculum structure.
- Modules and lessons.
- Real lesson content.
- Learning exercises.
- Optional non-diagnostic learning checks.
- Lesson-level completion.
- Real course progress.
- Reviewed self-reflection question bank.
- Adult-focused content suitability.
- Accessible course UI.

## O4 — Ethical Growth Rewards and Final Integration

- Private point ledger.
- Server-authoritative reward issuance.
- Learning badges.
- Anti-abuse controls.
- Data rights.
- Privacy and security regression.
- Complete end-to-end journeys.
- Four-persona testing.
- Final Phase O delivery report.

### Branch structure

Use:

- `feature/phase-o1-private-selfcare`
- `feature/phase-o2-growth-library`
- `feature/phase-o3-learning-courses`
- `feature/phase-o4-private-growth-rewards`

Create four independently reviewable stacked PRs.

O2 builds on validated O1.

O3 builds on validated O2.

O4 builds on validated O3.

Do not automatically merge or deploy.

---

# 4. VERIFY THE ACTUAL REPOSITORY BASELINE

Before implementation:

1. Fetch the latest repository state.
2. Inspect main.
3. Inspect existing Phase L, M and N PRs.
4. Verify the latest validated N3 baseline.
5. Confirm current Flyway migrations.
6. Inspect CI status and executable commits.
7. Read Phase J governance requirements.
8. Read the existing wellbeing and content architecture.
9. Identify real implementation gaps.
10. Preserve all validated A–N behavior.

Historical repository observations:

- Phase K was merged into main through PR #9.
- Phase L implementation exists in PRs #10–12.
- Phase M implementation exists in PRs #13–15.
- Phase N1 exists in PR #16.
- Phase N2 exists in PR #17.
- N3 had not yet been verified at the last inspection.
- These later PRs were stacked and not automatically merged into main.

Verify the actual current state.

Do not assume a previously reported result is still the latest.

### Required baseline

Build Phase O on the latest valid, tested Phase N3 integration when available.

If N3 remains incomplete:

- Do not invent completion.
- Do not rebuild unrelated N functionality.
- Work only on safely separable changes.
- Document dependency blockers.
- Avoid an incorrect PR base.
- Do not bypass required integration and privacy tests.

---

# 5. EXISTING FUNCTIONALITY TO REUSE

GreenOcean already contains significant wellbeing infrastructure.

Inspect:

### Backend

- `WellbeingController`
- `WellbeingService`
- `WellbeingRepository`
- `WellbeingSnapshotResponse`
- `CreateCheckInRequest`
- `CreateJournalEntryRequest`
- `CreateGoalRequest`
- `UpdateGoalRequest`
- `CreateReflectionRequest`
- `UpdateGrowthPathProgressRequest`
- `CatalogController`
- `CatalogService`
- `CatalogRepository`
- `ArticleResponse`
- `CreateArticleRequest`
- `MediaRecommendationResponse`
- Existing professional verification
- Existing administrative content review
- Existing account archive and deletion
- Existing notification preferences
- Existing Phase L post privacy and moderation
- Existing Phase J data-governance infrastructure

### Frontend

- `(app)/tools/[tool].tsx`
- Existing WellbeingHub
- CheckIns
- Goals
- Journal
- Assessment
- GrowthPaths
- Companion
- Articles
- Article detail
- Saved content
- Media recommendations
- Profile privacy
- Account data export
- Existing localization and design system

### Database

Inspect existing tables including:

- `wellbeing_check_ins`
- `wellbeing_journal_entries`
- `wellbeing_goals`
- `wellbeing_reflections`
- `guided_growth_paths`
- `user_growth_path_progress`
- `professional_articles`
- `media_recommendations`
- `article_saves`
- `media_saves`

Inspect the current schema before introducing new tables.

### Known gaps to verify

Previously observed:

1. Journal supports creation and deletion, but not a complete edit workflow.
2. Mood records are stored, but history and trend controls are limited.
3. The wellbeing snapshot returns a bounded number of recent records instead of a complete paginated history.
4. Goal progress can be advanced by an arbitrary percentage without a corresponding milestone.
5. Learning paths can be advanced by arbitrary percentage increments without actual lesson completion.
6. Existing growth-path metadata can advertise lesson counts without complete lesson content.
7. Current guided reflection writes answers into the journal, requiring clearer explicit save semantics.
8. Educational article submission is restricted to verified professionals, but the complete editorial lifecycle must be audited.
9. Media recommendations already exist, but books and source/rights governance require further development.
10. Certain historical educational seed records contain named review/credential claims that must not be treated as independently verified without evidence.

Confirm actual current behavior.

Do not overwrite old data or duplicate functioning systems.

---

# 6. REQUIREMENTS MATRIX

Create:

`docs/phase-o/requirements-matrix.md`

Track requirements O-01 through O-36.

For each requirement document:

- Approved product behavior.
- Existing implementation.
- Identified gap.
- Backend work.
- Frontend work.
- Data model.
- Privacy impact.
- Authorization.
- Content governance.
- Required tests.
- Actual implementation evidence.
- Final status.

Use statuses:

- VERIFIED_REUSED
- TARGETED_ENHANCEMENT
- MISSING
- IMPLEMENTED_TESTED
- APPROVAL_REQUIRED
- OPERATIONAL_BLOCKER
- NOT_EXECUTED

Maintain exact PR and commit references.

---

# PART I — O1: PRIVATE SELF-CARE

# 7. O-01 — Unified private wellbeing hub

Priority: P0

Create a coherent personal wellbeing destination.

Suggested user-facing name:

**My Wellbeing / فضای خودمراقبتی من**

Include:

- Private journal.
- Mood check-ins.
- Mood history.
- Gentle goals.
- Daily reflection.
- Guided reflection.
- Educational progress.
- Growth Library entry.

The hub must feel calm, supportive and optional.

### UX principles

- No pressure to participate.
- No guilt-based messages.
- No fabricated mental-health improvement.
- No competitive comparison.
- No requirement to publish social content.
- No requirement to contact a listener.
- No default disclosure of sensitive data.

Reuse existing navigation where appropriate.

Do not introduce unnecessary duplicate home screens.

---

# 8. O-02 — Complete private journal lifecycle

Priority: CRITICAL P0

Extend the existing journal.

Support:

- Create.
- Read.
- Edit.
- Delete.
- Search own entries.
- Sort by date.
- Paginated history.
- Optional title.
- Optional mood label.
- Optional private tags.
- Clear loading/error states.
- Safe edit conflict handling.
- Explicit save confirmation.

### Privacy

The journal is private account data.

Never expose journal entries through:

- Public profile APIs.
- Social feed.
- Community posts.
- Search indexing for other members.
- Listener session access.
- Professional dashboards.
- Notifications to other users.
- Growth recommendations.
- Analytics payloads.
- Administrative content tools without separately authorized purpose.

### No implicit publication

Never add an automatic "share your journal" action.

If a future explicit sharing feature is approved, it must create a separate, user-reviewed social post under Phase L's identity and audience controls.

Do not let journal content inherit a post's visibility setting.

### Drafts

Provide safe unsaved-changes handling.

Durable drafts require approved sensitive-data storage and retention.

Avoid storing private journal content in unprotected device storage.

Do not promise fully offline private persistence unless implemented securely and tested.

### Edit concurrency

Protect against stale updates and accidental overwrites.

Use version checks or an equivalent reliable strategy.

### Delete

Require owner authorization.

Update pagination, caches, search results and account archive behavior.

Use the approved erasure contract.

Do not promise physical deletion from all backups without verified implementation.

---

# 9. O-03 — Mood check-ins

Priority: P0

Reuse existing mood records.

Support:

- Voluntary recording.
- Optional private note.
- Clear timestamp.
- Editing and deletion.
- Paginated history.
- Multiple entries per day when permitted.
- User-friendly labels.
- Timezone-correct calendar display.

### Mood labels

Use understandable, non-clinical labels.

Avoid presenting every emotion on a simplistic good-versus-bad scale.

A difficult feeling is not a failure.

### Validation

Use server-authoritative timestamps.

Support Unicode, Persian and mixed-language notes.

Protect against duplicate submissions.

Do not require a sensitive diagnosis or mental-health category.

---

# 10. O-04 — Mood history and descriptive trends

Priority: P1

Provide privacy-preserving visual summaries.

Possible views:

- Calendar of self-recorded moods.
- Last seven days.
- Last thirty days.
- Longer user-selected periods.
- Frequency distribution of selected labels.
- Ability to inspect an individual entry.

### Critical limitation

The visualization describes user-recorded observations only.

It must not:

- Infer depression.
- Infer anxiety disorders.
- Calculate suicide risk.
- Score mental wellness clinically.
- Predict future emotional crises.
- Claim treatment success.
- Automatically share trends with listeners.
- Automatically recommend professional care based on a proprietary risk score.

### Missing data

Do not treat days without entries as negative or positive.

Do not imply that incomplete tracking is a failure.

### Performance

Avoid loading all historical sensitive records into a single giant response.

Use bounded, owner-scoped pagination and aggregation.

---

# 11. O-05 — Gentle personal goals

Priority: P0

Extend the existing goals system.

Support:

- Create goal.
- Edit title.
- Choose optional cadence.
- Add measurable or qualitative steps.
- Edit steps.
- Mark individual steps complete.
- Pause goal.
- Resume goal.
- Complete goal.
- Reopen if necessary.
- Archive goal.
- Delete goal.
- View own historical goals.

### Goal progress

Stop using arbitrary frontend increments as proof of progress.

For structured goals, derive progress from actual completed steps.

For open-ended qualitative goals, use a clear self-reported status without inventing objective percentage accuracy.

### Example

Goal: "Spend more time outdoors."

Steps:

1. Choose a convenient time.
2. Try one short walk.
3. Reflect on whether this routine is useful.

The member can modify the goal without a penalty.

### No pressure

Do not introduce:

- Mandatory daily streaks.
- Public achievement leaderboards.
- Punishment for missed goals.
- Shame-based completion messages.
- Automatic failure classifications.
- Forced goal sharing.

Goals remain private by default.

---

# 12. O-06 — Three optional daily reflection questions

Priority: P0

Implement a genuine daily reflection experience.

Every eligible day presents three editorially approved questions.

### Requirements

- Three prompts per day.
- Clear local date.
- User timezone awareness.
- Prompts from a reviewed question bank.
- Optional answers.
- Ability to skip.
- Ability to save explicitly.
- Ability to edit saved answers.
- Ability to delete saved answers.
- Private history.
- No diagnosis inference.
- No compulsory notifications.

### Example prompts

- "What helped you feel more comfortable today?"
- "What would you like to understand better about yourself?"
- "What small step might make tomorrow easier?"

Questions should be open-ended, gentle and non-judgmental.

### Daily selection

Use deterministic server-authoritative selection from currently published, approved prompts.

Persist or version the actual selected prompt set so that:

- Reloading does not unexpectedly replace questions.
- Timezone changes do not duplicate rewards or answers.
- A later editorial change does not silently rewrite historical answers.
- Old saved answers remain associated with the correct question revision.

Handle date boundaries explicitly.

### Answer storage

Do not store an answer merely because it was typed.

A member must explicitly save.

Never send answers to an external model.

Never attach responses to social profiles or listener histories.

### No streak pressure

Missing a day is neutral.

Do not display "You lost your progress" messages.

---

# 13. O-07 — Guided reflection companion

Priority: P1

Reuse the existing fixed-prompt reflection companion.

It must remain a guided writing tool, not an AI therapist.

### Enhance

- Clear prompt sequence.
- Back/next controls.
- Skip option.
- Explicit private save.
- Unsaved-change warning.
- Accurate progress indication.
- Reuse the private journal only with deliberate consent.
- Accessible text inputs.
- Localized copy.

### Scope

Do not:

- Simulate a human therapist.
- Claim to understand the user's mental state.
- Analyze responses clinically.
- Automatically classify risk.
- Send responses to third-party AI services.
- Initiate emergency interventions based on unvalidated interpretations.

Keep urgent-resource links independent and accessible.

---

# 14. O-08 — Existing wellbeing assessments

Priority: CRITICAL P0

Audit the existing five-question reflection tool.

The existing score and bands must not be presented as validated clinical measurements.

### Requirements

- Clearly describe the tool as non-diagnostic.
- Review whether a numerical total is appropriate.
- Do not interpret low scores as a medical condition.
- Do not automatically infer treatment needs.
- Preserve existing private records.
- Do not silently reclassify historical answers.
- Keep optional participation.
- Distinguish editorial guidance from clinical evidence.

If current scoring language is misleading, replace it with transparent descriptive feedback without fabricating scientific validity.

Any clinically validated instrument would require separate licensing, appropriate use authorization and a dedicated product decision.

---

# 15. O-09 — Sensitive data protection

Priority: CRITICAL P0

All O1 data are potentially sensitive.

### Required

- Owner-only authorization.
- Protected DTO projections.
- No public IDs usable for cross-account access.
- Account-switch isolation.
- Logout cache clearing.
- App background privacy handling.
- Bounded request bodies.
- Safe Unicode processing.
- No sensitive text in ordinary logs.
- No journal text in push notifications.
- No hidden sensitive analytics.
- Correct archive and deletion integration.

### Data rights

Reuse Phase J rights infrastructure.

For every new category define:

- Purpose.
- Authorized viewers.
- Retention basis.
- Export format.
- Erasure behavior.
- Backup handling.
- Legal/safety exception policy if applicable.

If consequential retention or processing rules remain unapproved, keep the affected new optional processing disabled rather than inventing a policy.

---

# PART II — O2: GROWTH LIBRARY AND VERIFIED CONTENT

# 16. O-10 — Growth Library architecture

Priority: P0

Create one coherent educational resource library.

Suggested name:

**Growth Library / کتابخانه رشد**

Organize resources by:

- Books.
- Films.
- Documentaries.
- Music.
- Articles.
- Short educational notes.
- Practical guides.
- Reflection templates.

### Discovery

Support:

- Browse categories.
- Search.
- Filter by resource type.
- View resource detail.
- Save for later.
- Remove saved item.
- View published references.
- View content warnings.
- View suitability information.

### Quality

Do not recommend resources merely because they are popular.

Do not optimize primarily for engagement.

Do not personalize recommendations using private diagnosis, journal entries, mood history or listener transcripts.

Use editorial categorization and explicit member-selected topics.

---

# 17. O-11 — Book recommendations

Priority: P0

Introduce a structured book resource type.

Possible metadata:

- Title.
- Author.
- Publication year.
- Language.
- Short description.
- General subject.
- Why the book is suggested.
- Suitable audience.
- Content warnings where relevant.
- Source/publisher reference.
- Legitimate external availability link.
- Editorial reviewer.
- Review date.

### Copyright

Do not copy complete copyrighted books.

Do not create unauthorized downloads.

Do not generate fabricated reading links.

Use legitimate sources and metadata.

### Safety

A book recommendation is not proof that reading it will treat a condition.

Avoid guaranteed therapeutic claims.

---

# 18. O-12 — Film, documentary and music recommendations

Priority: P0

Reuse existing media recommendation infrastructure.

Extend it where necessary.

### Support

- Film.
- Documentary.
- Music.
- Other approved educational media categories.

### Metadata

Include:

- Title.
- Creator/artist/director where relevant.
- General theme.
- Short description.
- Reason for recommendation.
- Content warnings.
- Age suitability.
- Language.
- Source reference.
- Legitimate external destination.

### Restrictions

Do not build:

- A streaming player.
- A copyrighted music library.
- An unauthorized video host.
- A music-download service.
- A platform reproducing copyrighted lyrics or scripts.

Do not present third-party music as medical treatment.

No unapproved autoplay.

No intrusive tracking of external media consumption.

---

# 19. O-13 — Authorized content contributor roles

Priority: CRITICAL P0

Separate social participation from official educational publishing.

### Contributor classes

#### Authorized editorial contributor

A platform account with an explicit current educational-content permission.

May prepare general educational material within the approved editorial scope.

#### Verified professional contributor

An account with independently verified professional qualifications and an approved contribution scope.

May submit content relevant to those qualifications.

#### Independent reviewer

An authorized person with the appropriate expertise and review permission for the specific content category.

### Critical rules

- Being a social group OWNER does not grant educational-publishing rights.
- Being a group MODERATOR does not grant professional authority.
- Being a platform ADMIN does not automatically make someone a psychologist or doctor.
- A professional's credential cannot be inferred from a display name.
- Content attribution must reflect real verified qualifications.
- A contributor cannot self-certify professional verification.
- Expired or revoked qualifications must affect future publication permissions.

### Publication permission

Separate:

- Create draft.
- Edit own draft.
- Submit for review.
- Review content.
- Approve content.
- Publish.
- Pin or feature official content.
- Retire content.

Enforce permissions server-side.

---

# 20. O-14 — Educational content types

Priority: P0

Support:

### Short note

A brief practical educational idea.

### Article

A longer, structured explanation with sources.

### Practical guide

A sequence of general educational steps.

### Reflection template

A reusable set of prompts that a member can optionally complete in their private space.

### Course lesson

A structured educational unit attached to an approved curriculum.

### Important terminology

Do not confuse:

- A member's private journal entry.
- An educational article.
- An official reflection template.

They must have separate data ownership and publication rules.

An empty reflection template is published educational content.

A member's completed reflection is private user content.

---

# 21. O-15 — Editorial publication workflow

Priority: CRITICAL P0

Implement a complete content lifecycle.

Suggested states:

- DRAFT
- SUBMITTED
- NEEDS_REVISION
- APPROVED
- PUBLISHED
- RETIRED
- REJECTED

Use existing review infrastructure where appropriate.

### Publishing requirements

- Verified author permission.
- Review of current content revision.
- Appropriate independent reviewer.
- Reviewer identity.
- Source references.
- Actual review date.
- Evidence/scope description.
- Age suitability.
- Content warnings.
- Copyright/licensing check where applicable.
- Approval before public display.

### Revision safety

A previously approved article cannot be changed to unreviewed content while retaining its approval.

Bind review to an exact revision.

Published edits must follow an approved revision workflow.

### Review transparency

An article may display:

- Authorship.
- Verified role, if genuine.
- Sources.
- Review status.
- Actual review date.

Do not display "clinically validated" merely because an editor approved publication.

### Historical content

Audit existing educational seed data.

If historical sample records contain unverified professional names or review assertions, do not display them as independently verified.

Use a forward-only correction, restricted publication state or verified editorial remediation as appropriate.

Do not rewrite applied migration history.

Do not invent citations, clinicians or credential approval.

---

# 22. O-16 — For Further Reading on social posts

Priority: CRITICAL P0

Implement an optional educational-resource attachment system.

User-facing title:

**For Further Reading / برای مطالعه بیشتر**

This feature connects the social community with educational resources without compromising privacy.

### Supported posts

- Named posts.
- Anonymous posts.

Both must follow the same post audience and privacy contracts.

### Author consent

The original post author controls whether educational resources may be attached to that post.

Use an explicit per-post opt-in or equivalent clear authorization.

No automatic opt-in for old posts.

The author may revoke permission.

### Suggested lifecycle

1. The author explicitly allows resource suggestions.
2. An authorized contributor or editor views a post they are already permitted to access.
3. They propose a relevant published educational resource.
4. The proposal is reviewed under the appropriate editorial policy.
5. The system checks the author's current attachment preference.
6. The association becomes visible to the post's authorized readers.
7. The author may remove the association or disable future attachments.
8. A retired resource is no longer actively recommended.

### Crucial privacy boundaries

- Do not expose anonymous authors.
- Do not expose author IDs to educational contributors.
- Do not widen post visibility.
- Do not allow a contributor to access posts outside their existing authorized audience.
- Do not grant an editor automatic access to private group content.
- Do not reveal private group membership.
- Do not index private narratives in educational search.
- Do not copy the post body into unrelated content-review logs.
- Do not send post text to unapproved external AI providers.

### Restricted audiences

A contributor cannot attach to a FOLLOWERS or COMMUNITY post unless they legitimately have the required current post access and the feature's additional authorization.

For restricted/hidden groups, fail closed if the proposed editorial workflow cannot preserve those constraints.

Do not create an exception solely for convenience.

### No diagnosis by attachment

Attaching an article must not imply that a professional diagnosed the post author.

Display neutral educational language.

Example:

"This resource offers general information related to the topic. It is not a personal assessment."

### Do not alter the original post

Educational contributors cannot:

- Rewrite post body.
- Change post anonymity.
- Change post audience.
- Change author identity.
- Convert the post into a professional consultation.
- Initiate unsolicited private contact.

### Moderation priority

If a post is removed, hidden, restricted or no longer accessible, its resource attachments must follow the same restrictions.

---

# 23. O-17 — Educational resource search, saves and quality

Priority: P1

Reuse existing search and saved-item infrastructure where practical.

### Requirements

- Search published educational resources.
- Filter resource type.
- Filter topic.
- View available language.
- Save resource privately.
- Remove saved resource.
- Enforce retirement/availability.
- Paginate.
- Avoid duplicates.
- Respect region and content eligibility where applicable.

### No sensitive profiling

Do not infer personal health conditions from a member's saved-resource list.

Do not automatically display personal journal-derived recommendations.

### Source quality

A source may be described as evidence-informed only when the editorial record supports the claim.

Maintain a clear distinction between:

- General education.
- Professional opinion.
- Research-backed guidance.
- Clinical treatment guidance.

Do not collapse these into one generic "verified" badge.

---

# PART III — O3: COURSES AND SELF-DISCOVERY

# 24. O-18 — Real course architecture

Priority: P0

Build on existing `guided_growth_paths`.

Implement genuine content hierarchy:

Course
→ Module
→ Lesson
→ Optional exercise
→ Completion.

### Course fields

- Title.
- Summary.
- Intended audience.
- General topic.
- Learning objectives.
- Language.
- Estimated duration.
- Author/editor attribution.
- Source references.
- Editorial review status.
- Current version.
- Publication state.

### Module fields

- Title.
- Description.
- Position/order.
- Lessons.

### Lesson fields

- Actual educational content.
- Learning objectives.
- Estimated duration.
- Optional educational exercise.
- Approved source references.
- Content warnings if applicable.

### Do not fake lessons

A course with "five lessons" must contain five real accessible lessons.

Do not use placeholder content in production.

Do not display a percentage unrelated to actual completion.

---

# 25. O-19 — Learning progress

Priority: CRITICAL P0

Replace arbitrary progress increments with verifiable lesson-level events.

### Requirements

- Start course.
- View lesson.
- Complete lesson explicitly.
- Mark completion only when the authorized action is valid.
- View completed lessons.
- Resume course.
- Complete course.
- Review completed content.

### Progress calculation

For example:

Completed required lessons / Total required lessons.

The server calculates the value.

Do not accept arbitrary client-provided progress percentages as authoritative.

### Revision handling

Version course content.

A new course revision must not silently erase legitimately completed historical lessons.

Define whether new required lessons affect current progress.

Show a truthful explanation.

### Avoid surveillance

Do not require invasive reading-time monitoring, camera analysis or behavior tracking to prove learning.

An explicit meaningful completion action is sufficient for ordinary self-paced material.

---

# 26. O-20 — Learning exercises

Priority: P1

Offer voluntary learning exercises.

Examples:

- Identify a personal boundary.
- Reflect on a communication example.
- Organize small goals.
- Practice a general breathing or grounding instruction.
- Consider alternative responses to a difficult situation.

### Exercise types

- Reading and reflection.
- General multiple-choice knowledge check.
- Personal writing prompt.
- Small action checklist.

### Sensitive answers

Private writing responses must not be required for course completion or reward eligibility unless a separate approved product decision explicitly changes that rule.

Prefer allowing users to complete a learning step without storing sensitive free-text responses.

Do not transmit personal responses to reviewers by default.

---

# 27. O-21 — Educational question checks

Priority: P1

Provide optional factual or conceptual learning checks.

They should assess understanding of the lesson, not psychological condition.

Example:

"Which response respects another person's boundaries?"

### Requirements

- Correct answer/reference based on the published lesson.
- Accessible explanations.
- Retake allowed.
- No shame for mistakes.
- No diagnosis.
- No hidden personality score.
- No public grade.

Do not reward unlimited repeated submissions.

---

# 28. O-22 — Adult-focused course suitability

Priority: CRITICAL P0

The current GreenOcean platform uses adult eligibility controls.

Phase O must respect those controls.

### Initial policy

Courses are for eligible adults.

Categorize using general learning needs and life-stage themes rather than automatically collecting precise birthdates.

### Do not

- Create child accounts.
- Advertise a minor-only service as available.
- Infer age from journal content.
- Bypass account-age authorization.
- Treat adult age eligibility as clinical suitability.

Child and adolescent learning programs require separate protection, legal, educational and product review.

Keep such programs outside this Phase O release.

---

# 29. O-23 — Reviewed daily question bank

Priority: P0

The daily questions in O1 must be backed by a reusable, versioned editorial system.

### Content requirements

- Positive, neutral and non-judgmental.
- General self-reflection.
- Suitable for adults.
- Written in clear language.
- Available in approved translations.
- Reviewed before publication.
- Avoid guaranteed psychological conclusions.
- Avoid forced disclosure.

### Workflow

Authorized contributor drafts questions.

Authorized reviewer approves exact question revisions.

Published prompt sets become eligible for daily selection.

Retired questions are not selected for new days.

Historical saved answers remain associated with their original versions.

### Quantity

Seed enough genuinely reviewed prompts for meaningful variation only when real approved content is available.

Do not populate the production database with fabricated clinical claims, fake expert attributions or unapproved sensitive prompts.

A smaller honest approved bank is preferable to a large fake one.

---

# 30. O-24 — Course completion integrity

Priority: CRITICAL P0

Define what constitutes actual completion.

### Valid completion

- Required lesson records exist.
- Each required lesson is explicitly completed.
- User has permission to access the course.
- Completion is recorded server-side.
- Duplicate completion does not create duplicate achievements.
- Course is in an eligible publication state.

### No unsupported certification

A GreenOcean completion badge is not:

- A professional qualification.
- A medical certificate.
- A therapist license.
- An accredited educational credential.

Do not present it as any of these.

---

# PART IV — O4: ETHICAL PRIVATE GROWTH REWARDS

# 31. O-25 — Personal Growth Points

Priority: P0

Implement a private point system.

Suggested display name:

**Growth Points / امتیاز رشد**

### Purpose

Recognize educational milestones without pressuring users to disclose personal information.

### Eligible examples

- Completing a real lesson.
- Completing a course.
- Completing an approved learning exercise.
- Demonstrating an explicitly recorded course milestone.

### Example initial values

- Lesson completion: 10 points.
- Eligible educational exercise: 5 points.
- Course completion: 30 points.

These example values are editable policy configuration, not medical or psychological scoring.

### Zero-point actions

The following receive zero growth points:

- Mood check-ins.
- Private journal writing.
- Daily private answers.
- Asking for listener support.
- Completing a listener session.
- Posting.
- Commenting.
- Liking.
- Following.
- Reporting.
- Disclosing sensitive experiences.

Do not tie points to the number of words a user writes.

---

# 32. O-26 — Private server-authoritative reward ledger

Priority: CRITICAL P0

Reward issuance must be server-authoritative.

### Requirements

- Point events are created by validated educational completion events.
- Clients cannot submit arbitrary point amounts.
- Points are not awarded twice for retries.
- Each reward references a real eligible activity.
- Rule version is recorded.
- Duplicate submissions are idempotent.
- Reconciliation is possible.
- Correction/reversal is auditable.
- Balance derives from valid ledger events.
- Account owner is always server-derived.

### Suggested ledger fields

- Event ID.
- Owner ID.
- Source activity type.
- Source completion ID.
- Reward rule/version.
- Point delta.
- Created timestamp.
- Reversal reference if applicable.

Do not store private journal text in the points ledger.

### Repeat attempts

A lesson must not generate points repeatedly because the user taps "Complete" again.

Define reward eligibility across course revisions.

Avoid farming through repeated course resets.

---

# 33. O-27 — Ethical anti-gaming controls

Priority: P0

Prevent:

- Client-submitted point balances.
- Repeated completion rewards.
- Course-reset farming.
- Duplicate requests.
- Cross-account rewards.
- Stale completion replays.
- Fraudulent completion IDs.
- Reversed activity retaining invalid points.
- Concurrent double issuance.

### Important ethical boundary

Do not solve reward abuse through invasive monitoring of:

- Journal content.
- Mood states.
- Listener conversations.
- Private messages.
- Camera or microphone activity.
- Sensitive browsing behavior.

Use bounded, minimally invasive technical controls.

---

# 34. O-28 — Learning badges

Priority: P1

Introduce simple private badges.

Examples:

- First Lesson.
- First Completed Course.
- Consistent Learning Milestone.
- Exploring New Skills.

### Requirements

- Based on genuine eligible activity.
- Private by default.
- Non-transferable.
- No cash value.
- No competitive leaderboard.
- No claim of clinical improvement.
- No public comparison.

### No streak punishment

Do not remove an earned badge because the user stops opening the app.

Avoid streak systems that induce guilt or compulsive usage.

---

# 35. O-29 — Personal growth dashboard

Priority: P1

Display:

- Total private growth points.
- Completed lessons.
- Completed courses.
- Earned private badges.
- Current educational paths.
- Optional learning milestones.

### Critical wording

Do not call the total:

- Mental-health score.
- Recovery score.
- Psychological quality score.
- Resilience diagnosis.
- Clinical improvement rating.

### Dashboard distinction

Learning progress and personal wellbeing are different.

Mood history may appear in the private wellbeing area but must not be converted into growth points.

---

# 36. O-30 — Cross-feature integration boundaries

Priority: CRITICAL P0

Phase O must integrate safely with GreenOcean's existing features.

### Phase L — Social publishing

Journal entries are not social posts.

Post-linked resources preserve identity and audience.

Social likes do not produce growth points.

### Phase M — Communities

A group OWNER or MODERATOR has no automatic access to private wellbeing data or professional content-publishing permissions.

Hidden community discussions are not exposed to the Growth Library.

### Phase N — Listener support

Listener support sessions remain separate.

No listener receives a user's private journal, mood history or self-reflection answers by default.

Growth points cannot influence queue priority or booking access.

### Phase P — Professional services

Educational author verification is separate from permission to deliver regulated clinical services.

Publishing an article does not grant professional consultation authorization.

### Phase Q — Safety and scaling

Phase O must implement its own essential privacy, review, abuse controls and reliable testing.

Do not defer all safety to Q.

### No automatic recommendation profiling

Do not automatically connect:

Journal → Diagnosis.

Mood → Treatment recommendation.

Post → Psychological assessment.

Course → Clinical qualification.

Learning points → Listener eligibility.

These links are outside approved Phase O scope.

---

# 37. O-31 — Content safety and wellbeing boundaries

Priority: CRITICAL P0

Apply suitable safety policies to educational content.

### Requirements

- Accurate authorship.
- Clear general educational scope.
- Source review.
- No fake clinical credentials.
- No fabricated evidence.
- No misleading medical promises.
- No unsupported cure claims.
- No hidden professional advertising.
- No direct solicitation through post attachments.
- Content-warning support.
- Authorized removal/retirement.
- Review of material revisions.

### Private content

Do not subject private journaling to public-post moderation or publication workflows.

Private writing is not automatically a social contribution.

Handle ordinary input safety and security without silently converting personal narratives into public moderation cases.

### Urgent resources

Maintain access to existing legitimate urgent-help information.

Do not claim private journaling is continuously monitored.

Do not claim the Reflection Companion provides crisis response.

---

# 38. O-32 — Privacy, retention and account rights

Priority: CRITICAL P0

Perform a focused Phase J integration.

Do not rewrite all of Phase J.

### Data categories

- Journal.
- Mood.
- Goals.
- Daily question responses.
- Guided reflections.
- Learning completion.
- Course progress.
- Saved resources.
- Growth points.
- Badge history.
- Editorial submissions.
- Reviewer decisions.

### Requirements

- Purpose-specific processing.
- Current owner access.
- Minimal staff access.
- Appropriate retention.
- Export.
- Correction where applicable.
- Deletion.
- Backup limits.
- Review of consent/notice where relevant.
- No unnecessary secondary use.
- No external AI transfer.

### Cross-account security

A second user must never obtain another user's:

- Journal text.
- Mood records.
- Goal information.
- Daily answers.
- Reflection history.
- Private learning rewards.
- Personal saved resources.

Do not assume hiding UI controls provides authorization.

Enforce access server-side.

---

# 39. O-33 — Notifications and participation autonomy

Priority: P1

All wellbeing reminders are optional.

Examples:

- A new approved lesson is available.
- An educational course has been updated.
- A user-selected learning reminder.

### Rules

- No mandatory daily check-in.
- No shame-based missed-day notifications.
- No public disclosure of learning status.
- No private journal body in notifications.
- No diagnostic notification content.
- No fabricated urgency.
- No excessive reminder frequency.

Reuse existing notification preferences and quiet-hour behavior.

If the applicable private reminder preference or operating permission does not exist, do not enable reminders by default.

---

# 40. O-34 — Localization, accessibility and usability

Priority: P0

Support:

- Persian.
- English.
- RTL.
- Mixed Persian/English text.
- Unicode-safe journal entries.
- Local dates and times.
- Proper text wrapping.
- Accessible forms.
- Screen-reader labels.
- Keyboard navigation.
- Dynamic text scaling.
- Clear save states.
- Offline/error handling.
- Account-switch data isolation.

### Journal experience

A user must not lose a long journal entry through an avoidable UI navigation mistake.

Use safe save/draft handling subject to approved sensitive-storage policy.

### Educational content

Support accessible article and lesson structure:

- Semantic headings.
- Readable paragraphs.
- Clear references.
- Appropriate contrast.
- Accessible resource links.
- Clear completion indicators.

### Mobile verification

Test on real Android/iOS devices where available.

Do not claim actual VoiceOver/TalkBack validation from JavaScript export tests.

---

# 41. O-35 — Security and abuse threat model

Priority: CRITICAL P0

Threat-model all four milestones.

### Private data attacks

- Access another user's journal.
- Enumerate private journal IDs.
- Read another user's mood history.
- Edit another user's goals.
- Forge daily question ownership.
- Retrieve old account data after switching users.
- Recover deleted private data from active cache.
- Access private draft text through logging.
- Inject malicious markup.
- Submit oversized Unicode payloads.

### Content governance attacks

- Ordinary member creates official expert content.
- Group moderator impersonates a professional.
- Unverified professional self-approves.
- Reviewer approves a stale article revision.
- Author edits published approved content without review.
- Expired credentials remain effective.
- Unauthorized article pinning.
- Retired resource remains promoted.
- Forged editorial review date.
- Hidden private post linked into public resource indexing.
- Anonymous post author leaked through attachment metadata.
- External media link used for unsafe redirection.

### Course/reward attacks

- Client forges progress.
- Client submits arbitrary points.
- Repeat-completion farming.
- Duplicate course completion.
- Concurrent reward issuance.
- Cross-account reward access.
- Replay of stale lesson completion.
- Unauthorized course content editing.
- Unpublished lessons counted as completed.
- Retired course content receiving new rewards contrary to policy.

### Required protections

- Server-derived ownership.
- Validated DTOs.
- Authorization at every object access.
- Transactional mutations.
- Database uniqueness constraints.
- Optimistic version checks where necessary.
- Idempotency.
- Rate limits.
- Purpose-limited audit logging.
- Accurate revision references.
- Safe cache invalidation.
- No privileged role inference.

Use real PostgreSQL for concurrency-sensitive tests.

---

# 42. O-36 — Final Phase O integration

Priority: CRITICAL P0

Phase O must end with functioning, tested user journeys rather than isolated feature demos.

### Journey A — Private reflection

User opens Wellbeing
→ creates journal entry
→ edits it
→ searches personal history
→ verifies it is inaccessible to another account
→ deletes it
→ verifies active views and archive behavior.

### Journey B — Mood history

User records moods on multiple dates
→ views calendar
→ filters records
→ edits/deletes an entry
→ verifies descriptive trends
→ receives no diagnostic claim.

### Journey C — Gentle goals

User creates a personal goal
→ adds steps
→ completes actual steps
→ sees progress derived correctly
→ pauses/resumes
→ completes the goal.

### Journey D — Daily questions

User opens daily prompts
→ answers one question
→ skips the others
→ explicitly saves
→ returns later
→ edits own response
→ confirms privacy
→ receives no penalty for missing a day.

### Journey E — Growth Library

User searches a book
→ reads its description
→ checks source
→ saves it
→ opens an authorized external destination
→ removes it from saved items.

Repeat for a film/music resource as appropriate.

### Journey F — Expert content

Verified authorized contributor creates an article
→ submits it
→ an appropriate independent reviewer checks the exact revision
→ publication is authorized
→ member reads it
→ edited content triggers required review again.

### Journey G — Linked resource on anonymous post

Author creates an anonymous post
→ explicitly permits related educational resources
→ authorized reviewer proposes a permitted published resource
→ association is approved
→ ordinary readers see For Further Reading
→ no reader learns the anonymous author identity
→ author disables the attachment.

Repeat for a named post and ensure no unauthorized group access.

### Journey H — Educational course

User selects a genuine course
→ reads real lessons
→ completes eligible activities
→ server records real progress
→ completes course
→ sees a private completion badge.

### Journey I — Ethical points

User completes one eligible lesson
→ receives one valid reward
→ retries the completion request
→ receives no duplicate points
→ tries forging a balance
→ request fails
→ private point total stays correct.

### Journey J — Rights and account deletion

User exports own private wellbeing and learning data
→ verifies correct ownership
→ requests erasure through approved account lifecycle
→ future access and rewards are revoked
→ account cache is cleared
→ retained exceptions follow documented policy.

---

# 43. DATABASE AND MIGRATION STRATEGY

Use additive Flyway migrations.

Determine the actual current migration version from the latest validated source.

Do not edit existing applied migration files.

### Possible additions

Use only where needed:

- Journal revision metadata.
- Journal tags.
- Mood history indexes.
- Goal steps.
- Goal lifecycle metadata.
- Daily question bank.
- Daily question versions.
- Private daily responses.
- Educational resource catalog extensions.
- Editorial contributor permissions.
- Educational content revisions.
- Editorial review decisions.
- Post-resource attachment records.
- Attachment author permission.
- Course modules.
- Course lessons.
- Lesson completion.
- Educational exercise completion.
- Growth point ledger.
- Private badges.

### Preserve historical data

Do not erase or reclassify:

- Journal entries.
- Mood check-ins.
- Goals.
- Reflection results.
- Existing growth paths.
- Existing educational articles.
- Saved media.
- Existing post visibility.
- Anonymous authorship.
- Account data-rights history.

### Migration quality

Test:

- Fresh database.
- Populated database.
- Existing journal records.
- Existing progress percentages.
- Existing published courses.
- Existing unreviewed educational seed content.
- Existing saved resources.
- Account export.
- Account deletion.
- Migration failure and retry.
- Index uniqueness and transactional integrity.

Do not convert historical fake-percentage progress into verified lesson completions.

Preserve it as legacy progress metadata where necessary and distinguish it from newly verified completion.

---

# 44. API CONTRACT

Reuse existing `/api/v1/wellbeing`, article and catalog API conventions.

Do not create duplicate controllers unnecessarily.

Illustrative additions:

## Private journal

- `GET /api/v1/wellbeing/journal`
- `GET /api/v1/wellbeing/journal/{id}`
- `PATCH /api/v1/wellbeing/journal/{id}`
- Existing create/delete endpoints.

## Mood

- `GET /api/v1/wellbeing/check-ins`
- `PATCH /api/v1/wellbeing/check-ins/{id}`
- `DELETE /api/v1/wellbeing/check-ins/{id}`
- `GET /api/v1/wellbeing/check-ins/summary`

## Goals

- Existing create/update endpoints.
- Goal-step endpoints.
- Goal pause/resume/archive endpoints.
- Owner deletion.

## Daily reflection

- `GET /api/v1/wellbeing/daily-reflection`
- `POST /api/v1/wellbeing/daily-reflection/responses`
- `PATCH /api/v1/wellbeing/daily-reflection/responses/{id}`
- `DELETE /api/v1/wellbeing/daily-reflection/responses/{id}`

## Growth Library

- `GET /api/v1/growth-library/resources`
- `GET /api/v1/growth-library/resources/{id}`
- Owner-scoped save/unsave endpoints.

## Educational content

- Contributor draft submission.
- Independent review.
- Publication.
- Revision.
- Retirement.

Follow existing admin conventions and recent-MFA rules.

## Post resources

- Author attachment-preference endpoint.
- Authorized resource proposal endpoint.
- Editorial approval endpoint.
- Authorized reader listing.
- Author removal/revocation endpoint.

## Courses

- `GET /api/v1/wellbeing/courses`
- `GET /api/v1/wellbeing/courses/{id}`
- `GET /api/v1/wellbeing/courses/{id}/lessons`
- `POST /api/v1/wellbeing/lessons/{id}/complete`
- `GET /api/v1/wellbeing/courses/me/progress`

## Growth points

- `GET /api/v1/wellbeing/growth-points/me`
- `GET /api/v1/wellbeing/badges/me`

Do not expose a general client endpoint for awarding arbitrary growth points.

All examples are illustrative.

Adapt to actual existing API conventions.

### Universal API rules

- Server-derived actor identity.
- Object-level authorization.
- Validated input.
- Bounded pagination.
- Consistent error codes.
- Revision control.
- Idempotent retries.
- Database-authoritative completion.
- No sensitive data leakage.
- No fabricated publication status.

---

# 45. FOUR-PERSONA VALIDATION

Use GreenOcean's established test methodology.

## Happy User

- Opens private wellbeing.
- Creates a journal entry.
- Records mood.
- Sets a gentle goal.
- Answers daily prompts.
- Reads approved content.
- Saves a recommended book.
- Completes a real lesson.
- Earns private points.
- Returns later and sees correct progress.

## Creative User

- Uses Persian and English.
- Uses long Unicode journal entries.
- Changes timezone.
- Answers only one of three daily questions.
- Skips several days.
- Pauses a goal.
- Uses mixed-script resource search.
- Opens a content-warning resource.
- Completes lessons out of order where permitted.
- Reviews an article whose version changed.
- Uses the app on multiple devices.
- Edits private drafts after network interruption.

## Confused User

- Confuses private journal with social post.
- Expects a missed day to reduce points.
- Misunderstands learning points as a mental-health score.
- Tries to complete a lesson without reading content.
- Cannot find a saved article.
- Does not understand why a clinical article is pending review.
- Mistakes a Growth Library link for a GreenOcean streaming player.
- Expects three questions to diagnose personality.
- Tries to attach a professional resource to their own post without permission.

Provide clear, calm explanations.

## Malicious User

- Enumerates journal IDs.
- Accesses another user's mood history.
- Changes another user's goal.
- Forges article credentials.
- Self-approves content.
- Reuses an expired professional credential.
- Edits an approved article after review.
- Pins unauthorized content.
- Attempts to expose anonymous post authors.
- Attaches public resources to hidden group narratives.
- Submits arbitrary growth points.
- Replays lesson completions.
- Double-awards under concurrency.
- Alters client timestamps.
- Attempts cross-account reward export.
- Retrieves private text from logs or cache.
- Uses a stale session after logout.

Use synthetic isolated accounts.

Do not run invasive tests against real users.

---

# 46. TEST EXECUTION

Run real validations after each milestone.

## O1

- Owner-scoped journal CRUD.
- Mood history.
- Goal-step progress.
- Daily question versioning.
- Privacy tests.
- Account-switch cache tests.
- Archive/erasure tests.
- Frontend wellbeing journeys.

## O2

- Editorial permissions.
- Verified professional credentials.
- Review workflow.
- Exact-revision approval.
- Content retirement.
- Resource saves.
- Safe external links.
- Anonymous post attachment.
- Restricted community attachment.
- Post-author consent.
- Real browser/API tests.

## O3

- Real lesson content.
- Course hierarchy.
- Lesson completion.
- Progress derivation.
- Course version handling.
- Prompt versioning.
- Adult eligibility.
- Accessible learning UI.

## O4

- Reward ledger.
- Duplicate prevention.
- Concurrency.
- Private badges.
- Rights integration.
- Full-stack journeys.
- Four personas.
- Final regression.

### Run

- Backend unit tests.
- Real PostgreSQL integration tests.
- Migration tests.
- Authorization tests.
- Concurrency tests.
- React Native logic tests.
- Component/browser tests.
- TypeScript validation.
- Lint.
- Web build.
- Android/iOS JS exports.
- True frontend/backend/PostgreSQL journeys.
- Native-device tests where actually performed.

### Evidence rules

Report exact:

- Commit SHA.
- Commands.
- Environment.
- Passed.
- Failed.
- Skipped.
- Not executed.

Do not fabricate CI evidence.

A mocked frontend test is not a real full-stack journey.

Do not label a Web export as native accessibility validation.

---

# 47. CROSS-PHASE REGRESSION

Preserve:

- A–F authentication, account access and security.
- G/H profile and content privacy.
- I listener qualification and live sessions.
- J consent, privacy and user rights.
- K final integration invariants.
- L anonymous publishing and social moderation.
- M public/private/hidden group access.
- N schedules, queues, booking and live handoff.

Especially ensure:

- No journal data enters group feeds.
- No private daily answers enter listener conversations.
- No educational editor gains group membership automatically.
- No resource attachment reveals anonymous authors.
- No growth points grant listener queue priority.
- No course activity bypasses account restrictions.
- No professional article permission becomes clinical-service authorization.

---

# 48. REQUIRED DOCUMENTATION

Create or update:

- `docs/phase-o/requirements-matrix.md`
- `docs/product/private-wellbeing-contract.md`
- `docs/product/growth-library-contract.md`
- `docs/product/educational-content-governance.md`
- `docs/product/for-further-reading-contract.md`
- `docs/product/ethical-growth-rewards.md`
- `docs/phase-o/journal-data-lifecycle.md`
- `docs/phase-o/mood-and-goal-design.md`
- `docs/phase-o/daily-question-versioning.md`
- `docs/phase-o/contributor-permissions.md`
- `docs/phase-o/editorial-review-state-machine.md`
- `docs/phase-o/resource-attachment-privacy.md`
- `docs/phase-o/course-and-lesson-model.md`
- `docs/phase-o/learning-completion-contract.md`
- `docs/phase-o/reward-ledger-and-idempotency.md`
- `docs/phase-o/sensitive-data-threat-model.md`
- `docs/phase-o/account-rights-integration.md`
- `docs/phase-o/owner-decisions.md`
- `docs/phase-o/operational-readiness.md`
- `docs/phase-o/test-evidence.md`
- `docs/phase-o/delivery-report.md`
- `docs/phase-o/phase-p-handoff.md`

Reuse authoritative earlier contracts rather than duplicating them.

---

# 49. REQUIRED PULL REQUESTS

## PR O1 — Private Wellbeing and Reflection

Deliver:

- Private journal improvements.
- Mood history and descriptive trends.
- Gentle goals and real milestones.
- Daily optional questions.
- Guided reflection.
- Privacy and account lifecycle.
- Backend/frontend tests.
- PostgreSQL tests.
- Real-stack journeys.

## PR O2 — Growth Library and Reviewed Educational Content

Deliver:

- Resource library.
- Books, films and music metadata.
- Specialist notes and articles.
- Editorial permissions.
- Independent review.
- Reflection templates.
- For Further Reading under eligible posts.
- Anonymous-author safety.
- Content source verification.
- Privacy/security tests.

## PR O3 — Real Learning Courses

Deliver:

- Course/module/lesson hierarchy.
- Actual reviewed lesson content.
- Educational exercises.
- Lesson completion.
- Genuine course progress.
- Question-bank governance.
- Adult suitability.
- Full-stack learning tests.

## PR O4 — Private Growth Points and Final Integration

Deliver:

- Private reward ledger.
- Controlled eligible rewards.
- Learning badges.
- Anti-abuse.
- Account data rights.
- Security hardening.
- Four-persona integration.
- Cross-phase regression.
- Final Phase O delivery report.

### PR rules

- Separate reviewable changes.
- Correct stacked dependency.
- Real tests.
- Accurate documentation.
- No fabricated credentials.
- No automatic merge.
- No production deployment.
- No unauthorized publication or sensitive-data activation.

---

# 50. DEFINITION OF DONE

Phase O is technically complete only when:

1. The private wellbeing hub is functional.
2. Personal journals support complete owner-managed lifecycle.
3. Journal data cannot leak into social surfaces.
4. Mood history is accurate and owner-scoped.
5. Mood charts contain no unsupported diagnostic inference.
6. Goals support meaningful real steps.
7. Arbitrary progress increments are removed from new goal workflows.
8. Three optional daily questions are available.
9. Answers require explicit saving.
10. Daily questions support version-safe history.
11. The reflection companion remains non-clinical.
12. Existing assessment language is scientifically honest.
13. Growth Library supports curated resources.
14. Books, films and music use authorized external references.
15. No unauthorized copyrighted media distribution is created.
16. Only authorized contributors submit official educational content.
17. Professional qualifications are independently verified before specialist attribution.
18. Current content revisions receive appropriate review.
19. Unverified historical credential claims are not presented as genuine.
20. Users can access reviewed articles and educational notes.
21. Reusable reflection templates are distinct from private journal entries.
22. Eligible posts can show For Further Reading resources.
23. Original post author consent is respected.
24. Anonymous post authors remain anonymous.
25. Restricted posts never gain broader visibility through attachments.
26. Courses contain actual lessons.
27. Course progress reflects real completion events.
28. Unpublished content cannot earn completion credit.
29. Adult eligibility is enforced.
30. No unapproved child-specific service is activated.
31. Growth points are private.
32. Points are server-authoritative.
33. Duplicate rewards are prevented.
34. Sensitive disclosure receives no rewards.
35. Social popularity receives no rewards.
36. Listener access/priority is never based on growth points.
37. Learning badges cannot be mistaken for professional credentials.
38. Data export and erasure cover all new categories.
39. Cross-account privacy tests pass.
40. Editorial role escalation tests pass.
41. PostgreSQL concurrency tests pass.
42. Real full-stack journeys pass.
43. Persian, English and RTL are verified.
44. All four persona test categories have evidence.
45. Existing A–N regressions remain intact.
46. All four milestone PRs are delivered.
47. Documentation includes actual evidence and residual risks.
48. Production release remains independently gated.

---

# 51. FINAL DELIVERY REPORT

Provide:

## A. Verified repository baseline

Actual A–N dependency and tested source.

## B. O1 implementation

Private journal, mood, goals and daily reflection.

## C. O2 implementation

Growth Library, content governance and post attachments.

## D. O3 implementation

Curriculum, lessons, educational exercises and genuine progress.

## E. O4 implementation

Growth points, badges, anti-gaming and integration.

## F. Requirement matrix

O-01 through O-36 with evidence.

## G. Architecture

Services, database structures, APIs, state machines and authorization.

## H. Privacy

Sensitive data, anonymous authorship, journal isolation, retention and account rights.

## I. Educational governance

Actual editorial review, source verification and professional qualification boundaries.

## J. Security

Threat model, access controls, abuse prevention and unresolved risks.

## K. Test evidence

Exact commands, commit SHAs, counts and outcomes.

## L. GitHub

Four PR URLs, branches, dependencies and statuses.

## M. External blockers

Unapproved legal, content, expert-review, native or operational dependencies.

## N. Phase P handoff

Only the additional professional-service features legitimately belonging to Phase P.

Do not unnecessarily rebuild Phase P services during Phase O.

---

# FINAL EXECUTION DIRECTIVE

Implement the complete GreenOcean Phase O through four ordered, independently reviewable milestones:

**O1 — Private Self-Care and Reflection**

**O2 — Growth Library and Reviewed Specialist Content**

**O3 — Educational Courses and Self-Discovery**

**O4 — Ethical Private Growth Rewards and Final Integration**

Begin from the latest actually validated Phase N3 source.

Reuse existing wellbeing, journal, goals, reflection, article, media, professional verification, user-rights and social privacy infrastructure.

Do not rebuild already functioning systems.

Complete personal journaling, mood history, gentle goals and three optional daily questions.

Build a curated Growth Library with books, films, music, educational articles, short specialist notes and reviewed reflection templates.

Only appropriately authorized contributors may submit official content.

Do not invent professional credentials or editorial approvals.

Allow approved educational resources to be attached to eligible named or anonymous posts only under explicit author permission and existing post-access restrictions.

Preserve anonymous author identity.

Implement real courses containing genuine educational lessons and exercises.

Replace arbitrary progress increments with server-derived lesson and milestone completion.

Introduce private Growth Points and learning badges based only on approved educational achievements.

Never reward mental-health disclosure, social popularity, listener usage or compulsive engagement.

Do not introduce unsupported clinical claims, unauthorized media streaming, external AI analysis or services for minors.

Preserve privacy, account rights, security and validated A–N invariants.

Execute real PostgreSQL, frontend, backend, concurrency, privacy and full-stack tests.

Deliver four reviewable Pull Requests, complete documentation and a comprehensive Phase O report.

Do not automatically merge, deploy, publish unreviewed content or activate unapproved production processing.

**Expected outcome: GreenOcean offers a meaningful, secure and non-clinical environment for private reflection, personal goal-setting, reviewed educational discovery, structured learning and ethical personal growth — without exposing sensitive information, creating misleading medical claims or turning self-care into a competitive activity.**
