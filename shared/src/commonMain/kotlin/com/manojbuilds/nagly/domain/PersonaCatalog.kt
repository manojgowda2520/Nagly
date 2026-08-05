package com.manojbuilds.nagly.domain

import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona

object PersonaCatalog {
    val all: List<Persona> = listOf(
        persona(
            id = "indian_mom",
            displayName = "Indian Mom",
            emoji = "🍛",
            isPro = false,
            neutral = listOf("Beta, sip some water.", "I'm not nagging, I'm caring.", "Water first, then chai."),
            worried = listOf("You're behind today, beta.", "Have you even looked at your bottle?", "Don't make me call you."),
            disappointed = listOf("I reminded you. Twice.", "Ignoring Amma? Bold choice.", "Fine. Stay thirsty then."),
            proud = listOf("That's my child!", "Goal done. Amma is proud.", "See? Listening works."),
        ),
        persona(
            id = "jewish_mom",
            displayName = "Jewish Mom",
            emoji = "🥯",
            isPro = false,
            neutral = listOf("Drink. For me.", "A little water never killed anyone.", "Hydrate, sweetheart."),
            worried = listOf("You're behind, and I'm worrying.", "So this is how we treat ourselves?", "I made time to remind you."),
            disappointed = listOf("I notice when I'm ignored.", "Two reminders. Zero sips?", "I shouldn't have to beg."),
            proud = listOf("Look at you, all hydrated!", "Such a good drinker today.", "I knew you had it in you."),
        ),
        persona(
            id = "southern_mom",
            displayName = "Southern Mom",
            emoji = "🍑",
            isPro = false,
            neutral = listOf("Sugar, take a sip.", "Water's waitin' on you.", "Don't make me ask twice, honey."),
            worried = listOf("You're fallin' behind, sugar.", "That bottle looks lonely.", "Mama's gettin' concerned."),
            disappointed = listOf("I asked nicely. Twice.", "Well, I never.", "Ignore me if you want, sugar."),
            proud = listOf("Bless your heart — goal met!", "That's my baby.", "Proud doesn't cover it."),
        ),
        persona(
            id = "italian_nonna",
            displayName = "Italian Nonna",
            emoji = "🍝",
            isPro = true,
            neutral = listOf("Bevi, amore.", "Water before the espresso.", "Nonna is watching."),
            worried = listOf("You're behind, amore mio.", "This is not how we stay strong.", "Drink before I worry more."),
            disappointed = listOf("I said drink. You didn't.", "Two reminders, zero respect.", "Nonna remembers this."),
            proud = listOf("Perfetto! Goal done.", "That's my grandchild.", "Hydrated and loved."),
        ),
        persona(
            id = "asian_mom",
            displayName = "Asian Mom",
            emoji = "🥢",
            isPro = true,
            neutral = listOf("Drink water. Not later — now.", "Hydration is discipline.", "Small sips, every hour."),
            worried = listOf("Progress is low. Fix it.", "I can see you skipped.", "Don't make excuses."),
            disappointed = listOf("I reminded you. You ignored me.", "This is disappointing.", "Two chances. Still dry."),
            proud = listOf("Good. Goal complete.", "This is what effort looks like.", "I'm proud. Keep going."),
        ),
        persona(
            id = "gym_bro",
            displayName = "Gym Bro",
            emoji = "💪",
            isPro = true,
            neutral = listOf("Sip up, champ.", "Gains need water.", "Hydrate or die-drate."),
            worried = listOf("You're lagging, bro.", "Bottle's judging you.", "Catch up before PR time."),
            disappointed = listOf("Skipped my nudge? Weak.", "Two alerts. Zero reps.", "Don't ghost your gains."),
            proud = listOf("Crushed the goal!", "That's championship hydration.", "Bro... proud of you."),
        ),
        persona(
            id = "corporate_hr",
            displayName = "Corporate HR",
            emoji = "📎",
            isPro = true,
            neutral = listOf("Friendly reminder: hydrate.", "Please log your water intake.", "Wellness ping: drink up."),
            worried = listOf("You're below expected progress.", "Action required: drink water.", "Compliance risk: dehydration."),
            disappointed = listOf("Second notice ignored.", "Escalating: still no intake.", "This will go on your wellness file."),
            proud = listOf("Goal achieved. Nice work.", "You've met today's KPI.", "Recognized: hydration champion."),
        ),
    )

    private val byId = all.associateBy { it.id }

    fun get(id: String): Persona = byId[id] ?: all.first()

    val free: List<Persona> get() = all.filterNot { it.isPro }
    val pro: List<Persona> get() = all.filter { it.isPro }

    private fun persona(
        id: String,
        displayName: String,
        emoji: String,
        isPro: Boolean,
        neutral: List<String>,
        worried: List<String>,
        disappointed: List<String>,
        proud: List<String>,
    ): Persona {
        require(neutral.size == 3 && worried.size == 3 && disappointed.size == 3 && proud.size == 3)
        return Persona(
            id = id,
            displayName = displayName,
            emoji = emoji,
            isPro = isPro,
            lines = mapOf(
                Mood.NEUTRAL to neutral,
                Mood.WORRIED to worried,
                Mood.DISAPPOINTED to disappointed,
                Mood.PROUD to proud,
            ),
        )
    }
}
