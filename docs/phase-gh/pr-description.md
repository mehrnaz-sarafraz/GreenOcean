Privacy-first profiles and optional onboarding now keep historical post audiences independent of profile visibility. Existing PUBLIC posts are conservatively restricted to approved followers by V40; future wider publication requires explicit per-post confirmation. Profiles/search redact private details; preferences remain owner-only; skip, completion and resumable progress have distinct persisted states. First-support navigation uses real allowlisted routes and never grants professional status or starts interactions automatically.

The continuation completes saved-export privacy regressions: bookmarks do not retain post previews after follow/community access is revoked, either block direction applies, content is moderated/deleted or the author becomes inactive. It also makes historical migration tests independent of test order and fixes exact-duration retention test fixtures across the autumn DST change. Production retention/security rules and applied migrations are unchanged.

Validation executed locally on disposable PostgreSQL 18.6 and Java 21:

- `clean verify`: **200 tests passed**, no failures/errors/skips. Mockito starts as a test-only Java agent because dynamic attachment is restricted in the local container.
- `npm test`: **59 helper tests passed**.
- G/H browser UI with isolated API fixtures: **9 passed**.
- G/H actual Expo + backend + PostgreSQL browser journeys: **4 passed**.
- TypeScript, ESLint, whitespace checks and Expo web/Android/iOS JS/assets export: passed.

See `docs/phase-gh/delivery-report.md`, `requirements-matrix.md`/`.json`, `test-evidence.json` and `test-coverage-matrix.md` for all 26 individual requirement decisions and four-persona traceability. Physical Android/iOS, TalkBack/VoiceOver and production-volume migration timing remain unexecuted. Sensitive topic collection stays default-off pending legal/product approval; H-14 analytics remains disabled and externally blocked. A bundle export is not a device test.

This PR is stacked on E/F (#5), branch `feature/phase-ef-account-access-security`, not main. Review the profile/audience and onboarding contracts, V40 historical audience narrowing and forward recovery before rollout. No merge or production deployment is included.

Publication note: this description is prepared for the existing PR #6. The local continuation has not been pushed and this body has not been applied to GitHub in the current session. CI run 40475942112 succeeded for the previous remote head `42ca364`; new continuation commits require their own CI run after publication.
