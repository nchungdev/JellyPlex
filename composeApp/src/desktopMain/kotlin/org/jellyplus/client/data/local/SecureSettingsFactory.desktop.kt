package org.jellyplus.client.data.local

import com.russhwolf.settings.Settings

actual fun createSecureSettings(): Settings {
    // On Desktop, we should ideally use a system keychain or similar.
    // For now, we'll use the default settings as a placeholder.
    return Settings()
}
