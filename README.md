# Nagly

**Someone who cares.** A hydration + medication reminder where a family persona (Mom, Dad, Dadi, Bestie…) nags you in their own voice.

## Flutter app (`app/`) — the shipping version
Android-first, Flutter 3.47, light theme only, local-first (SQLite), no accounts.

```bash
cd app
flutter test                       # 33 tests: domain logic + onboarding/trial/ad-unlock flows
flutter run                        # debug on emulator/device
flutter build appbundle --release  # signed with android/key.properties (not in git)
```

- `lib/domain/` — personas & lines, mood engine, streaks, bond meter, nudge planning, access/trial/monetization rules
- `lib/services/` — local notifications (lock-screen actions work with the app killed), RevenueCat billing, AdMob rewarded ads + RevenueCat Ads tracking, OneSignal
- `lib/config/integrations.dart` — fill in keys and set `sandboxMode = false` to go live
- `store/` — Play icon, feature graphic, listing copy & data-safety answers
- Store icons are rendered from the in-app logo: `flutter test test/generate_assets_test.dart --dart-define=GENERATE_ASSETS=true`

See [SHIPATON.md](SHIPATON.md) for the competition strategy, submission text, OneSignal campaigns and demo script. Privacy policy & terms live in `docs/` (GitHub Pages).

## Kotlin Multiplatform app (`shared/`, `androidApp/`, `iosApp/`)
The original KMP/Compose version, kept for reference. See [HANDOFF.md](HANDOFF.md).
