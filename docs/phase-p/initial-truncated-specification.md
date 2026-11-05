# GreenOcean — Phase P
## Online Professional Care, Clinical-Grade Foundations, Revenue Infrastructure, Professional Rankings and Iranian Market Readiness

### Complete Implementation Specification — P1 + P2 + P3 + P4 + P5
### Five Sequential, Independently Reviewable Pull Requests

---

# 1. MISSION

You are the principal full-stack engineer, Java/Spring Boot architect, PostgreSQL architect, telehealth systems engineer, financial-platform engineer, healthcare data security architect, React Native engineer, privacy-by-design specialist and quality assurance lead for GreenOcean.

Repository:

`mehrnaz-sarafraz/GreenOcean`

Technology stack:

- Java 21
- Spring Boot / Spring Security
- PostgreSQL / Flyway
- React Native / Expo / Expo Router
- TypeScript
- Web / Android / iOS

Implement:

**Phase P — Professional Care and Online Clinic Infrastructure**

The owner has selected **Model C: Full Online Clinic as the long-term product architecture**.

The first target market is **Iran**.

This implementation must establish a robust, modular, extensible system supporting:

1. Verified psychologists, counselors and psychiatrists.
2. Professional credential verification and renewal.
3. Detailed professional profiles.
4. Recurring and ad hoc appointment availability.
5. Free and paid consultation slots.
6. Direct appointment booking and special appointment requests.
7. Secure payment and commercial accounting.
8. Commission-based and professional subscription revenue.
9. Financial dashboards and no-show refund handling.
10. Professional text, audio and video consultation capabilities, subject to authorization.
11. Strong, privacy-preserving clinical record foundations.
12. Optional consensual follow-up.
13. Genuine client reviews and complaints.
14. Competitive professional rankings.
15. Weekly featured professional selections.
16. Transparent professional promotion and advertising.
17. Security, privacy, regulatory and operational readiness.

Deliver working, tested functionality rather than just design documentation.

Do not represent technically implemented services as legally authorized for production unless the required evidence exists.

---

# 2. OWNER-APPROVED PRODUCT CONTRACT

These decisions are approved.

## P-D01 — Professional categories

Support three distinct categories:

- Licensed psychologist.
- Licensed counselor.
- Psychiatrist.

Each category has independently verified professional scope and permitted services.

Do not infer that all professional types can deliver all services.

## P-D02 — Independent credential verification

Require:

- Professional application.
- Supporting evidence.
- Independent verification against appropriate authorities.
- Authorized review.
- Periodic re-verification.
- Expiry handling.
- Revocation.

A self-declared license number is not independent verification.

## P-D03 — Complete professional profiles

Include:

- Verified identity.
- Professional type.
- Qualifications.
- Professional license status.
- Specialties.
- Experience.
- Languages.
- Service descriptions.
- Permitted consultation modalities.
- Prices.
- Free consultation availability.
- Appointment calendar.
- Conditions of service.
- Verified client feedback.
- Accurate availability status.

Only display professional claims supported by valid evidence.

## P-D04 — Hybrid booking

Support:

- Direct booking of genuinely available published time slots.
- Special appointment requests requiring professional approval.

Do not double-book.

## P-D05 — Text, audio and video

Design for all three modalities.

Activate each modality only when the provider, infrastructure, professional scope, patient requirements, legal basis and operational safeguards have been approved and tested.

## P-D06 — Strong limited clinical records

Build a secure, modular professional care record foundation.

Support clinical notes, authorized patient information and documents within approved scope.

Do not automatically import personal journals, mood records, listener conversations or social activity.

## P-D07 — Prescribing

Prescription and advanced medical workflows remain disabled.

Do not implement live prescribing without separate regulatory, interoperability and operational approval.

## P-D08 — Hybrid business model

Support:

- Transparent per-session commission.
- Professional subscriptions.

Exact commercial rates, subscription tiers and settlement contracts must remain versioned configuration pending final owner approval.

## P-D09 — Authorized payments

Use a legally appropriate payment provider.

Provide:

- Payment verification.
- Invoices/receipts.
- Refunds.
- Provider settlement.
- Commission accounting.
- User financial history.

Do not collect or store raw payment-card credentials.

## P-D10 — Reviews and complaints

Accept reviews linked to real eligible service experiences.

Provide moderation, abuse prevention and independent complaints handling.

## P-D11 — Voluntary follow-up

Allow repeat booking and consensual follow-up plans.

No automatic therapy relationship or unsolicited treatment messaging.

## P-D12 — Initial country

Iran is the first intended market.

Do not automatically enable other countries.

Service authorization remains conditional on the actual legal and operational requirements.

---

# 3. ADDITIONAL OWNER REQUIREMENTS

The following requirements are explicitly included.

## P-D13 — Competitive professional rankings

Professionals must have competitive rankings based on transparent, verifiable factors, including:

- Verified service reliability.
- Genuine patient satisfaction.
- Eligible completed consultations.
- Reviewed educational articles.
- Reviewed published courses.
- Professional participation quality.
- Responsiveness and appointment reliability.

However, raw consultation volume must not dominate the ranking or create incentives for unnecessary care.

Rankings must not imply clinical superiority or guaranteed treatment outcomes.

## P-D14 — Weekly featured professionals

Automatically calculate eligible candidates for periodic weekly recognition.

Select a defined number of professionals through a transparent quality-gated policy.

Create an actual featured-professional placement on approved discovery surfaces.

Do not fabricate endorsements or imply official regulatory recognition.

## P-D15 — Flexible professional availability

Professionals must manage:

- Recurring weekly schedules.
- One-time appointments.
- Ad hoc slots for the current week.
- Short-notice availability.
- Paid slots.
- Free slots.
- Availability exceptions.
- Time off.
- Appointment conflicts.

## P-D16 — Appointment reminders and actual presence

Notify professionals before booked appointments.

Require actual authenticated readiness or check-in where necessary.

Calendar availability is not proof of being online.

## P-D17 — Professional no-show and financial protection

If a professional fails to attend:

- Record server-verifiable evidence.
- Apply the approved grace period.
- Notify the patient.
- Preserve financial protection.
- Initiate the appropriate refund or compensation workflow.
- Display accurate financial status in the patient's dashboard.
- Target prompt handling within 48 hours.

Do not falsely claim that an external bank has completed a refund when it has not.

## P-D18 — Future-ready clinical data architecture

Design clinical records for strong security, separation, granular access controls and future approved interoperability.

Do not prematurely implement a nationwide electronic health record or store sensitive information without the necessary permissions.

## P-D19 — Business model extensibility

The owner intends to develop and optimize the business model later.

Design configurable revenue rules and analytics boundaries now.

Do not freeze commission rates, subscription prices or promotional commercial policies in source code.

---

# 4. FIVE IMPLEMENTATION MILESTONES

## P1 — Professional Verification and Workspace

- Professional application.
- Credentials and validity.
- Periodic re-verification.
- Professional scope authorization.
- Detailed profile.
- Flexible calendar.
- Free/paid slots.
- Reminders.
- Qualification and schedule security.

## P2 — Bookings, Payments and Revenue

- Direct booking.
- Special appointment requests.
- Pricing.
- Booking lifecycle.
- Provider payment integration.
- Commission ledger.
- Subscription architecture.
- Financial dashboard.
- Refund workflow.
- No-show financial handling.

## P3 — Professional Sessions and Secure Records

- Professional consultation authorization.
- Text service.
- Audio/video provider integration with release gates.
- Session presence.
- Informed consent.
- Clinical records.
- Private professional notes.
- Authorized document storage.
- Secure audit and data lifecycle.

## P4 — Reputation, Rankings and Weekly Promotions

- Genuine verified reviews.
- Complaints handling.
- Ranking algorithm.
- Professional contribution metrics.
- Reputation integrity.
- Weekly featured selections.
- Transparent promotion labels.
- Professional ranking dashboard.
- Anti-manipulation.

## P5 — Follow-Up, Business Operations and Final Integration

- Voluntary treatment follow-up.
- Repeat booking.
- Financial reconciliation.
- Operational controls.
- Security hardening.
- Privacy rights.
- Full integration.
- Real PostgreSQL concurrency tests.
- Browser/backend journeys.
- Four-persona regression.
- Final Iran-market release readiness.

Create five separate stacked PRs.

Do not automatically merge or deploy.

---

# 5. VERIFY THE CURRENT REPOSITORY BASELINE

Before implementing:

1. Inspect the latest main.
2. Inspect Phase L, M, N and O branches.
3. Confirm the latest validated Phase O4 implementation.
4. Read current Phase P-adjacent code.
5. Inspect the latest Flyway migration.
6. Inspect current CI.
7. Read Phase J governance.
8. Read service-region authorization rules.
9. Inspect current professional verification and consultation data models.
10. Identify reusable infrastructure.

Historical observations include:

- Phase N3 in PR #18.
- Phase O1 in PR #19.
- Later Phase O milestones were not yet confirmed during the last inspection.

Verify the latest actual state.

### Dependency requirement

Use the latest validated O4 baseline.

If O4 is incomplete:

- Do not claim otherwise.
- Do not rebuild it under P.
- Complete only safely separable preparation.
- Track integration blockers.
- Do not open a PR on an incorrect base.

---

# 6. REUSE THE EXISTING PROFESSIONAL ARCHITECTURE

Inspect:

- `ProfessionalWorkflowController`
- `ProfessionalWorkflowService`
- `ProfessionalWorkflowRepository`
- `ProfessionalDocumentStorage`
- `ProfessionalApplicationRequest`
- `ProfessionalResponse`
- `CreateAvailabilityRequest`
- `ProfessionalAvailabilityResponse`
- `CreateConsultationRequest`
- `ConsultationResponse`
- `CreateProfessionalReviewRequest`
- `ProfessionalReviewResponse`
- `CatalogController`
- `CatalogService`
- `AccessPolicy`
- `MessagingService`
- Account verification and MFA
- Existing article approval
- Existing payments, if any
- Existing account archive/deletion
- Existing notification infrastructure
- Professional discovery frontend
- Professional profile frontend
- Existing consultation screens

Previously observed:

- Professional applications.
- Uploaded credentials.
- Verified status.
- Professional service regions.
- Availability slots.
- Consultation requests.
- Confirmation/cancellation.
- Professional messaging.
- Reviews.
- Professional articles.

Enhance these systems.

Do not create a parallel professional service platform.

### Existing gaps to verify

- Current professional reviews may not be limited to genuine completed consultations.
- Credential documents use an existing storage abstraction requiring a security and deployment review.
- Current availability supports slots, but recurring weekly and ad hoc free/paid availability require extensions.
- Consultation metadata must be integrated with actual session delivery.
- Financial accounting and refunds require dedicated infrastructure.
- The current professional profile includes ranking-related fields that must not be treated as trustworthy clinical scores without provenance.

Document the exact existing state before altering it.

---

# PART I — P1: PROFESSIONAL VERIFICATION AND AVAILABILITY

# 7. P-01 — Professional qualification lifecycle

Create a durable authorization lifecycle.

Suggested states:

- DRAFT
- SUBMITTED
- UNDER_REVIEW
- NEEDS_REVISION
- VERIFIED
- EXPIRED
- SUSPENDED
- REVOKED
- REJECTED

Verify which states already exist.

Use the smallest compatible extension.

### Verification requirements

- Professional identity.
- Professional category.
- Credential issuer.
- License identifier.
- Profession-specific scope.
- Jurisdiction.
- Verification method.
- Independent evidence.
- Reviewer.
- Verification date.
- Validity end date.
- Re-verification date.
- Current authorization status.

Do not expose complete identity documents publicly.

### Independent verification

Where an official verification endpoint is available and authorized, integrate it using an approved connector.

Otherwise support a documented human verification workflow.

Do not simulate a successful registry response.

No self-approval.

Privileged reviews require current role and appropriate recent MFA.

### Revocation

A revoked or expired professional cannot:

- Offer new regulated services.
- Publish new bookable clinical slots.
- Accept new clinical sessions.
- Enter unauthorized live sessions.
- Continue using clinical authorization through an old token.

Handle existing bookings and records safely.

---

# 8. P-02 — Professional scope matrix

Separate permissions by:

- Professional type.
- License.
- Verified specialty.
- Country.
- Service modality.
- Regulatory authorization.
- Platform operational availability.

Do not infer prescribing authority from professional profile text.

Do not infer video-care authorization from ability to create a video calendar slot.

### Initial professions

- Psychologist.
- Counselor.
- Psychiatrist.

Each may have different approved services.

A general editorial publishing role does not grant clinical authority.

---

# 9. P-03 — Professional workspace

Build a professional dashboard.

Include:

- Qualification status.
- Verification expiration.
- Profile editor.
- Service descriptions.
- Language settings.
- Pricing.
- Availability calendar.
- Upcoming bookings.
- Free consultations.
- Paid consultations.
- Appointment requests.
- Financial summaries.
- Subscription status.
- Patient communications.
- Authorized clinical records.
- Professional reviews.
- Reputation statistics.
- Ranking and featured status.
- Professional educational contributions.

### Authorization

All views require current permissions.

The professional can access only records belonging to their legitimately authorized care relationships.

---

# 10. P-04 — Recurring, ad hoc and short-notice scheduling

Implement a robust calendar.

### Recurring schedule

Examples:

- Saturdays, 09:00–12:00.
- Mondays, 14:00–18:00.
- Weekly recurring slots.

### Ad hoc schedule

Examples:

- Additional free slot this evening.
- One available hour tomorrow.
- A newly available appointment this week.

### Free slots

Professionals may voluntarily publish zero-price appointments.

A free appointment must retain:

- Professional verification.
- Booking authorization.
- Informed consent.
- Service scope.
- Session privacy.
- Appropriate attendance policy.
- Complaint reporting.

### Short-notice availability

A professional may publish a new slot shortly before its start if the configured minimum lead time, service rules and notification requirements allow it.

Do not represent an ad hoc slot as already confirmed for a specific patient until a real booking occurs.

### Timezone

Use IANA timezone identifiers.

Store authoritative instants.

Handle Iran's current time rules and future timezone-rule changes through a maintained timezone database.

Test DST boundaries where applicable to future markets.

### Capacity

Use real PostgreSQL exclusion/uniqueness constraints and transactional conflict handling.

No overlapping clinical appointments for the same professional.

No conflicts with professional activities that consume the same required capacity.

---

# 11. P-05 — Appointment reminders and readiness

Create configurable reminders.

Suggested initial technical defaults:

- 24 hours before a scheduled session.
- 60 minutes before.
- 15 minutes before.
- At the scheduled start when readiness remains missing.

Exact reminder frequency must respect user preferences and approved notification transports.

### Professional readiness

A calendar slot is not evidence that the professional has joined.

Use server-verified:

- Authenticated session.
- Current professional authorization.
- Appointment access.
- Fresh readiness.
- Actual session presence.

### Patient notifications

When readiness is missing:

- Show the actual current status.
- Avoid declaring a no-show prematurely.
- Explain the applicable grace period.
- Allow safe refresh.
- Offer support options after verified failure.

Do not claim push-notification delivery guarantees unless they are actually supported.

---

# PART II — P2: BOOKINGS, PAYMENTS AND BUSINESS INFRASTRUCTURE

# 12. P-06 — Direct and special appointment booking

Implement two pathways.

### Direct booking

The patient selects a genuinely available published slot.

The system atomically confirms the reservation under approved eligibility and financial conditions.

### Special request

The patient requests a different time.

The professional may:

- Approve a proposed appointment.
- Suggest another available time.
- Decline.

A request is not automatically a confirmed booking.

### Booking lifecycle

Suggested states:

- REQUESTED
- PENDING_PAYMENT
- PAYMENT_PENDING_VERIFICATION
- CONFIRMED
- CHECKIN_OPEN
- IN_PROGRESS
- COMPLETED
- CANCELLED
- NO_SHOW_PROFESSIONAL
- NO_SHOW_PATIENT
- SERVICE_FAILURE
- REFUND_PENDING
- REFUNDED

Keep booking, payment and session states as related but distinct state machines.

Do not force a single status enum to represent every independent financial and clinical transition.

---

# 13. P-07 — Professional service pricing

Each eligible service offering must define:

- Professional.
- Service type.
- Modality.
- Duration.
- Price.
- Currency.
- Whether free.
- Cancellation terms.
- Current professional authorization.
- Valid service region.
- Applicable commercial policy version.

### Iran

Use a canonical monetary unit appropriate to the authorized payment integration.

Make UI conversion and display explicit.

Avoid mixing Iranian rial and toman without correct conversion.

Use integer minor/canonical money units, not floating-point arithmetic.

### Pricing safety

- No hidden fees.
- No price changes after confirmed purchase without authorized consent.
- No automatic paid upgrade from a free slot.
- No misleading discounts.
- No unsupported insurance reimbursement promises.

Check applicable professional tariffs before activating live pricing.

---

# 14. P-08 — Commission and subscription business model

The owner selected a hybrid model.

Implement a modular commercial policy engine.

### Commission

Support:

- Percentage commission.
- Fixed commission where approved.
- Per-profession configuration.
- Per-service configuration.
- Effective dates.
- Versioning.
- Discount/free-session treatment.
- Refund reversal.
- Provider payout calculation.

Do not hardcode a permanent universal commission percentage.

### Professional subscriptions

Support configurable tiers and entitlements.

Potential future tier features may include:

- Extended schedule management.
- Advanced practice analytics.
- Administrative workflow enhancements.
- Additional operational tools.

Do not make a paid subscription substitute for professional credentials.

Do not make patient safety dependent on professional subscription status.

### Commercial fairness

A higher subscription price must not create a fake clinical quality score.

Any purchased advertising must be clearly labeled and separate from merit-based rankings.

### Deferred business decisions

Create a decision register for:

- Commission rates.
- Subscription pricing.
- Promotional entitlements.
- Refund cost allocation.
- Professional settlement cadence.
- Discount policies.
- Subscription refunds.
- Invoicing and tax requirements.

These are future configurable business decisions, not reasons to omit the platform architecture.

---

# 15. P-09 — Payment integration

Build a provider-agnostic payment gateway abstraction.

### Requirements

- Server-created payment intent.
- Verified provider callback/webhook.
- Signature/authenticity validation.
- Idempotent processing.
- Amount and currency verification.
- Booking ownership.
- Reconciliation.
- Payment receipt.
- Failure states.
- Refund initiation.
- Refund confirmation.
- Administrative audit.

### Never

- Trust a frontend success callback as proof of payment.
- Store raw card numbers or CVV.
- Mark a refund complete because a local database flag changed.
- Release provider payout before permitted financial conditions are met.
- Hold customer funds in an unauthorized platform wallet.

### Provider activation

If no suitable legally authorized Iranian payment provider is integrated and approved:

- Implement tested interfaces and synthetic adapters.
- Keep real money processing disabled.
- Clearly document missing credentials/contracts.
- Do not fabricate transaction success in production.

---

# 16. P-10 — Financial ledger and settlement

Implement an append-only or otherwise auditably immutable financial event ledger.

Record:

- Payment reference.
- Booking reference.
- Patient reference.
- Professional reference.
- Gross amount.
- Commission.
- Provider payable.
- Subscription charges.
- Refund amount.
- Financial state.
- Reconciliation evidence.
- Settlement reference.
- Event timestamp.
- Applicable commercial rule version.

### Invariants

- Financial entries balance.
- Duplicate callbacks do not double-credit.
- Refunds cannot exceed refundable amounts.
- Commission reversal is correct.
- A free consultation has zero charge.
- Cancelled unpaid bookings do not generate payouts.
- Reconciliation failures appear in an operational queue.

Use appropriate transaction boundaries and independent reconciliation.

Do not modify prior financial events silently.

Use compensating records where required.

---

# 17. P-11 — Patient financial dashboard

Create a patient financial area.

Suggested name:

**Payments and Refunds / پرداخت‌ها و بازپرداخت‌ها**

Display:

- Completed payments.
- Pending payments.
- Receipts.
- Session prices.
- Cancelled reservations.
- Refund requests.
- Refund status.
- Confirmed refunded amounts.
- Eligible credits, only if legally and technically supported.
- Disputed financial cases.

### No misleading balance

A refund receivable is not automatically a spendable wallet balance.

Clearly distinguish:

- Payment received by provider.
- Refund requested.
- Refund accepted.
- Refund submitted to payment provider.
- Refund confirmed by provider.
- Funds received, when confirmation is available.
- Credit issued through an approved licensed arrangement.

The UI must show actual state, not a promise presented as settled money.

---

# 18. P-12 — Professional no-show and 48-hour financial workflow

This is an explicit owner requirement.

### Evidence

No-show decisions must use server-confirmed appointment and session evidence.

Do not infer professional attendance from:

- Calendar existence.
- Notification delivery.
- A stale online flag.
- A frontend-only button.

### Grace period

Define a configurable, prominently disclosed grace period.

Suggested initial default:

10 minutes after scheduled start.

Allow documented handling of:

- Provider outage.
- Patient late arrival.
- Mutual rescheduling.
- Network failure.
- Professional absence.
- Dis