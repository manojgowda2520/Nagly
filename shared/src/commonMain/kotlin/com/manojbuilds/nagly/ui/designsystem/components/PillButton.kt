package com.manojbuilds.nagly.ui.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.manojbuilds.nagly.ui.designsystem.NaglyMotion
import com.manojbuilds.nagly.ui.designsystem.NaglyShapes
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors

enum class PillButtonVariant {
    Primary,
    Accent,
    Outlined,
}

@Composable
fun PillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PillButtonVariant = PillButtonVariant.Primary,
    enabled: Boolean = true,
    shape: Shape = NaglyShapes.pill,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit,
) {
    val colors = LocalNaglyColors.current
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = NaglyMotion.tweenNormal(),
        label = "pillButtonScale",
    )
    val scaledModifier = modifier
        .defaultMinSize(minHeight = NaglyShapes.minTapTarget)
        .scale(scale)

    when (variant) {
        PillButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = scaledModifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                content = content,
            )
        }
        PillButtonVariant.Accent -> {
            Button(
                onClick = onClick,
                modifier = scaledModifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onPrimary,
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                content = content,
            )
        }
        PillButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = scaledModifier,
                enabled = enabled,
                shape = shape,
                border = BorderStroke(1.dp, colors.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.textPrimary,
                ),
                contentPadding = contentPadding,
                interactionSource = interactionSource,
                content = content,
            )
        }
    }
}
