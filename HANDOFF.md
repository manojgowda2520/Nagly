# Nagly — Project Handoff / Claude Code Context

> Paste to Claude Code on a new machine (or say "read HANDOFF.md") to resume with full context.

## What Nagly is
A hydration app where **someone who loves you (mom / dad / dadi / bestie) nags you to drink
water** instead of a boring notification. The emotional relationship — not the tracking — is
the product. Tagline: "Someone who cares."

Core loop: set daily goal → pick your "nagger" persona → log water (bottle fills) → persona
reacts to behavior (worried / disappointed / proud) → reminders in her voice, timed to how far
behind you are. Free = Mom personas. Pro / watch-ad = Dad, Grandparent, Bestie.

## Competition
Shipaton 2026 (RevenueCat), ends Sep 30 2026. Builder is targeting an early personal deadline.
Targeting 4 awards:
- OneSignal "Keep Them Coming Back" — $25k — re-engagement messaging (messaging IS the product)
- #BuildInPublic — $30k — posting daily on X as @ManojBuilds06, tags #Shipaton #nagly
- Ship Kotlin Everywhere (JetBrains) — $15k — the KMP architecture (needs BOTH stores live)
- Catvertising — $15k — rewarded-ad persona unlocks
Judging: video-first. Screeners watch the first 2 min of the demo video + description. Do NOT
jam into every category. Lead the pitch with the joke, not "water tracker".

## Tech / architecture
- Kotlin Multiplatform + Compose Multiplatform shared UI. Android + iOS. ~88% commonMain.
- Package `com.manojbuilds.nagly`. Repo: https://github.com/manojgowda2520/Nagly
- No backend, no accounts, no login — local-only (SQLDelight), works offline. This is deliberate.
- Stack: Compose MP, SQLDelight, kotlinx-datetime, Koin, Coroutines/Flow.
- Nav: sealed `Screen` + `NavBackStack` (no nav library). System back via Compose ui-backhandler.
- Third-party services behind interfaces with FAKE impls, bound by Koin, toggled by
  `Integrations.SANDBOX_MODE`. Swap to real impls one Koin binding at a time when keys arrive:
  BillingRepository→FakeBilling (RevenueCat), PushClient→FakePush (OneSignal),
  AdClient→FakeAd (AdMob + RevenueCat Ads), Analytics (anonymous, planned).
- Persona model is TWO-LEVEL: Relationship (mom/dad/grandparent/bestie, tier FREE/PRO — gating
  lives here) → Persona variants. Lines keyed by Mood × DayPart. `skipLabels` = short button jabs.
- Notifications: LOCAL notifications (expect/actual: Android AlarmManager / iOS
  UNUserNotificationCenter) do the hourly nags — 3 action buttons (+250, +500, persona skip-jab),
  log without opening app. OneSignal is ONLY for cloud re-engagement (the award), not the nags.
- iOS fix already applied: `-lsqlite3` linker flag (SQLDelight device link).

## PAYMENTS — important, do not overbuild
No payment gateway, no email, no OTP, no confirmation email. RevenueCat + native store IAP:
user taps Go Pro → Apple/Google payment sheet → store charges + emails their own receipt +
handles renewals/refunds/tax → RevenueCat tells the app "isPro" → unlock. Apple/Google billing
is MANDATORY for digital goods; Stripe/Razorpay are forbidden. Only job = design the paywall screen.

## Current state (done)
- Full app runs in sandbox: onboarding quiz, Home (living hero), History (chat timeline),
  Personas (2-step picker), Insights, Settings/Profile, bottom nav, paywall, ad-unlock flow.
- v1.0 build + v1.1 "living UI" + v1.2 polish pass all committed & pushed. 70 tests pass.
- Design system: teal #0E7C86 / primary #4FC3F7 / accent #FF8A65, mood colors, 8pt spacing,
  rounded, light+dark. Chat timeline is the signature screen (keep it).
- Git identity set to Manoj Kumar K <mgmanoj1481@gmail.com>. History was rewritten to remove
  a co-author (Cursor commits as prakash@mobil80.com — RESET git identity after Cursor commits).

## Open issues / next steps
1. CRASH: builder reported a crash after v1.2; could NOT reproduce (app launched clean, no
   AndroidRuntime trace). Needs: reproduce with exact action (esp. back button), capture logcat.
2. Persona LINES are still placeholders — this is THE product, only the builder can write them.
   ~40 lines per Mood×DayPart per persona, plus skip-jabs. Start with Indian Mom.
3. App icon — 6 logo concepts shown; builder to pick (#1 drop=message or #5 drop-with-face
   recommended). Then export 1024×1024 + splash mark.
4. Privacy Policy + Terms — REQUIRED before store submission (Claude to draft, builder hosts).
5. Store paperwork = the real bottleneck: Play Console + App Store Connect app entries, IAP
   products (pro_monthly, pro_annual), merchant/bank/tax, RevenueCat project+keys, AdMob (slowest),
   APNs .p8 + FCM for OneSignal. NONE started yet. Start ASAP — approvals gate everything.
6. One-week plan: fix crash+UI → write lines → real RevenueCat → icon+screenshots+listing →
   privacy/terms → publish ANDROID first (Play review = hours; Apple = 1-3 days, risky) → 2-min
   demo video (first 15s = hook) → submit on Devpost.

## Accounts status
Done: GitHub repo, Devpost registered, OneSignal app created (free plan), some sponsor perks
(Mobbin, Paddle). Perks to still claim: OneSignal Growth (3mo free), Codemagic, AppScreens, Sentry.
Not started: Play Console/App Store Connect app entries, AdMob, RevenueCat project, store IAP.

## Key commands
Build+test both: `./gradlew :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64 :shared:allTests`
Run on Android device: `./gradlew :androidApp:installDebug && adb shell am start -n com.manojbuilds.nagly/.MainActivity`
Push: `git add -A && git commit -m "msg" && git push origin main`
Fix git identity (after Cursor): `git config user.name "Manoj Kumar K" && git config user.email "mgmanoj1481@gmail.com"`

## New-machine setup
Install: JDK 17, Android Studio + SDK (set ANDROID_HOME), Xcode (Mac, for iOS), Cursor, gh, adb.
`git clone https://github.com/manojgowda2520/Nagly.git`, set git identity, `gh auth login` as
manojgowda2520, re-add Cursor RevenueCat MCP (.cursor/mcp.json) and re-auth.

## Working style notes
- Builder builds via Cursor (loop-engineering prompts: run steps in order, verify+commit each).
  Claude writes the prompts + verifies builds independently + advises strategy. Communicate concise.
- Builder speaks via voice-to-text (transcripts can be garbled — confirm intent when unclear).
