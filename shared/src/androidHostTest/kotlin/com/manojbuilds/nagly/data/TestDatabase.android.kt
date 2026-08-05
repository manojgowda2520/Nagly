package com.manojbuilds.nagly.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.manojbuilds.nagly.db.NaglyDatabase

actual fun createTestSqlDriver(): SqlDriver {
    return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { driver ->
        NaglyDatabase.Schema.create(driver)
    }
}
