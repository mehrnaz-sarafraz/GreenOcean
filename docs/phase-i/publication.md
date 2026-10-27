# Phase I publication record

Reviewable draft: [PR #7](https://github.com/mehrnaz-sarafraz/GreenOcean/pull/7), branch `feature/phase-i-first-real-support`, base `feature/phase-gh-profile-privacy-onboarding` at `38c114add7ce1d0c8ffcce8c68239b43ca004eaa` (PR #6). No merge, force push or deployment.

GitHub connector object publication preserves identical file trees, but remote commit IDs differ from local IDs because commit metadata differs. Focused commits:

| Purpose | Local commit | Published commit |
|---|---|---|
| Backend lifecycle/authorization/migration/database tests |870297b|9247e7904b22e194a0cf0ae7e0841cec119c544b|
| Member/listener/live-chat UX and helpers |eb9064d|181f5b9de4f4d834eb8dd50677685ecb0b5abf87|
| Browser journeys, isolated QA/CI and hydration synchronization |42a517b|f45aeb51b6940fe21670fba6c74f7d5a32de84b6|

The following documentation commit contains the supplied specification, initial gap matrix, contract, final18-requirement report, state/API/privacy/operations/handoff and actual local test evidence. Its exact published ID is visible in PR history. Production source was unchanged by that documentation commit; `test-evidence.json` records source hashes.

Local final results:234 backend,64 helpers,83 fixture browser,20 real-server browser tests passed; typecheck/lint passed (11 existing warnings); all-platform JS/assets export exit0. Native/device, real staffing/policy/reviewer ceremony, load and legal/retention/monitoring launch gates remain open. See [delivery report](delivery-report.md).

GitHub Actions is independently tracked on the exact final PR head. At document publication it is pending, not claimed passed. The PR description is the current record of final run URLs/status so a documentation-only status update does not launch another build. [Actions runs](https://github.com/mehrnaz-sarafraz/GreenOcean/actions) retain standard workflow evidence. Keep the draft/launch gates separate from whether automated checks pass.

## Owner-requested foreground continuation

The re-supplied specification was identical to the committed specification. Executable continuation `4e3bfb4bfcd3b0f080831ffa991e7dab0940174b` fixes stale foreground polling across background/blur/return, with four helper regressions and one actual-component browser regression. [CI40534723099](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40534723099) succeeded on that exact source: 234 backend, 68 helper, 84 fixture-browser and 20 real-server-browser tests (406 total), types/lint/all-platform export. The following report commit is documentation only and preserves that executable source. Original publication notes above are the historical first-delivery snapshot. No merge, deployment or real listener enablement occurred.
