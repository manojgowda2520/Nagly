package com.manojbuilds.nagly.ui.persona

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.domain.model.DayPart
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.showAdUnlockOption
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import com.manojbuilds.nagly.ui.designsystem.components.PillButton
import com.manojbuilds.nagly.ui.designsystem.components.PillButtonVariant
import com.manojbuilds.nagly.ui.designsystem.components.SpeechBubbleSimple

@Composable
fun UnlockSheet(
    persona: Persona,
    isPro: Boolean,
    watchingAd: Boolean,
    onWatchAd: () -> Unit,
    onGoPro: () -> Unit,
    onDismiss: () -> Unit,
) {
    val preview = PersonaCatalog.linesFor(persona, Mood.NEUTRAL, DayPart.ANYTIME).first()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${persona.emoji} ${persona.displayName}") },
        text = {
            Column {
                SpeechBubbleSimple(
                    text = "\"$preview\"",
                    textStyle = MaterialTheme.typography.titleLarge,
                    textColor = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "She's locked for free accounts — borrow her for a day, or keep her forever.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = NaglySpacing.xs + 4.dp),
                )
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showAdUnlockOption(isPro)) {
                    PillButton(
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
                PillButton(
                    onClick = onGoPro,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = NaglySpacing.xs),
                    variant = PillButtonVariant.Outlined,
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
