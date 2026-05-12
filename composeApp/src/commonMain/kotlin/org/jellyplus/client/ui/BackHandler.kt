package org.jellyplus.client.ui

import androidx.compose.runtime.Composable

@Composable
expect fun JellyPlusBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)
