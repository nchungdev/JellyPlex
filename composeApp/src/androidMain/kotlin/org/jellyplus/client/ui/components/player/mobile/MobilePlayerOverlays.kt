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
    currentMarkerType: String?,
    autoSkipIntro: Boolean,
    autoSkipOutro: Boolean,
    autoNextCountdown: Int,
    isControlsVisible: Boolean,
    showSkipToast: Boolean,
    skipToastLabel: String,
    onSkipMarker: () -> Unit,
    onCancelAutoNext: () -> Unit,
) {
    val isAutoSkipping = when (currentMarkerType) {
        "Credits" -> autoSkipOutro
        else -> autoSkipIntro  // null = Intro
    }
    val skipLabel = when (currentMarkerType) {
        "Credits" -> "Skip Credits"
        else -> "Skip Intro"
    }
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

    // Skip button — only shown when not auto-skipping
    if (isInMarkerRange && !isAutoSkipping) {
        Button(
            onClick = onSkipMarker,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (isControlsVisible) 110.dp else 24.dp, end = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f), contentColor = Color.White),
            shape = RoundedCornerShape(4.dp),
        ) {
            Text(skipLabel, fontWeight = FontWeight.SemiBold)
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
        modifier = Modifier.align(Alignment.CenterStart).padding(start = 32.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier,
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

    // Auto-skip toast — center-bottom, above seekbar when controls visible
    AnimatedVisibility(
        visible = showSkipToast,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = if (isControlsVisible) 120.dp else 32.dp),
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(skipToastLabel, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
