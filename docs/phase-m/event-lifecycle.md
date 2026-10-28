# Text activity lifecycle

The existing community_events and attendance tables remain authoritative. TEXT_CIRCLE, WORKSHOP and Q_AND_A are text activities; VIDEO/AUDIO creation and registration are unavailable. No conferencing, unrestricted group chat, recording, clinical service or LIVE state is introduced. Historical transport metadata remains preserved, not represented as functioning transport.

Current managers with acknowledged rules create future schedules and edit exact revisions. Updates require a future scheduled event, valid UTC instants, end after start, positive optional capacity and a local safety ALLOW. Capacity cannot fall below committed attendance. All timestamps are stored as timestamptz and rendered in device timezone; ISO entry includes the offset. No date is silently fabricated.

Every attendance, membership mutation and lifecycle mutation locks the group before the event. The event row serializes last-seat capacity across instances; retries do not duplicate attendance. Registration requires ACTIVE group, current eligible unbanned/unrestricted membership, rules acknowledgment, future start, supported format and current authorized host. Blocking the host denies registration. Leaving attendance remains available without a new publication permission. Ordinary members never receive attendee identities.

SCHEDULED -> CANCELLED by manager; SCHEDULED -> COMPLETED only after the actual end time. These are versioned, audited transitions. Suspension/archive cancels scheduled events and revokes pending invitations/requests/transfers. Host removal cancels hosted schedules and removes the removed account's attendance. No automatic revival after restoration. A host who is merely demoted cannot accept new registrations until a valid manager handles the event.

Managers can submit one explicitly identified COMMUNITY discussion prompt. PostService performs ordinary L identity, body safety, current rules, block and audience checks. Pending/blocked prompt text is never published merely because the event exists. Replies reuse existing post/comment APIs; no separate transport. Discussion links still revalidate current post authorization.

New scheduling/registration/discussion stays default-off under greenocean.groups.activities-enabled. Tests explicitly enable the feature on synthetic data. Cancellation, privacy reads and rights workflows are not treated as approval for production launch.
