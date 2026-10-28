# Group privacy contract — Phase M

PUBLIC and PRIVATE groups are discoverable only to eligible authenticated members. PUBLIC means open membership, not public discussion: every group post remains members-only. PRIVATE membership requires manager approval. HIDDEN groups are excluded from the directory, global suggestions/search and counts, including for members; a separate owner-scoped workspace provides membership navigation. Hidden slugs are opaque server-generated identifiers and guessed references return the same404 as missing groups.

The server projects privacy-safe metadata before joining. Membership counts are null, not false zero counts; ordinary members never receive a full roster. Managers see the minimum roster needed for governance, not hidden attribution of anonymous posts. Membership never creates a follow or messaging relationship and never appears on another person's profile.

Activated privacy is immutable. Historical boolean false/true maps to PUBLIC/PRIVATE without changing post identity/audience or membership. The shared post-read predicate adds current group access even to historical posts carrying a community ID; this conservative restriction does not broaden access. Owner archives/management remain separate from access to other members' discussion.

Suspended/archived groups have no ordinary content access. RESTRICTED groups are read-only to existing authorized members. No clinical service, unrestricted internet indexing, sensitive recommendation model, video/audio transport or public invite link is introduced.
