package com.manojbuilds.nagly.ui.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglyMotion
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class Bubble(
    val xRatio: Float,
    var yRatio: Float,
    val radius: Float,
    val speed: Float,
)

private data class SplashParticle(
    val startX: Float,
    val startY: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val bornAt: Long,
)

private data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val bornAt: Long,
)

private val MILESTONES = listOf(0.5f to "☀️", 0.75f to "🌤")

@Composable
fun WaveBottle(
    progress: Float,
    expectedProgress: Float,
    waterColor: Color,
    modifier: Modifier = Modifier,
    onLogPulse: Boolean = false,
    goalReached: Boolean = false,
) {
    val colors = LocalNaglyColors.current
    val glass = colors.outline
    val foam = colors.card

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(NaglyMotion.DurationSlow, easing = FastOutSlowInEasing),
        label = "waveLevel",
    )

    val waveTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by waveTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )
    val sway by waveTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sway",
    )

    val shake = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }
    val bubbles = remember { mutableStateListOf<Bubble>() }
    var splashParticles by remember { mutableStateOf<List<SplashParticle>>(emptyList()) }
    var confetti by remember { mutableStateOf<List<ConfettiPiece>>(emptyList()) }
    var frameTick by remember { mutableStateOf(0L) }

    LaunchedEffect(onLogPulse) {
        if (onLogPulse) {
            shake.snapTo(0f)
            repeat(4) {
                shake.animateTo(if (it % 2 == 0) 3f else -3f, tween(50))
            }
            shake.animateTo(0f, tween(80))
            val now = frameTick
            splashParticles = List(12) {
                val angle = Random.nextFloat() * PI.toFloat() * 2f
                val speed = Random.nextFloat() * 4f + 2f
                SplashParticle(
                    startX = 0.5f,
                    startY = 1f - animatedProgress,
                    vx = sin(angle) * speed,
                    vy = -kotlin.math.cos(angle) * speed - 2f,
                    color = waterColor.copy(alpha = Random.nextFloat() * 0.4f + 0.5f),
                    size = Random.nextFloat() * 4f + 2f,
                    bornAt = now,
                )
            }
        }
    }

    LaunchedEffect(goalReached) {
        if (goalReached) {
            glowAlpha.animateTo(0.6f, tween(400))
            glowAlpha.animateTo(0.15f, tween(1200))
            val now = frameTick
            confetti = List(24) {
                ConfettiPiece(
                    x = Random.nextFloat(),
                    y = 0.2f,
                    vx = (Random.nextFloat() - 0.5f) * 3f,
                    vy = Random.nextFloat() * 2f + 1f,
                    color = listOf(
                        waterColor,
                        colors.accent,
                        colors.success,
                        colors.warning,
                    ).random(),
                    size = Random.nextFloat() * 5f + 3f,
                    rotation = Random.nextFloat() * 360f,
                    bornAt = now,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        if (bubbles.isEmpty()) {
            repeat(6) {
                bubbles += Bubble(
                    xRatio = Random.nextFloat() * 0.6f + 0.2f,
                    yRatio = Random.nextFloat(),
                    radius = Random.nextFloat() * 2.5f + 1.5f,
                    speed = Random.nextFloat() * 0.003f + 0.001f,
                )
            }
        }
        while (true) {
            frameTick++
            val p = animatedProgress.coerceAtLeast(0.05f)
            bubbles.forEachIndexed { i, b ->
                var ny = b.yRatio - b.speed
                if (ny < 0f) ny = Random.nextFloat()
                bubbles[i] = b.copy(yRatio = ny.coerceAtMost(p * 0.95f))
            }
            val now = frameTick
            splashParticles = splashParticles.filter { now - it.bornAt < 30 }
            confetti = confetti.filter { now - it.bornAt < 60 }
            kotlinx.coroutines.delay(16)
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val neckW = w * 0.28f
            val bodyTop = h * 0.18f
            val bodyLeft = w * 0.18f
            val bodyWidth = w * 0.64f
            val bodyHeight = h * 0.72f

            val bottlePath = bottleOutline(w, h, neckW, bodyTop, bodyLeft, bodyWidth, bodyHeight)

            translate(left = shake.value) {
                if (glowAlpha.value > 0f) {
                    drawPath(
                        bottlePath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                waterColor.copy(alpha = glowAlpha.value),
                                Color.Transparent,
                            ),
                            center = Offset(w / 2f, bodyTop + bodyHeight / 2f),
                            radius = w * 0.7f,
                        ),
                    )
                }

                drawPath(bottlePath, color = foam.copy(alpha = 0.35f))
                drawPath(bottlePath, color = glass, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))

                clipPath(bottlePath) {
                    val fillRatio = animatedProgress.coerceIn(0f, 1f)
                    if (fillRatio > 0.01f) {
                        val fillTop = bodyTop + bodyHeight * (1f - fillRatio)
                        val wavePath = waveSurfacePath(
                            left = bodyLeft,
                            top = fillTop,
                            width = bodyWidth,
                            bottom = bodyTop + bodyHeight,
                            phase = wavePhase,
                            amplitude = 3.5f + sway,
                            swayOffset = sway,
                        )
                        drawPath(
                            wavePath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    waterColor.copy(alpha = 0.95f),
                                    waterColor.copy(alpha = 0.65f),
                                ),
                                startY = fillTop,
                                endY = bodyTop + bodyHeight,
                            ),
                        )

                        bubbles.forEach { bubble ->
                            val bx = bodyLeft + bodyWidth * bubble.xRatio
                            val by = bodyTop + bodyHeight * (1f - bubble.yRatio.coerceIn(0f, fillRatio))
                            if (by >= fillTop) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.35f),
                                    radius = bubble.radius,
                                    center = Offset(bx, by),
                                )
                            }
                        }
                    }

                    MILESTONES.forEach { (ratio, _) ->
                        val markY = bodyTop + bodyHeight * (1f - ratio)
                        drawLine(
                            color = glass.copy(alpha = 0.7f),
                            start = Offset(bodyLeft + 4.dp.toPx(), markY),
                            end = Offset(bodyLeft + bodyWidth - 4.dp.toPx(), markY),
                            strokeWidth = 1.5f,
                            cap = StrokeCap.Round,
                        )
                    }
                }

                splashParticles.forEach { p ->
                    val age = (frameTick - p.bornAt).toFloat()
                    val px = bodyLeft + bodyWidth * p.startX + p.vx * age
                    val py = bodyTop + bodyHeight * p.startY + p.vy * age + age * 0.15f
                    drawCircle(
                        color = p.color.copy(alpha = (1f - age / 30f).coerceIn(0f, 1f)),
                        radius = p.size,
                        center = Offset(px, py),
                    )
                }

                confetti.forEach { c ->
                    val age = (frameTick - c.bornAt).toFloat()
                    val cx = c.x * w + c.vx * age
                    val cy = c.y * h + c.vy * age + age * 0.08f
                    val alpha = (1f - age / 60f).coerceIn(0f, 1f)
                    rotate(c.rotation + age * 4f, pivot = Offset(cx, cy)) {
                        drawRect(
                            color = c.color,
                            topLeft = Offset(cx - c.size / 2f, cy - c.size / 4f),
                            size = Size(c.size, c.size / 2f),
                            alpha = alpha,
                        )
                    }
                }
            }
        }

        MILESTONES.forEach { (ratio, emoji) ->
            val showWarning = expectedProgress >= ratio && progress < ratio
            val yFraction = 1f - ratio
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (130.dp * 0.18f + 130.dp * 0.72f * yFraction - 8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (showWarning) "⚠️" else emoji,
                    fontSize = if (showWarning) 12.sp else 10.sp,
                )
            }
        }
    }
}

private fun bottleOutline(
    w: Float,
    h: Float,
    neckW: Float,
    bodyTop: Float,
    bodyLeft: Float,
    bodyWidth: Float,
    bodyHeight: Float,
): Path = Path().apply {
    moveTo(w / 2f - neckW / 2f, 0f)
    lineTo(w / 2f + neckW / 2f, 0f)
    lineTo(w / 2f + neckW / 2f, bodyTop)
    lineTo(bodyLeft + bodyWidth, bodyTop)
    lineTo(bodyLeft + bodyWidth, bodyTop + bodyHeight)
    quadraticTo(bodyLeft + bodyWidth / 2f, h, bodyLeft, bodyTop + bodyHeight)
    lineTo(bodyLeft, bodyTop)
    lineTo(w / 2f - neckW / 2f, bodyTop)
    close()
}

private fun waveSurfacePath(
    left: Float,
    top: Float,
    width: Float,
    bottom: Float,
    phase: Float,
    amplitude: Float,
    swayOffset: Float,
): Path = Path().apply {
    val steps = 32
    val stepW = width / steps
    moveTo(left, bottom)
    lineTo(left, top)
    for (i in 0..steps) {
        val x = left + i * stepW
        val wave = sin(phase + i * 0.45f + swayOffset * 0.3f) * amplitude
        lineTo(x, top + wave)
    }
    lineTo(left + width, bottom)
    close()
}
