package com.manojbuilds.nagly.domain.model

data class DrinkLog(
    val id: Long,
    val timestampMs: Long,
    val amountMl: Int,
)

data class UserGoal(
    val dailyMl: Int,
    val wakeHour: Int,
    val sleepHour: Int,
    val personaId: String,
    val onboarded: Boolean,
)

enum class Mood {
    NEUTRAL,
    WORRIED,
    DISAPPOINTED,
    PROUD,
}
