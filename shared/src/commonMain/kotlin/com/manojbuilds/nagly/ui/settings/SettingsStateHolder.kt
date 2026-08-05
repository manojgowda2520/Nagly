package com.manojbuilds.nagly.ui.settings

import com.manojbuilds.nagly.billing.BillingRepository
import com.manojbuilds.nagly.data.AppFlagRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.displayToMl
import com.manojbuilds.nagly.domain.mlToDisplay
import com.manojbuilds.nagly.domain.model.UserGoal
import com.manojbuilds.nagly.domain.model.VolumeUnit
import com.manojbuilds.nagly.notifications.Notifier
import com.manojbuilds.nagly.platform.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val dailyMl: Int = 2000,
    val dailyDisplay: Int = 2000,
    val volumeUnit: VolumeUnit = VolumeUnit.ML,
    val wakeHour: Int = 7,
    val sleepHour: Int = 22,
    val personaId: String = "indian_mom",
    val personaLabel: String = "",
    val notificationsEnabled: Boolean = true,
    val isPro: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val restoreMessage: String? = null,
    val permissionMessage: String? = null,
    val error: String? = null,
    val versionName: String = AppInfo.versionName,
)

class SettingsStateHolder(
    private val goalRepository: GoalRepository,
    private val appFlagRepository: AppFlagRepository,
    private val billingRepository: BillingRepository,
    private val notifier: Notifier,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val restoreMessage = MutableStateFlow<String?>(null)
    private val permissionMessage = MutableStateFlow<String?>(null)
    private val isSaving = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            goalRepository.observeGoal(),
            appFlagRepository.observeBoolean(AppFlagRepository.KEY_NOTIFICATIONS_ENABLED, default = true),
            billingRepository.isPro,
        ) { goal, notificationsEnabled, isPro ->
            Triple(goal, notificationsEnabled, isPro)
        },
        restoreMessage,
        permissionMessage,
        isSaving,
    ) { triple, restore, permission, saving ->
        val (goal, notificationsEnabled, isPro) = triple
        val persona = PersonaCatalog.get(goal.personaId)
        SettingsUiState(
            dailyMl = goal.dailyMl,
            dailyDisplay = mlToDisplay(goal.dailyMl, goal.volumeUnit),
            volumeUnit = goal.volumeUnit,
            wakeHour = goal.wakeHour,
            sleepHour = goal.sleepHour,
            personaId = goal.personaId,
            personaLabel = "${persona.emoji} ${persona.displayName}",
            notificationsEnabled = notificationsEnabled,
            isPro = isPro,
            isLoading = false,
            isSaving = saving,
            restoreMessage = restore,
            permissionMessage = permission,
            versionName = AppInfo.versionName,
        )
    }.stateIn(scope, SharingStarted.Eagerly, SettingsUiState())

    fun setDailyDisplay(value: Int) {
        scope.launch {
            isSaving.value = true
            val goal = goalRepository.observeGoal().first()
            val ml = displayToMl(value.coerceIn(1, 9999), goal.volumeUnit)
            goalRepository.save(goal.copy(dailyMl = ml.coerceIn(500, 6000)))
            isSaving.value = false
        }
    }

    fun setVolumeUnit(unit: VolumeUnit) {
        scope.launch {
            isSaving.value = true
            val goal = goalRepository.observeGoal().first()
            goalRepository.save(goal.copy(volumeUnit = unit))
            isSaving.value = false
        }
    }

    fun setWakeHour(hour: Int) = updateGoal { it.copy(wakeHour = hour.coerceIn(0, 23)) }
    fun setSleepHour(hour: Int) = updateGoal { it.copy(sleepHour = hour.coerceIn(0, 23)) }

    fun setNotificationsEnabled(enabled: Boolean) {
        scope.launch {
            appFlagRepository.setBoolean(AppFlagRepository.KEY_NOTIFICATIONS_ENABLED, enabled)
        }
    }

    fun restorePurchases() {
        scope.launch {
            isSaving.value = true
            restoreMessage.value = null
            val result = billingRepository.restore()
            restoreMessage.value = when {
                result.isSuccess && billingRepository.isPro.value -> "Pro restored!"
                result.isSuccess -> "No active subscription found."
                else -> "Restore failed. Try again."
            }
            isSaving.value = false
        }
    }

    fun requestNotificationPermission() {
        scope.launch {
            val granted = notifier.requestPermission()
            permissionMessage.value = if (granted) {
                "Notifications enabled."
            } else {
                "Permission denied — enable in system settings."
            }
        }
    }

    fun clearMessages() {
        restoreMessage.value = null
        permissionMessage.value = null
    }

    private fun updateGoal(transform: (UserGoal) -> UserGoal) {
        scope.launch {
            isSaving.value = true
            val goal = goalRepository.observeGoal().first()
            goalRepository.save(transform(goal))
            isSaving.value = false
        }
    }
}
