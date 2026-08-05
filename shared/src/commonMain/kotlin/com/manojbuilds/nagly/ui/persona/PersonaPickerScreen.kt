package com.manojbuilds.nagly.ui.persona

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.ui.onboarding.PersonaStep

@Composable
fun PersonaPickerScreen(
    selectedId: String,
    onSelect: (String) -> Unit,
    canSelect: (Persona) -> Boolean,
    onLockedClick: (Persona) -> Unit,
    onBack: () -> Unit,
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
        PersonaStep(
            selectedId = selectedId,
            onSelect = onSelect,
            canSelect = canSelect,
            onLockedClick = onLockedClick,
        )
    }
}
