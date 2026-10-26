# Phase A rollout

1. Back up production and rehearse restoration in an isolated environment. Validate existing Flyway checksums; never repair divergent V0–V30 histories silently. The fetched main has a different migration history from the original local checkout. Databases deployed from that earlier history require a separate reviewed reconciliation; V31+ cannot fix historical checksum divergence.
2. Deploy the additive V31, V32 and V33 migrations to staging. V31 creates identity, acceptance, reference-location, regional permission, deletion/file jobs, durable throttles and retired-refresh history. V32 seeds countries only. V33 classifies existing professional/listener conversations and invalidates old unkeyed recovery/verification tokens and plaintext delivery payloads. Tell users with outstanding tokens to request new instructions.
3. Configure separate stable `GREENOCEAN_MAIL_OUTBOX_KEY`, HTTPS mail gateway/token and a maintained offline `GREENOCEAN_PASSWORD_BLOCKLIST_SHA256` corpus. Do not commit these files or values. The `prod` profile requires delivery, a separate key, Secure cookies, exact HTTPS origins and a breach corpus. Admin remains unavailable in prod until MFA is implemented.
4. Mail gateway contract: authenticated POST JSON `{deliveryId,recipient,template,secret}` plus Idempotency-Key. Accept 2xx only after durable idempotent queueing for actual email delivery. Templates currently are email-verification (15-minute six-digit code) and password-reset (30-minute high-entropy token). No payload/code logging. Sender, provider and delivery monitoring are operator responsibilities. Test transport is injected in tests; no fake receiver is enabled in production.
5. Supply approved pilot countries/services explicitly in regional_services. No production country/service is seeded as AVAILABLE. Import reviewed/versioned subdivision/GeoNames data before enabling local features. Never copy synthetic test rows into production.
6. Release frontend and backend contracts together. Older signup clients lacking adult attestation/current Terms must upgrade. Existing browser token storage is purged and requires sign-in; mobile keeps SecureStore and existing BCrypt credentials upgrade on successful login. Use same-site HTTPS web/API hosting; cross-site hosts require an explicitly reviewed BFF/proxy design.
7. Legacy users keep their original status/email verification and have an explicit activation route. Before shortening `greenocean.phase-a.legacy-grace-until`, establish an owner-approved notification schedule, show activation instructions, verify actual delivery and allow a staged grace period. Development keeps the legacy compatibility exception open until an operator sets a deadline. Production refuses the unspecified far-future default and requires an explicit reviewed deadline before launch. Grace allows historical community behavior with unknown eligibility and is a documented residual risk, not age assurance. New care/listener services remain closed without explicit region approval.
8. Verify deletion and retrying file erasure, restricted safety-record retention, bounded backups and erasure replay after restore. No promise of instant removal from all backups is made.
9. Run the QA script/CI, real-browser E2E and Android/iOS smoke tests, email deliverability and security review before enabling a pilot. MFA, market/legal/scope approval and retention remain release gates.

Rollback: prefer feature/config rollback, disable regional services, and retain additive schema. Do not drop new tables/acceptance records or restore erased personal data. Forward-fix migrations; never rewrite applied migrations.

## Environment properties

Spring properties can be supplied by deployment environment mapping or a private config file:

```
greenocean.mail.enabled=true
greenocean.mail.gateway-url=https://your-controlled-mail-gateway.example/deliver
greenocean.mail.gateway-token=${GREENOCEAN_MAIL_GATEWAY_TOKEN}
greenocean.mail.outbox-key=${GREENOCEAN_MAIL_OUTBOX_KEY}
greenocean.phase-a.legacy-grace-until=<operator-approved UTC instant>
greenocean.security.admin-enabled=false
greenocean.auth.web-cookie-secure=true
```

Development can explicitly disable Secure cookies for HTTP-only local browser testing. Production refuses that override. Encryption-key rotation must preserve the ability to decrypt pending items, or expire/reissue them deliberately; do not rotate without a queue plan.
