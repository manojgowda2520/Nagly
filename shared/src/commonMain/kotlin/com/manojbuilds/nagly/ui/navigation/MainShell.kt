package com.manojbuilds.nagly.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing

@Composable
fun MainShell(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    onQuickAddWater: () -> Unit,
    content: @Composable () -> Unit,
) {
    val naglyColors = LocalNaglyColors.current
    val leftTabs = listOf(MainTab.Home, MainTab.History)
    val rightTabs = listOf(MainTab.Characters, MainTab.Insights, MainTab.Profile)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onQuickAddWater,
                shape = CircleShape,
                containerColor = naglyColors.accent,
                contentColor = naglyColors.onPrimary,
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = 28.dp),
            ) {
                Text("💧", fontSize = 24.sp)
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(naglyColors.card)
                    .padding(horizontal = NaglySpacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leftTabs.forEach { tab ->
                    BottomNavItem(
                        tab = tab,
                        selected = tab == selectedTab,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Box(modifier = Modifier.weight(1f))
                rightTabs.forEach { tab ->
                    BottomNavItem(
                        tab = tab,
                        selected = tab == selectedTab,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier.weight(1f),
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

@Composable
private fun BottomNavItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val naglyColors = LocalNaglyColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = NaglySpacing.xxs),
    ) {
        Text(
            text = tab.emoji,
            fontSize = if (selected) 20.sp else 17.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) naglyColors.primary else naglyColors.textSecondary,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
        )
    }
}
