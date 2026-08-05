package com.manojbuilds.nagly.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNaglyColors.current
    val alpha = remember { Animatable(1f) }
    val dropY = remember { Animatable(0f) }
    val heartScale = remember { Animatable(0f) }
    val bubbleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        dropY.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        delay(200)
        heartScale.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        delay(300)
        bubbleAlpha.animateTo(1f, tween(500))
        delay(900)
        alpha.animateTo(0f, tween(600))
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = alpha.value)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.38f
            val dropOffset = dropY.value * 80f

            // Water drop
            val dropPath = Path().apply {
                moveTo(cx, cy - 30f + dropOffset)
                cubicTo(cx + 20f, cy - 10f + dropOffset, cx + 18f, cy + 20f + dropOffset, cx, cy + 30f + dropOffset)
                cubicTo(cx - 18f, cy + 20f + dropOffset, cx - 20f, cy - 10f + dropOffset, cx, cy - 30f + dropOffset)
                close()
            }
            drawPath(dropPath, colors.primary.copy(alpha = alpha.value))

            // Heart
            if (heartScale.value > 0f) {
                val hs = heartScale.value
                val heartPath = Path().apply {
                    val hy = cy + 50f
                    moveTo(cx, hy + 10f * hs)
                    cubicTo(cx - 25f * hs, hy - 10f * hs, cx - 35f * hs, hy + 15f * hs, cx, hy + 35f * hs)
                    cubicTo(cx + 35f * hs, hy + 15f * hs, cx + 25f * hs, hy - 10f * hs, cx, hy + 10f * hs)
                    close()
                }
                drawPath(heartPath, colors.accent.copy(alpha = alpha.value * 0.9f))
            }

            // Speech bubble
            if (bubbleAlpha.value > 0f) {
                val ba = bubbleAlpha.value * alpha.value
                drawRoundRect(
                    color = colors.card.copy(alpha = ba),
                    topLeft = Offset(cx - 60f, cy + 90f),
                    size = androidx.compose.ui.geometry.Size(120f, 50f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                )
                val tail = Path().apply {
                    moveTo(cx - 10f, cy + 140f)
                    lineTo(cx, cy + 155f)
                    lineTo(cx + 10f, cy + 140f)
                    close()
                }
                drawPath(tail, colors.card.copy(alpha = ba))
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .padding(horizontal = NaglySpacing.lg),
        ) {
            Text(
                text = "Someone who cares.",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary.copy(alpha = bubbleAlpha.value * alpha.value),
                textAlign = TextAlign.Center,
            )
        }
    }
}
