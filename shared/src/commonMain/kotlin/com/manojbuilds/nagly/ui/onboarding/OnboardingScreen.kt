package com.manojbuilds.nagly.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.model.ActivityLevel
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.model.Relationship
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import com.manojbuilds.nagly.ui.designsystem.components.NaglyCardOutlined
import com.manojbuilds.nagly.ui.designsystem.components.PillButton
import com.manojbuilds.nagly.ui.designsystem.components.SpeechBubbleSimple
import com.manojbuilds.nagly.ui.persona.RelationshipGrid
import com.manojbuilds.nagly.ui.persona.VariantList
import kotlin.math.roundToInt
import kotlin.time.Clock

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onWeightChange: (String) -> Unit,
    onActivityChange: (ActivityLevel) -> Unit,
    onSelectRelationship: (String) -> Unit,
    onSelectPersona: (String) -> Unit,
    onLockedRelationship: (Relationship) -> Unit,
    onLockedPersona: (Persona) -> Unit,
    onWakeChange: (Int) -> Unit,
    onSleepChange: (Int) -> Unit,
    onLogFirstGlass: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    canSelectPersona: (Persona) -> Boolean,
    permissionLine: String,
    unlockExpiries: Map<String, Long> = emptyMap(),
    nowMs: Long = Clock.System.now().toEpochMilliseconds(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(NaglySpacing.md),
    ) {
        OnboardingProgressDots(currentStep = state.step)

        Spacer(modifier = Modifier.height(NaglySpacing.sm))

        Text(
            text = stepTitle(state.step),
            style = MaterialTheme.typography.headlineMedium,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = NaglySpacing.md - 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(stepScrollModifier(state.step)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            when (state.step) {
                OnboardingStep.Weight -> WeightStep(
                    weightKg = state.weightKg,
                    onWeightChange = onWeightChange,
                )
                OnboardingStep.Activity -> ActivityStep(
                    selected = state.activity,
                    onSelect = onActivityChange,
                )
                OnboardingStep.Hours -> HoursStep(
                    wakeHour = state.wakeHour,
                    sleepHour = state.sleepHour,
                    onWakeChange = onWakeChange,
                    onSleepChange = onSleepChange,
                )
                OnboardingStep.BuildingPlan -> BuildingPlanStep()
                OnboardingStep.GoalReveal -> GoalRevealStep(dailyMl = state.dailyMl)
                OnboardingStep.Relationship -> RelationshipStep(
                    selectedRelationshipId = state.selectedRelationshipId,
                    unlockExpiries = unlockExpiries,
                    isPro = state.isPro,
                    onSelect = onSelectRelationship,
                    onLockedClick = onLockedRelationship,
                    nowMs = nowMs,
                )
                OnboardingStep.Variant -> {
                    val relationshipId = state.selectedRelationshipId ?: "mom"
                    VariantList(
                        relationshipId = relationshipId,
                        selectedId = state.personaId,
                        unlockExpiries = unlockExpiries,
                        isPro = state.isPro,
                        onSelect = onSelectPersona,
                        onLockedClick = onLockedPersona,
                        canSelect = canSelectPersona,
                        nowMs = nowMs,
                    )
                }
                OnboardingStep.FirstGlass -> FirstGlassStep(
                    personaId = state.personaId,
                    logged = state.firstGlassLogged,
                    reactionLine = state.reactionLine,
                    onLog = onLogFirstGlass,
                )
                OnboardingStep.Permission -> PermissionStep(
                    personaEmoji = PersonaCatalog.get(state.personaId).emoji,
                    personaName = PersonaCatalog.get(state.personaId).displayName,
                    line = permissionLine,
                )
            }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (state.step != OnboardingStep.Weight && state.step != OnboardingStep.BuildingPlan) {
                TextButton(onClick = onBack) { Text("Back") }
            } else {
                TextButton(onClick = {}, enabled = false) { Text("") }
            }
            when (state.step) {
                OnboardingStep.BuildingPlan -> Unit
                OnboardingStep.Relationship -> Unit
                OnboardingStep.Variant -> {
                    PillButton(onClick = onNext) { Text("Continue") }
                }
                OnboardingStep.FirstGlass -> {
                    if (state.firstGlassLogged) {
                        PillButton(onClick = onNext) { Text("Continue") }
                    }
                }
                OnboardingStep.Permission -> {
                    PillButton(onClick = onFinish) { Text("Allow & start") }
                }
                OnboardingStep.GoalReveal -> {
                    PillButton(onClick = onNext) { Text("Choose who nags you") }
                }
                else -> {
                    PillButton(onClick = onNext) { Text("Continue") }
                }
            }
        }
    }
}

private const val ONBOARDING_PROGRESS_STEPS = 8

private fun OnboardingStep.progressIndex(): Int = when (this) {
    OnboardingStep.Weight -> 0
    OnboardingStep.Activity -> 1
    OnboardingStep.Hours -> 2
    OnboardingStep.BuildingPlan -> 3
    OnboardingStep.GoalReveal -> 3
    OnboardingStep.Relationship -> 4
    OnboardingStep.Variant -> 5
    OnboardingStep.FirstGlass -> 6
    OnboardingStep.Permission -> 7
}

@Composable
private fun OnboardingProgressDots(currentStep: OnboardingStep) {
    val activeIndex = currentStep.progressIndex()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(ONBOARDING_PROGRESS_STEPS) { index ->
            val active = index == activeIndex
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (active) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        },
                    ),
            )
        }
    }
}

@Composable
private fun stepScrollModifier(step: OnboardingStep): Modifier = when (step) {
    OnboardingStep.Weight,
    OnboardingStep.FirstGlass,
    OnboardingStep.Relationship,
    OnboardingStep.Variant,
    -> Modifier.verticalScroll(rememberScrollState())

    else -> Modifier
}

private fun stepTitle(step: OnboardingStep): String = when (step) {
    OnboardingStep.Weight -> "What's your weight?"
    OnboardingStep.Activity -> "How active are you?"
    OnboardingStep.Hours -> "When are you awake?"
    OnboardingStep.BuildingPlan -> "Building your plan…"
    OnboardingStep.GoalReveal -> "Your daily goal"
    OnboardingStep.Relationship -> "Choose who nags you"
    OnboardingStep.Variant -> "Pick their vibe"
    OnboardingStep.FirstGlass -> "Log your first glass"
    OnboardingStep.Permission -> "Let them reach you"
}

@Composable
private fun WeightStep(weightKg: String, onWeightChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.sm),
    ) {
        Text(
            "We'll estimate your daily water goal from your weight.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = weightKg,
            onValueChange = onWeightChange,
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ActivityStep(selected: ActivityLevel, onSelect: (ActivityLevel) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.xs + 4.dp),
    ) {
        ActivityOption(
            label = "Mostly sitting",
            subtitle = "Desk job, light movement",
            selected = selected == ActivityLevel.SEDENTARY,
            onClick = { onSelect(ActivityLevel.SEDENTARY) },
        )
        ActivityOption(
            label = "Somewhat active",
            subtitle = "Walks, gym a few times a week",
            selected = selected == ActivityLevel.LIGHT,
            onClick = { onSelect(ActivityLevel.LIGHT) },
        )
        ActivityOption(
            label = "Very active",
            subtitle = "Daily workouts or physical job",
            selected = selected == ActivityLevel.ACTIVE,
            onClick = { onSelect(ActivityLevel.ACTIVE) },
        )
    }
}

@Composable
private fun ActivityOption(
    label: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NaglyCardOutlined(
        modifier = Modifier.fillMaxWidth(),
        selected = selected,
        onClick = onClick,
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = NaglySpacing.xxs),
        )
    }
}

@Composable
private fun BuildingPlanStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Text(
            "Crunching numbers…",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = NaglySpacing.sm),
        )
    }
}

@Composable
private fun GoalRevealStep(dailyMl: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.xs + 4.dp),
    ) {
        Text(
            "$dailyMl ml",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "per day — tuned to your weight and activity.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RelationshipStep(
    selectedRelationshipId: String?,
    unlockExpiries: Map<String, Long>,
    isPro: Boolean,
    onSelect: (String) -> Unit,
    onLockedClick: (Relationship) -> Unit,
    nowMs: Long,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Pick a relationship — then choose their exact vibe.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = NaglySpacing.sm),
        )
        RelationshipGrid(
            selectedRelationshipId = selectedRelationshipId,
            unlockExpiries = unlockExpiries,
            isPro = isPro,
            onSelect = onSelect,
            onLockedClick = onLockedClick,
            nowMs = nowMs,
        )
    }
}

@Composable
private fun FirstGlassStep(
    personaId: String,
    logged: Boolean,
    reactionLine: String?,
    onLog: () -> Unit,
) {
    val persona = PersonaCatalog.get(personaId)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.sm),
    ) {
        Text(
            "${persona.emoji} ${persona.displayName} wants to see you log one.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!logged) {
            PillButton(onClick = onLog, modifier = Modifier.fillMaxWidth()) {
                Text("Log 250 ml")
            }
        } else {
            SpeechBubbleSimple(
                text = "\"${reactionLine ?: "Nice start!"}\"",
                textStyle = MaterialTheme.typography.headlineMedium,
                textColor = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun HoursStep(
    wakeHour: Int,
    sleepHour: Int,
    onWakeChange: (Int) -> Unit,
    onSleepChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.md - 4.dp),
    ) {
        Text("Wake: ${formatHour(wakeHour)}", style = MaterialTheme.typography.titleLarge)
        Slider(
            value = wakeHour.toFloat(),
            onValueChange = { onWakeChange(it.roundToInt()) },
            valueRange = 0f..23f,
            steps = 22,
        )
        Text("Sleep: ${formatHour(sleepHour)}", style = MaterialTheme.typography.titleLarge)
        Slider(
            value = sleepHour.toFloat(),
            onValueChange = { onSleepChange(it.roundToInt()) },
            valueRange = 0f..23f,
            steps = 22,
        )
    }
}

@Composable
private fun PermissionStep(
    personaEmoji: String,
    personaName: String,
    line: String,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.sm),
    ) {
        Text("$personaEmoji $personaName", style = MaterialTheme.typography.headlineMedium)
        SpeechBubbleSimple(
            text = "\"$line\"",
            textStyle = MaterialTheme.typography.headlineMedium,
            textColor = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "They'll only nudge you while you're awake — and only when you're falling behind.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatHour(hour: Int): String {
    val h = ((hour + 11) % 12) + 1
    val suffix = if (hour < 12) "AM" else "PM"
    return "$h:00 $suffix"
}
