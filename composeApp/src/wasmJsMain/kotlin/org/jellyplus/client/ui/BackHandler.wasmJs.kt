package org.jellyplus.client.ui

import androidx.compose.runtime.Composable

@Composable
actual fun JellyPlusBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // Web doesn't have a standard system back button handled by Compose easily without extra JS.
}
