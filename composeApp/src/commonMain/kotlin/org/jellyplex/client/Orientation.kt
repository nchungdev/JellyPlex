package org.jellyplex.client

import androidx.compose.runtime.Composable

enum class ScreenOrientation {
    Portrait,
    Landscape,
    Unspecified,
}

@Composable
expect fun OrientationEffectImpl(orientation: ScreenOrientation)

@Composable
fun OrientationEffect(orientation: ScreenOrientation) {
    OrientationEffectImpl(orientation)
}
