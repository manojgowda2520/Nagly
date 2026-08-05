package com.manojbuilds.nagly.notifications

import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona

object NudgeActionIds {
    const val ADD_250 = "ADD_250"
    const val ADD_500 = "ADD_500"
    const val SKIP = "SKIP"
}

data class NudgeActions(
    val mood: Mood,
    val skipLabel: String,
    val add250Label: String = "+250 ml",
    val add500Label: String = "+500 ml",
) {
    /** iOS category identifier — titles are fixed per mood on that platform. */
    val iosCategoryId: String get() = iosCategoryIdFor(mood)
}

fun buildNudgeActions(persona: Persona, mood: Mood): NudgeActions {
    val skip = persona.skipLabels[mood]?.randomOrNull()
        ?: persona.skipLabels[Mood.NEUTRAL]?.firstOrNull()
        ?: "Skip"
    return NudgeActions(mood = mood, skipLabel = skip)
}

fun iosCategoryIdFor(mood: Mood): String = "WATER_NUDGE_${mood.name}"

/** Fixed skip titles for iOS categories (action titles bind to category). */
fun iosSkipTitle(mood: Mood): String = when (mood) {
    Mood.NEUTRAL -> "Later"
    Mood.WORRIED -> "Busy"
    Mood.DISAPPOINTED -> "Skip"
    Mood.PROUD -> "Nice"
}

fun amountForAction(actionId: String): Int? = when (actionId) {
    NudgeActionIds.ADD_250 -> 250
    NudgeActionIds.ADD_500 -> 500
    else -> null
}

fun isSkipAction(actionId: String): Boolean = actionId == NudgeActionIds.SKIP
