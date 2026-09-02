
در `docs/security.md` فعلاً فقط **وضعیت واقعی فعلی** را ثبت کن، نه چیزهایی که هنوز نداریم:

```markdown
# GreenOcean Security

## Current security model

GreenOcean currently uses:

- BCrypt password hashing
- JWT access tokens
- managed rotating refresh-token sessions
- hashed refresh tokens in persistent storage
- role-based authorization
- method-level authorization for privileged operations
- stateless Spring Security sessions
- environment-based secrets
- configurable CORS origins

## Security rules

Never commit:

- database passwords
- JWT secrets
- access tokens
- refresh tokens
- production credentials

Access JWTs must not be persisted in the database.

Refresh tokens must only be persisted as hashes.

## Planned hardening

The following are planned and are not yet considered complete:

- authentication rate limiting
- brute-force protection
- email verification
- password recovery
- refresh replay/concurrency protection
- production CORS policy
- production web token policy
- security headers
- security audit logging improvements