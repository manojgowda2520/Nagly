package com.manojbuilds.nagly.domain

import com.manojbuilds.nagly.domain.model.UserGoal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UnlockLogicTest {

    private val proPersona = PersonaCatalog.get("italian_nonna")
    private val goal = UserGoal(2000, 7, 22, "italian_nonna", true)

    @Test
    fun grantWindow_accessibleBeforeExpiry_byRelationship() {
        val now = 1_000_000L
        val unlocks = mapOf("grandparent" to now + TEMP_UNLOCK_MS)
        assertTrue(isPersonaAccessible(proPersona, isPro = false, unlocks, now))
        assertTrue(
            isRelationshipAccessible("grandparent", isPro = false, unlocks, now),
        )
        // Sibling variant in same relationship also unlocked
        assertTrue(
            isPersonaAccessible(
                PersonaCatalog.get("dadi_nani"),
                isPro = false,
                unlocks,
                now,
            ),
        )
        assertFalse(isPersonaAccessible(proPersona, isPro = false, unlocks, now + TEMP_UNLOCK_MS + 1))
    }

    @Test
    fun personaUnlockKey_doesNotUnlockWrongRelationship() {
        val now = 1_000_000L
        val unlocks = mapOf("dad" to now + TEMP_UNLOCK_MS)
        assertFalse(isPersonaAccessible(proPersona, isPro = false, unlocks, now))
        assertTrue(
            isPersonaAccessible(
                PersonaCatalog.get("punjabi_dad"),
                isPro = false,
                unlocks,
                now,
            ),
        )
    }

    @Test
    fun proBypass_neverNeedsTempUnlock() {
        assertTrue(
            isPersonaAccessible(
                persona = proPersona,
                isPro = true,
                activeUnlocks = emptyMap(),
                nowMs = 0L,
            ),
        )
        assertFalse(showAdUnlockOption(isPro = true))
        assertTrue(showAdUnlockOption(isPro = false))
    }

    @Test
    fun expiry_revertsToFreePersonaWithMessage() {
        val now = 5_000L
        val (updated, message) = resolveExpiredSelection(
            goal = goal,
            activeUnlocks = mapOf("grandparent" to 1_000L),
            isPro = false,
            nowMs = now,
        )
        assertEquals("indian_mom", updated.personaId)
        assertTrue(!message.isNullOrBlank())
    }

    @Test
    fun expiry_noChangeWhenStillValid() {
        val now = 5_000L
        val (updated, message) = resolveExpiredSelection(
            goal = goal,
            activeUnlocks = mapOf("grandparent" to 10_000L),
            isPro = false,
            nowMs = now,
        )
        assertEquals("italian_nonna", updated.personaId)
        assertNull(message)
    }

    @Test
    fun freeMom_alwaysAccessible() {
        val mom = PersonaCatalog.get("jewish_mom")
        assertTrue(
            isPersonaAccessible(mom, isPro = false, activeUnlocks = emptyMap(), nowMs = 0L),
        )
    }
}
