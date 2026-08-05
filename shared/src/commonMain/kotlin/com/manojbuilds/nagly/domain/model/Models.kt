package com.manojbuilds.nagly.domain.model

data class DrinkLog(
    val id: Long,
    val timestampMs: Long,
    val amountMl: Int,
)

enum class ActivityLevel {
    SEDENTARY,
    LIGHT,
    ACTIVE,
}

enum class VolumeUnit {
    ML,
    OZ,
}

data class UserGoal(
    val dailyMl: Int,
    val wakeHour: Int,
    val sleepHour: Int,
    val personaId: String,
    val onboarded: Boolean,
    val volumeUnit: VolumeUnit = VolumeUnit.ML,
)

enum class Mood {
    NEUTRAL,
    WORRIED,
    DISAPPOINTED,
    PROUD,
}
