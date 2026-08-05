package com.manojbuilds.nagly.data

import app.cash.sqldelight.db.SqlDriver
import com.manojbuilds.nagly.db.NaglyDatabase

expect fun createTestSqlDriver(): SqlDriver

fun createTestDatabase(): NaglyDatabase {
    val driver = createTestSqlDriver()
    return NaglyDatabase(driver)
}
