# GreenOcean Implementation Status

Last verified: 2026-09-13

This document is the final implementation inventory for the current English/LTR milestone. It distinguishes
finished application work from integrations that require an external provider, production infrastructure,
legal/clinical review, or the intentionally deferred Persian milestone.

## Outcome

- The frontend is API-backed; runtime mock/preview data has been removed.
- Account state, search history, onboarding choices, training modules, urgent resources, and reusable product options are persisted in PostgreSQL and read through the API.
- Historical development identities are removed by migration; curated categories, channels, policies, media, learning paths, and crisis resources remain as database-managed reference content.
- The backend can run locally without access to the original PostgreSQL computer.
- The implemented frontend and backend contracts are connected.
- Core member, professional, listener, community, moderation, and admin workflows are represented in both UI and API.
- English/LTR is the only active locale for this milestone.
- No known compile, lint, migration, or automated-test failure remains.

## Exact frontend route map

Expo Router route groups do not appear in the public URL. Parameters in square brackets are dynamic.

### Public, authentication, and information

| Address | Purpose |
| --- | --- |
| `/` | Restores the session and routes to the app or welcome page |
| `/welcome` | Product promise, trust signals, safety note, and primary calls to action |
| `/login` | Account sign-in and password-recovery entry |
| `/register` | Account creation with identity and location fields |
| `/onboarding` | Initial interests, support goals, privacy, and safety setup |
| `/urgent-help` | Public, pre-login emergency/crisis directory backed by verified database records |
| `/info/how-it-works` | Explanation of community, listener, professional, and wellbeing paths |
| `/info/about` | Product purpose, principles, and clinical boundaries |
| `/info/faq` | Privacy, verification, moderation, listeners, and urgent-help answers |
| `/info/privacy` | Published privacy policy with local fallback copy |
| `/info/terms` | Published terms with local fallback copy |
| `/info/guidelines` | Published community guidelines with local fallback copy |
| `/info/cookies` | Essential and optional storage choices |
| `/info/accessibility` | Accessibility baseline and pre-production checklist |
| `/info/help` | Help topics and routes into account, safety, professional, and listener support |
| `/info/contact` | Persisted support-request form |
| `/info/forgot-password` | Password-reset request |
| `/info/reset-password?token=...` | Password-reset confirmation |
| `/info/verify-email` | Email-code request and verification |

### Core signed-in product

| Address | Purpose |
| --- | --- |
| `/` after session restoration | Categorized feed with For You, Following, and Expert Answers tabs |
| `/create` | Create an experience, support question, or reflection with topic, mood, warning, visibility, and anonymous mode |
| `/post/[id]` | Post detail, comments, replies, likes, report route, and professional response |
| `/search` | Search people/posts and discover categories, professionals, communities, articles, and media |
| `/notifications` | Durable activity inbox, unread state, notices, and mark-all-read |
| `/profile` | Own profile, activity counts, saved/about links, settings, professional variant, and pinned article |
| `/user/[username]` | Privacy-aware public member profile, posts, follow/request, message, block, and report |
| `/communities` | Community discovery and membership controls |
| `/community/[slug]` | Community details, rules, events, feed, join state, and moderator controls |
| `/messages` | Conversation inbox, message requests, search, and new-conversation entry |
| `/chat/[id]` | Persisted private conversation with polling, safety actions, and sending |
| `/channels` | Moderated support channels with topic and membership state |
| `/support` | Chooses between trained listeners, peer groups, professionals, and urgent help |
| `/professionals` | Verified professional directory and promoted/featured placement |
| `/articles` | Professional knowledge library |
| `/article/[id]` | Article detail, evidence metadata, helpful action, and save state |
| `/article-editor` | Professional article authoring and pinning |
| `/media` | Psychology-related films, series, podcasts, and books with save state |
| `/safety-settings` | Sensitive-content and communication controls |
| `/+not-found` | Branded recovery page for unsupported routes |

### Account, privacy, community, and wellbeing tools

All of these use `/tools/[tool]`:

| Address | Purpose |
| --- | --- |
| `/tools/profile-edit` | Edit display name, username, bio, location, and private-profile state |
| `/tools/saved` | Unified saved posts, articles, and media |
| `/tools/followers` | Followers, following, incoming requests, outgoing requests, approve/decline/cancel/unfollow |
| `/tools/notification-settings` | Message, reply, follow, group, article, and quiet-hours preferences |
| `/tools/account-security` | Password change, session/export links, and password-confirmed account deletion |
| `/tools/privacy-data` | Private-profile switch, blocks, account-synchronized search history, and archive access |
| `/tools/blocked` | Blocked-account inventory and unblock |
| `/tools/safety-center` | Safety controls, reports, policy, support, and urgent routes |
| `/tools/my-reports` | Personal moderation-report status and outcomes |
| `/tools/report?targetType=...&targetId=...` | Confidential report form for an exact target |
| `/tools/urgent-help` | Country-aware emergency/crisis routing and clinical disclaimer |
| `/tools/message-requests` | Controls who may initiate a private conversation |
| `/tools/new-message` | Starts a conversation with a connection or listener |
| `/tools/community-create` | Creates a moderated public or private community |
| `/tools/community-rules?slug=...` | Owner/moderator rule editor |
| `/tools/community-members?slug=...` | Join requests, member roles, moderation, and removal |
| `/tools/community-events?slug=...` | Create/remove events and manage attendance |
| `/tools/wellbeing` | Private wellbeing dashboard and tool launcher |
| `/tools/check-ins` | Persisted mood/energy check-ins and history |
| `/tools/goals` | Persisted gentle goals and completion state |
| `/tools/journal` | Persisted private journal |
| `/tools/assessments` | Non-diagnostic wellbeing reflection with explicit safety boundary |
| `/tools/growth-paths` | Evidence-informed learning paths and progress |
| `/tools/companion` | Guided private reflection, explicitly not a therapist |
| `/tools/search-history` | Account-synchronized search history review/clear |
| `/tools/session-management` | Active session inventory and remote revocation |
| `/tools/data-export` | Downloadable account archive |
| `/tools/consultations` | Member appointment requests and cancellation |

### Professional and listener workflows

| Address | Purpose |
| --- | --- |
| `/professional/[id]/contact` | Start a professional conversation |
| `/professional/[id]/book` | Select a real published slot and request an appointment |
| `/professional/[id]/reviews` | Read and submit a moderated professional review |
| `/professional/apply/start` | Professional application and credential-file upload |
| `/professional/me/verification` | Current verification status and reviewer note |
| `/professional/me/consultations` | Publish/cancel availability and confirm, decline, complete, or cancel requests |
| `/listener/match` | Topic-based match and conversation creation |
| `/listener/apply` | Volunteer application and code-of-conduct acceptance |
| `/listener/training` | Persisted module progress and activation boundary |
| `/listener/profile` | Listener scope, experience, topics, languages, and safety score |

### Administration

| Address | Purpose |
| --- | --- |
| `/admin` | Role-gated operational overview and moderation queues |
| `/admin-tools/report-detail?id=...` | Report evidence, decision, note, status, and audit workflow |
| `/admin-tools/verification-detail?id=...` | Credential checklist, document scan state, and decision |
| `/admin-tools/appeals` | Review, uphold, or overturn moderation appeals |
| `/admin-tools/campaigns` | Create, start, pause, and measure labelled professional promotion |
| `/admin-tools/communities` | Community health metrics and archive/reactivate action |
| `/admin-tools/listeners` | Listener application, training, approval, and safeguarding queue |
| `/admin-tools/notifications` | Audience/category-aware in-app broadcasts and delivery history |
| `/admin-tools/support` | Support-request assignment/resolution and email-delivery state |
| `/admin-tools/exports` | Members, reports, professionals, and activity CSV snapshots/downloads |
| `/admin-tools/policies` | Versioned policy/taxonomy publishing |

The static Web export currently emits 62 route entries because Expo also renders route-group aliases.
The table above lists the canonical product URLs and every supported dynamic view.

## Completed gap matrix

### Product foundation and UI/UX

1. **Completed — English-first milestone:** runtime copy is English and the language provider is fixed to `en`.
2. **Completed — LTR:** text input direction, alignment, navigation, and current locale are LTR.
3. **Completed — design tokens:** ocean palette, semantic accents, spacing, radius, typography, max widths, and cross-platform shadows are centralized.
4. **Completed — shared components:** buttons, fields, icons, avatars, product headers, cards, notices, pills, metrics, sections, and empty states are reusable.
5. **Completed — responsive Web layout:** major pages use constrained content widths and wrapping grids.
6. **Completed — mobile navigation:** primary feed, explore, create, activity, and profile tabs are present.
7. **Completed — branded fallback:** unsupported URLs use a deliberate GreenOcean 404 experience.
8. **Completed — empty/loading/error states:** API-backed screens do not silently fall back to fake content.
9. **Completed — no mock runtime:** old mock data, mock discovery, mock operations, and data-mode modules are removed.
10. **Completed — safety language:** peer, listener, wellbeing, and professional boundaries are visible at decision points.

### Welcome, auth, onboarding, and account

11. **Completed — welcome page:** promise, value cards, trust cues, safety note, and product links.
12. **Completed — product information:** how-it-works, about, FAQ, policies, accessibility, cookies, and help.
13. **Completed — registration:** email, password, username, display name, birth year, country, and city.
14. **Completed — onboarding:** interests/support preferences and intended member/professional role are persisted without diagnostic inference.
15. **Completed — login/session restore:** secure native storage and Web fallback.
16. **Completed — token refresh:** refresh sessions rotate and are stored hashed.
17. **Completed — immediate revocation:** access JWTs are rejected after logout, logout-all, password change, or account deletion.
18. **Completed — session management:** users can list and revoke active sessions.
19. **Completed — account recovery:** request/confirm password reset and email verification contracts/UI.
20. **Completed — account archive:** user-scoped JSON export includes profile, preferences, wellbeing, reports, connections, saved activity, and search history.
21. **Completed — account deletion:** password confirmation, session revocation, private-data removal, content handling, and identity anonymization.

### Social feed and content

22. **Completed — categorized feed:** categories are grouped as emotions, conditions, and lived experiences.
23. **Completed — explicit post type:** lived experience, support question, or reflection.
24. **Completed — author-selected category:** category selection is required before publishing.
25. **Completed — feed tabs:** For You, Following, and Expert Answers.
26. **Completed — anonymous posts:** peer-facing identity is hidden while safety traceability is retained.
27. **Completed — visibility:** public, followers, and community visibility is enforced by backend queries.
28. **Completed — content warnings:** authors can add sensitive-content context.
29. **Completed — engagement:** post/comment likes, replies, bookmarks, counts, and idempotent notifications.
30. **Completed — post detail:** full conversation and professional response are connected to API data.
31. **Completed — deletion rules:** soft-deleted or unauthorized posts do not leak into feed/search/profile reads.
32. **Completed — search:** user and post search respects privacy, blocks, and visibility; submitted terms are de-duplicated and retained per account.

### Profiles, privacy, and notifications

33. **Completed — own profile:** activity, counts, settings, saved content, and professional variant.
34. **Completed — public profile:** member details, visible posts, follow state, block/report/message routes.
35. **Completed — private profiles:** follows become approval requests and activity is hidden until accepted.
36. **Completed — request management:** incoming/outgoing lists plus accept, decline, and cancel.
37. **Completed — blocking:** blocked accounts cannot follow or access disallowed interactions.
38. **Completed — professional profile detail:** title, specialization, workplace, clinic, location, country, language, gender, education, modes, rating, and score.
39. **Completed — professional pinned content:** a professional can publish and pin a scientific article on the profile.
40. **Completed — notification inbox:** social/professional/system events, unread count, read, read-all, and a real unread-only feed indicator.
41. **Completed — notification preferences:** category switches and quiet-hours state persist.

### Knowledge, media, professionals, and promotion

42. **Completed — article library:** professional authorship, evidence level, read time, detail, helpful, and saved state.
43. **Completed — article studio:** verified professionals can publish and choose pinned content.
44. **Completed — psychology media:** movie, series, and documentary recommendations with topic and save state.
45. **Completed — professional directory:** filtering/discovery data includes fit, specialty, language, location, verification, rating, and promotion.
46. **Completed — professional reviews:** one review per member/professional relationship with moderation status.
47. **Completed — credential application:** detailed professional, location, workplace, education, language, gender, license, and consultation-mode fields.
48. **Completed — document upload:** PDF/JPEG/PNG, 10 MB limit, content-signature verification, SHA-256, private storage key, and scan status.
49. **Completed — verification operations:** identity/education/license/sanctions/scope checklist and review decisions.
50. **Completed — availability:** professionals publish future 15-minute-to-8-hour slots.
51. **Completed — overlap protection:** per-professional transaction lock prevents concurrent overlapping slot creation.
52. **Completed — double-booking protection:** row lock and available-status transition allow only one appointment per slot.
53. **Completed — consultation lifecycle:** request, confirm, decline, complete, member cancellation, and safe slot release.
54. **Completed — promotion banner:** active verified-professional campaigns appear as clearly labelled featured placement.
55. **Completed — campaign operations:** scheduling, start/pause, impression/click fields, and verification eligibility.
56. **Completed — score integrity:** promotion is kept separate from the professional trust/rating presentation.

### Communities, channels, chat, and listener support

57. **Completed — community discovery/creation:** public/private type, membership, description, and topic context.
58. **Completed — private join requests:** request, cancel, owner/moderator inventory, approve, and decline.
59. **Completed — member roles:** owner/moderator/member roles and protected management actions.
60. **Completed — rules:** persisted community rules with manager editing.
61. **Completed — events:** create/remove event and attend/leave event.
62. **Completed — community feed:** membership and visibility rules enforced server-side.
63. **Completed — member moderation:** role updates and removal.
64. **Completed — support channels:** topic-based membership and discovery.
65. **Completed — conversation inbox:** user/professional conversations and unread metadata.
66. **Completed — direct chat:** persisted messages, access checks, send, and five-second foreground polling.
67. **Completed — message controls:** request settings and block/safety routes.
68. **Completed — listener discovery:** topic/language/availability profile data.
69. **Completed — listener matching:** creates a private support conversation.
70. **Completed — listener application:** motivation, experience, availability, and boundary agreement.
71. **Completed — listener academy:** five database-managed modules, persisted completion status, and supervisor-approval boundary.
72. **Completed — listener administration:** approve/reject and activation workflow.

### Wellbeing and safety

73. **Completed — mood check-ins:** private persisted records and trend-ready history.
74. **Completed — gentle goals:** create, list, and completion state.
75. **Completed — private journal:** persisted personal reflections.
76. **Completed — wellbeing reflection:** explicit non-diagnostic score and support routing.
77. **Completed — guided growth:** persisted path enrollment/progress.
78. **Completed — reflection companion:** guided writing with no therapist/diagnosis claim.
79. **Completed — reporting:** exact target, reason, detail, personal status history, and confidential handling.
80. **Completed — appeals:** member submission/history plus separate admin decision workflow.
81. **Completed — urgent help:** prominent emergency/crisis boundary, source/verification metadata, and a public pre-login route.
82. **Completed — support inbox:** persisted account/accessibility/moderation requests.
83. **Completed — policies:** public versioned privacy, terms, guidelines, and cookie content.
84. **Completed — safety settings:** sensitive content, muted terms, and contact preferences.

### Administration, reporting, and backend hardening

85. **Completed — role enforcement:** ADMIN-only operational data/actions and moderator-scoped queues.
86. **Completed — correct denial behavior:** authorization failures return HTTP 403, not HTTP 500.
87. **Completed — dashboard:** queue counts, reports, verification, reviews, users, and audit activity.
88. **Completed — report decisions:** approve, review/escalate, remove, notes, and audit events.
89. **Completed — user operations:** activate, suspend, or ban with a decision note.
90. **Completed — community operations:** health metrics and archive/reactivate.
91. **Completed — broadcasts:** safety/service/community/editorial category and audience targeting.
92. **Completed — support/email visibility:** request state and outbox delivery state are available to admins.
93. **Completed — operational exports:** members, reports, professionals, and activity allowlists.
94. **Completed — CSV privacy:** no email/private-message export, formula neutralization, expiry, no-store download, and download audit.
95. **Completed — migration completeness:** 31 migrations apply cleanly to a new embedded PostgreSQL instance.
96. **Completed — database-independent local run:** one PowerShell command starts temporary PostgreSQL and the API.
97. **Completed — structured errors:** validation, conflict, not-found, authentication, and authorization responses.
98. **Completed — test coverage:** auth, revocation, schema, content/social, private community, private profile, account deletion, availability, file signatures, product options, onboarding, search history, fixture removal, and urgent resources.
99. **Completed — dependency alignment:** Expo SDK 57 reports every direct dependency up to date.
100. **Completed — build verification:** TypeScript, ESLint, and a 62-route static Web export succeed.
101. **Completed — centralized product options:** 66 active records across category groups, post types/moods, journals, support, listener, reports, goals, assessments, media, professional identity, gender, and consultation modes are read from PostgreSQL.
102. **Completed — durable registration intent:** member/professional intent is accepted during registration, stored in preferences, and used after onboarding.
103. **Completed — durable onboarding gate:** completion state is stored per account and authenticated navigation sends unfinished accounts back to onboarding.
104. **Completed — durable search history:** recent searches are case-insensitively de-duplicated, capped at 20, clearable, and included in the account archive.
105. **Completed — development-fixture cleanup:** old `@greenocean.invalid` accounts and their posts, messages, reports, professional records, and operational activity are removed by an ordered migration.
106. **Completed — curated-reference separation:** categories, support channels, media guidance, growth paths, policies, option catalogs, training modules, and urgent resources stay in database tables rather than runtime source arrays.
107. **Completed — persistent admin audit trail:** report detail renders server-side database audit events instead of temporary in-memory action messages.
108. **Completed — public safety access:** urgent-help data is available without authentication and is linked directly from Welcome.
109. **Completed — honest trust visuals:** decorative fake member initials and fabricated availability/coming-event claims were removed from the UI.
110. **Completed — React state cleanup:** onboarding and assessment initialization no longer performs synchronous state cascades inside effects.

## Remaining items that cannot be completed only in this repository

These are not hidden UI/backend omissions. The in-product workflows and persistence boundaries exist, but completing
them requires a provider account, production environment, licensed/human review, or the deferred language decision.

| External item | Repository state | What is still required |
| --- | --- | --- |
| Transactional email | Reset/verification/support tokens and email outbox are implemented | Select SMTP/API provider, credentials, sender domain, templates, retries, bounces, and delivery worker |
| Credential object storage | Upload validation, hashing, metadata, local private storage, and scan state are implemented | Encrypted S3-compatible bucket, private IAM, retention policy, signed internal access, malware scanner webhook |
| Push notifications | Durable notifications, preferences, unread/read state, and polling exist | APNs/FCM credentials, device-token registration, consent UX, delivery worker, expiry and retry policy |
| Real-time sockets | Chat and notification polling work | Optional WebSocket/SSE gateway and presence scale-out if polling is replaced |
| Payments | Free/administrative appointment and promotion workflows exist | Merchant account, pricing/refunds/tax policy, payment gateway, webhook verification, reconciliation |
| Production deployment | Environment-driven secrets and health endpoint exist | Hosting, PostgreSQL, DNS, TLS, backup/restore drill, monitoring, alerts, log aggregation, rate-limit edge |
| Country crisis resources | Seven active records include official source URLs and verification dates for US, Canada, UK, and a global directory | Add and clinically re-verify every intended launch country on a recurring operational schedule |
| Clinical/legal review | Safety boundaries are present | Counsel and licensed clinical reviewers must approve policies, consent, moderation, and advertising rules |
| Accessibility certification | Semantic labels and responsive baseline exist | WCAG 2.2 AA audit, keyboard/screen-reader/device testing, captions, reduced-motion validation with users |
| Persian/RTL | Localization boundary remains simple and English is complete | Translate reviewed English copy, add Persian typography, plural/date rules, mirror navigation, RTL regression test |
| App-store release | Expo configuration and assets exist | Apple/Google accounts, certificates, privacy declarations, screenshots, review, and release pipeline |

## Dependency-security note

`npm audit` reports zero Critical and zero High vulnerabilities after the safe `js-yaml` update.
It still reports 15 Moderate advisories in Expo tooling/transitive packages. npm's proposed fixes would install
incompatible old major versions such as Expo 46, Expo Router 5, or Expo Sharing 14. Those force fixes were intentionally
not applied because they would break the verified Expo SDK 57 dependency contract. Track upstream Expo patches and
re-run the audit during regular dependency maintenance.

## Verification evidence

- Backend: 14 tests, 0 failures, 0 errors, 0 skipped.
- Flyway: 31 migrations validated; schema reached version 30 on a fresh PostgreSQL 14 instance.
- Product configuration regression: 66 active database options, 5 listener modules, 7 sourced crisis resources, durable onboarding/search state, and zero `@greenocean.invalid` accounts.
- Availability regression: overlapping slot rejected, second booking rejected, cancelled appointment releases the slot.
- Session regression: logged-out, password-changed, logout-all, and deleted-account access tokens are rejected.
- Upload regression: a real PDF signature is accepted and MIME-spoofed content is rejected.
- Runtime API smoke: member blocked from admin; credential upload; verification role; availability; booking;
  double-book rejection; slot release; private follow approval; private activity; CSV privacy; and audit logging passed.
- Frontend TypeScript: passed.
- Frontend ESLint: passed.
- Expo dependency compatibility: passed, dependencies up to date.
- Static Web export: passed, 62 routes emitted, including public `/urgent-help`.
- Browser smoke: welcome and login DOM loaded with no console error; welcome layout visually rendered.
- Source scan: no active mock modules, preview-mode keys, TODO, FIXME, or mojibake marker in runtime source.

## Local commands

```powershell
# Terminal 1: backend with its own temporary database
cd D:\p\GreenOcean\backend
.\scripts\run-embedded.ps1

# Terminal 2: frontend
cd D:\p\GreenOcean\frontend
npm install
npm start
```

For Web, press `w` after `npm start`, or run `npm run web`.
