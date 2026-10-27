# G/H baseline gap matrix

Baseline: E/F `9086d3a`, draft PR #5; no G/H branch existed remotely. E/F CI 40467058457 passed 313 tests. Targeted inspections confirm gaps below; A–F services are reused, not recreated. Full per-requirement audit fields: [gap-matrix.json](gap-matrix.json).

| ID | Initial status | Intent / observed gap | Existing location | Required change |
|---|---|---|---|---|
| G-01 | PARTIALLY_IMPLEMENTED | Minimal transactional profile initialization: Existing random private profile creation; rollback/retry evidence needs targeted extension | auth/service/RegistrationService.java; profile/entity/Profile.java | Preserve initialization, exercise retry/rollback/defaults |
| G-02 | IMPLEMENTED_NEEDS_TESTS | Non-email temporary identity: Random usernames; duplicate display names allowed | auth/service/RegistrationService.java | Test uniqueness and concurrent creation |
| G-03 | PARTIALLY_IMPLEMENTED | Editable social identity: Own endpoint, uniqueness; no reserved identifiers or row lock | profile/service/ProfileService.java | Normalize, reserve names, validate presentation; lock owner updates |
| G-04 | DEFECTIVE | Optional removable avatar: Empty string clears server reference; UI lacks avatar control or URL validation | profile/entity/Profile.java; tools/[tool].tsx | Expose clear control and safe HTTPS reference validation |
| G-05 | PARTIALLY_IMPLEMENTED | Private default: DB/entity private; UI public fallback | profile/entity/Profile.java; tools/[tool].tsx | Safe loading and private fallback; preserve historical choices |
| G-06 | DEFECTIVE | Location privacy: showLocation controls but private-profile audience ignored | profile/service/ProfileService.java | Apply field audience and separate eligibility explanation |
| G-07 | IMPLEMENTED_NEEDS_TESTS | Age minimization: No registration age value required; explicit legacy year control | profile/service/ProfileService.java | Test raw DTO exclusion and keep new-user age prompts absent |
| G-08 | DEFECTIVE | Message boundaries: Request checks exist; unsafe UI fallback, silent rollback; established block review required | messaging/repository/MessagingRepository.java; safety-settings.tsx | Accurate state/error and block checks on established routes |
| G-09 | PARTIALLY_IMPLEMENTED | Truthful content controls: Warning gate ignores blur preference; other settings not connected | features/content/post-card.tsx; post/repository/PostReadRepository.java | Separate controls, discovery filtering, truthful direct-link presentation |
| G-10 | DEFECTIVE | Private-profile projection: Bio leaks through profile/search; aggregate/anonymous audit gaps | profile/service/ProfileService.java; search/service/SearchService.java | Redact bio/location/counts and block secondary exposure |
| G-11 | DEFECTIVE | Durable post audience: PUBLIC depends on current privacy; composer always PUBLIC | post/repository/PostReadRepository.java; social/service/SocialInteractionRepository.java; create.tsx | Conservative historical migration; central access predicate; real audience picker |
| G-12 | IMPLEMENTED_NEEDS_TESTS | Existing account data rights: Owner archive/preferences and erasure already present | account/service/AccountService.java; account/repository/AccountRepository.java | Extend progress export/erase evidence, document external avatar copies |
| H-01 | DEFECTIVE | Optional stable topics: Display labels stored; arbitrary values accepted; sensitive collection prerequisites unresolved | preference/repository/UserPreferencesRepository.java; onboarding.tsx | Stable slugs/unambiguous legacy mapping, default-off collection gate |
| H-02 | PARTIALLY_IMPLEMENTED | Optional support style: Free-form string; null preserves so UI cannot clear | preference/dto/UpdateUserPreferencesRequest.java | Validate catalog enum, explicit empty clear |
| H-03 | DEFECTIVE | Independent safe defaults: Single toggle couples blur/stronger; overstated safety copy | onboarding.tsx | Explain defaults, use settings for independent controls |
| H-04 | DEFECTIVE | Owner partial persistence: Owner-scoped; read/update race can overwrite concurrent changes | preference/repository/UserPreferencesRepository.java | Row lock, validation and explicit clear semantics |
| H-05 | DEFECTIVE | Skip distinct from complete: Both call finish/boolean true | onboarding.tsx; (app)/_layout.tsx | Explicit durable state and compatibility route gate |
| H-06 | MISSING | Durable resume: Step only in component state | onboarding.tsx | Store last confirmed step and restore owner-only |
| H-07 | PARTIALLY_IMPLEMENTED | Edit later: Content settings exist; personalization lacks clear reentry | profile.tsx; safety-settings.tsx | Resume/edit entry without forced enrollment |
| H-08 | IMPLEMENTED_NEEDS_TESTS | No mandatory health disclosure: Minimal registration and optional selections | auth/service/RegistrationService.java; onboarding.tsx | Preserve and test zero-input journey/API |
| H-09 | PARTIALLY_IMPLEMENTED | Neutral empty preference journey: Empty arrays supported; clear/routing incomplete | onboarding.tsx | Allow empty, neutral Support destination |
| H-10 | PARTIALLY_IMPLEMENTED | Support/treatment boundaries: Boundaries partly present; availability claims need precision | support.tsx; onboarding.tsx | Concise truthful boundaries and no automatic care |
| H-11 | DEFECTIVE | Truthful purpose: Claims ranking that is not implemented | onboarding.tsx | Explain actual navigation/storage/audience/delete effects |
| H-12 | DEFECTIVE | Actual support destination: Routes based on obsolete intendedRole | onboarding.tsx | Allowlisted actual feed/communities/support/articles |
| H-13 | PARTIALLY_IMPLEMENTED | Separate professional applications: Dedicated flow exists; ordinary onboarding still role-routes | profile.tsx; onboarding.tsx | Remove role routing, retain server verification |
| H-14 | BLOCKED_EXTERNAL_DEPENDENCY | Privacy-preserving measurement: No approved anonymous analytics/retention infrastructure | preference/repository/UserPreferencesRepository.java | Keep analytics disabled; document external policy decision |

Database plan: additive V40 freezes legacy audiences, adds explicit onboarding state/progress and private legacy topics. Profile privacy choices, social relationships, roles, sessions and policy evidence remain intact. Product contracts must be read before implementation. Sensitive topic collection and analytics remain gated pending external decisions.
