# E/F native and assistive-technology validation — NOT EXECUTED

JavaScript/asset exports for Android and iOS are compilation checks. They do not demonstrate device, native binary, Keychain/Keystore, screenshot, backup, deep-link or screen-reader behavior. Record build SHA, device/OS, tester, exact result and evidence before clearing these gates. Use only synthetic accounts and text.

| Check | Android | iOS | Required observation |
|---|---|---|---|
| Login, show/hide, paste, autofill | NOT RUN | NOT RUN | Passwords remain masked after blur; email/password LTR in EN/FA; password manager works; duplicate submit guarded |
| Recovery intent | NOT RUN | NOT RUN | Only approved greenocean recovery destination; token scrubbed from navigation; valid/expired/replayed link; no token in logs or shared previews |
| Secure storage | NOT RUN | NOT RUN | SecureStore restricted accessibility; no bearer in AsyncStorage; no plaintext draft storage; partial-write/read/delete failures fail closed |
| Backup, restore, uninstall | NOT RUN | NOT RUN | Confirm Expo backup exclusions, Keychain persistence and device-only restoration behavior; a missing/inaccessible key requires login |
| Background privacy | NOT RUN | NOT RUN | App-switcher/capture prevention works before private content; inactive state hides content; public resources remain available |
| Five-minute boundary | NOT RUN | NOT RUN | At <5 minutes hidden draft can return; at >=5 minutes private subtree/credentials discarded and server sign-in required. This is not biometric MFA |
| Session bounds | NOT RUN | NOT RUN | Mobile 30-day overall/24-hour idle, pending 1-hour/15-minute, staff 12-hour/30-minute policy enforced by server; polling does not refresh idle |
| Password confirmation | NOT RUN | NOT RUN | Native current refresh token bound to proof; new access/refresh pair saved securely; old bearer and refresh cannot regain privileges |
| Change/reset/revoke/logout all | NOT RUN | NOT RUN | Correct success, confirmation, retry, immediate local private-cache clear and persisted server revocation |
| Offline sign-out | NOT RUN | NOT RUN | No claim of confirmed server revocation; encrypted pending revocation survives reconnect/restart; public pages remain usable |
| Account switching and late responses | NOT RUN | NOT RUN | A's feed, messages, journal, consultations and unfinished inputs never appear for B; old downloads rejected |
| Draft expiry | NOT RUN | NOT RUN | Memory-only drafts are lost on logout/expiry/long background; no hidden unencrypted persistence introduced |
| Exports | NOT RUN | NOT RUN | Temporary native moderation CSV removed after sharing success/cancel/error; force-kill cleanup and explicit external saved copies assessed separately |
| Staff security keys | NOT RUN | NOT RUN | Native unsupported path states unavailable; it never silently substitutes password/email for WebAuthn. Approved HTTPS staff browser required |
| Accessibility | NOT RUN | NOT RUN | TalkBack/VoiceOver labels/order/error announcements; keyboard and largest practical text; 320/375/390 widths, landscape and RTL |

Compromised/rooted devices can defeat OS controls. Do not describe SecureStore or capture protection as absolute guarantees. User-saved exports are outside application recall. Production staff access stays disabled until approved origin, factor/recovery policy and hardware validation are complete.
