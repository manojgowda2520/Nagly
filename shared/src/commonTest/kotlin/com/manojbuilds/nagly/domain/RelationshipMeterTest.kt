package com.manojbuilds.nagly.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class RelationshipMeterTest {

    @Test
    fun levelFromStreak_thresholds() {
        assertEquals(RelationshipLevel.STRANGER, levelFromStreak(0))
        assertEquals(RelationshipLevel.FRIENDLY, levelFromStreak(1))
        assertEquals(RelationshipLevel.FRIENDLY, levelFromStreak(2))
        assertEquals(RelationshipLevel.CLOSE, levelFromStreak(3))
        assertEquals(RelationshipLevel.FAMILY, levelFromStreak(7))
        assertEquals(RelationshipLevel.SOUL_REMINDER, levelFromStreak(14))
        assertEquals(RelationshipLevel.SOUL_REMINDER, levelFromStreak(30))
    }

    @Test
    fun levelFromConsistency_thresholds() {
        assertEquals(RelationshipLevel.STRANGER, levelFromConsistency(0))
        assertEquals(RelationshipLevel.STRANGER, levelFromConsistency(2))
        assertEquals(RelationshipLevel.FRIENDLY, levelFromConsistency(3))
        assertEquals(RelationshipLevel.CLOSE, levelFromConsistency(6))
        assertEquals(RelationshipLevel.FAMILY, levelFromConsistency(10))
        assertEquals(RelationshipLevel.SOUL_REMINDER, levelFromConsistency(13))
    }

    @Test
    fun computeRelationshipLevel_takesHigher() {
        assertEquals(RelationshipLevel.FAMILY, computeRelationshipLevel(currentStreak = 7, daysMetGoalIn14 = 2))
        assertEquals(RelationshipLevel.CLOSE, computeRelationshipLevel(currentStreak = 1, daysMetGoalIn14 = 6))
        assertEquals(RelationshipLevel.SOUL_REMINDER, computeRelationshipLevel(currentStreak = 14, daysMetGoalIn14 = 13))
    }

    @Test
    fun relationshipProgressToNext_atMaxIsOne() {
        assertEquals(1f, relationshipProgressToNext(RelationshipLevel.SOUL_REMINDER, 20, 14))
    }

    @Test
    fun relationshipProgressToNext_midLevel() {
        val progress = relationshipProgressToNext(RelationshipLevel.FRIENDLY, currentStreak = 1, daysMetGoalIn14 = 3)
        assertTrue(progress in 0f..1f)
        assertTrue(progress > 0f)
    }

    @Test
    fun countDaysMetGoalInWindow() {
        val today = LocalDate(2026, 8, 5)
        val logs = mapOf(
            today to 2000,
            today.minusDays(1) to 2000,
            today.minusDays(2) to 500,
        )
        assertEquals(2, countDaysMetGoalInWindow(logs, dailyMl = 2000, windowDays = 14, today = today))
    }
}

private fun LocalDate.minusDays(days: Int): LocalDate =
    LocalDate.fromEpochDays(toEpochDays() - days)
