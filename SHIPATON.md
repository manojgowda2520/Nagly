# Nagly × Shipaton 2026 — winning playbook

Deadline: **Wed Sep 30, 11:45 pm PDT**. App must be **live on Google Play** by then, with a working **RevenueCat** purchase, a **free trial** (we have a 7-day app-managed trial + a 7-day store trial on Annual) and a **<2-min demo video** on YouTube/Vimeo.

## Awards we're targeting, and why Nagly can win them

| Award | Criteria (from the rules) | Our angle | What's built |
|---|---|---|---|
| **RevenueCat Peace Prize** | Impact · Feasibility | Medication non-adherence is one of healthcare's biggest avoidable problems; reminders that *sound like family* get acted on. **One medication reminder is free forever** — we never paywall someone's most important pill. Works offline, no account, built for parents & grandparents. | Medication mode, lock-screen "Took it 💊 / Snooze / Not today", adherence stats, family weekly share, free-forever pill |
| **RevenueCat Design Award** | Innovative ideas · Aesthetics | Personas as emotional UI: moods drive animation (proud bounces, worried wobbles, disappointed sighs). Water that tilts with the real phone (accelerometer), bubbles on every sip, confetti on goal, day-part living backgrounds, typing-dot speech bubbles, history as a chat. | All of it — see `lib/ui/widgets/` |
| **HAMM (Help Apps Make Money)** | Revenue streams, innovation, sustainability | Diversified: Lifetime (anchor) + Annual w/ trial + Monthly + rewarded ads + 7-day no-card welcome trial. **Persona-personalized paywall** (the locked persona pleads for itself). **RevenueCat Placements** per trigger (`persona_locked`, `medication_limit`, `trial_end`, `streak_upsell`, `settings`) so each moment can get its own offering / A/B experiment. Customer attributes (persona, care mode, streak, bond) segment revenue charts. Ethical line (free pill) builds trust → conversion. | `services/billing.dart`, `ui/screens/paywall_screen.dart` |
| **Keep Them Coming Back (OneSignal)** | Implementation · User value · Creativity | Cloud messages **in the persona's voice**, segmented by tags the app syncs: win-back after silence, streak milestones, trial ending, upsell for engaged free users; IAM triggers; deep links (`route` → paywall/home/…). | `services/push.dart`, tags in `domain/push_tags.dart` |
| **Catvertising** (bonus) | Ads integrated naturally | Rewarded ads *only*, user-initiated, as a "borrow a persona for 24h" sampler that doubles as the most honest upsell. Every ad event reported to **RevenueCat Ads** (`Purchases.adTracker`) + RevenueCat server-side reward verification. | `services/ads.dart` |

**Decided 2026-09-24:** focus on OneSignal, Catvertising, Peace, Design, HAMM + influencer entry **Abbey's Kitchen**. Skip Funnel Vision (Stripe, judged on web sales), Galaxy (review time), Layers, Replit. Devpost needs a 1179x2556 screenshot (in `app/store/devpost/`), 1024 icon, free trial for judges; write Devpost text in own words (office advice). Office (Mobil80) internal pitch ~Oct 1, 100-pt rubric: Problem 15, Functionality 25, UI/UX 20, Innovation 15, Polish 15, Track fit 10; top 3 get Rs 30k/20k/10k if live in stores.

Skip: Grand Prize (needs revenue traction), Kotlin Everywhere (we're Flutter now), Funnel Vision (needs Stripe web funnel), Galaxy (optional second store if time allows — same AAB works).

## Timeline (11 days)

| Day | Owner | Task |
|---|---|---|
| Sep 19–20 | Builder | Play Console app entry; upload `app-release.aab` to **Internal testing**; create products; RevenueCat project + Play service-account credentials; OneSignal app + Firebase; AdMob app + rewarded unit |
| Sep 20–21 | Claude | Plug in keys (`lib/config/integrations.dart`, AdMob id in `AndroidManifest.xml`), flip `sandboxMode=false`, real test purchase on device, capture screenshots |
| Sep 21 | Builder | Store listing + data safety (copy in `app/store/PLAY_LISTING.md`), submit **Production** review (review can take days — do it early) |
| Sep 22–24 | Builder | OneSignal campaigns live (below), record demo video, enable GitHub Pages for `/docs` |
| Sep 25–29 | Both | Submit to Devpost early; buffer for review rejections |

## Devpost submission text (drafts)

**Tagline:** Someone who cares. Your family nags you to drink water and take your pills — lovingly, relentlessly.

**Description:**
> Reminders are easy to ignore. Your mom isn't. Nagly turns hydration and medication reminders into messages from a persona who loves you — Indian Mom, Punjabi Dad, Italian Nonna, your Bestie, even Corporate HR. They react to what you actually do: proud when you're on track, worried when you fall behind, disappointed when you ignore them twice. Every reminder has lock-screen buttons (+250 ml, "Took it 💊"), so you log without opening the app. Built in Flutter, local-first and account-free, with RevenueCat powering a lifetime/annual/monthly paywall, rewarded ads, and OneSignal re-engagement in the persona's own voice.

**Peace Prize — how it benefits people:**
> Around half of people with chronic conditions don't take medication as prescribed, and forgetting is one of the most common reasons. Generic alarms get swiped away; a message that sounds like your mother gets answered. Nagly's medication mode reminds you in a familiar, caring voice and lets you confirm the dose in one tap from the lock screen. We made a deliberate choice: **one medication reminder is free forever**, so nobody loses the reminder for their most important pill because they can't pay. It works offline, needs no account, keeps health data on the phone, and includes a one-tap weekly summary people can share with a family member who worries about them — built with ageing parents and multi-generational families in mind.

**Design Award — where to look:**
> 1) Tilt your phone on Home — the water in the bottle stays level and sloshes (accelerometer-driven wave physics), and bubbles rise on every sip. 2) Watch the persona avatar: its animation is its mood — a proud bounce, a worried wobble, a disappointed sigh. 3) Speech bubbles "type" before a new line appears. 4) Backgrounds shift with the time of day. 5) History is a chat with your nagger, with a mood stripe on every message. 6) The paywall is personal: the persona you tried to unlock is the one asking you to keep them.

**HAMM — monetization:**
> Free forever: Mom + water + one medication. Every new user gets a 7-day no-card welcome trial of everything, so they bond with Dad or Nonna before the paywall appears. Pro is sold as Lifetime ($29.99, the anchor), Annual ($19.99 with a store free trial) and Monthly ($1.99), fetched from RevenueCat per **placement** — the locked-persona, medication-limit, trial-end, streak-upsell and settings moments can each run their own offering or experiment. Rewarded ads (tracked through RevenueCat Ads) let free users borrow a persona for 24h, a sampler that converts. Customer attributes (persona, care mode, streak, bond level) let us see which voices and moments drive revenue. [Add real numbers after launch.]

**OneSignal — Keep Them Coming Back:**
> Local notifications handle the hourly nags; OneSignal handles everything cloud, in the same persona voice. The app syncs exactly 6 tags (persona_id, care_mode, current_streak, last_log_at, trial_started_at, is_pro — never health data); timestamps feed "time elapsed" segments, so they never go stale. Cloud pushes carry "+250 ml / +500 ml" action buttons that log water without opening the app, and every sip, goal and dose taken is reported as a OneSignal Outcome, so the dashboard shows which message actually made someone drink — not just who opened it. Three Journeys, every message written per persona with Liquid: a two-step win-back that ends in an in-app "Log a glass" button, streak celebrations at 3 and 7 days, and a trial-ending ask that opens the paywall. Messages deep-link into the right screen via a `route` field.

## OneSignal dashboard (built, Nagly App 2858113c…)

Segments (tags → time-elapsed filters):
- **Gone quiet (no water logged 2+ days)** — `last_log_at` elapsed > 2 days
- **Streak 3+ days** — `current_streak` > 2
- **Trial ends tomorrow (not Pro)** — `trial_started_at` elapsed > 6 and < 7 days AND `is_pro` = false

Push templates (Liquid `{% case tag.persona_id %}` title + body, Mom fallback, 1-day TTL):
- Win-back 1 · gentle / Win-back 2 · guilt trip — collapse `nagly_winback`, `route=home`, buttons `add_250` / `add_500`
- Streak 3 days / Streak 7 days · celebration — collapse `nagly_streak`, `route=insights`
- Trial ends tomorrow — collapse `nagly_trial`, `route=paywall`

Journeys (all **Draft** until the builder approves Set live):
1. **Win-back: Mom misses you** — Gone quiet → 8am–6pm window → Win-back 1 → wait 24h → Win-back 2 → IAM "Welcome back 💛" (button `add_250`). Exits when they log again; re-entry after 7 days.
2. **Streak celebrations: proud family** — Streak 3+ → window → Streak 3 push → wait 4 days → window → Streak 7 push. Exits if the streak breaks; re-entry after 14 days.
3. **Trial ending: keep me around?** — Trial ends tomorrow → window → trial push → IAM "Keep me around? 🥺" (button `paywall`; opens the ad hub in Plan B). Exits on going Pro; once per user.

**2026-09-24 update:** all 5 push templates now target iOS too (they were Android-only, created before APNs was set up) and carry a **persona picture** via Liquid in the image URL: `https://manojgowda2520.github.io/Nagly/push/{{ tag.persona_id | default: "indian_mom" }}.png` (12 images in `docs/push/`, Fluent UI Emoji art, MIT). Early real data: Win-back Journey entered by 9 users; Win-back 1 delivered 5, 1 click (20% CTR). iOS shows the image only with a Notification Service Extension (not added; Android shows Big Picture).

Push goals are Clicks (>10% CTR) for now. After launch, switch them to the `water_logged` Outcome if the goal picker offers it.

## Demo video (≤2:00) — first 15 seconds sell it

1. **0:00–0:12** Lock screen buzzes: "👩 Indian Mom — Afternoon check — bottle empty?" Thumb taps **+250 ml**. Text on screen: *"Your reminders finally have a mom."*
2. **0:12–0:35** Onboarding speed-run → pick Corny Dad → tap bottle for first glass → "You're on a roll — hydrated!"
3. **0:35–0:55** Home: tilt the phone (water sloshes), log, confetti; tap bubble for a new line.
4. **0:55–1:15** Medication mode: "Did you take your BP tablet?" → Took it 💊. "One medication is free forever."
5. **1:15–1:35** History chat + Insights + share my week.
6. **1:35–1:55** Locked Nonna → watch ad → 24h unlock → personalized paywall (Lifetime / Annual / Monthly).
7. **1:55–2:00** Logo + "Someone who cares."

Use the in-app sandbox tools (Settings → Sandbox) to record: *Send a test nudge in 5s*, *Seed a week of history*, *Trial ends in 1 hour*.
