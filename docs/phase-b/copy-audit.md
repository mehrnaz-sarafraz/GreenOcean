# Phase B copy and trust audit

| Existing statement | Intended meaning | Supporting evidence | Misunderstanding risk | Replacement | UI location |
|---|---|---|---|---|---|
| You do not have to carry it alone | Emotional welcome | Community product purpose | Implied guaranteed human support | Find understanding, meaningful connection, and the right next step | Welcome headline |
| Safety first / safety-first experience / create a safe space | Privacy/safety values | Reporting, blocks, private defaults exist | Universal safety or operational supervision | Privacy and clear boundaries; reports may be reviewed, no guaranteed response | Welcome / About / FAQ |
| Talk when you need to | Listener pathway | Listener request workflow | Immediate or round-the-clock capacity | Matching depends on availability; no immediate response guaranteed | Listener card / explainer / FAQ |
| Join a moderated support circle | Community pathway | Reporting and moderator roles exist; no staffing SLA proven | Continuous proactive moderation | Participate in communities; reporting/blocking and authorized review | Community / How |
| Verified professionals (decorative trust badges) | Credential distinction | Existing credential review; regional scopes separate | Everyone has clinical privilege everywhere | Qualifications reviewed; care requires scope, location, scheduling and terms | Professional card / explainer / FAQ |
| Your support circle is waiting | Login encouragement | Login only, no live capacity proof | Available people waiting for this visitor | Sign in to continue to your permitted services | Login |
| Automated systems identify a serious risk | Safety exception | No verified dangerous-content detection in current code | Automated crisis monitoring | Authorized staff may review reported information; no continuous monitoring claim | Privacy summary removed; FAQ/product copy |
| Hardcoded privacy/Terms text after failed API request | Legal fallback | Published policy service exists | Stale copy represented as current | Current published document unavailable; retry, public resources/back | Policy page |
| Immediate, country-aware routes | Crisis entry | Curated directory only covers some countries | Universal/current location coverage | Public directory, manually choose country; explicit no-local state | Urgent Help |
| Private by default | Default privacy | Phase A profiles private; location/year hidden, unknown requests off | Absolute confidentiality / public posts private | Describe exact defaults; public posts remain public; no absolute confidentiality | How / FAQ / Welcome |
| Personalise your space with conditions | Onboarding description | Preferences optional; not required to explore | Diagnosis disclosure required before signup | Minimal account and email verification; optional preferences can be skipped | How / Welcome |

All newly authored public copy is in `frontend/src/localization/public-copy.ts` (English/Persian). Published policy and directory/resource bodies stay in the language supplied by their publisher; UI clearly says a translation may be unavailable. All resource text is rendered literally. No endorsements, example people, success/session counts or response times are added. No analytics or trackers are added. Device language preference stores only `en` or `fa`.
