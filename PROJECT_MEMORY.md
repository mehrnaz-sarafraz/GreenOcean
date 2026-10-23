# GreenOcean — Project Memory

This file records durable product and architecture decisions.

## Product definition

GreenOcean is a privacy-conscious social and support platform for mental health and lived experience.
It combines categorized social sharing, supportive communities, trained peer listeners, verified
mental-health professionals, evidence-informed articles, psychology media discovery, messaging,
wellbeing reflection tools, and trust-and-safety operations.

It is not a medical-record system, diagnostic tool, therapist, or crisis service. The product must not
infer or present diagnoses, suicide-risk scores, or clinical conclusions from user activity.

Core promise: “A safe place to share your feelings, stories, and experiences.”

Values: No judgement · Human support · Verified professionals

## Locked technical decisions

| Area | Decision |
| --- | --- |
| Frontend | React Native + Expo SDK 57 + TypeScript |
| Web | Expo Router static rendering |
| Backend | Java 21 + Spring Boot modular monolith |
| Database | PostgreSQL |
| API | Versioned REST under `/api/v1` |
| Authentication | JWT access token plus rotating, hashed refresh-token sessions |
| Access revocation | Access JWTs are validated against an active server-side session |
| Schema | Flyway is the source of truth |
| Repository | One monorepo with `frontend/`, `backend/`, and documentation |

## Current language decision

The current release is English-only and LTR. Persian/RTL remains a product requirement, but the user
explicitly deferred its implementation until the English product is complete. Runtime language
switching and Persian mock copy must not be reintroduced before that milestone.

## Product and safety principles

- Safety, privacy, moderation, and respectful interaction are product features.
- Wellbeing tools use non-diagnostic language and keep private records separate from the social feed.
- Anonymous publishing hides identity from peers, not from authorized safety operations.
- Private profiles require explicit follow approval before activity is disclosed.
- Private communities require membership approval and role-based management.
- Professionals use a dedicated profile and verification history, never a single boolean flag.
- Credential files live in file/object storage, not PostgreSQL; only metadata and hashes are persisted.
- Appointment availability is explicitly published and collision-protected.
- Account deletion revokes all sessions and removes or anonymizes data according to its purpose.
- Administrative reads and changes are role-gated and sensitive operations are audited.

## Implemented scope — 2026-09-13

- English/LTR design system, welcome and information pages, auth/onboarding, responsive navigation, feed,
  categorized creation, post detail, profiles, search, notifications, communities, knowledge, media,
  conversations, support channels, listeners, professionals, consultation booking, wellbeing, safety,
  account controls, and administration.
- API-backed runtime data; old frontend mock and preview-mode modules are removed.
- PostgreSQL schema has 31 Flyway migrations through `V30`.
- Configurable product choices, listener training, onboarding state, recent search history, and sourced urgent-help records are PostgreSQL-backed.
- Historical development identities are deleted by migration; curated reference content remains in the database.
- Authentication supports registration, login, refresh rotation, logout, logout-all, password change,
  server-side access-token revocation, session inventory, and individual session revocation.
- Professionals can apply, upload signature-validated documents, publish availability, receive
  appointment requests, manage consultations, write articles, pin scientific content, and receive reviews.
- Communities support private join requests, approval/decline, roles, rules, events, attendance, and removal.
- Messaging uses API persistence and short polling; notifications use polling and durable read state.
- Admin operations include moderation queues, users, communities, verification, document scan state,
  listeners, campaigns, broadcasts, support, policies, exports, and audit activity.
- The frontend passes TypeScript, ESLint, Expo dependency compatibility, and static Web export.
- The backend integration/unit suite passes against a real PostgreSQL database.
- Public urgent help is available before sign-in; source URLs and verification dates are stored with each resource.

## Production-only integrations still requiring external setup

- SMTP/transactional-email provider and delivery worker for the implemented email outbox
- Encrypted production object storage and malware-scanning service for credential uploads
- APNs/FCM push delivery and a WebSocket/SSE gateway if real-time delivery replaces polling
- Payment/billing provider for paid promotion or paid appointments
- Deployment environment, DNS/TLS, secret management, backups, metrics, alerting, and log aggregation
- Clinical/legal review of safety copy and country-specific urgent-help resources
- Human accessibility audit and physical iOS/Android device QA
- Persian translation and RTL pass, intentionally deferred

The exact route map, completed-gap matrix, and verification evidence live in [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md).
