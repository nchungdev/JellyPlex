package org.jellyplus.client

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun JellyPlusLoadingLogo(modifier: Modifier) {
    JellyPlusLoadingLogoFallback(modifier = modifier)
}
