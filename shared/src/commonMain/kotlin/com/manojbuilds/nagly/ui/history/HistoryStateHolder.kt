package com.manojbuilds.nagly.ui.history

import com.manojbuilds.nagly.data.DrinkLogRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.bestStreak
import com.manojbuilds.nagly.domain.currentStreak
import com.manojbuilds.nagly.domain.model.Mood
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

enum class DayHeat {
    EMPTY,
    PARTIAL,
    MET,
}

data class DayBar(
    val date: LocalDate,
    val label: String,
    val totalMl: Int,
)

data class CalendarDay(
    val date: LocalDate?,
    val totalMl: Int,
    val heat: DayHeat,
)

data class HistoryUiState(
    val days: List<DayBar> = emptyList(),
    val calendarDays: List<CalendarDay> = emptyList(),
    val monthLabel: String = "",
    val dailyMl: Int = 2000,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val emptyLine: String = "",
    val personaName: String = "",
    val isLoading: Boolean = false,
)

class HistoryStateHolder(
    drinkLogRepository: DrinkLogRepository,
    goalRepository: GoalRepository,
    private val clock: Clock = Clock.System,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val timeZone = TimeZone.currentSystemDefault()

    val uiState: StateFlow<HistoryUiState> = combine(
        goalRepository.observeGoal(),
        drinkLogRepository.observeRange(fromMs = monthStartMs(), toMs = rangeEndMs()),
    ) { goal, logs ->
        val today = clock.now().toLocalDateTime(timeZone).date
        val monthStart = LocalDate(today.year, today.month, 1)
        val days = (0..6).map { offset ->
            val date = today.minus(6 - offset, DateTimeUnit.DAY)
            val total = totalForDate(logs, date)
            DayBar(
                date = date,
                label = date.dayOfWeek.name.take(2),
                totalMl = total,
            )
        }
        val logsByDay = logs.groupBy {
            Instant.fromEpochMilliseconds(it.timestampMs).toLocalDateTime(timeZone).date
        }.mapValues { (_, dayLogs) -> dayLogs.sumOf { it.amountMl } }

        val persona = PersonaCatalog.get(goal.personaId)
        HistoryUiState(
            days = days,
            calendarDays = buildCalendarDays(monthStart, today, logsByDay, goal.dailyMl),
            monthLabel = "${monthStart.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${monthStart.year}",
            dailyMl = goal.dailyMl,
            currentStreak = currentStreak(logsByDay, goal.dailyMl, today),
            bestStreak = bestStreak(logsByDay, goal.dailyMl),
            emptyLine = PersonaCatalog.linesFor(
                persona,
                Mood.WORRIED,
                com.manojbuilds.nagly.domain.model.DayPart.ANYTIME,
            ).ifEmpty {
                PersonaCatalog.linesFor(
                    persona,
                    Mood.WORRIED,
                    com.manojbuilds.nagly.domain.model.DayPart.AFTERNOON,
                )
            }.first(),
            personaName = persona.displayName,
        )
    }.stateIn(scope, SharingStarted.Eagerly, HistoryUiState())

    private fun buildCalendarDays(
        monthStart: LocalDate,
        today: LocalDate,
        logsByDay: Map<LocalDate, Int>,
        dailyMl: Int,
    ): List<CalendarDay> {
        val monthEnd = monthStart.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        val leading = monthStart.dayOfWeek.ordinal
        val cells = mutableListOf<CalendarDay>()
        repeat(leading) { cells += CalendarDay(date = null, totalMl = 0, heat = DayHeat.EMPTY) }
        var date = monthStart
        while (date <= monthEnd && date <= today) {
            val total = logsByDay[date] ?: 0
            val heat = when {
                total >= dailyMl -> DayHeat.MET
                total > 0 -> DayHeat.PARTIAL
                else -> DayHeat.EMPTY
            }
            cells += CalendarDay(date = date, totalMl = total, heat = heat)
            date = date.plus(1, DateTimeUnit.DAY)
        }
        return cells
    }

    private fun totalForDate(
        logs: List<com.manojbuilds.nagly.domain.model.DrinkLog>,
        date: LocalDate,
    ): Int = logs.filter {
        Instant.fromEpochMilliseconds(it.timestampMs).toLocalDateTime(timeZone).date == date
    }.sumOf { it.amountMl }

    private fun monthStartMs(): Long {
        val today = clock.now().toLocalDateTime(timeZone).date
        val monthStart = LocalDate(today.year, today.month, 1)
        return monthStart.atStartOfDayIn(timeZone).toEpochMilliseconds()
    }

    private fun rangeEndMs(): Long {
        val today = clock.now().toLocalDateTime(timeZone).date
        return today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds()
    }
}
