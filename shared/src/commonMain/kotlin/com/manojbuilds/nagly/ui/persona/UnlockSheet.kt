package com.manojbuilds.nagly.ui.persona

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.showAdUnlockOption

@Composable
fun UnlockSheet(
    persona: Persona,
    isPro: Boolean,
    watchingAd: Boolean,
    onWatchAd: () -> Unit,
    onGoPro: () -> Unit,
    onDismiss: () -> Unit,
) {
    val preview = persona.lines.getValue(Mood.NEUTRAL).first()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${persona.emoji} ${persona.displayName}") },
        text = {
            Column {
                Text(
                    text = "\"$preview\"",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "She's locked for free accounts — borrow her for a day, or keep her forever.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showAdUnlockOption(isPro)) {
                    Button(
                        onClick = onWatchAd,
                        enabled = !watchingAd,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (watchingAd) {
                                "Playing ad..."
                            } else {
                                "Watch an ad — 24 hours with ${persona.displayName}"
                            },
                        )
                    }
                }
                OutlinedButton(
                    onClick = onGoPro,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    Text("Go Pro — keep her forever")
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Not now")
                }
            }
        },
        dismissButton = {},
    )
}

@Composable
fun FakeAdOverlay(
    personaName: String,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Sandbox rewarded ad") },
        text = {
            Text("Pretend commercial for unlocking $personaName. Hang tight ~3 seconds.")
        },
        confirmButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        },
    )
}
