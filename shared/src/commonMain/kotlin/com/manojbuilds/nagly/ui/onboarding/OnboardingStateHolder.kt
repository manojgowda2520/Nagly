package com.manojbuilds.nagly.ui.onboarding

import com.manojbuilds.nagly.billing.BillingRepository
import com.manojbuilds.nagly.data.DrinkLogRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.data.UnlockRepository
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.model.ActivityLevel
import com.manojbuilds.nagly.domain.model.DayPart
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.model.UserGoal
import com.manojbuilds.nagly.domain.recommendedDailyMl
import com.manojbuilds.nagly.notifications.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OnboardingStep {
    Weight,
    Activity,
    Hours,
    BuildingPlan,
    GoalReveal,
    Relationship,
    Variant,
    FirstGlass,
    Permission,
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Weight,
    val weightKg: String = "70",
    val activity: ActivityLevel = ActivityLevel.LIGHT,
    val dailyMl: Int = recommendedDailyMl(70, ActivityLevel.LIGHT),
    val personaId: String = "indian_mom",
    val selectedRelationshipId: String? = "mom",
    val wakeHour: Int = 7,
    val sleepHour: Int = 22,
    val firstGlassLogged: Boolean = false,
    val reactionLine: String? = null,
    val unlockedIds: Set<String> = emptySet(),
    val isPro: Boolean = false,
)

class OnboardingStateHolder(
    private val goalRepository: GoalRepository,
    unlockRepository: UnlockRepository,
    private val drinkLogRepository: DrinkLogRepository,
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
            val weight = kg.toIntOrNull() ?: 70
            val daily = recommendedDailyMl(weight, it.activity)
            it.copy(weightKg = kg, dailyMl = daily)
        }
    }

    fun setActivity(level: ActivityLevel) {
        draft.update {
            val weight = it.weightKg.toIntOrNull() ?: 70
            it.copy(activity = level, dailyMl = recommendedDailyMl(weight, level))
        }
    }

    fun selectRelationship(id: String) {
        draft.update { it.copy(selectedRelationshipId = id, step = OnboardingStep.Variant) }
    }

    fun selectPersona(id: String) {
        draft.update { it.copy(personaId = id) }
    }

    fun setWakeHour(hour: Int) = draft.update { it.copy(wakeHour = hour.coerceIn(0, 23)) }
    fun setSleepHour(hour: Int) = draft.update { it.copy(sleepHour = hour.coerceIn(0, 23)) }

    fun next() {
        draft.update {
            val next = when (it.step) {
                OnboardingStep.Weight -> OnboardingStep.Activity
                OnboardingStep.Activity -> OnboardingStep.Hours
                OnboardingStep.Hours -> OnboardingStep.BuildingPlan
                OnboardingStep.BuildingPlan -> it.step
                OnboardingStep.GoalReveal -> OnboardingStep.Relationship
                OnboardingStep.Relationship -> it.step
                OnboardingStep.Variant -> OnboardingStep.FirstGlass
                OnboardingStep.FirstGlass -> if (it.firstGlassLogged) OnboardingStep.Permission else it.step
                OnboardingStep.Permission -> it.step
            }
            it.copy(step = next)
        }
        if (draft.value.step == OnboardingStep.BuildingPlan) {
            startBuildingPlan()
        }
    }

    fun back() {
        draft.update {
            val prev = when (it.step) {
                OnboardingStep.Weight -> it.step
                OnboardingStep.Activity -> OnboardingStep.Weight
                OnboardingStep.Hours -> OnboardingStep.Activity
                OnboardingStep.BuildingPlan -> OnboardingStep.Hours
                OnboardingStep.GoalReveal -> OnboardingStep.Hours
                OnboardingStep.Relationship -> OnboardingStep.GoalReveal
                OnboardingStep.Variant -> OnboardingStep.Relationship
                OnboardingStep.FirstGlass -> OnboardingStep.Variant
                OnboardingStep.Permission -> OnboardingStep.FirstGlass
            }
            it.copy(step = prev)
        }
    }

    private fun startBuildingPlan() {
        scope.launch {
            delay(1800)
            draft.update { it.copy(step = OnboardingStep.GoalReveal) }
        }
    }

    fun logFirstGlass() {
        scope.launch {
            drinkLogRepository.add(250)
            val persona = PersonaCatalog.get(draft.value.personaId)
            val line = PersonaCatalog.linesFor(persona, Mood.PROUD, DayPart.ANYTIME).first()
            draft.update { it.copy(firstGlassLogged = true, reactionLine = line) }
        }
    }

    fun canSelect(persona: Persona, unlocked: Set<String>, isPro: Boolean): Boolean {
        return !PersonaCatalog.isPro(persona) || isPro || persona.relationshipId in unlocked
    }

    fun permissionLine(): String {
        val persona = PersonaCatalog.get(draft.value.personaId)
        return PersonaCatalog.linesFor(persona, Mood.NEUTRAL, DayPart.ANYTIME)
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
