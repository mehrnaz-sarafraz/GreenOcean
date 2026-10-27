# Phase D initial targeted inspection

Baseline: C `9aaacc1` (tested executable code `82b3919`, 188 passing tests, CI 40362730975), stacked on B `b370ea0` and A `745c24d`; main remains `ab44bc1`. PRs #1–#3 are open drafts. Actual migrations end at V35. Only email verification, delivery, pending access/correction and retention are in scope.

| ID | Initial classification | Actual evidence / missing work |
|---|---|---|
| D-01 | COMPLETE_PARTIAL | SecureRandom/6 digits exist; concurrent resends can leave competing challenges; locale-independent leading zeros need evidence |
| D-02 | COMPLETE_PARTIAL | AES-GCM and email-bound HMAC exist; same derived key used for both; no key ID/rotation support; exhausted failures retain secrets |
| D-03 | REUSE_BUT_ADD_TESTS | Server SQL enforces 15 minutes; boundary/client-clock/delayed delivery tests incomplete |
| D-04 | MODIFY_DEFECTIVE | Atomic consume and activation transaction exist; user status not checked before consuming; concurrent resend/confirm needs account locking |
| D-05 | COMPLETE_PARTIAL / BLOCKED_EXTERNAL | Outbox/gateway exist; network occurs inside DB transaction, immediate retries, unused sender, no lease/outcomes/callback/operational evidence; no real provider credentials or approved sender supplied |
| D-06 | COMPLETE_PARTIAL | Durable account/email/IP limits exist; verification/resend budgets fixed; resend cooldown and correction-stable budget missing |
| D-07 | COMPLETE_PARTIAL | Existing localized C activation screen works; missing masked destination, no-mail guidance, correction and cooldown/focus evidence |
| D-08 | MODIFY_DEFECTIVE | Resend invalidates codes, but no serialized generation and already-verified accounts still receive codes |
| D-09 | MODIFY_DEFECTIVE | C checks adult/country/current Terms; suspended/deleted confirmation can falsely report success; preserve separate regional service checks |
| D-10 | REUSE_BUT_ADD_TESTS | Existing AccessPolicy/interceptor deny PENDING; need negative HTTP/DB evidence for each action category |
| D-11 | IMPLEMENT_MISSING | No pending-owner email-correction endpoint/UI or reauthentication/audit/invalidation flow |
| D-12 | IMPLEMENT_MISSING | No abandoned-account job; reuse existing erasure, exclude legacy/active/suspended/history, bounded locks and outage pause. Owner approved 7 days since last verification activity on 2026-10-26; default dry-run/disabled until operator enables. |

Plan: one additive migration; extend existing assistance, cipher and mail worker; add pending correction and bounded scheduled cleanup using existing erasure; extend activation UI and existing tests/CI. Do not replace tokens, account state machine, public pages, geography or policy acceptance.
