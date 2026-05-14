package org.jellyplus.client.ui.components.player.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.domain.models.MediaType

@Composable
internal fun DesktopPlayerControls(
    item: org.jellyplus.client.domain.models.MediaItem,
    parentItem: org.jellyplus.client.domain.models.MediaItem?,
    isVisible: Boolean,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    showNextPrev: Boolean,
    playFocusRequester: FocusRequester,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onPrevEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    onSeekLeft: () -> Unit,
    onSeekRight: () -> Unit,
    onMoreClick: () -> Unit,
) {
    AnimatedVisibility(visible = isVisible, enter = fadeIn(), exit = fadeOut()) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {

            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 32.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DesktopPlayerIconButton(
                    Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack,
                    modifier = Modifier.focusProperties { down = playFocusRequester },
                )
                Spacer(modifier = Modifier.width(24.dp))
                val titleText = remember(item, parentItem) {
                    if (item.type == MediaType.EPISODE) {
                        val s = parentItem?.title ?: "Series"
                        val sn = item.parentIndexNumber?.let { "S${it.toString().padStart(2, '0')}" } ?: ""
                        val ep = item.index?.let { "E${it.toString().padStart(2, '0')}" } ?: ""
                        val sep = if (sn.isNotEmpty() && ep.isNotEmpty()) " " else ""
                        "$s - $sn$sep$ep - ${item.title}"
                    } else item.title
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        titleText,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                DesktopPlayerIconButton(Icons.Default.MoreVert, size = 48.dp, iconSize = 26.dp, onClick = onMoreClick)
            }

            // Center controls
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                if (showNextPrev) DesktopPlayerIconButton(Icons.Default.SkipPrevious, size = 56.dp, iconSize = 32.dp) { onPrevEpisode() }
                DesktopPlayerIconButton(Icons.Default.Replay10, size = 64.dp, iconSize = 40.dp) { onRewind() }
                DesktopPlayerButton(onClick = onPlayPause, size = 96.dp, modifier = Modifier.focusRequester(playFocusRequester)) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(56.dp))
                }
                DesktopPlayerIconButton(Icons.Default.Forward10, size = 64.dp, iconSize = 40.dp) { onForward() }
                if (showNextPrev) DesktopPlayerIconButton(Icons.Default.SkipNext, size = 56.dp, iconSize = 32.dp) { onNextEpisode() }
            }

            // Bottom controls
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(start = 32.dp, end = 32.dp, bottom = 20.dp),
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
                            modifier = Modifier.height(40.dp).padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(formatDesktopTime(currentPosition), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            Text(" / ", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
                            Text(formatDesktopTime(duration), color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    DesktopPlayerIconButton(Icons.Default.Subtitles, size = 38.dp, iconSize = 21.dp) {}
                    Spacer(modifier = Modifier.width(18.dp))
                    DesktopPlayerIconButton(Icons.Default.Audiotrack, size = 38.dp, iconSize = 21.dp) {}
                }
                Spacer(modifier = Modifier.height(2.dp))
                DesktopSeekbar(
                    currentPosition = currentPosition,
                    duration = duration,
                    onSeekLeft = onSeekLeft,
                    onSeekRight = onSeekRight,
                )
            }
        }
    }
}

@Composable
private fun DesktopSeekbar(
    currentPosition: Long,
    duration: Long,
    onSeekLeft: () -> Unit,
    onSeekRight: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.fillMaxWidth().height(30.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) when (keyEvent.key) {
                    Key.DirectionLeft -> { onSeekLeft(); true }
                    Key.DirectionRight -> { onSeekRight(); true }
                    else -> false
                } else false
            },
        contentAlignment = Alignment.Center,
    ) {
        val barHeight = if (isFocused) 10.dp else 6.dp
        val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
        Box(modifier = Modifier.fillMaxWidth().height(barHeight).background(Color.White.copy(alpha = if (isFocused) 0.3f else 0.2f), RoundedCornerShape(5.dp)))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            Box(modifier = Modifier.fillMaxWidth(progress).height(barHeight).background(if (isFocused) Color.White else Color(0xFF24D366), RoundedCornerShape(5.dp)))
        }
    }
}
