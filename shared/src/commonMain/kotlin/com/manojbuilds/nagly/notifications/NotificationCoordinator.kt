package com.manojbuilds.nagly.notifications

import com.manojbuilds.nagly.data.DrinkLogRepository
import com.manojbuilds.nagly.data.GoalRepository
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Keeps local nudges in sync with goal + today's consumption.
 * Decision logic for notification actions lives here / commonMain.
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

    suspend fun logFromNotification(amountMl: Int) {
        ignoredNudgeStore.onLogged()
        drinkLogRepository.add(amountMl)
    }

    fun handleAction(actionId: String) {
        when {
            isSkipAction(actionId) -> skipFromNotification()
            else -> {
                val amount = amountForAction(actionId) ?: return
                scope.launch { logFromNotification(amount) }
            }
        }
    }

    fun skipFromNotification() {
        ignoredNudgeStore.onNudgeFired()
        scope.launch { rescheduleNow() }
    }

    fun onNudgeDelivered() {
        ignoredNudgeStore.onNudgeFired()
    }

    private suspend fun rescheduleNow() {
        val goal = goalRepository.observeGoal().first()
        val consumed = drinkLogRepository.observeToday().first().sumOf { it.amountMl }
        scheduler.reschedule(
            nowMs = clock.now().toEpochMilliseconds(),
            goal = goal,
            consumedMl = consumed,
        )
    }
}
