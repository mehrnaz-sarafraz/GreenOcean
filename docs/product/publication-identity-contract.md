# Publication identity contract (Phase L)

Every new composer starts without an identity selection. Choose ANONYMOUS or SOCIAL_IDENTITY before writing; back navigation preserves text. Review identity, original audience, optional warning/topic and community before committing. PUBLIC is eligible authenticated members, never public internet. Broader audience requires explicit confirmation. Profile privacy never broadens a post's historical restriction.

Server requires publicationIdentity matching anonymous. Legacy anonymous=true remains explicit anonymous intent; legacy anonymous=false or omitted anonymous without identity gets400 and an upgrade instruction. Deploy coordinated clients before enabling publication. No silent named fallback. Published identity and author are immutable at database level; edit accepts only body plus current revision. Identity change requires a new conscious publication, never relabeling history.

Anonymous DTOs contain author:null, never author IDs, avatars or profile links. Anonymous posts are excluded from named Following and other-author profile lists; neutral discovery still enforces the original audience. No anonymous-author messaging shortcut. Owner-only mine/export can show owned content. Authorized safety attribution must be separate, single-target, purpose-limited, staff-MFA protected and audited.

Small audiences, timing, writing style and voluntarily disclosed details may permit inference. Screens explicitly warn about this. Removing content cannot erase others' prior viewing/copies. No perfect anonymity promise.

Draft approval: owner chose private server storage,7days after last edit,20 maximum per account, bounded cleanup. Reads/export exclude expired rows immediately; physical worker removes200 expired rows per minute. Drafts are absent from notifications/feed/search. Explicit save reports server success; reload restores only saved content. Unsaved changes can be lost on navigation, and clients must not claim they were saved.
