package com.manojbuilds.nagly.domain

import com.manojbuilds.nagly.domain.model.Mood
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MoodEngineTest {

    @Test
    fun computeMood_proudTakesPriority() {
        assertEquals(
            Mood.PROUD,
            computeMood(progressRatio = 1f, expectedRatio = 0.9f, ignoredNudgeCount = 5),
        )
    }

    @Test
    fun computeMood_disappointedWhenIgnored() {
        assertEquals(
            Mood.DISAPPOINTED,
            computeMood(progressRatio = 0.4f, expectedRatio = 0.4f, ignoredNudgeCount = 2),
        )
    }

    @Test
    fun computeMood_worriedWhenBehind() {
        assertEquals(
            Mood.WORRIED,
            computeMood(progressRatio = 0.2f, expectedRatio = 0.5f, ignoredNudgeCount = 0),
        )
    }

    @Test
    fun computeMood_neutralOtherwise() {
        assertEquals(
            Mood.NEUTRAL,
            computeMood(progressRatio = 0.5f, expectedRatio = 0.5f, ignoredNudgeCount = 1),
        )
    }

    @Test
    fun expectedRatio_beforeWakeAndAfterSleep() {
        assertEquals(0f, expectedRatio(nowHour = 5, wakeHour = 7, sleepHour = 22))
        assertEquals(1f, expectedRatio(nowHour = 23, wakeHour = 7, sleepHour = 22))
        assertTrue(expectedRatio(nowHour = 14, wakeHour = 7, sleepHour = 22) in 0.4f..0.6f)
    }

    @Test
    fun recommendedDailyMl_clamped() {
        assertEquals(1500, recommendedDailyMl(30))
        assertEquals(2450, recommendedDailyMl(70))
        assertEquals(4000, recommendedDailyMl(200))
    }

    @Test
    fun currentStreak_emptyAndGaps() {
        assertEquals(0, currentStreak(emptyMap(), dailyMl = 2000))

        val d1 = LocalDate(2026, 8, 1)
        val d2 = LocalDate(2026, 8, 2)
        val d4 = LocalDate(2026, 8, 4)
        val logs = mapOf(
            d1 to 2000,
            d2 to 2000,
            d4 to 2000,
        )
        assertEquals(1, currentStreak(logs, dailyMl = 2000))
    }

    @Test
    fun currentStreak_todayIncompleteDoesNotBreakPrior() {
        val d1 = LocalDate(2026, 8, 1)
        val d2 = LocalDate(2026, 8, 2)
        val d3 = LocalDate(2026, 8, 3)
        val logs = mapOf(
            d1 to 2000,
            d2 to 2000,
            d3 to 500, // today incomplete
        )
        assertEquals(2, currentStreak(logs, dailyMl = 2000))
    }

    @Test
    fun pickLine_avoidsPreviousWhenPoolAllows() {
        val persona = PersonaCatalog.get("indian_mom")
        val first = pickLine(persona, Mood.NEUTRAL, previousLine = null)
        repeat(20) {
            val next = pickLine(persona, Mood.NEUTRAL, previousLine = first)
            assertNotEquals(first, next)
        }
    }

    @Test
    fun personaCatalog_hasThreeLinesPerMood() {
        PersonaCatalog.all.forEach { persona ->
            Mood.entries.forEach { mood ->
                assertEquals(3, persona.lines.getValue(mood).size, "${persona.id} $mood")
            }
        }
        assertEquals(3, PersonaCatalog.free.size)
        assertEquals(4, PersonaCatalog.pro.size)
    }
}
