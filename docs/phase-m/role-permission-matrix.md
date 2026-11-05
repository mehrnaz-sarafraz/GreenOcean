# Current group role permissions

| Capability | MEMBER | MODERATOR | OWNER | Platform reviewer |
|---|---|---|---|---|
| Read current permitted discussion | Yes | Yes | Yes | No implicit membership bypass |
| Publish screened narrative | Current rules, no active restriction | Same | Same | Same if separately a member |
| View complete roster / private applicants | No | Bounded management view | Bounded management view | Requires separately authorized purpose; no generic membership bypass |
| Invite / approve private access / remove ordinary member | No | Yes | Yes | No implicit group role |
| Ban/restrict ordinary member | No | Yes | Yes | Privileged safety scope only |
| Assign/demote moderators / lift bans / offer ownership | No | No | Yes | Separate recovery procedure |
| Edit stricter group rules | No | Validated/versioned | Validated/versioned | Global policy remains authoritative |
| Reveal anonymous post attribution | No | No | No | Existing purpose-audited ADMIN-only L pathway |
| Activate proposed group | No | No | No | Independent current platform role + recent strong MFA + enabled operational gate |

Demotion is checked from current DB state. Pending invitations do not survive loss of inviter permission. Group roles never confer access to staff queues, private messages, listener conversations, credentials or account-security data. Last-owner exit/removal is refused; no random successor is selected. Ownership and moderator-management duties are explained before acceptance.
