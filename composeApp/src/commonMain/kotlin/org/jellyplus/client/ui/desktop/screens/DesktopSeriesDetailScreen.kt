package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.components.DetailActionIcon
import org.jellyplus.client.ui.viewmodels.SeriesDetailViewModel

@Composable
fun DesktopSeriesDetailScreen(
    item: MediaItem,
    viewModel: SeriesDetailViewModel,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onPlayEpisode: (MediaItem, MediaItem?, List<MediaItem>) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val baseUrl = state.baseUrl
    val playFocusRequester = remember { FocusRequester() }
    val backFocusRequester = remember { FocusRequester() }
    val seasonsTabFocusRequester = remember { FocusRequester() }
    val overviewTabFocusRequester = remember { FocusRequester() }

    // Map of FocusRequesters for each season chip
    val seasonFocusRequesters = remember(state.seasons) {
        state.seasons.associate { it.id to FocusRequester() }
    }

    // Logic to request focus when the screen is loaded/resumed
    LaunchedEffect(state.seasons, state.selectedSeason, state.selectedTabIndex) {
        if (state.seasons.isNotEmpty()) {
            kotlinx.coroutines.delay(200) // Small delay to ensure UI is ready
            val targetSeasonId = state.selectedSeason?.id
            if (state.selectedTabIndex == 0 && targetSeasonId != null) {
                // Focus the currently selected season chip
                seasonFocusRequesters[targetSeasonId]?.requestFocus()
            } else if (state.selectedTabIndex == 1) {
                overviewTabFocusRequester.requestFocus()
            } else {
                playFocusRequester.requestFocus()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1113))) {
        AsyncImage(
            model = item.getBackdropUrl(baseUrl),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(500.dp),
            contentScale = ContentScale.Crop
        )
        // Solid Dim Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .background(Color.Black.copy(alpha = 0.5f))
        )
        // Gradient Fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF0F1113)),
                        startY = 100f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(200.dp))

            Column(modifier = Modifier.padding(horizontal = 48.dp)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 64.sp,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Action, Drama", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
                    Text(
                        "  •  TV-MA",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        "  •  ${item.year?.toString() ?: ""}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        "  •  ${state.seasons.size} Seasons",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Play Button & Actions
                val addFocusRequester = remember { FocusRequester() }
                val favFocusRequester = remember { FocusRequester() }
                val shareFocusRequester = remember { FocusRequester() }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var isPlayFocused by remember { mutableStateOf(false) }
                    Button(
                        onClick = {
                            val firstEpisode = state.episodes.firstOrNull()
                            if (firstEpisode != null) onPlayEpisode(firstEpisode, item, state.episodes)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlayFocused) Color.White else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .height(56.dp)
                            .width(200.dp)
                            .focusRequester(playFocusRequester)
                            .onFocusChanged { isPlayFocused = it.isFocused }
                            .focusProperties {
                                up = backFocusRequester
                                down = seasonsTabFocusRequester
                                right = addFocusRequester
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        val label =
                            if (state.selectedSeason != null) "Play ${state.selectedSeason?.title}" else "Play Season 1"
                        Text(
                            label,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailActionIcon(
                            Icons.Default.Add,
                            "Add to list",
                            modifier = Modifier.focusRequester(addFocusRequester).focusProperties {
                                up = backFocusRequester
                                down = seasonsTabFocusRequester
                                left = playFocusRequester
                                right = favFocusRequester
                            })
                        DetailActionIcon(
                            Icons.Default.FavoriteBorder,
                            "Favorite",
                            modifier = Modifier.focusRequester(favFocusRequester).focusProperties {
                                up = backFocusRequester
                                down = overviewTabFocusRequester
                                left = addFocusRequester
                                right = shareFocusRequester
                            })
                        DetailActionIcon(
                            Icons.Default.Share,
                            "Share",
                            modifier = Modifier.focusRequester(shareFocusRequester).focusProperties {
                                up = backFocusRequester
                                down = overviewTabFocusRequester
                                left = favFocusRequester
                            })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tabs
                val tabs = listOf("Seasons", "Overview")

                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    tabs.forEachIndexed { index, title ->
                        var isFocused by remember { mutableStateOf(false) }
                        val isSelected = state.selectedTabIndex == index
                        val tabRequester = if (index == 0) seasonsTabFocusRequester else overviewTabFocusRequester

                        Column(
                            modifier = Modifier
                                .focusRequester(tabRequester)
                                .onFocusChanged {
                                    isFocused = it.isFocused
                                    if (it.isFocused) viewModel.selectTabIndex(index)
                                }
                                .focusProperties {
                                    up = if (index == 0) playFocusRequester else shareFocusRequester
                                }
                                .focusable()
                                .clickable { viewModel.selectTabIndex(index) }
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = title,
                                color = if (isFocused || isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                                fontSize = 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Spacer(Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(3.dp)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (state.selectedTabIndex) {
                    0 -> {
                        // Seasons as Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.seasons) { season ->
                                val isSelected = season.id == state.selectedSeason?.id
                                var isFocused by remember { mutableStateOf(false) }
                                val requester = seasonFocusRequesters[season.id] ?: remember { FocusRequester() }

                                Surface(
                                    onClick = { viewModel.selectSeason(season) },
                                    color = when {
                                        isFocused -> Color.White
                                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else -> Color.White.copy(alpha = 0.05f)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .focusRequester(requester)
                                        .onFocusChanged {
                                            isFocused = it.isFocused
                                            if (it.isFocused) viewModel.selectSeason(season)
                                        }
                                        .focusProperties {
                                            up = seasonsTabFocusRequester
                                        }
                                        .focusable()
                                        .border(
                                            width = 2.dp,
                                            color = if (isFocused) MaterialTheme.colorScheme.primary else if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Text(
                                        text = season.title,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                        color = if (isFocused) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        // Episodes
                        Spacer(modifier = Modifier.height(12.dp))
                        if (state.isLoadingEpisodes) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(24.dp))
                        } else if (state.episodes.isEmpty()) {
                            Text("No episodes found", color = Color.White.copy(alpha = 0.4f), fontSize = 16.sp)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.episodes.forEach { episode ->
                                    EpisodeCard(
                                        episode = episode,
                                        baseUrl = baseUrl,
                                        onClick = { onPlayEpisode(episode, item, state.episodes) },
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // Overview Tab
                        val overview = item.overview?.takeIf { it.isNotBlank() }
                        if (overview != null) {
                            Text(
                                text = overview,
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 18.sp,
                                lineHeight = 28.sp,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                            Spacer(modifier = Modifier.height(48.dp))
                        }

                        val actors = item.people?.filter { it.type == "Actor" }?.take(5)?.joinToString { it.name }
                        val genres = item.genres?.joinToString()

                        Row(horizontalArrangement = Arrangement.spacedBy(64.dp)) {
                            if (!actors.isNullOrBlank()) {
                                MetaItem("Cast", actors)
                            }
                            if (!genres.isNullOrBlank()) {
                                MetaItem("Genres", genres)
                            }
                            MetaItem("Rating", "${item.rating ?: "N/A"}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Floating Back Button
        var isBackFocused by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .padding(32.dp)
                .size(48.dp)
                .focusRequester(backFocusRequester)
                .onFocusChanged { isBackFocused = it.isFocused }
                .focusable()
                .clickable { onBack() }
                .focusProperties {
                    down = playFocusRequester
                }
                .background(
                    if (isBackFocused) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.5f),
                    CircleShape
                )
                .border(
                    width = 2.dp,
                    color = if (isBackFocused) Color.White else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Back",
                tint = if (isBackFocused) Color.Black else Color.White
            )
        }
    }
}

@Composable
private fun EpisodeCard(episode: MediaItem, baseUrl: String, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isFocused) Color.White.copy(alpha = 0.08f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused },
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.width(160.dp).height(90.dp).clip(RoundedCornerShape(6.dp))) {
                AsyncImage(
                    model = episode.getBackdropUrl(baseUrl) ?: episode.getImageUrl(baseUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )

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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildString {
                        episode.index?.let { append("E${it.toString().padStart(2, '0')}  ") }
                        append(episode.title)
                    },
                    color = if (isFocused) MaterialTheme.colorScheme.primary else if (episode.isPlayed) Color.White.copy(alpha = 0.5f) else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                episode.overview?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                Icons.Default.PlayCircle,
                null,
                tint = if (isFocused) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}


@Composable
private fun MetaItem(label: String, value: String) {
    Column {
        Text(label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
    }
}
