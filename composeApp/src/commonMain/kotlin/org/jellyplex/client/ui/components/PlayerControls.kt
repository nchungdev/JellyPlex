package org.jellyplex.client.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlayerControls(
    title: String,
    isVisible: Boolean,
    isPlaying: Boolean,
    progress: Float,
    showSkipIntro: Boolean,
    showSkipEnding: Boolean,
    showNextPrev: Boolean,
    isSyncPlayActive: Boolean,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onSkipIntro: () -> Unit,
    onSkipEnding: () -> Unit,
    onNextEpisode: () -> Unit,
    onPrevEpisode: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onBack: () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
        ) {
            // Top Bar
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )

                if (isSyncPlayActive) {
                    Box(
                        modifier =
                            Modifier
                                .background(Color.Blue.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text("SyncPlay Active", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            // Skip Intro Button
            if (showSkipIntro) {
                Button(
                    onClick = onSkipIntro,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 120.dp, end = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Skip Intro", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // Skip Ending Button
            if (showSkipEnding) {
                Button(
                    onClick = onSkipEnding,
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 80.dp, end = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Skip Ending", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }

            // Next/Prev Episode Buttons
            if (showNextPrev) {
                Row(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 200.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Button(
                        onClick = onPrevEpisode,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("Prev", color = Color.White)
                    }
                    Button(
                        onClick = onNextEpisode,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text("Next", color = Color.White)
                    }
                }
            }

            // Bottom Controls
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.BottomCenter),
            ) {
                Slider(
                    value = progress,
                    onValueChange = onSeek,
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        SliderDefaults.colors(
                            // Plex Gold
                            thumbColor = Color(0xFFFFB300),
                            activeTrackColor = Color(0xFFFFB300),
                        ),
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("0:45 / 2:15", color = Color.White, fontSize = 14.sp)

                    Row {
                        Button(
                            onClick = { onSpeedChange(if (playbackSpeed < 2.0f) playbackSpeed + 0.25f else 0.5f) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text("${playbackSpeed}x", color = Color.White)
                        }

                        Text(
                            text = if (isPlaying) "PAUSE" else "PLAY",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    Text("1080p", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}
