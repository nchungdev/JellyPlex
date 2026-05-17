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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.ui.mobile.screens.HeroActionRow
import org.jellyplus.client.ui.mobile.screens.MetaDot
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
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
    isFavorite: (MediaItem) -> Boolean = { false },
    onToggleFavorite: (MediaItem) -> Unit = {},
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
        contentPadding = PaddingValues(top = 0.dp, bottom = paddingValues.calculateBottomPadding() + 24.dp),
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

                // The hero banner already has 16.dp internal bottom padding, so
                // the section right after it uses a reduced top spacer to keep
                // the hero→section gap equal to the section→section gap (20.dp).
                var afterHero = false
                fun topGap(base: Dp = 20.dp): Dp = if (afterHero) (base - 16.dp).coerceAtLeast(0.dp) else base
                orderedSections.filter { it in enabledSections }.forEach { sectionId ->
                    when (sectionId) {
                        HomeSectionIdHero -> if (homeState.featuredItems.isNotEmpty()) {
                            val heroes = homeState.featuredItems.take(5)
                            item {
                                HeroPager(
                                    items = heroes,
                                    baseUrl = baseUrl,
                                    onMediaClick = onMediaClick,
                                    onContinueWatchingClick = onContinueWatchingClick,
                                    onToggleWatchLater = onToggleWatchLater,
                                    isWatchLater = isWatchLater,
                                    isFavorite = isFavorite,
                                    onToggleFavorite = onToggleFavorite,
                                )
                            }
                            afterHero = true
                        }
                        HomeSectionIdContinue -> if (homeState.resumeItems.isNotEmpty()) {
                            val gap = topGap(); afterHero = false
                            item {
                                Spacer(Modifier.height(gap))
                                SectionHeader("Continue Watching", onViewAll = onContinueWatchingHeaderClick)
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(homeState.resumeItems) { item ->
                                        MobileContinueWatchingCard(item, baseUrl, onClick = { onContinueWatchingClick(item) })
                                    }
                                }
                            }
                        }
                        HomeSectionIdRecent -> if (homeState.recentlyAddedItems.isNotEmpty()) {
                            val gap = topGap(); afterHero = false
                            item {
                                Spacer(Modifier.height(gap))
                                SectionHeader("Recently Added")
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(homeState.recentlyAddedItems) { item ->
                                        MobilePosterCard(item, baseUrl, onClick = { onMediaClick(item) })
                                    }
                                }
                            }
                        }
                        HomeSectionIdGenreRows -> topGenres.forEach { (genre, genreItems) ->
                            val gap = topGap(); afterHero = false
                            item {
                                Spacer(Modifier.height(gap))
                                SectionHeader(genre)
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(genreItems.take(10)) { item ->
                                        MobilePosterCard(item, baseUrl, onClick = { onMediaClick(item) })
                                    }
                                }
                            }
                        }
                        HomeSectionIdMovies -> if (state.movies.isNotEmpty()) {
                            val gap = topGap(); afterHero = false
                            item {
                                Spacer(Modifier.height(gap))
                                SectionHeader("Movies", onViewAll = { onViewAll(MediaType.MOVIE, "Movies") })
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(state.movies) { item ->
                                        MobilePosterCard(item, baseUrl, onClick = { onMediaClick(item) })
                                    }
                                }
                            }
                        }
                        HomeSectionIdTv -> if (state.tvShows.isNotEmpty()) {
                            val gap = topGap(); afterHero = false
                            item {
                                Spacer(Modifier.height(gap))
                                SectionHeader("TV Series", onViewAll = { onViewAll(MediaType.SERIES, "TV Series") })
                                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    items(state.tvShows) { item ->
                                        MobilePosterCard(item, baseUrl, onClick = { onMediaClick(item) })
                                    }
                                }
                            }
                        }
                        HomeSectionIdGenres -> if (allGenres.isNotEmpty()) {
                            val gap = topGap(28.dp); afterHero = false
                            item {
                                Spacer(Modifier.height(gap))
                                SectionHeader("Genres")
                                Spacer(Modifier.height(12.dp))
                                GenreGrid(allGenres.take(10), allGenres.size > 10, onViewAllGenre)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Hero pager ────────────────────────────────────────────────────────────────

@Composable
private fun HeroPager(
    items: List<MediaItem>,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
    onToggleWatchLater: (MediaItem) -> Unit,
    isWatchLater: (MediaItem) -> Boolean,
    isFavorite: (MediaItem) -> Boolean,
    onToggleFavorite: (MediaItem) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { page ->
        HeroBanner(
            items[page], baseUrl, onMediaClick, onContinueWatchingClick,
            onToggleWatchLater, isWatchLater, isFavorite, onToggleFavorite,
            pageCount = items.size, currentPage = pagerState.currentPage,
        )
    }
}

@Composable
private fun HeroPageDots(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { i ->
            val selected = currentPage == i
            Box(
                modifier = Modifier
                    .size(width = if (selected) 18.dp else 6.dp, height = 6.dp)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else Color.White.copy(alpha = 0.4f),
                        RoundedCornerShape(3.dp),
                    ),
            )
        }
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
    isFavorite: (MediaItem) -> Boolean,
    onToggleFavorite: (MediaItem) -> Unit,
    pageCount: Int = 1,
    currentPage: Int = 0,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onMediaClick(item) },
    ) {
        AsyncImage(
            model = item.getBackdropUrl(baseUrl) ?: item.getImageUrl(baseUrl),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight(0.3f).align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth().fillMaxHeight(0.65f).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF181818))))
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Default.Star, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    "FEATURED TODAY",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                item.title,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 28.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item.year?.let {
                    Text("$it", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    MetaDot()
                }
                item.genres?.firstOrNull()?.let {
                    Text(it, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    MetaDot()
                }
                Text(
                    "HD",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                )
                item.rating?.let { rating ->
                    MetaDot()
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(13.dp))
                    Text("${(rating * 10).toInt() / 10f}", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            HeroActionRow(
                isWatchLater = isWatchLater(item),
                isFavorite = isFavorite(item),
                onPlay = { onContinueWatchingClick(item) },
                onToggleWatchLater = { onToggleWatchLater(item) },
                onToggleFavorite = { onToggleFavorite(item) },
                compactPlay = true,
            )
        }
    }
}

// ── State views ───────────────────────────────────────────────────────────────

@Composable
private fun HomeErrorState(error: String?, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 100.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CloudOff, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
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

    Column(modifier = Modifier.width(164.dp).clickable { onClick() }) {
        Box(modifier = Modifier.height(101.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp))) {
            AsyncImage(model = item.getBackdropUrl(baseUrl), contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (progress > 0) {
                Box(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).background(Color.Gray.copy(alpha = 0.5f))) {
                    Box(modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(item.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        val subText = if (item.type == MediaType.EPISODE) "S${item.parentIndexNumber ?: 0}E${item.index ?: 0}" else "Resume watching"
        Text(subText, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
private fun MobilePosterCard(item: MediaItem, baseUrl: String, onClick: () -> Unit) {
    Column(modifier = Modifier.width(120.dp)) {
        MediaPoster(item, baseUrl, onClick = onClick, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        Text(
            item.title,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            lineHeight = 15.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
