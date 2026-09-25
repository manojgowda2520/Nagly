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
  - Android developer verification: identity filled, com.manojbuilds.nagly **Registered** (checked 2026-09-21).
- **RevenueCat** (project "Nagly", app "Nagly (Play Store)"):
  - Products created: `nagly_lifetime`, `nagly_pro:annual`, `nagly_pro:monthly`; all attached to entitlement **`pro`**.
  - Offering **`default`** ("Nagly Pro plans") — **done and Current**: `$rc_lifetime`→nagly_lifetime, `$rc_annual`→nagly_pro:annual, `$rc_monthly`→nagly_pro:monthly.
  - **Service Account Credentials JSON is NOT uploaded** → purchases can't be validated until the user uploads it (Play Console → Setup → API access → service account with financial permissions; RevenueCat → Apps → Nagly (Play Store)). It's a secret — the user uploads it, never paste it to Claude.
- **OneSignal** (app 2858113c-…): 3 segments, 5 persona push templates, 3 Journeys (win-back, streaks, trial ending) — all **Drafts**; set live only after user approval, ideally after Production approval.
- **GitHub Pages**: privacy (with `#delete` section) and terms live at manojgowda2520.github.io/Nagly/.

## Payments / ads switch (remote)
RevenueCat → Product catalog → Offerings → default → Metadata: `{"monetization_mode": "payments" | "ads" | "both"}`.
payments = paywall only, no ads (current). ads = no purchases, rewarded ads unlock 24h. both = paywall + ads on locked voices.
iOS is always payments. **iOS must have NO ads at all** (owner's rule, App Store policy fear): useAdMob=false on iOS; before the first iOS build, strip the google_mobile_ads pod from the iOS binary too (no GAD keys, no ATT prompt, no SKAdNetwork). Applied on next app launch. Code: `Integrations.applyRemoteMode` in lib/config/integrations.dart, read in RevenueCatBillingService.init.

## Pending (in order)
1. ~~RevenueCat offering~~ (done 2026-09-21).
2. ~~Service-account JSON~~ (2026-09-21): GCP project nagly-3e4a0, APIs on, SA revenuecat@nagly-3e4a0.iam.gserviceaccount.com invited in Play (Nagly only), JSON uploaded to RevenueCat. Credentials VALID; RTDN topic projects/nagly-3e4a0/topics/Play-Store-Notifications connected + test received (all one-time products).
3. ~~AdMob~~ (2026-09-21): personal AdMob account, app ID ca-app-pub-7379182928133388~6057773279, rewarded unit ca-app-pub-7379182928133388/4618814501 (test unit still used in debug). App is "not listed" in AdMob; link Play store in AdMob once Nagly is public. Built 1.0.0+3 AAB; user uploads.
4. Real test purchase from the closed-testing link.
5. Ship 1.0.0 (3) to Production as an update.
6. OneSignal Journeys "Set live" (user approval).
7. Demo video + Devpost submission before **Sep 30, 11:45 pm PDT** (texts/script in SHIPATON.md).
8. iOS (XcelAudit Apple team VGK8LY4A86): bundle ID registered (IAP + Push), app created (Apple ID 6814609746, SKU nagly-ios), IAPs created: group "Nagly Pro" → nagly_pro_annual (1y), nagly_pro_monthly (1m); non-consumable nagly_lifetime. Done 2026-09-22: prices ($29.99 / $19.99 + 7-day free trial / $1.99, 175 countries), IAP names, RevenueCat App Store app (appl_ key in app, 3 products -> pro, in default offering), ASC server notification URLs -> RevenueCat, app icon, subtitle, category Health & Fitness + Lifestyle, content rights, age 4+, privacy types (not yet Published), price Free. Build 3 uploaded (Ready to Submit, location purpose-string warning). Build 4 (adds NSLocationWhenInUseUsageDescription) archived but NOT uploaded: Xcode lost its Apple account sign-in. iOS builds: `tool/build_ios.sh build ipa --release` (ad-free stub). Upload: xcodebuild -exportArchive with destination=upload, team VGK8LY4A86. Screenshots in app/store/ios_screenshots (1242x2688).
Still need: user's App Review contact info; availability (China?); version page save + select build; IAP review screenshots; OneSignal APNs .p8; submit (user OK).

## Machine setup notes (from the original Mac)
- Flutter 3.47.x, JDK 17, Android SDK 36. Run tests with `flutter test` (on macOS without `timeout`: `perl -e 'alarm shift; exec @ARGV' 240 flutter test`).
- `path_provider_foundation` is pinned to 2.4.1 (macOS CLT linker issue).
- Release signing: `app/android/key.properties` + upload keystore are **not in git**. Copy them securely from the original Mac (`~/development/nagly-keys/`) — without them you can't sign updates Play will accept. Back them up.
- Claude in Chrome: Play Console is now also signed in on the **personal** Chrome profile, so personal Chrome covers everything.

## Rules the user set
- In the Work Chrome profile, touch only the Nagly app in Play Console.
- Ask before accepting terms, submitting for review, publishing, or setting Journeys live.
- Never handle passwords, service-account JSON, or secret keys (only public keys/IDs).

## Status 2026-09-22 evening
- iOS 1.0 build 5 + 3 IAPs + Nagly Pro group SUBMITTED to App Review (auto-release on approval). Privacy published, not a medical device, 174 countries (no China), Free.
- TestFlight: external group "Public testers", build 5 waiting beta review, public link https://testflight.apple.com/join/neVfb471
- OneSignal iOS APNs active (key 9B7V775A4K, topic-specific Production); 3 Journeys live.
- RevenueCat monetization_mode = "both" (Android ads on for Catvertising; iOS always payments).
- Android build 5 AAB built (undo-snackbar fix). Play submission 4 (prod build 2 + alpha build 3) still in review; upload build 5 after approval, then Production.
- Uploads from CLI hit "Failed to Use Accounts"; workaround: `open app/build/ios/archive/Runner.xcarchive` -> Organizer -> Distribute App.
- Pending: demo video + Devpost (Sep 30), AdMob store link after Play goes public.


## Status 2026-09-25
- **iOS 1.0 (build 5) APPROVED and LIVE on the App Store**: https://apps.apple.com/us/app/nagly-moms-water-pill-nags/id6814609746 (also live in IN, GB). TestFlight public link now joinable: https://testflight.apple.com/join/neVfb471
- Google Play: Production build 2 + Alpha build 3 still in review (since Sep 21). After approval upload build 5 AAB as Production update.
- Play notifications (Mobil80 account, Sep 24): app transfer from XcelAudit Technologies LLP in progress. User asked to confirm it doesn't include Nagly.
- Next: real iOS test purchase (user), video script, WhatsApp invite, Devpost fact sheet, OneSignal A/B split (Journey editor was not loading).

## Post-hackathon list (after Sep 30, 2026)
- **Welcome-trial reset on reinstall.** The 7-day app-managed trial start (`Keys.trialEndsAt`) lives only in the local SQLite db, so uninstall + reinstall gives a fresh trial. Store trials and purchases are tied to Apple ID / Google account and are not affected.
  - iOS: also write the trial start to the Keychain (survives uninstall) and read it back on first launch.
  - Android: no Keychain equivalent; check trial eligibility server-side via RevenueCat (e.g. a subscriber attribute) once there are real users.
  - Decided 2026-09-25: leave as is until after the deadline (low abuse risk: reinstall wipes history, streak, meds).
- iOS Notification Service Extension so OneSignal push images show on iPhone (Android already shows them).
- OneSignal A/B variant in the Win-back Journey (Journey editor was not loading on 2026-09-24).
