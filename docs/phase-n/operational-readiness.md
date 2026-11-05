# Operational readiness and recovery
Production gates are independent and default off: underlying I live support; N processing approval/reference/retention; duration; scheduling; booking; queue; extension; follow-up; actual purge. Applying migrations alone activates none. Owner metadata retention is 30 days; that does not approve clinical personal notes, which remain disabled.

| Failure or change | Required response and recovery |
|---|---|
| No trained listeners / staffed window withdrawn | Refuse new queue admission; withdraw affected proposals; terminate waiting if no qualified staffed window remains; show alternatives |
| Brief readiness gap between sessions | Preserve only existing original wait; clearly show no current readiness; no new admission from staffing/calendar records alone |
| Queue / schedule database unavailable | Return recoverable failure; never invent an entry, booking, ETA or confirmation; retry the same intent key after recovery |
| Notification provider unavailable / delayed | State remains queryable; generic optional notices do not authorize actions; dedupe by owner/entity/kind and discard stale delivery windows |
| Booking handoff interrupted | Read persisted booking and unique I request; no replacement chat; I offer/activation/presence/reconnect deadlines remain authoritative |
| Listener/member disconnects | I interrupts and blocks sending; reconnect cannot reset the planned end or approve extension while interrupted |
| Qualification, account, block or region revoked | Existing I safety invalidation wins; pending booking/queue reconciliation withdraws access; no fake reassignment |
| Missing member / listener | Reconcile distinct check-in/readiness/no-handoff outcomes after ten-minute grace; no penalty or inferred emergency intervention |
| Backend / worker restart | Re-read PostgreSQL admission, proposal, original deadlines and source link; repeat reconciliation idempotently |
| Timezone error | Pause affected future discovery; never move confirmed UTC instants; explicitly cancel/reschedule affected reservations |
| Duplicate reminders | Verify dedupe ledger; recipient preference opt-out must not starve another recipient's pending notice |
| Booking shutdown | Stop new booking writes, cancel pending bookings and pending source offers; captured active sessions retain their bounded I lifecycle |
| Queue shutdown | Cancel WAITING/OFFERED; valid already-transferred I requests remain independent; no hidden requeue |
| Underlying I disabled / emergency shutdown | End open I activity for service safety; no message, extension, queue or booking can bypass it |
| Supervisor unavailable / complaint | Use actual staffed incident procedure and existing report/block/end controls; do not claim an automatic emergency escalation service |
| Retention job delayed | Inspect eligible backlog/last success; restore approved cadence/capacity; replay bounded batches; verify no open commitment was purged |

The staff dashboard uses current role and recent MFA. It shows aggregate queue/bookings, current readiness, captured active-session counts, observed worker successes, retention eligibility and alert conditions. It contains no conversation text, feedback answers or other queued identities. A last success is evidence of reconciliation, not a promise of future health. Alert conditions are visible signals only: **real alert delivery is not configured or verified** by this implementation. No real recipient or delivery drill is fabricated.

Keep region/public urgent resources and non-listener support choices independent. Do not present the queue as emergency care or 24/7 service. Use consistent flags and migrated application versions across all service instances before a pilot.
