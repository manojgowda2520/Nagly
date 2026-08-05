package com.manojbuilds.nagly.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors

@Composable
fun MainShell(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    content: @Composable () -> Unit,
) {
    val naglyColors = LocalNaglyColors.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                tonalElevation = 2.dp,
                containerColor = naglyColors.card,
            ) {
                MainTab.entries.forEach { tab ->
                    val selected = tab == selectedTab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onTabSelected(tab) },
                        icon = {
                            Text(
                                text = tab.emoji,
                                fontSize = if (selected) 22.sp else 18.sp,
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = naglyColors.primary.copy(alpha = 0.18f),
                            selectedIconColor = naglyColors.primary,
                            selectedTextColor = naglyColors.textPrimary,
                            unselectedIconColor = naglyColors.textSecondary,
                            unselectedTextColor = naglyColors.textSecondary,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            content()
        }
    }
}
