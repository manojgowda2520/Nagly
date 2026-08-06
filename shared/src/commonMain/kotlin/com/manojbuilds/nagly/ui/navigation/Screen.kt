package com.manojbuilds.nagly.ui.navigation

sealed interface Screen {
    data object Today : Screen
    data object History : Screen
    data object Personas : Screen
    data object Insights : Screen
    data object Settings : Screen
    data object Onboarding : Screen
    data object Paywall : Screen
}

enum class MainTab(val label: String, val emoji: String) {
    Home("Home", "🏠"),
    History("History", "📅"),
    Characters("Chars", "💬"),
    Insights("Insights", "📊"),
    Profile("Profile", "👤"),
}

fun MainTab.toScreen(): Screen = when (this) {
    MainTab.Home -> Screen.Today
    MainTab.History -> Screen.History
    MainTab.Characters -> Screen.Personas
    MainTab.Insights -> Screen.Insights
    MainTab.Profile -> Screen.Settings
}

fun Screen.toMainTabOrNull(): MainTab? = when (this) {
    Screen.Today -> MainTab.Home
    Screen.History -> MainTab.History
    Screen.Personas -> MainTab.Characters
    Screen.Insights -> MainTab.Insights
    Screen.Settings -> MainTab.Profile
    Screen.Onboarding, Screen.Paywall -> null
}
