package com.manojbuilds.nagly.push

import com.manojbuilds.nagly.billing.BillingRepository
import com.manojbuilds.nagly.data.DrinkLogRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.domain.currentStreak
import com.manojbuilds.nagly.domain.model.DrinkLog
import com.manojbuilds.nagly.domain.model.UserGoal
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class PushTags(
    val personaId: String,
    val currentStreak: Int,
    val dailyGoalMl: Int,
    val lastLogDaysAgo: Int,
    val isPro: Boolean,
)

fun computePushTags(
    goal: UserGoal,
    recentLogs: List<DrinkLog>,
    isPro: Boolean,
    nowMs: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): PushTags {
    val logsByDay = recentLogs.groupBy { log ->
        Instant.fromEpochMilliseconds(log.timestampMs)
            .toLocalDateTime(timeZone)
            .date
    }.mapValues { (_, logs) -> logs.sumOf { it.amountMl } }

    val today = Instant.fromEpochMilliseconds(nowMs).toLocalDateTime(timeZone).date
    val lastLogMs = recentLogs.maxOfOrNull { it.timestampMs }
    val lastLogDaysAgo = if (lastLogMs == null) {
        999
    } else {
        val lastDay = Instant.fromEpochMilliseconds(lastLogMs).toLocalDateTime(timeZone).date
        (today.toEpochDays() - lastDay.toEpochDays()).toInt().coerceAtLeast(0)
    }

    return PushTags(
        personaId = goal.personaId,
        currentStreak = currentStreak(logsByDay, goal.dailyMl, today),
        dailyGoalMl = goal.dailyMl,
        lastLogDaysAgo = lastLogDaysAgo,
        isPro = isPro,
    )
}

fun PushClient.applyTags(tags: PushTags) {
    setTag("persona_id", tags.personaId)
    setTag("current_streak", tags.currentStreak.toString())
    setTag("daily_goal_ml", tags.dailyGoalMl.toString())
    setTag("last_log_days_ago", tags.lastLogDaysAgo.toString())
    setTag("is_pro", tags.isPro.toString())
}

class PushTagSync(
    private val pushClient: PushClient,
    private val goalRepository: GoalRepository,
    private val drinkLogRepository: DrinkLogRepository,
    private val billingRepository: BillingRepository,
    private val clock: Clock = Clock.System,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        pushClient.initialize()
        scope.launch {
            combine(
                goalRepository.observeGoal(),
                drinkLogRepository.observeRange(
                    fromMs = clock.now().toEpochMilliseconds() - 30L * 24L * 60L * 60L * 1000L,
                    toMs = clock.now().toEpochMilliseconds() + 1L,
                ),
                billingRepository.isPro,
            ) { goal, logs, isPro ->
                computePushTags(
                    goal = goal,
                    recentLogs = logs,
                    isPro = isPro,
                    nowMs = clock.now().toEpochMilliseconds(),
                )
            }
                .distinctUntilChanged()
                .collect { tags -> pushClient.applyTags(tags) }
        }
    }
}
