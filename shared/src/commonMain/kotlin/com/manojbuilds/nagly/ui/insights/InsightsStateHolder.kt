package com.manojbuilds.nagly.ui.insights

import com.manojbuilds.nagly.data.DrinkLogRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.bestStreak
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.notifications.IgnoredNudgeStore
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

data class InsightCard(
    val emoji: String,
    val title: String,
    val value: String,
    val subtitle: String = "",
)

data class InsightsUiState(
    val cards: List<InsightCard> = emptyList(),
    val personaEmoji: String = "",
    val personaName: String = "",
    val emptyLine: String = "",
    val hasData: Boolean = false,
)

class InsightsStateHolder(
    drinkLogRepository: DrinkLogRepository,
    goalRepository: GoalRepository,
    private val ignoredNudgeStore: IgnoredNudgeStore,
    private val clock: Clock = Clock.System,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val timeZone = TimeZone.currentSystemDefault()

    val uiState: StateFlow<InsightsUiState> = combine(
        goalRepository.observeGoal(),
        drinkLogRepository.observeRange(
            fromMs = weekStartMs(clock, timeZone),
            toMs = clock.now().toEpochMilliseconds() + 1L,
        ),
    ) { goal, logs ->
        val persona = PersonaCatalog.get(goal.personaId)
        val today = clock.now().toLocalDateTime(timeZone).date
        val days = (0..6).map { offset ->
            val date = today.minus(6 - offset, DateTimeUnit.DAY)
            val total = logs.filter {
                Instant.fromEpochMilliseconds(it.timestampMs).toLocalDateTime(timeZone).date == date
            }.sumOf { it.amountMl }
            date to total
        }
        val hasData = days.any { it.second > 0 }
        if (!hasData) {
            return@combine InsightsUiState(
                personaEmoji = persona.emoji,
                personaName = persona.displayName,
                emptyLine = PersonaCatalog.linesFor(
                    persona,
                    Mood.NEUTRAL,
                    com.manojbuilds.nagly.domain.model.DayPart.ANYTIME,
                ).firstOrNull() ?: "Log a few sips and I'll show you how you're doing.",
                hasData = false,
            )
        }

        val totals = days.map { it.second }
        val avg = totals.sum() / totals.size.coerceAtLeast(1)
        val best = days.maxBy { it.second }
        val bestLabel = "${best.first.month.name.take(3)} ${best.first.day}"

        val logsByDay = days.associate { it.first to it.second }
        val streak = bestStreak(logsByDay, goal.dailyMl)
        val proudDays = days.count { it.second >= goal.dailyMl }
        val moodTrend = when {
            proudDays >= 5 -> "On a roll — $proudDays goal days this week"
            proudDays >= 3 -> "Solid week — $proudDays days at goal"
            proudDays >= 1 -> "Building up — $proudDays goal day${if (proudDays == 1) "" else "s"}"
            else -> "Room to grow — keep sipping"
        }

        val ignoredHourLabel = if (ignoredNudgeStore.count >= 2) {
            val hour = clock.now().toLocalDateTime(timeZone).hour
            val band = when (hour) {
                in goal.wakeHour..11 -> "Morning"
                in 12..16 -> "Afternoon"
                else -> "Evening"
            }
            "$band nudges often skipped lately"
        } else {
            null
        }

        val cards = buildList {
            add(InsightCard("💧", "Weekly average", "${avg}ml", "per day"))
            add(InsightCard("🏆", "Best day", "${best.second}ml", bestLabel))
            add(InsightCard("🔥", "Longest streak", "$streak days", "all time best"))
            add(InsightCard("😊", "Mood trend", moodTrend))
            ignoredHourLabel?.let {
                add(InsightCard("⏰", "Nudge pattern", it))
            } ?: add(
                InsightCard(
                    "⏰",
                    "Nudge pattern",
                    "You're listening well",
                    "No ignored nudge streak",
                ),
            )
        }

        InsightsUiState(
            cards = cards,
            personaEmoji = persona.emoji,
            personaName = persona.displayName,
            hasData = true,
        )
    }.stateIn(scope, SharingStarted.Eagerly, InsightsUiState())
}

private fun weekStartMs(clock: Clock, timeZone: TimeZone): Long {
    val today = clock.now().toLocalDateTime(timeZone).date
    val weekStart = today.minus(6, DateTimeUnit.DAY)
    return weekStart.atStartOfDayIn(timeZone).toEpochMilliseconds()
}
