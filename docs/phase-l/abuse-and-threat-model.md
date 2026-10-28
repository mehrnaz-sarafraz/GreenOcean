# Phase L abuse and threat model

| Threat | Enforced boundary | Test evidence / limits |
|---|---|---|
| Named legacy fallback / forged identity | Validated explicit identity; DB immutable attribution | L1 publishing/migration tests |
| Anonymous correlation through Following or profile totals | Exclude anonymous from named-author surfaces; suppress historical reaction totals | L2 + GH serialized responses; residual text/timing risk remains |
| Cross-account draft/appeal/export | Authenticated owner predicates | L1/L3 and GH export tests |
| Old approval after edit | Assessment + current content revision locks | L3 concurrency edit/approval race |
| Concurrent moderator decisions | Single committed decision with row locks | L3 concurrency one winner |
| Appeal flooding / duplicate appeal | One assessment appeal; bounded hourly submissions | L3 ownership and concurrent appeal tests |
| Posting/comment flooding and edits | Existing rate window records plus owner advisory locks; successful edit attempts share bounded windows | L1 duplicate race; L3 edit-limit test |
| Forged/stale staff permission | Existing live JWT/session validation, recent MFA and current DB role | EF + L3; real key custody still external |
| Queue deanonymization | No author fields; separate ADMIN purpose/audit endpoint | L3 redaction/attribution tests |
| Report action without takedown | REMOVE hides target and updates current assessment | L3 saved/direct access regression |
| Replaying generic appeal action | Linked appeals rejected by old endpoint | L3 revision workflow |
| Provider outage / unsafe regex | No external provider; bounded 10k local screen; exceptions hold | L1 outage and L3 bounded examples |
| Re-identification through external viewing | Cannot retract screenshots, copied text or prior knowledge | Explicit residual risk guidance |

No automatic suspension thresholds, sensitive analytics, external AI, media, or unrestricted internet publication. Authorized report intake has bounded hourly submissions without suppressing legitimate distress by keyword alone. Thresholds are implementation abuse limits, not promises of actual moderation staffing.

False positives: safe links/contact-shaped strings and quotations may be held; author may revise or appeal. False negatives: no signal means only local-screen pass, not semantic safety. Unrecognized hate, coercion, sexual harassment, idioms and transliterations may evade these rules. Keep publication gated and use human report/takedown operations after approval; strengthen the local classifier only under approved policy. No percentage accuracy claim is justified by the fixture set.
