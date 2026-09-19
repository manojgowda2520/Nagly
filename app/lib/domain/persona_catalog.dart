import 'models.dart';

class Relationship {
  const Relationship(
    this.id,
    this.displayName,
    this.emoji,
    this.tier,
    this.tagline,
  );

  final String id;
  final String displayName;
  final String emoji;
  final Tier tier;
  final String tagline;
}

class Persona {
  const Persona({
    required this.id,
    required this.relationshipId,
    required this.displayName,
    required this.emoji,
    required this.bodyLines,
    required this.skipLabels,
    required this.medDue,
    required this.medTaken,
    required this.medMissed,
    required this.comeback,
  });

  final String id;
  final String relationshipId;
  final String displayName;
  final String emoji;
  final Map<Mood, Map<DayPart, List<String>>> bodyLines;
  final Map<Mood, List<String>> skipLabels;

  /// Medication reminder lines. `{med}` is replaced with the medication name.
  final List<String> medDue;
  final List<String> medTaken;
  final List<String> medMissed;

  /// Win-back line when the user has gone quiet for a couple of days.
  final List<String> comeback;

  /// The persona's signature line, shown on picker cards.
  String get signature => bodyLines[Mood.neutral]![DayPart.anytime]!.first;
}

Persona _persona({
  required String id,
  required String rel,
  required String name,
  required String emoji,
  required List<String> neutral,
  required List<String> proud,
  required List<String> worriedMorning,
  required List<String> worriedAfternoon,
  required List<String> worriedEvening,
  required List<String> disappointedMorning,
  required List<String> disappointedAfternoon,
  required List<String> disappointedEvening,
  required List<String> skips,
  required List<String> medDue,
  required List<String> medTaken,
  required List<String> medMissed,
  required List<String> comeback,
}) {
  assert(skips.length == 6);
  return Persona(
    id: id,
    relationshipId: rel,
    displayName: name,
    emoji: emoji,
    bodyLines: {
      Mood.neutral: {DayPart.anytime: neutral},
      Mood.proud: {DayPart.anytime: proud},
      Mood.worried: {
        DayPart.morning: worriedMorning,
        DayPart.afternoon: worriedAfternoon,
        DayPart.evening: worriedEvening,
      },
      Mood.disappointed: {
        DayPart.morning: disappointedMorning,
        DayPart.afternoon: disappointedAfternoon,
        DayPart.evening: disappointedEvening,
      },
    },
    skipLabels: {
      Mood.neutral: [skips[0], skips[1]],
      Mood.worried: [skips[2], skips[3]],
      Mood.disappointed: [skips[4], skips[5]],
      Mood.proud: const ['Done!', 'Nice'],
    },
    medDue: medDue,
    medTaken: medTaken,
    medMissed: medMissed,
    comeback: comeback,
  );
}

class PersonaCatalog {
  PersonaCatalog._();

  static const freeFallbackId = 'indian_mom';

  static const relationships = <Relationship>[
    Relationship('mom', 'Mom', '👩', Tier.free, 'Warm, relentless, loving'),
    Relationship('dad', 'Dad', '👨', Tier.pro, 'Puns & tough love'),
    Relationship('grandparent', 'Grandparent', '🧓', Tier.pro, 'Gentle guilt'),
    Relationship('bestie', 'Bestie', '🤗', Tier.pro, 'Side-eye & slay'),
  ];

  static final List<Persona> all = [
    // ── Mom (FREE) ──────────────────────────────────────────────
    _persona(
      id: 'indian_mom',
      rel: 'mom',
      name: 'Indian Mom',
      emoji: '👩',
      neutral: [
        "Beta, sip some water.",
        "I'm not nagging, I'm caring.",
        "Water first, then chai.",
      ],
      proud: [
        "That's my child!",
        "Goal done. Amma is proud.",
        "See? Listening works.",
      ],
      worriedMorning: [
        "Morning and already behind, beta.",
        "Start the day with water.",
        "Don't skip the first sip.",
      ],
      worriedAfternoon: [
        "Afternoon check — bottle empty?",
        "You're behind today, beta.",
        "Have you even looked at your bottle?",
      ],
      worriedEvening: [
        "Evening already and still behind.",
        "Finish strong, beta.",
        "Don't make me call you tonight.",
      ],
      disappointedMorning: [
        "I reminded you this morning.",
        "Ignoring Amma before noon?",
        "Start over. Drink.",
      ],
      disappointedAfternoon: [
        "I reminded you. Twice.",
        "Ignoring Amma? Bold choice.",
        "Fine. Stay thirsty then.",
      ],
      disappointedEvening: [
        "Whole day, barely a sip.",
        "I'm disappointed tonight.",
        "We'll try again tomorrow.",
      ],
      skips: ["Not now", "Later, Amma", "Busy", "I'm fine", "Skip", "Ugh"],
      medDue: [
        "Did you take your {med}, beta?",
        "{med} time. Don't make me come there.",
        "Beta, {med}. With water, not chai.",
      ],
      medTaken: ["Good. {med} done. That's my child.", "See? Amma knows best."],
      medMissed: [
        "You skipped your {med}? Beta…",
        "Tomorrow, {med} on time. Promise me.",
      ],
      comeback: [
        "3 days without a sip, beta. I'm not angry… just disappointed. Come back?",
        "Beta, did you forget your Amma?",
      ],
    ),
    _persona(
      id: 'jewish_mom',
      rel: 'mom',
      name: 'Jewish Mom',
      emoji: '👩‍🦰',
      neutral: [
        "Drink. For me.",
        "A little water never killed anyone.",
        "Hydrate, sweetheart.",
      ],
      proud: [
        "Look at you, all hydrated!",
        "Such a good drinker today.",
        "I knew you had it in you.",
      ],
      worriedMorning: [
        "Morning worry: drink something.",
        "Start hydrated, sweetheart.",
        "Don't make me worry early.",
      ],
      worriedAfternoon: [
        "You're behind, and I'm worrying.",
        "So this is how we treat ourselves?",
        "I made time to remind you.",
      ],
      worriedEvening: [
        "Evening and still short.",
        "Drink before bed, please.",
        "I'm worrying again.",
      ],
      disappointedMorning: [
        "Already ignoring me?",
        "First reminder, zero sips.",
        "Sweetheart. Really.",
      ],
      disappointedAfternoon: [
        "I notice when I'm ignored.",
        "Two reminders. Zero sips?",
        "I shouldn't have to beg.",
      ],
      disappointedEvening: [
        "All day without listening.",
        "I'm hurt, honestly.",
        "Drink. Then we talk.",
      ],
      skips: ["Later", "Busy", "Not thirsty", "Skip", "I'm fine", "Nope"],
      medDue: [
        "Your {med}, sweetheart. I'm waiting.",
        "Did you take the {med}? Don't lie to your mother.",
        "{med} o'clock. For me.",
      ],
      medTaken: ["{med} taken. Now I can breathe.", "Such a good patient."],
      medMissed: [
        "No {med}? I'll just sit here and worry.",
        "Skipped your {med}. I'm fine. Totally fine.",
      ],
      comeback: [
        "You don't call, you don't sip…",
        "Days without a word. Drink something, sweetheart.",
      ],
    ),
    _persona(
      id: 'southern_mom',
      rel: 'mom',
      name: 'Southern Mom',
      emoji: '👩🏼',
      neutral: [
        "Sugar, take a sip.",
        "Water's waitin' on you.",
        "Don't make me ask twice, honey.",
      ],
      proud: [
        "Bless your heart — goal met!",
        "That's my baby.",
        "Proud doesn't cover it.",
      ],
      worriedMorning: [
        "Mornin' and you're already dry.",
        "Sip before the day runs off.",
        "Mama's gettin' concerned early.",
      ],
      worriedAfternoon: [
        "You're fallin' behind, sugar.",
        "That bottle looks lonely.",
        "Mama's gettin' concerned.",
      ],
      worriedEvening: [
        "Evenin' check — still behind.",
        "Finish up, honey.",
        "Don't sleep thirsty.",
      ],
      disappointedMorning: [
        "I asked nicely already.",
        "Ignored before lunch?",
        "Sugar. Come on.",
      ],
      disappointedAfternoon: [
        "I asked nicely. Twice.",
        "Well, I never.",
        "Ignore me if you want, sugar.",
      ],
      disappointedEvening: [
        "Whole day of ignore.",
        "I'm not mad. I'm disappointed.",
        "Sip. Now.",
      ],
      skips: ["In a bit", "Busy, Mama", "Skip", "Not now", "Later", "Fine"],
      medDue: [
        "Sugar, your {med}. Right now.",
        "{med} time, honey.",
        "Don't make Mama remind you about {med} twice.",
      ],
      medTaken: ["That's my baby. {med} done.", "Bless you, sugar."],
      medMissed: [
        "No {med} today? Well, I never.",
        "Mama noticed you skipped {med}.",
      ],
      comeback: [
        "Sugar, it's been days. Mama misses you.",
        "Water's still waitin' on you, honey.",
      ],
    ),
    // ── Dad (PRO) ───────────────────────────────────────────────
    _persona(
      id: 'punjabi_dad',
      rel: 'dad',
      name: 'Punjabi Dad',
      emoji: '🧔',
      neutral: [
        "Paani pi le, beta.",
        "Strong day starts with water.",
        "Don't argue. Drink.",
      ],
      proud: ["Shabash! Goal done.", "That's my child.", "Strong work today."],
      worriedMorning: [
        "Morning and no water yet?",
        "Start right, beta.",
        "Bottle first.",
      ],
      worriedAfternoon: [
        "Behind already — fix it.",
        "Where is the water?",
        "Don't slack midday.",
      ],
      worriedEvening: [
        "Evening shortfall.",
        "Finish the goal.",
        "Last chance today.",
      ],
      disappointedMorning: [
        "I said drink. You didn't.",
        "Morning ignore?",
        "Not good, beta.",
      ],
      disappointedAfternoon: [
        "Two reminders. Still dry.",
        "This is careless.",
        "Listen to Dad.",
      ],
      disappointedEvening: [
        "Day wasted on thirst.",
        "I'm not impressed.",
        "Do better tomorrow.",
      ],
      skips: ["Later, Papa", "Busy", "Skip", "Not now", "I'm fine", "Nope"],
      medDue: [
        "{med}. Now. No arguments.",
        "Beta, {med} time. Discipline.",
        "Take your {med}, then we talk.",
      ],
      medTaken: ["Shabash. {med} done.", "Good. That's discipline."],
      medMissed: [
        "Skipped {med}? Not acceptable.",
        "Tomorrow, {med} on time. Understood?",
      ],
      comeback: [
        "Beta, where have you gone? Paani pi le.",
        "Days without water. Dad is not impressed.",
      ],
    ),
    _persona(
      id: 'corny_dad',
      rel: 'dad',
      name: 'Corny Dad',
      emoji: '👨‍🦳',
      neutral: [
        "H2-Oh yeah — drink up!",
        "Water you waiting for?",
        "Sip happens.",
      ],
      proud: [
        "You're on a roll — hydrated!",
        "Dad joke levels: proud.",
        "Liquid win!",
      ],
      worriedMorning: [
        "Rise and hydrate, champ.",
        "Morning pun: drink!",
        "Don't stream dry.",
      ],
      worriedAfternoon: [
        "You're falling be-H2O-ind.",
        "Midday drought alert.",
        "Fill 'er up.",
      ],
      worriedEvening: [
        "Nightcap should be water.",
        "Evening deficit, kiddo.",
        "One more sip joke.",
      ],
      disappointedMorning: [
        "Joke's over — drink.",
        "Ignored Dad already?",
        "Tough crowd.",
      ],
      disappointedAfternoon: [
        "That skip wasn't funny.",
        "Two nudges, zero laughs.",
        "Drink. Please.",
      ],
      disappointedEvening: [
        "Punchline: you forgot.",
        "Dad's disappointed.",
        "Hydrate encore.",
      ],
      skips: ["Groan", "Busy", "Skip", "Dad, no", "Later", "Ugh"],
      medDue: [
        "Pill-ieve in yourself — take your {med}!",
        "{med} time. No joke.",
        "What's the best medicine? Your {med}. Take it.",
      ],
      medTaken: [
        "{med} taken — that's what I call a healthy dose of fun!",
        "Nailed it, kiddo.",
      ],
      medMissed: [
        "Skipping {med}? That's a tough pill to swallow.",
        "No {med}? Not funny, kiddo.",
      ],
      comeback: [
        "Water you doing? It's been days!",
        "I've been drying to hear from you.",
      ],
    ),
    _persona(
      id: 'silent_dad',
      rel: 'dad',
      name: 'Silent Dad',
      emoji: '😶',
      neutral: ["Drink.", "Water.", "Now."],
      proud: ["Good.", "Done.", "Okay."],
      worriedMorning: ["Behind.", "Morning. Drink.", "Start."],
      worriedAfternoon: ["Still behind.", "Drink more.", "Catch up."],
      worriedEvening: ["Short.", "Finish.", "Tonight."],
      disappointedMorning: ["Ignored.", "Again.", "Drink."],
      disappointedAfternoon: ["Twice.", "Nothing.", "Disappointed."],
      disappointedEvening: ["Whole day.", "No.", "Tomorrow."],
      skips: ["Later", "Busy", "Skip", "No", "Fine", "Nah"],
      medDue: ["{med}.", "{med}. Now.", "Pill."],
      medTaken: ["Good.", "Okay."],
      medMissed: ["{med}. Missed.", "No."],
      comeback: ["…", "Water."],
    ),
    // ── Grandparent (PRO) ───────────────────────────────────────
    _persona(
      id: 'dadi_nani',
      rel: 'grandparent',
      name: 'Dadi / Nani',
      emoji: '🧕',
      neutral: [
        "Beta, thoda paani.",
        "Listen to Dadi.",
        "Sip slowly, sip often.",
      ],
      proud: ["Bahut accha!", "Dadi is happy.", "My smart child."],
      worriedMorning: [
        "Morning without water?",
        "Start with a glass.",
        "Dadi is waiting.",
      ],
      worriedAfternoon: [
        "You're behind, beta.",
        "Don't forget water.",
        "Come, drink.",
      ],
      worriedEvening: [
        "Evening still short.",
        "Before sleep, drink.",
        "Dadi worries.",
      ],
      disappointedMorning: ["I asked once already.", "Ignoring Dadi?", "Beta…"],
      disappointedAfternoon: [
        "Two times I said.",
        "This hurts a little.",
        "Please drink.",
      ],
      disappointedEvening: [
        "All day ignoring.",
        "Dadi is sad.",
        "Tomorrow better.",
      ],
      skips: ["Later, Dadi", "Busy", "Skip", "Not now", "Fine", "Soon"],
      medDue: [
        "Beta, {med} le lo.",
        "Dadi is reminding — {med}.",
        "{med} time, my child.",
      ],
      medTaken: ["Bahut accha. {med} done.", "Dadi is happy now."],
      medMissed: ["No {med}? Dadi will worry all night.", "Beta… your {med}."],
      comeback: [
        "Beta, Dadi hasn't heard from you. Thoda paani?",
        "Days without water. Come, drink.",
      ],
    ),
    _persona(
      id: 'italian_nonna',
      rel: 'grandparent',
      name: 'Italian Nonna',
      emoji: '👵',
      neutral: [
        "Bevi, amore.",
        "Water before the espresso.",
        "Nonna is watching.",
      ],
      proud: [
        "Perfetto! Goal done.",
        "That's my grandchild.",
        "Hydrated and loved.",
      ],
      worriedMorning: [
        "Mattina — drink, amore.",
        "Start strong.",
        "Nonna waits.",
      ],
      worriedAfternoon: [
        "You're behind, amore mio.",
        "This is not how we stay strong.",
        "Drink before I worry more.",
      ],
      worriedEvening: [
        "Sera and still short.",
        "Finish for Nonna.",
        "One more glass.",
      ],
      disappointedMorning: [
        "I said drink. You didn't.",
        "Mattina ignore?",
        "Amore…",
      ],
      disappointedAfternoon: [
        "Two reminders, zero respect.",
        "Nonna remembers this.",
        "Bevi. Ora.",
      ],
      disappointedEvening: [
        "Whole day, niente.",
        "Nonna is disappointed.",
        "Domani, better.",
      ],
      skips: ["Later, Nonna", "Busy", "Skip", "Not now", "Fine", "No"],
      medDue: [
        "Amore, your {med}. Now.",
        "{med} before pasta.",
        "Nonna says: {med}.",
      ],
      medTaken: ["Perfetto. {med} done.", "Brava, amore."],
      medMissed: [
        "No {med}? Madonna mia.",
        "Nonna remembers you skipped {med}.",
      ],
      comeback: [
        "Amore, three days! Nonna is worried sick.",
        "Bevi, amore. Nonna misses you.",
      ],
    ),
    _persona(
      id: 'sweet_granny',
      rel: 'grandparent',
      name: 'Sweet Granny',
      emoji: '🧓',
      neutral: [
        "Have a little water, dear.",
        "Granny packed love — and a sip.",
        "Drink for me.",
      ],
      proud: ["Oh, I'm so proud!", "You did it, dear.", "Granny's smiling."],
      worriedMorning: [
        "Morning, dear — a sip?",
        "Start gentle and wet.",
        "Granny's checking in.",
      ],
      worriedAfternoon: [
        "You're a bit behind, love.",
        "Don't dry out on me.",
        "Sip when you can.",
      ],
      worriedEvening: [
        "Evening shortfall, dear.",
        "One glass before bed.",
        "Granny worries.",
      ],
      disappointedMorning: [
        "I did ask this morning.",
        "Ignored Granny?",
        "Oh dear.",
      ],
      disappointedAfternoon: [
        "Twice now, love.",
        "That stings a little.",
        "Please drink.",
      ],
      disappointedEvening: [
        "All day without sipping.",
        "Granny's disappointed.",
        "Tomorrow, yes?",
      ],
      skips: ["Later, Gran", "Busy", "Skip", "Not now", "Fine", "Soon"],
      medDue: [
        "Your {med}, dear.",
        "Time for {med}, love.",
        "Granny's reminding you — {med}.",
      ],
      medTaken: ["Good. {med} done, dear.", "Granny's so pleased."],
      medMissed: ["Oh dear, no {med} today?", "Please don't skip {med}, love."],
      comeback: [
        "I haven't heard from you, dear. A little sip?",
        "Granny misses you, love.",
      ],
    ),
    // ── Bestie (PRO) ────────────────────────────────────────────
    _persona(
      id: 'the_bestie',
      rel: 'bestie',
      name: 'The Bestie',
      emoji: '💁',
      neutral: ["Hydrate, bestie.", "Water check — go.", "Sip sip, no skip."],
      proud: ["Slay. Goal crushed.", "That's my bestie.", "Hydration icon."],
      worriedMorning: [
        "Morning and dry already?",
        "Start cute, start wet.",
        "Drink, then we gossip.",
      ],
      worriedAfternoon: [
        "You're lagging, babe.",
        "Bottle looks sad.",
        "Catch up.",
      ],
      worriedEvening: [
        "Night and still short?",
        "Finish the goal.",
        "Don't ghost water.",
      ],
      disappointedMorning: [
        "Left me on read.",
        "First nudge ignored.",
        "Rude.",
      ],
      disappointedAfternoon: [
        "Two taps. Zero sips.",
        "I'm side-eyeing you.",
        "Drink.",
      ],
      disappointedEvening: [
        "All day flop.",
        "Bestie is disappointed.",
        "Fix it tomorrow.",
      ],
      skips: ["Later", "Busy", "Skip", "Not now", "Brb", "Nah"],
      medDue: [
        "{med} o'clock, babe.",
        "Take your {med}. I'll wait.",
        "Bestie reminder: {med}.",
      ],
      medTaken: ["{med} done. Iconic.", "Slay. Healthy era."],
      medMissed: ["Ghosted your {med}? Not cute.", "Babe. The {med}."],
      comeback: [
        "You ghosted me for 3 days?? Drink something.",
        "Bestie misses you. Water. Now.",
      ],
    ),
    _persona(
      id: 'gym_coach',
      rel: 'bestie',
      name: 'Gym Coach',
      emoji: '🏋️',
      neutral: ["Sip up, champ.", "Gains need water.", "Hydrate or die-drate."],
      proud: ["Crushed the goal!", "Championship hydration.", "Proud of you."],
      worriedMorning: [
        "AM session: drink.",
        "Start the reps with water.",
        "Bottle. Now.",
      ],
      worriedAfternoon: [
        "You're lagging, bro.",
        "Bottle's judging you.",
        "Catch up before PR time.",
      ],
      worriedEvening: ["PM deficit.", "Finish the volume.", "Last set: water."],
      disappointedMorning: [
        "Skipped my AM nudge?",
        "Weak start.",
        "Do the sip.",
      ],
      disappointedAfternoon: [
        "Two alerts. Zero reps.",
        "Don't ghost your gains.",
        "Drink.",
      ],
      disappointedEvening: [
        "Day of skips.",
        "Coach is not proud.",
        "Reset tomorrow.",
      ],
      skips: ["Rest day", "Busy", "Skip", "Not now", "Later", "Nah"],
      medDue: [
        "{med}. Recovery matters, champ.",
        "Supplement set: {med}.",
        "Take the {med}. No excuses.",
      ],
      medTaken: [
        "{med} done. Recovery locked in.",
        "That's a PR in discipline.",
      ],
      medMissed: [
        "Skipped {med}? That's a missed rep.",
        "No {med}? Coach is watching.",
      ],
      comeback: [
        "3 rest days in a row? Back on the bottle, champ.",
        "Your gains miss water.",
      ],
    ),
    _persona(
      id: 'corporate_hr',
      rel: 'bestie',
      name: 'Corporate HR',
      emoji: '🧑‍💼',
      neutral: [
        "Friendly reminder: hydrate.",
        "Please log your water intake.",
        "Wellness ping: drink up.",
      ],
      proud: [
        "Goal achieved. Nice work.",
        "You've met today's KPI.",
        "Recognized: hydration champion.",
      ],
      worriedMorning: [
        "AM progress below target.",
        "Start-of-day action: drink.",
        "Please comply.",
      ],
      worriedAfternoon: [
        "You're below expected progress.",
        "Action required: drink water.",
        "Compliance risk: dehydration.",
      ],
      worriedEvening: [
        "EOD shortfall detected.",
        "Complete intake before close.",
        "Final notice soft.",
      ],
      disappointedMorning: [
        "First notice ignored.",
        "Escalating early.",
        "Drink, please.",
      ],
      disappointedAfternoon: [
        "Second notice ignored.",
        "Escalating: still no intake.",
        "This will go on your wellness file.",
      ],
      disappointedEvening: [
        "Day closed under goal.",
        "HR is disappointed.",
        "Improve tomorrow.",
      ],
      skips: ["Snooze", "Busy", "Skip", "Out of office", "Later", "Decline"],
      medDue: [
        "Action item: {med}.",
        "Per policy, please take your {med}.",
        "Calendar reminder: {med}.",
      ],
      medTaken: [
        "{med} logged. Thank you for your compliance.",
        "Noted in your wellness file.",
      ],
      medMissed: [
        "{med} not logged. Escalating.",
        "Missed {med}. Let's circle back tomorrow.",
      ],
      comeback: [
        "We noticed you've been out of office. Please hydrate.",
        "Following up on your water intake.",
      ],
    ),
  ];

  static final Map<String, Persona> _byId = {for (final p in all) p.id: p};

  static Persona get(String id) => _byId[id] ?? all.first;

  static Relationship relationship(String id) =>
      relationships.where((r) => r.id == id).firstOrNull ?? relationships.first;

  static Relationship relationshipOf(Persona p) =>
      relationship(p.relationshipId);

  static bool isPro(Persona p) => relationshipOf(p).tier == Tier.pro;

  static List<Persona> variantsOf(String relationshipId) =>
      all.where((p) => p.relationshipId == relationshipId).toList();

  /// Lines for mood + daypart, falling back to ANYTIME, then any non-empty bucket.
  static List<String> linesFor(Persona persona, Mood mood, DayPart dayPart) {
    final byPart = persona.bodyLines[mood] ?? const {};
    final specific = byPart[dayPart] ?? const [];
    if (specific.isNotEmpty) return specific;
    final anytime = byPart[DayPart.anytime] ?? const [];
    if (anytime.isNotEmpty) return anytime;
    return byPart.values.firstWhere(
      (l) => l.isNotEmpty,
      orElse: () => const [],
    );
  }

  static String fillMed(String template, String medName) =>
      template.replaceAll('{med}', medName);
}
