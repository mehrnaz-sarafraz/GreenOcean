# Phase M delivery — validation in progress

Latest source was refreshed from GitHub before M3. Main remains A–K at30a4b3a. L3 d09fbd4, M1 77c338c and M2 e4a401a are the actual validated dependency stack; the stale local copy was not used as the implementation base. PR13 targets L3; PR14 targets M1; M3 targets M2. No merge/deployment is performed.

M1 delivers durable PUBLIC/PRIVATE/HIDDEN privacy, private proposals, exact independent MFA approval and atomic activation with safe historical migration. M2 delivers rules-based public/private membership, target-bound seven-day invitations, roles/bans/restrictions, accepted ownership transfer, roster privacy and current group contributions. M3 adds versioned text activities, serialized capacity, host removal/closure handling, exact scoped content restrictions, independent appeals, purpose-limited platform report/lifecycle review, data rights and English/Persian UI.

Validation is not yet final for M3. See test-evidence for actual completed executions and failed environment attempts; do not infer success from implementation. Final acceptance requires the complete backend, frontend and real-stack CI checks against the pushed M3 head. Physical devices and actual legal/moderation/infrastructure approval remain external blockers. All newly introduced operationally gated writes remain default off.
