# Targeted E/F baseline inspection

Baseline d135328 (D PR4, based C/B/A); tested executable ee4ca2c has254 passing tests, CI40372115704. Clean local tree, origin fetched; PR1–4 remain open drafts. New branch feature/phase-ef-account-access-security. D05 real provider and D07 native testing remain external/unexecuted. No other active local edits were present.

| Dependency | Classification | Targeted finding and planned files |
|---|---|---|
| Password encoder/policy, C normalization | REUSED_WITH_TARGETED_TESTS | PBKDF2 + legacy BCrypt upgrade retained; catch malformed modern hashes; all new password UI must use15–128 code points |
| Login/assistance services and D outbox | INCOMPLETE_REQUIRES_EXTENSION | preserve services; finish recovery links, status restrictions, safe event classification and UI |
| JWT/session live validator | REUSED_WITH_TARGETED_TESTS | signature/issuer/roles/revocation retained; add absolute/idle/session-scope enforcement |
| Refresh rotation/retired hashes | DEFECTIVE_REQUIRES_FIX | rotation currently extends30days repeatedly; user/session lock order and old-token logout need coordination |
| HttpOnly refresh + memory access token | INCOMPLETE_REQUIRES_EXTENSION | keep storage model; serialize refresh across tabs, offline-logout tombstone and explicit reconciliation |
| Security settings | DEFECTIVE_REQUIRES_FIX |12-character policy and false other-sessions-only success, no logout-all confirmation/proof; extend existing route |
| Private DataProvider | INCOMPLETE_REQUIRES_EXTENSION | user-keyed remount retained; reject stale epochs at API and provider boundary; local lock hides/unmounts private state |
| Privileged access | BLOCKED_EXTERNAL_DEPENDENCY | existing admin-disabled boundary retained; implement WebAuthn verification/enrollment where feasible; real RP/origin/keys/bootstrap approval absent, production remains disabled |
| Security events | INCOMPLETE_REQUIRES_EXTENSION | reuse audit_logs; fixed safe event types, bounded owner inventory, retention/monitoring |

Plan: V37 additive session backfill/WebAuthn tables; SessionPolicy/RecentAuthentication and existing AuthenticationService/validator; one existing auth/session architecture; shared password fields/recovery in existing info routes; existing Account & Security/session routes; privacy lifecycle at AuthProvider/DataProvider; targeted standards-backed tests, all regressions, stacked draft PR. Consumer passkeys E12 and optional consumer MFA E13 are approved deferred scope.
