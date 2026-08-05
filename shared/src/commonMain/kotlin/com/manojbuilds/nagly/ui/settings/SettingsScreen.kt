package com.manojbuilds.nagly.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.config.Integrations
import com.manojbuilds.nagly.domain.model.VolumeUnit
import com.manojbuilds.nagly.domain.volumeUnitLabel
import com.manojbuilds.nagly.platform.PlatformActions
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onDailyGoalChange: (Int) -> Unit,
    onVolumeUnitChange: (VolumeUnit) -> Unit,
    onWakeChange: (Int) -> Unit,
    onSleepChange: (Int) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onOpenPersonas: () -> Unit,
    onRestorePurchases: () -> Unit,
    onRequestPermission: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    if (state.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        state.restoreMessage?.let { msg ->
            MessageBanner(msg, onDismissMessage)
        }
        state.permissionMessage?.let { msg ->
            MessageBanner(msg, onDismissMessage)
        }

        SectionTitle("Daily goal")
        Text(
            "${state.dailyDisplay} ${volumeUnitLabel(state.volumeUnit)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Slider(
            value = state.dailyDisplay.toFloat(),
            onValueChange = { onDailyGoalChange(it.roundToInt()) },
            valueRange = when (state.volumeUnit) {
                VolumeUnit.ML -> 500f..4000f
                VolumeUnit.OZ -> 17f..135f
            },
            steps = 20,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.volumeUnit == VolumeUnit.ML,
                onClick = { onVolumeUnitChange(VolumeUnit.ML) },
                label = { Text("ml") },
            )
            FilterChip(
                selected = state.volumeUnit == VolumeUnit.OZ,
                onClick = { onVolumeUnitChange(VolumeUnit.OZ) },
                label = { Text("oz") },
            )
        }

        SectionTitle("Waking hours")
        Text("Wake: ${formatHour(state.wakeHour)}", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = state.wakeHour.toFloat(),
            onValueChange = { onWakeChange(it.roundToInt()) },
            valueRange = 0f..23f,
            steps = 22,
        )
        Text("Sleep: ${formatHour(state.sleepHour)}", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = state.sleepHour.toFloat(),
            onValueChange = { onSleepChange(it.roundToInt()) },
            valueRange = 0f..23f,
            steps = 22,
        )

        SectionTitle("Persona")
        SettingsRow(
            title = state.personaLabel,
            subtitle = "Change who nags you",
            onClick = onOpenPersonas,
        )

        SectionTitle("Notifications")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Nudges enabled", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Quiet outside wake–sleep hours",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = state.notificationsEnabled, onCheckedChange = onNotificationsChange)
        }
        OutlinedButton(onClick = onRequestPermission, modifier = Modifier.fillMaxWidth()) {
            Text("Re-request notification permission")
        }

        SectionTitle("Subscription")
        OutlinedButton(
            onClick = onRestorePurchases,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isSaving) "Working…" else "Restore purchases")
        }
        if (state.isPro) {
            Text("Pro active", color = MaterialTheme.colorScheme.primary)
        }

        SectionTitle("Support & legal")
        SettingsRow("Privacy Policy") { PlatformActions.openUrl(Integrations.PRIVACY_URL) }
        SettingsRow("Terms of Use") { PlatformActions.openUrl(Integrations.TERMS_URL) }
        SettingsRow("Manage subscription") { PlatformActions.openUrl(Integrations.MANAGE_SUBSCRIPTION_URL) }
        SettingsRow("Rate Nagly") { PlatformActions.rateApp() }
        SettingsRow("Share Nagly") { PlatformActions.shareApp(Integrations.SHARE_MESSAGE) }
        SettingsRow("Contact support") {
            PlatformActions.openEmail(Integrations.SUPPORT_EMAIL, "Nagly support")
        }

        Text(
            "Version ${state.versionName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
}

@Composable
private fun MessageBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onDismiss)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Text("✕", style = MaterialTheme.typography.labelLarge)
    }
}

private fun formatHour(hour: Int): String {
    val h = ((hour + 11) % 12) + 1
    val suffix = if (hour < 12) "AM" else "PM"
    return "$h:00 $suffix"
}
