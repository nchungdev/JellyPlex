package org.jellyplus.client.ui.components.player.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.domain.models.MediaType

internal enum class MarkerState { IDLE, MARKING }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobilePlayerBottomControls(
    item: org.jellyplus.client.domain.models.MediaItem,
    currentPosition: Long,
    duration: Long,
    markerState: MarkerState,
    selectedTextTrackIndex: Int,
    onSeek: (Long) -> Unit,
    onSeekStarted: () -> Unit,
    onSeekFinished: (Long) -> Unit,
    onShowCaptionDialog: () -> Unit,
    onShowAudioDialog: () -> Unit,
    onMarkToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggingValue by remember { mutableStateOf<Float?>(null) }
    val displayPosition = draggingValue?.toLong() ?: currentPosition

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp, bottom = 32.dp),
    ) {
        // Info row: time | spacer | captions | mark (episodes) | audio
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(formatPlayerTime(displayPosition), color = Color.White, fontSize = 12.sp)
            Text(" / ", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            Text(formatPlayerTime(duration), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onShowCaptionDialog, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Subtitles,
                    null,
                    tint = if (selectedTextTrackIndex >= 0) Color(0xFF24D366) else Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            if (item.type == MediaType.EPISODE) {
                IconButton(onClick = onMarkToggle, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.BookmarkAdd,
                        contentDescription = "Mark preview",
                        tint = if (markerState == MarkerState.MARKING) Color.Red else Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            IconButton(onClick = onShowAudioDialog, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Audiotrack, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // Seekbar row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp), // Increased height for better touch target
            contentAlignment = Alignment.CenterStart,
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)),
            )
            val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
            // Active track
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .background(Color(0xFF24D366), RoundedCornerShape(2.dp)),
            )
            if (duration > 0) {
                Slider(
                    value = draggingValue ?: currentPosition.toFloat(),
                    onValueChange = {
                        if (draggingValue == null) onSeekStarted()
                        draggingValue = it
                    },
                    onValueChangeFinished = {
                        draggingValue?.let {
                            onSeekFinished(it.toLong())
                            draggingValue = null
                        }
                    },
                    valueRange = 0f..duration.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(20.dp) // Slightly larger thumb for better visibility while dragging
                                .background(Color.White, CircleShape)
                        )
                    },
                )
            }
        }
    }
}

internal fun formatPlayerTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}
