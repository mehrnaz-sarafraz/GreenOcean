# E/F rollout and operations

No production migration, cleanup, deployment or merge was performed. This is an implementation and review runbook; operational steps require the owner's release authorization.

## Compatibility and migrations

E/F starts at Phase D `d135328` (D executable `ee4ca2c`, green run 40372115704). Continuation starts at E/F `c491588`; existing changes are retained. Additive migrations V37, V38 and V39 introduce bounded session/assurance fields, WebAuthn credentials/ceremonies and independently provisioned enrollment permits. No historical applied migration was edited during continuation.

Fresh schema tests and a populated V36-to-current test preserve password hashes, refresh verifiers, users and original session timestamps. Historical sessions have no trustworthy platform type: they receive LEGACY and stricter web bounds (24 hours overall, 30 minutes idle), with shorter pending/staff caps. Migration time never starts a fresh unlimited lifetime. Some older phone sessions will require login. Communicate this before rollout. Unknown migrated sessions have no inherited recent credential proof or MFA.

Deploy coordinated backend/client contracts. Native password confirmation now returns a fresh token pair instead of 204 and requires its session's refresh token; web confirmation moves to the Origin/header-checked cookie endpoint. Clients must store the response before making the sensitive request. The old bearer is rejected immediately by V39 token_version. Never add a downgrade endpoint for outdated clients; direct them to upgrade/sign in. Keep existing cookie name/path while replacing cookie contents. Prefer forward fixes over reverting to a backend that ignores lifetime/version fields.

## Configuration and gates

Existing environment mappings live in application.properties. Approved defaults:

| Environment | Default |
|---|---|
| GREENOCEAN_SESSION_WEB_OVERALL / WEB_IDLE | PT24H / PT30M |
| GREENOCEAN_SESSION_MOBILE_OVERALL / MOBILE_IDLE | P30D / PT24H |
| GREENOCEAN_SESSION_PRIVILEGED_OVERALL / PRIVILEGED_IDLE | PT12H / PT30M |
| GREENOCEAN_SESSION_RECENT_AUTH | PT10M |
| GREENOCEAN_SECURITY_EVENTS_RETENTION_DAYS | 30 |
| GREENOCEAN_ADMIN_ENABLED / WEBAUTHN_ENABLED | false / false |
| GREENOCEAN_WEB_COOKIE_SECURE | true in HTTPS deployments |

Set exact approved HTTPS origins, RP ID and WebAuthn origin list; no wildcard credentialed origins. Existing production startup guards keep administration disabled even when development WebAuthn tests pass. Reviewed staff accounts, independent first-enrollment permits, approved hardware and lost-factor identity proofing are prerequisites. Permits contain high-entropy one-use secrets; only hashes are stored. Issue them through an independently authorized process with a short expiry, never via a public account-management endpoint or shared bypass credential. An interrupted first enrollment consumes the permit and needs a newly authorized one. Enroll a backup key; removing a factor revokes sessions and cannot remove the last remaining key. Lost-all-factor recovery requires independent identity review, revocation, a new permit and audit, not email/password recovery alone.

Keep the Phase D mail transport/outbox and stable separated keys. Supply approved HTTPS recovery base URL, gateway credentials, sender/DNS verification, template approval, authenticated delivery-status webhook and explicitly authorized staging recipients. Staging/prod validators reject unsafe/missing configuration. Test real acceptance and inbox delivery separately. Controlled QA delivery is not production evidence. Never enable sending to real recipients during synthetic tests.

Supply a reviewed compromised-password SHA-256 corpus and existing JWT/outbox secrets via secret management. Preserve old delivery keys during overlap. Do not print recovery links, raw cookies, tokens, email payloads, passwords or private text. Set TLS, HSTS, restrictive CSP, no-referrer/no-store, query/log redaction and backup controls on the actual hosting/ingress. Recovery tokens use fragments and the UI scrubs its current history entry; prior leaked links cannot be recalled. Third-party scripts must not run on recovery pages. Configure a reviewed trusted proxy that strips incoming forwarding headers; direct backend default does not trust them.

## Events and alert handling

Reuse SECURITY-category audit records and fixed `security_event kind=...` operational logs. Owner UI returns at most 50 safe events. Default retention is 30 days; purge is bounded and leaves legal/moderation evidence intact. Storage is capped at 200 events per actor/hour (anonymous failures share a coarse bucket), so this is sampled/bounded evidence rather than an exhaustive forensic trail. Alert pipelines must account for capped counts and collector delay. No mental-health behavior, precise geography, raw IP or user-entered device label should be added as a fraud signal.

Suggested operator review thresholds, to be tuned in staging rather than treated as standard mandates:

- Any REFRESH_REPLAY or unexpected MFA_REMOVED: review promptly, corroborate server revocation, notify the verified owner through the existing outbox, inspect other safe events.
- >=10 PRIVILEGED_DENIED or MFA_FAILURE in five minutes: alert authorized staff; verify configuration/clock/legitimate expired proof before declaring compromise.
- >=30 AUTH_RATE_LIMITED per minute or persistent login-busy responses: review source/account distribution, hashing capacity and trusted ingress. Do not disable account budgets or add permanent lockouts.
- Pending security mail/provider failures older than the approved operational SLA: investigate transport and key configuration; never equate queued/accepted with delivered.

The external collector, alert rules, incident recipients and SLA are not deployed or tested here. Do not claim operational monitoring PASS until a controlled alert and authorized notification exercise is recorded. Security emails are coalesced to at most one per owner/hour; not every individual event produces another email. Successful native/web sign-out is local immediately, but network failure leaves an explicit pending-server-revocation state; reconciliation must succeed before silent cookie restore/new sign-in.

## Reviewer/release checklist

- [ ] Verify stacked PR base is D and includes A/B/C; no main merge or force push.
- [ ] Review all E-01..E-15 and F-01..F-20 rows and actual named test evidence.
- [ ] Validate fresh and populated migration results, communicated legacy mobile expiration, coordinated token-response rollout and recovery plan.
- [ ] Run the native and assistive-technology checklist; JS exports are not device passes.
- [ ] Approve real RP/origin, staff hardware/backup/recovery and independent permit provisioning; keep production admin disabled until separately approved.
- [ ] Verify real mail sender, DNS, staging inbox, callback, recovery URL and separated key management; do not substitute synthetic capture.
- [ ] Verify trusted ingress/TLS/headers, offline logout reconciliation and two-tab account switching on actual hosting.
- [ ] Verify a private-data-free operational alert exercise and recovery/suspension/deletion revocation checks.
- [ ] Approve remaining risks and deployment independently; this checklist does not authorize it.
