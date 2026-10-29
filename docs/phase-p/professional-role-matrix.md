# Roles and purposes
| Actor | Allowed | Denied |
|---|---|---|
| Applicant | Own application, encrypted uploads, self-declared profile and private schedule drafts | Self-verification, editing verified identity/specialties |
| Current independent moderator + recent MFA | Revision-bound credential review and purpose-audited document inspection | Platform release approval, unrelated clinical narratives |
| Current administrator + recent MFA | Recording actual bounded country/modality release evidence | Fabricating licensing or provider authorization |
| Current qualified professional | Publishing offers only with all platform/current-region approvals | Prescribing, unapproved modalities, patient-private wellbeing data |
| Appointment participant | Own opt-in reminder and short-lived authenticated readiness | Another patient's appointment or readiness |

Clinical/financial/editorial authority remains separate. E/F privileged interception supplies recent strong MFA; private document content additionally checks recent authentication explicitly.

## P3 clinical and financial separation

PROFESSIONAL is added only after the independent qualification decision and enables the existing security-key enrollment pathway; the role alone does not authorize care. Professional, CLINIC_FINANCE and CLINICAL_RECORDS_OPERATOR sessions use privileged lifetime bounds and the existing independently permitted FIDO enrollment/reauthentication. Qualification changes invalidate stale role-claim JWTs; clients must refresh or sign in.

| Actor | Clinical narratives | Finance | Public clinical notice review |
|---|---|---|---|
| Patient | Own approved categories; fresh password for export; review path for restricted categories | Own invoices, receipts, obligations | Can choose own exact version only |
| Assigned current professional | Current consent, scope, policy window and recent key verification required | Own earned/payable/held metadata | No implicit publishing authority |
| ADMIN | No universal narrative access | Authorized operations with recent MFA | Independent author/reviewer only |
| CLINIC_FINANCE | None through financial role | Purpose-limited operations with recent MFA | None through finance role |
| CLINICAL_RECORDS_OPERATOR | Independent time-limited category/encounter grant and recent MFA | None through records role | None through records role |
| Moderator, listener, educational editor | None through these roles | None through these roles | None through these roles |


P4/P5 purpose roles: CLINIC_REPUTATION_OPERATOR reviews raw feedback and independent recognition; CLINIC_OPERATIONS reviews nonnarrative incidents/reminder dispatch; CLINIC_PRIVACY_OPERATOR reviews category rights and bounded holds. All require current account authority and recent security-key authentication. None receives clinical narrative access by role. ADMIN sees aggregate business/configuration; CLINIC_FINANCE sees financial operations; direct records require participant authorization or a separately approved expiring CLINICAL_RECORDS_OPERATOR grant. Case/feature/hold self approval is rejected. Legacy professional generic messaging cannot start new care or send messages; new care uses bounded encounters.
