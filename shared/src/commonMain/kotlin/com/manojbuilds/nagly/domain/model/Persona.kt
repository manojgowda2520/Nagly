package com.manojbuilds.nagly.domain.model

enum class Tier { FREE, PRO }

enum class DayPart { MORNING, AFTERNOON, EVENING, ANYTIME }

data class Relationship(
    val id: String,
    val displayName: String,
    val emoji: String,
    val tier: Tier,
)

data class Persona(
    val id: String,
    val relationshipId: String,
    val displayName: String,
    val emoji: String,
    val bodyLines: Map<Mood, Map<DayPart, List<String>>>,
    val skipLabels: Map<Mood, List<String>>,
)
