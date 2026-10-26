# Account lifecycle

New public registrations create USER accounts in PENDING with a random temporary username and private profile. The access-token session may authenticate a pending owner for activation, own settings, export and deletion; it never grants social authorization. `PhaseAInterceptor` and `AccessPolicy.require` enforce social eligibility from the live database.

Verification uses a 15-minute single-use code, keyed HMAC bound to the normalized email, a durable five-attempt/15-minute account budget and IP budgets. Resending invalidates old codes. Protected delivery payloads are purged after successful delivery or expiry. Verification activates a pending account with adult eligibility. Regional access remains independently closed until explicitly enabled.

ACTIVE, SUSPENDED and DELETED remain separate from age evidence, roles and deletion progress. Suspension is checked on every JWT and refresh. Password changes/reset revoke sessions. A retired refresh token revokes its family and the revocation commits even when the API returns 401. Concurrent refresh is serialized; the client also shares one in-flight refresh.

Legacy accounts are never marked verified or assigned independent age assurance. The rollout grace flag is a temporary compatibility exception for community access only; it must not be treated as evidence of age or email verification. See the rollout plan before setting the deadline. Legacy activation is available through `/identity/activate` plus email verification.

## Erasure

DELETE `/api/v1/account` always derives ownership from the authenticated subject and requires the current password. Existing domain erasure is reused. Additional cleanup removes recovery/verification tokens, delivery payloads, eligibility/acceptances, search history and support-request identity; deactivates direct conversations and removes cached identity titles. Sessions and public profile access stop immediately within the committed transaction.

Database erasure completes transactionally. Credential files are queued for an idempotent retrying worker after the transaction, so a storage failure cannot roll back database erasure or delete files before a rollback. Deletion status remains PROCESSING/FAILED_RETRYABLE until files complete. No keys are included in logs. Failed pre-commit requests can be retried by the owner; they do not claim completion.

Reports and minimal pseudonymous moderation references remain for safety review. This is not permission for unlimited retention: launch requires a jurisdiction-specific retention schedule, restricted reviewer access, periodic expiry jobs and documented exceptions/holds. ON_HOLD is reserved for a future authorized retention workflow; there is no public hold override.

Backups are not immediately edited. Operators must define bounded encrypted-backup retention and replay deletion tombstones after restoration before bringing restored data online. This task does not claim that production backups or report-retention expiry have been implemented.
