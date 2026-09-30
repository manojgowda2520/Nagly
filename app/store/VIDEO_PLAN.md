# Nagly demo video: AI story + real app (≤ 2:00)

Same style as the teammate's Saathi video: cinematic AI story scenes (Google Flow / Veo)
between **real app screens** on branded cards, with an ElevenLabs voice-over and subtitles.
Story scenes are AI; everything showing the app is the real app (screen recordings or screenshots).

**Look:** Saathi is dark purple at night. Nagly is **warm daylight**: cream/peach cards
(#FFF3E6 → #FFD9C2), teal accent (#12808C), dark-brown serif headlines, small caps chapter
labels like `01 · SHE DOESN'T GIVE UP`.

---

## How the app really works (keep the story true to it)

Nagly is a single-user app. Nobody else sends the reminders: **the app** writes every nudge
in the voice of the persona you pick (Mom, Dad, Grandparent, Bestie, Spouse), and you can
rename it to a real person ("Subbu Amma"). The real Amma never sees or sends anything.
So in the story, Amma appears only as a **memory** of home and as **her own user** of Nagly.

## 1. Characters (make these first in Flow, reuse as "ingredients" in every scene)

**Arjun (the user):** 26, South Indian software engineer living alone in a Bengaluru flat.
Slim, short black hair, light stubble, rectangular glasses, grey hoodie over a t-shirt.
Busy, always on the laptop, a big steel water bottle he never finishes.

**Subbu Amma (his mother / the persona):** 55, lives in Mysuru. Warm round face, silver-streaked
hair in a low bun, small bindi, reading glasses on a chain, cotton saree (mustard with maroon
border). Loving but relentless.

Character-sheet prompt (run once per character, pick the best image, save as ingredient):
> Photorealistic character reference sheet, front view and three-quarter view, neutral
> background, soft daylight. [paste character description]. Natural skin texture, cinematic,
> 35mm film look.

Flow settings: Veo, 16:9, 8-second clips, generate 2–4 per scene and keep the best.
**Mute Flow's own audio** (or keep only ambient); the voice-over comes from ElevenLabs.

---

## 2. Timeline

| # | Time | Type | Visual | Voice-over / on-screen text |
|---|---|---|---|---|
| 1 | 0:00–0:08 | AI | Scene A: Arjun at laptop late morning, phone buzzes "Reminder: Drink water", he swipes it away without looking | VO: "Every day, we swipe away a hundred reminders." Label: `A NORMAL DAY` |
| 2 | 0:08–0:16 | AI | Scene B (memory, warm faded look): years ago at home in Mysuru, Subbu Amma hands teenage-looking Arjun a steel tumbler of water and wags her finger; he drinks it | VO: "Back home, there was one voice I never ignored. Now I live alone, and nobody nags me." |
| 3 | 0:16–0:21 | Card | Nagly logo + "Reminders that sound like Mom" | VO: "This is Nagly." |
| 4 | 0:21–0:30 | Real | Card + real lock screen: a Nagly nudge from the Mom persona he renamed "Subbu Amma", tap **+250 ml** | Headline: *Nags you can't ignore.* VO: "Nagly writes your reminders in the voice of someone who loves you. I picked Mom and named her Subbu Amma. Log right from the lock screen." |
| 5 | 0:30–0:38 | AI | Scene C: Arjun reads the nudge, smiles, rolls his eyes, drinks from the bottle | Nagly's Mom persona line (ElevenLabs, 2nd voice, as if he "hears" it): "Beta, sip some water. I'm not nagging, I'm caring." |
| 6 | 0:38–0:50 | Real | Home screen: bottle fills, tilt so water sloshes, avatar mood proud → worried | `01 · SHE HAS MOODS` VO: "She's proud when you're on track, and worried when you're not." |
| 7 | 0:50–1:00 | Real | Personas: 5 families (Mom, Dad, Grandparent, Bestie, Spouse) → Make it yours → type "Subbu Amma" | `02 · MAKE IT YOURS` VO: "Fifteen voices. Or give it the name of your real mom." |
| 8 | 1:00–1:08 | AI | Scene D: Arjun at the gym, phone buzzes, he laughs and scoops creatine into his shaker | `03 · NOT JUST WATER` |
| 9 | 1:08–1:18 | Real | Meds & Supplements: quick picks (Creatine, Protein, BP tablet, Vitamin D), tap **Took it** | VO: "Pills, vitamins, creatine, protein. One reminder is free, forever." |
| 10 | 1:18–1:26 | AI | Scene E: Subbu Amma now has Nagly on her own phone; it reminds her, she takes her BP tablet at the dining table, smiling | VO: "I set it up for Amma too. Her reminders come from the Spouse persona, in Appa's voice." |
| 11 | 1:26–1:36 | Real | History as a chat + Insights + streak | `04 · IT REMEMBERS` VO: "Your history is a chat with your nagger. Your bond grows as you stay consistent." |
| 12 | 1:36–1:44 | Real | Paywall plea "Keep Dad around?" (Lifetime / Annual / Monthly) | VO: "Free to start. Pro unlocks every voice. Powered by RevenueCat." |
| 13 | 1:44–1:54 | AI | Scene F: evening, Arjun video-calls Amma, both laughing, each raises a glass of water to the camera | VO: "People ignore alarms. They don't ignore their mom." |
| 14 | 1:54–2:00 | Card | Logo, "Live on the App Store", App Store badge | VO: "Nagly. Download it today." |

Optional: replace scene 13 with 3 seconds of **you (Manoj) on camera** saying "I built Nagly
because I ignore every alarm, but never my mom." Judges remember a face.

---

## 3. Flow scene prompts (8 s each, 16:9, add both character ingredients where they appear)

**Scene A — ignoring the alarm**
> Cinematic, photorealistic. Late morning in a small modern Bengaluru apartment, sunlight
> through a window, plants on the sill. [Arjun] sits at a desk typing on a laptop, headphones
> around his neck, a full steel water bottle untouched next to him. His phone on the desk
> lights up with a notification. Without looking away from the laptop he swipes it away with
> one finger. Slow push-in, shallow depth of field, warm natural colours. No readable text on
> screens.

**Scene B — memory of home**
> Cinematic, photorealistic, nostalgic warm faded colours, soft film grain. A bright
> traditional South Indian kitchen in Mysuru, brass vessels. [Subbu Amma], a few years
> younger, hands a steel tumbler of water to [Arjun] (younger, college age, backpack on,
> rushing out), wags her finger lovingly; he laughs and drinks it in one go. Gentle handheld
> camera, morning light. No readable text.

**Scene C — the nag works**
> Cinematic, photorealistic. Same apartment. [Arjun] picks up his buzzing phone, reads it,
> breaks into a guilty smile and rolls his eyes, then unscrews the steel bottle and drinks a
> long sip, looking at the camera sheepishly. Close-up, warm light, shallow depth of field. No
> readable text on the phone.

**Scene D — gym + creatine**
> Cinematic, photorealistic. A bright neighbourhood gym in the evening. [Arjun] in a black
> t-shirt, between sets, his phone buzzes on the bench. He glances at it, laughs, and scoops
> white powder from a tub into a shaker bottle, then shakes it. Medium shot, energetic but
> warm lighting. No brand logos, no readable text.

**Scene E — Amma uses Nagly herself**
> Cinematic, photorealistic. A calm South Indian home dining table in the morning, a steel
> plate, a small pill box. [Subbu Amma]'s phone on the table lights up. She smiles, picks a
> small white tablet from the pill box, takes it with water from a steel tumbler, and nods to
> herself contentedly. Soft light, slow dolly, peaceful mood. No readable text.

**Scene F — the video call**
> Cinematic, photorealistic. Evening in the Bengaluru apartment, warm lamp light. [Arjun] on a
> video call on his phone held at arm's length, laughing, raising his steel water bottle
> towards the phone like a toast. Over-the-shoulder shot so the phone screen shows a blurred
> older woman smiling. Cosy, emotional, warm tones. No readable text.

Tips: phones in AI scenes must never show a readable UI (it will look fake and wrong). All
real UI comes from the app recordings in rows 4, 6, 7, 9, 11, 12.

---

## 4. ElevenLabs

- **Narrator:** warm, friendly Indian English voice (male or female), calm pace. Paste the VO
  lines in order, one generation per row so timing is easy to edit.
- **Subbu Amma:** an older Indian woman voice, affectionate, slightly exasperated. Lines:
  - "Beta, sip some water. I'm not nagging, I'm caring."
  - (optional, over row 9) "Took your creatine? Good boy."
- Only use a **cloned real voice** (e.g. your own mom's) if she has agreed.
- Export as MP3/WAV, line up in the editor, add soft background music at ~15% volume.

## 5. Real app segments (Claude can prepare)

Branded cards like the Saathi video: cream/peach background, serif headline on the left, the
real phone screen recording on the right, chapter label on top, subtitle at the bottom.
Needed recordings (iPhone screen recording or simulator):
1. Lock-screen push from Subbu Amma → +250 ml
2. Home: log a glass, tilt, mood change
3. Personas → Make it yours → "Subbu Amma"
4. Meds & Supplements → Creatine quick pick → Took it
5. History chat → Insights
6. Paywall plea

## 6. Edit + export

- Editor: CapCut / iMovie / DaVinci. 16:9, 1080p, burned-in subtitles.
- Check: ≤ 2:00, no fake UI in AI scenes, App Store link on the last card.
- Upload to YouTube as **Public**, then paste the link into Devpost step 2.
