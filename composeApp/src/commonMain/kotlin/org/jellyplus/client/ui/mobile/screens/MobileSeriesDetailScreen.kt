package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.viewmodels.SeriesDetailViewModel

@Composable
fun MobileSeriesDetailScreen(
    item: MediaItem,
    viewModel: SeriesDetailViewModel,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onPlayEpisode: (MediaItem, MediaItem?, List<MediaItem>) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val baseUrl = state.baseUrl

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1113))) {
        AsyncImage(
            model = item.getBackdropUrl(baseUrl),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(280.dp),
            contentScale = ContentScale.Crop
        )
        // Status Bar Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF0F1113)),
                        startY = 100f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(140.dp))

            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                AsyncImage(
                    model = item.getImageUrl(baseUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .width(120.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                    Text(
                        item.title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 32.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${state.seasons.size} Seasons • TV-MA",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }

            // Play Button
            Button(
                onClick = {
                    val firstEpisode = state.episodes.firstOrNull()
                    if (firstEpisode != null) onPlayEpisode(firstEpisode, item, state.episodes) else onPlay()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24D366)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Play ${state.selectedSeason?.title ?: "Season 1"}",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            // Seasons Row acting as Tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.seasons) { season ->
                    MobileSeasonCard(season, baseUrl, isSelected = season.id == state.selectedSeason?.id) {
                        viewModel.selectSeason(season)
                    }
                }
            }

            // Episodes
            if (state.isLoadingEpisodes) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF24D366))
                }
            } else {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    state.episodes.forEach { episode ->
                        MobileEpisodeItem(episode, baseUrl) { onPlayEpisode(episode, item, state.episodes) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Fixed Back Button with Status Bar Padding
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
    }
}

@Composable
fun MobileSeasonCard(season: MediaItem, baseUrl: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) Color(0xFF24D366).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick() }
            .border(1.dp, if (isSelected) Color(0xFF24D366) else Color.Transparent, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                season.title,
                color = if (isSelected) Color(0xFF24D366) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                season.overview?.takeIf { it.isNotBlank() } ?: "No description",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MobileEpisodeItem(episode: MediaItem, baseUrl: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(140.dp).height(80.dp).clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = episode.getImageUrl(baseUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }

            // Progress Bar if watching
            if (episode.playbackPositionTicks > 0 && episode.runTimeTicks != null && episode.runTimeTicks > 0) {
                val progress = episode.playbackPositionTicks.toFloat() / episode.runTimeTicks.toFloat()
                Box(
                    modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.White.copy(alpha = 0.3f))
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(progress).height(4.dp).background(Color(0xFF24D366))
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                episode.title,
                color = if (episode.isPlayed) Color.White.copy(alpha = 0.5f) else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Episode ${episode.index ?: ""} ${if (episode.isPlayed) "• Played" else ""}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}
