package org.jellyplus.client.ui.components.player.mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.domain.models.MediaType

@Composable
internal fun BoxScope.MobilePlayerOverlays(
    item: org.jellyplus.client.domain.models.MediaItem,
    isLongPressing: Boolean,
    showSeekFeedback: Boolean,
    seekFeedback: String,
    seekFeedbackIsRight: Boolean,
    showGestureIndicator: Boolean,
    gestureType: String,
    volume: Float,
    brightness: Float,
    isInMarkerRange: Boolean,
    currentMarkerEndMs: Long,
    markerState: MarkerState,
    autoNextCountdown: Int,
    isControlsVisible: Boolean,
    onSkipMarker: () -> Unit,
    onCancelAutoNext: () -> Unit,
    onCancelMarking: () -> Unit,
) {
    // x2 speed indicator
    AnimatedVisibility(
        visible = isLongPressing,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.Center),
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Text("▶▶ 2×", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }

    // Skip marker button
    if (isInMarkerRange) {
        Button(
            onClick = onSkipMarker,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (isControlsVisible) 110.dp else 24.dp, end = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f), contentColor = Color.White),
            shape = RoundedCornerShape(4.dp),
        ) {
            Text("Skip Intro", fontWeight = FontWeight.SemiBold)
        }
    }

    // Marking indicator banner
    if (markerState == MarkerState.MARKING) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp)
                .background(Color.Red.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(modifier = Modifier.size(8.dp).background(Color.White, CircleShape))
            Text("Marking preview…", color = Color.White, fontSize = 13.sp)
        }
    }

    // Persistent mark cancel button when controls hidden
    if (item.type == MediaType.EPISODE && markerState == MarkerState.MARKING && !isControlsVisible) {
        androidx.compose.material3.IconButton(
            onClick = onCancelMarking,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 32.dp)
                .size(40.dp)
                .background(Color.Red.copy(alpha = 0.85f), CircleShape),
        ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = "Cancel mark", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }

    // Double-tap seek feedback
    AnimatedVisibility(
        visible = showSeekFeedback,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Row(Modifier.fillMaxWidth()) {
                if (seekFeedbackIsRight) Spacer(Modifier.weight(0.5f))
                Box(Modifier.weight(0.5f), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(seekFeedback + "s", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (!seekFeedbackIsRight) Spacer(Modifier.weight(0.5f))
            }
        }
    }

    // Brightness gesture indicator (Volume uses system UI)
    AnimatedVisibility(
        visible = showGestureIndicator && gestureType == "brightness",
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.Center),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 64.dp).align(Alignment.CenterStart)
        ) {
            Box(
                modifier = Modifier.width(6.dp).height(120.dp)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(3.dp)),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(brightness)
                        .align(Alignment.BottomCenter)
                        .background(Color.White, RoundedCornerShape(3.dp)),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Icon(Icons.Default.WbSunny, null, tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }

    // Auto-next countdown
    if (autoNextCountdown > 0 && item.type == MediaType.EPISODE) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Next episode in ${autoNextCountdown}s", color = Color.White, fontSize = 14.sp)
                TextButton(onClick = onCancelAutoNext) {
                    Text("Cancel", color = Color(0xFF24D366), fontSize = 13.sp)
                }
            }
        }
    }
}
