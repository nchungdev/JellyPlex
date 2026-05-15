package org.jellyplus.client

import androidx.compose.runtime.Composable

@Composable
actual fun OrientationEffectImpl(orientation: ScreenOrientation) {
    // No-op for wasm
}

@Composable
actual fun PlayerFullscreenEffect(enabled: Boolean) {
    // No-op for wasm
}
