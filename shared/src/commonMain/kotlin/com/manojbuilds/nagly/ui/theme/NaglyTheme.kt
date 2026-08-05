package com.manojbuilds.nagly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.manojbuilds.nagly.ui.designsystem.NaglyColors
import com.manojbuilds.nagly.ui.designsystem.LocalNaglyColors

fun NaglyColors.toMaterialColorScheme(isDark: Boolean) = if (isDark) {
    darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = primary.copy(alpha = 0.85f),
        onSecondary = onPrimary,
        tertiary = accent,
        onTertiary = onPrimary,
        background = background,
        onBackground = textPrimary,
        surface = card,
        onSurface = textPrimary,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = textSecondary,
        outline = outline,
        error = danger,
        onError = Color.White,
        primaryContainer = primary.copy(alpha = 0.18f),
        onPrimaryContainer = textPrimary,
    )
} else {
    lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        secondary = primary.copy(alpha = 0.75f),
        onSecondary = onPrimary,
        tertiary = accent,
        onTertiary = onPrimary,
        background = background,
        onBackground = textPrimary,
        surface = card,
        onSurface = textPrimary,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = textSecondary,
        outline = outline,
        error = danger,
        onError = Color.White,
        primaryContainer = primary.copy(alpha = 0.15f),
        onPrimaryContainer = textPrimary,
    )
}

private val NaglyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 56.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 42.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        lineHeight = 23.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
)

@Composable
fun NaglyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val naglyColors = if (darkTheme) NaglyColors.Dark else NaglyColors.Light
    CompositionLocalProvider(LocalNaglyColors provides naglyColors) {
        MaterialTheme(
            colorScheme = naglyColors.toMaterialColorScheme(darkTheme),
            typography = NaglyTypography,
            content = content,
        )
    }
}
