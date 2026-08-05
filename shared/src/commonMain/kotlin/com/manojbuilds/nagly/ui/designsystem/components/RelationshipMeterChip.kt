package com.manojbuilds.nagly.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.RelationshipLevel
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing

@Composable
fun RelationshipMeterChip(
    level: RelationshipLevel,
    progressToNext: Float,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNaglyColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card.copy(alpha = 0.7f))
            .padding(horizontal = NaglySpacing.sm, vertical = NaglySpacing.xs),
    ) {
        Text(
            text = "${level.emoji} ${level.label}",
            style = MaterialTheme.typography.labelLarge,
            color = colors.textPrimary,
        )
        LinearProgressIndicator(
            progress = { progressToNext.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NaglySpacing.xxs)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = colors.accent,
            trackColor = colors.outline.copy(alpha = 0.2f),
        )
    }
}
