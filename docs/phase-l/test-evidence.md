# Phase L executed evidence

Final tested executable source: `8843a613a52ac5120c97b3eeddba6a6da75c25ca`. Machine summary: test-evidence.json. The counts below are from the clean published follow-up execution; baseline history and corrected initial failures are retained separately in audit-follow-up.md. All counts below are from final reports, not assertions that code exists.

| Suite / check | Actual result | Command |
|---|---|---|
| Complete A–L backend, disposable PostgreSQL | 327 passed;0failed/errors/skipped | `cd backend && bash mvnw -B verify` with isolated GREENOCEAN_TEST_DB_URL |
| Frontend logic | 69 passed;0failed/skipped | `cd frontend && npm test` |
| Complete browser/component integration | 101 passed;0failed/skipped | `cd frontend && CI=1 npx playwright test` |
| Complete real-backend browser journeys | 27 passed;0failed/skipped | Start PhaseCLiveServer + disposable PostgreSQL; `cd frontend && CI=1 npx playwright test --config playwright.live.config.mjs` |
| TypeScript | Passed | `npm run typecheck` |
| ESLint | 0errors;11 pre-existing warnings | `npm run lint` |
| Web/Android/iOS JS and assets | Exported successfully | `CI=1 npx expo export --platform all` |
| Source secret patterns and tracked historical migration hashes | No candidate credentials;31 tracked originals unchanged | `python3 scripts/phase_k_source_checks.py` |
| Published L3 GitHub CI | All four jobs successful at head `9ed4998` | [Run40568529464](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40568529464), independently read on2026-10-28 |
| Physical native devices, TalkBack/VoiceOver, actual staff key custody | NOT EXECUTED | Not inferred from exports or virtual keys |

Historical original L3 local runtime (not the current CI runner): OpenJDK21.0.12, PostgreSQL16.15 with existing V0 UUID compatibility; repository CI configuration uses PostgreSQL18. Local Java Mockito agent was supplied explicitly through Surefire argLine because self-attach is restricted in this container. PostgreSQL ran only on isolated loopback with synthetic users. The container maps one UID; its disposable PostgreSQL startup used a test-only UID shim outside the repository. No production/user database was used. These are functional tests, not production capacity or real key-custody evidence.

L1/L2 remote heads991c77c/fd908d4 were checked through GitHub: backend, frontend, registration-live and release-scans completed successfully. Their historical successful CI is not called new L3 CI. Phase K PR9 is merged into main30a4b3a; L1 PR10 and L2 PR11 remain stacked separately.

## Four-persona evidence

| Persona | Actual executable evidence |
|---|---|
| Happy | L1 final publication/draft; L2 follow consent and saved/replies; L3 allowed review, appeal and independent reinstatement; complete live L1/L2/L3 |
| Creative | Persian/ZWNJ/Arabic letter variants and Unicode fixtures; nested anonymous replies; stable cursor under inserts; narrow Persian RTL browser cases; reload recovery |
| Confused | No implicit identity; pending/revision messages; duplicate appeal conflict; failed-save text retained; recoverable appeal transport failure; bilingual review guidance |
| Malicious | Missing identity/forged ordinary staff access; other-owner drafts/appeals; stale revision and duplicate review; concurrent decisions/appeals; anonymous Following/notification protections; report and edit flood limits; linked generic appeal bypass; EF key replay and I listener isolation regressions |

Concurrency evidence uses real PostgreSQL transactions: one winning moderation decision, one appeal for simultaneous retries, and an edit that invalidates an older approval. Existing auth/follow/report/live races remain in the complete backend/live suites. Serialized API tests inspect anonymous author, notice and export responses, not only Java objects.

## Failures found and resolved

An initial L3 appeal query used PENDING while the existing appeal schema uses OPEN; fixed to reuse the real lifecycle. A test initially expected an IllegalArgumentException from a translated repository error; the bypass now returns the explicit domain ConflictException. Legacy activation and language tests could interact with server-rendered controls before hydration; readiness now waits for account policy initialization / language effect, retaining every behavioral assertion. An initial staff browser mock did not match nested URLs; its route was corrected. None of those failed attempts is counted as final passing execution.

## Moderation quality and residual limits

Local deterministic examples cover legitimate distress/anger, quoted/negated abuse, direct threats/insults, promotion, private information, HTML-like input, mixed scripts/format characters, Persian idioms and bounded invalid/long text. Safe links and quotations can be over-held (false positives). Indirect hate, sexual harassment, coercion, transliteration, homographs and context outside the submitted text can be missed (false negatives). These examples do not establish an accuracy percentage or comprehensive semantic moderation. Unknown rules-only cases can still escape recognized signals; production publishing remains gated on approved policy, human operations and the existing K blockers.

Review states preserve originals and append human decisions; owner exports include explanations/appeals without reviewer identity, erasure removes them with owned assessment data. Removal does not erase previously viewed/copied text. External processing, evidence retention/holds, staffing and response commitments still require actual approval. No external AI was enabled.

## Published evidence correction — 2026-10-28

GitHub job logs directly confirm324 backend,69 logic,99 browser and27 real-stack tests (519 total) at `9ed4998`. Backend job122416934849 reports zero failures/errors/skips; frontend122416935209 reports69 logic and99 browser passes; live122416934664 reports27 passes. This verification reads existing CI and is not a fresh local execution. PR12 exists and the previous upload-blocked labels are obsolete. Native-device and actual staffing evidence remain unexecuted/unverified. Follow-up corrections/tests: audit-follow-up.md.

Final follow-up clean CI:327+69+101+27=524 passes at `8843a613a52ac5120c97b3eeddba6a6da75c25ca`. Current CI uses Java21/PostgreSQL18/Node24; native tests remain NOT_EXECUTED.
