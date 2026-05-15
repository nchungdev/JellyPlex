package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.components.MediaPoster
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
    val scope = rememberCoroutineScope()
    val seasonListState = rememberLazyListState()
    val episodeListState = rememberLazyListState()
    val episodeRowHeight = 180.dp
    val episodeWideItemWidth = 232.dp
    val episodeItemSpacing = 12.dp
    var episodeCenterJob by remember { mutableStateOf<Job?>(null) }
    fun alignEpisodeToStart(index: Int) {
        episodeCenterJob?.cancel()
        episodeCenterJob = scope.launch {
            delay(35)
            episodeListState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(state.seasons) {
        if (state.selectedSeason == null && state.seasons.isNotEmpty()) {
            viewModel.selectSeason(state.seasons.first())
        }
    }

    LaunchedEffect(state.selectedSeason?.id) {
        episodeListState.scrollToItem(0)
    }

    DesktopHeroDetailScaffold(
        item = item,
        baseUrl = baseUrl,
        primaryLabel = "Watch",
        metadata = buildString {
            val genres = item.genres?.take(3)?.joinToString("   ")
            if (!genres.isNullOrBlank()) append(genres) else append("TV Series")
            item.year?.let { append("   $it") }
            if (state.seasons.isNotEmpty()) append("   ${state.seasons.size} Seasons")
        },
        onBack = onBack,
        onPrimaryAction = {
            val firstEpisode = state.episodes.firstOrNull()
            if (firstEpisode != null) onPlayEpisode(firstEpisode, item, state.episodes) else onPlay()
        },
        overview = item.overview,
        detailContentSpacing = 72.dp,
        focusScrollBottomClearance = 24.dp,
    ) {
        if (state.seasons.isNotEmpty() || state.episodes.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (state.seasons.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        state = seasonListState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(state.seasons, key = { _, season -> season.id }) { index, season ->
                            SeasonChip(
                                title = season.title,
                                selected = season.id == state.selectedSeason?.id,
                                onClick = { viewModel.selectSeason(season) },
                                onFocus = {
                                    scope.launch { seasonListState.animateScrollToItem(index) }
                                },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(episodeRowHeight),
                ) {
                    when {
                        state.isLoadingEpisodes -> EpisodeLoadingContent(Modifier.fillMaxSize())
                        state.episodes.isNotEmpty() -> {
                            LazyRow(
                                modifier = Modifier.fillMaxSize(),
                                state = episodeListState,
                                contentPadding = PaddingValues(top = 18.dp, bottom = 22.dp),
                                horizontalArrangement = Arrangement.spacedBy(episodeItemSpacing),
                            ) {
                                itemsIndexed(state.episodes, key = { _, episode -> episode.id }) { index, episode ->
                                    MediaPoster(
                                        item = episode,
                                        baseUrl = baseUrl,
                                        onClick = { onPlayEpisode(episode, item, state.episodes) },
                                        onFocus = { alignEpisodeToStart(index) },
                                        aspectRatio = 16f / 9f,
                                        showLabel = true,
                                        modifier = Modifier
                                            .width(episodeWideItemWidth)
                                            .onKeyEvent { event ->
                                                event.type == KeyEventType.KeyDown &&
                                                    event.key == Key.DirectionRight &&
                                                    index == state.episodes.lastIndex
                                            },
                                    )
                                }
                            }
                        }
                        else -> EmptyEpisodeContent(Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeLoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(34.dp),
        )
    }
}

@Composable
private fun EmptyEpisodeContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            "No episodes found",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SeasonChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    onFocus: () -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        color = when {
            focused -> Color.White
            selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
            else -> Color.White.copy(alpha = 0.08f)
        },
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(
            width = 2.dp,
            color = when {
                focused -> MaterialTheme.colorScheme.primary
                selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                else -> Color.Transparent
            },
        ),
        modifier = Modifier
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) {
                    onFocus()
                    if (!selected) onClick()
                }
            },
    ) {
        Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)) {
            Text(
                title,
                color = if (focused) Color.Black else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
