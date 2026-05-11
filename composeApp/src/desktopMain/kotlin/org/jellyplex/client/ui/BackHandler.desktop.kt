package org.jellyplex.client.ui

import androidx.compose.runtime.Composable

@Composable
actual fun JellyPlexBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // Desktop typically doesn't have a system back button.
    // For now, we do nothing. In the future, we could handle the Escape key here.
}
