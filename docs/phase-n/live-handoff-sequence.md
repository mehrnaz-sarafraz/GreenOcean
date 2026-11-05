# Live handoff sequences
## Immediate
```mermaid
sequenceDiagram
 Member->>Server: Explicit Phase I request
 Server->>Listener: Genuine targeted offer
 Listener->>Server: Accept
 Member->>Server: Activate
 Server->>Server: Create one listener conversation
```
## Scheduled
```mermaid
sequenceDiagram
 Listener->>Server: Publish qualified calendar
 Member->>Server: Reserve an available slot
 Server-->>Member: Confirm immutable appointment
 Member->>Server: Check in during server window
 Member->>Server: Request genuinely ready listener at start
 Server->>Listener: Existing Phase I offer
 Listener->>Server: Accept
 Member->>Server: Activate
 Server->>Server: Create one listener conversation
```
## Queue
```mermaid
sequenceDiagram
 Member->>Server: Explicit bounded queue admission
 Server->>Server: Verify staffed qualified actual coverage
 Server->>Member: Persisted short-lived listener proposal
 Member->>Server: Confirm exact proposal readiness
 Server->>Server: Atomically create I request and mark TRANSFERRED
 Server->>Listener: Existing Phase I offer
 Listener->>Server: Accept
 Member->>Server: Activate
 Server->>Server: Create one listener conversation
```
The transfer commit is the independence point: later parent expiry cannot invalidate this I request. The existing Phase I acceptance, activation, presence, interruption and safety rules remain authoritative.
