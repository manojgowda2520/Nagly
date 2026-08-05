package com.manojbuilds.nagly.platform

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

class AndroidHapticFeedback(private val view: View) : HapticFeedback {
    override fun lightTap() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    override fun success() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }
}

@Composable
actual fun rememberPlatformHaptics(): HapticFeedback {
    val view = LocalView.current
    return remember(view) { AndroidHapticFeedback(view) }
}
