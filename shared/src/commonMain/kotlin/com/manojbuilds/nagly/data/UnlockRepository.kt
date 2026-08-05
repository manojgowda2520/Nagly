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
        val nowMs = clock.now().toEpochMilliseconds()
        return queries.selectActive(nowMs)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows ->
                val current = clock.now().toEpochMilliseconds()
                rows.filter { it.expiresAtMs > current }.map { it.personaId }.toSet()
            }
    }

    suspend fun grant(personaId: String, durationMs: Long) = withContext(Dispatchers.IO) {
        val nowMs = clock.now().toEpochMilliseconds()
        queries.deleteExpired(nowMs)
        queries.upsert(
            personaId = personaId,
            expiresAtMs = nowMs + durationMs,
        )
    }

    suspend fun expiresAtMs(personaId: String): Long? = withContext(Dispatchers.IO) {
        val nowMs = clock.now().toEpochMilliseconds()
        queries.selectActive(nowMs)
            .executeAsList()
            .firstOrNull { it.personaId == personaId }
            ?.expiresAtMs
    }
}
