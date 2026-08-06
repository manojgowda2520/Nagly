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
import androidx.compose.runtime.CompositionLocalProvider
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
import com.manojbuilds.nagly.ui.insights.InsightsScreen
import com.manojbuilds.nagly.ui.insights.InsightsStateHolder
import com.manojbuilds.nagly.ui.navigation.MainShell
import com.manojbuilds.nagly.ui.navigation.MainTab
import com.manojbuilds.nagly.ui.navigation.NaglyBackHandler
import com.manojbuilds.nagly.ui.navigation.Screen
import com.manojbuilds.nagly.ui.navigation.rememberNavBackStack
import com.manojbuilds.nagly.ui.navigation.toMainTabOrNull
import com.manojbuilds.nagly.ui.navigation.toScreen
import com.manojbuilds.nagly.ui.onboarding.OnboardingScreen
import com.manojbuilds.nagly.ui.onboarding.OnboardingStateHolder
import com.manojbuilds.nagly.ui.onboarding.OnboardingStep
import com.manojbuilds.nagly.ui.paywall.PaywallScreen
import com.manojbuilds.nagly.ui.persona.FakeAdOverlay
import com.manojbuilds.nagly.ui.persona.PersonaPickerScreen
import com.manojbuilds.nagly.ui.persona.UnlockSheet
import com.manojbuilds.nagly.ui.persona.previewPersonaForRelationship
import com.manojbuilds.nagly.ui.settings.SettingsScreen
import com.manojbuilds.nagly.ui.settings.SettingsStateHolder
import com.manojbuilds.nagly.ui.splash.SplashScreen
import com.manojbuilds.nagly.platform.LocalHaptics
import com.manojbuilds.nagly.platform.rememberPlatformHaptics
import com.manojbuilds.nagly.ui.theme.NaglyTheme
import com.manojbuilds.nagly.ui.today.TodayScreen
import com.manojbuilds.nagly.ui.today.TodayStateHolder
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App() {
    NaglyTheme {
        val haptics = rememberPlatformHaptics()
        var showSplash by remember { mutableStateOf(true) }
        CompositionLocalProvider(LocalHaptics provides haptics) {
            if (showSplash) {
                SplashScreen(onFinished = { showSplash = false })
            } else {
                AppContent()
            }
        }
    }
}

@Composable
private fun AppContent() {
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
        val navStack = rememberNavBackStack(Screen.Today)
        var navReady by remember { mutableStateOf(false) }
        LaunchedEffect(goal.onboarded) {
            if (!navReady) {
                navStack.resetTo(if (goal.onboarded) Screen.Today else Screen.Onboarding)
                navReady = true
            } else if (!goal.onboarded && navStack.current == Screen.Today) {
                navStack.resetTo(Screen.Onboarding)
            }
        }
        if (!navReady) return@Surface

        val currentScreen = navStack.current

        val todayStateHolder = koinInject<TodayStateHolder>()
        val todayState by todayStateHolder.uiState.collectAsState()
        val onboardingHolder = koinInject<OnboardingStateHolder>()
        val onboardingState by onboardingHolder.uiState.collectAsState()
        val historyHolder = koinInject<HistoryStateHolder>()
        val historyState by historyHolder.uiState.collectAsState()
        val insightsHolder = koinInject<InsightsStateHolder>()
        val insightsState by insightsHolder.uiState.collectAsState()
        val settingsHolder = koinInject<SettingsStateHolder>()
        val settingsState by settingsHolder.uiState.collectAsState()
        val billing = koinInject<BillingRepository>()
        val adClient = koinInject<AdClient>()
        val expiryWatcher = koinInject<UnlockExpiryWatcher>()
        val expiryMessage by expiryWatcher.expiryMessage.collectAsState()
        val scope = rememberCoroutineScope()
        var purchasing by remember { mutableStateOf(false) }
        var lockedPersona by remember { mutableStateOf<Persona?>(null) }
        var watchingAd by remember { mutableStateOf(false) }

        val currentTab = currentScreen.toMainTabOrNull()
        val consumeBack = when {
            lockedPersona != null -> true
            watchingAd -> true
            currentScreen == Screen.Onboarding ->
                onboardingState.step != OnboardingStep.BuildingPlan
            currentScreen == Screen.Paywall -> true
            currentTab != null -> navStack.canPop || currentTab != MainTab.Home
            else -> false
        }

        NaglyBackHandler(enabled = consumeBack) {
            when {
                lockedPersona != null -> lockedPersona = null
                watchingAd -> {
                    (adClient as? FakeAdClient)?.cancel()
                    watchingAd = false
                }
                currentScreen == Screen.Onboarding -> onboardingHolder.back()
                currentScreen == Screen.Paywall -> navStack.pop()
                currentTab != null -> {
                    if (navStack.canPop) {
                        navStack.pop()
                    } else if (currentTab != MainTab.Home) {
                        navStack.resetTo(Screen.Today)
                    }
                }
            }
        }

        if (currentTab != null) {
            MainShell(
                selectedTab = currentTab,
                onTabSelected = { tab -> navStack.navigateToTab(tab) },
            ) {
                when (currentScreen) {
                    Screen.Today -> TodayScreen(
                        state = todayState,
                        onLog = todayStateHolder::log,
                        onUndo = todayStateHolder::undo,
                        onUndoEntry = todayStateHolder::undoEntry,
                        onCycleLine = todayStateHolder::cycleLine,
                        onOpenHistory = { navStack.push(Screen.History) },
                        onOpenPersonas = { navStack.push(Screen.Personas) },
                    )
                    Screen.History -> HistoryScreen(state = historyState)
                    Screen.Personas -> PersonaPickerScreen(
                        selectedId = goal.personaId,
                        unlockExpiries = unlockExpiries,
                        isPro = onboardingState.isPro,
                        relationshipLevel = todayState.relationshipLevel,
                        relationshipProgress = todayState.relationshipProgress,
                        onSelect = { id ->
                            onboardingHolder.savePersonaOnly(id) {}
                        },
                        canSelect = { persona ->
                            onboardingHolder.canSelect(
                                persona,
                                onboardingState.unlockedIds,
                                onboardingState.isPro,
                            )
                        },
                        onLockedClick = { persona -> lockedPersona = persona },
                        onLockedRelationship = { relationship ->
                            lockedPersona = previewPersonaForRelationship(relationship.id)
                        },
                    )
                    Screen.Insights -> InsightsScreen(state = insightsState)
                    Screen.Settings -> SettingsScreen(
                        state = settingsState,
                        onDailyGoalChange = settingsHolder::setDailyDisplay,
                        onVolumeUnitChange = settingsHolder::setVolumeUnit,
                        onWakeChange = settingsHolder::setWakeHour,
                        onSleepChange = settingsHolder::setSleepHour,
                        onNotificationsChange = settingsHolder::setNotificationsEnabled,
                        onOpenPersonas = { navStack.push(Screen.Personas) },
                        onRestorePurchases = settingsHolder::restorePurchases,
                        onRequestPermission = settingsHolder::requestNotificationPermission,
                        onDismissMessage = settingsHolder::clearMessages,
                    )
                    else -> Unit
                }
            }
        } else {
            when (currentScreen) {
                Screen.Onboarding -> OnboardingScreen(
                    state = onboardingState,
                    onWeightChange = onboardingHolder::setWeight,
                    onActivityChange = onboardingHolder::setActivity,
                    onSelectRelationship = onboardingHolder::selectRelationship,
                    onSelectPersona = onboardingHolder::selectPersona,
                    onLockedRelationship = { relationship ->
                        lockedPersona = previewPersonaForRelationship(relationship.id)
                    },
                    onLockedPersona = { persona ->
                        if (PersonaCatalog.isPro(persona)) {
                            lockedPersona = persona
                        }
                    },
                    onWakeChange = onboardingHolder::setWakeHour,
                    onSleepChange = onboardingHolder::setSleepHour,
                    onLogFirstGlass = onboardingHolder::logFirstGlass,
                    onNext = onboardingHolder::next,
                    onFinish = {
                        onboardingHolder.finish { navStack.resetTo(Screen.Today) }
                    },
                    canSelectPersona = { persona ->
                        onboardingHolder.canSelect(
                            persona,
                            onboardingState.unlockedIds,
                            onboardingState.isPro,
                        )
                    },
                    permissionLine = onboardingHolder.permissionLine(),
                    unlockExpiries = unlockExpiries,
                )
                Screen.Paywall -> PaywallScreen(
                    purchasing = purchasing,
                    onPurchase = { packageId ->
                        scope.launch {
                            purchasing = true
                            billing.purchase(packageId)
                            purchasing = false
                            if (navStack.canPop) {
                                navStack.pop()
                            } else {
                                navStack.navigateToTab(MainTab.Characters)
                            }
                        }
                    },
                    onRestore = {
                        scope.launch {
                            purchasing = true
                            billing.restore()
                            purchasing = false
                            if (billing.isPro.value) {
                                if (navStack.canPop) {
                                    navStack.pop()
                                } else {
                                    navStack.navigateToTab(MainTab.Characters)
                                }
                            }
                        }
                    },
                    onClose = { navStack.pop() },
                )
                else -> Unit
            }
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
                            unlockRepository.grant(persona.relationshipId, TEMP_UNLOCK_MS)
                            onboardingHolder.selectPersona(persona.id)
                            onboardingHolder.savePersonaOnly(persona.id) {}
                            lockedPersona = null
                        }
                    }
                },
                onGoPro = {
                    lockedPersona = null
                    navStack.push(Screen.Paywall)
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
                title = { Text("They're gone for now") },
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
