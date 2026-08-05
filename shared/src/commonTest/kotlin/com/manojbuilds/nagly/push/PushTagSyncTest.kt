package com.manojbuilds.nagly.push

import com.manojbuilds.nagly.domain.model.DrinkLog
import com.manojbuilds.nagly.domain.model.UserGoal
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class PushTagSyncTest {

    @Test
    fun computePushTags_mapsExpectedKeys() {
        val goal = UserGoal(2000, 7, 22, "jewish_mom", true)
        val tz = TimeZone.UTC
        // Fixed "now": 2026-08-05 12:00 UTC
        val nowMs = 1_786_219_200_000L
        val earlierSameDay = nowMs - 3_600_000L
        val tags = computePushTags(
            goal = goal,
            recentLogs = listOf(DrinkLog(1, earlierSameDay, 2000)),
            isPro = true,
            nowMs = nowMs,
            timeZone = tz,
        )
        assertEquals("jewish_mom", tags.personaId)
        assertEquals(2000, tags.dailyGoalMl)
        assertEquals(0, tags.lastLogDaysAgo)
        assertEquals(true, tags.isPro)
    }

    @Test
    fun fakePushClient_storesTags() {
        val client = FakePushClient()
        client.applyTags(
            PushTags(
                personaId = "gym_bro",
                currentStreak = 3,
                dailyGoalMl = 2500,
                lastLogDaysAgo = 1,
                isPro = false,
            ),
        )
        val snap = client.tagsSnapshot()
        assertEquals("gym_bro", snap["persona_id"])
        assertEquals("3", snap["current_streak"])
        assertEquals("2500", snap["daily_goal_ml"])
        assertEquals("1", snap["last_log_days_ago"])
        assertEquals("false", snap["is_pro"])
    }
}
