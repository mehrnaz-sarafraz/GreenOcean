# Phase B visual QA

Source revision `048387e53605b0a8c2ad5d7a1c00e422b9ced8e2`; successful [CI run](https://github.com/mehrnaz-sarafraz/GreenOcean/actions/runs/40319624695). Images come from the actual Expo browser application, using synthetic public API fixtures and no real member data. The downloaded browser artifact SHA-256 matched GitHub's recorded digest; provenance is retained in `test-evidence.json`.

The review inspected top/pathway/footer views, Persian desktop/mobile, large text and a failed policy request. Controls and body text wrap without horizontal clipping in the inspected images. Large headings require additional vertical scrolling at enlarged text. These are viewport captures at different ScrollView positions, not a single stitched full-page capture. They do not establish native device or assistive-technology results.

| Retained preview | Purpose |
|---|---|
| [Desktop 1440](screenshots/welcome-desktop-1440.png) | Header, mission, optional account entry, public resources and pathway cards |
| [Mobile 320](screenshots/welcome-mobile-320.png) | Narrow-width header, readable mission and reachable actions |
| [Pathway 320](screenshots/welcome-pathways-320.png) | Stacked card/body/action wrapping |
| [Persian 375](screenshots/welcome-persian-375.png) | RTL direction, translated copy, header/control wrapping |
| [Policy unavailable](screenshots/policy-unavailable.png) | Honest error, retry and public exit routes |

The full CI browser report includes every tested width and screenshot attachment. Native platform checks remain in [the unexecuted smoke checklist](mobile-smoke-checklist.md).
