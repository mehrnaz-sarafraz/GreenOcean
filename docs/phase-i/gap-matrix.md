# Phase I targeted baseline gap matrix

Baseline: local validated G/H `785f45a` (200 backend, 59 helper, 9 fixture-browser, 4 real-server G/H tests in its evidence). Its continuation source was published on PR #6 as `38c114add7ce1d0c8ffcce8c68239b43ca004eaa` before creating the stacked Phase I branch. The equivalent tree is `f9b7dab1d8015861c4777d0679d83f68285b1a47`. No main merge is required. No A–H full audit is repeated. This table records initial gaps; see [delivery report](delivery-report.md) for final statuses.

| ID | Initial classification | Actual behavior | Existing dependencies | Smallest implementation / required evidence |
|---|---|---|---|---|
| I-01 | REUSED_WITH_TARGETED_TESTS | H supportDestination routes are real and do not create sessions | onboarding-policy.ts; support.tsx | Preserve routing; intentional request UI only |
| I-02 | PARTIALLY_IMPLEMENTED | AccessPolicy and session interceptor exist, matching does not revalidate both parties at every transition | AccessPolicy; PhaseAInterceptor | Reuse gates at transitions and inside messaging |
| I-03 | DEFECTIVE | Approved application/coursework exists; supervised practice is self-completable | AdminRepository; ListenerRepository; training catalog | Separate independent staff live authorization evidence; preserve legacy roles and fail closed |
| I-04 | DEFECTIVE | Stored online Boolean has no authenticated lease | ListenerRepository; ListenerResponse | Session-bound readiness lease with rotation and foreground heartbeat |
| I-05 | DEFECTIVE | match creates chat immediately and falls back offline | ListenerService.match | Idempotent 90-second request; no chat at creation; bounded rate limit |
| I-06 | MISSING | No listener acceptance endpoint or incoming offers | ListenerController | Owner listener accept/decline with server deadline and transactional lock |
| I-07 | MISSING | No cancel/expire semantics | ListenerService | Terminal deadlines, lazy+scheduled sweep and idempotent cancel |
| I-08 | MISSING | No cross-instance capacity invariant | MessagingRepository | PostgreSQL transaction advisory lock + partial unique indexes |
| I-09 | DEFECTIVE | LISTENER service kind exists but DIRECT reuse is unsafe | MessagingRepository.directConversation | Only explicit member activation after acceptance creates one session conversation |
| I-10 | MISSING | Chat membership is the only session state | chat/[id].tsx | Active/interrupted/terminal lifecycle, both participant heartbeats, bounded reconnect |
| I-11 | DEFECTIVE | LISTENER send checks caller region but not active support state | MessagingService | Current pair/session/qualification checks, DB insert guard and idempotent message key |
| I-12 | DEFECTIVE | Offline fallback and optimistic readiness claims | listener/[action].tsx; support.tsx | Honest unavailable screen and explicit alternatives |
| I-13 | VERIFIED_AND_REUSED | Feed/community/article/urgent routes exist | support.tsx; H routing | Retain real routes, no auto membership or clinical promises |
| I-14 | PARTIALLY_IMPLEMENTED | Report conversation membership and bidirectional blocks exist | ReportRepository; SocialService | Terminate live sessions on safety changes; retain purpose-limited report API/MFA |
| I-15 | PARTIALLY_IMPLEMENTED | Design tokens/English/Persian/RTL and auth isolation exist | product-ui; language-provider; AuthProvider | Localized member/listener states, timers, foreground handling and accessible controls |
| I-16 | BLOCKED_EXTERNAL_DEPENDENCY | No approved aggregate telemetry or metadata retention decision | G/H operations; E/F security logging | No trackers or chat/topic analytics; J decisions and release gate |
| I-17 | MISSING | A–H tests exist, no live acceptance tests | backend tests; frontend e2e/live-e2e | Real DB lifecycle/races/migrations and real two-party browser journey |
| I-18 | MISSING | No Phase I handoff | docs/phase-i | J/K decision register, exact test evidence and operational checklist |

Database scope: additive V41 only, no historical migration edits. Presence, independently reviewed live authorization, bounded requests/session linkage, idempotent message key and uniqueness/authorization constraints. Existing users/roles/history retained. Backend controls are authoritative; frontend states are projections. Privacy risks and tests are detailed in the product contract and final per-requirement delivery matrix. Operational production enablement is false by default; missing legal/staffing approvals are not fabricated.
