package com.manojbuilds.nagly.ui.today

import com.manojbuilds.nagly.data.DrinkLogRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.behindSeverity
import com.manojbuilds.nagly.domain.computeMood
import com.manojbuilds.nagly.domain.currentStreak
import com.manojbuilds.nagly.domain.dayPartFor
import com.manojbuilds.nagly.domain.expectedRatio
import com.manojbuilds.nagly.domain.pickLine
import com.manojbuilds.nagly.domain.model.DrinkLog
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.UserGoal
import com.manojbuilds.nagly.notifications.nextNudgeAtMs
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TodayStateHolder(
    private val drinkLogRepository: DrinkLogRepository,
    private val goalRepository: GoalRepository,
    private val clock: Clock = Clock.System,
    private val ignoredNudgeCountProvider: () -> Int = { 0 },
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var cachedMood: Mood? = null
    private var cachedLine: String? = null
    private var lastGoal: UserGoal? = null
    private val lineTick = MutableStateFlow(0)

    val uiState: StateFlow<TodayUiState> = combine(
        goalRepository.observeGoal(),
        drinkLogRepository.observeToday(),
        drinkLogRepository.observeRange(
            fromMs = clock.now().toEpochMilliseconds() - 14L * 24L * 60L * 60L * 1000L,
            toMs = clock.now().toEpochMilliseconds() + 1L,
        ),
        lineTick,
    ) { goal, todayLogs, recentLogs, _ ->
        buildState(goal, todayLogs, recentLogs)
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = TodayUiState(),
    )

    private fun buildState(
        goal: UserGoal,
        todayLogs: List<DrinkLog>,
        recentLogs: List<DrinkLog>,
    ): TodayUiState {
        lastGoal = goal
        val persona = PersonaCatalog.get(goal.personaId)
        val consumed = todayLogs.sumOf { it.amountMl }
        val progressRatio = if (goal.dailyMl <= 0) {
            0f
        } else {
            consumed.toFloat() / goal.dailyMl.toFloat()
        }
        val now = clock.now()
        val nowHour = now.toLocalDateTime(TimeZone.currentSystemDefault()).hour
        val expected = expectedRatio(nowHour, goal.wakeHour, goal.sleepHour)
        val mood = computeMood(
            progressRatio = progressRatio,
            expectedRatio = expected,
            ignoredNudgeCount = ignoredNudgeCountProvider(),
        )

        if (mood != cachedMood || cachedLine == null) {
            val dayPart = dayPartFor(nowHour, goal.wakeHour, goal.sleepHour)
            cachedLine = pickLine(persona, mood, dayPart = dayPart, previousLine = cachedLine)
            cachedMood = mood
        }

        val expectedMl = (expected * goal.dailyMl).toInt()
        val behind = (expectedMl - consumed).coerceAtLeast(0)
        val severity = behindSeverity(progressRatio, expected)
        val guilt = when (mood) {
            Mood.PROUD -> 0.12f
            Mood.NEUTRAL -> (0.25f + severity * 0.25f).coerceIn(0.2f, 0.5f)
            Mood.WORRIED -> (0.55f + severity * 0.3f).coerceIn(0.55f, 0.85f)
            Mood.DISAPPOINTED -> 1f
        }

        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
        val logsByDay = recentLogs.groupBy { it.localDate() }
            .mapValues { (_, logs) -> logs.sumOf { it.amountMl } }

        val recentCustom = todayLogs.asReversed()
            .firstOrNull { it.amountMl != 250 && it.amountMl != 500 }
            ?.amountMl
            ?: recentLogs.asReversed()
                .firstOrNull { it.amountMl != 250 && it.amountMl != 500 }
                ?.amountMl

        val nowMs = now.toEpochMilliseconds()
        val nextAt = nextNudgeAtMs(nowMs, goal, consumed)
        val nextLabel = if (nextAt == null) {
            if (consumed >= goal.dailyMl) "Goal met — no more nudges" else "No nudge scheduled"
        } else {
            formatCountdown(nextAt - nowMs)
        }

        return TodayUiState(
            personaName = persona.displayName,
            personaEmoji = persona.emoji,
            personaLine = cachedLine.orEmpty(),
            mood = mood,
            consumedMl = consumed,
            dailyMl = goal.dailyMl,
            behindMl = behind,
            streak = currentStreak(logsByDay, goal.dailyMl, today),
            progress = progressRatio.coerceIn(0f, 1.2f).coerceAtMost(1f),
            guiltProgress = guilt,
            hourOfDay = nowHour,
            canUndo = todayLogs.isNotEmpty(),
            isLoading = false,
            drinks = todayLogs.sortedByDescending { it.timestampMs },
            recentCustomMl = recentCustom,
            nextNudgeLabel = nextLabel,
        )
    }

    fun cycleLine() {
        val goal = lastGoal ?: return
        val persona = PersonaCatalog.get(goal.personaId)
        val mood = cachedMood ?: return
        val nowHour = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        val dayPart = dayPartFor(nowHour, goal.wakeHour, goal.sleepHour)
        cachedLine = pickLine(persona, mood, dayPart = dayPart, previousLine = cachedLine)
        lineTick.value = lineTick.value + 1
    }

    fun log(amountMl: Int) {
        scope.launch { drinkLogRepository.add(amountMl) }
    }

    fun undo() {
        scope.launch { drinkLogRepository.undoLast() }
    }

    fun undoEntry(id: Long) {
        scope.launch { drinkLogRepository.delete(id) }
    }

    private fun formatCountdown(remainingMs: Long): String {
        val total = remainingMs.coerceAtLeast(0L)
        val hours = total / (60L * 60L * 1000L)
        val minutes = (total % (60L * 60L * 1000L)) / (60L * 1000L)
        return if (hours > 0) "Next nudge in ${hours}h ${minutes}m" else "Next nudge in ${minutes}m"
    }

    private fun DrinkLog.localDate(): LocalDate {
        return Instant.fromEpochMilliseconds(timestampMs)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }
}
