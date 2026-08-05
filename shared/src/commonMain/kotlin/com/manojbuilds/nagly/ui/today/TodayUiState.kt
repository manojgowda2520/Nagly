package com.manojbuilds.nagly.ui.today

import com.manojbuilds.nagly.domain.model.Mood

data class TodayUiState(
    val personaName: String = "",
    val personaEmoji: String = "",
    val personaLine: String = "",
    val mood: Mood = Mood.NEUTRAL,
    val consumedMl: Int = 0,
    val dailyMl: Int = 2000,
    val streak: Int = 0,
    val progress: Float = 0f,
    val canUndo: Boolean = false,
    val isLoading: Boolean = true,
)
