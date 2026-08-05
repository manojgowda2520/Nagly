package com.manojbuilds.nagly.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

interface HapticFeedback {
    fun lightTap()
    fun success()
}

private object NoOpHapticFeedback : HapticFeedback {
    override fun lightTap() = Unit
    override fun success() = Unit
}

val LocalHaptics = compositionLocalOf<HapticFeedback> { NoOpHapticFeedback }

@Composable
expect fun rememberPlatformHaptics(): HapticFeedback
