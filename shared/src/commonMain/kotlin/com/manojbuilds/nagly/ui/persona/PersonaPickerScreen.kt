package com.manojbuilds.nagly.ui.persona

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.countdownLabel
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import kotlin.time.Clock

@Composable
fun PersonaPickerScreen(
    selectedId: String,
    unlockExpiries: Map<String, Long>,
    onSelect: (String) -> Unit,
    canSelect: (Persona) -> Boolean,
    onLockedClick: (Persona) -> Unit,
    onBack: () -> Unit,
    nowMs: Long = Clock.System.now().toEpochMilliseconds(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        TextButton(onClick = onBack) { Text("Back") }
        Text(
            "Pick your nagger",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(PersonaCatalog.all, key = { it.id }) { persona ->
                val unlocked = canSelect(persona)
                val selected = persona.id == selectedId
                val expires = unlockExpiries[persona.id]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            shape = RoundedCornerShape(18.dp),
                        )
                        .clickable {
                            if (unlocked) onSelect(persona.id) else onLockedClick(persona)
                        }
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${persona.emoji}  ${persona.displayName}",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                        )
                        when {
                            expires != null && expires > nowMs && persona.isPro -> {
                                Text(
                                    countdownLabel(expires, nowMs),
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                            persona.isPro && !unlocked -> {
                                Text(
                                    "Pro",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                    Text(
                        text = persona.lines.getValue(Mood.NEUTRAL).first(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}
