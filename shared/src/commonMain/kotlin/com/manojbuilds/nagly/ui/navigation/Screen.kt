package com.manojbuilds.nagly.ui.navigation

sealed interface Screen {
    data object Today : Screen
    data object History : Screen
    data object PersonaPicker : Screen
    data object Onboarding : Screen
    data object Paywall : Screen
}
