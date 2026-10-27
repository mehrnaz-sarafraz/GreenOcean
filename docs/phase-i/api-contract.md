# Phase I API contract

All routes use existing bearer/HttpOnly-to-bearer session handling, `/me` identity and A–H eligibility. No request body supplies authoritative member/listener status, sender, participant list or service kind. All normal responses remain `Cache-Control: no-store`. The configured live flag defaults false; region approval alone never enables live support.

| Method / route | Authority / effect |
|---|---|
| GET `/api/v1/listener-support/availability` | Eligible member sees enabled flag, configured offer seconds and currently eligible, fresh, ready, unoccupied listeners; no request created |
| GET `/api/v1/listeners` | Existing discovery route now derives readiness and independent eligibility; returns no roster while disabled; old online Boolean and topic matching are not authority |
| POST `/api/v1/listeners/match` | Retired, 410 `CLIENT_UPGRADE_REQUIRED`; never creates/reuses a conversation |
| GET/PUT `/api/v1/listeners/me/availability` | Own state; PUT `{ready,leaseId}`. Explicit true rotates to this session; false must match current owner session and lease. A stale device cannot switch off the new owner's readiness |
| POST `/api/v1/listeners/me/heartbeat` | `{leaseId}`; authenticated owner session/version and unexpired lease only; cannot revive an expired lease |
| POST `/api/v1/listener-support/requests` | `{clientRequestId: UUID, listenerId?: UUID}`; no topics required or accepted. Same owner/key returns original outcome; different target with same key conflicts |
| GET `/api/v1/listener-support/requests/me` | At most 20 own latest participation states, no other member's requests |
| GET `/api/v1/listener-support/incoming` | Only qualified targeted listener; at most one open offer/session |
| GET `/api/v1/listener-support/requests/{id}` | Participant-only current state, server timestamps and safe peer identity |
| POST `/api/v1/listener-support/requests/{id}/accept` | Target listener only; strict `expires_at > clock_timestamp()` in the state-changing SQL; no conversation |
| POST `/api/v1/listener-support/requests/{id}/decline` | Target listener, REQUESTED only, idempotent once declined |
| POST `/api/v1/listener-support/requests/{id}/cancel` | Requester only, REQUESTED/ACCEPTED, idempotent terminal acknowledgement |
| POST `/api/v1/listener-support/requests/{id}/activate` | Requester only after acceptance and before activation deadline; one atomic LISTENER conversation and exactly two members |
| POST `/api/v1/listener-support/requests/{id}/heartbeat` | Current eligible participant foreground pulse; cannot activate an offer or reopen terminal session |
| GET `/api/v1/listener-support/sessions/{id}` | Participant state; ID is the durable support request/session ID |
| GET `/api/v1/listener-support/conversations/{id}` | Participant projection for a linked LISTENER conversation; ordinary DM cannot masquerade as a live request |
| POST `/api/v1/listener-support/sessions/{id}/end` | Either participant ACTIVE/INTERRUPTED → COMPLETED; repeat returns recorded terminal outcome |
| PUT `/api/v1/admin/listener-live-authorizations/{applicationId}` | Existing admin enablement + current staff roles + recent privileged MFA; `{practiceApproved,conductVersion}`. Independent actor cannot be the listener; requires approved application/coursework and a full published listener code |

Existing messages API is reused. For a LISTENER chat, POST `/conversations/{id}/messages` requires `{body,clientMessageId: UUID}`, max 5000 characters, active authorized pair and live session. Same key/body returns the original message; same key/different body conflicts. Missing key is rejected. New outgoing live messages stop during interruption, after completion, blocking or authorization change. Ordinary/direct/professional/group messaging retains its service rules. `ConversationResponse` adds `serviceKind` and optional `supportRequestId`; clients must not infer service kind from display text or `DIRECT` alone.

Responses expose ID, current status, reason code, trusted server/deadline timestamps, optional linked conversation ID, safe peer name/ID, listener-side Boolean and session timing bounds. No health topic/preferences/transcript is included in request state. State polling itself does not accept/activate a request. Request/availability response is a snapshot, not a guarantee that a human will accept.

Stable error codes include `LIVE_SERVICE_DISABLED` (503), `NO_AVAILABLE_LISTENER`, `REQUEST_ALREADY_OPEN`, `LISTENER_BUSY`, `STALE_LEASE`, `REQUEST_EXPIRED`, `REQUEST_CANCELLED`, `INVALID_TRANSITION`, `IDEMPOTENCY_CONFLICT`, `SESSION_NOT_ACTIVE` (409), `REQUEST_NOT_FOUND` (404), `SERVICE_FORBIDDEN`, `LISTENER_NOT_AUTHORIZED`, `ACCEPT_FORBIDDEN`, `ACTIVATION_FORBIDDEN` (403), `MESSAGE_KEY_REQUIRED` (400) and `RATE_LIMITED` (429, Retry-After 60). Earlier session/region interceptors preserve their existing safe error envelopes. Unknown or private target state is never returned to a nonparticipant.

The existing report target `CONVERSATION` uses participant membership, including ended sessions; report creation does not automatically copy messages or open unrestricted staff transcript access. Existing bidirectional `/social/blocks/{id}` is reused. Export adds own participation metadata and **own authored** listener messages, not a second path to another participant's blocked/revoked body. Erasure removes new request/presence/authorization rows through the existing lifecycle.
