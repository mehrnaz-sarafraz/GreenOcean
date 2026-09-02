# GreenOcean — Project Memory

> This file is the durable, version-controlled context for GreenOcean. Update it when a product or architecture decision changes.

## Product definition

GreenOcean is a privacy-conscious social and support platform for mental health and human experiences. It combines:

- social sharing of feelings, stories, and lived experiences;
- supportive community interaction (reactions, comments, follows, bookmarks, communities);
- participation by verified mental-health professionals, including a future free-consultation capability.

It is **not** a medical-record system. The product must not infer or store diagnoses, depression/anxiety scores, suicide-risk scores, or similar sensitive health profiles.

Core promise: **“A safe place to share your feelings, stories and experiences.”**

Values: **No judgement · Real support · Verified professionals**

## Product principles

- Safety, moderation, privacy, and respectful interaction are core product features.
- Account deletion is handled with soft deletion where appropriate; historical content and moderation records are not accidentally destroyed.
- Professionals are represented by a dedicated profile plus a verification history, never a simple `is_doctor` flag.
- Uploaded media belongs in object storage, not PostgreSQL.

## Technical decisions (locked for V1)

| Area | Decision |
| --- | --- |
| Mobile frontend | React Native + Expo + TypeScript |
| Backend | Java + Spring Boot |
| Database | PostgreSQL |
| API | REST |
| Authentication | JWT access token + rotating/managed refresh-token sessions |
| Backend architecture | Modular monolith; reconsider microservices only after real growth demands it |
| Database migrations | Flyway is the source of truth; never make untracked manual schema changes |
| Repositories | One Git repository with `frontend/` and `backend/` for initial development; split only if collaboration/release needs prove it necessary |

Target repository layout:

```text
GreenOcean/
  frontend/    # React Native / Expo
  backend/     # Spring Boot
  docs/
```

## Internationalization

Localization is a first-class requirement, not a later translation pass:

- English uses LTR layout.
- Persian and Arabic use RTL layout.
- Text, alignment, navigation affordances, and layout direction must follow the active locale.

## Initial registration and onboarding

Registration needs: email, password, birth year, country, and city. Collect some fields in a slide-based onboarding flow for a better experience and later privacy-respecting personalization/analytics.

## Initial user-facing scope

1. Welcome and onboarding
2. Authentication
3. Home/feed
4. Create post
5. Post detail and nested comments
6. Profile
7. Search/explore
8. Notifications
9. Communities
10. Professionals and verification
11. Consultation (future V1 increment)
12. Messaging (future V1 increment)

## Visual direction

The visual identity evokes a calm, deep green ocean: neither a stereotypical medical-app green nor a crypto-exchange green. The welcome screen uses an image background, a strong dark/transparent overlay, layered content, and a subtle animated entrance (in the emotional direction of 7 Cups, without copying it).

## Data-contract summary

The approved schema contract defines 23 tables across these domains:

```text
AUTH:        users, profiles, user_sessions, roles, user_roles
SOCIAL:      follows, blocks, bookmarks
CONTENT:     posts, comments, media, tags, post_tags
COMMUNITY:   communities, community_members
PROFESSIONAL: professional_profiles, professional_verifications
ENGAGEMENT:  post_likes, comment_likes, notifications
SAFETY:      reports, audit_logs
```

Common rules:

- UUID primary keys; UTC `TIMESTAMPTZ` timestamps.
- Case-insensitive uniqueness for email and username.
- Composite primary keys for relationship tables when appropriate.
- Strict foreign keys, explicit `CASCADE` / `RESTRICT` / `SET NULL` behavior.
- CHECK constraints enforce invariants such as no self-follow, one report target, and community visibility requiring a community.
- Passwords and refresh tokens are stored only as hashes; access JWTs are not stored.
- Likes are domain-specific: `post_likes` and `comment_likes`, not a generic polymorphic table.

The complete detailed schema contract is currently retained in the original pasted specification attached to this task. Before implementation, copy it into `docs/schema-contract-v1.md` and review any outstanding dependency-order adjustments in the Flyway migrations.

## Backend package direction

Use domain-oriented modules under `com.greenocean.backend`, such as `auth`, `profile`, `post`, `social`, `community`, `professional`, `engagement`, and `moderation`; each owns its controller, service, repository, entity, and DTOs. Avoid a single global `entity/` bucket.

## Implementation order

```text
Local tools → Git/GitHub → repository scaffolding → PostgreSQL
→ Spring Boot + Flyway schema → schema verification tests → JPA entities
→ auth API → Expo app/navigation → onboarding/auth UI → feed and content
→ remaining domains → security/deployment
```

## Current state — 2026-08-24

- The private GitHub monorepo is connected on `main` with `backend/` and `frontend/` roots.
- Backend runs on Java 21, Spring Boot 4.1, PostgreSQL 18 and Flyway. Ten migrations are applied and schema constraints have integration coverage.
- Authentication is implemented end to end: registration, BCrypt passwords, JWT access tokens, rotating hashed refresh-token sessions, login, refresh, logout, logout-all and password change.
- Profile APIs support own-profile reads/updates, privacy controls and privacy-filtered profile lookup by username.
- Backend health, auth and profile flows have automated integration tests and PowerShell smoke tests.
- The current tested milestone adds posts, visibility-aware feed, comments/replies, post and comment likes, bookmarks,
  follow/block rules, user/post search, communities and notification delivery/read state.
- Frontend uses Expo SDK 57, React Native 0.86, React 19 and strict TypeScript with Expo Router.
- Frontend includes theme tokens, English/Persian localization, RTL-aware layout, Welcome/Login/Register, secure token
  storage, automatic refresh, session restoration, Feed/Create/Post Detail/Search/Profile, Community and Notification routes.
- Content/social/community/notification code compiles and the frontend passes type-check, lint and static web export.
  Local PostgreSQL integration tests and the end-to-end smoke script must pass before this milestone is committed.
- After that commit, the next milestone is Professionals + verification + Consultation, followed by Messaging,
  Moderation/Media completion, deployment hardening and the dedicated GreenOcean branding/detail pass.


## Current state — 2026-09-02

* GreenOcean is maintained as a GitHub monorepo with `backend/`, `frontend/`, and `docs/` at the repository root.
* Backend runs on Java 21, Spring Boot 4.1, PostgreSQL 18, Spring Security, JPA, and Flyway.
* The database schema is currently migrated through V13. Flyway remains the authoritative source of truth for schema evolution.
* Authentication supports registration, BCrypt password hashing, JWT access tokens, rotating hashed refresh-token sessions, login, refresh, logout, logout-all, password change, and authenticated user lookup.
* Profile functionality includes account/profile management, privacy controls, public profile lookup, and profile statistics.
* Social and content functionality includes posts, visibility-aware feeds, comments and replies, likes, bookmarks, follows, blocks, search, communities, and notifications.
* Professional functionality includes professional profiles, professional discovery, verification data, and administrative verification operations.
* Additional application functionality includes articles, media recommendations, messaging, support channels, user preferences, and basic moderation/admin capabilities.
* The frontend uses Expo SDK 57, React Native 0.86, React 19, TypeScript, and Expo Router.
* Frontend localization currently includes English and Persian with RTL-aware behavior.
* The frontend includes authentication and onboarding, feed, content creation, post detail, profiles, search, notifications, communities, professionals, articles, media, messaging, support, safety settings, and administration screens.
* Local PostgreSQL connectivity has been verified and the current development database successfully validates all 13 Flyway migrations and reaches schema version V13.
* Repository cleanup and documentation are the current hardening milestone.
* The next milestone is automated quality gates and CI, followed by security hardening, frontend architecture improvements, missing product infrastructure, production readiness, and release polish.

## Current hardening roadmap

```text
1. Repository cleanup and durable documentation
2. Automated quality gates and CI
3. Authentication and application security hardening
4. Frontend architecture and automated testing
5. Core product infrastructure and safety workflows
6. Production, deployment and observability
7. Branding, accessibility, performance and release preparation
```
