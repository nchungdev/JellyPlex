package org.jellyplus.client.ui

import androidx.compose.runtime.Composable

@Composable
actual fun JellyPlusBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // Desktop typically doesn't have a system back button.
    // For now, we do nothing. In the future, we could handle the Escape key here.
}
