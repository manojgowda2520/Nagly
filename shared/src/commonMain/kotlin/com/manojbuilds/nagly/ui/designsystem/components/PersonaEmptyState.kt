package com.manojbuilds.nagly.ui.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing

@Composable
fun PersonaEmptyState(
    personaEmoji: String,
    personaName: String,
    line: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNaglyColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(NaglySpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = personaEmoji,
            style = MaterialTheme.typography.displayMedium,
        )
        Text(
            text = personaName,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            modifier = Modifier.padding(top = NaglySpacing.xs),
        )
        SpeechBubbleSimple(
            text = "\"$line\"",
            textStyle = MaterialTheme.typography.bodyLarge,
            textColor = colors.textPrimary,
            backgroundColor = colors.card,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NaglySpacing.md),
        )
        Text(
            text = "Someone who cares.",
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = NaglySpacing.sm),
        )
    }
}
