package org.jellyplus.client.data.local

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver? = null
}
