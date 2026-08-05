package com.manojbuilds.nagly.ui.persona

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.countdownLabel
import com.manojbuilds.nagly.domain.isRelationshipAccessible
import com.manojbuilds.nagly.domain.model.DayPart
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.model.Relationship
import com.manojbuilds.nagly.domain.model.Tier
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import com.manojbuilds.nagly.ui.designsystem.components.NaglyCardOutlined

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
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NaglySpacing.xs + 4.dp),
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.xs + 4.dp),
    ) {
        PersonaCatalog.relationships.forEach { relationship ->
            val accessible = isRelationshipAccessible(
                relationship.id,
                isPro,
                unlockExpiries,
                nowMs,
            )
            val selected = relationship.id == selectedRelationshipId
            val expires = unlockExpiries[relationship.id]
            NaglyCardOutlined(
                modifier = Modifier,
                selected = selected,
                contentPadding = NaglySpacing.sm,
                onClick = {
                    if (accessible) onSelect(relationship.id) else onLockedClick(relationship)
                },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(relationship.emoji, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        relationship.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = NaglySpacing.xxs),
                    )
                    when {
                        expires != null && expires > nowMs && relationship.tier == Tier.PRO -> {
                            Text(
                                countdownLabel(expires, nowMs),
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(top = NaglySpacing.xxs),
                            )
                        }
                        relationship.tier == Tier.PRO && !accessible -> {
                            Text(
                                "🔒",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(top = NaglySpacing.xxs),
                            )
                        }
                    }
                }
            }
        }
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
        verticalArrangement = Arrangement.spacedBy(NaglySpacing.xs + 4.dp),
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
    val preview = PersonaCatalog.linesFor(persona, Mood.NEUTRAL, DayPart.ANYTIME).first()
    NaglyCardOutlined(
        modifier = modifier.fillMaxWidth(),
        selected = selected,
        onClick = onClick,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${persona.emoji}  ${persona.displayName}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            when {
                selected -> {
                    Text(
                        "Active",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                expires != null && expires > nowMs && PersonaCatalog.isPro(persona) -> {
                    Text(
                        countdownLabel(expires, nowMs),
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                PersonaCatalog.isPro(persona) && !unlocked -> {
                    Text(
                        "🔒",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Text(
            text = "\"$preview\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = NaglySpacing.xs),
        )
    }
}

fun previewPersonaForRelationship(relationshipId: String): Persona =
    PersonaCatalog.variantsOf(relationshipId).first()
