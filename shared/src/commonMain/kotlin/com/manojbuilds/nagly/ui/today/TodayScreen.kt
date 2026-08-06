package com.manojbuilds.nagly.ui.today

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.platform.LocalHaptics
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglyMotion
import com.manojbuilds.nagly.ui.designsystem.NaglyShapes
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import com.manojbuilds.nagly.ui.designsystem.components.MoodRing
import com.manojbuilds.nagly.ui.designsystem.components.PillButton
import com.manojbuilds.nagly.ui.designsystem.components.PillButtonVariant
import com.manojbuilds.nagly.ui.designsystem.components.RelationshipMeterChip
import com.manojbuilds.nagly.ui.designsystem.components.SpeechBubbleAnimated
import com.manojbuilds.nagly.ui.designsystem.components.WaveBottle
import com.manojbuilds.nagly.ui.designsystem.dayPartBrush
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TodayScreen(
    state: TodayUiState,
    onLog: (Int) -> Unit,
    onUndo: () -> Unit,
    onUndoEntry: (Long) -> Unit = {},
    onCycleLine: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenPersonas: () -> Unit = {},
) {
    val colors = LocalNaglyColors.current
    val haptics = LocalHaptics.current
    var showCustomDialog by remember { mutableStateOf(false) }
    var logPulse by remember { mutableStateOf(false) }
    var showDroplet by remember { mutableStateOf(false) }
    var prevConsumed by remember { mutableStateOf(state.consumedMl) }
    fun logWithFeedback(amount: Int) {
        haptics.lightTap()
        logPulse = !logPulse
        showDroplet = true
        onLog(amount)
    }
    LaunchedEffect(state.consumedMl) {
        if (state.consumedMl > prevConsumed) {
            if (state.progress >= 1f) haptics.success()
        }
        prevConsumed = state.consumedMl
    }
    LaunchedEffect(showDroplet) {
        if (showDroplet) {
            kotlinx.coroutines.delay(800)
            showDroplet = false
        }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = NaglyMotion.DurationSlow, easing = FastOutSlowInEasing),
        label = "fill",
    )
    val guiltColor = guiltGaugeColor(state.mood, state.guiltProgress, colors.primary, colors.warning, colors.danger)
    val dayBrush = dayPartBrush(hour = state.hourOfDay, colors = colors, intensity = 1f)

    val breath = rememberInfiniteTransition(label = "breath")
    val bob by breath.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dayBrush)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = NaglySpacing.sm, vertical = NaglySpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showDroplet) {
            FloatingDroplet(modifier = Modifier.padding(bottom = NaglySpacing.xxs))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onOpenPersonas) {
                Text("${state.personaEmoji} ${state.personaName}")
            }
            if (state.streak > 0) {
                Text(
                    text = "🔥 ${state.streak}-day",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.accent,
                    modifier = Modifier
                        .background(colors.accent.copy(alpha = 0.15f), NaglyShapes.pill)
                        .padding(horizontal = NaglySpacing.xs, vertical = NaglySpacing.xxs),
                )
            }
        }

        RelationshipMeterChip(
            level = state.relationshipLevel,
            progressToNext = state.relationshipProgress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = NaglySpacing.xs),
        )

        // Character zone ~35%
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = NaglySpacing.xs),
        ) {
            MoodRing(
                progress = state.guiltProgress,
                color = guiltColor,
                ringSize = 168.dp,
                strokeWidth = 10.dp,
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = bob.dp),
            ) {
                Text(text = expressiveFace(state.mood, state.personaEmoji), fontSize = 64.sp)
                Text(
                    text = moodCaption(state.mood),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary,
                )
            }
        }

        SpeechBubbleAnimated(
            text = state.personaLine.ifBlank { "Loading your nag..." },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NaglySpacing.xs)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCycleLine,
                ),
            backgroundColor = colors.card.copy(alpha = 0.92f),
            textColor = colors.textPrimary,
            textStyle = MaterialTheme.typography.titleLarge,
            tailAtStart = false,
        )
        Text(
            "Tap for more",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = NaglySpacing.xxs, bottom = NaglySpacing.sm),
        )

        // Bottle with animated wave fill
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp, 180.dp)) {
            WaveBottle(
                progress = animatedProgress,
                expectedProgress = state.expectedProgress,
                waterColor = colors.primary,
                modifier = Modifier.fillMaxSize(),
                onLogPulse = logPulse,
                goalReached = state.progress >= 1f,
            )
        }

        Text(
            text = formatLiters(state.consumedMl) + " / " + formatLiters(state.dailyMl),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = NaglySpacing.xs),
        )
        Text(
            text = if (state.behindMl > 0) {
                "behind by ${state.behindMl} ml"
            } else if (state.progress >= 1f) {
                "Goal crushed — proud of you"
            } else {
                "on track"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = if (state.behindMl > 0) colors.warning else colors.success,
        )
        Text(
            text = state.nextNudgeLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.padding(bottom = NaglySpacing.sm),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NaglySpacing.xs),
        ) {
            PillButton(
                onClick = { logWithFeedback(250) },
                modifier = Modifier.weight(1f),
                variant = PillButtonVariant.Primary,
            ) { Text("+250", maxLines = 1, softWrap = false) }
            PillButton(
                onClick = { logWithFeedback(500) },
                modifier = Modifier.weight(1f),
                variant = PillButtonVariant.Accent,
            ) { Text("+500", maxLines = 1, softWrap = false) }
            val custom = state.recentCustomMl
            if (custom != null) {
                PillButton(
                    onClick = { logWithFeedback(custom) },
                    modifier = Modifier.weight(1f),
                    variant = PillButtonVariant.Outlined,
                ) { Text("+$custom", maxLines = 1, softWrap = false) }
            } else {
                PillButton(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier.weight(1f),
                    variant = PillButtonVariant.Outlined,
                ) { Text("Custom", maxLines = 1, softWrap = false) }
            }
        }
        if (state.recentCustomMl != null) {
            TextButton(onClick = { showCustomDialog = true }) {
                Text("Other amount")
            }
        }

        if (state.drinks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(NaglySpacing.sm))
            Text(
                "Today's sips",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
            )
            state.drinks.take(6).forEach { drink ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = NaglySpacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        formatTime(drink.timestampMs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(0.3f),
                    )
                    Text(
                        "${drink.amountMl} ml",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(0.4f),
                    )
                    TextButton(onClick = { onUndoEntry(drink.id) }) {
                        Text("Undo")
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        var text by remember { mutableStateOf("300") }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Custom sip") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter(Char::isDigit).take(4) },
                    label = { Text("Amount (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = text.toIntOrNull()
                        if (amount != null && amount > 0) {
                            showCustomDialog = false
                            logWithFeedback(amount)
                        }
                    },
                ) { Text("Log it") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("Cancel") }
            },
            shape = NaglyShapes.card,
        )
    }
}

private fun guiltGaugeColor(
    mood: Mood,
    guilt: Float,
    calm: Color,
    warning: Color,
    danger: Color,
): Color {
    return when (mood) {
        Mood.PROUD, Mood.NEUTRAL -> lerp(calm, warning, (guilt * 1.2f).coerceIn(0f, 1f))
        Mood.WORRIED -> lerp(warning, danger, ((guilt - 0.5f) * 2f).coerceIn(0f, 1f))
        Mood.DISAPPOINTED -> danger
    }
}

private fun expressiveFace(mood: Mood, personaEmoji: String): String = personaEmoji

private fun moodCaption(mood: Mood): String = when (mood) {
    Mood.NEUTRAL -> "Keeping an eye on you"
    Mood.WORRIED -> "Getting worried"
    Mood.DISAPPOINTED -> "Disappointed"
    Mood.PROUD -> "So proud"
}

private fun formatLiters(ml: Int): String {
    if (ml % 1000 == 0) return "${ml / 1000}L"
    val tenths = (ml + 50) / 100
    val whole = tenths / 10
    val frac = tenths % 10
    return "${whole}.${frac}L"
}

private fun formatTime(timestampMs: Long): String {
    val local = Instant.fromEpochMilliseconds(timestampMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return local.hour.toString().padStart(2, '0') + ":" + local.minute.toString().padStart(2, '0')
}

@Composable
private fun FloatingDroplet(modifier: Modifier = Modifier) {
    val offsetY = remember { androidx.compose.animation.core.Animatable(0f) }
    val alpha = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(Unit) {
        offsetY.animateTo(-48f, tween(700, easing = FastOutSlowInEasing))
        alpha.animateTo(0f, tween(300))
    }
    Text(
        text = "💧",
        fontSize = 28.sp,
        modifier = modifier
            .offset(y = offsetY.value.dp)
            .alpha(alpha.value),
    )
}
