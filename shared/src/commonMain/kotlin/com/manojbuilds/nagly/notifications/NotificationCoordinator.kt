package com.manojbuilds.nagly.notifications

import com.manojbuilds.nagly.data.DrinkLogRepository
import com.manojbuilds.nagly.data.GoalRepository
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Keeps local nudges in sync with goal + today's consumption.
 */
class NotificationCoordinator(
    private val goalRepository: GoalRepository,
    private val drinkLogRepository: DrinkLogRepository,
    private val scheduler: NudgeScheduler,
    private val ignoredNudgeStore: IgnoredNudgeStore,
    private val clock: Clock = Clock.System,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        scope.launch {
            combine(
                goalRepository.observeGoal(),
                drinkLogRepository.observeToday().map { logs -> logs.sumOf { it.amountMl } },
            ) { goal, consumed -> goal to consumed }
                .distinctUntilChanged()
                .collect { (goal, consumed) ->
                    scheduler.reschedule(
                        nowMs = clock.now().toEpochMilliseconds(),
                        goal = goal,
                        consumedMl = consumed,
                    )
                }
        }
    }

    suspend fun logFromNotification(amountMl: Int = 250) {
        ignoredNudgeStore.onLogged()
        drinkLogRepository.add(amountMl)
    }

    fun onNudgeDelivered() {
        ignoredNudgeStore.onNudgeFired()
    }
}
