# Targeted live support privacy and threat review

See [product contract](../product/live-peer-support-contract.md), [state machine](state-machine.md), [API](api-contract.md), [E/F session policy](../security/authentication-session-policy.md) and [G/H privacy contract](../product/profile-and-content-privacy-contract.md).

| Threat | Enforced mitigation / evidence |
|---|---|
| Fake online flag / offline fallback | Existing Boolean never qualifies a live offer; approved application + all active modules + independent staff review of practice/current published code + active eligible account + ready current authenticated session/version + fresh lease + free capacity |
| Self-authorized or forged listener | Client cannot control another principal; practice endpoint retains staff role/admin enablement/recent MFA, rejects self-review; self-completed practice alone does not qualify |
| Stale multi-device presence | Lease rotation, session ownership, token version and expiry; old heartbeat/off switch rejects without altering new lease. Revoked session removes readiness evidence |
| Premature or fake chat | REQUESTED/ACCEPTED hold no conversation; only requester activation after real listener acceptance creates a distinct service_kind LISTENER chat; legacy instant matching is 410 |
| Expired request accepted after scheduler delay | Database-clock predicates in the mutation, lazy reconciliation and scheduler; exact boundary loses acceptance; terminal rows cannot reopen |
| Multi-instance capacity / retries | PostgreSQL transaction advisory lock shared across processes, partial unique indexes on open member/listener rows, cross-role occupancy check, one unique linked chat, request/message retry keys |
| Block/suspension/qualification race | Safety-change triggers serialize with transitions; every read/send rechecks current eligibility, pair block and lifecycle. Message INSERT trigger independently rejects non-active/forged sending |
| Wrong participants / ordinary-DM reuse | Deferred constraint checks linked chat service kind and exactly its member/listener; personal DIRECT lookup excludes LISTENER; client never supplies service kind or sender identity |
| Disconnection / stale UI | Foreground-only polling/pulses, readiness stopped on background, active stale → INTERRUPTED, both fresh pulses required to resume before bounded timeout; confirmed-state composer disables on failed refresh; no crisis inference |
| Old private data in polling/export | Safety-ended/blocked/unlinked legacy live chats fail closed. Completed authorized history is preserved; export copies only the owner's authored listener messages. Auth epoch/remount isolation is reused |
| Report evidence overreach | Existing participant-authorized report route, no automatic transcript collection and no broad staff read API; original privileged controls remain intact |
| Sensitive preferences / telemetry | No health topics copied or accepted in new requests, no diagnosis, ranking or tracker; no message bodies/identity payloads in new analytics; no aggregate telemetry pipeline introduced |
| Deployment without real humans | Feature flag false by default; V41 enables no country, role, roster or presence. Missing full listener code, independently approved roster, supervision/staff coverage and retention decisions are release blockers |

The advisory lock deliberately serializes short live-support database transitions in this MVP. It avoids process-local race assumptions at the cost of throughput; measure contention before scaling. No external network call or human wait occurs while the lock is held. Existing hashed rate-window infrastructure joins the request transaction so no second connection is held waiting on its own lock. Partial indexes and message/participant constraints remain independent safeguards. Deadlocks/DB errors roll back the transaction rather than leaking capacity; production retry/error monitoring remains an operational responsibility.

A heartbeat is authenticated recent device activity and explicit readiness, not proof of sustained human attention or a promise of acceptance. Previously received content cannot be recalled. No absolute anonymity, E2EE, clinical confidentiality, GDPR certification, 24/7 staffing or emergency intervention is claimed. Physical mobile backgrounding, staff operations, production-scale lock timing and retention review remain external evidence gaps.
