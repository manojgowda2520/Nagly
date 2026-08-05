package com.manojbuilds.nagly.ui.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.moodColor

@Composable
fun MoodRing(
    progress: Float,
    mood: Mood,
    modifier: Modifier = Modifier,
    ringSize: Dp = 220.dp,
    strokeWidth: Dp = 8.dp,
    trackColor: Color? = null,
    moodColor: Color? = null,
) {
    val colors = LocalNaglyColors.current
    val ringColor = moodColor ?: mood.moodColor(colors)
    val backgroundTrack = trackColor ?: colors.outline.copy(alpha = 0.35f)
    val clampedProgress = progress.coerceIn(0f, 1f)

    Canvas(modifier = modifier.size(ringSize)) {
        val stroke = strokeWidth.toPx()
        val diameter = this.size.minDimension - stroke
        val topLeft = Offset(stroke / 2f, stroke / 2f)
        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

        drawArc(
            color = backgroundTrack,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        if (clampedProgress > 0f) {
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * clampedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
fun MoodRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    ringSize: Dp = 220.dp,
    strokeWidth: Dp = 8.dp,
    trackColor: Color? = null,
) {
    val colors = LocalNaglyColors.current
    val backgroundTrack = trackColor ?: colors.outline.copy(alpha = 0.35f)
    val clampedProgress = progress.coerceIn(0f, 1f)

    Canvas(modifier = modifier.size(ringSize)) {
        val stroke = strokeWidth.toPx()
        val diameter = this.size.minDimension - stroke
        val topLeft = Offset(stroke / 2f, stroke / 2f)
        val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

        drawArc(
            color = backgroundTrack,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        if (clampedProgress > 0f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * clampedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
    }
}
