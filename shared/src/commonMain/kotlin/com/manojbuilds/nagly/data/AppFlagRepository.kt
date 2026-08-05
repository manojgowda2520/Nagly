package com.manojbuilds.nagly.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.manojbuilds.nagly.db.NaglyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppFlagRepository(
    private val database: NaglyDatabase,
) {
    private val queries get() = database.appFlagQueries

    fun observeBoolean(key: String, default: Boolean = false): Flow<Boolean> {
        return queries.select(key)
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { row -> if (row == null) default else row == 1L }
    }

    suspend fun setBoolean(key: String, value: Boolean) = withContext(Dispatchers.IO) {
        queries.upsert(key = key, value_ = if (value) 1L else 0L)
    }

    companion object {
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}
