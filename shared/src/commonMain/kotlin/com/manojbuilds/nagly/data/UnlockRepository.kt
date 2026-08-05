package com.manojbuilds.nagly.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.manojbuilds.nagly.db.NaglyDatabase
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UnlockRepository(
    private val database: NaglyDatabase,
    private val clock: Clock = Clock.System,
) {
    private val queries get() = database.personaUnlockQueries

    fun observeUnlocked(): Flow<Set<String>> {
        return observeUnlockExpiries().map { it.keys }
    }

    /** Active unlocks keyed by relationshipId. */
    fun observeUnlockExpiries(): Flow<Map<String, Long>> {
        val nowMs = clock.now().toEpochMilliseconds()
        return queries.selectActive(nowMs)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                val current = clock.now().toEpochMilliseconds()
                rows.filter { it.expiresAtMs > current }
                    .associate { it.relationshipId to it.expiresAtMs }
            }
    }

    suspend fun grant(relationshipId: String, durationMs: Long) = withContext(Dispatchers.IO) {
        val nowMs = clock.now().toEpochMilliseconds()
        queries.deleteExpired(nowMs)
        queries.upsert(
            relationshipId = relationshipId,
            expiresAtMs = nowMs + durationMs,
        )
    }

    suspend fun expiresAtMs(relationshipId: String): Long? = withContext(Dispatchers.IO) {
        val nowMs = clock.now().toEpochMilliseconds()
        queries.selectActive(nowMs)
            .executeAsList()
            .firstOrNull { it.relationshipId == relationshipId }
            ?.expiresAtMs
    }
}
