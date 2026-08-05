package com.manojbuilds.nagly.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.sqliter.DatabaseConfiguration
import com.manojbuilds.nagly.db.NaglyDatabase

actual fun createTestSqlDriver(): SqlDriver {
    return NativeSqliteDriver(
        schema = NaglyDatabase.Schema,
        name = "nagly-test.db",
        onConfiguration = { config ->
            config.copy(
                inMemory = true,
                extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true),
            )
        },
    )
}
