---
name: nagly-ios-no-ads
description: Nagly has no ads on any platform (removed Oct 2 2026, v1.0.5); iOS and Android are payments-only
metadata:
  type: feedback
---

Nagly has no ads anywhere since 1.0.5 (commit 2409e5b, 2026-10-02): google_mobile_ads, the ads service and the remote `monetization_mode` switch were removed. iPhone and Android build from the same payments-only code (RevenueCat Lifetime/Annual/Monthly).

**Why:** the owner first banned ads on iOS over App Store policy worries (2026-09-22), then asked to "remove ads concept from the app make ios and android same" (2026-10-02).
**How to apply:** never re-add ad SDKs, ad placements, ATT prompts or AD_ID permission. Play Console Advertising ID declaration should be "No" once 1.0.5 is approved. See [[nagly-flutter-rewrite]].
