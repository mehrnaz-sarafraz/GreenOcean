# Phase L requirements matrix

Verified integrated baseline: main30a4b3a (A–K / V43); L1 PR10 /991c77c, L2 PR11 /fd908d4. No repeated A–K redevelopment. All paths below are inside backend/src/main/java/com/greenocean/backend unless a frontend/docs path is shown. Exact test class names and executed results are in test-evidence.md. Approval blockers are not counted as tested operations.

| ID | Existing gap and completed change | Primary files / DB | Tests and evidence | Final technical status |
|---|---|---|---|---|
| L-01 | Explicit identity first | post/dto/CreatePostRequest.java; frontend/src/app/(app)/create.tsx | L1Publishing, browser L1 | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-02 | Optional topic; Unicode writing | PostService; create.tsx | L1Publishing; L1 browser Persian | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-03 | Final identity/audience approval | create.tsx; publication-identity-contract.md | L1 browser + live L1 | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-04 | Durable anonymity across authorized surfaces | PostReadRepository; NotificationRepository; ProfileService | L1/L2/L3; GH privacy | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-05 | Owner drafts, immutable edits, deletion | SocialDrafts; PostService; V44; my-posts.tsx | L1Publishing; LMigration; live L1 | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-06 | Server pre-publication assessment | SocialPublishing; ContentSafety; PostService | L1Publishing; L3Safety | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-07 | Versioned local classifier and fail-safe | ContentClassifier; ContentSafety; social_assessments | L1 outage; L3Safety bounded examples | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-08 | Default-off operations and staff gates | SocialPublishing; PhaseAInterceptor; operations.md | L1Disabled; EF MFA; L3Review | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-09 | Server Following excludes anonymous authors | PostReadRepository; index.tsx; V45 | L2Social; browser/live L2 | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-10 | Chronological authorized Discover | PostReadRepository; can_read_social_post; index.tsx | L2 stable cursor; GH audience | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-11 | Saved collection revalidates access | PostReadRepository; bookmark removal; index.tsx | L2Social; L3 report removal; GH export | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-12 | Moderated bounded reply trees and owner delete | PostService; post/[id].tsx | L2Social; L3 comment review | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-13 | Existing consent-based follow relationships | SocialService; tools followers; user/[username].tsx | L2 live; CommunityNotification regressions | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-14 | Recipient acceptance and listener isolation | MessagingService/Repository; message-requests.tsx; V45 | L2Social; I regressions; live L2 | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-15 | Privacy-aware search and notifications | SearchService; NotificationRepository; notifications.tsx | L2Social; GH privacy; existing regressions | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-16 | Voluntary, noncompetitive interaction | support-oriented-social-contract.md; chronological feeds | Reused UI + browser/feed checks; no new profiling | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-17 | Revision-aware privileged human review | SocialReviewService/Controller; community-review; V46 | L3Review + L3Concurrency + browser/live L3 | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-18 | Owner notices and independent appeals | SocialReviewService; existing moderation_appeals; content-decisions | L3 ownership/independence/race; browser/live L3 | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-19 | Bounded abuse controls and actual takedown | SocialPublishing; ReportRepository; AdminRepository | L1 duplicate concurrency; L3 abuse and report tests | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |
| L-20 | A–L integration and evidence | All L test suites; test-evidence.md | Full suite and live results recorded separately | IMPLEMENTED_TESTED; integration outcomes in test-evidence.md |

Backend scope: validated owner/identity/audience contracts, current-role/MFA enforcement, version locks, transactional state and bounded pagination. Frontend scope: explicit three-stage composer, optional topic, drafts, owned edits, named Following/Discover/Saved, replies, relationships, requests, bilingual review and appeals. DB scope: V44 (identity/drafts/assessments), V45 (feed indexes/message state), V46 (existing appeal link/review metadata); historical identity/audience untouched.

Privacy impact: unpublished narratives stay private, anonymous ordinary projections redact identity, client sensitive state clears on blur/account change, owner exports/erasure include new data. Security impact: no client-authoritative author/moderation status, privileged identity access separate and audited, report takedown enforced on target.

APPROVAL_REQUIRED / EXTERNAL_BLOCKER: final policy wording, actual staffing/escalation/response obligations, production staff MFA/key recovery, processing/evidence retention, K release blockers and real native accessibility/device execution. These qualify L-07/L-08/L-17/L-18/L-20; they are not fabricated or solved by passing code tests. Local screening is limited and publication stays off.

Final execution: 324 backend +69 frontend logic +99 browser +27 real-stack tests passed, with zero final failures/skips. Third PR upload remains blocked by automatic approval review; see delivery-report.md section L. This delivery blocker is not an unimplemented social feature.
