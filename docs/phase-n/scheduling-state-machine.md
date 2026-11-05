# Scheduling state machine
Slots are derived from current qualified calendars, exceptions, immutable reservation intervals and open live commitments. They are not persistent claims of readiness.

CONFIRMED → CHECKED_IN: owning member, server check-in window. CHECKED_IN → HANDOFF_REQUESTED: owning member, appointment reached, actual readiness, unique Phase I request. HANDOFF_REQUESTED → ACTIVE: existing Phase I request activates; ACTIVE → COMPLETED or CANCELLED_BY_SERVICE follows Phase I termination. Either participant may cancel a pending reservation; active sessions use Phase I end controls. All other terminal rows remain terminal under the database transition guard.

CONFIRMED without check-in becomes MEMBER_NO_SHOW at the grace deadline. CHECKED_IN without a handoff becomes LISTENER_NO_SHOW. Eligibility/qualification/blocks create CANCELLED_BY_SERVICE rather than blaming the member. A declined/expired linked offer becomes EXPIRED. No automatic rebooking or reassignment occurs.

Each booking state transition creates a bounded-format event with status, reason code and timestamp, never conversation text. Sweep and read reconciliation recover persisted state after restart. Client keys uniquely identify the original reservation intent.
