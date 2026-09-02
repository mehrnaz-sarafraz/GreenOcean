# Local Development

## Requirements

- Java 21
- PostgreSQL
- Node.js
- npm

## PostgreSQL

Local database:

`greenocean`

## Backend environment variables

Required:

- `GREENOCEAN_DB_PASSWORD`
- `GREENOCEAN_JWT_SECRET`

Optional/current configuration:

- `GREENOCEAN_ALLOWED_ORIGIN_PATTERNS`

Never commit local secrets.

## Run backend

From `backend/`:

```powershell
$env:GREENOCEAN_DB_PASSWORD = "your-password"
$env:GREENOCEAN_JWT_SECRET = "development-secret-at-least-32-bytes"

.\mvnw.cmd spring-boot:run