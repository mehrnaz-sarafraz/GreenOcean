# Phase A requirements and audit

Audit base: `origin/main` at `ab44bc1`, fetched 2026-10-26. This supersedes older project-memory registration requirements. The full supplied contract is retained in [the approved specification](phase-a-approved-specification.md).

| Requirement | Initial classification | Evidence in original code | Implemented change | Remaining verification / decision |
|---|---|---|---|---|
| A-01 age and eligibility | Missing | `RegistrationService.register` checked future birth year only; frontend allowed 16+ | Adult attestation mandatory; optional legacy birth year rejects known minors; separate age result/method; centralized interactive access policy; anonymous curated resources | Self-attestation cannot establish actual age. Independent provider is not connected. Legacy grace is an explicit transitional risk. |
| A-02 geography/services | Missing | `RegisterRequest.countryCode` accepted any two letters; city was free text | Versioned ISO catalog with localized names, subdivision/city relationships, server search and validation, per-service regional availability defaulting closed | Approved pilot countries, licensed/versioned subdivision and GeoNames imports, regional operational review |
| A-03 basic/professional roles | Partially Implemented | Registration assigned USER but saved PROFESSIONAL intent. `ProfessionalWorkflowService.apply`, document storage and human review already existed; listener training existed | Removed professional selection from signup, reuse existing workflows, role changes checked against live database, admin routes disabled by default | Production administrative MFA is not implemented: prod startup refuses enabled admin routes |
| A-04 account lifecycle/deletion | Implemented but Unsafe | Status model and password-confirmed erasure existed; verification codes were unkeyed, outbox had no sender; account initially ACTIVE | New registration PENDING; protected expiring outbox, gateway transport, durable attempt limits, verification activation, refresh replay handling; additional erasure and retryable file cleanup | Real email delivery, backup retention operations and safety-record retention sign-off |
| A-05 minimal signup/privacy | Implemented but Unsafe | All profile fields required; Terms acceptance client-only; public profile/default open messages; BCrypt 72-byte issue; web localStorage | Minimal signup, random display identity, server version acceptance, private defaults, first-contribution review, compatible PBKDF2 upgrade, offline breach-list support, HttpOnly web refresh sessions | Configure a maintained offline compromised-password corpus; validate browser/mobile/device flows |
| A-06 service boundaries | Partially Implemented | Verified professional checks, listener training, urgent directory and reporting already present | Regional service gate, explicit conversation service kind and separate public resources/boundary copy | Professional care market/scope approval, UI/device and operational verification |

## Preserved existing capabilities

Session ownership, password-confirmed deletion, post deletion, profile/privacy controls, professional applications and credential scanning/review, listener coursework and review, published policy documents, reporting and appeal workflows, verified crisis-resource directory. No historical V0–V30 migration is modified.

## Scope distinctions

Account deletion, post deletion and message deletion are separate operations. Dedicated message-for-me and message-for-everyone APIs were not present and are not introduced here. Their follow-up needs membership/ownership checks, sender-only global deletion policy, recipient tombstones, report preservation, unread counters, attachment cleanup, offline synchronization and concurrency tests.

## Readiness

The implementation is a reviewable Phase A candidate, **not a completed production release**. External market, email, MFA, reference-data and retention decisions remain. No legal-compliance or independent age-verification claim is made.
