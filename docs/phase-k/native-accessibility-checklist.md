# Native and assistive acceptance

Every physical-platform entry below is NOT_EXECUTED_DEVICE_REQUIRED for Android and iOS. No signed build, physical device, emulator, adb or xcrun was available in the Windows workspace. JS/asset export is build evidence only. Real native release remains blocked.

Record actual device/OS/build, installation/signing source, language/text/assistive settings, network state, timing, result and defect link for each case; do not infer a pass from a screenshot or browser test.

| Required case | Expected observable result |
|---|---|
| Signed installation/first launch and public Welcome | Correct environment, no production fixture endpoints, clear service boundaries |
| Registration15–128 Unicode/autofill/paste/keyboard | No truncation/composition trap, accessible labels, error focus, no secret preview |
| OTP verification/resend/expired code | Correct owner, current code, bounded retries, accurate unavailable delivery |
| Login/recovery/password change | One-use reset and every-device revocation; no token in URL/logs/clipboard |
| SecureStore/keychain and backup exclusion | Actual OS accessibility/migration/backup behavior; no plaintext secret in general storage |
| Background/screen lock5min | Private view obscured immediately; long return requires server sign-in; no stale account cache |
| Short background/foreground and force stop | Safe drafts within policy, fresh server authorization before enabling sensitive actions |
| Logout/current/all devices/account switch | Old private text/session cannot return through late network responses |
| Optional onboarding/skip/resume | No required health topics, no auto-join or automatic service enrollment |
| Private/public profile and historical post audiences | Only permitted viewers; public transition does not widen old posts |
| Listener ready then background with slow response | No background renewal or stale response resurrection; bounded server lease fallback |
| Request/accept/decline/cancel/expire | Honest server-confirmed state and accessible countdown; no chat before activation |
| Active messages/reload/disconnect/reconnect | Correct pair, composer disabled when unconfirmed/interrupted/terminal, no duplicate sends |
| End/block/suspend/revoke | Sending immediately denied and prohibited views cleared |
| Reports/appeals/consent/receipt | Owner-only status, no preselected grant or conflated Terms/privacy, real withdrawal effect |
| Export/correction/erase and failure retry | Own data only, correct reauth; external deletion failure not called completed |
| Offline at each sensitive action | Truthful retry/error and no false save/completion or silent re-enrollment |
| English/Persian/RTL/mixed email/code/UUID | Logical bidi placement, readable digits/secret fields and no clipped controls |
| Dynamic text200%, landscape/small screen | Usable content/actions with native keyboard and scrolling |
| TalkBack/VoiceOver | Logical reading/focus order, named buttons/fields, announced errors/loading/status |
| Keyboard focus/targets/contrast | Visible focus, accessible hit areas, no color-only state |
| Capture/app switcher previews | Actual screen/app-switcher protections tested per OS, safe failure if unavailable |
| Lock-screen/push privacy if enabled | No private message/topic/diagnostic preview; no push pipeline claimed where absent |
| Real staff WebAuthn / key loss/recovery | Approved RP/origin, physical/backup keys and operational provisioning; no email/password MFA downgrade |

Web evidence remains separate: final full actual-component browser suite includes responsive widths, FA/RTL, keyboard focus, contrast checks, enlarged text, safe literal policy output and error/status handling. See B/C/EF/I/J detailed evidence for exact browser assertions. This is not a claim that all WCAG or native accessibility requirements have been independently assessed.
