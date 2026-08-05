package com.manojbuilds.nagly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val WarmCoral = Color(0xFFE07A5F)
private val DeepTeal = Color(0xFF3D5A5B)
private val SoftCream = Color(0xFFFFF6EF)
private val Ink = Color(0xFF2B2118)
private val SoftBlush = Color(0xFFF2C6B8)
private val NightInk = Color(0xFF1A1410)
private val NightSurface = Color(0xFF2A211C)
private val NightCoral = Color(0xFFFF9B82)
private val NightTeal = Color(0xFF8FB4B5)

private val LightColors = lightColorScheme(
    primary = WarmCoral,
    onPrimary = Color.White,
    secondary = DeepTeal,
    onSecondary = Color.White,
    tertiary = SoftBlush,
    background = SoftCream,
    onBackground = Ink,
    surface = Color(0xFFFFFCFA),
    onSurface = Ink,
    surfaceVariant = Color(0xFFF7E8DF),
    onSurfaceVariant = Color(0xFF5C4A3D),
    outline = Color(0xFFD7C2B4),
)

private val DarkColors = darkColorScheme(
    primary = NightCoral,
    onPrimary = NightInk,
    secondary = NightTeal,
    onSecondary = NightInk,
    tertiary = SoftBlush,
    background = NightInk,
    onBackground = Color(0xFFF8EFE7),
    surface = NightSurface,
    onSurface = Color(0xFFF8EFE7),
    surfaceVariant = Color(0xFF3A2E27),
    onSurfaceVariant = Color(0xFFD8C4B6),
    outline = Color(0xFF6A5649),
)

private val NaglyTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
    ),
)

@Composable
fun NaglyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = NaglyTypography,
        content = content,
    )
}
