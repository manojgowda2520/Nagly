package com.manojbuilds.nagly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

/**
 * Screen back stack for in-app navigation.
 *
 * iOS uses Compose [androidx.compose.ui.backhandler.BackHandler] (ui-backhandler), which hooks
 * swipe-from-edge / predictive back when the host supports it. It does not replace a native
 * UINavigationController stack; persona variant drill-down stays in local state inside
 * [com.manojbuilds.nagly.ui.persona.PersonaPickerScreen].
 */
class NavBackStack(initial: Screen) {
    private val stack: SnapshotStateList<Screen> = mutableStateListOf(initial)

    val current: Screen
        get() = stack.last()

    val canPop: Boolean
        get() = stack.size > 1

    fun push(screen: Screen) {
        stack.add(screen)
    }

    fun pop(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    fun resetTo(screen: Screen) {
        stack.clear()
        stack.add(screen)
    }

    fun navigateToTab(tab: MainTab) {
        resetTo(tab.toScreen())
    }
}

@Composable
fun rememberNavBackStack(initial: Screen): NavBackStack = remember {
    NavBackStack(initial)
}
