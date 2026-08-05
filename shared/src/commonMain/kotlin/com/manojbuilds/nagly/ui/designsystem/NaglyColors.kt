package com.manojbuilds.nagly.ui.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.manojbuilds.nagly.domain.model.Mood

data class NaglyColors(
    val background: Color,
    val card: Color,
    val primary: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val moodHappy: Color,
    val moodConcerned: Color,
    val moodSad: Color,
    val moodDisappointed: Color,
    val moodProud: Color,
    val outline: Color,
    val surfaceVariant: Color,
    val onPrimary: Color,
) {
    companion object {
        val Light = NaglyColors(
            background = Color(0xFFFAFBFC),
            card = Color(0xFFFFFFFF),
            primary = Color(0xFF4FC3F7),
            accent = Color(0xFFFF8A65),
            success = Color(0xFF4CAF50),
            warning = Color(0xFFFFC107),
            danger = Color(0xFFEF5350),
            textPrimary = Color(0xFF212121),
            textSecondary = Color(0xFF757575),
            moodHappy = Color(0xFFA5D6A7),
            moodConcerned = Color(0xFFFFE082),
            moodSad = Color(0xFF90A4AE),
            moodDisappointed = Color(0xFFB0BEC5),
            moodProud = Color(0xFF81D4FA),
            outline = Color(0xFFE0E0E0),
            surfaceVariant = Color(0xFFF0F4F8),
            onPrimary = Color.White,
        )

        val Dark = NaglyColors(
            background = Color(0xFF0E1A24),
            card = Color(0xFF1A2836),
            primary = Color(0xFF81D4FA),
            accent = Color(0xFFFFAB91),
            success = Color(0xFF66BB6A),
            warning = Color(0xFFFFD54F),
            danger = Color(0xFFEF5350),
            textPrimary = Color(0xFFECEFF1),
            textSecondary = Color(0xFF90A4AE),
            moodHappy = Color(0xFFA5D6A7),
            moodConcerned = Color(0xFFFFE082),
            moodSad = Color(0xFF78909C),
            moodDisappointed = Color(0xFF90A4AE),
            moodProud = Color(0xFF4FC3F7),
            outline = Color(0xFF37474F),
            surfaceVariant = Color(0xFF162029),
            onPrimary = Color(0xFF0E1A24),
        )
    }
}

val LocalNaglyColors = staticCompositionLocalOf { NaglyColors.Light }

fun Mood.moodColor(colors: NaglyColors): Color = when (this) {
    Mood.NEUTRAL -> colors.moodHappy
    Mood.WORRIED -> colors.moodConcerned
    Mood.DISAPPOINTED -> colors.moodDisappointed
    Mood.PROUD -> colors.moodProud
}
