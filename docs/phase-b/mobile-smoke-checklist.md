# Phase B mobile smoke checklist — not executed

No Android/iOS simulator or physical device execution is claimed. Chromium responsive/RTL coverage does not substitute for native validation.

For each Android and iOS device, with a clean guest session and again with an active/pending/expired session:

- Open Welcome, verify readable mission, three paths and contextual capacity/boundary text.
- Confirm safe-area insets/notch/bottom navigation, portrait/landscape scrolling and reachable CTAs.
- Enable system large text (including maximum practical accessibility size); check headings, cards, footer and buttons do not clip.
- Use TalkBack/VoiceOver: logical headings, labels, language controls, no decorative icon announcements; activate all actions.
- Switch English/Persian, restart, and confirm direction and saved language. Email/token fields remain LTR.
- Tap signup twice; return via Back; sign in; pending owner must reach activation, not protected community.
- Reach public urgent help during guest/authentication-loading/offline states; choose/change country without GPS; verify honest no-local state.
- Open a published policy, deep link, Back, retry network failure, and unavailable document. No stale hardcoded policy appears.
- Verify HTTPS/tel resource links with platform confirmation, malformed-link rejection, and returning from the external app.
- Open contact/recovery/verification forms; show keyboard and confirm fields/actions remain reachable. Never enter real health history in QA.
- Test reduced motion, offline recovery and font-loading failure. Public urgent information must not be blocked by decorative fonts.

Record device/OS/app version, build identifier, persona, result and screenshots. Product, operational and legal release gates inherited from Phase A still apply.
