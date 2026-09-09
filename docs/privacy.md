# GreenOcean privacy and data handling

GreenOcean is a social support product, not a medical-record or emergency-response system. This engineering baseline requires qualified privacy/legal review before public launch.

## Data categories and purpose

| Data | Purpose | Default handling |
| --- | --- | --- |
| Email and password hash | Account access and recovery | Private; passwords are never stored |
| Profile and preferences | Identity, discovery and user controls | Visibility follows profile settings |
| Posts, comments and reactions | Community participation | User-selected audience and soft deletion |
| Private messages | Requested support conversations | Conversation members only |
| Professional credentials | Authorised verification | Private, access-controlled media |
| Reports and audit events | Safety review and accountability | Restricted to authorised staff |
| Operational logs/metrics | Reliability and abuse investigation | No tokens, reset links or document bytes |

GreenOcean must not infer or persist diagnoses, symptom scores, suicide-risk scores or similar sensitive-health profiles without a separate legal, privacy and security review.

## User controls

Users control profile visibility, location display, sensitive-content behaviour, message requests, muted terms and account blocks. A block prevents private messaging and hides attached media across the relationship. Reporting is confidential and creates a review item; it does not automatically punish the reported person.

## Retention baseline

- Unattached uploads expire after 24 hours and are purged from metadata and storage.
- Active content and messages are retained while needed to provide the service.
- Deleted user-facing content may be soft-deleted for a limited safety, fraud and recovery window.
- Open reports, decisions and audit logs use a separately approved safety-retention schedule.
- Professional documents must be removed after the verification/legal retention period; reviewers should not download local copies.
- Backups inherit source-data sensitivity and require documented expiry and deletion.

Exact retention periods, export/deletion SLAs, controller identity, subprocessors, cross-border transfers and contact details remain launch blockers for legal and operations owners.

## Storage, transfer and incidents

Production traffic must use HTTPS. PostgreSQL, media volumes and backups must be encrypted at rest by the hosting environment. Secrets belong in a secret manager, never source control or container images. Original filenames never become storage paths.

Suspected exposure of credentials, verification documents, private messages or safety reports requires access containment, evidence preservation, secret rotation, impact analysis and the applicable notification process. See `operations.md`.
