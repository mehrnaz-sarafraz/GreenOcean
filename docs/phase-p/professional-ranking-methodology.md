# Professional reputation methodology

P4 keeps patient experience, appointment reliability and independently reviewed education separate. These are service-experience signals, never clinical competence, safety or health outcomes. Revenue, subscription, raw booking volume and private learning activity provide no score advantage.

Only governed, delivered encounters with matching immutable profession at booking enter the relevant profession/mode cohort. Unknown historical cohorts are excluded, not backfilled. Public text and aggregate inclusion require separate patient choices. One latest approved opted-in vote per distinct verified adult patient is used in the approved time window. Minimum independent sample defaults in the UI to five; an independently approved methodology determines the actual threshold. Below it there is no score or small count.

For experience, ratings four/five are positive. Weights decay with a 90-day half-life. With weighted positives s, total weight n, prior weight w and positive fraction p, a=s+w*p, b=n-s+w*(1-p). The conservative index is mean minus 1.96 times the beta standard deviation, rounded to steps of five and capped at 95. This is a heuristic index, not a formal confidence interval or guarantee. Voluntary reporting and selection bias remain; it must not be described as the experience of every patient.

Reliability uses independently adjudicated no-shows within the opted-in feedback cohort, with the same minimum-sample protection. An allegation alone changes no indicator. Education counts only currently published, independently reviewed educational resources, deduplicated by digest and topic/month and capped. Methods are versioned, independently approved and expiring. Public projections omit internal staff identifiers.

Legacy generic reviews remain private to their author and cannot establish verified clinical experience. Public UUIDv4 feedback identifiers disclose no creation timestamp. Raw feedback is encrypted with a separate key; public text is an independently redacted projection. Patients can withdraw public use; audit and dispute history remain governed separately.
