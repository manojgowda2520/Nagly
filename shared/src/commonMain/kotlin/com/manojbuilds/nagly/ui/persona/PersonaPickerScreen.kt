package com.manojbuilds.nagly.ui.persona

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.model.Relationship
import kotlin.time.Clock

@Composable
fun PersonaPickerScreen(
    selectedId: String,
    unlockExpiries: Map<String, Long>,
    isPro: Boolean,
    onSelect: (String) -> Unit,
    canSelect: (Persona) -> Boolean,
    onLockedClick: (Persona) -> Unit,
    onLockedRelationship: (Relationship) -> Unit = { relationship ->
        onLockedClick(previewPersonaForRelationship(relationship.id))
    },
    nowMs: Long = Clock.System.now().toEpochMilliseconds(),
) {
    var relationshipId by rememberSaveable {
        mutableStateOf(PersonaCatalog.relationshipOf(PersonaCatalog.get(selectedId)).id)
    }
    var showVariants by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            if (showVariants) "Pick their vibe" else "Pick your nagger",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        if (showVariants) {
            TextButton(onClick = { showVariants = false }) {
                Text("← All relationships")
            }
            VariantList(
                relationshipId = relationshipId,
                selectedId = selectedId,
                unlockExpiries = unlockExpiries,
                isPro = isPro,
                onSelect = onSelect,
                onLockedClick = onLockedClick,
                canSelect = canSelect,
                nowMs = nowMs,
            )
        } else {
            RelationshipGrid(
                selectedRelationshipId = relationshipId,
                unlockExpiries = unlockExpiries,
                isPro = isPro,
                onSelect = { id ->
                    relationshipId = id
                    showVariants = true
                },
                onLockedClick = onLockedRelationship,
                nowMs = nowMs,
            )
        }
    }
}
