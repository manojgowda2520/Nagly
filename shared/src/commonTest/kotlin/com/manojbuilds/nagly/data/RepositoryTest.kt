package com.manojbuilds.nagly.data

import com.manojbuilds.nagly.domain.model.UserGoal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepositoryTest {

    @Test
    fun drinkLog_addObserveAndUndo() = runTest {
        val db = createTestDatabase()
        val repo = DrinkLogRepository(db)

        repo.add(250)
        repo.add(500)

        val today = repo.observeToday().first()
        assertEquals(2, today.size)
        assertEquals(250, today[0].amountMl)
        assertEquals(500, today[1].amountMl)

        val range = repo.observeRange(0L, Long.MAX_VALUE).first()
        assertEquals(2, range.size)

        repo.undoLast()
        val afterUndo = repo.observeToday().first()
        assertEquals(1, afterUndo.size)
        assertEquals(250, afterUndo.single().amountMl)
    }

    @Test
    fun goal_defaultThenSave() = runTest {
        val db = createTestDatabase()
        val repo = GoalRepository(db)

        val initial = repo.observeGoal().first()
        assertEquals(GoalRepository.DEFAULT_GOAL, initial)
        assertEquals(false, initial.onboarded)

        val saved = UserGoal(
            dailyMl = 2500,
            wakeHour = 6,
            sleepHour = 23,
            personaId = "jewish_mom",
            onboarded = true,
        )
        repo.save(saved)
        assertEquals(saved, repo.observeGoal().first())
    }

    @Test
    fun unlock_grantAndObserveActive() = runTest {
        val db = createTestDatabase()
        val repo = UnlockRepository(db)

        assertTrue(repo.observeUnlocked().first().isEmpty())

        repo.grant("grandparent", durationMs = 60_000L)
        val unlocked = repo.observeUnlocked().first()
        assertEquals(setOf("grandparent"), unlocked)

        val expires = repo.expiresAtMs("grandparent")
        assertTrue(expires != null && expires > 0L)
    }
}
