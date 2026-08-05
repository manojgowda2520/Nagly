package com.manojbuilds.nagly.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.manojbuilds.nagly.db.NaglyDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = NaglyDatabase.Schema,
            context = context,
            name = "nagly.db",
        )
    }
}
