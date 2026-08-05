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

// Teal-forward palette (#0E7C86 family) — warm character, not clinical
private val Teal = Color(0xFF0E7C86)
private val TealDeep = Color(0xFF0A5C63)
private val TealSoft = Color(0xFF5BA8B0)
private val Mist = Color(0xFFE8F4F5)
private val Foam = Color(0xFFF4FBFC)
private val Ink = Color(0xFF1A2E30)
private val CoralAccent = Color(0xFFE07A5F)

private val NightBg = Color(0xFF0C1A1C)
private val NightSurface = Color(0xFF152628)
private val NightTeal = Color(0xFF4DB8C2)
private val NightOn = Color(0xFFE8F4F5)

private val LightColors = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    secondary = TealDeep,
    onSecondary = Color.White,
    tertiary = CoralAccent,
    background = Foam,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Color(0xFF3D5A5B),
    outline = Color(0xFFB5D4D7),
)

private val DarkColors = darkColorScheme(
    primary = NightTeal,
    onPrimary = NightBg,
    secondary = TealSoft,
    onSecondary = NightBg,
    tertiary = CoralAccent,
    background = NightBg,
    onBackground = NightOn,
    surface = NightSurface,
    onSurface = NightOn,
    surfaceVariant = Color(0xFF1E3437),
    onSurfaceVariant = Color(0xFFB5D4D7),
    outline = Color(0xFF3D5A5B),
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
