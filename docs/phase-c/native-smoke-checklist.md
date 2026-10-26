# Phase C native checks — NOT EXECUTED

Run on Android and iOS with a test backend, isolated database, configured secure synthetic mail transport and explicitly approved test service region. Never use production people or email accounts.

1. Welcome→Create account; validate empty/malformed inputs, age self-attestation, current Terms links/acceptance, optional profile omission and country selection. Use TalkBack/VoiceOver and hardware keyboard; verify first invalid focus/announcements, text scaling and keyboard does not obscure CTA.
2. Paste and autofill a 15–128-code-point password, including long Persian/emoji passphrase. Toggle visibility, check password manager/new-password attributes and mixed RTL/LTR credentials. No confirmation required.
3. Slow/offline/timeout/server-error/rate-limit submission; repeat taps. Confirm no duplicate account, readable feedback, retained in-memory non-sensitive values, retry and verification after uncertain success. Check shared network does not cause undue blocking with configured limits.
4. Submit minimal account; capture delivered code through test transport, verify PENDING cannot access social routes. Verify code→activation→optional onboarding skip→permitted feed; logout/signin again. No services in an unapproved region.
5. Close/restart app during verification; return via Sign in or Already submitted/Verify email. Re-enter email, resend, use expired/wrong/consumed code, recover and verify. Never persist password or code in AsyncStorage/logs. Native session credentials keep inherited secure storage.
6. Change published Terms between initial review/submission, then between signup/verification. Reaccept current version without resubmitting profile information. Verify old acceptance evidence stays intact.
7. Later choose Persian display name and a unique username; existing names/roles/profile privacy remain intact. Professional/listener application still needs separate approval. Check second device and external email app return.
8. Visual review at smallest supported device width, landscape, largest supported system text scale, safe areas, contrast and focus. Record OS/device/runtime, commit and pass/fail evidence.

Do not mark these checks passed based on a web screenshot or a JavaScript bundle export.
