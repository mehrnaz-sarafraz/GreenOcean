# Phase A threat model

Target review frameworks: OWASP ASVS 5.0 and NIST SP 800-63B-4; these are review targets, not certifications.

| Threat | Control | Residual risk / launch condition |
|---|---|---|
| Client-side age/status/role forgery | Mandatory backend attestation, immutable trust method from public payload, live account/role checks, professional review | Self-attestation can be false; legacy grace temporarily permits unknown eligibility |
| Geographic ID manipulation or unapproved care | Reference validation, separate permission rows, default closed services, conversation service kind | Residence/current country remain user-declared; approved markets and professional scope needed |
| Code guessing/offline DB attack | Email-bound HMAC, durable attempts, IP throttle, expiry/single use, AES-GCM outbox | Deploy separate stable encryption key and manage rotation; distributed IP attacks remain limited by account budget |
| Refresh replay/races | Row lock, retired-token history, committed family revocation, client single-flight | Theft can cause forced sign-out; no prevention of XSS-based actions |
| Browser token theft/CSRF | Memory-only access, HttpOnly refresh cookie, strict Origin/custom-header gate, exact HTTPS prod origins | XSS remains a risk; same-site deployment required, actual browser cookie flow needs E2E |
| Password truncation/weakness | New PBKDF2 hashes, legacy BCrypt verification/upgrade, 15–128 code points, known-common checks, configurable offline SHA-256 corpus | Default development list is small; production refuses startup without a configured corpus |
| Cross-account deletion/exposure | Subject-derived owner, password reauthentication, existing ownership checks, deleted profile exclusion, data-provider isolation | Backup/report retention and real multi-user browser/device validation remain operational work |
| Privileged takeover | Existing RBAC plus default disabled admin API, production prohibition until MFA integrated | MFA integration is intentionally still a launch blocker |
| Delivery/stored-secret leakage | HTTPS-only trusted gateway, no redirects, authorization secret, encrypted payload, no provider/secret logging, purge | Gateway must be idempotent, private and configured; real deliverability not tested |

No automated dangerous-content detection is claimed. Existing reports/manual review are reused. Sensitive SQL/provider errors are not returned publicly or logged with exception contents. Test artifacts contain only synthetic data. No production user or public service was attacked.
