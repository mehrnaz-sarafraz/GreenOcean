# Phase N privacy and data lifecycle
Owner decision, 2026-10-28: retain completed N metadata for **30 days after completion**. Production automatic purge remains default-off; the operations report exposes its actual state. Processing approval, a policy reference and bounded category durations must all be explicit before N feature gates can operate. Test-profile approval is synthetic QA configuration, not production legal approval.

| Category | Purpose and access | Completion boundary | Rights and cleanup |
|---|---|---|---|
| Calendar rules/exceptions/settings | Listener-owned published availability and preference; members see only genuine derived slots | Explicit retirement or validity end; settings after their records/references end | Own archive/erasure; expired unreferenced availability removed under scheduling policy |
| Staffed windows | Independent approved queue operating coverage; current staff + recent MFA; listener export excludes reviewer identity | Revocation or window end | Listener erase and bounded expired-window cleanup |
| Booking/check-in/events | Participant reservation and truthful lifecycle; no topics | Last terminal booking event | Minimized own export; participant erase; terminal-only 30-day purge |
| Queue/proposal/events | Explicit bounded wait and auditable I transfer; only owning member sees queue data | Terminal queue result or transferred I completion, whichever is later | Own language/reminder choice and metadata export; erase invalidates allocation; terminal-only purge |
| Duration/extension/consent/warning ledger | Capacity deadline and matching participant consent | I completion | Authorized participant metadata export; source erasure cascades; expired N columns cleared only by guarded cleanup |
| Optional feedback | Voluntary service-experience answers, member only | I completion; edits do not renew the boundary | Owner correction/removal/archive; 30-day cleanup |
| Generic notice ledger | Preference-aware deduplication, no narrative | Parent lifecycle | Own ledger export; linked generic notifications removed with expired source records |
| Worker heartbeat | Non-personal last successful reconciliation time | Current operational state | Fixed bounded worker keys, no member identifiers |

No diagnosis, medication, inferred distress, private support topics, group memberships, journal text, personal note or transcript is copied into scheduling/queue/feedback. Matching reads none of those sources.

Cleanup is transactional, bounded to 100 rows per category per pass, idempotent and excludes live/open commitments. Dependency removal can make further calendar records eligible during a pass; dry-run counts describe current eligibility. Operators must monitor backlog and size the sweep cadence to the approved policy. The default half-hour cadence is configurable. No purge is claimed to have run in production.

N-only duration columns can be nulled only for terminal requests older than the configured approved boundary inside a transaction-local retention context. They cannot be reset on active rows. Underlying I participation, conversations and messages retain their separate existing J lifecycle; this change does not silently delete those historical records or export the other participant's private transcript. Previously delivered copies and backups cannot be recalled automatically. Production backup expiry and deletion replay require a real operational drill. Legacy generic notifications without an attributable source link keep their existing notification lifecycle rather than guessing associations.
