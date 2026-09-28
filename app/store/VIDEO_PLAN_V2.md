# Nagly — Shipaton demo video v2 (the one that wins)

Target: **1:55–2:00**, 16:9 1080p, YouTube public. Judges watch the first 20 seconds to decide
whether to keep watching, and the video carries the first screening rounds. Every second must
either make them *feel* the problem or *see the real app solve it*.

## What judges must come away with (maps to the awards we enter)
| Takeaway | Award | Where in video |
|---|---|---|
| "I'd never ignore that reminder" (the idea in 5 s) | all | Hook 0:00–0:08 |
| Reminders really arrive, in her voice, with her face | OneSignal | 0:22–0:42 |
| It looks and feels crafted (mood, tilt, bubbles, chat) | Design | 0:42–1:05 |
| Free forever for the one pill that matters; Amma uses it | Peace Prize | 1:05–1:22 |
| Smart paywall + placements + targeting | HAMM | 1:22–1:36 |
| It's live, shipped fast, real users | all / Grand | 1:36–1:50 |

## Production pipeline
1. **AI story shots (Google Flow):** keyframe first, then animate.
   - Make a still for each shot with Nano Banana using the Arjun / Subbu Amma reference sheets → pick
     the best → Veo 3.1 **Quality** "frames to video" from that still. This fixes face drift.
   - Only 5 shots, 4–6 s used from each. No phone screens readable in AI shots.
2. **Real app footage (your iPhone):** iOS Screen Recording, shot list below.
3. **Motion graphics (HyperFrames, HTML → MP4):** notification stack, "how a reminder reaches you",
   15-voices wall, RevenueCat placement map, OneSignal Journey, proof numbers, titles, transitions.
   Built from the app's real text, colours, icon and persona images (docs/push/*.png).
4. **Sound (ElevenLabs):** narrator (Arjun, warm Indian-English male, 20s), Amma persona voice
   (warm older Indian woman), notification "ding" + whoosh SFX, soft acoustic music bed.
5. **Assembly:** one HyperFrames composition (or ffmpeg) with music ducking under voice.

## Script (≈ 1:58)

| # | Time | Visual | Audio (VO = Arjun narrator, AMMA = persona voice) |
|---|---|---|---|
| 1 | 0:00–0:04 | REAL: lock screen, grey "Reminder: Drink water" banner, thumb swipes it away. Two more generic alarms swiped. | SFX: dull beep ×3. VO: "I ignore every reminder on my phone." |
| 2 | 0:04–0:09 | AI S1: Arjun at laptop, doesn't even look at his buzzing phone; full bottle untouched. | VO: "Water. Vitamins. My mom's calls…" |
| 3 | 0:09–0:15 | AI S2 (warm memory): Amma in Mysuru kitchen hands him a steel tumbler, wags finger; he drinks it all. | VO: "…but back home, there was one voice I never ignored." AMMA (soft, from memory): "Kanna, drink water first." |
| 4 | 0:15–0:19 | MOTION: title — Nagly icon drops like a notification, "Reminders that sound like Mom". | SFX: ding. VO: "So now my phone sounds like her." |
| 5 | 0:19–0:27 | REAL: onboarding pick Mom → Personas → Make it yours → type "Subbu Amma", pick 👵🏽, Save. | VO: "Pick who nags you, and give them a real name." |
| 6 | 0:27–0:37 | MOTION: "How a reminder reaches you" — the day as a timeline 8 am → 10 pm; Nagly spaces nudges around your goal; each one pops as an iOS-style banner from Subbu Amma with a different line and mood. | VO: "Nagly plans nudges across your day, and each one is written in her voice." |
| 7 | 0:37–0:44 | REAL: real Subbu Amma notification lands on the lock screen (her photo), tap → app opens, tap +250 ml. | SFX: ding. AMMA: "Beta, sip some water. I'm not nagging, I'm caring." |
| 8 | 0:44–0:49 | AI S3: Arjun reads it, guilty grin, rolls eyes, drinks from his bottle. | (music lifts) |
| 9 | 0:49–1:02 | REAL: Home — bottle fills with bubbles, avatar goes worried → proud, confetti at goal; tilt phone, water sloshes. Quick cut: History chat. | VO: "She's proud when I'm on track, worried when I'm not. Tilt the phone — the water actually moves." AMMA: "That's my child!" |
| 10 | 1:02–1:09 | MOTION: 15-voices wall — Mom, Dad, Grandparent, Bestie, Spouse cards flip with their real lines. | VO: "Fifteen voices. Dad's puns, Nonna's guilt, your bestie's side-eye." |
| 11 | 1:09–1:16 | REAL: Meds & Supplements → quick pick Creatine 5 g → reminder time → Save. Med nudge "Took it 💊". | VO: "Pills, vitamins, creatine, protein. One reminder is free, forever." |
| 12 | 1:16–1:22 | AI S4: Amma at her dining table, phone lights up, she takes her BP tablet, smiles. | VO: "I set it up for Amma too. Appa's voice reminds her now." |
| 13 | 1:22–1:32 | REAL: tap a locked persona → paywall "Keep Dad around?" → plans. MOTION overlay: RevenueCat placements map (trial_end → Annual, medication_limit → Monthly). | VO: "Free to start. The paywall is personal, and RevenueCat picks the right plan for the moment." |
| 14 | 1:32–1:40 | SCREEN: OneSignal Journey canvas (win-back → streak → trial) + push with persona photo on the phone. MOTION: "15 voices · 1 template". | VO: "OneSignal brings you back, in the same voice, with her face on it." |
| 15 | 1:40–1:48 | MOTION proof: "Live on the App Store · 1.0.1 shipped in 2 days · first paying users · 20% win-back click rate". | VO: "Live on the App Store, with real people already drinking more water." |
| 16 | 1:48–1:55 | AI S5: evening, Arjun alone on sofa, video call, toasts his bottle at the phone, laughs. | VO: "People ignore alarms. They don't ignore their mom." |
| 17 | 1:55–1:58 | MOTION end card: icon, "Nagly", App Store badge, "Search Nagly". | SFX: final ding. |

## iPhone recording shot list (Settings → Control Centre → Screen Recording; mic OFF)
Before: Do Not Disturb off, battery > 50 %, set clock-looking tidy (no other notifications),
persona = Mom renamed "Subbu Amma" 👵🏽, a little history logged today.
- R1 — **Generic alarms** (Clock app): 3 quick "Drink water" alarms/reminders, swipe each away.
- R2 — **Make it yours:** Personas → Mom → Make it yours → clear name → type "Subbu Amma" → emoji → Save. Slowly.
- R3 — **Real Nagly notification:** lock the phone; a reminder arrives (I'll send you a real OneSignal
  test push with her photo, or set a med reminder 1 min ahead) → tap → app opens → +250 ml.
- R4 — **Home:** log 250 twice, then 500 to reach the goal (confetti); then tilt the phone left/right for 5 s.
- R5 — **History** tab scroll, then **Insights**.
- R6 — **Supplements:** Meds → Add → tap Creatine quick pick → 5 g → 9:00 → Save.
- R7 — **Paywall:** on a non-Pro state tap Dad → paywall → scroll plans (don't buy).
- Optional R8 — your face, 5 s: "I built Nagly because I ignore every alarm — but never my mom."

## Open decisions
- iPhone notification action buttons (+250 ml / Took it) exist only on Android today → see chat.
