# Phase D operational contract

## Environment boundaries

| Setting / behavior | Development | Automated QA | Staging | Production |
|---|---|---|---|---|
| Mail enabled / mode | disabled / development by default; explicit controlled gateway optional | synthetic transport and loopback HTTPS capture only | enabled / staging, startup enforced | enabled / production, startup enforced |
| Sender / recipient | no-reply@greenocean.invalid default cannot be used for live deployment | synthetic example.test only | verified sender + exact authorized recipient allowlist | verified sender + operational provider account |
| Gateway | configured HTTPS, no redirects; private Bearer token | local ephemeral TLS certificate trusted only by test client | authorized HTTPS endpoint and separate credential | authorized HTTPS endpoint and separate credential |
| Encryption | explicitly supply stable master key; JWT fallback is development compatibility only | synthetic fixture keys | supply separate stable >=32 character master and key ID | separate master required by existing production validator; secret manager |
| Retry / worker | 5 attempts, batch10, lease30s, poll5s | controlled failures, restart and competing workers | same bounded defaults; tune after observing latency | same; lease must exceed 10s request timeout and deployment timing margin |
| Abuse limits | 5 guesses, 5 deliveries, 3 corrections per account / 15 minutes; resend60s | same budgets; test profile cooldown0 except dedicated cooldown60 test | same configurable defaults; existing IP limits retained | same; monitor before changing budgets |
| Monitoring | aggregate safe log events | assertions, JUnit, traces and controlled mailbox | alert routing and callback integration must be configured | operational alerts and incident ownership required |
| Pending cleanup | disabled, dry-run true, pause false | explicitly constructed enabled workers on disposable accounts | first enable dry-run only, review bounded counts | enable real cleanup only after dry-run review; pause on delivery incidents |
| External evidence | no real delivery claimed | no real inbox delivery claimed | NOT EXECUTED: no supplied sender/provider credentials | NOT EXECUTED; release remains blocked |

Configuration lives in backend/src/main/resources/application.properties. Use deployment secrets, never committed .env values. Mail variables: GREENOCEAN_MAIL_ENABLED, _MODE, _FROM, _GATEWAY_URL, _GATEWAY_TOKEN, _TEMPLATES_APPROVED, _STAGING_RECIPIENTS, _EVENT_KEY, _OUTBOX_KEY, _KEY_ID, _PREVIOUS_KEYS, _MAX_ATTEMPTS, _BATCH_SIZE, _LEASE_SECONDS (each prefix GREENOCEAN_MAIL). Poll overrides use Spring properties greenocean.mail.poll-ms and metrics-poll-ms. Verification variables: GREENOCEAN_VERIFICATION_ATTEMPTS, _DELIVERIES, _WINDOW, _RESEND_COOLDOWN_SECONDS; correction uses GREENOCEAN_EMAIL_CORRECTION_ATTEMPTS. Set a strong, distinct event key of at least32 characters if callbacks are enabled.

## Trusted gateway contract and templates

POST the configured HTTPS endpoint with Bearer credential, Content-Type application/json and Idempotency-Key equal to the stable delivery UUID. The JSON fields are deliveryId, sender, recipient, template, templateVersion=1, language, expiresAt (UTC), secret. The only templates are email-verification and password-reset. No endpoint accepts arbitrary recipient/template input from the client. The gateway must authenticate, validate this envelope, escape template substitutions and never log the secret, request body or authorization header. Restrict outbound network access to the approved gateway. Separate staging and production credentials/senders; staging transport enforces the exact recipient allowlist.

The deployed gateway owns actual HTML/plain-text rendering and sender verification. Reviewed version1 wording must identify GreenOcean, show the six-digit verification code as text (including leading zeros), explain15-minute server expiry, newest-code-only behavior, and how to ignore an unrequested email. Provide equivalent English/Persian content and plain-text alternatives; never claim ownership proves age, professional eligibility or MFA. Password-reset wording must be distinct and describe recovery only. Current worker sends language=en because the existing account/challenge schema does not persist preferred language; transport supports en/fa, tested directly. Persian UI remains supported. Do not set TEMPLATES_APPROVED=true until the actual gateway templates have been reviewed.

Gateway must atomically deduplicate deliveryId across concurrent requests and restarts, bind it to one immutable envelope and retain deduplication for at least the challenge lifetime plus retry/lease recovery margin. Reject expired envelopes even if a request was delayed in transit. A duplicate accepted request must return the original successful outcome/reference, not create another email. Return2xx only after durable acceptance. Optional X-Provider-Reference is a bounded safe identifier (160 characters). Application stores SENT + ACCEPTED, which proves gateway acceptance only. It does not prove inbox placement. Configure provider references in staging so callback correlation checks both identifiers.

408/409 and transport timeout have uncertain acceptance: retry the same deliveryId.429/5xx retry; other non2xx are permanent. Application backoff is exponential15..300 seconds plus jitter, capped attempts and server expiry. Lease recovery uses the same ID and a fresh lease token; stale finishers cannot update newer claims. Gateway idempotency is a required external guarantee, not an exactly-once promise made by this database alone. Cancellation cannot recall mail already in flight; old codes still fail server verification.

Optional delivery callbacks: trusted gateway POST /api/v1/mail/events with X-GreenOcean-Mail-Key and JSON deliveryId, providerReference, outcome (DELIVERED or BOUNCED), occurredAt (ISO timestamp). Only SENT rows are updated; an existing stored provider reference must match. Rows without a provider reference can accept a trusted gateway event by delivery UUID. Duplicate/older events do not replace a newer event, timestamps older than7days or more than5minutes ahead reject. Callback does not activate an account. Keep its secret separate from sending credential, validate provider signatures in the gateway, restrict ingress and forward normalized events only. Unconfigured callback key rejects requests.

Before staging: supply authorized provider credentials and sender domain, verify SPF/DKIM/DMARC and domain alignment in that provider, review both templates, configure exact test recipients, authorized callback and alerts. Send an owner-authorized synthetic test; record provider ID, acceptance, delivered/bounce evidence and recipient observation without recording OTPs. Test throttling, timeout and bounce recovery. None of these real-provider/DNS/inbox checks was performed in this task.

## Key deployment and recovery

Preserve the existing master on first V36 rollout. Existing v1 ciphertext and HMAC hashes are readable with the same master. V2 derives different AES-GCM and verification-HMAC keys and prefixes the key ID; only the active key creates new data. Previous keys use semicolon-separated id=master entries in GREENOCEAN_MAIL_PREVIOUS_KEYS, with at most4 total keys. IDs have at most32 letters/digits/underscore/hyphen. Never reuse an ID with a different master. Store each key in the secret manager and keep it out of logs, PRs and shell history.

Rotation: deploy a new active ID/master while retaining the old active entry in previous keys on every instance. Roll all instances with overlapping rings before removal; otherwise old processes cannot read newer payloads. Startup readiness decrypts live queued payloads before dispatch; missing/corrupt key fails explicitly. Restore the previous ring on failure; never delete the queue to make startup pass. Keep overlap until all old encrypted queue entries and leases have drained/expired AND old verification/reset credentials have expired (verification15minutes, reset30minutes, plus deployment margin). Inspect ciphertext/hash prefixes with read-only aggregate queries; never output payloads. Sent payloads are purged, but live challenge HMACs can still require an old key even when no queue remains. No eager re-encryption job is introduced.

## Upgrade and recovery

V36 is additive to V35. Back up database and key configuration before deployment. It adds verification activity, outbox owner/challenge links, scheduling/lease/outcome metadata and indexes. Duplicate pre-existing unconsumed verification challenges are retired before enforcing one current challenge per user; the newest remains. Existing verified ACTIVE accounts, password hashes and recoverable encrypted queue payloads are preserved. Existing SENT rows become ACCEPTED; exhausted failures become DEAD with secrets purged. The disposable-schema migration test upgrades V35 data toV36 and validates checksums.

Use Flyway normally; never edit V1–V35, run repair or delete history to hide failures. On failure stop rollout, preserve data/keys, investigate and apply a forward migration. Application rollback to old worker code after V36 is unsafe because it does not understand leases/extended states; pause dispatch and use a reviewed compatible forward fix. No production migration or deployment was executed here.

The application currently shares its existing datasource between worker, auth and cleanup. There is no new separately isolated DB role or service. Deployment operators should provision an application-specific role (no superuser), controlled migration credentials and restricted gateway secrets/egress. Cleanup requires existing erasure permissions across account-owned tables; do not pretend a narrow outbox-only role can run the whole app.

## Monitoring and incidents

mail_delivery logs contain safe outcome codes; mail_queue reports pending/retry/terminal/stuck/bounced totals every minute. pending_cleanup logs dry-run candidate counts or erased/failures, no addresses/codes. Monitor new KEY_OR_PAYLOAD_ERROR, permanent failures, increasing DEAD/retry, expired leases and BOUNCED; configure external alert routing, trend thresholds and an on-call owner. Totals are cumulative, so alert on changes/age as well as counts. Provider acceptance without callback may remain ACCEPTED indefinitely; investigate provider records rather than assume delivery.

Safe read-only inspection examples (no secret fields):

```sql
SELECT status,outcome_code,count(*) FROM email_outbox GROUP BY status,outcome_code;
SELECT count(*),min(created_at) FROM email_outbox WHERE status IN ('PENDING','FAILED');
SELECT count(*) FROM email_outbox WHERE status='SENDING' AND lease_until<CURRENT_TIMESTAMP;
SELECT delivery_state,count(*) FROM email_outbox WHERE status='SENT' GROUP BY delivery_state;
```

During gateway outage: keep OTP verification/pending restrictions intact, let bounded retries run, alert, pause pending cleanup using GREENOCEAN_PENDING_CLEANUP_PAUSED=true and restart/reconfigure affected instances. Do not manually mark failed mail SENT or extend an old code. After gateway/key recovery issue a fresh code through the existing rate-limited resend; old code remains invalid. Review affected cohorts before resuming cleanup. An accepted-but-delayed message still expires server-side; provider retry never extends OTP lifetime.

## Approved pending retention

Owner approved **7days after last verification activity** on2026-10-26. This is product configuration, not a legal retention assertion. Implementation conservatively also protects recent account creation and login, taking the latest timestamp. Successful challenge issuance, email correction, pending eligibility updates and recovery update activity; ignored/throttled requests do not extend it. Live recovery credentials and rows locked by authorized operations are excluded. Threshold is GREENOCEAN_PENDING_RETENTION=P7D; scheduler defaults hourly, batch50 (maximum1000). Override with GREENOCEAN_PENDING_CLEANUP_POLL_MS and _BATCH_SIZE.

Only phase-A-enrolled PENDING + unverified accounts qualify. Legacy ACTIVE, any ACTIVE, SUSPENDED, verified, new/recent accounts and accounts with social/professional/support history are excluded. Latest noncancelled verification delivery must be SENT and ACCEPTED/DELIVERED; queued/failed/dead/bounced/never-accepted accounts are protected. Acceptance is not inbox proof, so operators must also pause for broad delivery incidents, including incidents affecting mail already accepted by the gateway.

Set GREENOCEAN_PENDING_CLEANUP_ENABLED=true with _DRY_RUN=true first; review counts and exclusions. Set _DRY_RUN=false only after operational review. _PAUSED=true stops selections. Each selected user is locked with SKIP LOCKED, eligibility is rechecked and existing transactional erasure is reused. Temporary challenges/outbox/preferences/eligibility/policy/session rows are removed, profile is anonymized and user becomes a DELETED tombstone for FK integrity. This is anonymization, not physical deletion of every row. Existing deletion completion marker remains. Durable hashed rate-limit windows are not directly reversible to an email; the existing worker removes windows older than expiry+1day. Failed erasure rolls back, logs failure count and retries on the next scheduled pass. No cleanup was enabled against real accounts.

## Native and assistive-technology checklist — NOT EXECUTED

Web export and browser assertions do not substitute for Android/iOS devices or screen readers. On each native platform test: six-digit and leading-zero entry, OTP autofill/paste including Persian digits, keyboard/LTR credentials inside RTL, TalkBack/VoiceOver labels/focus/error announcements, dynamic text, contrast and tap targets; resend cooldown/background/resume/client clock changes, offline timeout/retry, close/reopen, correction with password and all-device session revocation, delayed old mail rejection, region restrictions after verification, public resources while pending. Repeat real provider test only with authorized mailboxes. Record OS/device/app build and observations before changing D07 to fully tested.
