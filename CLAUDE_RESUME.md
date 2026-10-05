# Resume Nagly with Claude (new machine / new account)

Say to Claude Code: **"Read CLAUDE_RESUME.md and docs/claude-memory/, then continue."**

## 2026-09-30 evening IST (older — see "Oct 4–5" at the bottom for latest)
- **iOS 1.0.3 (8)** submitted to App Review, auto-release. Fix: lock-screen buttons open the app so taps save
  (commit 1cae2b4, local only, not pushed). Needs a real-device check of "Took it" from the lock screen (TestFlight).
- **YouTube LIVE (Oct 1): https://youtu.be/cYa6-qM5r9I** (channel mj manoj) = Nagly_demo_v2.mp4 with music lowered (MUSIC_VOL 0.14). Thumbnail: app/store/youtube/Nagly_thumbnail.png. **Devpost SUBMITTED (Oct 1 ~01:00 IST)**: https://devpost.com/software/nagly-reminders-that-sound-like-mom (video embedded, wording fixed).
- Demo video: `app/store/video/Nagly_demo_v3.mp4` (1:50, 1080p). Built by `app/store/video/assemble.py`
  (`~/development/videnv/bin/python assemble.py` → writes Nagly_demo_v2.mp4; copy to v3).
  AI clips in `clips_q/K1..K5.mp4` (Veo 3.1 Quality via Google Flow), real iPhone recordings in `rec/`.
  Story: Arjun swipes machine alarms → memory of Amma's "drink water first" → Nagly nudges in her voice →
  he sips (open tumbler) → Amma gets "Appa" BP-tablet nudge (clear glass) → video call toast (clear glass).
  User rules for the video: no fake app features (tilt line removed), no closed-lid drinking, soft smiles,
  notifications must visibly arrive on the phone first.
- **Still to do (user):** upload video to YouTube (credit: Voices ElevenLabs.io · Music Google Flow Music ·
  Video Google Flow (Veo 3.1) · Motion HyperFrames) → paste link in Devpost Project details → change
  "without opening the app" to "with one tap on the reminder" in story/Challenges/OneSignal/Grand Prize answers →
  user ticks terms and clicks Submit (Claude must not submit).
- Devpost draft: devpost.com/submit-to/29969-revenuecat-shipaton-2026/manage/submissions/1202724-nagly-reminders-that-sound-like-mom
- Flow credits nearly used up; ElevenLabs free plan (~7,000 credits left).

## Where things stand (older, 2026-09-21)
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

## Status 2026-09-25 (late)
- Devpost work **paused by the user** (fact sheet ready in app/store/DEVPOST.md; video + written answers not started; deadline Sep 30 11:45 pm PDT, target submit Sep 28).
- Pending on user: upload iOS 1.0.1 (6) via Xcode Organizer (archive built 17:48, no ad SDK), test one judge promo code, ask friends for permission to quote them.
- Android 1.0.1 (6) AAB built (reward verification off); upload after Google approves the Sep 21 review.
- RevenueCat Targeting rule live: trial_end -> annual_first, medication_limit -> monthly_first, else default. Public TestFlight link disabled.

## Status 2026-09-26 (~1:30 AM IST)
- **iOS 1.0.1 (6) submitted to App Review** (Waiting for Review). Auto-release after approval. Contains: Spouse, Make it yours (example "Subbu Amma", no autocorrect on the name field), Meds & Supplements + quick picks, 8-step spotlight tour, placement-highlight paywall. Subtitle "Water, pills & supplement nags"; 7 screenshots Mom-first.
- Upload tip: `xcodebuild -exportArchive ... destination=upload` works once Xcode has an Apple account session (Validate App in Organizer refreshes it).
- Android 1.0.1 (6) AAB built 2026-09-25 22:42; upload after Google approves the Sep 21 review.
- If 1.0.1 still Waiting for Review on Sep 28, consider an expedited review request (deadline Sep 30).

## Oct 1 afternoon (deadline extended to 2 Oct 00:30 IST)
- Competition review: app/store/research/shipaton_gallery/ (scraper, pools, scores_*.json, Nagly_Shipaton_Review.pdf). Focus: OneSignal (#4 public) + Peace Prize; HAMM #2.
- Devpost story: added "Who it helps", "How OneSignal brings people back", "Where to look (design)", 3 GIFs (app/store/devpost/gifs). Numbers: 46 active customers, win-back 28 users ~14% CTR, in-app 75–80%.
- OneSignal: A/B push sent 15:45 IST (ab_tests/efe74594-e6c3-499d-91f6-8ae2d204fa45, 10+10 delivered); in-app "Hi from Nagly (everyone, once)" live; templates "Hydration check A/B".
- Android still In review. PENDING at ~22:00 when user says "refresh": D1 proof screenshots (Journey, A/B, RevenueCat), D2 upload to gallery, D3 update numbers + A/B result in story; user edits judge answers by hand (auto-edit blocked).

## Oct 2 (after Shipaton deadline)
- iOS 1.0.4 (9) LIVE (Settings fixes). iOS 1.0.5 (10) uploaded, ON HOLD (no user-visible change).
- Ads removed from the app entirely (commit 2409e5b): iPhone and Android now build from the same payments-only code.
- Android: 1.0.5 (10) submitted for review on Production + Closed testing (Alpha); Ads declaration -> No.
  Internal testing has 1.0.5 as a draft. AFTER APPROVAL: set App content -> Advertising ID -> No and submit.
- Final competition report: ~/Desktop/Shipaton-2026-Nagly-Awards-FINAL-After-Deadline.pdf (private; scores other teams).

## Oct 4–5 (switching Claude account again — START HERE)
- Status: Shipaton judging Oct 1–13, winners Oct 21. Internal hackathon done (no win).
- OneSignal health check (Oct 4): WORKING. Win-back 1 sent Oct 4 8:02 AM (28 sent, 25 delivered, 8% CTR; Android 13 / iOS 12);
  Win-back 2 8:04 AM (21 delivered, 9.5%); Trial-ending Oct 2 (11 delivered). In-app "Hi from Nagly" 20 impressions 75% CTR,
  "Welcome back" 7 at 85.7%. A/B: gentle 1/10 clicks vs guilt 0/10. Only failure = simulator test device (BadEnvironmentKeyInToken).
  Streak 3/7 Journeys: 0 sends (no user at a streak yet) — normal.
- Best award chance (my judge-style estimate): OneSignal (2nd–3rd), then Peace/HAMM. Catvertising out (ads removed).
- PENDING:
  1. Android 1.0.5 review — when approved: Play Console App content -> Advertising ID -> "No", submit; check store page shows
     no "Contains ads"; optionally publish internal-testing 1.0.5 draft.
  2. App Store ASO (needs user OK + ASC login): subtitle "Hydration & Medicine Reminder"; keywords
     "drink,tracker,medication,vitamin,supplement,creatine,protein,dose,tablet,parents,family,habit,bp"; promo text
     "One tap on the reminder logs your water or pill. No ads, ever. Mom's voice, water tracking and your most important pill reminder stay free forever.";
     description "LOG WITHOUT OPENING THE APP" -> "ONE TAP ON THE REMINDER". Ship with iOS 1.0.5 (10), currently ON HOLD.
  3. Shipaton Sale form: advised skipping.
- Rules still apply: Claude never handles passwords/secret keys, asks before submit/publish/push, no incentivized reviews,
  competitor scores stay private (app/store/research/shipaton_gallery/final/ is untracked on purpose).

## Oct 5 (new Claude account)
- Android 1.0.5 (10) APPROVED and LIVE: Production full rollout (178 countries) + Closed testing Alpha, published Oct 5 11:47.
  Internal-testing 1.0.5 draft left unpublished (not needed).
- Play App content -> Advertising ID set to "No" (1.0.5 merged manifest has no AD_ID permission). Play lists it under
  "What you've told us"; "Send for review" stays disabled because a declaration alone needs no review — it is applied with the next release.
- Next: App Store ASO text with iOS 1.0.5 (10), needs user OK + ASC login in Chrome.
- Oct 5: Play store page checked: no "Contains ads". Play DESCRIPTION is stale (says "watch a short ad", "12 voices, 4 families");
  corrected draft in app/store/listing/play_description_1.0.5.txt, waiting for user OK to save + send for review.
- Oct 5: App Store promotional text set on live 1.0.4 (no review). iOS 1.0.5 version created and SAVED, not submitted:
  build 10, subtitle "Hydration & Medicine Reminder", new keywords, description "ONE TAP ON THE REMINDER" + "NO ADS",
  What's New "Small improvements and fixes. Nagly stays ad-free…", auto-release. Needs user OK -> Add for Review -> Submit.
