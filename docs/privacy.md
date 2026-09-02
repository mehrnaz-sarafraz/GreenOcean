# GreenOcean Privacy Principles

GreenOcean is a privacy-conscious social and support platform.

It is not a medical-record system.

## Sensitive inference

GreenOcean must not infer or persist:

- diagnoses
- depression scores
- anxiety scores
- suicide-risk scores
- similar sensitive health profiling

unless a future product requirement undergoes an explicit privacy, legal, and security review.

## Data minimization

Collect only data required for a clear product purpose.

## Location

Location information must respect user privacy preferences.

## Professional information

Professional verification information must be separated from ordinary user-profile information and handled with appropriate access controls.

## Media

Uploaded media should ultimately be stored in dedicated object storage rather than directly in PostgreSQL.

## Moderation and safety

Privacy does not mean destructive deletion of safety-critical records.

Moderation and audit information may require separate retention rules from ordinary user-facing content.