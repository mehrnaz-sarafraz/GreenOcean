# Actual live support state machine

```mermaid
stateDiagram-v2
    [*] --> REQUESTED
    REQUESTED --> ACCEPTED: Target listener accepts before deadline
    REQUESTED --> DECLINED: Listener declines or becomes unavailable
    REQUESTED --> CANCELLED: Member cancels
    REQUESTED --> EXPIRED: Acceptance deadline
    ACCEPTED --> ACTIVE: Member explicitly activates
    ACCEPTED --> CANCELLED: Member cancels / listener unavailable
    ACCEPTED --> EXPIRED: Activation deadline
    ACTIVE --> INTERRUPTED: Participant pulse stale
    INTERRUPTED --> ACTIVE: Both renew within reconnect limit
    INTERRUPTED --> COMPLETED: Reconnect limit or participant ends
    ACTIVE --> COMPLETED: Either participant ends
    REQUESTED --> ENDED_SAFETY: Authorization revoked
    ACCEPTED --> ENDED_SAFETY: Authorization revoked
    ACTIVE --> ENDED_SAFETY: Authorization revoked
    INTERRUPTED --> ENDED_SAFETY: Authorization revoked
```

Terminal rows never reopen. No conversation exists in REQUESTED/ACCEPTED. Conversation creation belongs to explicit activation. State GET is an authorized polling projection, not implied acceptance. Idempotent retries of accept/activate/cancel/end return the recorded outcome without creating another conversation. Deadline comparisons are strict: at the exact deadline acceptance loses. Presence and interrupted deadlines are snapshot configuration values.
