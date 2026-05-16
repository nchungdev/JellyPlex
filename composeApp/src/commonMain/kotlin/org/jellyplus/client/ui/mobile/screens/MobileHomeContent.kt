package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.ui.components.MediaPoster
import org.jellyplus.client.ui.home.HomeSectionIdContinue
import org.jellyplus.client.ui.home.HomeSectionIdGenreRows
import org.jellyplus.client.ui.home.HomeSectionIdGenres
import org.jellyplus.client.ui.home.HomeSectionIdHero
import org.jellyplus.client.ui.home.HomeSectionIdMovies
import org.jellyplus.client.ui.home.HomeSectionIdRecent
import org.jellyplus.client.ui.home.HomeSectionIdTv
import org.jellyplus.client.ui.home.orderedHomeSectionIds
import org.jellyplus.client.ui.home.parseHomeSectionIds
import org.jellyplus.client.ui.viewmodels.HomeViewModel
import org.jellyplus.client.ui.viewmodels.MainState
import org.jellyplus.client.ui.viewmodels.MainViewModel

@Composable
internal fun HomeContent(
    viewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    state: MainState,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
    onContinueWatchingHeaderClick: () -> Unit,
    onViewAll: (MediaType, String) -> Unit,
    onViewAllGenre: (String) -> Unit = {},
    onToggleWatchLater: (MediaItem) -> Unit = {},
    isWatchLater: (MediaItem) -> Boolean = { false },
    paddingValues: PaddingValues,
    homeSectionOrder: String,
    homeEnabledSections: String,
    onHeaderAlphaChange: (Float) -> Unit = {},
) {
    val homeState by homeViewModel.state.collectAsState()
    val hasHomeContent = homeState.featuredItems.isNotEmpty() ||
        homeState.resumeItems.isNotEmpty() || homeState.recentlyAddedItems.isNotEmpty()
    val hasMainContent = state.movies.isNotEmpty() || state.tvShows.isNotEmpty()
    val isHomeLoading = !hasHomeContent && !hasMainContent && (homeState.isLoading || state.isLoading)
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val orderedSections = remember(homeSectionOrder) { orderedHomeSectionIds(homeSectionOrder) }
    val enabledSections = remember(homeEnabledSections) { parseHomeSectionIds(homeEnabledSections).toSet() }
    val heroCollapseDistancePx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val headerAlpha by remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 1f
                else -> (listState.firstVisibleItemScrollOffset / heroCollapseDistancePx).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(headerAlpha, isHomeLoading, homeState.featuredItems.isNotEmpty()) {
        onHeaderAlphaChange(if (isHomeLoading || homeState.featuredItems.isEmpty()) 1f else headerAlpha)
    }

    if (isHomeLoading) {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), bottom = paddingValues.calculateBottomPadding()),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
        return
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 0.dp, bottom = paddingValues.calculateBottomPadding()),
    ) {
        when {
            homeState.error != null && !hasHomeContent && !hasMainContent -> item {
                HomeErrorState(homeState.error, onRetry = { homeViewModel.loadHomeContent(); viewModel.loadData() })
            }
            !hasHomeContent && !hasMainContent -> item { HomeEmptyState() }
            else -> {
                val allItems = (homeState.recentlyAddedItems + state.movies + state.tvShows).distinctBy { it.id }
                val topGenres = allItems
                    .flatMap { item -> (item.genres ?: emptyList()).map { it to item } }
                    .groupBy({ it.first }, { it.second })
                    .filter { it.value.size >= 2 }
                    .entries.sortedByDescending { it.value.size }.take(5)
                val allGenres = (homeState.recentlyAddedItems + state.movies + state.tvShows)
                    .flatMap { it.genres ?: emptyList() }.distinct().sorted()

                orderedSections.filter { it in enabledSections }.forEach { sectionId ->
                    when (sectionId) {
                        HomeSectionIdHero -> homeState.featuredItems.firstOrNull()?.let { hero ->
                            item { HeroBanner(hero, baseUrl, onMediaClick, onContinueWatchingClick, onToggleWatchLater, isWatchLater) }
                        }
                        HomeSectionIdContinue -> if (homeState.resumeItems.isNotEmpty()) item {
                            Spacer(Modifier.height(20.dp))
                            SectionHeader("Continue Watching", onViewAll = onContinueWatchingHeaderClick)
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(homeState.resumeItems) { item ->
                                    MobileContinueWatchingCard(item, baseUrl, onClick = { onContinueWatchingClick(item) })
                                }
                            }
                        }
                        HomeSectionIdRecent -> if (homeState.recentlyAddedItems.isNotEmpty()) item {
                            Spacer(Modifier.height(20.dp))
                            SectionHeader("Recently Added")
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(homeState.recentlyAddedItems) { item ->
                                    MediaPoster(item, baseUrl, onClick = { onMediaClick(item) }, modifier = Modifier.width(120.dp))
                                }
                            }
                        }
                        HomeSectionIdGenreRows -> topGenres.forEach { (genre, genreItems) ->
                            item {
                                Spacer(Modifier.height(20.dp))
                                SectionHeader(genre)
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(genreItems.take(10)) { item ->
                                        MediaPoster(item, baseUrl, onClick = { onMediaClick(item) }, modifier = Modifier.width(120.dp))
                                    }
                                }
                            }
                        }
                        HomeSectionIdMovies -> if (state.movies.isNotEmpty()) item {
                            Spacer(Modifier.height(20.dp))
                            SectionHeader("Movies", onViewAll = { onViewAll(MediaType.MOVIE, "Movies") })
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(state.movies) { item ->
                                    MediaPoster(item, baseUrl, onClick = { onMediaClick(item) }, modifier = Modifier.width(120.dp))
                                }
                            }
                        }
                        HomeSectionIdTv -> if (state.tvShows.isNotEmpty()) item {
                            Spacer(Modifier.height(20.dp))
                            SectionHeader("TV Series", onViewAll = { onViewAll(MediaType.SERIES, "TV Series") })
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(state.tvShows) { item ->
                                    MediaPoster(item, baseUrl, onClick = { onMediaClick(item) }, modifier = Modifier.width(120.dp))
                                }
                            }
                        }
                        HomeSectionIdGenres -> if (allGenres.isNotEmpty()) item {
                            Spacer(Modifier.height(28.dp))
                            SectionHeader("Genres")
                            Spacer(Modifier.height(12.dp))
                            GenreGrid(allGenres.take(10), allGenres.size > 10, onViewAllGenre)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

// ── Hero banner ───────────────────────────────────────────────────────────────

@Composable
private fun HeroBanner(
    item: MediaItem,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
    onToggleWatchLater: (MediaItem) -> Unit,
    isWatchLater: (MediaItem) -> Boolean,
) {
    Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).clickable { onMediaClick(item) }) {
        AsyncImage(
            model = item.getBackdropUrl(baseUrl) ?: item.getImageUrl(baseUrl),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        // Top gradient — status bar readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)))
        )
        // Bottom scrim — blends into page background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF181818))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
            Text(
                item.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp,
                maxLines = 3,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                DetailActionButton(
                    icon = Icons.Default.Add,
                    label = "Later",
                    tint = if (isWatchLater(item)) MaterialTheme.colorScheme.primary else Color.White,
                    onClick = { onToggleWatchLater(item) },
                )
                DetailActionButton(
                    icon = Icons.Default.Info,
                    label = "Info",
                    onClick = { onMediaClick(item) },
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { onContinueWatchingClick(item) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Play", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ── State views ───────────────────────────────────────────────────────────────

@Composable
private fun HomeErrorState(error: String?, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 100.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CloudOff, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(24.dp))
        Text("Connection Error", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(error ?: "Unable to reach server", color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("Retry", color = Color.Black)
        }
    }
}

@Composable
private fun HomeEmptyState() {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Inbox, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(100.dp))
        Spacer(Modifier.height(24.dp))
        Text("Library is empty", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("No media found on this server", color = Color.White.copy(alpha = 0.5f))
    }
}

@Composable
private fun GenreGrid(genres: List<String>, hasMore: Boolean, onViewAllGenre: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        genres.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { genre ->
                    Surface(onClick = { onViewAllGenre(genre) }, modifier = Modifier.weight(1f).height(48.dp),
                        color = Color.White.copy(alpha = 0.07f), shape = RoundedCornerShape(12.dp)) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(genre, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (hasMore) {
            TextButton(onClick = {}, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("See all genres", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ── Shared section header ─────────────────────────────────────────────────────

@Composable
internal fun SectionHeader(title: String, onViewAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (onViewAll != null) Modifier.clickable(onClick = onViewAll) else Modifier)
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (onViewAll != null) {
            IconButton(onClick = onViewAll) { Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.5f)) }
        }
    }
}

// ── Continue Watching card ────────────────────────────────────────────────────

@Composable
internal fun MobileContinueWatchingCard(item: MediaItem, baseUrl: String, onClick: () -> Unit) {
    val progress = if (item.runTimeTicks != null && item.runTimeTicks > 0)
        item.playbackPositionTicks.toFloat() / item.runTimeTicks.toFloat() else 0f

    Column(modifier = Modifier.width(256.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).clickable { onClick() }) {
        Box(modifier = Modifier.height(144.dp).fillMaxWidth()) {
            AsyncImage(model = item.getBackdropUrl(baseUrl), contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (progress > 0) {
                Box(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).background(Color.Gray.copy(alpha = 0.5f))) {
                    Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                }
            }
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            val subText = if (item.type == MediaType.EPISODE) "S${item.parentIndexNumber ?: 0}E${item.index ?: 0}" else "Resume watching"
            Text(subText, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, maxLines = 1)
        }
    }
}
