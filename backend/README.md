# GreenOcean Backend

Spring Boot REST API for GreenOcean.

## Local requirements

- Java 21
- PostgreSQL 18
- Database named `greenocean`

## Run locally

Set local secrets for the current PowerShell session and use the Maven Wrapper:

```powershell
$env:GREENOCEAN_DB_PASSWORD = "your-local-postgres-password"
$env:GREENOCEAN_JWT_SECRET = [guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')
.\mvnw.cmd spring-boot:run
```

Health check: `GET http://localhost:8080/api/v1/health`

Authentication endpoints:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me` (Bearer access token required)
- `POST /api/v1/auth/change-password` (Bearer access token required)
- `POST /api/v1/auth/logout-all` (Bearer access token required)
- `GET /api/v1/profiles/me` (Bearer access token required)
- `PATCH /api/v1/profiles/me` (Bearer access token required)
- `GET /api/v1/profiles/{username}` (Bearer access token required)

Run the complete authentication smoke test while the backend is running:

```powershell
.\scripts\smoke-auth.ps1
.\scripts\smoke-profile-account.ps1
```

## Test

```powershell
$env:GREENOCEAN_DB_PASSWORD = "your-local-postgres-password"
.\mvnw.cmd test
```

Tests use a dedicated non-production JWT secret from the `test` profile. Real JWT and database secrets are never committed.

Flyway migrations under `src/main/resources/db/migration` are the database schema source of truth.
