# Phase P requirement coverage

Implementation and release acceptance are separate. Exact-head CI for P1–P4 passed. P5 evidence is tracked in test-evidence.md. Genuine approvals and physical native validation remain separate acceptance gates.

| Requirement | Implementation / evidence | Remaining gate |
|---|---|---|
| P01 — Profession eligibility | ProfessionalAuthority, CredentialDocuments, ProfessionalScheduling; P1 V67–73; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P02 — Independent qualification verification | ProfessionalAuthority, CredentialDocuments, ProfessionalScheduling; P1 V67–73; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P03 — Secure credential documents | ProfessionalAuthority, CredentialDocuments, ProfessionalScheduling; P1 V67–73; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P04 — Honest professional profiles | ProfessionalAuthority, CredentialDocuments, ProfessionalScheduling; P1 V67–73; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P05 — Recurring calendars | ProfessionalAuthority, CredentialDocuments, ProfessionalScheduling; P1 V67–73; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P06 — Ad hoc availability | ProfessionalAuthority, CredentialDocuments, ProfessionalScheduling; P1 V67–73; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P07 — Free consultation slots | ProfessionalAuthority, CredentialDocuments, ProfessionalScheduling; P1 V67–73; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P08 — Reminders and authenticated readiness | ProfessionalAuthority, CredentialDocuments, ProfessionalScheduling; P1 V67–73; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P09 — Real booking | ClinicCommerce, provider port/worker, immutable ledger; P2 V74–75; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P10 — Appointment lifecycle | ClinicCommerce, provider port/worker, immutable ledger; P2 V74–75; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P11 — Integer pricing and terms | ClinicCommerce, provider port/worker, immutable ledger; P2 V74–75; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P12 — Versioned commission | ClinicCommerce, provider port/worker, immutable ledger; P2 V74–75; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P13 — Administrative subscriptions | ClinicCommerce, provider port/worker, immutable ledger; P2 V74–75; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P14 — Provider-verified payment | ClinicCommerce, provider port/worker, immutable ledger; P2 V74–75; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P15 — Ledger and patient workspace | ClinicCommerce, provider port/worker, immutable ledger; P2 V74–75; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P16 — Evidence-based no-show and refund target | ClinicCommerce, provider port/worker, immutable ledger; P2 V74–75; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P17 — Refund disputes and reconciliation | ClinicCommerce, provider port/worker, immutable ledger; P2 V74–75; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P18 — Professional earnings and settlement | ClinicCommerce, provider port/worker, immutable ledger; P2 V74–75; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P19 — Versioned clinical consent | ClinicalCare/ClinicalFiles, category grants, media port; P3 V76–78; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P20 — Authorized text consultations | ClinicalCare/ClinicalFiles, category grants, media port; P3 V76–78; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P21 — Approved media adapter architecture | ClinicalCare/ClinicalFiles, category grants, media port; P3 V76–78; targeted tests and shared regression | Real approved media adapter and physical audio/video validation NOT_EXECUTED |
| P22 — Isolated clinical records | ClinicalCare/ClinicalFiles, category grants, media port; P3 V76–78; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P23 — Versioned encrypted clinical notes | ClinicalCare/ClinicalFiles, category grants, media port; P3 V76–78; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P24 — Protected clinical attachments | ClinicalCare/ClinicalFiles, category grants, media port; P3 V76–78; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P25 — Purpose-scoped clinical roles and rights | ClinicalCare/ClinicalFiles, category grants, media port; P3 V76–78; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P26 — Verified patient feedback | ProfessionalReputation, feedback and weekly operations; P4 V79–80; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P27 — Two-dimensional reputation | ProfessionalReputation, feedback and weekly operations; P4 V79–80; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P28 — Fair competitive ranking | ProfessionalReputation, feedback and weekly operations; P4 V79–80; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P29 — Auditable weekly recognition | ProfessionalReputation, feedback and weekly operations; P4 V79–80; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P30 — Earned recognition versus advertising | ProfessionalReputation, feedback and weekly operations; P4 V79–80; targeted tests and shared regression | Sponsored placement BLOCKED_POLICY_REQUIRED |
| P31 — Fraud and manipulation prevention | ProfessionalReputation, feedback and weekly operations; P4 V79–80; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P32 — Voluntary clinical follow-up | ClinicIntegration, category rights/follow-up/operations UI; P5 V81–83; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P33 — Purpose-limited operational dashboards | ClinicIntegration, category rights/follow-up/operations UI; P5 V81–83; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P34 — Commercial model flexibility | ClinicIntegration, category rights/follow-up/operations UI; P5 V81–83; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P35 — Iran legal release gates | ClinicIntegration, category rights/follow-up/operations UI; P5 V81–83; targeted tests and shared regression | Iran organizational/legal/clinical/provider approvals APPROVAL_REQUIRED |
| P36 — Security and misuse controls | ClinicIntegration, category rights/follow-up/operations UI; P5 V81–83; targeted tests and shared regression | Real purpose/platform/provider authorization before production |
| P37 — Privacy and category retention | ClinicIntegration, category rights/follow-up/operations UI; P5 V81–83; targeted tests and shared regression | Category lawful retention, backups and processor policies UNAPPROVED; automatic purge off |
| P38 — Notification integration | ClinicIntegration, category rights/follow-up/operations UI; P5 V81–83; targeted tests and shared regression | In-app notices implemented; native push receipt NOT_EXECUTED |
| P39 — QA and native validation | ClinicIntegration, category rights/follow-up/operations UI; P5 V81–83; targeted tests and shared regression | Synthetic automated QA; physical Android/iOS, real providers and production capacity NOT_EXECUTED |
| P40 — Complete A–O integration | ClinicIntegration, category rights/follow-up/operations UI; P5 V81–83; targeted tests and shared regression | Stacked PR review/merge and real release gates remain; no deployment |
