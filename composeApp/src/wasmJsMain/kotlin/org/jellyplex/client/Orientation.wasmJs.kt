package org.jellyplex.client

import androidx.compose.runtime.Composable

@Composable
actual fun OrientationEffectImpl(orientation: ScreenOrientation) {
    // No-op for wasm
}
