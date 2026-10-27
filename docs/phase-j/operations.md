# Phase J operations and release checklist

## Baseline and safe settings

J includes final I beb5b78 (including foreground response invalidation), and PR8 remains stacked on I PR7. No main merge, deployment, country enablement or real sensitive-processing approval occurs here.

| Existing Spring property / environment | Default / behavior |
|---|---|
| app.privacy.support-topics-enabled / APP_PRIVACY_SUPPORT_TOPICS_ENABLED | false; feature flag alone is insufficient |
| app.governance.optional-processing-approved / APP_GOVERNANCE_OPTIONAL_PROCESSING_APPROVED | false; topics also require a current independently approved notice and current affirmative owner grant |
| app.governance.policy-publication-enabled / APP_GOVERNANCE_POLICY_PUBLICATION_ENABLED | false; legal rollout approval still required |
| app.governance.rights-intake-enabled / APP_GOVERNANCE_RIGHTS_INTAKE_ENABLED | false; true requires actual nonblank rights-owner and bounded response-hours1–8760 |
| app.governance.rights-owner / APP_GOVERNANCE_RIGHTS_OWNER | empty; appoint accountable handler, not a fake contact |
| app.governance.rights-response-hours / APP_GOVERNANCE_RIGHTS_RESPONSE_HOURS | 0; operational target, not statutory deadline |

OPTIONAL_ANALYTICS and OPTIONAL_COMMUNICATIONS grants are unavailable regardless of general approval flag because no approved pipeline exists. Essential verification/recovery/security mail remains independent of optional campaigns. No switch substitutes for counsel, staffing or organizational evidence.

## Policy and rights administration

Existing AdminService creates unpublished drafts; publish-on-create is rejected. Locale/effective date and status are explicit. Different current ADMIN reviews actual text with a safe reference. Editing content/identity/date invalidates approval. Publisher requires current ADMIN, existing recent strong authentication, approved rollout and a still-authorized independent reviewer. Older draft versions cannot replace a newer published sequence. Published content/review evidence is immutable; retirement preserves public history. Binding translated Terms/listener-code publication is blocked until reviewed locale-aware acceptance is supported. No AI legal text is published by migration/tests.

Human-rights intake uses authenticated owner/kind/retry UUID, no mandatory health narrative or identity scan. Same retry converges; conflicting/open-kind duplicates fail clearly. Staff queue/status uses existing ADMIN and recent MFA. RESOLVED means a human case status, not automatic universal erasure. Safe references contain bounded letters/numbers/._/-; no sensitive prose. Existing password-confirmed self-service erase and archive work while intake is disabled. Before enablement, appoint monitored handler/escalation, applicable response rules and proportionate identity review. All rights do not automatically apply identically to every basis/category.

## Migration and compatibility

V42 adds policy locale/effective/review fields, immutable purpose events/notice receipts and owner-scoped rights cases. No grant/receipt is backfilled. V43 preserves published review evidence and invalidates an edited draft approval. Applied migrations remain unchanged. Fresh and populated V41 upgrades are tested. Take approved recoverable backup and measure DDL/index locking on actual volume before rollout; deploy compatible clients/backend together and recover with additive forward fixes. Do not relax E/F MFA, I session rules or G/H audiences to make staff operations available. Production admin guard remains unchanged. New documentation-only commits preserve the tested executable.

## Incident lifecycle (J-14)

1. Detect through existing fixed-classification security events, mail outcome/queue visibility and authorized report/rights workflows; configure and rehearse real restricted operator alert routing before launch. No new alert provider is provisioned.
2. Assign opaque incident reference and accountable security lead in an approved restricted case system; record timestamps/actions/evidence references, never raw tokens/passwords/private message bodies in application logs.
3. Contain by existing session/refresh revocation, account suspension, disabled live/optional/publication flags and D key/gateway rotation procedures. Verify current pair/audience access fails closed.
4. Preserve minimum relevant evidence only under authorized access and counsel-approved hold. SECURITY logs remain bounded (default30days); no automatic universal hold or indefinite retention is invented.
5. Assess affected data/people/territories with privacy/legal/service owners. Notification recipients/timing/legal conclusions require professional assessment; no automatic statutory notices are sent.
6. Recover with additive fixes and isolated synthetic regression, verify MFA/ownership/mail secrets/backup deletion replay, obtain actual re-enablement approval and record post-incident minimization/review.

Scope includes visibility mistakes, support disclosure, compromised credentials/mail keys, excessive staff access, lost professional files, processor mistakes and report misuse. Policy/rights audit uses actor/action/entity reference and empty metadata, not private content. Actual monitoring, evidence system, staff rota and incident-response delivery remain external operational dependencies. Existing audit access is privileged; SECURITY retention does not silently redefine all governance/staff audit retention.

Reviewer checklist: correct I ancestry; no prior migrations changed; forged/missing grants; current notice; rollback/self-review/revoked reviewer; immutable review evidence; report audiences and appeal ownership; archive completeness/erasure; disabled defaults; native/processor/retention/legal launch blockers. No certification claim.
