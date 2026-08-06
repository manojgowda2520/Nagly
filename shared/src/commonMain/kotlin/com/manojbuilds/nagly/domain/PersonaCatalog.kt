package com.manojbuilds.nagly.domain

import com.manojbuilds.nagly.domain.model.DayPart
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.model.Relationship
import com.manojbuilds.nagly.domain.model.Tier

object PersonaCatalog {
    val relationships: List<Relationship> = listOf(
        Relationship("mom", "Mom", "👩", Tier.FREE),
        Relationship("dad", "Dad", "👨", Tier.PRO),
        Relationship("grandparent", "Grandparent", "🧓", Tier.PRO),
        Relationship("bestie", "Bestie", "🤗", Tier.PRO),
    )

    private val relationshipsById = relationships.associateBy { it.id }

    val all: List<Persona> = listOf(
        // Mom (FREE)
        buildPersona(
            id = "indian_mom", relationshipId = "mom", displayName = "Indian Mom", emoji = "👩",
            neutral = listOf("Beta, sip some water.", "I'm not nagging, I'm caring.", "Water first, then chai."),
            proud = listOf("That's my child!", "Goal done. Amma is proud.", "See? Listening works."),
            worriedMorning = listOf("Morning and already behind, beta.", "Start the day with water.", "Don't skip the first sip."),
            worriedAfternoon = listOf("Afternoon check — bottle empty?", "You're behind today, beta.", "Have you even looked at your bottle?"),
            worriedEvening = listOf("Evening already and still behind.", "Finish strong, beta.", "Don't make me call you tonight."),
            disappointedMorning = listOf("I reminded you this morning.", "Ignoring Amma before noon?", "Start over. Drink."),
            disappointedAfternoon = listOf("I reminded you. Twice.", "Ignoring Amma? Bold choice.", "Fine. Stay thirsty then."),
            disappointedEvening = listOf("Whole day, barely a sip.", "I'm disappointed tonight.", "We'll try again tomorrow."),
            skips = moodSkips("Not now", "Later, Amma", "Busy", "I'm fine", "Skip", "Ugh"),
        ),
        buildPersona(
            id = "jewish_mom", relationshipId = "mom", displayName = "Jewish Mom", emoji = "👩‍🦰",
            neutral = listOf("Drink. For me.", "A little water never killed anyone.", "Hydrate, sweetheart."),
            proud = listOf("Look at you, all hydrated!", "Such a good drinker today.", "I knew you had it in you."),
            worriedMorning = listOf("Morning worry: drink something.", "Start hydrated, sweetheart.", "Don't make me worry early."),
            worriedAfternoon = listOf("You're behind, and I'm worrying.", "So this is how we treat ourselves?", "I made time to remind you."),
            worriedEvening = listOf("Evening and still short.", "Drink before bed, please.", "I'm worrying again."),
            disappointedMorning = listOf("Already ignoring me?", "First reminder, zero sips.", "Sweetheart. Really."),
            disappointedAfternoon = listOf("I notice when I'm ignored.", "Two reminders. Zero sips?", "I shouldn't have to beg."),
            disappointedEvening = listOf("All day without listening.", "I'm hurt, honestly.", "Drink. Then we talk."),
            skips = moodSkips("Later", "Busy", "Not thirsty", "Skip", "I'm fine", "Nope"),
        ),
        buildPersona(
            id = "southern_mom", relationshipId = "mom", displayName = "Southern Mom", emoji = "👩🏼",
            neutral = listOf("Sugar, take a sip.", "Water's waitin' on you.", "Don't make me ask twice, honey."),
            proud = listOf("Bless your heart — goal met!", "That's my baby.", "Proud doesn't cover it."),
            worriedMorning = listOf("Mornin' and you're already dry.", "Sip before the day runs off.", "Mama's gettin' concerned early."),
            worriedAfternoon = listOf("You're fallin' behind, sugar.", "That bottle looks lonely.", "Mama's gettin' concerned."),
            worriedEvening = listOf("Evenin' check — still behind.", "Finish up, honey.", "Don't sleep thirsty."),
            disappointedMorning = listOf("I asked nicely already.", "Ignored before lunch?", "Sugar. Come on."),
            disappointedAfternoon = listOf("I asked nicely. Twice.", "Well, I never.", "Ignore me if you want, sugar."),
            disappointedEvening = listOf("Whole day of ignore.", "I'm not mad. I'm disappointed.", "Sip. Now."),
            skips = moodSkips("In a bit", "Busy, Mama", "Skip", "Not now", "Later", "Fine"),
        ),
        // Dad (PRO)
        buildPersona(
            id = "punjabi_dad", relationshipId = "dad", displayName = "Punjabi Dad", emoji = "🧔",
            neutral = listOf("Paani pi le, beta.", "Strong day starts with water.", "Don't argue. Drink."),
            proud = listOf("Shabash! Goal done.", "That's my child.", "Strong work today."),
            worriedMorning = listOf("Morning and no water yet?", "Start right, beta.", "Bottle first."),
            worriedAfternoon = listOf("Behind already — fix it.", "Where is the water?", "Don't slack midday."),
            worriedEvening = listOf("Evening shortfall.", "Finish the goal.", "Last chance today."),
            disappointedMorning = listOf("I said drink. You didn't.", "Morning ignore?", "Not good, beta."),
            disappointedAfternoon = listOf("Two reminders. Still dry.", "This is careless.", "Listen to Dad."),
            disappointedEvening = listOf("Day wasted on thirst.", "I'm not impressed.", "Do better tomorrow."),
            skips = moodSkips("Later, Papa", "Busy", "Skip", "Not now", "I'm fine", "Nope"),
        ),
        buildPersona(
            id = "corny_dad", relationshipId = "dad", displayName = "Corny Dad", emoji = "👨‍🦳",
            neutral = listOf("H2-Oh yeah — drink up!", "Water you waiting for?", "Sip happens."),
            proud = listOf("You're on a roll — hydrated!", "Dad joke levels: proud.", "Liquid win!"),
            worriedMorning = listOf("Rise and hydrate, champ.", "Morning pun: drink!", "Don't stream dry."),
            worriedAfternoon = listOf("You're falling be-H2O-ind.", "Midday drought alert.", "Fill 'er up."),
            worriedEvening = listOf("Nightcap should be water.", "Evening deficit, kiddo.", "One more sip joke."),
            disappointedMorning = listOf("Joke's over — drink.", "Ignored Dad already?", "Tough crowd."),
            disappointedAfternoon = listOf("That skip wasn't funny.", "Two nudges, zero laughs.", "Drink. Please."),
            disappointedEvening = listOf("Punchline: you forgot.", "Dad's disappointed.", "Hydrate encore."),
            skips = moodSkips("Groan", "Busy", "Skip", "Dad, no", "Later", "Ugh"),
        ),
        buildPersona(
            id = "silent_dad", relationshipId = "dad", displayName = "Silent Dad", emoji = "😶",
            neutral = listOf("Drink.", "Water.", "Now."),
            proud = listOf("Good.", "Done.", "Okay."),
            worriedMorning = listOf("Behind.", "Morning. Drink.", "Start."),
            worriedAfternoon = listOf("Still behind.", "Drink more.", "Catch up."),
            worriedEvening = listOf("Short.", "Finish.", "Tonight."),
            disappointedMorning = listOf("Ignored.", "Again.", "Drink."),
            disappointedAfternoon = listOf("Twice.", "Nothing.", "Disappointed."),
            disappointedEvening = listOf("Whole day.", "No.", "Tomorrow."),
            skips = moodSkips("Later", "Busy", "Skip", "No", "Fine", "Nah"),
        ),
        // Grandparent (PRO)
        buildPersona(
            id = "dadi_nani", relationshipId = "grandparent", displayName = "Dadi / Nani", emoji = "🧕",
            neutral = listOf("Beta, thoda paani.", "Listen to Dadi.", "Sip slowly, sip often."),
            proud = listOf("Bahut accha!", "Dadi is happy.", "My smart child."),
            worriedMorning = listOf("Morning without water?", "Start with a glass.", "Dadi is waiting."),
            worriedAfternoon = listOf("You're behind, beta.", "Don't forget water.", "Come, drink."),
            worriedEvening = listOf("Evening still short.", "Before sleep, drink.", "Dadi worries."),
            disappointedMorning = listOf("I asked once already.", "Ignoring Dadi?", "Beta…"),
            disappointedAfternoon = listOf("Two times I said.", "This hurts a little.", "Please drink."),
            disappointedEvening = listOf("All day ignoring.", "Dadi is sad.", "Tomorrow better."),
            skips = moodSkips("Later, Dadi", "Busy", "Skip", "Not now", "Fine", "Soon"),
        ),
        buildPersona(
            id = "italian_nonna", relationshipId = "grandparent", displayName = "Italian Nonna", emoji = "👵",
            neutral = listOf("Bevi, amore.", "Water before the espresso.", "Nonna is watching."),
            proud = listOf("Perfetto! Goal done.", "That's my grandchild.", "Hydrated and loved."),
            worriedMorning = listOf("Mattina — drink, amore.", "Start strong.", "Nonna waits."),
            worriedAfternoon = listOf("You're behind, amore mio.", "This is not how we stay strong.", "Drink before I worry more."),
            worriedEvening = listOf("Sera and still short.", "Finish for Nonna.", "One more glass."),
            disappointedMorning = listOf("I said drink. You didn't.", "Mattina ignore?", "Amore…"),
            disappointedAfternoon = listOf("Two reminders, zero respect.", "Nonna remembers this.", "Bevi. Ora."),
            disappointedEvening = listOf("Whole day, niente.", "Nonna is disappointed.", "Domani, better."),
            skips = moodSkips("Later, Nonna", "Busy", "Skip", "Not now", "Fine", "No"),
        ),
        buildPersona(
            id = "sweet_granny", relationshipId = "grandparent", displayName = "Sweet Granny", emoji = "🧓",
            neutral = listOf("Have a little water, dear.", "Granny packed love — and a sip.", "Drink for me."),
            proud = listOf("Oh, I'm so proud!", "You did it, dear.", "Granny's smiling."),
            worriedMorning = listOf("Morning, dear — a sip?", "Start gentle and wet.", "Granny's checking in."),
            worriedAfternoon = listOf("You're a bit behind, love.", "Don't dry out on me.", "Sip when you can."),
            worriedEvening = listOf("Evening shortfall, dear.", "One glass before bed.", "Granny worries."),
            disappointedMorning = listOf("I did ask this morning.", "Ignored Granny?", "Oh dear."),
            disappointedAfternoon = listOf("Twice now, love.", "That stings a little.", "Please drink."),
            disappointedEvening = listOf("All day without sipping.", "Granny's disappointed.", "Tomorrow, yes?"),
            skips = moodSkips("Later, Gran", "Busy", "Skip", "Not now", "Fine", "Soon"),
        ),
        // Bestie (PRO)
        buildPersona(
            id = "the_bestie", relationshipId = "bestie", displayName = "The Bestie", emoji = "💁",
            neutral = listOf("Hydrate, bestie.", "Water check — go.", "Sip sip, no skip."),
            proud = listOf("Slay. Goal crushed.", "That's my bestie.", "Hydration icon."),
            worriedMorning = listOf("Morning and dry already?", "Start cute, start wet.", "Drink, then we gossip."),
            worriedAfternoon = listOf("You're lagging, babe.", "Bottle looks sad.", "Catch up."),
            worriedEvening = listOf("Night and still short?", "Finish the goal.", "Don't ghost water."),
            disappointedMorning = listOf("Left me on read.", "First nudge ignored.", "Rude."),
            disappointedAfternoon = listOf("Two taps. Zero sips.", "I'm side-eyeing you.", "Drink."),
            disappointedEvening = listOf("All day flop.", "Bestie is disappointed.", "Fix it tomorrow."),
            skips = moodSkips("Later", "Busy", "Skip", "Not now", "Brb", "Nah"),
        ),
        buildPersona(
            id = "gym_coach", relationshipId = "bestie", displayName = "Gym Coach", emoji = "🏋️",
            neutral = listOf("Sip up, champ.", "Gains need water.", "Hydrate or die-drate."),
            proud = listOf("Crushed the goal!", "Championship hydration.", "Proud of you."),
            worriedMorning = listOf("AM session: drink.", "Start the reps with water.", "Bottle. Now."),
            worriedAfternoon = listOf("You're lagging, bro.", "Bottle's judging you.", "Catch up before PR time."),
            worriedEvening = listOf("PM deficit.", "Finish the volume.", "Last set: water."),
            disappointedMorning = listOf("Skipped my AM nudge?", "Weak start.", "Do the sip."),
            disappointedAfternoon = listOf("Two alerts. Zero reps.", "Don't ghost your gains.", "Drink."),
            disappointedEvening = listOf("Day of skips.", "Coach is not proud.", "Reset tomorrow."),
            skips = moodSkips("Rest day", "Busy", "Skip", "Not now", "Later", "Nah"),
        ),
        buildPersona(
            id = "corporate_hr", relationshipId = "bestie", displayName = "Corporate HR", emoji = "🧑‍💼",
            neutral = listOf("Friendly reminder: hydrate.", "Please log your water intake.", "Wellness ping: drink up."),
            proud = listOf("Goal achieved. Nice work.", "You've met today's KPI.", "Recognized: hydration champion."),
            worriedMorning = listOf("AM progress below target.", "Start-of-day action: drink.", "Please comply."),
            worriedAfternoon = listOf("You're below expected progress.", "Action required: drink water.", "Compliance risk: dehydration."),
            worriedEvening = listOf("EOD shortfall detected.", "Complete intake before close.", "Final notice soft."),
            disappointedMorning = listOf("First notice ignored.", "Escalating early.", "Drink, please."),
            disappointedAfternoon = listOf("Second notice ignored.", "Escalating: still no intake.", "This will go on your wellness file."),
            disappointedEvening = listOf("Day closed under goal.", "HR is disappointed.", "Improve tomorrow."),
            skips = moodSkips("Snooze", "Busy", "Skip", "Out of office", "Later", "Decline"),
        ),
    )

    private val byId = all.associateBy { it.id }

    fun get(id: String): Persona = byId[id] ?: all.first()

    fun relationship(id: String): Relationship =
        relationshipsById[id] ?: relationships.first()

    fun relationshipOf(persona: Persona): Relationship =
        relationship(persona.relationshipId)

    fun isPro(persona: Persona): Boolean =
        relationshipOf(persona).tier == Tier.PRO

    fun variantsOf(relationshipId: String): List<Persona> =
        all.filter { it.relationshipId == relationshipId }

    val free: List<Persona> get() = all.filterNot(::isPro)
    val pro: List<Persona> get() = all.filter(::isPro)

    /**
     * Lines for mood + daypart, falling back to ANYTIME when the daypart bucket is empty,
     * then any non-empty bucket for that mood.
     */
    fun linesFor(persona: Persona, mood: Mood, dayPart: DayPart): List<String> {
        val byPart = persona.bodyLines[mood].orEmpty()
        val specific = byPart[dayPart].orEmpty()
        if (specific.isNotEmpty()) return specific
        val anytime = byPart[DayPart.ANYTIME].orEmpty()
        if (anytime.isNotEmpty()) return anytime
        return byPart.values.firstOrNull { it.isNotEmpty() }.orEmpty()
    }

    private fun moodSkips(
        n1: String, n2: String,
        w1: String, w2: String,
        d1: String, d2: String,
    ): Map<Mood, List<String>> = mapOf(
        Mood.NEUTRAL to listOf(n1, n2),
        Mood.WORRIED to listOf(w1, w2),
        Mood.DISAPPOINTED to listOf(d1, d2),
        Mood.PROUD to listOf("Done!", "Nice"),
    )

    private fun buildPersona(
        id: String,
        relationshipId: String,
        displayName: String,
        emoji: String,
        neutral: List<String>,
        proud: List<String>,
        worriedMorning: List<String>,
        worriedAfternoon: List<String>,
        worriedEvening: List<String>,
        disappointedMorning: List<String>,
        disappointedAfternoon: List<String>,
        disappointedEvening: List<String>,
        skips: Map<Mood, List<String>>,
    ): Persona {
        require(neutral.size == 3 && proud.size == 3)
        require(
            listOf(
                worriedMorning, worriedAfternoon, worriedEvening,
                disappointedMorning, disappointedAfternoon, disappointedEvening,
            ).all { it.size == 3 },
        )
        require(skips.values.all { it.size >= 2 })
        return Persona(
            id = id,
            relationshipId = relationshipId,
            displayName = displayName,
            emoji = emoji,
            bodyLines = mapOf(
                Mood.NEUTRAL to mapOf(DayPart.ANYTIME to neutral),
                Mood.PROUD to mapOf(DayPart.ANYTIME to proud),
                Mood.WORRIED to mapOf(
                    DayPart.MORNING to worriedMorning,
                    DayPart.AFTERNOON to worriedAfternoon,
                    DayPart.EVENING to worriedEvening,
                ),
                Mood.DISAPPOINTED to mapOf(
                    DayPart.MORNING to disappointedMorning,
                    DayPart.AFTERNOON to disappointedAfternoon,
                    DayPart.EVENING to disappointedEvening,
                ),
            ),
            skipLabels = skips,
        )
    }
}
