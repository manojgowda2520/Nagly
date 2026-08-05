package com.manojbuilds.nagly.ui.today

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglyShapes
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import com.manojbuilds.nagly.ui.designsystem.components.PillButton
import com.manojbuilds.nagly.ui.designsystem.components.PillButtonVariant
import com.manojbuilds.nagly.ui.designsystem.components.SpeechBubbleSimple
import com.manojbuilds.nagly.ui.designsystem.moodColor
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TodayScreen(
    state: TodayUiState,
    onLog: (Int) -> Unit,
    onUndo: () -> Unit,
    onUndoEntry: (Long) -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenPersonas: () -> Unit = {},
) {
    val naglyColors = LocalNaglyColors.current
    var showCustomDialog by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 800),
        label = "fill",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    ),
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = NaglySpacing.md, vertical = NaglySpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            NaglyShapes.pill,
                        )
                        .padding(horizontal = NaglySpacing.xs + 4.dp, vertical = 6.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(NaglySpacing.xs))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(220.dp),
        ) {
            FillBottle(
                progress = animatedProgress,
                modifier = Modifier.size(140.dp, 200.dp),
            )
            Text(
                text = state.personaEmoji,
                fontSize = 42.sp,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 28.dp),
            )
        }

        Text(
            text = "${state.consumedMl} / ${state.dailyMl} ml",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = NaglySpacing.xxs),
        )

        Text(
            text = moodLabel(state.mood),
            style = MaterialTheme.typography.labelLarge,
            color = state.mood.moodColor(naglyColors),
            modifier = Modifier.padding(top = NaglySpacing.xs + 4.dp),
        )
        SpeechBubbleSimple(
            text = state.personaLine.ifBlank { "Loading your nag..." },
            textStyle = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NaglySpacing.xxs, bottom = NaglySpacing.xs),
        )

        Text(
            text = state.nextNudgeLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(NaglySpacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            QuickAdd(
                label = "+250",
                onClick = { onLog(250) },
                modifier = Modifier.weight(1f),
                emphasized = true,
            )
            QuickAdd(
                label = "+500",
                onClick = { onLog(500) },
                modifier = Modifier.weight(1f),
            )
            val custom = state.recentCustomMl
            if (custom != null) {
                QuickAdd(
                    label = "+$custom",
                    onClick = { onLog(custom) },
                    modifier = Modifier.weight(1f),
                )
            } else {
                PillButton(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier.weight(1f),
                    variant = PillButtonVariant.Outlined,
                ) {
                    Text("Custom")
                }
            }
        }

        if (state.recentCustomMl != null) {
            TextButton(onClick = { showCustomDialog = true }) {
                Text("Other amount")
            }
        }

        Spacer(modifier = Modifier.height(NaglySpacing.xs + 4.dp))

        Text(
            "Today's sips",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = NaglySpacing.xs),
        )
        if (state.drinks.isEmpty()) {
            Text(
                "No drinks yet — tap a quick-add.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            state.drinks.forEach { drink ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = NaglySpacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = formatTime(drink.timestampMs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(64.dp),
                    )
                    Text(
                        text = "${drink.amountMl} ml",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { onUndoEntry(drink.id) }) {
                        Text("Undo")
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomAmountDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { amount ->
                showCustomDialog = false
                onLog(amount)
            },
        )
    }
}

@Composable
private fun QuickAdd(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    PillButton(
        onClick = onClick,
        modifier = modifier,
        variant = if (emphasized) PillButtonVariant.Primary else PillButtonVariant.Accent,
    ) {
        Text(label)
    }
}

@Composable
private fun FillBottle(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val water = MaterialTheme.colorScheme.primary
    val glass = MaterialTheme.colorScheme.outline
    val foam = MaterialTheme.colorScheme.surface
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val neckW = w * 0.28f
        val bodyTop = h * 0.18f
        val bodyLeft = w * 0.18f
        val bodyWidth = w * 0.64f
        val bodyHeight = h * 0.72f

        val path = Path().apply {
            moveTo(w / 2f - neckW / 2f, 0f)
            lineTo(w / 2f + neckW / 2f, 0f)
            lineTo(w / 2f + neckW / 2f, bodyTop)
            lineTo(bodyLeft + bodyWidth, bodyTop)
            lineTo(bodyLeft + bodyWidth, bodyTop + bodyHeight)
            quadraticTo(
                bodyLeft + bodyWidth / 2f,
                h,
                bodyLeft,
                bodyTop + bodyHeight,
            )
            lineTo(bodyLeft, bodyTop)
            lineTo(w / 2f - neckW / 2f, bodyTop)
            close()
        }
        drawPath(path, color = foam.copy(alpha = 0.35f))
        drawPath(path, color = glass, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))

        val fillH = bodyHeight * progress.coerceIn(0f, 1f)
        if (fillH > 0f) {
            val fillTop = bodyTop + bodyHeight - fillH
            drawRoundRect(
                color = water.copy(alpha = 0.75f),
                topLeft = Offset(bodyLeft + 3.dp.toPx(), fillTop),
                size = Size(bodyWidth - 6.dp.toPx(), fillH),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
            )
        }
    }
}

@Composable
private fun CustomAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var text by remember { mutableStateOf("300") }
    AlertDialog(
        onDismissRequest = onDismiss,
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
                    if (amount != null && amount > 0) onConfirm(amount)
                },
            ) { Text("Log it") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = NaglyShapes.dialog,
    )
}

private fun moodLabel(mood: Mood): String = when (mood) {
    Mood.NEUTRAL -> "Keeping an eye on you"
    Mood.WORRIED -> "Getting worried"
    Mood.DISAPPOINTED -> "Disappointed"
    Mood.PROUD -> "Proud of you"
}

private fun formatTime(timestampMs: Long): String {
    val local = Instant.fromEpochMilliseconds(timestampMs)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val h = local.hour.toString().padStart(2, '0')
    val m = local.minute.toString().padStart(2, '0')
    return "$h:$m"
}
