package com.manojbuilds.nagly.ui.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.billing.FakeBillingRepository
import com.manojbuilds.nagly.domain.PersonaCatalog
import com.manojbuilds.nagly.ui.designsystem.NaglySpacing
import com.manojbuilds.nagly.ui.designsystem.components.NaglyCardOutlined
import com.manojbuilds.nagly.ui.designsystem.components.PillButton

@Composable
fun PaywallScreen(
    purchasing: Boolean,
    onPurchase: (String) -> Unit,
    onRestore: () -> Unit,
    onClose: () -> Unit,
) {
    var selected by remember { mutableStateOf(FakeBillingRepository.PACKAGE_ANNUAL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(NaglySpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onClose) { Text("Close") }
        }

        Text(
            "Unlock every nagger",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            "Free forever for logging. Pro is just more personalities.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = NaglySpacing.xs, bottom = NaglySpacing.md - 4.dp),
        )

        Text("You'll get", style = MaterialTheme.typography.titleLarge)
        PersonaCatalog.pro.forEach { persona ->
            Text(
                text = "${persona.emoji}  ${persona.displayName}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = NaglySpacing.xs),
            )
        }

        Column(
            modifier = Modifier.padding(top = NaglySpacing.lg - 4.dp),
            verticalArrangement = Arrangement.spacedBy(NaglySpacing.xs + 4.dp),
        ) {
            PlanCard(
                title = "Annual",
                subtitle = "Best value · \$29.99/year",
                selected = selected == FakeBillingRepository.PACKAGE_ANNUAL,
                badge = "Best value",
                onClick = { selected = FakeBillingRepository.PACKAGE_ANNUAL },
            )
            PlanCard(
                title = "Monthly",
                subtitle = "\$4.99/month",
                selected = selected == FakeBillingRepository.PACKAGE_MONTHLY,
                badge = null,
                onClick = { selected = FakeBillingRepository.PACKAGE_MONTHLY },
            )
        }

        PillButton(
            onClick = { onPurchase(selected) },
            enabled = !purchasing,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NaglySpacing.md),
        ) {
            Text(if (purchasing) "Working on it..." else "Go Pro")
        }

        TextButton(
            onClick = onRestore,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Restore purchases")
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    badge: String?,
    onClick: () -> Unit,
) {
    NaglyCardOutlined(
        modifier = Modifier.fillMaxWidth(),
        selected = selected,
        onClick = onClick,
    ) {
        Row {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            if (badge != null) {
                Text(
                    badge,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = NaglySpacing.xxs),
        )
    }
}
