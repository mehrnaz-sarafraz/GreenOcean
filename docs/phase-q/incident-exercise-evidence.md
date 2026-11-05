# Controlled synthetic incident evidence

No production user, real payment, clinical provider or human on-call drill is used. Initial Q3 target35 tests passed; actual loopback HTTP delivery expanded this to44; source signal/ranking/hold refinements then passed52. The later Q2/P4/Q3 migration/concurrency regression passed90,0 failures/errors/skips. Subsequent fairness refinements require final validation in test-evidence.md. Seven Q3 rendered EN/FA/RTL browser cases passed. Actual-stack P4 and Q3 both passed after explicit listener-region fixture provisioning; the initial region-related403 is retained as a failed fixture attempt rather than counted as a pass.

|Exercise|Verified component evidence / integration|Limit|
|---|---|---|
|Credential compromise|Q2 session/role revocation and races; Q3 current-MFA ownership/route gates and containment|No real compromised account or external attacker exercise|
|Database unavailability|Migration rollback/retry, independent-connection lock races; database-dependent dispatch explicitly identified|Actual isolated database outage/restart and external probe timing are Q4 measurements, not claimed here|
|Email outage|Existing PhaseDMailGatewayTest actual HTTPS refusal/retry contract; Q3 metadata-only repeated-failure signal and signed HTTP alert tests|No production mail provider or real recipient|
|Payment callback failure|Existing P2 authenticated callbacks and financial invariant tests; Q3 PAYMENT_CALLBACK classification and new-initiation containment|No actual provider funds; callbacks/reconciliation/refunds are preserved during containment|
|Refund processing failure|Existing P2 refund state/provider-evidence checks; Q3 overdue unconfirmed refund detector deduplicates an unassigned incident|Synthetic overdue source record; not a legal deadline or actual financial receipt|
|Listener staffing gap|I/N actual qualification/readiness/coverage checks; Q3 real API/two-WebAuthn ownership, queue pause, preserved state/own archive and independent recovery|Virtual staff, not evidence of real coverage or24/7 operations|
|Clinical media interruption|P3 provider/consent/session admission checks; Q3 fixed technical-failure metadata and media-admission containment|No actual clinical audio/video call or provider SLA|
|Visibility regression|L/M/Q2 real JWT cross-object/source privacy regressions; Q3 independent source containment and no public evidence leakage|Application regression scope, not an external penetration test|
|Encryption key misconfiguration|Existing cipher/rotation/missing-key/tamper tests; Q3 KEY_CONFIGURATION classification|No real key custody or production rotation/restore claim|

Detection and lifecycle timeline use actual database timestamps and actors; alert receipt tests capture an actual signed HTTP request before provider2xx, then separately authenticate the designated recipient's acknowledgement. Retry/claim-recovery tests prove the protocol, not human staffing. Concurrent admission tests compare actual insertion time with committed containment authorization and reject all later admissions. Detailed load/failure/restore durations and RTO/RPO proposals belong to Q4; where a component exercise is not a complete end-to-end fault scenario, that distinction remains explicit.
