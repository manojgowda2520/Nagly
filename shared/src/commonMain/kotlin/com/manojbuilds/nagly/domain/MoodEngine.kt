package com.manojbuilds.nagly.domain

import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import kotlinx.datetime.LocalDate
import kotlin.math.max

fun computeMood(
    progressRatio: Float,
    expectedRatio: Float,
    ignoredNudgeCount: Int,
): Mood {
    return when {
        progressRatio >= 1f -> Mood.PROUD
        ignoredNudgeCount >= 2 -> Mood.DISAPPOINTED
        progressRatio < expectedRatio - 0.15f -> Mood.WORRIED
        else -> Mood.NEUTRAL
    }
}

/**
 * Fraction of the waking day elapsed. 0 before wake, 1 after sleep.
 * Handles schedules that wrap midnight (sleepHour < wakeHour).
 */
fun expectedRatio(nowHour: Int, wakeHour: Int, sleepHour: Int): Float {
    require(nowHour in 0..23)
    require(wakeHour in 0..23)
    require(sleepHour in 0..23)

    if (wakeHour == sleepHour) return 1f

    val wakingHours = if (sleepHour > wakeHour) {
        sleepHour - wakeHour
    } else {
        (24 - wakeHour) + sleepHour
    }

    val elapsed = when {
        sleepHour > wakeHour -> {
            when {
                nowHour < wakeHour -> 0
                nowHour >= sleepHour -> wakingHours
                else -> nowHour - wakeHour
            }
        }
        else -> {
            // Overnight schedule, e.g. wake 22 sleep 6
            when {
                nowHour >= wakeHour -> nowHour - wakeHour
                nowHour < sleepHour -> (24 - wakeHour) + nowHour
                else -> wakingHours
            }
        }
    }

    return (elapsed.toFloat() / wakingHours.toFloat()).coerceIn(0f, 1f)
}

fun recommendedDailyMl(weightKg: Int): Int {
    return (weightKg * 35).coerceIn(1500, 4000)
}

/**
 * Consecutive completed days ending on [today] or yesterday.
 * A run that ended earlier is not a current streak (returns 0).
 * Today incomplete does not break a prior streak ending yesterday.
 */
fun currentStreak(
    logsByDay: Map<LocalDate, Int>,
    dailyMl: Int,
    today: LocalDate,
): Int {
    if (logsByDay.isEmpty() || dailyMl <= 0) return 0

    val completedDays = logsByDay
        .filterValues { it >= dailyMl }
        .keys
        .sorted()
    if (completedDays.isEmpty()) return 0

    val runEnd = completedDays.last()
    val yesterday = today.minusDays(1)
    if (runEnd != today && runEnd != yesterday) return 0

    var streak = 1
    for (i in completedDays.lastIndex downTo 1) {
        val current = completedDays[i]
        val previous = completedDays[i - 1]
        if (previous == current.minusDays(1)) {
            streak++
        } else {
            break
        }
    }
    return streak
}

fun bestStreak(logsByDay: Map<LocalDate, Int>, dailyMl: Int): Int {
    if (logsByDay.isEmpty() || dailyMl <= 0) return 0
    val completedDays = logsByDay
        .filterValues { it >= dailyMl }
        .keys
        .sorted()
    if (completedDays.isEmpty()) return 0

    var best = 1
    var run = 1
    for (i in 1..completedDays.lastIndex) {
        if (completedDays[i - 1] == completedDays[i].minusDays(1)) {
            run++
            best = max(best, run)
        } else {
            run = 1
        }
    }
    return best
}

fun pickLine(persona: Persona, mood: Mood, previousLine: String?): String {
    val lines = persona.lines[mood].orEmpty()
    require(lines.isNotEmpty()) { "Persona ${persona.id} has no lines for $mood" }
    if (lines.size == 1) return lines.first()
    val candidates = if (previousLine == null) lines else lines.filter { it != previousLine }
    return candidates.random()
}

private fun LocalDate.minusDays(days: Int): LocalDate {
    return this.toEpochDays().let { epoch ->
        LocalDate.fromEpochDays(epoch - days)
    }
}
