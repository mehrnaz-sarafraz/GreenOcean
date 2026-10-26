# Geography and service availability

V32 seeds the JDK 21 ISO 3166-1 country catalog, with English, Persian and Arabic display names and a provenance/version marker. It does not enable services. This catalog describes country identifiers, not deployment approval or current device location.

V31 adds country/subdivision/city tables. Subdivisions use ISO 3166-2 codes; cities use stable numeric GeoNames IDs, with version and source import responsibility recorded by operators. Composite subdivision/country relationships prevent cross-country assignments. Activation validates city, country and subdivision ownership before saving. No giant city enum or fabricated production location list is used.

Residence country, current country and preferred language have separate fields. A current country, when provided, can further restrict access. Self-declared geography is not a verified device location. Optional profile location is separate and cannot authorize services. Changing profile location does not rewrite residence/permissions.

`regional_services` separately controls COMMUNITY, LISTENER, PROFESSIONAL_DIRECTORY, PROFESSIONAL_CARE and URGENT_RESOURCES with AVAILABLE/UNAVAILABLE/LIMITED/UNDER_REVIEW. Missing rows mean UNDER_REVIEW and denied restricted operations. LIMITED also remains denied until a specific limited capability contract is implemented. Crisis-directory and curated public resources remain accessible without an account, including when restricted services are unavailable.

Only explicit server-side configuration may enable a country/service. Do not ship test rows to production. The tests insert synthetic configuration inside isolated/rolled-back fixtures. A reviewed pilot manifest should be deployed transactionally by an operator, for example inserting an approved ISO country and service into regional_services. No administration panel is added.

API: GET `/geography/countries?q=`, `/geography/subdivisions?country=`, `/geography/cities?country=&subdivision=&q=`, `/geography/services?country=`. Search results are bounded; local names can be searched. Signup does not ask for a city. Import authenticated, licensed reference datasets before enabling any local capability requiring cities; no production city dataset is bundled.
