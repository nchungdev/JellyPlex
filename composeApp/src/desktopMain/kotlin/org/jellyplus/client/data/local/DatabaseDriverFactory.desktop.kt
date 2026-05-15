package org.jellyplus.client.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.jellyplus.client.data.db.JellyPlusDatabase

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver? {
        val driver = JdbcSqliteDriver("jdbc:sqlite:jellyplus.db")
        runCatching { JellyPlusDatabase.Schema.create(driver) }
        return driver
    }
}
