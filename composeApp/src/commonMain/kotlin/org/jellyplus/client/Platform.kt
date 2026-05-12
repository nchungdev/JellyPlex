package org.jellyplus.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.russhwolf.settings.Settings

enum class UiType {
    Mobile,
    Desktop,
}

val LocalUiType = compositionLocalOf { UiType.Mobile }

@Composable
expect fun getUiType(): UiType

@Composable
expect fun provideSettings(): Settings

@Composable
expect fun AppBackHandler(enabled: Boolean = true, onBack: () -> Unit)

expect fun getDeviceName(): String
