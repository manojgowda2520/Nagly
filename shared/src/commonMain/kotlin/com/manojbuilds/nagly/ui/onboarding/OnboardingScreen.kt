package com.manojbuilds.nagly.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onWeightChange: (String) -> Unit,
    onManualMlChange: (String) -> Unit,
    onSelectPersona: (String) -> Unit,
    onWakeChange: (Int) -> Unit,
    onSleepChange: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
    canSelectPersona: (Persona) -> Boolean,
    permissionLine: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        Text(
            text = when (state.step) {
                OnboardingStep.Goal -> "Your daily goal"
                OnboardingStep.Persona -> "Who's going to nag you?"
                OnboardingStep.Hours -> "When are you awake?"
                OnboardingStep.Permission -> "Let her reach you"
            },
            style = MaterialTheme.typography.headlineMedium,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 20.dp),
        ) {
            when (state.step) {
                OnboardingStep.Goal -> GoalStep(
                    state = state,
                    onWeightChange = onWeightChange,
                    onManualMlChange = onManualMlChange,
                )
                OnboardingStep.Persona -> PersonaStep(
                    selectedId = state.personaId,
                    onSelect = onSelectPersona,
                    canSelect = canSelectPersona,
                )
                OnboardingStep.Hours -> HoursStep(
                    wakeHour = state.wakeHour,
                    sleepHour = state.sleepHour,
                    onWakeChange = onWakeChange,
                    onSleepChange = onSleepChange,
                )
                OnboardingStep.Permission -> PermissionStep(
                    personaEmoji = PersonaCatalog.get(state.personaId).emoji,
                    personaName = PersonaCatalog.get(state.personaId).displayName,
                    line = permissionLine,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (state.step != OnboardingStep.Goal) {
                TextButton(onClick = onBack) { Text("Back") }
            } else {
                TextButton(onClick = {}, enabled = false) { Text("") }
            }
            Button(
                onClick = {
                    if (state.step == OnboardingStep.Permission) onFinish() else onNext()
                },
            ) {
                Text(if (state.step == OnboardingStep.Permission) "Allow & start" else "Continue")
            }
        }
    }
}

@Composable
private fun GoalStep(
    state: OnboardingUiState,
    onWeightChange: (String) -> Unit,
    onManualMlChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            "Enter your weight and we'll estimate, or set milliliters yourself.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = state.weightKg,
            onValueChange = onWeightChange,
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.manualMl,
            onValueChange = onManualMlChange,
            label = { Text("Or daily goal (ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Goal: ${state.dailyMl} ml / day",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun PersonaStep(
    selectedId: String,
    onSelect: (String) -> Unit,
    canSelect: (Persona) -> Boolean,
    onLockedClick: ((Persona) -> Unit)? = null,
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(PersonaCatalog.all, key = { it.id }) { persona ->
            val unlocked = canSelect(persona)
            val selected = persona.id == selectedId
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = RoundedCornerShape(18.dp),
                    )
                    .clickable {
                        if (unlocked) onSelect(persona.id) else onLockedClick?.invoke(persona)
                    }
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${persona.emoji}  ${persona.displayName}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f),
                    )
                    if (persona.isPro && !unlocked) {
                        Text(
                            "Pro",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                Text(
                    text = persona.lines.getValue(Mood.NEUTRAL).first(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
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
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("$personaEmoji $personaName", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "\"$line\"",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "She'll only nudge you while you're awake — and only when you're falling behind.",
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
