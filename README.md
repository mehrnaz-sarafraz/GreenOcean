# GreenOcean Phase A candidate

Backend: Java 21 / Spring Boot / PostgreSQL / Flyway. Frontend: Expo 57 / React Native / TypeScript.

Start with [the requirements/audit](docs/product/phase-a-requirements.md), [test results](docs/testing/phase-a-results.md), [test matrix](docs/testing/phase-a-test-matrix.md), and [rollout plan](docs/migrations/phase-a-rollout.md). The original approved specification is retained in docs/product/phase-a-approved-specification.md.

Run isolated Windows QA with `powershell -File scripts/run-phase-a-qa.ps1` (PostgreSQL 18 binaries required). It creates a new temporary cluster and does not use the developer/user database. On Linux/CI use a disposable PostgreSQL database and set GREENOCEAN_TEST_DB_URL, GREENOCEAN_TEST_DB_USER and GREENOCEAN_TEST_DB_PASSWORD before `backend/mvnw clean verify`. Tests intentionally refuse an unspecified database.

Frontend checks: Node 24, `npm ci`, `npx expo customize tsconfig.json`, `npm run typecheck`, `npm run lint`, `npm test`, `npx expo export --platform web`. Native-helper tests need Node 24 but no package download. Android/iOS device testing is separate.

The optional `embedded-local` Maven profile retains the old packaged Windows PostgreSQL launcher dependency; normal production/CI builds do not download Windows database binaries.

No pilot service region is enabled by default. New accounts can verify email and manage their account while services await rollout. Production requires the explicit prod profile, configured mail gateway, a separate encryption key, exact HTTPS origins, an offline breached-password hash corpus and a reviewed legacy-account grace deadline. Administrative production access remains disabled pending MFA. Do not deploy with the development defaults.

This branch is intended for a draft pull request and review. It must not be merged or deployed automatically.
