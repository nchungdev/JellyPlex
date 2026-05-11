package org.jellyplex.client.ui

import androidx.compose.runtime.Composable

@Composable
actual fun JellyPlexBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // Web doesn't have a standard system back button handled by Compose easily without extra JS.
}
