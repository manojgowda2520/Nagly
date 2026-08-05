package com.manojbuilds.nagly.ui.persona

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.countdownLabel
import com.manojbuilds.nagly.domain.isRelationshipAccessible
import com.manojbuilds.nagly.domain.model.DayPart
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.model.Relationship
import com.manojbuilds.nagly.domain.model.Tier
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors
import com.manojbuilds.nagly.ui.designsystem.NaglyShapes
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import com.manojbuilds.nagly.ui.designsystem.components.SpeechBubbleSimple

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RelationshipGrid(
    selectedRelationshipId: String?,
    unlockExpiries: Map<String, Long>,
    isPro: Boolean,
    onSelect: (String) -> Unit,
    onLockedClick: (Relationship) -> Unit,
    nowMs: Long,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNaglyColors.current
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NaglySpacing.sm),
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.md),
    ) {
        PersonaCatalog.relationships.forEachIndexed { index, relationship ->
            val accessible = isRelationshipAccessible(
                relationship.id,
                isPro,
                unlockExpiries,
                nowMs,
            )
            val selected = relationship.id == selectedRelationshipId
            val expires = unlockExpiries[relationship.id]
            val rotation = if (index % 2 == 0) -2.5f else 2.5f
            PolaroidCard(
                emoji = relationship.emoji,
                title = relationship.displayName,
                tier = relationship.tier,
                accessible = accessible,
                selected = selected,
                expires = expires,
                nowMs = nowMs,
                rotation = rotation,
                userIsPro = isPro,
                onClick = {
                    if (accessible) onSelect(relationship.id) else onLockedClick(relationship)
                },
                colors = colors,
            )
        }
    }
}

@Composable
private fun PolaroidCard(
    emoji: String,
    title: String,
    tier: Tier,
    accessible: Boolean,
    selected: Boolean,
    expires: Long?,
    nowMs: Long,
    rotation: Float,
    userIsPro: Boolean,
    onClick: () -> Unit,
    colors: com.manojbuilds.nagly.ui.designsystem.NaglyColors,
) {
    val borderColor = when {
        selected -> colors.primary
        !accessible -> colors.warning.copy(alpha = 0.6f)
        else -> colors.outline.copy(alpha = 0.3f)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(150.dp)
            .rotate(rotation)
            .shadow(if (selected) 8.dp else 4.dp, NaglyShapes.card)
            .clip(NaglyShapes.card)
            .background(colors.card)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = NaglyShapes.card,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(NaglySpacing.sm),
    ) {
        TierBadge(tier = tier, accessible = accessible, userIsPro = userIsPro)
        Text(
            emoji,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(vertical = NaglySpacing.xs),
        )
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        when {
            selected -> {
                Text(
                    "Active",
                    color = colors.primary,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = NaglySpacing.xxs),
                )
            }
            expires != null && expires > nowMs && tier == Tier.PRO -> {
                Text(
                    countdownLabel(expires, nowMs),
                    color = colors.accent,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = NaglySpacing.xxs),
                )
            }
            !accessible && tier == Tier.PRO -> {
                BorrowTicketStub(modifier = Modifier.padding(top = NaglySpacing.xs))
            }
        }
    }
}

@Composable
private fun TierBadge(tier: Tier, accessible: Boolean, userIsPro: Boolean) {
    val colors = LocalNaglyColors.current
    val (label, bg) = when {
        tier == Tier.FREE -> "Free" to colors.success.copy(alpha = 0.2f)
        userIsPro -> "Pro" to colors.primary.copy(alpha = 0.2f)
        accessible -> "Pro" to colors.primary.copy(alpha = 0.2f)
        else -> "Watch Ad 🎬" to colors.warning.copy(alpha = 0.2f)
    }
    Box(
        modifier = Modifier
            .clip(NaglyShapes.pill)
            .background(bg)
            .padding(horizontal = NaglySpacing.xs, vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textPrimary)
    }
}

@Composable
private fun BorrowTicketStub(modifier: Modifier = Modifier) {
    val colors = LocalNaglyColors.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(colors.warning.copy(alpha = 0.15f))
            .border(1.dp, colors.warning.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = NaglySpacing.xs, vertical = NaglySpacing.xxs),
    ) {
        Text(
            "Borrow for 24h",
            style = MaterialTheme.typography.labelMedium,
            color = colors.warning,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun VariantList(
    relationshipId: String,
    selectedId: String?,
    unlockExpiries: Map<String, Long>,
    isPro: Boolean,
    onSelect: (String) -> Unit,
    onLockedClick: (Persona) -> Unit,
    canSelect: (Persona) -> Boolean,
    nowMs: Long,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.sm),
    ) {
        items(PersonaCatalog.variantsOf(relationshipId), key = { it.id }) { persona ->
            VariantCard(
                persona = persona,
                selected = persona.id == selectedId,
                unlocked = canSelect(persona),
                expires = unlockExpiries[persona.relationshipId],
                isPro = isPro,
                nowMs = nowMs,
                onClick = {
                    if (canSelect(persona)) onSelect(persona.id) else onLockedClick(persona)
                },
            )
        }
    }
}

@Composable
fun VariantCard(
    persona: Persona,
    selected: Boolean,
    unlocked: Boolean,
    expires: Long?,
    isPro: Boolean,
    nowMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNaglyColors.current
    val lines = remember(persona.id) {
        PersonaCatalog.linesFor(persona, Mood.NEUTRAL, DayPart.ANYTIME).ifEmpty {
            PersonaCatalog.linesFor(persona, Mood.NEUTRAL, DayPart.AFTERNOON)
        }
    }
    var lineIndex by remember(persona.id) { mutableIntStateOf(0) }
    val previewLine = lines.getOrElse(lineIndex % lines.size.coerceAtLeast(1)) { "Hello!" }

    val borderColor = when {
        selected -> colors.primary
        !unlocked -> colors.warning.copy(alpha = 0.6f)
        else -> colors.outline.copy(alpha = 0.3f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (selected) 6.dp else 3.dp, NaglyShapes.card)
            .clip(NaglyShapes.card)
            .background(colors.card)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = NaglyShapes.card,
            )
            .padding(NaglySpacing.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        ) {
            Text(
                text = persona.emoji,
                style = MaterialTheme.typography.headlineMedium,
            )
            Column(modifier = Modifier.weight(1f).padding(start = NaglySpacing.xs)) {
                Text(
                    persona.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                )
                TierBadge(
                    tier = if (PersonaCatalog.isPro(persona)) Tier.PRO else Tier.FREE,
                    accessible = unlocked,
                    userIsPro = isPro,
                )
            }
            when {
                selected -> {
                    Text(
                        "Active",
                        color = colors.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                expires != null && expires > nowMs && PersonaCatalog.isPro(persona) -> {
                    Text(
                        countdownLabel(expires, nowMs),
                        color = colors.accent,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                PersonaCatalog.isPro(persona) && !unlocked -> {
                    BorrowTicketStub()
                }
            }
        }

        SpeechBubbleSimple(
            text = "\"$previewLine\"",
            textStyle = MaterialTheme.typography.bodyLarge,
            textColor = colors.textPrimary,
            backgroundColor = colors.background,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NaglySpacing.sm)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (lines.size > 1) lineIndex = (lineIndex + 1) % lines.size
                    },
                ),
        )
        if (lines.size > 1) {
            Text(
                "Tap bubble to hear more",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                modifier = Modifier.padding(top = NaglySpacing.xxs),
            )
        }
    }
}

fun previewPersonaForRelationship(relationshipId: String): Persona =
    PersonaCatalog.variantsOf(relationshipId).first()
