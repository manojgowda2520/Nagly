package com.manojbuilds.nagly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.manojbuilds.nagly.ads.AdClient
import com.manojbuilds.nagly.ads.FakeAdClient
import com.manojbuilds.nagly.billing.BillingRepository
import com.manojbuilds.nagly.data.GoalRepository
import com.manojbuilds.nagly.data.UnlockRepository
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.TEMP_UNLOCK_MS
import com.manojbuilds.nagly.domain.UnlockExpiryWatcher
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.ui.history.HistoryScreen
import com.manojbuilds.nagly.ui.history.HistoryStateHolder
import com.manojbuilds.nagly.ui.navigation.Screen
import com.manojbuilds.nagly.ui.onboarding.OnboardingScreen
import com.manojbuilds.nagly.ui.onboarding.OnboardingStateHolder
import com.manojbuilds.nagly.ui.paywall.PaywallScreen
import com.manojbuilds.nagly.ui.persona.FakeAdOverlay
import com.manojbuilds.nagly.ui.persona.PersonaPickerScreen
import com.manojbuilds.nagly.ui.persona.UnlockSheet
import com.manojbuilds.nagly.ui.theme.NaglyTheme
import com.manojbuilds.nagly.ui.today.TodayScreen
import com.manojbuilds.nagly.ui.today.TodayStateHolder
import kotlinx.coroutines.launch
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
            val unlockRepository = koinInject<UnlockRepository>()
            val goal by goalRepository.observeGoal().collectAsState(
                initial = GoalRepository.DEFAULT_GOAL,
            )
            val unlockExpiries by unlockRepository.observeUnlockExpiries()
                .collectAsState(initial = emptyMap())
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
            val billing = koinInject<BillingRepository>()
            val adClient = koinInject<AdClient>()
            val expiryWatcher = koinInject<UnlockExpiryWatcher>()
            val expiryMessage by expiryWatcher.expiryMessage.collectAsState()
            val scope = rememberCoroutineScope()
            var purchasing by remember { mutableStateOf(false) }
            var lockedPersona by remember { mutableStateOf<Persona?>(null) }
            var watchingAd by remember { mutableStateOf(false) }

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
                    unlockExpiries = unlockExpiries,
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
                    onLockedClick = { persona -> lockedPersona = persona },
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
                        } else if (PersonaCatalog.isPro(persona)) {
                            lockedPersona = persona
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
                Screen.Paywall -> PaywallScreen(
                    purchasing = purchasing,
                    onPurchase = { packageId ->
                        scope.launch {
                            purchasing = true
                            billing.purchase(packageId)
                            purchasing = false
                            screen = Screen.PersonaPicker
                        }
                    },
                    onRestore = {
                        scope.launch {
                            purchasing = true
                            billing.restore()
                            purchasing = false
                            if (billing.isPro.value) screen = Screen.PersonaPicker
                        }
                    },
                    onClose = { screen = Screen.PersonaPicker },
                )
            }

            lockedPersona?.let { persona ->
                UnlockSheet(
                    persona = persona,
                    isPro = onboardingState.isPro,
                    watchingAd = watchingAd,
                    onWatchAd = {
                        scope.launch {
                            watchingAd = true
                            adClient.loadRewarded()
                            val result = adClient.showRewarded()
                            watchingAd = false
                            if (result.isSuccess) {
                                unlockRepository.grant(persona.id, TEMP_UNLOCK_MS)
                                onboardingHolder.selectPersona(persona.id)
                                onboardingHolder.savePersonaOnly(persona.id) {}
                                lockedPersona = null
                                screen = Screen.Today
                            }
                        }
                    },
                    onGoPro = {
                        lockedPersona = null
                        screen = Screen.Paywall
                    },
                    onDismiss = { lockedPersona = null },
                )
            }

            if (watchingAd) {
                FakeAdOverlay(
                    personaName = lockedPersona?.displayName ?: "her",
                    onCancel = {
                        (adClient as? FakeAdClient)?.cancel()
                        watchingAd = false
                    },
                )
            }

            expiryMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = expiryWatcher::clearExpiryMessage,
                    title = { Text("She's gone for now") },
                    text = { Text(message) },
                    confirmButton = {
                        TextButton(onClick = expiryWatcher::clearExpiryMessage) {
                            Text("Okay")
                        }
                    },
                )
            }
        }
    }
}
