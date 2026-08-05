package com.manojbuilds.nagly.notifications

import com.manojbuilds.nagly.domain.model.UserGoal
import kotlin.test.Test
import kotlin.test.assertTrue

class NudgeSchedulerTest {

    private val goal = UserGoal(
        dailyMl = 2000,
        wakeHour = 8,
        sleepHour = 22,
        personaId = "indian_mom",
        onboarded = true,
    )

    @Test
    fun nextNudgeTimes_respectsWakeSleepAndCaps() {
        // 10:00 local-ish via fixed epoch chosen for wake window
        val now = hourOnSameDayMs(1_700_000_000_000L, 10)
        val times = nextNudgeTimes(nowMs = now, goal = goal, consumedMl = 250)
        assertTrue(times.isNotEmpty())
        assertTrue(times.size <= 8)
        times.zipWithNext().forEach { (a, b) ->
            assertTrue(b - a >= 45L * 60L * 1000L)
        }
        val sleep = sleepBoundaryMs(now, goal.wakeHour, goal.sleepHour)
        assertTrue(times.all { it in (now + 1)..(sleep - 1) })
    }

    @Test
    fun nextNudgeTimes_emptyWhenGoalMet() {
        val now = hourOnSameDayMs(1_700_000_000_000L, 10)
        val times = nextNudgeTimes(nowMs = now, goal = goal, consumedMl = 2000)
        assertTrue(times.isEmpty())
    }

    @Test
    fun nextNudgeTimes_emptyOutsideWakingHours() {
        val now = hourOnSameDayMs(1_700_000_000_000L, 23)
        val times = nextNudgeTimes(nowMs = now, goal = goal, consumedMl = 0)
        assertTrue(times.isEmpty())
    }
}
