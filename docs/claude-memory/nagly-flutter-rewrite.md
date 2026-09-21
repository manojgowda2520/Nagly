---
name: nagly-flutter-rewrite
description: "Nagly app is being rewritten from Kotlin Multiplatform to Flutter (decided 2026-09-19), Play Store target, fakes for integrations"
metadata: 
  node_type: memory
  type: project
  originSessionId: ba2c6623-702e-454f-b3a8-5ba0c047ca70
  modified: 2026-09-18T19:10:21.335Z
---

On 2026-09-19 the user decided to rewrite Nagly (repo /Users/m80-admin/Claude/Nagly, github manojgowda2520/Nagly) in Flutter, on a `flutter` branch; Kotlin code kept as reference. Same app id com.manojbuilds.nagly. Target: Play Store-ready for Shipaton 2026 (ends Sep 30).

**Why:** User wants Flutter and wants to win the Shipaton prizes; delegated scope decisions to Claude ("full ready app, decide yourself").
**How to apply:** Scope = water parity + Care Modes (Water + Medication) + 7-day trial + 3-plan paywall per HANDOFF.md. Build RevenueCat/AdMob/OneSignal behind fakes (user has some keys, will provide later). User does not want to do manual steps — Claude installs tools itself (no sudo; tools in ~/development, SDK in ~/Library/Android/sdk). User accepted Android SDK licenses. See [[nagly-user-workflow]].

Publishing: user has a fully functional Google Play Console account (confirmed 2026-09-19) — Play Store is the primary publish target, no 14-day closed-test blocker.

Status 2026-09-19 end of session: Flutter app complete in app/ on branch `flutter` (4 commits, not pushed). 33 tests pass. Signed release AAB built (sandboxMode=true — fakes, NOT for production). Upload key in ~/development/nagly-keys/. Waiting on user for: RevenueCat goog_ key, OneSignal App ID, AdMob ids; Play internal-testing upload so IAP products can be created. Run `flutter test` via perl alarm timeout (macOS has no `timeout`); path_provider_foundation pinned to 2.4.1 because macOS 27 CLT linker breaks objective_c native asset.
