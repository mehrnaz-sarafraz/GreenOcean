# Production operations runbook

## Release boundary

The repository builds production images but does not publish or deploy them. A release requires TLS ingress, a managed secret store, encrypted PostgreSQL/media storage, monitoring destinations, a mail-delivery webhook and a tested off-site backup target.

## Configuration

Start from `.env.production.example`; never use it as a real secret file. Production startup rejects wildcard/non-HTTPS CORS origins, unverified-email login and no-op token delivery. Use at least 32 random bytes for the JWT secret and rotate it through planned session invalidation.

## Health and observability

- Liveness/readiness: `/actuator/health/liveness` and `/actuator/health/readiness`.
- Prometheus: `/actuator/prometheus`; restrict network access to the collector.
- Every HTTP response carries `X-Request-Id`; production logs are structured and include it.
- Alert on sustained 5xx rate, auth abuse, critical safety-queue growth, database saturation, media capacity and backup failure.
- Never export request bodies, auth headers, tokens, private messages or verification metadata to logging/error vendors.

## Backup and restore

`scripts/backup-postgres.ps1` produces a custom PostgreSQL backup. Encrypt and move it off-site, and snapshot the media volume on the same schedule—a database backup without matching media is incomplete.

Suggested baseline: daily backups, 30 daily copies and monthly restore drills. Restore into isolation first; validate Flyway history and row/media counts before production recovery. `restore-postgres.ps1` requires the explicit `-ConfirmRestore` switch.

## Incident checklist

1. Declare owner/severity and preserve request IDs and audit records.
2. Contain access and rotate exposed secrets without destroying evidence.
3. Determine affected users/data and the exposure window.
4. Follow legal notification requirements and communicate without speculation.
5. Recover from a known-good state, monitor and record remediation actions.
