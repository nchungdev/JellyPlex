package org.jellyplus.client.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.jellyplus.client.data.db.JellyPlusDatabase

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver? {
        val context = ContextProvider.context
            ?: throw IllegalStateException("Context not initialized. Call ContextProvider.init(context) before creating the database.")
        return AndroidSqliteDriver(JellyPlusDatabase.Schema, context, "jellyplus.db")
    }
}
