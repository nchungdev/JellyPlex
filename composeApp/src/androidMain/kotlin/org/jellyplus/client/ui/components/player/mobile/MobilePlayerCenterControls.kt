package org.jellyplus.client.ui.components.player.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun MobilePlayerCenterControls(
    isPlaying: Boolean,
    isBuffering: Boolean,
    showNextPrev: Boolean,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onPrevEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        if (showNextPrev) {
            IconButton(onClick = onPrevEpisode, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipPrevious, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(36.dp))
            }
        }

        IconButton(onClick = onRewind, modifier = Modifier.size(64.dp)) {
            Icon(Icons.Default.Replay10, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(48.dp))
        }

        if (isBuffering) {
            CircularProgressIndicator(modifier = Modifier.size(72.dp), color = Color.White, strokeWidth = 3.dp)
        } else {
            IconButton(onClick = onPlayPause, modifier = Modifier.size(80.dp)) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(72.dp),
                )
            }
        }

        IconButton(onClick = onForward, modifier = Modifier.size(64.dp)) {
            Icon(Icons.Default.Forward10, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(48.dp))
        }

        if (showNextPrev) {
            IconButton(onClick = onNextEpisode, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipNext, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(36.dp))
            }
        }
    }
}
