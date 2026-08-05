package com.manojbuilds.nagly.platform

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPlatformHaptics(): HapticFeedback = object : HapticFeedback {
    override fun lightTap() = Unit
    override fun success() = Unit
}
