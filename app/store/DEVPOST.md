# Devpost fact sheet — Nagly (Shipaton 2026)

Facts, links and numbers to fill the Devpost form. Write the answers in your own words
(the office advice: judges don't want AI-sounding text). Numbers are as of 2026-09-25;
refresh them on the day you submit.

Deadline: **Sep 30, 2026, 11:45 pm PDT** (= Oct 1, 12:15 pm IST). Aim to submit by **Sep 28**.

---

## 1. Required for every entry

| Field | Value | Status |
|---|---|---|
| App store URL | https://apps.apple.com/us/app/nagly-moms-water-pill-nags/id6814609746 | ✅ live |
| Google Play URL | https://play.google.com/store/apps/details?id=com.manojbuilds.nagly | ⏳ in review |
| Demo video (≤ 2 min, public YouTube/Vimeo) | — | ❌ to record |
| App icon 1024×1024 | `app/store/devpost/icon-1024.png` | ✅ |
| Screenshot 1179×2556, no frame | `app/store/devpost/1_home.png` (+ 2–4) | ✅ |
| Judge access | 7-day free trial + free Lifetime promo codes (§4) | ✅ |
| Text description | §3 | you write |

## 2. IDs and links the form may ask for

- OneSignal App ID: `2858113c-c323-453d-bbc9-976b8d4c999e`
- Support: https://manojgowda2520.github.io/Nagly/ · Privacy: …/privacy.html · Terms: …/terms.html
- Built with: Flutter, Dart, RevenueCat (purchases_flutter, RevenueCat Ads tracking, placements), OneSignal, Google AdMob (Android), SQLite, flutter_local_notifications

## 3. Description — structure from RevenueCat's pitching guide

Logline pattern: *For [who], Nagly helps [job] by [approach], so they can [outcome].*

- **Problem:** generic reminders get swiped away. About half of people with chronic conditions don't take medicines as prescribed; forgetting is a top reason. People ignore alarms, not their mom.
- **What it is:** water + medication reminders written in the voice of someone who loves you. 15 personas in 5 families: Mom (free), Dad, Grandparent, Bestie, Spouse. "Make it yours": rename your persona to the real person ("Lakshmi Amma", "Priya").
- **How it's different:** mood engine (proud / worried / disappointed) changes the words *and* the animation; log from the lock screen (+250 ml / +500 ml / "Took it"); history is a chat with your nagger; bond level grows with consistency.
- **Evidence:** live on the App Store; first real users and a first paid subscriber within a day (numbers in §5).

## 4. Judge access

- 7-day free trial of everything, no card (starts after onboarding).
- **Promo codes (created 2026-09-25):** App Store offer "Shipaton judges - free Lifetime" — free Nagly Pro Lifetime, one-time codes, expire Nov 30, 2026. The CSV is in your Downloads folder — **never commit it or post codes publicly**; paste ~5 into the Devpost judges field only.
- Judge instructions to paste with the codes:
  1. Install Nagly from the App Store link above.
  2. Redeem a code: https://apps.apple.com/redeem?ctx=offercodes&id=6814609746&code=CODE (or App Store → your photo → Redeem Gift Card or Code).
  3. Open Nagly → Settings → **Restore purchases** → every persona and unlimited medications unlock.
- Android (once live): Play Console → Monetize → Promo codes for `nagly_lifetime`.

## 5. Numbers (2026-09-25)

- RevenueCat: 32 customers seen, **1 paid subscriber, $2 revenue** (App Store, Monthly).
- OneSignal: 20 devices, 8 iPhones opted in to push.
- Win-back Journey: 9 users entered; Win-back 1 push delivered 5, 1 click (**20% CTR**).
- App Store approved 2026-09-25 (first submission, no rejection).

## 6. Per-award answers — requirement + facts to write from

### Keep Them Coming Back (OneSignal) — 1st $25k
Requirement: describe how OneSignal was used, including the campaign deployed; give the App ID.
- 3 live Journeys: Win-back (2-step push → in-app "Welcome back" with a log button), Streak celebrations (3 and 7 days), Trial ending (push → in-app "Keep me around?" → paywall).
- Every push is written per persona with Liquid (`{% case tag.persona_id %}`) — 15 voices, one template.
- **Persona picture in the push**, chosen per user by Liquid in the image URL.
- Action buttons on cloud pushes: +250 ml / +500 ml log water without opening the app.
- Outcomes: water logged / goal met / dose taken reported back, so the dashboard shows which push made someone drink, not just who clicked.
- 6 tags only, never health data; timestamps (not strings) so "time since last log" segments stay accurate.
- Deep links via `route` (home / insights / paywall).

### Catvertising (RevenueCat Ads)
Requirement: how the app uses RevenueCat Ads — placements and fit with the revenue stack.
- Rewarded ads only, only when the user taps "Watch ad". Never interstitials, never banners, never on iPhone.
- The ad *is* the upsell: watch one ad → borrow Dad / Nonna / Bestie / Spouse for 24 h, or unlock more medication reminders for 24 h.
- Every ad event reported to RevenueCat Ads (loaded, displayed, opened, revenue, failed) with placement names, so ad revenue sits next to subscription revenue.
- Remote switch (RevenueCat offering metadata `monetization_mode`: payments / ads / both) — no app update needed.
- ⚠️ Android only, and Android is still in Google review. Judges can only see it once Play approves.

### HAMM (Help Apps Make Money)
Requirement: strategy, paywall/pricing approach, conversion or revenue numbers.
- Free forever: Mom + water + 1 medication. 7-day no-card welcome trial of everything.
- Pro: Lifetime $29.99 (anchor), Annual $19.99 with store 7-day trial, Monthly $1.99.
- Personal paywall: the locked persona pleads for itself ("Keep Dad around").
- Paywall opened from 6 moments (locked persona, medication limit, trial end, streak upsell, custom persona, settings) — each passes a RevenueCat **placement** name.
- Live RevenueCat Targeting rule "Paywall moments (placements)" (2026-09-25): `trial_end` → offering `annual_first` (paywall opens on Annual, which has the store free trial — the natural next step when the app trial ends); `medication_limit` → `monthly_first` (opens on $1.99 Monthly — small step for someone who just needs a second pill reminder); everything else → `default` (Lifetime first). The offering's `highlight` metadata picks the plan the paywall opens on (app 1.0.1+). Change or A/B test from the dashboard with no app update.
- Rewarded ads as a second stream on Android.
- Customer attributes (persona, care mode, streak, bond) sent to RevenueCat for segmenting.
- Numbers: §5.

### Peace Prize
Requirement: how the app benefits individuals, the community or society.
- One medication reminder is free forever — nobody loses the reminder for their most important pill because they can't pay.
- No account, works offline, health data never leaves the phone.
- Familiar voice for older parents and multi-generational families; one-tap "Took it" from the lock screen; weekly summary to share with family.
- Strongest proof: a short real story from a parent/grandparent using it.

### Design Award
Requirement: unique design elements and where judges should look.
- Tilt the phone on Home: the water stays level and sloshes (accelerometer).
- Bubbles on every sip, confetti on goal.
- Avatar animation = mood (proud bounce, worried wobble, disappointed sigh).
- Speech bubble "types" before a new line; backgrounds change with the time of day.
- History is a chat with your nagger, with a mood stripe on each message.
- The paywall is personal: the persona you tried to unlock asks you to keep them.

### Influencer award
Not entering (decided 2026-09-25): Abbey's Kitchen is about meals/nutrition without calorie counting; Nagly is water + medication.

## 7. Awards to select on the form
Keep Them Coming Back (OneSignal) · Catvertising · HAMM · RevenueCat Peace Prize · RevenueCat Design Award.
