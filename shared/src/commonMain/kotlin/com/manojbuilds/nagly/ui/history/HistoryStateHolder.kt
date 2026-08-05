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

data class DayBar(
    val date: LocalDate,
    val label: String,
    val totalMl: Int,
)

data class HistoryUiState(
    val days: List<DayBar> = emptyList(),
    val dailyMl: Int = 2000,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val emptyLine: String = "",
    val personaName: String = "",
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
        drinkLogRepository.observeRange(fromMs = rangeStartMs(), toMs = rangeEndMs()),
    ) { goal, logs ->
        val today = clock.now().toLocalDateTime(timeZone).date
        val days = (0..6).map { offset ->
            val date = today.minus(6 - offset, DateTimeUnit.DAY)
            val total = logs
                .filter {
                    Instant.fromEpochMilliseconds(it.timestampMs)
                        .toLocalDateTime(timeZone).date == date
                }
                .sumOf { it.amountMl }
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
            dailyMl = goal.dailyMl,
            currentStreak = currentStreak(logsByDay, goal.dailyMl),
            bestStreak = bestStreak(logsByDay, goal.dailyMl),
            emptyLine = persona.lines.getValue(Mood.WORRIED).first(),
            personaName = persona.displayName,
        )
    }.stateIn(scope, SharingStarted.Eagerly, HistoryUiState())

    private fun rangeStartMs(): Long {
        val today = clock.now().toLocalDateTime(timeZone).date
        return today.minus(6, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds()
    }

    private fun rangeEndMs(): Long {
        val today = clock.now().toLocalDateTime(timeZone).date
        return today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds()
    }
}
