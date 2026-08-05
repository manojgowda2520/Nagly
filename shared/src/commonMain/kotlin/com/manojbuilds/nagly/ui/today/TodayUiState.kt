package com.manojbuilds.nagly.ui.today

import com.manojbuilds.nagly.domain.model.DrinkLog
import com.manojbuilds.nagly.domain.model.Mood

data class TodayUiState(
    val personaName: String = "",
    val personaEmoji: String = "",
    val personaLine: String = "",
    val mood: Mood = Mood.NEUTRAL,
    val consumedMl: Int = 0,
    val dailyMl: Int = 2000,
    val behindMl: Int = 0,
    val streak: Int = 0,
    val progress: Float = 0f,
    val expectedProgress: Float = 0f,
    val guiltProgress: Float = 0.3f,
    val hourOfDay: Int = 12,
    val canUndo: Boolean = false,
    val isLoading: Boolean = true,
    val drinks: List<DrinkLog> = emptyList(),
    val recentCustomMl: Int? = null,
    val nextNudgeLabel: String = "",
)
