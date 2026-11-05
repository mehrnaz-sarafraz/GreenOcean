# Physical Android/iOS acceptance — DEVICE_TEST_REQUIRED

No physical device session is connected. All19 scenarios on both Android and iOS remain DEVICE_TEST_REQUIRED. Browser/RTL tests and Expo compilation are not native acceptance. Real provider media/push cases additionally require actual approved providers and reviewer authorization.

Prepare a new evidence package:

```powershell
python scripts/phase_q5_native_acceptance.py --initialize <new-private-file.json>
python scripts/phase_q5_native_acceptance.py --validate <private-file.json>
```

Use synthetic accounts only. Record physical model, OS, exact app build/commit, actual test date, reviewer, outcome, artifact SHA256 and failure reference. Keep screenshots/logs in controlled evidence storage; never commit private tokens or clinical content. Validation only reports missing cases or INDEPENDENT_REVIEW_REQUIRED; it never grants release approval.

Scenarios: registration; session recovery; logout; secure storage; biometrics/passkeys; Persian/RTL; screen readers; large fonts; background/foreground; network loss; real push; private journal privacy; listener readiness; appointment reminders; microphone/camera permissions; approved real clinical audio/video; app termination; account switch; stale sensitive cache clearing. Test access refusal, account-switch cleanup, token removal and delayed responses, not merely screen rendering. Separate local emulator diagnostics from physical evidence.
