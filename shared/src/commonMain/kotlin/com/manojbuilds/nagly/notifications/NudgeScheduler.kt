package com.manojbuilds.nagly.notifications

import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.computeMood
import com.manojbuilds.nagly.domain.dayPartFor
import com.manojbuilds.nagly.domain.expectedRatio
import com.manojbuilds.nagly.domain.pickLine
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.UserGoal
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

private const val MIN_INTERVAL_MS = 45L * 60L * 1000L
private const val MAX_NUDGES = 8
private const val MS_PER_HOUR = 60L * 60L * 1000L

fun nextNudgeTimes(
    nowMs: Long,
    goal: UserGoal,
    consumedMl: Int,
): List<Long> {
    val remainingMl = (goal.dailyMl - consumedMl).coerceAtLeast(0)
    if (remainingMl == 0) return emptyList()

    val wakeMs = hourOnSameDayMs(nowMs, goal.wakeHour)
    val sleepMs = sleepBoundaryMs(nowMs, goal.wakeHour, goal.sleepHour)
    val windowStart = maxOf(nowMs + MIN_INTERVAL_MS, wakeMs)
    if (windowStart >= sleepMs) return emptyList()

    val windowMs = sleepMs - windowStart
    val sipsLeft = (remainingMl / 250.0).coerceAtLeast(1.0)
    val intervalMs = (windowMs / sipsLeft).toLong().coerceAtLeast(MIN_INTERVAL_MS)

    val times = mutableListOf<Long>()
    var cursor = windowStart
    while (cursor < sleepMs && times.size < MAX_NUDGES) {
        times += cursor
        cursor += intervalMs
    }
    return times
}

fun projectedMood(
    atMs: Long,
    goal: UserGoal,
    consumedMl: Int,
    ignoredNudgeCount: Int,
): Mood {
    val hour = Instant.fromEpochMilliseconds(atMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .hour
    val progress = if (goal.dailyMl <= 0) 0f else consumedMl.toFloat() / goal.dailyMl
    val expected = expectedRatio(hour, goal.wakeHour, goal.sleepHour)
    return computeMood(progress, expected, ignoredNudgeCount)
}

fun nudgeBody(
    atMs: Long,
    goal: UserGoal,
    consumedMl: Int,
    ignoredNudgeCount: Int,
    previousLine: String?,
): String {
    val mood = projectedMood(atMs, goal, consumedMl, ignoredNudgeCount)
    val persona = PersonaCatalog.get(goal.personaId)
    val hour = Instant.fromEpochMilliseconds(atMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .hour
    val dayPart = dayPartFor(hour, goal.wakeHour, goal.sleepHour)
    return pickLine(persona, mood, dayPart = dayPart, previousLine = previousLine)
}

class NudgeScheduler(
    private val notifier: Notifier,
    private val ignoredNudgeStore: IgnoredNudgeStore,
) {
    private var previousLine: String? = null

    fun reschedule(nowMs: Long, goal: UserGoal, consumedMl: Int) {
        notifier.cancelAll()
        if (!goal.onboarded) return
        val times = nextNudgeTimes(nowMs, goal, consumedMl)
        val persona = PersonaCatalog.get(goal.personaId)
        times.forEachIndexed { index, atMs ->
            val body = nudgeBody(
                atMs = atMs,
                goal = goal,
                consumedMl = consumedMl,
                ignoredNudgeCount = ignoredNudgeStore.count,
                previousLine = previousLine,
            )
            previousLine = body
            notifier.schedule(
                id = index + 1,
                atEpochMs = atMs,
                title = "${persona.emoji} ${persona.displayName}",
                body = body,
            )
        }
    }
}

class IgnoredNudgeStore {
    var count: Int = 0
        private set

    fun onNudgeFired() {
        count += 1
    }

    fun onLogged() {
        count = 0
    }
}

internal fun hourOnSameDayMs(nowMs: Long, hour: Int): Long {
    val tz = TimeZone.currentSystemDefault()
    val local = Instant.fromEpochMilliseconds(nowMs).toLocalDateTime(tz)
    val target = LocalDateTime(
        year = local.year,
        month = local.month,
        day = local.day,
        hour = hour,
        minute = 0,
        second = 0,
        nanosecond = 0,
    )
    return target.toInstant(tz).toEpochMilliseconds()
}

internal fun sleepBoundaryMs(nowMs: Long, wakeHour: Int, sleepHour: Int): Long {
    val wakeMs = hourOnSameDayMs(nowMs, wakeHour)
    val sleepMs = hourOnSameDayMs(nowMs, sleepHour)
    return if (sleepHour > wakeHour) {
        sleepMs
    } else if (nowMs >= wakeMs) {
        sleepMs + 24L * MS_PER_HOUR
    } else {
        sleepMs
    }
}
