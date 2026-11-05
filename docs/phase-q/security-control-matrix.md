# Q2 security control review

Scope: source descended from Q1 `62c60dcdd08c4253d699083a68cd7413fde292da`; current Q2 changes and additive V86. This is an internal implementation review, not a penetration-test certificate or permission to launch. The exact final source and CI result belong in test-evidence.md.

|Control / reference|Implementation and executable evidence|Residual / gate|
|---|---|---|
|ASVS 5.0.0 authentication, session management|Existing SessionPolicy/RecentAuthentication, current database account/session checks; Q2 role-removal transaction revokes sessions and bound grants. PhaseEFSessionIntegrationTest, PhaseEFMfaBoundaryTest, PhaseQ2SecurityTest, PhaseQ2ConcurrencyTest|Actual staff enrollment, custody and independent identity review required|
|ASVS authorization; API Top 10 2023 API1/API3/API5|Object ownership in source services; current explicit Q1 domain roles; no implicit ADMIN clinical access; exact revision and two independent administrators for anonymous attribution|See authorization-matrix.md; a finite regression suite does not prove all future routes|
|Sensitive evidence access|SocialAttribution and existing ClinicalCare, session/version/policy binding, bounded expiry, independent approval, one-time social reveal, durable withdrawal and revocation|No emergency override, unrestricted database browsing or broad staff data export is introduced|
|Cryptography/storage|Existing AES-256-GCM envelope with key identifier, random nonce and purpose AAD; duplicate identifiers now rejected; cipher and Q2 tests cover rotation, wrong owner, tampering and missing key|Production key provisioning, access separation, backup custody and rotation drill NOT_EXECUTED|
|Resource abuse / API4|Existing source budgets, pagination and timeouts; BoundedJsonFilter caps JSON streams at 1 MiB, including +json and unknown-length counting; no rejected-body echo|Not comprehensive remediation for parser CVEs; inspect dependency residuals|
|Privacy observability|Default-off fixed-dimension in-memory SafeTelemetry; tests deny payload, identity and URI labels|No third-party collector approved; operational alerting belongs to Q3|
|Dependency review|Fresh Q1 CI artifact verified by SHA-256; same Maven graph; compatible npm patches with integrity verification|Residual advisories remain; final Q2 fresh CI and accountable review required|
|Native MASVS 2.1.0|Shared application controls and source review only|Physical Android/iOS, secure storage, screenshots, logs, lifecycle and network interception NOT_EXECUTED|

Reference versions checked 2026-11-04: [ASVS 5](https://asvs.owasp.org/), [API Security 2023](https://owasp.org/API-Security/editions/2023/en/0x11-t10/), [MASVS 2.1.0](https://github.com/OWASP/masvs/releases/tag/v2.1.0). These are review references; no blanket compliance claim is made.
