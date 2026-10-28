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
Queue sequence is pending the N2 milestone. The existing Phase I acceptance, activation, presence, interruption and safety rules remain authoritative.
