# Professional ranking assurance

P4 remains the source of feedback eligibility, cohort/mode separation, current qualification, evidence window, conservative experience index, newcomer discovery, educational contribution, rotation, independent weekly publication and public privacy rules. Q3 does not introduce a second reputation engine or a clinical competence score. The existing bounded Bayesian-style index uses a conservative approximation and is not a validated clinical-outcome measure or a formal confidence interval.

Q3 adds an independently reviewed audit per exact source policy, profession and TEXT/AUDIO/VIDEO mode. Current TRUST_CLINIC and CLINIC_REPUTATION_OPERATOR authority plus recent MFA is required to propose/approve. A different actual authenticated operator must approve the referenced fairness review. Review interval is configurable1–90 days, explicitly selected; expiry/revocation, source-policy expiry/change, inactive reviewers or role removal stop public ranking and weekly proposal/publication. Source method approval, advertising/privacy promotion gates and current professional qualification still apply separately. No audits or operators are seeded. Isolated test fixtures explicitly identify synthetic review records.

The staff screen records an opaque evidence reference and next due review. A recorded staff decision does not certify that an independent professional body or external reviewer performed an audit. Actual reviewer qualifications, methodology ownership, legal advertising/privacy assessment and periodic human review remain launch prerequisites.

|Synthetic cohort / manipulation|Executable checks|
|---|---|
|New professional / few reviews|P4 `newProfessionalsReceiveNoInventedScoreOrSmallPatientCount`; Q3 four-participant regression|
|Established / many reviews|P4 distinct verified participation; Q3 twelve-participant bounded indicator|
|Mixed modes / unknown historical profession|Q3 audio history excluded from text-mode votes; P4 unknown historical profession excluded|
|Repeated/free sessions / suspicious repeats|P4 repeated free sessions from one patient do not become five independent votes|
|Short-term inactivity|Q3 fourteen-day historical feedback remains qualified and sample-valid within the source window|
|Revoked/suspended qualification|P4 no longer appears in rankings/weekly features|
|False complaints / negative feedback|Unreviewed allegation does not change ranking; negative feedback does not remove care/new free booking|
|Subscription/revenue inflation|P4 subscription and revenue changes do not buy ranking or eligibility|
|Stale source inputs / self publication|P4 changed availability invalidates weekly proposal; independent bounded publication and rotation remain enforced|
|Missing, stale, revoked or wrong-mode audit|Q3 public ranking fails closed; no synthetic approval is created automatically|

Fixtures represent controlled historical metadata, including simulated media-delivery evidence; they are not actual therapy sessions or evidence of real patient outcomes. Final executed results belong in test-evidence.md. The actual-stack P4 journey now checks403 before the Q3 fairness audit, obtains a second virtual-key review through the real API, then retains all original private-feedback, unscored newcomer and independent weekly-publication assertions.
