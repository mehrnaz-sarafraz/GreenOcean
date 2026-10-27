# G/H migration and rollout

Base: E/F `9086d3a`, PR #5. G/H PR #6 is stacked on E/F, not stale main. Review in dependency order A → B → C → D → E/F → G/H. No automatic merge/deploy.

## V40

Only new migration: `V40__durable_audience_and_progressive_onboarding.sql`. Existing Flyway files are immutable. Fresh and populated V39 synthetic databases are tested. The migration preserves profile choices and stored media references, roles, policies, sessions, relationships, communities and applications. It narrows legacy PUBLIC to FOLLOWERS because publication consent cannot be reconstructed. It does not delete content or reset intentional profile choices. Owner-facing release notes must explain reduced discoverability; republication to a broader audience requires a new explicit decision, never an automatic bulk restore.

Onboarding true becomes LEGACY_UNKNOWN, false becomes NOT_STARTED; no claimed historical completion. Topic labels are mapped only by exact unambiguous catalog match. Unmapped values remain in private `legacy_support_topics`, available for clear/export/deletion, without recommendation/analytics processing. Existing inactive valid topic slugs are retained but cannot be newly selected. Confirm a retention review of unmapped historical sensitive values; no new unbounded duplicate archive is introduced.

Before rollout, rehearse on an isolated sanitized copy; measure V40 table-update and DDL lock time at production volume. The migration is one transactional pass, with no network I/O; take a recoverable database backup and use a maintenance window if measured lock time exceeds the site's budget. Do not run it on a live database from this task. Deploy backend and frontend together, invalidate old application caches and verify the startup migration before opening traffic. Old clients submitting PUBLIC without explicit confirmation fail closed; old sensitive labels cannot be newly collected. No new indexes are needed: post primary key, relationship composite keys and preference owner primary key are reused.

Forward recovery: fix with V41+, retain FOLLOWERS ceilings and explicit state. Do not reverse V40, restore a pre-V40 audience query, or fabricate public consent. Operational rollback must keep the restrictive audience predicate and reject old broad-publication payloads. Export/erasure continues to use A/E/F infrastructure; new progress/legacy values are in the same owner preferences row and are erased together.

## External gates

`app.privacy.support-topics-enabled=false` is the default; environment binding `APP_PRIVACY_SUPPORT_TOPICS_ENABLED`. Before enabling, obtain the applicable lawful basis/special-category processing decision, notice/consent design if required, necessity/proportionality and retention approval. Terms acceptance is insufficient. Keep collection off until those decisions exist. The software supports an enabled, validated active-slug catalog, tested only with synthetic data. No diagnosis/inference, personalized ranking or topic analytics is implemented.

H-14: no onboarding telemetry pipeline is enabled. Owner progress is needed for resume and is not anonymous analytics. Third-party trackers/events remain absent pending approved purpose, consent/basis, aggregation design and bounded retention. Failure to collect analytics cannot block onboarding.

Real avatar uploads are outside the existing architecture: only an optional HTTPS reference is stored, with explicit empty-string removal. No server fetch, storage subsystem, metadata extraction or promise to erase external images. External hosting can observe image requests; use trusted hosts and do not place sensitive data in URLs. Existing professional-document erasure is unchanged. Backups, security retention exceptions and previous client copies follow existing account policies; removal cannot recall received data.

## Phase I handoff

Use the existing support-style allowlist and routes. LISTENER lands on Support discovery regardless of availability; it never creates a session or opens unsolicited DMs. Existing service gates still decide actual matching/care. Topics currently do not rank feed/search; any future use needs a separately reviewed privacy contract and must apply the same audience/block/moderation predicates before ranking. Do not infer conditions from topics or clicks.

Native Android/iOS physical devices and VoiceOver/TalkBack must be validated with the device matrix before native release. Cross-platform JS export is not evidence of device behavior. Existing mail delivery, staff assurance and hosting operational gates from E/F still apply.
