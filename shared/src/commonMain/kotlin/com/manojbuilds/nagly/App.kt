package com.manojbuilds.nagly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.manojbuilds.nagly.ui.common.PlaceholderScreen
import com.manojbuilds.nagly.ui.navigation.Screen
import com.manojbuilds.nagly.ui.theme.NaglyTheme
import com.manojbuilds.nagly.ui.today.TodayScreen
import com.manojbuilds.nagly.ui.today.TodayStateHolder
import org.koin.compose.koinInject

@Composable
fun App() {
    NaglyTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
        ) {
            var screen by remember { mutableStateOf<Screen>(Screen.Today) }
            val todayStateHolder = koinInject<TodayStateHolder>()
            val todayState by todayStateHolder.uiState.collectAsState()

            when (val current = screen) {
                Screen.Today -> TodayScreen(
                    state = todayState,
                    onLog = todayStateHolder::log,
                    onUndo = todayStateHolder::undo,
                    onOpenHistory = { screen = Screen.History },
                    onOpenPersonas = { screen = Screen.PersonaPicker },
                )
                Screen.History -> PlaceholderScreen(
                    title = "History",
                    onBack = { screen = Screen.Today },
                )
                Screen.PersonaPicker -> PlaceholderScreen(
                    title = "Personas",
                    onBack = { screen = Screen.Today },
                )
                Screen.Onboarding -> PlaceholderScreen(
                    title = "Onboarding",
                    onBack = { screen = Screen.Today },
                )
                Screen.Paywall -> PlaceholderScreen(
                    title = "Paywall",
                    onBack = { screen = Screen.Today },
                )
            }
        }
    }
}
