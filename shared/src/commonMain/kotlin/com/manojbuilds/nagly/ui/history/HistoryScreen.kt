package com.manojbuilds.nagly.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import com.manojbuilds.nagly.ui.designsystem.dayPartBrush
import com.manojbuilds.nagly.ui.designsystem.components.NaglyCard
import com.manojbuilds.nagly.ui.designsystem.components.PillButton
import com.manojbuilds.nagly.ui.designsystem.components.PillButtonVariant
import com.manojbuilds.nagly.ui.designsystem.components.SpeechBubbleSimple

private enum class HistoryViewMode {
    Conversation,
    Chart,
}

@Composable
fun HistoryScreen(
    state: HistoryUiState,
) {
    val colors = LocalNaglyColors.current
    var viewMode by remember { mutableStateOf(HistoryViewMode.Conversation) }
    val empty = state.days.all { it.totalMl == 0 }
    val tabTint = dayPartBrush(colors = colors, intensity = 0.45f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tabTint)
            .padding(NaglySpacing.md),
    ) {
        Text("History", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NaglySpacing.xs, bottom = NaglySpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(NaglySpacing.sm),
        ) {
            StreakChip(label = "Current", value = state.currentStreak, modifier = Modifier.weight(1f))
            StreakChip(label = "Best", value = state.bestStreak, modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = NaglySpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(NaglySpacing.xs),
        ) {
            PillButton(
                onClick = { viewMode = HistoryViewMode.Conversation },
                modifier = Modifier.weight(1f),
                variant = if (viewMode == HistoryViewMode.Conversation) {
                    PillButtonVariant.Primary
                } else {
                    PillButtonVariant.Outlined
                },
            ) {
                Text("Conversation", maxLines = 1, softWrap = false)
            }
            PillButton(
                onClick = { viewMode = HistoryViewMode.Chart },
                modifier = Modifier.weight(1f),
                variant = if (viewMode == HistoryViewMode.Chart) {
                    PillButtonVariant.Primary
                } else {
                    PillButtonVariant.Outlined
                },
            ) {
                Text("Chart", maxLines = 1, softWrap = false)
            }
        }

        when (viewMode) {
            HistoryViewMode.Conversation -> ConversationView(state, empty, colors)
            HistoryViewMode.Chart -> ChartView(state, empty, colors)
        }
    }
}

@Composable
private fun ConversationView(
    state: HistoryUiState,
    empty: Boolean,
    colors: com.manojbuilds.nagly.ui.designsystem.NaglyColors,
) {
    if (empty || state.chatItems.isEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${state.personaEmoji} ${state.personaName} says:",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            SpeechBubbleSimple(
                text = "\"${state.emptyLine}\"",
                textStyle = MaterialTheme.typography.titleMedium,
                textColor = colors.textPrimary,
                backgroundColor = colors.card,
                modifier = Modifier.padding(top = NaglySpacing.sm),
            )
        }
        return
    }

    val listState = rememberLazyListState()
    LaunchedEffect(state.chatItems.size) {
        if (state.chatItems.isNotEmpty()) {
            listState.animateScrollToItem(state.chatItems.lastIndex)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.xxs),
    ) {
        items(state.chatItems, key = { item ->
            when (item) {
                is ChatItem.DayDivider -> "d-${item.label}"
                is ChatItem.Message -> item.message.id
            }
        }) { item ->
            when (item) {
                is ChatItem.DayDivider -> DayDividerChip(item.label, colors)
                is ChatItem.Message -> ChatBubble(item.message, colors)
            }
        }
    }
}

@Composable
private fun DayDividerChip(label: String, colors: com.manojbuilds.nagly.ui.designsystem.NaglyColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = NaglySpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.outline.copy(alpha = 0.3f)),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = NaglySpacing.sm),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(colors.outline.copy(alpha = 0.3f)),
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    colors: com.manojbuilds.nagly.ui.designsystem.NaglyColors,
) {
    val bubbleColor = if (message.isUser) colors.primary.copy(alpha = 0.15f) else colors.card
    val textColor = if (message.isUser) colors.primary else colors.textPrimary
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp)
    } else {
        RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        ) {
            if (!message.isUser && message.personaEmoji != null) {
                Text(
                    text = message.personaEmoji,
                    modifier = Modifier.padding(end = NaglySpacing.xxs),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(
                horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .clip(shape)
                        .background(bubbleColor)
                        .padding(horizontal = NaglySpacing.sm, vertical = NaglySpacing.xs),
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor,
                    )
                }
                Text(
                    text = formatChatTime(message.timestampMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun ChartView(
    state: HistoryUiState,
    empty: Boolean,
    colors: com.manojbuilds.nagly.ui.designsystem.NaglyColors,
) {
    val barColor = colors.primary
    val goalColor = colors.accent
    val trackColor = colors.outline.copy(alpha = 0.2f)
    val metColor = colors.primary
    val partialColor = colors.primary.copy(alpha = 0.35f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = NaglySpacing.md),
    ) {
        if (empty) {
            Text(
                text = "${state.personaEmoji} ${state.personaName} says:",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            SpeechBubbleSimple(
                text = "\"${state.emptyLine}\"",
                textStyle = MaterialTheme.typography.titleMedium,
                textColor = colors.textPrimary,
                backgroundColor = colors.card,
                modifier = Modifier.padding(top = NaglySpacing.sm),
            )
        } else {
            Text("Last 7 days", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = NaglySpacing.xs),
            ) {
                val maxMl = maxOf(state.dailyMl, state.days.maxOf { it.totalMl }, 1).toFloat()
                val barWidth = size.width / (state.days.size * 2f)
                val goalY = size.height * (1f - state.dailyMl / maxMl)

                drawLine(
                    color = goalColor,
                    start = Offset(0f, goalY),
                    end = Offset(size.width, goalY),
                    strokeWidth = 3f,
                )

                state.days.forEachIndexed { index, day ->
                    val x = barWidth + index * (barWidth * 2f)
                    val barHeight = size.height * (day.totalMl / maxMl)
                    drawRoundRect(
                        color = trackColor,
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, size.height),
                        cornerRadius = CornerRadius(12f, 12f),
                    )
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(12f, 12f),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                state.days.forEach { day ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(day.label, style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                        Text(
                            "${day.totalMl}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Text(
                text = state.monthLabel,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.padding(top = NaglySpacing.md, bottom = NaglySpacing.xs),
            )
            CalendarHeatGrid(
                days = state.calendarDays,
                metColor = metColor,
                partialColor = partialColor,
                emptyColor = trackColor,
            )
        }
    }
}

@Composable
private fun StreakChip(label: String, value: Int, modifier: Modifier = Modifier) {
    NaglyCard(
        modifier = modifier,
        contentPadding = NaglySpacing.sm,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text(
            "$value day${if (value == 1) "" else "s"}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun CalendarHeatGrid(
    days: List<CalendarDay>,
    metColor: Color,
    partialColor: Color,
    emptyColor: Color,
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            dayLabels.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        days.chunked(7).forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = NaglySpacing.xxs),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(NaglySpacing.xxs)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    day.date == null -> Color.Transparent
                                    day.heat == DayHeat.MET -> metColor
                                    day.heat == DayHeat.PARTIAL -> partialColor
                                    else -> emptyColor
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        day.date?.day?.let { dom ->
                            Text(
                                "$dom",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
