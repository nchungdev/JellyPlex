package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.components.MediaPoster
import org.jellyplus.client.ui.viewmodels.SeriesDetailViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MobileSeriesDetailScreen(
    item: MediaItem,
    viewModel: SeriesDetailViewModel,
    recommendedItems: List<MediaItem> = emptyList(),
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onPlayEpisode: (MediaItem, MediaItem?, List<MediaItem>) -> Unit,
    onRecommendedClick: (MediaItem) -> Unit = {},
    isFavorite: (MediaItem) -> Boolean,
    onToggleFavorite: (MediaItem) -> Unit,
    isWatchLater: (MediaItem) -> Boolean,
    onToggleWatchLater: (MediaItem) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val baseUrl = state.baseUrl
    var overviewExpanded by remember { mutableStateOf(false) }
    val favorite = isFavorite(item)
    val watchLater = isWatchLater(item)
    val listState = rememberLazyListState()
    val collapseProgress by remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 1f
                else -> (listState.firstVisibleItemScrollOffset / 420f).coerceIn(0f, 1f)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF181818)),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Backdrop + overlaid info ─────────────────────────────────────────
            item {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                AsyncImage(
                    model = item.getBackdropUrl(baseUrl) ?: item.getImageUrl(baseUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Top gradient — status bar readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.3f)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)
                            )
                        )
                )
                // Bottom scrim — behind info overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF181818))
                            )
                        )
                )
                // Info overlay at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .graphicsLayer {
                            alpha = (1f - collapseProgress * 1.35f).coerceIn(0f, 1f)
                            translationY = -28f * collapseProgress
                        }
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 28.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (state.seasons.isNotEmpty()) {
                            Text(
                                "${state.seasons.size} Season${if (state.seasons.size > 1) "s" else ""}",
                                color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp
                            )
                            Text("•", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp)
                        }
                        item.year?.let {
                            Text("$it", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            Text("•", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp)
                        }
                        Text(
                            "HD",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                        item.rating?.let { rating ->
                            Text("•", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp)
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(13.dp))
                            Text("${(rating * 10).toInt() / 10f}", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                        DetailActionButton(
                            icon = Icons.Default.Add,
                            label = "Later",
                            tint = if (watchLater) MaterialTheme.colorScheme.primary else Color.White,
                            onClick = { onToggleWatchLater(item) }
                        )
                        DetailActionButton(
                            icon = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            label = "Favorite",
                            tint = if (favorite) MaterialTheme.colorScheme.primary else Color.White,
                            onClick = { onToggleFavorite(item) }
                        )
                        DetailActionButton(Icons.Default.Share, "Share")
                        DetailActionButton(Icons.Default.Download, "Download")
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val firstEpisode = state.episodes.firstOrNull()
                            if (firstEpisode != null) onPlayEpisode(firstEpisode, item, state.episodes) else onPlay()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Play ${state.selectedSeason?.title ?: "Season 1"}",
                            color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                }
            }
            }

            // ── Overview ────────────────────────────────────────────────────────
            if (!item.overview.isNullOrBlank()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = item.overview,
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
            }

            // ── Season tabs — sticky header ──────────────────────────────────────
            if (state.seasons.isNotEmpty()) {
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF181818))
                    ) {
                        if (collapseProgress > 0.85f) {
                            Spacer(
                                modifier = Modifier
                                    .statusBarsPadding()
                                    .height(56.dp)
                            )
                        }
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
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
                }
            }

            // ── Episodes ─────────────────────────────────────────────────────────
            if (state.isLoadingEpisodes) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                items(state.episodes) { episode ->
                    MobileEpisodeItem(
                        episode = episode,
                        baseUrl = baseUrl,
                        onClick = { onPlayEpisode(episode, item, state.episodes) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            if (recommendedItems.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    DetailSectionHeader("Similar")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recommendedItems) { related ->
                            MediaPoster(
                                item = related,
                                baseUrl = baseUrl,
                                onClick = { onRecommendedClick(related) },
                                modifier = Modifier.width(118.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }

        MobileSeriesCollapsingBar(
            title = item.title,
            progress = collapseProgress,
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(1f),
        )
    }
}

@Composable
private fun MobileSeriesCollapsingBar(
    title: String,
    progress: Float,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF181818).copy(alpha = progress.coerceIn(0f, 1f)))
            .statusBarsPadding()
            .height(56.dp)
            .padding(start = 4.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.48f + progress * 0.22f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    alpha = progress.coerceIn(0f, 1f)
                    translationY = (1f - progress) * 18f
                    translationX = (1f - progress) * 28f
                },
        )
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
fun MobileEpisodeItem(
    episode: MediaItem,
    baseUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
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
            if (episode.playbackPositionTicks > 0 && episode.runTimeTicks != null && episode.runTimeTicks > 0) {
                val progress = episode.playbackPositionTicks.toFloat() / episode.runTimeTicks.toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
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
