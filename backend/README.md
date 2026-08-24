# GreenOcean Backend

Spring Boot REST API for GreenOcean.

## Local requirements

- Java 21
- PostgreSQL 18
- Database named `greenocean`

## Run locally

Preferred local command (database password is requested securely and the JWT secret is generated automatically):

```powershell
.\scripts\run-local.ps1
```

Alternatively, set local secrets for the current PowerShell session and use the Maven Wrapper:

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

Content and social endpoints (Bearer access token required):

- `POST /api/v1/posts`
- `GET /api/v1/posts/feed?page=0&size=20`
- `GET /api/v1/posts/{postId}`
- `DELETE /api/v1/posts/{postId}`
- `GET|POST /api/v1/posts/{postId}/comments`
- `PUT|DELETE /api/v1/posts/{postId}/like`
- `PUT|DELETE /api/v1/posts/{postId}/bookmark`
- `PUT|DELETE /api/v1/comments/{commentId}/like`
- `PUT|DELETE /api/v1/social/follows/{userId}`
- `PUT|DELETE /api/v1/social/blocks/{userId}`
- `GET /api/v1/search/users?q=...`
- `GET /api/v1/search/posts?q=...`
- `POST /api/v1/communities`
- `GET /api/v1/communities?q=...`
- `GET /api/v1/communities/slug/{slug}`
- `PUT|DELETE /api/v1/communities/{communityId}/membership`
- `GET /api/v1/communities/{communityId}/posts`
- `GET /api/v1/notifications`
- `GET /api/v1/notifications/unread-count`
- `PUT /api/v1/notifications/{notificationId}/read`
- `PUT /api/v1/notifications/read-all`

Post visibility supports `PUBLIC`, `FOLLOWERS`, and `COMMUNITY`. Feed and search results enforce
visibility, membership, blocks, soft deletion, and anonymous-author privacy on the server.
Likes, comments, replies, and follows create notifications only when the interaction is newly created;
repeating an idempotent request does not produce duplicate notifications.

Run the complete authentication smoke test while the backend is running:

```powershell
.\scripts\smoke-auth.ps1
.\scripts\smoke-profile-account.ps1
.\scripts\smoke-content-social.ps1
```

## Test

Preferred local command (the password is requested securely and is not printed or stored):

```powershell
.\scripts\test-local.ps1
```

Alternatively, when `GREENOCEAN_DB_PASSWORD` is already correct in the current terminal:

```powershell
$env:GREENOCEAN_DB_PASSWORD = "your-local-postgres-password"
.\mvnw.cmd test
```

Tests use a dedicated non-production JWT secret from the `test` profile. Real JWT and database secrets are never committed.

Flyway migrations under `src/main/resources/db/migration` are the database schema source of truth.
