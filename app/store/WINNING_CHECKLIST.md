# Winning checklist — Office hackathon + Shipaton (Devpost)

Made 2026-09-27. Tick items as you go (`[x]`).

**Deadlines**
- Devpost: **Sep 30, 11:45 pm PDT = Oct 1, 12:15 pm IST**. Target: submit **Sep 28**, edit until the deadline.
- Office pitch: **~Oct 1** (projector). Top 3: Rs 30k / 20k / 10k if the app is live in stores.

Legend: 👤 = you · 🤖 = Claude · ⭐ = biggest impact

---

## A. Shared (do once, use for both)

### Stores
- [x] 👤⭐ Check Play Console notifications (Nagly only) — Sep 27: still in review, nothing needed
- [x] 👤 Ask the office organisers — Sep 27: App Store alone counts
- [ ] 🤖 When Google approves: upload Android 1.0.1 (6), update Play listing text
- [ ] 🤖 When Google approves: test rewarded ad on a real Android phone, link AdMob, create Play promo codes
- [ ] 👤 Update Nagly on your iPhone to 1.0.1 from the App Store and click through everything once

### Demo video (≤ 2 min, public YouTube) ⭐
- [ ] 🤖 Shot list + script (bullets, you say it in your own words)
- [ ] 🤖 Seed a clean demo state (streak, history, bond level) on the simulator/phone
- [ ] 👤 Record on your real iPhone (screen recording + your voice)
  - [ ] 0:00 Hook: boring alarm vs. "Subbu Amma" push on the lock screen
  - [ ] Log water from the lock screen (+250 ml), no app open
  - [ ] Tilt the phone → water sloshes; bubbles, confetti on goal
  - [ ] Avatar mood: proud / worried / disappointed
  - [ ] Meds & Supplements: Creatine / Protein quick pick, "Took it"
  - [ ] Personas → Spouse → Make it yours ("Subbu Amma")
  - [ ] Locked persona → personal paywall plea
  - [ ] History as a chat, Insights, weekly report
  - [ ] OneSignal: push with persona photo + Journey canvas (screen capture)
  - [ ] Close: "Live on the App Store" + link
- [ ] 👤 Upload to YouTube as **Public** (not Unlisted/Private), check it plays logged-out

### Proof / numbers (refresh on submit day)
- [ ] 🤖 RevenueCat: customers, paid subscribers, revenue
- [ ] 🤖 OneSignal: devices, push opt-ins, Journey sends + click rate
- [ ] 👤⭐ Friends' permission to quote them (creatine quote, paid-subscription quote), first names only
- [x] 👤⭐ One real user story — shared (friend screenshots); confirm consent before quoting
- [ ] 👤 Share the App Store link in 2–3 more groups (more users + maybe 1–2 more paying users)

---

## B. Devpost (Shipaton)

Enter these 5 awards: **Keep Them Coming Back (OneSignal) · Design · Peace Prize · HAMM · Catvertising**.
Skip: Grand Prize focus (traction), Influencer awards.

### Required fields
- [ ] App Store URL — ✅ ready
- [ ] Google Play URL — only if live by submit time
- [ ] Video link (public)
- [ ] Icon 1024 — ✅ `app/store/devpost/icon-1024.png`
- [ ] Screenshots 1179×2556, no frame — ✅ `app/store/devpost/v101/`
- [ ] Judge access: 7-day trial + ~5 Lifetime promo codes + redeem steps (§4 of `DEVPOST.md`). **Never post codes publicly or commit the CSV.**
- [ ] OneSignal App ID: `2858113c-c323-453d-bbc9-976b8d4c999e`

### Written answers (you write in your own words; facts are in `DEVPOST.md`)
- [ ] 🤖 Bullet drafts per award → 👤 rewrite in your voice
- [ ] ⭐ **OneSignal** (best chance): 3 Journeys, 15 voices in one Liquid template, persona photo in push, +250/+500 ml action buttons, outcomes tracked, 20% win-back CTR, screenshot of Journey canvas
- [ ] **Design**: tilt physics, mood animation, chat history, personal paywall, spotlight tour — say *where* to look
- [ ] **Peace Prize**: 1 med reminder free forever, offline, no account, data stays on phone, older parents; the real family story
- [ ] **HAMM**: free/trial/Pro ladder, 6 paywall placements, live targeting rule (trial_end → annual_first, medication_limit → monthly_first), offering metadata picks the plan, real revenue
- [ ] **Catvertising**: rewarded-only, ad = upsell (borrow a persona 24 h), RevenueCat Ads events, remote `monetization_mode` switch. Android only — weak unless Play is live
- [ ] Description: logline → problem → what it is → what's new in 1.0.1 → proof

### Submit
- [ ] 👤 Submit by **Sep 28** (can still edit until the deadline)
- [ ] 👤 Open the public submission page logged-out: video plays, links open, images show
- [ ] 👤 Final number refresh on Sep 30 before the deadline

---

## C. Office hackathon (100-point rubric)

Estimated today: **~83–94 / 100**.

| Category | Pts | What earns it in the pitch |
|---|---|---|
| Problem | 15 | "People ignore alarms, not their mom." Adherence + supplements for young colleagues |
| Functionality | 25 | Live demo on your phone: lock-screen log, meds, paywall, push |
| UI / UX | 20 | Tilt water, moods, chat history, tour |
| Innovation | 15 | Personas as the interface, Make it yours, persona photo in push |
| Polish | 15 | Live on App Store, real payment, 1.0.1 already shipped, privacy/terms |
| Track fit | 10 | No themes → fit to the goal: a real app, live in stores, with real users and payments |

### Before the pitch
- [x] 👤⭐ Track — no themes; Track fit = "shipped a real, live app" (App Store live, real payment, 1.0.1 update)
- [ ] 🤖 Slides (6–8), one per rubric category, labelled with the category, short plain text
- [ ] 🤖 QR code to the App Store page on slide 1 and the last slide
- [ ] 👤 Rehearse the 5-minute pitch twice, timed
- [ ] 👤⭐ Backup: the demo video downloaded **offline** on the laptop, in case Wi-Fi or mirroring fails
- [ ] 👤 Test iPhone → projector mirroring (cable/AirPlay) in the actual room if possible
- [ ] 👤 On demo day: phone charged, Do Not Disturb **off for Nagly** only, a push scheduled to arrive during the demo
- [ ] 👤 Have 2–3 colleagues install it before the pitch (live users in the room)

### Pitch flow (~5 min)
1. [ ] Hook — alarm vs. Subbu Amma push (Problem)
2. [ ] Live demo (Functionality + UI/UX)
3. [ ] What's different (Innovation)
4. [ ] Proof: live on App Store, paying user, friend quotes, 20% push CTR (Polish)
5. [ ] Track fit: "not a prototype — live, paid, updated" + what's next (Shipaton, Android)
6. [ ] QR code, questions

### Likely judge questions — prepare 1-line answers
- [ ] How is this different from a normal reminder app?
- [ ] How do you make money?
- [ ] Is health data safe? (stays on the phone, no account)
- [ ] Why not on Android yet? (in Google review since Sep 21)
- [ ] How much did you build with AI? (be honest, stress the decisions and shipping)

---

## D. Timeline

| Day | Do |
|---|---|
| **Sep 27 (today)** | Play notifications · organiser question · friends' permissions · video script · tell track |
| **Sep 28** | Record + upload video · write answers · **submit Devpost** |
| **Sep 29** | Office slides · rehearse · Android 1.0.1 if Play approved |
| **Sep 30** | Refresh numbers · final Devpost edits (deadline 11:45 pm PDT) |
| **Oct 1** | Office pitch · Devpost closes 12:15 pm IST |
