package com.manojbuilds.nagly.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.ui.designsystem.NaglyShapes
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment

@Composable
fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    tailAtStart: Boolean = true,
) {
    val colors = LocalNaglyColors.current
    val bubbleColor = backgroundColor ?: colors.card
    val contentColor = textColor ?: colors.textPrimary

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = NaglySpacing.xxs),
        ) {
            drawSpeechBubble(
                color = bubbleColor,
                tailAtStart = tailAtStart,
            )
        }
        Text(
            text = text,
            style = textStyle,
            color = contentColor,
            modifier = Modifier
                .padding(
                    start = NaglySpacing.sm,
                    end = NaglySpacing.sm,
                    top = NaglySpacing.sm,
                    bottom = NaglySpacing.sm + NaglySpacing.xxs,
                ),
        )
    }
}

private fun DrawScope.drawSpeechBubble(
    color: Color,
    tailAtStart: Boolean,
) {
    val tailHeight = 10.dp.toPx()
    val cornerRadius = 16.dp.toPx()
    val tailWidth = 14.dp.toPx()
    val bodyBottom = size.height - tailHeight

    val roundRectPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = bodyBottom,
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            ),
        )
    }

    val tailCenterX = if (tailAtStart) cornerRadius + tailWidth else size.width - cornerRadius - tailWidth
    val tailPath = Path().apply {
        moveTo(tailCenterX - tailWidth / 2f, bodyBottom)
        lineTo(tailCenterX, size.height)
        lineTo(tailCenterX + tailWidth / 2f, bodyBottom)
        close()
    }

    drawPath(roundRectPath, color)
    drawPath(tailPath, color)
}

@Composable
fun SpeechBubbleSimple(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    textColor: Color? = null,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    val colors = LocalNaglyColors.current
    val bubbleColor = backgroundColor ?: colors.surfaceVariant
    val contentColor = textColor ?: colors.textPrimary

    Box(
        modifier = modifier
            .clip(NaglyShapes.card)
            .background(bubbleColor)
            .padding(
                horizontal = NaglySpacing.sm,
                vertical = NaglySpacing.xs,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = text, style = textStyle, color = contentColor)
    }
}
