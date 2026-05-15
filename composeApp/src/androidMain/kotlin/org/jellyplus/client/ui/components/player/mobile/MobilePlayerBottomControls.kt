package org.jellyplus.client.ui.components.player.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Surface
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobilePlayerBottomControls(
    item: org.jellyplus.client.domain.models.MediaItem,
    currentPosition: Long,
    duration: Long,
    selectedTextTrackIndex: Int,
    onSeek: (Long) -> Unit,
    onSeekStarted: () -> Unit,
    onSeekFinished: (Long) -> Unit,
    onShowCaptionDialog: () -> Unit,
    onShowAudioDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggingValue by remember { mutableStateOf<Float?>(null) }
    val displayPosition = draggingValue?.toLong() ?: currentPosition
    val isInteractingWithSeekbar = draggingValue != null
    val seekbarHorizontalInset = 14.dp
    val seekbarLineHeight = 4.dp
    val seekbarThumbSize = if (isInteractingWithSeekbar) 20.dp else seekbarLineHeight + 2.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF1F2430).copy(alpha = 0.72f),
            ) {
                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(formatPlayerTime(displayPosition), color = Color.White, fontSize = 13.sp)
                    Text(" / ", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    Text(formatPlayerTime(duration), color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF1F2430).copy(alpha = 0.72f),
            ) {
                Row(
                    modifier = Modifier.height(38.dp).padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onShowCaptionDialog, modifier = Modifier.size(38.dp)) {
                        Icon(
                            Icons.Default.Subtitles,
                            null,
                            tint = if (selectedTextTrackIndex >= 0) Color(0xFF00D4A8) else Color.White,
                            modifier = Modifier.size(21.dp),
                        )
                    }

                    Spacer(modifier = Modifier.size(10.dp))

                    IconButton(onClick = onShowAudioDialog, modifier = Modifier.size(38.dp)) {
                        Icon(Icons.Default.Audiotrack, null, tint = Color.White, modifier = Modifier.size(21.dp))
                    }
                }
            }
        }

        // Seekbar row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = seekbarHorizontalInset),
                contentAlignment = Alignment.CenterStart,
            ) {
                val progress = if (duration > 0) (displayPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(seekbarLineHeight)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(2.dp)),
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(seekbarLineHeight)
                        .background(Color(0xFF00D4A8), RoundedCornerShape(2.dp)),
                )

                if (duration > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(seekbarThumbSize),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(seekbarThumbSize)
                                .background(Color.White, CircleShape),
                        )
                    }
                }
            }

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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = seekbarHorizontalInset),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                    ),
                    thumb = {
                        Box(Modifier.size(0.dp))
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
