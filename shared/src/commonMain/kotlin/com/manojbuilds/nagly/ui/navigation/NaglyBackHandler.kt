package com.manojbuilds.nagly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler

/** Wraps Compose Multiplatform [BackHandler] with the required experimental opt-in. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NaglyBackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
