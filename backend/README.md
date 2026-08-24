# GreenOcean Backend

Spring Boot REST API for GreenOcean.

## Local requirements

- Java 21
- PostgreSQL 18
- Database named `greenocean`

## Run locally

Set the database password for the current PowerShell session and use the Maven Wrapper:

```powershell
$env:GREENOCEAN_DB_PASSWORD = "your-local-postgres-password"
.\mvnw.cmd spring-boot:run
```

Health check: `GET http://localhost:8080/api/v1/health`

## Test

```powershell
$env:GREENOCEAN_DB_PASSWORD = "your-local-postgres-password"
.\mvnw.cmd test
```

Flyway migrations under `src/main/resources/db/migration` are the database schema source of truth.
