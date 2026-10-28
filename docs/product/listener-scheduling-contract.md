# Listener scheduling contract
A future booking reserves an opportunity with a currently independently qualified listener. It is not evidence of online presence or a promise of a conversation. No payment, penalty, clinical topic, auto recurrence, or inferred treatment relationship is created.

Policy is read from listener_support_policy: 30-minute session, two upcoming bookings, 14-day horizon, 30-minute minimum lead, check-in from five minutes before until ten minutes after start. The configurable five-minute break and 150-second Phase I handoff headroom make initial slot starts 40 minutes apart. The entire 30-minute session must fit before the next commitment, including a delayed start. A late handoff that would overlap is refused rather than shortened or reassigned.

Weekly rules use explicit IANA zones and local civil dates. Exceptions can add or remove availability. Pausing stops new discovery while preserving confirmed reservations. Editing/removing availability referenced by an upcoming reservation is rejected; explicitly cancel or honor it first. Changing the calendar timezone preference never moves saved rules or confirmed UTC instants.

Cancellation is immediate and penalty-free. Rescheduling atomically cancels the original and reserves the chosen replacement; if replacement fails, the original remains. Replaying a cancelled reservation key returns its terminal result.

At the appointment, the member checks in, explicitly requests a genuinely ready listener, waits for listener acceptance, then explicitly activates the existing Phase I conversation. No earlier step creates chat. Readiness must still come from the authenticated Phase I workspace lease.

All production Phase N gates default off. Operational approval, appropriate processing purpose/retention, staffing and regional authorization remain mandatory before real use.
