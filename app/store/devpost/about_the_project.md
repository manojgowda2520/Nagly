## Inspiration
I ignore every reminder on my phone. I swipe them away without reading. But when my mom calls and asks "did you drink water? did you take your tablet?", I do it. About half of people with long-term conditions don't take their medicines as prescribed, and forgetting is one of the top reasons. The problem isn't the reminder. It's that nobody listens to an alarm.

So I built Nagly: reminders that sound like someone who loves you.

## Who it's for
- People who swipe away every reminder: busy professionals, students, anyone living away from home.
- People on daily medicines, vitamins or supplements (creatine, protein, BP tablets).
- Families: set it up for your parents, with a voice they already listen to.

## What it does
- **Water + meds + supplements.** Log water with one tap, add pills, vitamins, creatine or protein from quick picks, and tap "Took it".
- **15 voices in 5 families:** Mom, Dad, Grandparent, Bestie and Spouse. Mom is free forever.
- **Make it yours:** rename your persona to the real person, like "Subbu Amma", with your own emoji.
- **Mood engine:** the persona is proud, worried or disappointed depending on how your day is going. The words change and so does the animation.
- **Log from the lock screen:** +250 ml, +500 ml or "Took it" right on the notification.
- **History is a chat** with your nagger, and your bond level grows as you stay consistent.
- **Tilt the phone** and the water in the bottle stays level and sloshes.
- No account, works offline, health data never leaves the phone.

## How I built it
- **Flutter** (one codebase for iPhone and Android), SQLite for local data, flutter_local_notifications for on-device nudges with action buttons.
- **RevenueCat** for payments: Lifetime, Annual (with a 7-day store trial) and Monthly, plus a 7-day no-card welcome trial of everything. The paywall opens from 8 moments, each with a RevenueCat placement. A live targeting rule sends `trial_end` to an offering that opens on Annual and `medication_limit` to one that opens on Monthly, so I can change pricing without an app update.
- **OneSignal** for cloud pushes and Journeys: win-back, streak celebrations (3 and 7 days) and trial ending. One template speaks in 15 voices using Liquid, and the push image is the user's own persona. Water logged and doses taken are reported back as outcomes.
- **RevenueCat Ads** (Android only): rewarded ads only when you choose to watch one, to borrow a paid persona for 24 hours. The iPhone app has no ad SDK at all.

## How it makes money
- **Free forever:** Mom's voice, water tracking and one medicine or supplement reminder. Nobody loses the reminder for their most important pill because they can't pay.
- **7-day welcome trial** of everything, no card.
- **Nagly Pro** through RevenueCat: Lifetime $29.99, Annual $19.99 (7-day store trial) or Monthly $1.99. Pro unlocks 12 more voices, custom names and unlimited reminders.
- **The paywall is personal:** the persona you tried to unlock asks you to keep them. RevenueCat placements and targeting open the right plan for the moment (trial ending → Annual, a second pill reminder → Monthly).
- **On Android**, opt-in rewarded ads (RevenueCat Ads) let you borrow a paid voice for 24 hours.

## Traction so far
- Live on the App Store on Sep 25. 1.0.1 shipped the next day from user feedback; 1.0.2 (lock-screen buttons on iPhone) on Sep 29.
- 43 active customers and the first paying subscriber in week one.
- OneSignal win-back Journey: 26 users, ~14% push click rate, 75% click rate on the "Welcome back" message.

## Challenges
- Writing 15 personalities that feel caring, not annoying, and funny without being mean.
- Getting notification action buttons to log water with one tap on the reminder on both platforms.
- Keeping the iPhone build completely free of ad code while Android has rewarded ads.
- First time shipping to both stores: privacy rules for many countries, store reviews, in-app purchases and promo codes.

## What I learned
Tone matters more than features. When the reminder sounds like a person, people answer it. The first real users asked for creatine and protein reminders, so version 1.0.1 added supplements, the Spouse family and "Make it yours" the day after launch.

## What's next
Android launch (in Google review now), shared family mode so a parent can see "took it" from their kid, and more languages.
