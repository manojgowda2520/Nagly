package com.manojbuilds.nagly.ui.navigation

sealed interface Screen {
    data object Today : Screen
    data object History : Screen
    data object Personas : Screen
    data object Settings : Screen
    data object Onboarding : Screen
    data object Paywall : Screen
}

enum class MainTab(val label: String, val emoji: String) {
    Today("Today", "💧"),
    History("History", "📅"),
    Personas("Personas", "💬"),
    Settings("Settings", "⚙️"),
}

fun MainTab.toScreen(): Screen = when (this) {
    MainTab.Today -> Screen.Today
    MainTab.History -> Screen.History
    MainTab.Personas -> Screen.Personas
    MainTab.Settings -> Screen.Settings
}

fun Screen.toMainTabOrNull(): MainTab? = when (this) {
    Screen.Today -> MainTab.Today
    Screen.History -> MainTab.History
    Screen.Personas -> MainTab.Personas
    Screen.Settings -> MainTab.Settings
    Screen.Onboarding, Screen.Paywall -> null
}
