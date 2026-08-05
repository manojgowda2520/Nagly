package com.manojbuilds.nagly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.ui.common.PlaceholderScreen
import com.manojbuilds.nagly.ui.history.HistoryScreen
import com.manojbuilds.nagly.ui.history.HistoryStateHolder
import com.manojbuilds.nagly.ui.navigation.Screen
import com.manojbuilds.nagly.ui.onboarding.OnboardingScreen
import com.manojbuilds.nagly.ui.onboarding.OnboardingStateHolder
import com.manojbuilds.nagly.ui.persona.PersonaPickerScreen
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
            val goalRepository = koinInject<GoalRepository>()
            val goal by goalRepository.observeGoal().collectAsState(
                initial = GoalRepository.DEFAULT_GOAL,
            )
            var screen by remember { mutableStateOf<Screen?>(null) }
            LaunchedEffect(goal.onboarded) {
                if (screen == null) {
                    screen = if (goal.onboarded) Screen.Today else Screen.Onboarding
                } else if (!goal.onboarded && screen == Screen.Today) {
                    screen = Screen.Onboarding
                }
            }
            val currentScreen = screen ?: return@Surface

            val todayStateHolder = koinInject<TodayStateHolder>()
            val todayState by todayStateHolder.uiState.collectAsState()
            val onboardingHolder = koinInject<OnboardingStateHolder>()
            val onboardingState by onboardingHolder.uiState.collectAsState()
            val historyHolder = koinInject<HistoryStateHolder>()
            val historyState by historyHolder.uiState.collectAsState()

            when (currentScreen) {
                Screen.Today -> TodayScreen(
                    state = todayState,
                    onLog = todayStateHolder::log,
                    onUndo = todayStateHolder::undo,
                    onOpenHistory = { screen = Screen.History },
                    onOpenPersonas = { screen = Screen.PersonaPicker },
                )
                Screen.History -> HistoryScreen(
                    state = historyState,
                    onBack = { screen = Screen.Today },
                )
                Screen.PersonaPicker -> PersonaPickerScreen(
                    selectedId = goal.personaId,
                    onSelect = { id ->
                        onboardingHolder.savePersonaOnly(id) {
                            screen = Screen.Today
                        }
                    },
                    canSelect = { persona ->
                        onboardingHolder.canSelect(
                            persona,
                            onboardingState.unlockedIds,
                            onboardingState.isPro,
                        )
                    },
                    onLockedClick = { screen = Screen.Paywall },
                    onBack = { screen = Screen.Today },
                )
                Screen.Onboarding -> OnboardingScreen(
                    state = onboardingState,
                    onWeightChange = onboardingHolder::setWeight,
                    onManualMlChange = onboardingHolder::setManualMl,
                    onSelectPersona = { id ->
                        val persona = PersonaCatalog.get(id)
                        if (onboardingHolder.canSelect(
                                persona,
                                onboardingState.unlockedIds,
                                onboardingState.isPro,
                            )
                        ) {
                            onboardingHolder.selectPersona(id)
                        }
                    },
                    onWakeChange = onboardingHolder::setWakeHour,
                    onSleepChange = onboardingHolder::setSleepHour,
                    onNext = onboardingHolder::next,
                    onBack = onboardingHolder::back,
                    onFinish = {
                        onboardingHolder.finish { screen = Screen.Today }
                    },
                    canSelectPersona = { persona ->
                        onboardingHolder.canSelect(
                            persona,
                            onboardingState.unlockedIds,
                            onboardingState.isPro,
                        )
                    },
                    permissionLine = onboardingHolder.permissionLine(),
                )
                Screen.Paywall -> PlaceholderScreen(
                    title = "Paywall",
                    onBack = { screen = Screen.PersonaPicker },
                )
            }
        }
    }
}
