package com.manojbuilds.nagly.domain

/**
 * Bond depth derived from streak and recent consistency — no persistence.
 * Ordered from lowest to highest warmth.
 */
enum class RelationshipLevel(val label: String, val emoji: String) {
    STRANGER("Stranger", "👋"),
    FRIENDLY("Friendly", "🙂"),
    CLOSE("Close", "💛"),
    FAMILY("Family", "🏠"),
    SOUL_REMINDER("Soul Reminder", "✨"),
    ;

    companion object {
        val ordered = entries.toList()
    }
}

fun levelFromStreak(streak: Int): RelationshipLevel = when {
    streak >= 14 -> RelationshipLevel.SOUL_REMINDER
    streak >= 7 -> RelationshipLevel.FAMILY
    streak >= 3 -> RelationshipLevel.CLOSE
    streak >= 1 -> RelationshipLevel.FRIENDLY
    else -> RelationshipLevel.STRANGER
}

fun levelFromConsistency(daysMetGoalIn14: Int): RelationshipLevel = when {
    daysMetGoalIn14 >= 13 -> RelationshipLevel.SOUL_REMINDER
    daysMetGoalIn14 >= 10 -> RelationshipLevel.FAMILY
    daysMetGoalIn14 >= 6 -> RelationshipLevel.CLOSE
    daysMetGoalIn14 >= 3 -> RelationshipLevel.FRIENDLY
    else -> RelationshipLevel.STRANGER
}

/**
 * Combined bond level — takes the higher of streak-based and consistency-based levels.
 */
fun computeRelationshipLevel(
    currentStreak: Int,
    daysMetGoalIn14: Int,
): RelationshipLevel {
    val fromStreak = levelFromStreak(currentStreak)
    val fromConsistency = levelFromConsistency(daysMetGoalIn14)
    return if (fromStreak.ordinal >= fromConsistency.ordinal) fromStreak else fromConsistency
}

/**
 * Progress toward the next level, 0..1. Returns 1f when at max level.
 */
fun relationshipProgressToNext(
    level: RelationshipLevel,
    currentStreak: Int,
    daysMetGoalIn14: Int,
): Float {
    if (level == RelationshipLevel.SOUL_REMINDER) return 1f
    val next = RelationshipLevel.ordered[level.ordinal + 1]

    val streakTarget = streakThresholdFor(next)
    val consistencyTarget = consistencyThresholdFor(next)

    val streakProgress = if (streakTarget <= 0) 1f else (currentStreak.toFloat() / streakTarget).coerceIn(0f, 1f)
    val consistencyProgress = if (consistencyTarget <= 0) 1f else {
        (daysMetGoalIn14.toFloat() / consistencyTarget).coerceIn(0f, 1f)
    }
    return ((streakProgress + consistencyProgress) / 2f).coerceIn(0f, 1f)
}

private fun streakThresholdFor(level: RelationshipLevel): Int = when (level) {
    RelationshipLevel.FRIENDLY -> 1
    RelationshipLevel.CLOSE -> 3
    RelationshipLevel.FAMILY -> 7
    RelationshipLevel.SOUL_REMINDER -> 14
    RelationshipLevel.STRANGER -> 0
}

private fun consistencyThresholdFor(level: RelationshipLevel): Int = when (level) {
    RelationshipLevel.FRIENDLY -> 3
    RelationshipLevel.CLOSE -> 6
    RelationshipLevel.FAMILY -> 10
    RelationshipLevel.SOUL_REMINDER -> 13
    RelationshipLevel.STRANGER -> 0
}

fun countDaysMetGoalInWindow(
    logsByDay: Map<kotlinx.datetime.LocalDate, Int>,
    dailyMl: Int,
    windowDays: Int = 14,
    today: kotlinx.datetime.LocalDate,
): Int {
    return (0 until windowDays).count { offset ->
        val date = today.minusDays(offset)
        (logsByDay[date] ?: 0) >= dailyMl
    }
}

private fun kotlinx.datetime.LocalDate.minusDays(days: Int): kotlinx.datetime.LocalDate {
    return kotlinx.datetime.LocalDate.fromEpochDays(toEpochDays() - days)
}

// Warmer line preference at higher bond levels — uses PROUD mood when close+ if catalog has lines.
fun preferredMoodForBond(baseMood: com.manojbuilds.nagly.domain.model.Mood, level: RelationshipLevel): com.manojbuilds.nagly.domain.model.Mood {
    if (level.ordinal >= RelationshipLevel.CLOSE.ordinal &&
        baseMood == com.manojbuilds.nagly.domain.model.Mood.NEUTRAL
    ) {
        // Placeholder: catalog has no separate "warm neutral" pool; keep existing pickLine mood.
        // Could upgrade to PROUD when progress is high — left as no-op per spec.
    }
    return baseMood
}
