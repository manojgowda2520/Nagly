package com.manojbuilds.nagly.ui.onboarding

import com.manojbuilds.nagly.billing.BillingRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.data.UnlockRepository
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.recommendedDailyMl
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.model.UserGoal
import com.manojbuilds.nagly.notifications.Notifier
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

enum class OnboardingStep {
    Goal,
    Persona,
    Hours,
    Permission,
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Goal,
    val weightKg: String = "70",
    val dailyMl: Int = recommendedDailyMl(70),
    val useManualMl: Boolean = false,
    val manualMl: String = "2000",
    val personaId: String = "indian_mom",
    val wakeHour: Int = 7,
    val sleepHour: Int = 22,
    val unlockedIds: Set<String> = emptySet(),
    val isPro: Boolean = false,
)

class OnboardingStateHolder(
    private val goalRepository: GoalRepository,
    unlockRepository: UnlockRepository,
    private val notifier: Notifier,
    billingRepository: BillingRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val draft = MutableStateFlow(OnboardingUiState())

    val uiState: StateFlow<OnboardingUiState> = combine(
        draft,
        unlockRepository.observeUnlocked(),
        billingRepository.isPro,
    ) { current, unlocked, isPro ->
        current.copy(unlockedIds = unlocked, isPro = isPro)
    }.stateIn(scope, SharingStarted.Eagerly, OnboardingUiState())

    fun setWeight(value: String) {
        draft.update {
            val kg = value.filter(Char::isDigit).take(3)
            val daily = kg.toIntOrNull()?.let(::recommendedDailyMl) ?: it.dailyMl
            it.copy(weightKg = kg, dailyMl = daily, useManualMl = false)
        }
    }

    fun setManualMl(value: String) {
        draft.update {
            val ml = value.filter(Char::isDigit).take(4)
            it.copy(
                manualMl = ml,
                dailyMl = ml.toIntOrNull()?.coerceIn(500, 6000) ?: it.dailyMl,
                useManualMl = true,
            )
        }
    }

    fun selectPersona(id: String) {
        draft.update { it.copy(personaId = id) }
    }

    fun setWakeHour(hour: Int) = draft.update { it.copy(wakeHour = hour.coerceIn(0, 23)) }
    fun setSleepHour(hour: Int) = draft.update { it.copy(sleepHour = hour.coerceIn(0, 23)) }

    fun next() {
        draft.update {
            val next = when (it.step) {
                OnboardingStep.Goal -> OnboardingStep.Persona
                OnboardingStep.Persona -> OnboardingStep.Hours
                OnboardingStep.Hours -> OnboardingStep.Permission
                OnboardingStep.Permission -> it.step
            }
            it.copy(step = next)
        }
    }

    fun back() {
        draft.update {
            val prev = when (it.step) {
                OnboardingStep.Goal -> it.step
                OnboardingStep.Persona -> OnboardingStep.Goal
                OnboardingStep.Hours -> OnboardingStep.Persona
                OnboardingStep.Permission -> OnboardingStep.Hours
            }
            it.copy(step = prev)
        }
    }

    fun canSelect(persona: Persona, unlocked: Set<String>, isPro: Boolean): Boolean {
        return !PersonaCatalog.isPro(persona) || isPro || persona.relationshipId in unlocked
    }

    fun permissionLine(): String {
        val persona = PersonaCatalog.get(draft.value.personaId)
        return PersonaCatalog.linesFor(persona, Mood.NEUTRAL, com.manojbuilds.nagly.domain.model.DayPart.ANYTIME)
            .firstOrNull()
            ?: "Let me nudge you when you forget to drink."
    }

    fun finish(onDone: () -> Unit) {
        scope.launch {
            notifier.requestPermission()
            val state = draft.value
            goalRepository.save(
                UserGoal(
                    dailyMl = state.dailyMl,
                    wakeHour = state.wakeHour,
                    sleepHour = state.sleepHour,
                    personaId = state.personaId,
                    onboarded = true,
                ),
            )
            onDone()
        }
    }

    fun savePersonaOnly(personaId: String, onDone: () -> Unit) {
        scope.launch {
            val existing = goalRepository.observeGoal().first()
            goalRepository.save(existing.copy(personaId = personaId))
            onDone()
        }
    }
}
