package org.jellyplex.client.ui

import androidx.compose.runtime.Composable

@Composable
expect fun JellyPlexBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
)
