package com.manojbuilds.nagly.domain

import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Persona
import com.manojbuilds.nagly.domain.model.UserGoal

const val TEMP_UNLOCK_MS: Long = 24L * 60L * 60L * 1000L

data class UnlockStatus(
    val relationshipId: String,
    val expiresAtMs: Long,
)

fun isPersonaAccessible(
    persona: Persona,
    isPro: Boolean,
    activeUnlocks: Map<String, Long>,
    nowMs: Long,
): Boolean {
    if (!PersonaCatalog.isPro(persona)) return true
    if (isPro) return true
    val expires = activeUnlocks[persona.relationshipId] ?: return false
    return expires > nowMs
}

fun isRelationshipAccessible(
    relationshipId: String,
    isPro: Boolean,
    activeUnlocks: Map<String, Long>,
    nowMs: Long,
): Boolean {
    val relationship = PersonaCatalog.relationship(relationshipId)
    if (relationship.tier == com.manojbuilds.nagly.domain.model.Tier.FREE) return true
    if (isPro) return true
    val expires = activeUnlocks[relationshipId] ?: return false
    return expires > nowMs
}

fun showAdUnlockOption(isPro: Boolean): Boolean = !isPro

fun resolveExpiredSelection(
    goal: UserGoal,
    activeUnlocks: Map<String, Long>,
    isPro: Boolean,
    nowMs: Long,
    freeFallbackId: String = "indian_mom",
): Pair<UserGoal, String?> {
    val persona = PersonaCatalog.get(goal.personaId)
    val stillOk = isPersonaAccessible(persona, isPro, activeUnlocks, nowMs)
    if (stillOk) return goal to null
    val fallback = PersonaCatalog.get(freeFallbackId)
    val message = pickLine(fallback, Mood.DISAPPOINTED)
    return goal.copy(personaId = freeFallbackId) to message
}

fun countdownLabel(expiresAtMs: Long, nowMs: Long): String {
    val remaining = (expiresAtMs - nowMs).coerceAtLeast(0L)
    val hours = remaining / (60L * 60L * 1000L)
    val minutes = (remaining % (60L * 60L * 1000L)) / (60L * 1000L)
    return if (hours > 0) "${hours}h ${minutes}m left" else "${minutes}m left"
}
