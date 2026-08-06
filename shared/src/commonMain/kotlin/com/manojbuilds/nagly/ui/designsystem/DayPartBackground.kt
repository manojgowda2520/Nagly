package com.manojbuilds.nagly.ui.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Soft low-saturation daypart tints — never muddy brown.
 * [intensity] 1f = Home hero; ~0.45f = subtler tab screens.
 */
fun dayPartTopTint(hour: Int, intensity: Float = 1f): Color {
    val base = when (hour) {
        in 5..10 -> Color(0xFFFFE4D6) // soft peach morning
        in 11..16 -> Color(0xFFD6F0FA) // soft sky afternoon
        in 17..20 -> Color(0xFFFFE8C8) // soft amber evening
        else -> Color(0xFF1A2A3A) // soft navy night
    }
    return base.copy(alpha = (0.55f * intensity).coerceIn(0.15f, 0.75f))
}

@Composable
fun dayPartBrush(
    hour: Int = currentHour(),
    colors: NaglyColors = LocalNaglyColors.current,
    intensity: Float = 1f,
): Brush {
    val top = dayPartTopTint(hour, intensity)
    val bottom = colors.background
    return Brush.verticalGradient(
        colors = listOf(top, bottom),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY,
    )
}

fun currentHour(clock: Clock = Clock.System): Int =
    clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
