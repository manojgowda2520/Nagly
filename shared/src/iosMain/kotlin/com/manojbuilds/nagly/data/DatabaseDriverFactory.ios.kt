package com.manojbuilds.nagly.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.manojbuilds.nagly.db.NaglyDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = NaglyDatabase.Schema,
            name = "nagly.db",
        )
    }
}
