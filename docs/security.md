# GreenOcean Security

## Current security model

GreenOcean uses BCrypt password hashing, short-lived HMAC-SHA256 JWT access tokens, stateless Spring Security sessions,
role-based authorization, and method-level authorization for privileged operations. JWT signing and database secrets are
supplied through environment variables and are never stored in the repository.

Refresh tokens are random opaque values. Only SHA-256 hashes are persisted. Every successful refresh rotates the token
under a pessimistic database lock and stores the retired hash until its expiry. Reuse of a retired token revokes that
session family and writes a security audit event. The frontend serializes refresh attempts so concurrent API requests do
not accidentally reuse a token.

## Authentication protection

- Login, registration, refresh, verification, and recovery endpoints are rate limited.
- Repeated login failures temporarily lock the normalized identity; successful login clears its failure state.
- Unknown accounts still execute a BCrypt comparison to reduce timing-based account discovery.
- Public verification and recovery responses do not disclose whether an email address exists.
- Verification and password-reset tokens are random, hashed at rest, expiring, single-use, and superseded when reissued.
- Password reset and password change revoke all active refresh sessions.
- Production login requires a verified email address.
- Security-sensitive events are recorded in `audit_logs` without storing credentials or raw tokens.

The built-in rate limiter is process-local and protects the current modular-monolith deployment. A multi-instance
deployment must add a shared edge or distributed rate limiter as part of production infrastructure.

## Token delivery

Development can run with the no-op delivery adapter; raw tokens are deliberately never written to logs. A built-in HTTPS
webhook adapter is enabled with:

- `greenocean.auth.token-delivery.webhook-url`
- `greenocean.auth.token-delivery.webhook-secret`

The webhook receives an authenticated JSON request containing `type`, `email`, `token`, and `expiresAt`. The `prod`
profile refuses to start while the no-op adapter is active, so verification and recovery cannot be silently deployed
without a real delivery channel.

## Browser and transport policy

Mobile builds store access and refresh tokens in Expo SecureStore. Web builds use `sessionStorage`, never
`localStorage`, so tokens are cleared when the browser session ends. Auth responses send `Cache-Control: no-store` and
`Pragma: no-cache`.

API responses include a restrictive Content Security Policy, `X-Frame-Options: DENY`,
`X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer`, and a restrictive Permissions Policy. Spring's HSTS
header is emitted on HTTPS responses. The production profile trusts framework-forwarded headers and accepts only
explicit HTTPS CORS origins; wildcards, HTTP origins, credentials, paths, queries, and fragments are rejected at startup.

## Security rules

Never commit database passwords, JWT secrets, access tokens, refresh tokens, verification/reset tokens, webhook secrets,
or production credentials. Access JWTs must not be persisted. Opaque authentication tokens must only be persisted as
one-way hashes.

## Production configuration

Activate the `prod` profile and provide:

- `GREENOCEAN_DB_PASSWORD`
- `GREENOCEAN_JWT_SECRET` (at least 32 bytes)
- `GREENOCEAN_ALLOWED_ORIGIN_PATTERNS` (comma-separated explicit HTTPS origins)
- `GREENOCEAN_AUTH_TOKEN_DELIVERY_WEBHOOK_URL` and `GREENOCEAN_AUTH_TOKEN_DELIVERY_WEBHOOK_SECRET`, or another
  `AuthTokenDelivery` implementation

Terminate TLS at the trusted reverse proxy, strip client-supplied forwarded headers there, and pass only sanitized proxy
headers to the application.

## Product and operational controls

Media uploads have size/type/signature validation, opaque storage keys, authenticated visibility-aware reads and automatic expiry when unattached. Professional credentials use a private purpose and are never returned from public profiles.

Messages support client idempotency keys, bounded pages, send throttling, block enforcement and confidential reports. Report and verification decisions are audit logged; reports do not automatically remove content or suspend accounts.

Production emits structured logs with request correlation IDs and exposes health/Prometheus endpoints. Restrict collectors and never send secrets or user content. Backup, restore and incident procedures live in `operations.md`.
