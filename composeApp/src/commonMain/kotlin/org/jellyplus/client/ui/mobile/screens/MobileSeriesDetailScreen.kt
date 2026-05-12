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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var overviewExpanded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(item.userData?.isFavorite == true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1113))
            .verticalScroll(rememberScrollState())
    ) {
        // ── Backdrop ────────────────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            AsyncImage(
                model = item.getBackdropUrl(baseUrl) ?: item.getImageUrl(baseUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Top gradient — darkens status bar area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)
                        )
                    )
            )
            // Bottom gradient — fades into background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF0F1113))
                        )
                    )
            )
            // X close button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // ── Title ───────────────────────────────────────────────────────────
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 28.sp,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp)
        )

        // ── Metadata row ─────────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (state.seasons.isNotEmpty()) {
                Text(
                    "${state.seasons.size} Season${if (state.seasons.size > 1) "s" else ""}",
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 13.sp
                )
                Text("•", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp)
            }
            item.year?.let {
                Text("$it", color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp)
                Text("•", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp)
            }
            Text(
                "HD",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(3.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }

        // ── Rating ──────────────────────────────────────────────────────────
        item.rating?.let { rating ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(15.dp))
                Text(
                    "${(rating * 10).toInt() / 10f} / 10",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        // ── Action icons ────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            DetailActionButton(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = "My List",
                tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                onClick = { isFavorite = !isFavorite }
            )
            DetailActionButton(Icons.Default.Share, "Share")
            DetailActionButton(Icons.Default.Download, "Download")
        }

        // ── Play button ─────────────────────────────────────────────────────
        Button(
            onClick = {
                val firstEpisode = state.episodes.firstOrNull()
                if (firstEpisode != null) onPlayEpisode(firstEpisode, item, state.episodes) else onPlay()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .height(52.dp)
        ) {
            Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Play ${state.selectedSeason?.title ?: "Season 1"}",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // ── Overview ────────────────────────────────────────────────────────
        val overview = item.overview
        if (!overview.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = overview,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                maxLines = if (overviewExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = if (overviewExpanded) "Less" else "More",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 16.dp, top = 2.dp)
                    .clickable { overviewExpanded = !overviewExpanded }
            )
        }

        // ── Season tabs ─────────────────────────────────────────────────────
        if (state.seasons.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            DetailSectionHeader("Episodes")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.seasons) { season ->
                    MobileSeasonTab(
                        season = season,
                        isSelected = season.id == state.selectedSeason?.id,
                        onClick = { viewModel.selectSeason(season) }
                    )
                }
            }
        }

        // ── Episodes ─────────────────────────────────────────────────────────
        Spacer(Modifier.height(16.dp))
        if (state.isLoadingEpisodes) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                state.episodes.forEach { episode ->
                    MobileEpisodeItem(episode, baseUrl) { onPlayEpisode(episode, item, state.episodes) }
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun MobileSeasonTab(season: MediaItem, isSelected: Boolean, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(
                if (isSelected) accent.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(20.dp)
            )
            .border(
                1.dp,
                if (isSelected) accent else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            season.title,
            color = if (isSelected) accent else Color.White.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}

@Composable
fun MobileSeasonCard(season: MediaItem, baseUrl: String, isSelected: Boolean, onClick: () -> Unit) {
    MobileSeasonTab(season = season, isSelected = isSelected, onClick = onClick)
}

@Composable
fun MobileEpisodeItem(episode: MediaItem, baseUrl: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(140.dp).height(80.dp).clip(RoundedCornerShape(8.dp))) {
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
            // Progress bar
            if (episode.playbackPositionTicks > 0 && episode.runTimeTicks != null && episode.runTimeTicks > 0) {
                val progress = episode.playbackPositionTicks.toFloat() / episode.runTimeTicks.toFloat()
                Box(
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                        .background(Color.White.copy(alpha = 0.25f))
                        .align(Alignment.BottomCenter)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                episode.title,
                color = if (episode.isPlayed) Color.White.copy(alpha = 0.45f) else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val subText = buildString {
                episode.index?.let { append("E$it") }
                if (episode.isPlayed) {
                    if (isNotEmpty()) append("  •  ")
                    append("Watched")
                }
            }
            if (subText.isNotEmpty()) {
                Text(subText, color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp)
            }
        }
    }
}
