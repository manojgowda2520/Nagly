package com.manojbuilds.nagly.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HistoryScreen(
    state: HistoryUiState,
) {
    val barColor = MaterialTheme.colorScheme.primary
    val goalColor = MaterialTheme.colorScheme.secondary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val metColor = MaterialTheme.colorScheme.primary
    val partialColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val empty = state.days.all { it.totalMl == 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("History", style = MaterialTheme.typography.headlineMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StreakChip(label = "Current", value = state.currentStreak)
            StreakChip(label = "Best", value = state.bestStreak)
        }

        if (empty) {
            Text(
                text = "${state.personaName} says:",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "\"${state.emptyLine}\"",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            Text("Last 7 days", style = MaterialTheme.typography.titleLarge)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 8.dp),
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
                        Text(day.label, style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${day.totalMl}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Text(
                text = state.monthLabel,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
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
private fun StreakChip(label: String, value: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
    metColor: androidx.compose.ui.graphics.Color,
    partialColor: androidx.compose.ui.graphics.Color,
    emptyColor: androidx.compose.ui.graphics.Color,
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
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    day.date == null -> androidx.compose.ui.graphics.Color.Transparent
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
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
