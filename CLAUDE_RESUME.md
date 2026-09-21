# Resume Nagly with Claude (new machine / new account)

Say to Claude Code: **"Read CLAUDE_RESUME.md and docs/claude-memory/, then continue."**
Branch: `flutter`. State as of 2026-09-21.

## Where things stand
- **App**: Flutter, in `app/`. Version **1.0.0 (2)**, `sandboxMode=false` (real RevenueCat + OneSignal), AdMob still Google **test** IDs. 34 tests pass.
- **Play Console** (Mobil80 org account, Work Chrome profile; touch only the Nagly app):
  - All "Set up your app" tasks done (listing, 4 screenshots, Data safety, PEGI 3, declarations).
  - Closed testing (Alpha, tester list "Mobil80"): build 2 **approved and live**.
  - **Production: build 2 submitted by the user — in review.** Will go live when approved.
  - Products (all active): `nagly_lifetime` (purchase option `lifetime`, $29.99); subscription `nagly_pro` → base plans `annual` $19.99 (+ offer `trial-7d`, 7-day free trial) and `monthly` $1.99.
  - Notification: "Android developer verification by Sep 30, 2026" (account-level) — user should complete it.
- **RevenueCat** (project "Nagly", app "Nagly (Play Store)"):
  - Products created: `nagly_lifetime`, `nagly_pro:annual`, `nagly_pro:monthly`; all attached to entitlement **`pro`**.
  - Offering **`default`** ("Nagly Pro plans") with packages Lifetime / Annual / Monthly — was being saved when the session ended. **Verify it exists and that each package has the right identifier ($rc_lifetime, $rc_annual, $rc_monthly) and product.** Make it the Current offering.
  - **Service Account Credentials JSON is NOT uploaded** → purchases can't be validated until the user uploads it (Play Console → Setup → API access → service account with financial permissions; RevenueCat → Apps → Nagly (Play Store)). It's a secret — the user uploads it, never paste it to Claude.
- **OneSignal** (app 2858113c-…): 3 segments, 5 persona push templates, 3 Journeys (win-back, streaks, trial ending) — all **Drafts**; set live only after user approval, ideally after Production approval.
- **GitHub Pages**: privacy (with `#delete` section) and terms live at manojgowda2520.github.io/Nagly/.

## Pending (in order)
1. Verify/finish the RevenueCat `default` offering (above).
2. User: upload Play service-account JSON to RevenueCat (can take 24–36 h to activate).
3. User: send AdMob App ID (`ca-app-pub-…~…`) + Rewarded unit ID (`ca-app-pub-…/…`) → put in `app/android/app/src/main/AndroidManifest.xml` and `app/lib/config/integrations.dart`, bump to **1.0.0+3**, `flutter test`, `flutter build appbundle --release`. AAB is ~62 MB (too big for Claude's browser upload — user drags it into Play Console).
4. Real test purchase from the closed-testing link.
5. Ship 1.0.0 (3) to Production as an update.
6. OneSignal Journeys "Set live" (user approval).
7. Demo video + Devpost submission before **Sep 30, 11:45 pm PDT** (texts/script in SHIPATON.md).
8. Later (Oct+): iOS App Store version.

## Machine setup notes (from the original Mac)
- Flutter 3.47.x, JDK 17, Android SDK 36. Run tests with `flutter test` (on macOS without `timeout`: `perl -e 'alarm shift; exec @ARGV' 240 flutter test`).
- `path_provider_foundation` is pinned to 2.4.1 (macOS CLT linker issue).
- Release signing: `app/android/key.properties` + upload keystore are **not in git**. Copy them securely from the original Mac (`~/development/nagly-keys/`) — without them you can't sign updates Play will accept. Back them up.
- Claude in Chrome must be signed in to the same Claude account in both Chrome profiles (personal = RevenueCat/OneSignal, Work = Play Console).

## Rules the user set
- In the Work Chrome profile, touch only the Nagly app in Play Console.
- Ask before accepting terms, submitting for review, publishing, or setting Journeys live.
- Never handle passwords, service-account JSON, or secret keys (only public keys/IDs).
