package com.manojbuilds.nagly.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.manojbuilds.nagly.db.NaglyDatabase
import com.manojbuilds.nagly.domain.model.UserGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GoalRepository(
    private val database: NaglyDatabase,
) {
    private val queries get() = database.userGoalQueries

    fun observeGoal(): Flow<UserGoal> {
        return queries.select()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> row?.toDomain() ?: DEFAULT_GOAL }
    }

    suspend fun save(goal: UserGoal) = withContext(Dispatchers.IO) {
        queries.upsert(
            dailyMl = goal.dailyMl.toLong(),
            wakeHour = goal.wakeHour.toLong(),
            sleepHour = goal.sleepHour.toLong(),
            personaId = goal.personaId,
            onboarded = if (goal.onboarded) 1L else 0L,
        )
    }

    private fun com.manojbuilds.nagly.db.User_goal.toDomain() = UserGoal(
        dailyMl = dailyMl.toInt(),
        wakeHour = wakeHour.toInt(),
        sleepHour = sleepHour.toInt(),
        personaId = personaId,
        onboarded = onboarded != 0L,
    )

    companion object {
        val DEFAULT_GOAL = UserGoal(
            dailyMl = 2000,
            wakeHour = 7,
            sleepHour = 22,
            personaId = "indian_mom",
            onboarded = false,
        )
    }
}
