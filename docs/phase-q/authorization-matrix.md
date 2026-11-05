# Executable authorization review

Authorization is enforced by the source service and current database state. A hidden button is not evidence. Cross-user failures must preserve the original owner's data and omit private content. Anonymous display never authorizes staff attribution.

|Actor / object|Expected boundary|Executable source evidence|
|---|---|---|
|Anonymous / public policy, catalogs, eligibility|Public projections only; no private account or content|PhaseBPublicAccessIntegrationTest, PhaseAIntegrationTest|
|Unverified / eligible member workflows|Verification and current service eligibility required|PhaseDVerificationIntegrationTest, PhaseGHPrivacyIntegrationTest|
|Member and ten explicit staff roles / another member's private journal|GET and DELETE return 404, owner entry unchanged; no staff inheritance|PhaseQ2SecurityTest actual JWT requests for USER, ADMIN, MODERATOR and explicit operational roles|
|Expired/revoked/suspended identity / protected routes|401; stale JWT cannot acquire current role or session authority|PhaseEFSessionIntegrationTest, PhaseQ2SecurityTest|
|Member / another member's profile-private fields and export|Owner-only projection and archive|PhaseGHPrivacyIntegrationTest, ProfileAccountIntegrationTest|
|Nonmember, pending, removed, blocked member / private group|No discovery or membership-content bypass|PhaseM1PrivacyTest, PhaseM2MembershipTest, PhaseM3PrivacySurfaceTest|
|Listener and unrelated staff / support session|Assigned participant and current readiness/consent required|PhaseILiveSupportIntegrationTest, PhaseN2StaffingAccessTest, PhaseN3SessionsTest|
|Administrator / anonymous held source|No single-person reveal; fixed purpose, exact revision, independent current administrator, current MFA, current sessions; one use|PhaseL3ReviewTest, PhaseQ2SecurityTest, PhaseQ2ConcurrencyTest|
|Patient, treating professional / clinical record|Existing category, relationship, current consent and policy checks retained|PhaseP3Test, PhaseP5Test|
|Records operator / exceptional clinical record|Independent second authorized operator, exact revision, current policy, both live sessions and MFA; revoke/withdraw immediately denies|PhaseQ2ClinicalAccessTest; PhaseP3Test|
|Administrator / clinical record without source entitlement|Denied; ADMIN is not a substitute for clinical authority|PhaseQ2ClinicalAccessTest, PhaseP3Test|
|Professional, operator, patient / payments and incidents|Source-specific ownership and financial purpose; no clinical-text projection|PhaseP2PaymentsTest, PhaseP5Test, PhaseQ1SourceIsolationTest|
|Social/listener/clinical/financial/platform operator / trust case|Explicit current domain role, no narrative copying or implicit ADMIN role; second independent review|PhaseQ1Test, PhaseQ1SourceIsolationTest, PhaseQ1ConcurrencyTest|
|Platform trust operator / telemetry|Current explicit TRUST_PLATFORM plus recent strong authentication; others denied|PhaseQ2SecurityTest|

This maps executable fixtures, not an assertion that every actor/object cross-product has been run. Remaining external provider routes, device-specific storage, production identity provisioning and unrestricted infrastructure access require independent review. Q0 performs the final cross-phase completeness audit after Q1–Q5.
