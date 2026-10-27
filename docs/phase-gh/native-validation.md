# Native validation — NOT RUN

CI exports Android/iOS JavaScript/assets with the same Expo code. This does not launch a device, exercise SecureStore/native networking, or prove VoiceOver/TalkBack behavior. Physical Android/iOS validation remains a release gate; Windows cannot run iOS Simulator. No native test is counted passed.

| Scenario | Android / TalkBack | iOS / VoiceOver | Expected |
|---|---|---|---|
| Fresh activated member | NOT RUN | NOT RUN | Private profile, no unsafe loading toggle, no health question required |
| Three optional steps / zero selections | NOT RUN | NOT RUN | Continue, skip step/all, distinct saved status; meaningful Support destination |
| Force stop after step 1/2 | NOT RUN | NOT RUN | Restore last confirmed owner progress after fresh launch |
| Switch accounts / background | NOT RUN | NOT RUN | A–F private-cache clearing; no cross-account restoration or app-switcher leak |
| Offline save / reconnect | NOT RUN | NOT RUN | Error, no false advancement; privacy rollback visible; retry works |
| Independent blur/discovery/request toggles | NOT RUN | NOT RUN | Accessible saved value; mandatory moderation unchanged |
| Profile edit, Persian name, remove avatar | NOT RUN | NOT RUN | Correct labels, Unicode/bidi, explicit removal, no keyboard overlap |
| Audience picker, community, broad confirmation | NOT RUN | NOT RUN | Restrictive initial audience; explicit wider consent; real membership required |
| Narrow screen, landscape, 200% text | NOT RUN | NOT RUN | No clipping/overflow, scroll/safe areas, large usable targets |
| Screen reader / external keyboard | NOT RUN | NOT RUN | Logical focus, checkbox/radio/switch states, errors announced |
| First support routes / unavailable listener | NOT RUN | NOT RUN | Valid authorized destination; truthful discovery fallback, no automatic interaction |
| Export/delete and revoked content | NOT RUN | NOT RUN | Existing reauthentication, new progress erased, inaccessible content cleared on re-entry |

Use only synthetic accounts and isolated services. Record device, OS/build, assistive-technology settings, actual results and issue links before changing any NOT RUN entry. Do not treat browser mobile viewport checks as native execution.
