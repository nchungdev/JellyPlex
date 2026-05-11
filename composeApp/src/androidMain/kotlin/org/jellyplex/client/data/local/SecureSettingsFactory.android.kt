package org.jellyplex.client.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createSecureSettings(): Settings {
    // Note: We need a way to access context.
    // For now, we'll use a hack or assume context is initialized.
    // In a real app, use a proper dependency injection or a startup initializer.
    val context = ContextProvider.context
        ?: throw IllegalStateException("Context not initialized. Call ContextProvider.init(context) in your Application class or Activity.")

    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_jellyplex_settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    return SharedPreferencesSettings(sharedPreferences)
}

object ContextProvider {
    var context: Context? = null
}
