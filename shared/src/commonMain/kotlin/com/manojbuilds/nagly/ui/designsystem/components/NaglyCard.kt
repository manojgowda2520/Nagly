package com.manojbuilds.nagly.ui.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglyShapes
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing

@Composable
fun NaglyCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = NaglySpacing.sm,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalNaglyColors.current
    Card(
        modifier = modifier,
        shape = NaglyShapes.card,
        colors = CardDefaults.cardColors(containerColor = colors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        content = {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NaglyCardOutlined(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    contentPadding: Dp = NaglySpacing.sm,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalNaglyColors.current
    val borderColor = if (selected) colors.primary else colors.outline
    val borderWidth = if (selected) 2.dp else 1.dp
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = NaglyShapes.cardLarge,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                colors.primary.copy(alpha = 0.12f)
            } else {
                colors.card
            },
        ),
        border = BorderStroke(borderWidth, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp),
        content = {
            Column(modifier = Modifier.padding(contentPadding), content = content)
        },
    )
}
