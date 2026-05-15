package org.jellyplus.client.ui.desktop.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.ui.common.navigation.DpadSectionNavigator
import org.jellyplus.client.ui.common.navigation.gridItemDpadHandler
import org.jellyplus.client.ui.common.navigation.rememberDpadGridNavigator
import org.jellyplus.client.ui.common.navigation.rememberDpadSectionNavigator
import org.jellyplus.client.ui.common.navigation.sectionItemDpadHandler
import org.jellyplus.client.ui.components.MediaPoster
import org.jellyplus.client.ui.desktop.DesktopContentLeftPadding
import org.jellyplus.client.ui.desktop.DesktopContentRightPadding
import org.jellyplus.client.ui.desktop.DesktopSidebarLogoSize
import org.jellyplus.client.ui.desktop.DesktopSidebarTopPadding
import org.jellyplus.client.ui.desktop.DesktopSidebarWidth
import org.jellyplus.client.ui.mobile.screens.MobileAuthLogo
import org.jellyplus.client.ui.viewmodels.HomeState
import org.jellyplus.client.ui.viewmodels.HomeViewModel
import org.jellyplus.client.ui.viewmodels.HistoryViewModel
import org.jellyplus.client.ui.viewmodels.MainViewModel
import org.jellyplus.client.ui.viewmodels.SessionViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.lazy.itemsIndexed as lazyListItemsIndexed

private enum class NavDestination { Home, Search, History, Favorites, Profile }

private enum class DashboardSectionClickMode { Detail, Play }

private data class DashboardSectionSpec(
    val title: String,
    val items: List<MediaItem>,
    val viewAllType: MediaType?,
    val clickMode: DashboardSectionClickMode,
    val itemAspectRatio: Float = 2f / 3f,
    val itemWidth: androidx.compose.ui.unit.Dp = 110.dp,
)

private val DashboardPosterWidth = 110.dp
private val DashboardItemSpacing = 12.dp
private val DashboardFocusGutter = 6.dp
private val DashboardWideItemWidth = DashboardPosterWidth * 2 + DashboardItemSpacing
private val DashboardSectionHeaderHeight = 30.dp
private val DashboardSectionRowTopPadding = 18.dp
private val DashboardSectionRowBottomPadding = 22.dp
private val DashboardSectionGap = 38.dp
private const val DashboardMaxVisibleItems = 10
private const val DashboardMinWideItems = 3
private const val DashboardMinPosterItems = 7

private fun dashboardSectionHeight(
    itemWidth: androidx.compose.ui.unit.Dp,
    itemAspectRatio: Float,
): androidx.compose.ui.unit.Dp =
    DashboardSectionHeaderHeight +
        DashboardSectionRowTopPadding +
        (itemWidth / itemAspectRatio) +
        DashboardSectionRowBottomPadding

@Composable
fun DesktopMainScreen(
    viewModel: MainViewModel,
    sessionViewModel: SessionViewModel? = null,
    selectedNavIndex: Int,
    onSelectedNavIndexChange: (Int) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
    onViewAll: (MediaType, String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val homeViewModel: HomeViewModel = koinViewModel()
    val homeState by homeViewModel.state.collectAsState()
    val selectedNav = NavDestination.entries.getOrElse(selectedNavIndex) { NavDestination.Home }
    val sidebarFocusRequester = remember { FocusRequester() }

    val favoriteItems = remember(state.movies, state.tvShows, state.items) {
        (state.movies + state.tvShows + state.items)
            .filter { it.userData?.isFavorite == true }
            .distinctBy { it.id }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1113))) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedNav) {
                NavDestination.Home -> MainDashboard(
                    viewModel = viewModel,
                    state = state,
                    homeState = homeState,
                    onMediaClick = onMediaClick,
                    onContinueWatchingClick = onContinueWatchingClick,
                    onViewAll = onViewAll,
                    onReloadHome = { homeViewModel.loadHomeContent() },
                    onFocusExit = {
                        try { sidebarFocusRequester.requestFocus() } catch (_: IllegalStateException) {}
                    },
                )
                NavDestination.Search -> SearchPlaceholder()
                NavDestination.History -> DesktopHistoryScreen(
                    onContinueWatchingClick = onContinueWatchingClick,
                    onMediaClick = onMediaClick,
                )
                NavDestination.Favorites -> MediaGrid("Favorites", favoriteItems, state.baseUrl, onMediaClick)
                NavDestination.Profile -> DesktopProfilePlaceholder()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(136.dp)
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(alpha = 0.36f),
                            0.48f to Color.Black.copy(alpha = 0.22f),
                            1.00f to Color.Transparent,
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(DesktopSidebarWidth)
                .padding(vertical = DesktopSidebarTopPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MobileAuthLogo(modifier = Modifier.size(DesktopSidebarLogoSize))
            Spacer(Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                NavIcon(Icons.Default.Home, selectedNav == NavDestination.Home, sidebarFocusRequester) { onSelectedNavIndexChange(NavDestination.Home.ordinal) }
                NavIcon(Icons.Default.Search, selectedNav == NavDestination.Search, sidebarFocusRequester) { onSelectedNavIndexChange(NavDestination.Search.ordinal) }
                NavIcon(Icons.Default.History, selectedNav == NavDestination.History, sidebarFocusRequester) { onSelectedNavIndexChange(NavDestination.History.ordinal) }
                NavIcon(Icons.Default.Favorite, selectedNav == NavDestination.Favorites, sidebarFocusRequester) { onSelectedNavIndexChange(NavDestination.Favorites.ordinal) }
            }
            Spacer(Modifier.weight(1f))
            NavIcon(Icons.Default.Person, selectedNav == NavDestination.Profile, sidebarFocusRequester) { onSelectedNavIndexChange(NavDestination.Profile.ordinal) }
        }
    }
}

@Composable
private fun MainDashboard(
    viewModel: MainViewModel,
    state: org.jellyplus.client.ui.viewmodels.MainState,
    homeState: HomeState,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
    onViewAll: (MediaType, String) -> Unit,
    onReloadHome: () -> Unit,
    onFocusExit: () -> Unit,
) {
    val contentHorizontalPadding = DesktopContentLeftPadding
    val heroBottomPadding = 346.dp
    val focusedSectionBottomPadding = 86.dp
    val isHomeLoading = homeState.isLoading || !homeState.hasLoaded
    val dashboardSections = remember(
        homeState.featuredItems,
        homeState.resumeItems,
        homeState.recentlyAddedItems,
        state.movies,
        state.tvShows,
    ) {
        buildList {
            if (homeState.featuredItems.isNotEmpty()) {
                add(DashboardSectionSpec("Suggestion", homeState.featuredItems, null, DashboardSectionClickMode.Detail))
            }
            if (homeState.resumeItems.isNotEmpty()) {
                add(
                    DashboardSectionSpec(
                        title = "Continue Watching",
                        items = homeState.resumeItems,
                        viewAllType = null,
                        clickMode = DashboardSectionClickMode.Play,
                        itemAspectRatio = 16f / 9f,
                        itemWidth = DashboardWideItemWidth,
                    )
                )
            }
            if (homeState.recentlyAddedItems.isNotEmpty()) {
                add(DashboardSectionSpec("Recently Added", homeState.recentlyAddedItems, null, DashboardSectionClickMode.Detail))
            }
            if (state.movies.isNotEmpty()) {
                add(DashboardSectionSpec("Movies", state.movies, MediaType.MOVIE, DashboardSectionClickMode.Detail))
            }
            if (state.tvShows.isNotEmpty()) {
                add(DashboardSectionSpec("TV Series", state.tvShows, MediaType.SERIES, DashboardSectionClickMode.Detail))
            }
        }
    }
    var focusedMediaItem by remember { mutableStateOf<MediaItem?>(null) }
    var focusedSectionIndex by remember { mutableStateOf(0) }
    val navigator = rememberDpadSectionNavigator(count = dashboardSections.size.coerceAtLeast(1), onExitTop = onFocusExit)

    LaunchedEffect(dashboardSections.size) {
        if (dashboardSections.isNotEmpty()) {
            kotlinx.coroutines.delay(100)
            navigator.focusSection(0)
        }
    }

    LaunchedEffect(homeState.featuredItems, dashboardSections) {
        if (focusedMediaItem == null) {
            focusedMediaItem = homeState.featuredItems.firstOrNull() ?: dashboardSections.firstOrNull()?.items?.firstOrNull()
        } else if (dashboardSections.none { section -> section.items.any { it.id == focusedMediaItem?.id } }) {
            focusedMediaItem = homeState.featuredItems.firstOrNull() ?: dashboardSections.firstOrNull()?.items?.firstOrNull()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = focusedMediaItem,
            animationSpec = tween(durationMillis = 320),
            label = "desktopHeroFocusTransition",
        ) { heroItem ->
            heroItem?.let { item ->
                var titleFontSize by remember(item.id) { mutableStateOf(30.sp) }

                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = item.getBackdropUrl(state.baseUrl) ?: item.getImageUrl(state.baseUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Black.copy(alpha = 0.18f),
                                        0.52f to Color.Black.copy(alpha = 0.46f),
                                        1.0f to Color(0xFF0F1113),
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colorStops = arrayOf(
                                        0.00f to Color.Black.copy(alpha = 0.60f),
                                        0.30f to Color.Black.copy(alpha = 0.36f),
                                        0.66f to Color.Transparent,
                                        1.00f to Color.Transparent,
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.00f to Color.Transparent,
                                        0.34f to Color.Transparent,
                                        0.64f to Color.Black.copy(alpha = 0.24f),
                                        1.00f to Color.Black.copy(alpha = 0.76f),
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = contentHorizontalPadding, bottom = heroBottomPadding)
                            .fillMaxWidth(0.62f),
                    ) {
                        val metadata = buildDesktopHeroMetadata(item)
                        if (metadata.isNotBlank()) {
                            Text(
                                text = metadata,
                                color = Color.White.copy(alpha = 0.68f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = titleFontSize,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif,
                            lineHeight = titleFontSize * 1.22f,
                            maxLines = 2,
                            overflow = TextOverflow.Visible,
                            onTextLayout = { result ->
                                if (result.hasVisualOverflow && titleFontSize > 18.sp) {
                                    titleFontSize *= 0.85f
                                }
                            },
                        )
                        DesktopHeroInfoRow(item)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
        ) {
            if (isHomeLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center),
                )
            } else if (homeState.error != null && dashboardSections.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Connection Error", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(homeState.error ?: "Unable to connect to server", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                    Spacer(Modifier.height(24.dp))
                    org.jellyplus.client.ui.components.FocusableButton(
                        onClick = {
                            onReloadHome()
                            viewModel.loadData()
                        },
                        modifier = Modifier.width(160.dp).height(48.dp)
                    ) {
                        Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (dashboardSections.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inbox, null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(100.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Library is empty", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                dashboardSections.forEachIndexed { index, section ->
                    if (index > focusedSectionIndex + 1) return@forEachIndexed
                    val sectionHeight = dashboardSectionHeight(section.itemWidth, section.itemAspectRatio)
                    DashboardSection(
                        title = section.title,
                        items = section.items,
                        baseUrl = state.baseUrl,
                        isLoading = false,
                        onItemFocus = {
                            focusedSectionIndex = index
                            focusedMediaItem = it
                        },
                        onItemClick = { item ->
                            if (section.clickMode == DashboardSectionClickMode.Play) onContinueWatchingClick(item) else onMediaClick(item)
                        },
                        onViewAll = section.viewAllType?.let { type -> { onViewAll(type, section.title) } },
                        itemAspectRatio = section.itemAspectRatio,
                        itemWidth = section.itemWidth,
                        horizontalPadding = contentHorizontalPadding,
                        sectionIndex = index,
                        focusedSectionIndex = focusedSectionIndex,
                        onFocusedSectionIndexChange = { focusedSectionIndex = it },
                        navigator = navigator,
                        onExitLeft = onFocusExit,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .then(
                                if (index == focusedSectionIndex + 1) {
                                    Modifier.offset(
                                        y = sectionHeight -
                                            focusedSectionBottomPadding -
                                            DashboardSectionRowBottomPadding +
                                            DashboardSectionGap
                                    )
                                } else {
                                    Modifier.padding(bottom = focusedSectionBottomPadding)
                                }
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSection(
    title: String,
    items: List<MediaItem>,
    baseUrl: String,
    isLoading: Boolean,
    onItemFocus: (MediaItem) -> Unit,
    onItemClick: (MediaItem) -> Unit,
    onViewAll: (() -> Unit)?,
    itemAspectRatio: Float = 2f / 3f,
    itemWidth: androidx.compose.ui.unit.Dp = 110.dp,
    horizontalPadding: androidx.compose.ui.unit.Dp = 28.dp,
    sectionIndex: Int,
    focusedSectionIndex: Int,
    onFocusedSectionIndexChange: (Int) -> Unit = {},
    navigator: DpadSectionNavigator,
    onExitLeft: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var centerScrollJob by remember { mutableStateOf<Job?>(null) }
    fun alignFocusedItemToStart(index: Int) {
        centerScrollJob?.cancel()
        centerScrollJob = scope.launch {
            delay(35)
            listState.animateScrollToItem(index)
        }
    }
    val sectionAlpha by animateFloatAsState(
        targetValue = if (sectionIndex < focusedSectionIndex) 0f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "desktopSectionFocusAlpha",
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(sectionAlpha)
            .zIndex(if (sectionIndex == focusedSectionIndex) 2f else 1f),
    ) {
        SectionHeader(title = title, horizontalPadding = horizontalPadding)
        if (isLoading && items.isEmpty()) {
            org.jellyplus.client.ui.components.MediaRowPlaceholder(padding = PaddingValues(horizontal = horizontalPadding))
        } else {
            val visibleItems = items.take(DashboardMaxVisibleItems)
            val minItems = if (itemAspectRatio > 1f) DashboardMinWideItems else DashboardMinPosterItems
            val fillerCount = (minItems - visibleItems.size).coerceAtLeast(0)
            val viewMoreAction = onViewAll ?: {}
            val rowItemCount = visibleItems.size + fillerCount + 1
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(
                    start = 0.dp,
                    end = horizontalPadding,
                    top = DashboardSectionRowTopPadding,
                    bottom = DashboardSectionRowBottomPadding,
                ),
                horizontalArrangement = Arrangement.spacedBy(DashboardItemSpacing),
                modifier = Modifier.fillMaxWidth().padding(start = horizontalPadding),
            ) {
                lazyListItemsIndexed(visibleItems) { index, item ->
                    MediaPoster(
                        item = item,
                        baseUrl = baseUrl,
                        onClick = { onItemClick(item) },
                        onFocus = {
                            onItemFocus(item)
                            alignFocusedItemToStart(index)
                        },
                        aspectRatio = itemAspectRatio,
                        showLabel = itemAspectRatio > 1f,
                        modifier = Modifier
                            .width(itemWidth)
                            .then(
                                if (index == 0) Modifier.focusRequester(navigator.requesters[sectionIndex])
                                else Modifier
                            )
                            .sectionItemDpadHandler(
                                itemIndex = index,
                                itemCount = rowItemCount,
                                sectionIndex = sectionIndex,
                                navigator = navigator,
                                onExitLeft = onExitLeft,
                                onNavigateUp = {
                                    if (sectionIndex > 0) onFocusedSectionIndexChange(sectionIndex - 1)
                                },
                                onNavigateDown = {
                                    if (sectionIndex < navigator.count - 1) onFocusedSectionIndexChange(sectionIndex + 1)
                                },
                            ),
                    )
                }
                items(fillerCount) {
                    EmptyMediaSlot(
                        aspectRatio = itemAspectRatio,
                        modifier = Modifier.width(itemWidth),
                    )
                }
                item(key = "$title-view-more") {
                    ViewMoreCard(
                        aspectRatio = itemAspectRatio,
                        onClick = viewMoreAction,
                        onFocus = { alignFocusedItemToStart(visibleItems.size + fillerCount) },
                        modifier = Modifier
                            .width(itemWidth)
                            .sectionItemDpadHandler(
                                itemIndex = visibleItems.size + fillerCount,
                                itemCount = rowItemCount,
                                sectionIndex = sectionIndex,
                                navigator = navigator,
                                onExitLeft = onExitLeft,
                                onNavigateUp = {
                                    if (sectionIndex > 0) onFocusedSectionIndexChange(sectionIndex - 1)
                                },
                                onNavigateDown = {
                                    if (sectionIndex < navigator.count - 1) onFocusedSectionIndexChange(sectionIndex + 1)
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMediaSlot(
    aspectRatio: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .background(
                Color.White.copy(alpha = 0.045f),
                RoundedCornerShape(8.dp),
            ),
    )
}

private fun buildDesktopHeroMetadata(item: MediaItem): String = buildString {
    val genres = item.genres?.take(3)?.joinToString("   ")
    if (!genres.isNullOrBlank()) {
        append(genres)
    } else {
        append(
            when (item.type) {
                MediaType.MOVIE -> "Movie"
                MediaType.SERIES -> "TV Series"
                MediaType.EPISODE -> "Episode"
                else -> item.type.value
            }
        )
    }
    item.year?.let { append("   $it") }
}

@Composable
private fun DesktopHeroInfoRow(item: MediaItem) {
    Row(
        modifier = Modifier.padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item.rating?.let { rating ->
            Surface(color = Color(0xFFFFB300), shape = RoundedCornerShape(4.dp)) {
                Text(
                    "IMDb",
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                )
            }
            Text(
                rating.toString(),
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
            )
        }
        item.year?.let {
            Text(
                it.toString(),
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
            )
        }
        item.runTimeTicks?.let { ticks ->
            val minutes = ticks / 10_000_000 / 60
            if (minutes > 0) {
                Text(
                    "${minutes / 60}h ${minutes % 60}m",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                )
            }
        }
        if (item.type == MediaType.EPISODE) {
            val season = item.parentIndexNumber?.let { "S${it.toString().padStart(2, '0')}" }
            val episode = item.index?.let { "E${it.toString().padStart(2, '0')}" }
            val label = listOfNotNull(season, episode).joinToString(" ")
            if (label.isNotBlank()) {
                Text(
                    label,
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.SansSerif,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = 28.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(DashboardSectionHeaderHeight)
            .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
        )
    }
}

@Composable
private fun ViewMoreCard(
    aspectRatio: Float,
    onClick: () -> Unit,
    onFocus: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusedScale = if (aspectRatio > 1f) 7f / 6f else 1.2f
    val scale by animateFloatAsState(if (isFocused) focusedScale else 1f, label = "desktopViewMoreFocusScale")
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = if (isFocused) 0.18f else 0.08f),
        modifier = modifier
            .aspectRatio(aspectRatio)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) onFocus()
            }
            .focusable()
            .zIndex(if (isFocused) 10f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0.5f)
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.35f)),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Xem thêm",
                color = Color.White,
                fontSize = if (aspectRatio > 1f) 16.sp else 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
            )
        }
    }
}

@Composable
private fun MediaGrid(
    title: String,
    items: List<MediaItem>,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = DesktopContentLeftPadding, top = 48.dp, end = DesktopContentRightPadding, bottom = 0.dp)
    ) {
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No content found", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            val gridNavigator = rememberDpadGridNavigator(rowCount = (items.size + 7) / 8)

            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = DashboardFocusGutter, top = 6.dp, end = DashboardFocusGutter, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    val rowIndex = index / 8
                    val colIndex = index % 8

                    MediaPoster(
                        item = item,
                        baseUrl = baseUrl,
                        onClick = { onMediaClick(item) },
                        onFocus = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (colIndex == 0) Modifier.focusRequester(gridNavigator.rowRequesters[rowIndex])
                                else Modifier
                            )
                            .gridItemDpadHandler(
                                rowIndex = rowIndex,
                                colIndex = colIndex,
                                colCount = if (rowIndex == (items.size - 1) / 8) (items.size % 8).let { if (it == 0) 8 else it } else 8,
                                navigator = gridNavigator,
                            ),
                    )
                }
            }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(300)
                try { gridNavigator.rowRequesters[0].requestFocus() } catch (_: IllegalStateException) {}
            }
        }
    }
}

@Composable
private fun DesktopHistoryScreen(
    onContinueWatchingClick: (MediaItem) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
) {
    val viewModel: HistoryViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val watchedItems = remember(state.watchedItems, state.resumeItems) {
        state.watchedItems
            .filterNot { watched -> state.resumeItems.any { it.id == watched.id } }
            .distinctBy { it.id }
    }
    val hasContent = state.resumeItems.isNotEmpty() || watchedItems.isNotEmpty()
    val continueWatchingListState = rememberLazyListState()
    val continueWatchingScope = rememberCoroutineScope()
    var continueWatchingCenterJob by remember { mutableStateOf<Job?>(null) }
    fun alignContinueWatchingItemToStart(index: Int) {
        continueWatchingCenterJob?.cancel()
        continueWatchingCenterJob = continueWatchingScope.launch {
            delay(35)
            continueWatchingListState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = DesktopContentLeftPadding, top = 48.dp, end = DesktopContentRightPadding, bottom = 24.dp),
    ) {
        Text("History", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))

        when {
            state.isLoading && !hasContent -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            !state.isLoading && !hasContent && state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "Unable to load history", color = Color.White.copy(alpha = 0.55f))
                }
            }
            !state.isLoading && !hasContent -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No watch history yet", color = Color.White.copy(alpha = 0.55f), fontSize = 18.sp)
                }
            }
            else -> {
                if (state.resumeItems.isNotEmpty()) {
                    SectionHeader(title = "Continue Watching", horizontalPadding = 0.dp)
                    LazyRow(
                        state = continueWatchingListState,
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentPadding = PaddingValues(start = 0.dp, end = DashboardFocusGutter, top = 18.dp, bottom = 22.dp),
                        horizontalArrangement = Arrangement.spacedBy(DashboardItemSpacing),
                    ) {
                        lazyListItemsIndexed(state.resumeItems) { index, item ->
                            MediaPoster(
                                item = item,
                                baseUrl = state.baseUrl,
                                onClick = { onContinueWatchingClick(item) },
                                onFocus = { alignContinueWatchingItemToStart(index) },
                                aspectRatio = 16f / 9f,
                                showLabel = true,
                                modifier = Modifier.width(DashboardWideItemWidth),
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }

                if (watchedItems.isNotEmpty()) {
                    SectionHeader(title = "Watched", horizontalPadding = 0.dp)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 0.dp, top = 18.dp, end = DashboardFocusGutter, bottom = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        items(watchedItems.size) { index ->
                            val item = watchedItems[index]
                            MediaPoster(
                                item = item,
                                baseUrl = state.baseUrl,
                                onClick = { onMediaClick(item) },
                                aspectRatio = 16f / 9f,
                                showLabel = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(16.dp))
            Text("Search functionality coming soon", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp)
        }
    }
}

@Composable
private fun DesktopProfilePlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Person, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(16.dp))
            Text("Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NavIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    @Suppress("UNUSED_PARAMETER") sidebarFocusRequester: FocusRequester,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.1f else 1.0f)

    Box(
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .background(
                color = if (isFocused) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, null,
            tint = when {
                isSelected || isFocused -> MaterialTheme.colorScheme.primary
                else -> Color.White.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(24.dp),
        )
    }
}
