package com.manojbuilds.nagly.domain.model

data class Persona(
    val id: String,
    val displayName: String,
    val emoji: String,
    val isPro: Boolean,
    val lines: Map<Mood, List<String>>,
)
