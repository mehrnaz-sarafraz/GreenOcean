# Phase I operations and safe rollout

## Production remains disabled

`GREENOCEAN_LIVE_SUPPORT_ENABLED=false` is the default. No migration enables a country or creates real listeners/readiness. First approve the service and current-country/residence scopes through existing regional controls. Public urgent resources and permitted non-listener routes stay independent. No deployment/merge is part of this work.

The current seed contains a listener-code summary without full published content. This is **not** sufficient conduct evidence. Publish an owner-approved full code using the existing privileged policy workflow, not a guessed policy inserted by this phase. Review the existing application and completion of **all active** modules; supervised practice must be independently reviewed by authorized staff, not self-certified. Record approval of the applicable published code version in the dedicated staff live-authorization route under existing recent MFA. Preserve legacy applications/roles/history; they fail closed for new live matching until evidence exists. Test fixture policy version 900001 and test-only roster provisioning are synthetic and never part of the production jar or V41.

## Configurable technical timing

| Environment variable | Default | Allowed bound |
|---|---|---|
| GREENOCEAN_LIVE_SUPPORT_OFFER_SECONDS | 90 | 30–120 |
| GREENOCEAN_LIVE_SUPPORT_ACTIVATION_SECONDS | 60 | 15–90 |
| GREENOCEAN_LIVE_SUPPORT_HEARTBEAT_SECONDS | 15 | 5–30; freshness must be at least twice this |
| GREENOCEAN_LIVE_SUPPORT_PRESENCE_SECONDS | 45 | 30–120 |
| GREENOCEAN_LIVE_SUPPORT_RECONNECT_SECONDS | 120 | 30–600 after the actual stale-pulse deadline |
| GREENOCEAN_LIVE_SUPPORT_SWEEP_MS | 10000 | 1000–30000 |

Invalid settings fail startup. Offer/activation deadlines use DB clock, and presence/reconnect duration is captured in each request. Keep configuration identical across instances; a disabled instance conservatively terminates open live work. Lazy authorization makes a delayed sweep safe. REQUESTED also reserves the listener's single provisional slot for at most 90 seconds; this prevents parallel offers/harassment and does not create a queue. Available listeners are selected by oldest assignment in their current short-lived readiness lease, then UUID; no sensitive topic profiling or guaranteed fairness across expired leases is claimed.

Missing foreground pulses interrupt sending after 45 seconds; both participants may resume within 120 seconds from that stale deadline. Otherwise the session completes with CONNECTION_TIMEOUT, not a clinical emergency. Termination closes readiness; new live availability requires an explicit fresh action. Browser reload restores participant state, never recreates an offer or reopens an ended session. Repeated API operations are idempotent where documented. Client countdowns are presentational and cannot extend server deadlines.

## V41 migration and recovery

Only additive `V41__live_listener_acceptance_and_sessions.sql` is new. It adds independent approval evidence, session-bound presence, requests/session state, deadline/participant/capacity indexes, a nullable retry key on messages, and authorization triggers/functions. It rewrites no historical participants/messages or A–H migrations. Existing LISTENER service conversations with no linked accepted request are preserved but not automatically readable/active as new sessions; see Phase J review of legacy history access. DIRECT, PROFESSIONAL and GROUP services retain their service rules.

Rehearse fresh and populated V40 upgrades on sanitized isolated data; inspect index creation and DDL lock time at actual volume, take an approved recoverable backup and coordinate deployment. Deploy compatible backend/frontend together. Old instant-match clients receive 410. A prior backend without live messaging enforcement must not be used for rollback while live sessions exist. Safe emergency shutdown: disable the live flag consistently, let reconciliation safety-end open work and verify no new sending/readiness. Preserve authorized historical records and report handling; do not mass-delete history or relax auth to recover service. Forward-fix schema with a new migration. Transactional failed-migration recovery is tested with a **test-only** synthetic V42; it is not a real application migration.

## Human service launch checklist

- Owner approval for Model A timing expectations and published service boundaries.
- Genuine approved listener roster, practice/code evidence, capacity expectations and revocation process.
- Actual supervisory/incident-response staffing, coverage hours and report handling; no 24/7 claim without evidence.
- Approved regional services and applicable jurisdiction/legal review; no blanket country enablement.
- Qualified staff account security, recent MFA and admin operational enablement.
- Phase J decisions for request metadata, message/history access, report evidence, retention/backups, deletion and notices.
- Verify interruption, shutdown, report/block, revocation and account erasure on isolated rehearsal data.
- Run the native/manual matrix and production-scale contention/migration checks; web export is not a native device test.
- Review final-head CI and the A–H stacked dependency chain before any release.

## Measurement and retention decision register

I-16 aggregate instrumentation is intentionally absent pending an approved purpose, aggregation/access and bounded retention design. Existing security/report/approval records are reused; no third-party tracker or sensitive analytics payload is added. Presence is transient and deleted when expired; request lifecycle metadata is not claimed anonymous and remains owner data, not analytics. Existing messages/received copies, report evidence and security records retain existing policy boundaries. No new indefinite-retention policy, automated clinical escalation or destructive history purge is invented. Request metadata retention and real launch approval remain **NEEDS_OWNER_DECISION / Phase J**, with the service disabled in production until resolved.
