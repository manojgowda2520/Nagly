package com.manojbuilds.nagly.domain

import com.manojbuilds.nagly.billing.BillingRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.data.UnlockRepository
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class UnlockExpiryWatcher(
    private val goalRepository: GoalRepository,
    unlockRepository: UnlockRepository,
    billingRepository: BillingRepository,
    private val clock: Clock = Clock.System,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _expiryMessage = MutableStateFlow<String?>(null)
    val expiryMessage: StateFlow<String?> = _expiryMessage.asStateFlow()

    init {
        scope.launch {
            combine(
                goalRepository.observeGoal(),
                unlockRepository.observeUnlockExpiries(),
                billingRepository.isPro,
            ) { goal, unlocks, isPro ->
                Triple(goal, unlocks, isPro)
            }.collect { (goal, unlocks, isPro) ->
                val (updated, message) = resolveExpiredSelection(
                    goal = goal,
                    activeUnlocks = unlocks,
                    isPro = isPro,
                    nowMs = clock.now().toEpochMilliseconds(),
                )
                if (message != null && updated.personaId != goal.personaId) {
                    goalRepository.save(updated)
                    _expiryMessage.value = message
                }
            }
        }
    }

    fun clearExpiryMessage() {
        _expiryMessage.value = null
    }
}
