package com.manojbuilds.nagly.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.manojbuilds.nagly.db.NaglyDatabase
import com.manojbuilds.nagly.domain.model.DrinkLog
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DrinkLogRepository(
    private val database: NaglyDatabase,
    private val clock: Clock = Clock.System,
) {
    private val queries get() = database.drinkLogQueries

    fun observeToday(): Flow<List<DrinkLog>> {
        val (fromMs, toMs) = todayRangeMs(clock)
        return queries.selectToday(fromMs, toMs)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }
    }

    fun observeRange(fromMs: Long, toMs: Long): Flow<List<DrinkLog>> {
        return queries.selectRange(fromMs, toMs)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }
    }

    suspend fun add(amountMl: Int) = withContext(Dispatchers.IO) {
        queries.insert(
            timestampMs = clock.now().toEpochMilliseconds(),
            amountMl = amountMl.toLong(),
        )
    }

    suspend fun undoLast() = withContext(Dispatchers.IO) {
        val last = queries.selectLast().executeAsOneOrNull() ?: return@withContext
        queries.deleteById(last.id)
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteById(id)
    }

    private fun com.manojbuilds.nagly.db.Drink_log.toDomain() = DrinkLog(
        id = id,
        timestampMs = timestampMs,
        amountMl = amountMl.toInt(),
    )
}
