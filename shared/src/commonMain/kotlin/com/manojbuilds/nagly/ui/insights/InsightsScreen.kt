package com.manojbuilds.nagly.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import com.manojbuilds.nagly.ui.designsystem.components.NaglyCard
import com.manojbuilds.nagly.ui.designsystem.components.PersonaEmptyState

@Composable
fun InsightsScreen(state: InsightsUiState) {
    val colors = LocalNaglyColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(NaglySpacing.md),
    ) {
        Text(
            "Insights",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
        )
        Text(
            "${state.personaEmoji} ${state.personaName}'s weekly read",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = NaglySpacing.xxs, bottom = NaglySpacing.md),
        )

        if (!state.hasData) {
            PersonaEmptyState(
                personaEmoji = state.personaEmoji,
                personaName = state.personaName,
                line = state.emptyLine,
            )
        } else {
            state.cards.forEach { card ->
                NaglyCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = NaglySpacing.sm),
                    contentPadding = NaglySpacing.sm,
                ) {
                    Text(card.emoji, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        card.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = NaglySpacing.xxs),
                    )
                    Text(
                        card.value,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                    )
                    if (card.subtitle.isNotBlank()) {
                        Text(
                            card.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
