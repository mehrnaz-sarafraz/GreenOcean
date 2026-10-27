# G/H API additions and compatibility

All social/profile/preference APIs remain authenticated, owner-scoped where applicable and subject to A–F eligibility/session gates. Public-resource APIs do not serve member posts. Normal responses retain `Cache-Control: no-store`.

## Profiles

`PATCH /api/v1/profiles/me` retains partial-update semantics. Omitted/null optional strings preserve values; `avatarUrl: ""` explicitly clears the stored reference and `bio: ""` clears biography. An avatar must be a public HTTPS URL without credentials or a local/IP host; the server does not fetch it. The profile editor sends an explicit empty value on removal. New optional media is not required. Display names accept Persian/Unicode and NFC normalization but reject control/bidi-override characters. Usernames normalize to lowercase ASCII, 3–30 characters, internal dots/underscores supported for legacy compatibility, no boundary dot; reserved platform/staff identifiers and delimited variants are rejected. The unique case-insensitive database index remains authoritative. Username changes do not change UUID social references or account email.

`GET /profiles/{username}` returns minimal discoverable identity to eligible nonfollowers, including optional avatar; biography, explicitly enabled location/year and relationship counts require profile-detail visibility. Private support preferences/email/eligibility never enter this DTO. Anonymous posts are omitted from other members' author listings/counts. The existing private-profile author-list gate is retained as an additional restriction; an explicitly broad post may be accessible in feed/direct link while that author's private profile list remains gated. Global category and helpful-activity counts are withheld as zero for compatibility, not evidence that no such activity exists.

## Post publication and access

`POST /api/v1/posts` requires existing `body`, `anonymous` boolean, `visibility`; existing category/type/community fields retain their meaning. `visibility=PUBLIC` additionally requires `broaderAudienceConfirmed=true`. Missing/null/false confirmation is rejected with a safe 400. FOLLOWERS and COMMUNITY do not require that field. COMMUNITY requires actual membership and matching community ID; other audiences reject a community ID. `contentWarning` remains optional and is now editable in the existing composer. PUBLIC means eligible GreenOcean members, not unauthenticated guests. Explicit audience is permanent; there is no audience-widening update API.

`can_read_social_post(id, viewer)` is shared by feed/search/author/community/direct queries, visible-post counts and notification reads/counts. Comments, likes and bookmarks first authorize the parent post. Current follow/community membership, block status, author status and content status are checked at read time. Discovery additionally applies stronger controls (warning-marked exclusion), medication reduction (literal medication keyword/category slug), and muted terms. Direct links retain authorization and visual warnings; those discovery preferences are not confidentiality boundaries. Exports are owner archives, not alternate public reads. Anonymous author attribution remains internal, excluded from ordinary DTOs/author listings; anonymous replies have no notification actor.

## Preferences and progress

`GET/PATCH /api/v1/preferences/me` adds:

| Field | Meaning |
|---|---|
| onboardingStatus | NOT_STARTED, IN_PROGRESS, SKIPPED, COMPLETED; LEGACY_UNKNOWN is returned for legacy dismissal evidence |
| onboardingStep | Last confirmed step 0–3, not current unfinished UI input |
| legacySupportTopics | Read-only owner legacy labels with no unambiguous mapping; no recommendation use |
| topicCollectionEnabled | Read-only legal/product configuration gate, false by default |
| clearLegacySupportTopics | PATCH-only true explicitly removes legacy labels |

`supportTopics` accepts active catalog **slugs**, maximum 30; [] explicitly clears. Nonempty writes are rejected while collection is disabled, even if crafted manually. `supportStyle` accepts the active catalog keys SHARED_EXPERIENCES, SUPPORT_GROUPS, LISTENER, PROFESSIONAL_KNOWLEDGE; empty string explicitly clears. Null/omitted fields preserve existing values. Null/blank collection elements and oversized payloads are invalid. Unrelated properties survive row-locked concurrent patches. This is not a cross-device conflict-resolution UI: simultaneous writes to the same field use last committed write.

COMPLETED requires confirmed step 3. Skip persists SKIPPED without marking every stage completed. Old `onboardingComplete=true` PATCH is compatibility dismissal as LEGACY_UNKNOWN, never new evidence of completion; `intendedRole` remains inert compatibility data, not a role grant/navigation source. The boolean is true for SKIPPED/COMPLETED/LEGACY_UNKNOWN and false for new/in-progress. Preferences may be edited without forced re-enrollment. No clinical fields or freeform health history have been added.

Existing account archive includes the new preference fields. Existing password-confirmed account deletion removes the whole preferences/progress row and nulls avatar reference, and invalidates sessions. No new account infrastructure.

Saved previews of other members' posts in the archive also recheck current post authorization. Losing follow/community access, blocking or moderation removes the preview from future exports; an old bookmark never grants a durable right to retrieve another member's content. Own authored posts/comments remain part of the owner's data-rights archive, not an alternate public feed.
