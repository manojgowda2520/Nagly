# Google Play listing — Nagly

Assets in this folder: `icon-512.png` (Play icon), `icon-1024.png` (Devpost icon), `feature-graphic.png` (1024×500).
Screenshots: still to capture (phone, 1080×2400+; Devpost needs 1179×2556 without frames).

## Store listing

**App name (≤30):** `Nagly: Water & Pill Reminder`

**Short description (≤80):**
`Mom nags you to drink water & take your pills. Lovingly. Relentlessly.`

**Full description (≤4000):**

```
Your reminders finally have a personality.

Nagly is the water and medication reminder that sounds like someone who loves you. Pick your nagger — Indian Mom, Punjabi Dad, Italian Nonna, your Bestie, even Corporate HR — and they'll check on you all day, in their own voice.

"Afternoon check — bottle empty? You're behind today, beta."

💧 LOG WITHOUT OPENING THE APP
Every reminder has buttons: +250 ml, +500 ml, or a cheeky "Later, Amma". One tap from your lock screen and she's satisfied. For now.

🎭 12 VOICES, 4 FAMILIES
Mom (always free), Dad, Grandparent and Bestie — each with three personalities. Their mood changes with your behaviour: proud when you're on track, worried when you're behind, disappointed when you ignore them twice.

💊 MEDICATION REMINDERS — ONE IS FREE, FOREVER
"Did you take your BP tablet, beta?" Tap "Took it" right from the notification. Your most important pill is never behind a paywall.

🫧 A BOTTLE THAT'S ACTUALLY ALIVE
Water sloshes when you tilt your phone. Bubbles rise with every sip. Confetti when you hit your goal.

💬 YOUR HISTORY, AS A CHAT
Every sip becomes a little conversation with your nagger. It's the most wholesome chat log you own.

📈 INSIGHTS & A BOND THAT GROWS
Streaks, weekly charts, your best hour — and a relationship that grows from Stranger to Soul Reminder the more consistent you are.

👨‍👩‍👧 KEEP FAMILY IN THE LOOP
Share a simple weekly summary with the people who worry about you.

🔒 PRIVATE BY DESIGN
No account. Your water and medication log stays on your phone.

FREE FOREVER: Mom personas, water tracking, reminders, and one medication.
TRY EVERYTHING FREE FOR 7 DAYS — no card needed.
NAGLY PRO: every persona + unlimited medications. Lifetime $29.99, Annual $19.99 (7-day free trial) or Monthly $1.99.
Or watch a short ad to borrow any persona for 24 hours.

Nagly is a reminder tool, not a medical device. Always follow your doctor's advice.
```

**Category:** Health & Fitness · **Tags:** Hydration, Reminders, Habit tracker
**Contact email:** mgmanoj1481@gmail.com (public on the listing — change if you prefer a dedicated address)
**Privacy policy URL:** https://manojgowda2520.github.io/Nagly/privacy.html

## In-app products (create in Play Console → Monetize)

| Product ID | Type | Price (USD) | Notes |
|---|---|---|---|
| `nagly_lifetime` | One-time product | $29.99 | Primary offer ("BEST VALUE") |
| `nagly_pro` → base plan `annual` | Subscription | $19.99 / year | Add offer: 7-day free trial, new customers |
| `nagly_pro` → base plan `monthly` | Subscription | $1.99 / month | |

Then in RevenueCat: import all three → attach to entitlement `pro` → Offering `default` with packages **Lifetime**, **Annual**, **Monthly**. Optionally add Placements `persona_locked`, `medication_limit`, `trial_end`, `streak_upsell`, `settings` (the app already requests offerings per placement, so each can get its own offering/experiment).

## Data safety form (answers)

- **Data collected & shared:**
  - *Device or other IDs* — Advertising ID (AdMob, only when a rewarded ad is watched); anonymous app user ID (RevenueCat, OneSignal). Purpose: advertising, app functionality, analytics.
  - *Financial info → Purchase history* — RevenueCat / Google Play. Purpose: app functionality.
  - *App activity → Other actions* — non-health tags (streak length, days since last log, persona) sent to OneSignal. Purpose: app functionality / developer communications.
- **Health info (water intake, medication names):** **Not collected** — stored only on device, never transmitted.
- Data encrypted in transit: **Yes** (all SDKs use HTTPS).
- Users can request deletion: **Yes** (email).
- No account creation.

## Other declarations

- **Ads:** Yes (rewarded ads only).
- **Content rating:** Everyone / PEGI 3 — no violence, no user-generated content.
- **Target audience:** 18+ (avoids Families policy requirements).
- **Health apps declaration:** medication reminder — not a medical device; no clinical claims.
- **Permissions:** POST_NOTIFICATIONS (reminders), RECEIVE_BOOT_COMPLETED (restore reminders after reboot). No exact-alarm permission is requested.
