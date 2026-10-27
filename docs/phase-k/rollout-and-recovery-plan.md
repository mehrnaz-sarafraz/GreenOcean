# Rollout and recovery — plan, not authorization

No main merge, deployment, production migration, real user security test or service enablement is performed by K. Draft PR9 is stacked on J PR8; preserve the ordered A→B→C→D→EF→GH→I→J→K dependency chain and review/retarget each PR deliberately. A green build never approves deployment.

| Environment | Configuration/data/access | Release gate |
|---|---|---|
| Development | Local secrets, synthetic accounts, private origins; no copied production content | Essential tests and private exposure only |
| Automated QA | Test profile, disposable PG18, example.test email, loopback/key-protected test-only transport/staff/listener fixtures; temporary HTTPS test certificate | CI only; no production binary contains fixture classes; no real service approval inferred |
| Staging | Actual provisioned private infrastructure, separate secrets/DB/mail allowlist, exact HTTPS origins, restricted approved operators | Infrastructure/access/advisory qualification and approved rehearsal required; not currently provisioned/verified |
| Production/pilot | Prod validation, real legal/region/staff/mail/monitoring/backup/native requirements, explicit owner authorization | PRODUCTION_NO_GO / PILOT_BLOCKED until blockers resolved |

Before release: verify final executable source and dependencies, current source/migration evidence, database backup/replay and volume-shaped DDL; supply GREENOCEAN_DB_PASSWORD/JWT_SECRET, separate stable outbox/event keys and approved gateway credentials using a real secret manager; preserve old mail key IDs for overlap. Configure exact HTTPS origins/Secure cookie, coordinated new clients/proof responses, privacy/cache behavior and offline breached-password corpus. Never copy test JWT/mail secrets or disable auth to get a health check green.

Keep GREENOCEAN_LIVE_SUPPORT_ENABLED=false, GREENOCEAN_ADMIN_ENABLED=false and APP_PRIVACY_SUPPORT_TOPICS_ENABLED=false pending their prerequisites. APP_GOVERNANCE_OPTIONAL_PROCESSING_APPROVED, POLICY_PUBLICATION_ENABLED and RIGHTS_INTAKE_ENABLED remain false. Analytics/marketing have no approved pipeline; no grants activated. Do not create new AVAILABLE country/care rows as a deployment convenience. The prod guard deliberately still rejects enabled administration; real staff enablement requires a separately reviewed operational release.

GET /api/v1/health is application liveness only; it returns a static UP/timestamp and does not prove DB/mail/queue/operator readiness. Gate actual rollout on a restricted DB/auth/allowed synthetic request plus mail outcomes, source and migration validation, capacity/error/backlog checks and operator confirmation. No new privacy-sensitive telemetry is introduced to simulate operational approval.

Proposed initial controlled staging qualification is four concurrent synthetic workers, zero authorization/privacy/capacity violations, no unexplained5xx/pool leak, and observed per-scenario latency distribution. Suggested discussion targets (not approved SLOs): p95<2s for ordinary reads/transitions and<5s for password hashing/erase on specified staging hardware. Owner must choose actual pilot capacity and binding commitments before any public promise. Review DB query/lock and connection timeouts under real load; current small QA run does not establish saturation or bounded production waits.

## Recovery register

Every row requires a named actual operator and evidence trail before launch; roles below are responsibilities, not fictitious contacts. No measured/approved production RTO/RPO is claimed.

| Scenario / detection | Containment and recovery/data preservation | Required operator / validation |
|---|---|---|
| Migration failure / startup logs | Stop rollout, isolate incompatible instance; preserve backup/failed history; reviewed additive forward fix, no checksum rewrite/clean | DB/release owner; existing failed-migration tests plus real-volume rehearsal |
| DB outage/pool exhaustion / request failures | Stop new intake, avoid retry storms; restore DB/connectivity and reconcile durable state before resume | Infrastructure; database health and contention drill outstanding |
| Mail outage / D queue outcomes | Keep pending state honest, pause cleanup during outage; repair provider, idempotent retries, verify authorized inbox | Delivery; controlled gateway failure/retry tests passed, real provider drill missing |
| Key compromise / security event | Revoke affected sessions, rotate JWT/gateway/crypto keys with reviewed overlap, assess exposure privately | Security/delivery; operator custody and recovery approval needed |
| Lost old mail key / decrypt failure | Do not erase ciphertext blindly; recover retained key material; expire/reissue challenges through approved account flow | Delivery/security; D overlap tests do not prove real key backup |
| Auth/revocation outage / failed login or replay | Fail closed, local logout guard/private memory clear; repair authority then verify live DB session status | Security; EF races/cache tests, external outage exercise missing |
| Listener matching outage / transition failures | Consistently disable live support across instances, no hidden queue, reconcile terminal records and show alternatives | Listener operations; I rollback/restart tests, no real coverage claim |
| Live shutdown / disabled flag | Stop new readiness/send and safety-end open work, preserve authorized history/evidence; explicit fresh readiness after approved resume | Listener/security; controlled disabled-service test |
| Moderators unavailable / backlog | Keep purpose-limited report intake/status honest; no automated unrestricted transcript access or false handled claim | Moderation; actual rota/escalation unavailable |
| Bad client rollout / UI errors | Disable affected feature, retain server predicates; deploy compatible forward client/backend, communicate forced login/legacy bounds | Release/client owner; old instant-match/old authorization binaries unsafe rollback |
| Private-content exposure / report | Remove affected access, preserve minimal authorized evidence, revoke sessions/audiences as needed, assess legal notifications | Security/privacy/legal; no automatic statutory notification |
| Credential-file erasure failure / retry status | Keep FAILED_RETRYABLE, repair storage permission/connectivity, rerun bounded idempotent worker; never claim completed early | Storage/account owner; existing fault/retry tests |
| Backup restore / checksum mismatch | Quarantine; restore using matching tools, compare state and Flyway history, reconcile later erasure/revocation ledger and service gates | DB/privacy owner; isolated checksum restore executed, actual old-backup replay external |

Rollback feasibility: schema is additive but old application code can bypass new session/audience/consent/live predicates. Do not roll back to those binaries on live traffic. Prefer safe feature shutdown and reviewed forward fixes. Communicate legacy session tightening, coordinated reauthentication response changes, historical audience preservation and temporarily unavailable services before rollout. Retention proposals in J remain unapproved; no new purge is authorized by this plan.
