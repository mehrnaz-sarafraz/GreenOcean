# GreenOcean Backend

Spring Boot REST API for GreenOcean. It is a modular monolith backed by PostgreSQL and Flyway.

## Local requirements

- Java 21
- PowerShell
- Either the bundled embedded PostgreSQL runner or an existing PostgreSQL installation

## Run without an installed database

This is the recommended command for frontend and local API development:

```powershell
cd D:\p\GreenOcean\backend
.\scripts\run-embedded.ps1
```

It starts a real temporary PostgreSQL 14 process, applies all Flyway migrations, loads curated reference content,
and runs the API on `http://localhost:8080`. No development identities remain after migration. The temporary data is discarded when the process stops.

## Run with persistent PostgreSQL

```powershell
cd D:\p\GreenOcean\backend
.\scripts\run-local.ps1
```

Or configure the process directly:

```powershell
$env:GREENOCEAN_DB_PASSWORD = "your-local-postgres-password"
$env:GREENOCEAN_JWT_SECRET = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
.\mvnw.cmd spring-boot:run
```

Health check: `GET http://localhost:8080/api/v1/health`

## Implemented domains

- Authentication: registration, login, rotating refresh sessions, logout, logout-all, password change, session listing and revocation
- Account: privacy-safe JSON archive and password-confirmed account deletion/anonymization
- Profiles/social: public/private profiles, posts by username, follows, private follow requests, approvals, blocks, followers, and following
- Content: categorized posts, post types, anonymous publishing, visibility enforcement, comments/replies, reactions, bookmarks, and professional responses
- Communities: discovery, creation, public/private membership, join requests, roles, moderation, rules, events, attendance, and member feeds
- Catalog: support categories, 66 database-managed product options, professional directory/details, articles, helpful/saved state, psychology media recommendations, and sourced crisis resources
- Professionals: credential applications, signed document upload, manual scan/review status, reviews, availability slots, appointment requests, and practice-side consultation management
- Messaging/support: user and professional conversations, messages, support channels, listener matching, applications, training, and activation
- Wellbeing: check-ins, goals, journals, non-diagnostic reflections, and guided growth progress
- Safety: reports, personal report history, moderation appeals, durable onboarding/preferences/search history, public policies, public urgent resources, and assistance requests
- Administration: role-gated operational dashboard, queues, broadcasts, campaigns, policy publishing, account/community actions, credential scanning state, CSV exports, and audit logging

## Security behavior

- Passwords and refresh tokens are stored only as hashes.
- Every access token is checked against its active server-side session, so logout, logout-all, password change, or deletion immediately revokes it.
- Authorization failures return structured HTTP 403 responses.
- Private-profile, community-membership, block, soft-delete, and anonymous-author rules are enforced by backend queries.
- Professional uploads are size-limited, stored outside PostgreSQL, hashed, and validated by actual PDF/JPEG/PNG signatures.
- Appointment slots are row-locked when booked and per-professional schedules are transaction-locked while checking overlap.
- Admin exports use allowlisted datasets, CSV formula neutralization, no-store responses, and download audit events.

## Migrations

Flyway migrations under `src/main/resources/db/migration` are the schema source of truth.
There are 31 ordered migrations (`V0` through `V30`), including local UUID compatibility, the original schema,
product workflow completion, availability, public policies, database-managed product configuration, development-fixture cleanup, and sourced crisis resources.

## Test

With a PostgreSQL database available:

```powershell
.\mvnw.cmd test
```

The suite covers application startup, schema constraints, auth/session revocation, profile/account behavior,
content/social/community notifications, availability collision and double booking, uploaded-file signature checks,
registration intent, onboarding/search persistence, product configuration, fixture removal, and crisis-resource access.

Optional running-server smoke scripts:

```powershell
.\scripts\smoke-auth.ps1
.\scripts\smoke-profile-account.ps1
.\scripts\smoke-content-social.ps1
```

See [../IMPLEMENTATION_STATUS.md](../IMPLEMENTATION_STATUS.md) for the complete product coverage and production integration checklist.
