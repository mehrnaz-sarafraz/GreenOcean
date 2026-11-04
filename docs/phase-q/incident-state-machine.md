# Incident lifecycle and classification

Default off: `GREENOCEAN_OPERATIONS_ENABLED=false`. Incidents store opaque references, fixed service/kind/severity, actual actor/owner and a revisioned append-only timeline. They never copy source narrative. Idempotency keys are domain-bound and cannot be reused for different evidence/classification. Detection may retain owner=NULL; this means an unresolved staffing blocker, not fictional ownership.

Lifecycle: DETECTED → ACKNOWLEDGED → TRIAGED → CONTAINMENT → INVESTIGATING → RECOVERY_PENDING → RECOVERING → RESOLVED → POST_INCIDENT_REVIEW → CLOSED. Each transition requires exact current revision and opaque evidence reference. Initial acknowledgement assigns the current operator only with genuine acknowledged coverage and available shared capacity. Incident ownership and Q1 case assignment use the same database advisory assignment lock and capacity count. Ordinary transitions require the owner. Recovery requires a different currently staffed operator and a fresh fifteen-minute proposal bound to the proposing owner's live session/token version and current authority. Self-review and stale sessions fail closed.

Classification may be revised by the current owner during detection/investigation with a new exact-revision timeline event. Later new facts require a separately evidenced follow-up incident. An explicitly authorized staffing operator may take over a genuinely unavailable owner's open incident only with current personal coverage/capacity. Pending recovery is invalidated and returns to investigation; an old recovery approval cannot follow the new owner.

|Severity|Criteria / examples|
|---|---|
|SEV1|Critical confidentiality, authentication compromise, corrupted obligations or essential broad outage; one serious confidential exposure can qualify|
|SEV2|Major consequential service failure, overdue refund processing, incorrect staffed availability, substantial provider disruption|
|SEV3|Limited but consequential degradation requiring accountable review|
|SEV4|Minor operational issue with limited impact|

Severity is a human/source technical classification, never a count-only psychological-risk score. Fixed kinds cover data exposure, credential compromise, database/email outage, payment callback, refund delay, staffing gap, media interruption, visibility regression, key configuration, service failure and backup failure.

Containment proposals bind incident revision, author session/version, domain, control, target and reference. A second currently staffed operator with the affected service purpose approves. Approval uses a shared/exclusive database admission lock: earlier admitted transactions may finish before the pause commits; later admissions fail closed. Independent incidents retain independent holds; one recovery cannot reopen another's restriction. Pauses do not silently expire. RESUME needs independently authorized recovery and releases only that incident's hold. Runtime feature flags and other service approvals are never enabled automatically.
