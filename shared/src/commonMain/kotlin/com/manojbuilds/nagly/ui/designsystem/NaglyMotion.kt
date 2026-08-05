package com.manojbuilds.nagly.ui.designsystem

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object NaglyMotion {
    const val DurationFast = 150
    const val DurationNormal = 250
    const val DurationSlow = 350

    fun tweenFast() = tween<Float>(durationMillis = DurationFast)
    fun tweenNormal() = tween<Float>(durationMillis = DurationNormal)
    fun tweenSlow() = tween<Float>(durationMillis = DurationSlow)

    fun springSnappy() = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    fun springGentle() = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow,
    )
}
