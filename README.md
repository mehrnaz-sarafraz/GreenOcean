# GreenOcean

GreenOcean is a privacy-conscious social and support platform for people to share feelings, stories, and lived experiences, connect with supportive communities, and discover verified mental-health professionals.

> A safe place to share your feelings, stories and experiences.

GreenOcean is **not** a medical-record system and must not infer or store diagnoses, mental-health scores, suicide-risk scores, or similar sensitive health profiles.

## Core values

**No judgement · Real support · Verified professionals**

## Technology

### Frontend

* React Native
* Expo
* TypeScript
* Expo Router
* English and Persian localization
* RTL-aware user interface

### Backend

* Java 21
* Spring Boot 4.1
* Spring Security
* Spring Data JPA
* Flyway
* PostgreSQL
* JWT access tokens
* Rotating refresh-token sessions

### Architecture

GreenOcean currently uses a modular-monolith architecture.

The repository is organized as:

```text
GreenOcean/
├── backend/
├── frontend/
├── docs/
├── PROJECT_MEMORY.md
└── README.md
```

## Current development status

The current application includes:

* Registration and authentication
* JWT access and refresh-token sessions
* User profiles and privacy settings
* Feed and post creation
* Comments and replies
* Post and comment likes
* Bookmarks
* Follow and block relationships
* Search
* Communities
* Notifications
* Professional profiles
* Professional verification data
* Articles and media recommendations
* Messaging
* User preferences
* Support channels
* Basic administration and moderation functionality

The PostgreSQL schema is managed exclusively through Flyway migrations.

Current migration level:

```text
V13
```

Existing migrations must not be edited after they have been applied. Schema changes must be introduced using new Flyway migrations.

## Local development requirements

Install:

* Java 21
* PostgreSQL
* Node.js
* npm

The project currently uses PostgreSQL database:

```text
greenocean
```

## Backend

From the repository root:

```powershell
cd backend
```

Set the required environment variables:

```powershell
$env:GREENOCEAN_DB_PASSWORD = "your-local-postgres-password"
$env:GREENOCEAN_JWT_SECRET = "your-development-jwt-secret-at-least-32-bytes"
```

Run the backend:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs locally at:

```text
http://localhost:8080
```

Health endpoint:

```text
http://localhost:8080/api/v1/health
```

Never commit database passwords, JWT secrets, production credentials, or local environment files.

## Frontend

Open another terminal:

```powershell
cd frontend
npm install
```

Create a local `.env` when needed based on:

```text
frontend/.env.example
```

For local web/iOS development:

```text
EXPO_PUBLIC_API_URL=http://localhost:8080
```

For an Android emulator, the backend is generally available through:

```text
http://10.0.2.2:8080
```

Run Expo:

```powershell
npm start
```

Useful frontend checks:

```powershell
npm run typecheck
npm run lint
```

## Database migrations

Flyway is the source of truth for the database schema.

Migration files are located in:

```text
backend/src/main/resources/db/migration/
```

To inspect migration history in PostgreSQL:

```sql
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Do not modify an already-applied migration.

Create a new migration instead:

```text
V14__description_here.sql
```

## Documentation

Project documentation lives under:

```text
docs/
```

Start with:

* `docs/README.md`
* `docs/architecture.md`
* `docs/schema-contract-v1.md`
* `docs/local-development.md`
* `docs/security.md`
* `docs/privacy.md`

Long-lived product and architectural decisions are also recorded in:

```text
PROJECT_MEMORY.md
```

## Repository rules

Do not commit:

* `.env` files
* credentials or secrets
* local Maven repositories
* `node_modules`
* build artifacts
* IDE configuration
* generated temporary files

Keep changes small, reviewable, and domain-oriented.

## Development roadmap

The current hardening roadmap is:

1. Repository cleanup and documentation
2. Automated quality gates and CI
3. Security hardening
4. Frontend architecture improvements
5. Core product infrastructure
6. Production and deployment readiness
7. Product polish and release preparation
