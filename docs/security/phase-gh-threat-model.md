# Targeted G/H privacy review

Authoritative product rules: [profile/audience contract](../product/profile-and-content-privacy-contract.md), [onboarding contract](../product/progressive-onboarding-contract.md). Authentication, authorization and retention still follow [E/F policy](authentication-session-policy.md) and existing Phase A controls.

| Threat | Mitigation / evidence boundary |
|---|---|
| Historical audience escalation | V40 conservatively freezes legacy PUBLIC to FOLLOWERS; durable audiences ignore profile transitions. Real PostgreSQL migration and multi-viewer transition tests. |
| Alternate content API bypass | Shared `can_read_social_post` on direct/feed/search/community/author reads, archive saved-post previews and notification reads/counts; actions/comments call authorized get. Blocks, deletion/moderation and current community/follow checks apply. |
| Private biography/location/age leakage | Profile DTO uses field audience; user search redacts bio/counts for nonfollowers; account identity/preferences never joined. Eligible-member discoverability includes the explicitly public optional avatar. |
| Anonymous reidentification | Null author DTO, omit anonymous author-list entries/counts for others, suppress attributable anonymous-comment notification actors; category global post counts withheld. Internal safety attribution is retained. |
| Cross-account update/cache | Existing authenticated `/me` routes and A–F cache epoch/remount isolation retained; owner row locks prevent unrelated patch loss; direct post focus revalidation clears failed access. Received copies cannot be recalled. |
| Forged topic/style/progress payload | Bean size/enum bounds, active stable slug check, default-off sensitive collection gate; current support-style allowlist/catalog; empty clear and null preservation. Errors contain no submitted values. |
| Privilege escalation/open redirects | `intendedRole` remains compatibility data only; no elevated role mutation; fixed support route allowlist and current eligibility check; dedicated professional workflow unchanged. |
| Avatar abuse/identity impersonation | No server-side URL fetch; reject non-HTTPS/credentials/local/IP paths; reserved usernames, uniqueness constraint and write lock; controls removed from display names; Unicode/Persian preserved. External tracking/metadata cannot be guaranteed absent; user notice. |
| Blocked established conversations | Block checks apply before existing direct conversation reuse and inside conversation listing/read/send; requests-off distinct from an established conversation. |
| Misleading safety settings | Separate blur/discovery controls; visual blur explicitly not confidentiality; mandatory status authorization remains independent; failed optimistic update rolls back with alert. |
| Sensitive instrumentation | No topic/free-text event analytics; collection off until external legal/product approval; progress owner-only, not claimed anonymous. Export/erase covers new fields. |

Residual risks: no clinical-confidentiality guarantee or formal certification; external avatar copies/requests; previously received client content; native assistive technology unexecuted; production-scale migration lock timing unmeasured; sensitive-collection legal basis and aggregate telemetry architecture unresolved. No real account, identity document or health record is used for testing.
