# Nagly — Design

Static HTML design references for Nagly. Open either file in a browser.

| File | What it is |
|---|---|
| [`mockups.html`](mockups.html) | Full app mockups — all screens in Nagly's real design tokens: **00** lock-screen notifications (the core product), **01** onboarding funnel (10 steps), **02** daily use (both Home modes, chat timeline, insights), **03** personas & care modes + ad-unlock, **04** trial & 3-plan paywall, **05** OneSignal re-engagement engine. |
| [`overview.html`](overview.html) | One-page team overview — concept, core loop, notifications, personas, Care Modes, pricing, tech stack, and the Shipaton award strategy. |

## Notes
- These are **design references**, not app code. The app UI is built in Compose (`shared/`).
- Theme: the app ships **light-only**; `mockups.html` reflects that. `overview.html` is a document, so it follows the reader's light/dark.
- Live versions also exist as private Claude artifacts on the builder's account (see the links inside `overview.html`).
- Source of truth for decisions (pricing, Care Modes, awards, notification split) is [`../HANDOFF.md`](../HANDOFF.md).
