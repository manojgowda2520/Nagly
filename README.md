# Nagly

**Someone who cares.** A hydration + medication reminder where a family persona (Mom, Dad, Dadi, Bestie…) nags you in their own voice.

## Flutter app (`app/`) — the shipping version
Live on the App Store and Google Play. Flutter 3.47, one codebase for iPhone and Android, light theme only,
local-first (SQLite), no accounts, no ads (payments only, via RevenueCat).

```bash
cd app
flutter test                       # domain logic + onboarding/trial/paywall flows
flutter run                        # debug on emulator/device
flutter build appbundle --release  # signed with android/key.properties (not in git)
```

- `lib/domain/` — personas & lines, mood engine, streaks, bond meter, nudge planning, access/trial/monetization rules
- `lib/services/` — local notifications with one-tap reminder buttons, RevenueCat billing, OneSignal
- `lib/config/integrations.dart` — public SDK keys/IDs only (no secrets in git)
- `store/` — store icons, screenshots, listing copy, demo video sources
- Store icons are rendered from the in-app logo: `flutter test test/generate_assets_test.dart --dart-define=GENERATE_ASSETS=true`

See [SHIPATON.md](SHIPATON.md) for the competition strategy, submission text, OneSignal campaigns and demo script. Privacy policy & terms live in `docs/` (GitHub Pages).

The original Kotlin Multiplatform prototype was removed on 2026-10-05; it is preserved in git under the tag `kmp-archive`.
