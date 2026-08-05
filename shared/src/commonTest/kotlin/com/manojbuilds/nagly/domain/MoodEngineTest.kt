package com.manojbuilds.nagly.domain

import com.manojbuilds.nagly.domain.model.ActivityLevel
import com.manojbuilds.nagly.domain.model.DayPart
import com.manojbuilds.nagly.domain.model.Mood
import com.manojbuilds.nagly.domain.model.Tier
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
    fun dayPartFor_splitsWakingDayIntoThirds() {
        // wake 7 sleep 22 → 15 waking hours → thirds of 5h: 7–12, 12–17, 17–22
        assertEquals(DayPart.MORNING, dayPartFor(hour = 8, wakeHour = 7, sleepHour = 22))
        assertEquals(DayPart.AFTERNOON, dayPartFor(hour = 14, wakeHour = 7, sleepHour = 22))
        assertEquals(DayPart.EVENING, dayPartFor(hour = 20, wakeHour = 7, sleepHour = 22))
        assertEquals(DayPart.MORNING, dayPartFor(hour = 5, wakeHour = 7, sleepHour = 22))
        assertEquals(DayPart.EVENING, dayPartFor(hour = 23, wakeHour = 7, sleepHour = 22))
    }

    @Test
    fun behindSeverity_onTrackAndBehind() {
        assertEquals(0f, behindSeverity(progressRatio = 0.5f, expectedRatio = 0.5f))
        assertEquals(0f, behindSeverity(progressRatio = 0.8f, expectedRatio = 0.5f))
        assertEquals(0f, behindSeverity(progressRatio = 0.1f, expectedRatio = 0f))
        assertEquals(1f, behindSeverity(progressRatio = 0f, expectedRatio = 0.5f))
        assertEquals(0.5f, behindSeverity(progressRatio = 0.25f, expectedRatio = 0.5f))
    }

    @Test
    fun recommendedDailyMl_clamped() {
        assertEquals(1500, recommendedDailyMl(30, ActivityLevel.SEDENTARY))
        assertEquals(2695, recommendedDailyMl(70))
        assertEquals(4000, recommendedDailyMl(200, ActivityLevel.SEDENTARY))
    }

    @Test
    fun recommendedDailyMl_activityMultipliers() {
        assertEquals(2205, recommendedDailyMl(63, ActivityLevel.SEDENTARY))
        assertEquals(2695, recommendedDailyMl(70, ActivityLevel.LIGHT))
        assertEquals(2940, recommendedDailyMl(70, ActivityLevel.ACTIVE))
    }

    @Test
    fun currentStreak_empty() {
        assertEquals(0, currentStreak(emptyMap(), dailyMl = 2000, today = LocalDate(2026, 8, 5)))
    }

    @Test
    fun currentStreak_runEndingToday() {
        val today = LocalDate(2026, 8, 5)
        val logs = mapOf(
            LocalDate(2026, 8, 3) to 2000,
            LocalDate(2026, 8, 4) to 2000,
            today to 2000,
        )
        assertEquals(3, currentStreak(logs, dailyMl = 2000, today = today))
    }

    @Test
    fun currentStreak_runEndingYesterday() {
        val today = LocalDate(2026, 8, 5)
        val logs = mapOf(
            LocalDate(2026, 8, 3) to 2000,
            LocalDate(2026, 8, 4) to 2000,
            today to 500, // today incomplete
        )
        assertEquals(2, currentStreak(logs, dailyMl = 2000, today = today))
    }

    @Test
    fun currentStreak_runEndingThreeDaysAgo_isZero() {
        val today = LocalDate(2026, 8, 5)
        val logs = mapOf(
            LocalDate(2026, 8, 1) to 2000,
            LocalDate(2026, 8, 2) to 2000,
        )
        assertEquals(0, currentStreak(logs, dailyMl = 2000, today = today))
    }

    @Test
    fun currentStreak_threeWeekOldRun_isZero() {
        val today = LocalDate(2026, 8, 5)
        val logs = mapOf(
            LocalDate(2026, 7, 12) to 2000,
            LocalDate(2026, 7, 13) to 2000,
            LocalDate(2026, 7, 14) to 2000,
        )
        assertEquals(0, currentStreak(logs, dailyMl = 2000, today = today))
    }

    @Test
    fun pickLine_avoidsPreviousWhenPoolAllows() {
        val persona = PersonaCatalog.get("indian_mom")
        val first = pickLine(persona, Mood.NEUTRAL, dayPart = DayPart.ANYTIME, previousLine = null)
        repeat(20) {
            val next = pickLine(persona, Mood.NEUTRAL, dayPart = DayPart.ANYTIME, previousLine = first)
            assertNotEquals(first, next)
        }
    }

    @Test
    fun linesFor_fallsBackToAnytimeWhenDayPartEmpty() {
        val persona = PersonaCatalog.get("indian_mom")
        val anytime = PersonaCatalog.linesFor(persona, Mood.NEUTRAL, DayPart.ANYTIME)
        assertEquals(3, anytime.size)
        // Neutral has only ANYTIME — requesting MORNING falls back
        assertEquals(anytime, PersonaCatalog.linesFor(persona, Mood.NEUTRAL, DayPart.MORNING))
    }

    @Test
    fun linesFor_usesDayPartWhenPresent() {
        val persona = PersonaCatalog.get("indian_mom")
        val morning = PersonaCatalog.linesFor(persona, Mood.WORRIED, DayPart.MORNING)
        val afternoon = PersonaCatalog.linesFor(persona, Mood.WORRIED, DayPart.AFTERNOON)
        assertEquals(3, morning.size)
        assertEquals(3, afternoon.size)
        assertNotEquals(morning, afternoon)
    }

    @Test
    fun personaCatalog_taxonomyAndPlaceholders() {
        assertEquals(4, PersonaCatalog.relationships.size)
        assertEquals(Tier.FREE, PersonaCatalog.relationship("mom").tier)
        assertEquals(Tier.PRO, PersonaCatalog.relationship("dad").tier)
        assertEquals(3, PersonaCatalog.variantsOf("mom").size)
        assertEquals(3, PersonaCatalog.free.size)
        assertEquals(9, PersonaCatalog.pro.size)
        PersonaCatalog.all.forEach { persona ->
            assertEquals(3, PersonaCatalog.linesFor(persona, Mood.NEUTRAL, DayPart.ANYTIME).size)
            assertEquals(3, PersonaCatalog.linesFor(persona, Mood.PROUD, DayPart.ANYTIME).size)
            DayPart.entries.filter { it != DayPart.ANYTIME }.forEach { part ->
                assertEquals(3, PersonaCatalog.linesFor(persona, Mood.WORRIED, part).size, "${persona.id} worried $part")
                assertEquals(3, PersonaCatalog.linesFor(persona, Mood.DISAPPOINTED, part).size, "${persona.id} disappointed $part")
            }
            Mood.entries.forEach { mood ->
                assertTrue(persona.skipLabels.getValue(mood).size >= 2, "${persona.id} skip $mood")
            }
        }
    }
}
