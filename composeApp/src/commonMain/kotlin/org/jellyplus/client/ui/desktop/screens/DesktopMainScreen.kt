package org.jellyplus.client.ui.desktop.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
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
import org.jellyplus.client.ui.navigation.DpadSectionNavigator
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import org.jellyplus.client.logDebug
import org.jellyplus.client.ui.navigation.RequestInitialFocus
import org.jellyplus.client.ui.navigation.gridItemDpadHandler
import org.jellyplus.client.ui.navigation.rememberDpadGridNavigator
import org.jellyplus.client.ui.navigation.rememberDpadSectionNavigator
import org.jellyplus.client.ui.navigation.sectionItemDpadHandler
import org.jellyplus.client.ui.components.MediaPoster
import org.jellyplus.client.ui.desktop.DesktopContentLeftPadding
import org.jellyplus.client.ui.desktop.DesktopContentRightPadding
import org.jellyplus.client.ui.desktop.DesktopContentHorizontalPadding
import org.jellyplus.client.ui.desktop.DesktopTopNavHeight
import org.jellyplus.client.ui.desktop.DesktopSidebarLogoSize
import org.jellyplus.client.ui.mobile.screens.MobileAuthLogo
import org.jellyplus.client.ui.viewmodels.HomeState
import org.jellyplus.client.ui.viewmodels.HomeViewModel
import org.jellyplus.client.ui.viewmodels.HistoryViewModel
import org.jellyplus.client.ui.viewmodels.MainViewModel
import org.jellyplus.client.ui.viewmodels.SearchViewModel
import org.jellyplus.client.ui.viewmodels.SessionViewModel
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.foundation.lazy.itemsIndexed as lazyListItemsIndexed

private enum class NavDestination { ForYou, Library, Search, Settings }

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
    val selectedNav = NavDestination.entries.getOrElse(selectedNavIndex) { NavDestination.ForYou }
    val navFocusRequester = remember { FocusRequester() }

    LaunchedEffect(homeState.featuredItems, homeState.resumeItems, homeState.recentlyAddedItems) {
        viewModel.registerItems(
            homeState.featuredItems + homeState.resumeItems + homeState.recentlyAddedItems
        )
    }
    val favoriteItems = state.favoriteItems

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF181818))) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedNav) {
                NavDestination.ForYou -> MainDashboard(
                    viewModel = viewModel,
                    state = state,
                    homeState = homeState,
                    onMediaClick = onMediaClick,
                    onContinueWatchingClick = onContinueWatchingClick,
                    onReloadHome = { homeViewModel.loadHomeContent() },
                    onFocusExit = {
                        try { navFocusRequester.requestFocus() } catch (_: IllegalStateException) {}
                    },
                )
                NavDestination.Library -> Box(modifier = Modifier.fillMaxSize().padding(top = DesktopTopNavHeight)) {
                    DesktopLibraryScreen(
                        favorites = favoriteItems,
                        watchLater = state.watchLaterItems,
                        baseUrl = state.baseUrl,
                        onMediaClick = onMediaClick,
                        onContinueWatchingClick = onContinueWatchingClick,
                    )
                }
                NavDestination.Search -> Box(modifier = Modifier.fillMaxSize().padding(top = DesktopTopNavHeight)) {
                    DesktopSearchContent(onMediaClick = onMediaClick)
                }
                NavDestination.Settings -> {
                    val sv = sessionViewModel ?: koinViewModel()
                    Box(modifier = Modifier.fillMaxSize().padding(top = DesktopTopNavHeight)) {
                        org.jellyplus.client.ui.screens.SettingsScreen(
                            sessionViewModel = sv,
                            onBack = {},
                        )
                    }
                }
            }
        }

        DesktopTopNav(
            selected = selectedNav,
            navFocusRequester = navFocusRequester,
            onSelect = { onSelectedNavIndexChange(it.ordinal) },
            modifier = Modifier.align(Alignment.TopStart),
        )
    }
}

@Composable
private fun DesktopTopNav(
    selected: NavDestination,
    navFocusRequester: FocusRequester,
    onSelect: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(DesktopTopNavHeight)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Black.copy(alpha = 0.55f),
                        1.0f to Color.Transparent,
                    )
                )
            )
            .padding(horizontal = DesktopContentHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MobileAuthLogo(modifier = Modifier.size(DesktopSidebarLogoSize))
        Spacer(Modifier.width(16.dp))
        NavTab(
            label = "For you",
            selected = selected == NavDestination.ForYou,
            focusRequester = navFocusRequester,
            onClick = { onSelect(NavDestination.ForYou) },
        )
        NavTab(
            label = "Library",
            selected = selected == NavDestination.Library,
            onClick = { onSelect(NavDestination.Library) },
        )
        Spacer(Modifier.weight(1f))
        NavTab(
            label = "Search",
            icon = Icons.Default.Search,
            selected = selected == NavDestination.Search,
            onClick = { onSelect(NavDestination.Search) },
        )
        NavTab(
            label = "Settings",
            selected = selected == NavDestination.Settings,
            onClick = { onSelect(NavDestination.Settings) },
        )
    }
}

@Composable
private fun NavTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    focusRequester: FocusRequester? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val bg = when {
        isFocused -> Color.White
        selected -> Color.White.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    val fg = if (isFocused) Color.Black else Color.White.copy(alpha = if (selected) 1f else 0.7f)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = bg,
        modifier = Modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChangedCompat { isFocused = it },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
            Text(label, color = fg, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun Modifier.onFocusChangedCompat(onChanged: (Boolean) -> Unit): Modifier =
    this.onFocusChanged { onChanged(it.isFocused) }

@Composable
private fun MainDashboard(
    viewModel: MainViewModel,
    state: org.jellyplus.client.ui.viewmodels.MainState,
    homeState: HomeState,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
    onReloadHome: () -> Unit,
    onFocusExit: () -> Unit,
) {
    val hPad = DesktopContentHorizontalPadding
    val hasHomeContent = homeState.featuredItems.isNotEmpty() ||
        homeState.resumeItems.isNotEmpty() ||
        homeState.recentlyAddedItems.isNotEmpty()
    val hasMainContent = state.movies.isNotEmpty() || state.tvShows.isNotEmpty()
    val isHomeLoading = !hasHomeContent && !hasMainContent && (homeState.isLoading || state.isLoading)

    val featured = homeState.featuredItems
    val suggested = remember(featured) { featured.take(3) }
    val hotTopics = remember(featured) { featured }

    // Highlighted genres derived from the genres we actually have on loaded
    // movies/series — top genres by item count.
    val genreRows = remember(state.movies, state.tvShows) {
        val pool = state.movies + state.tvShows
        pool.asSequence()
            .flatMap { item -> (item.genres ?: emptyList()).map { it to item } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, items) -> items.distinctBy { it.id } }
            .filterValues { it.size >= 3 }
            .entries
            .sortedByDescending { it.value.size }
            .take(4)
            .map { it.key to it.value }
    }

    var heroItem by remember { mutableStateOf<MediaItem?>(null) }
    val scrollState = rememberScrollState()
    val firstRowFocus = remember { FocusRequester() }

    if (!isHomeLoading) RequestInitialFocus(firstRowFocus, suggested.size to hotTopics.size)

    Box(modifier = Modifier.fillMaxSize()) {
        // Hero backdrop — only while focus is inside the Hot topics row.
        Crossfade(
            targetState = heroItem,
            animationSpec = tween(durationMillis = 300),
            label = "desktopHero",
        ) { item ->
            if (item != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = item.getBackdropUrl(state.baseUrl) ?: item.getImageUrl(state.baseUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.30f),
                                    0.45f to Color.Black.copy(alpha = 0.45f),
                                    1.0f to Color(0xFF181818),
                                )
                            )
                        )
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.60f),
                                    0.45f to Color.Black.copy(alpha = 0.25f),
                                    1.0f to Color.Transparent,
                                )
                            )
                        )
                    )
                }
            }
        }

        when {
            isHomeLoading -> CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center),
            )

            !hasHomeContent && !hasMainContent -> Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Default.Inbox, null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(96.dp))
                Spacer(Modifier.height(20.dp))
                Text("Library is empty", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                val reloadFocus = remember { FocusRequester() }
                RequestInitialFocus(reloadFocus)
                org.jellyplus.client.ui.components.FocusableButton(
                    onClick = { onReloadHome(); viewModel.loadData() },
                    modifier = Modifier.width(160.dp).height(48.dp).focusRequester(reloadFocus),
                ) { Text("Reload", color = Color.Black, fontWeight = FontWeight.Bold) }
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = DesktopTopNavHeight + 12.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                // Hero info text — shown only when a Hot topics item is focused.
                Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    heroItem?.let { item ->
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = hPad)
                                .fillMaxWidth(0.6f),
                        ) {
                            val metadata = buildDesktopHeroMetadata(item)
                            if (metadata.isNotBlank()) {
                                Text(
                                    metadata,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            Text(
                                item.title,
                                color = Color.White,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            DesktopHeroInfoRow(item)
                        }
                    }
                }

                var rowIndex = 0
                if (suggested.isNotEmpty()) {
                    DesktopMediaRow(
                        title = "Suggested",
                        items = suggested,
                        baseUrl = state.baseUrl,
                        horizontalPadding = hPad,
                        cardWidth = 360.dp,
                        captionMode = CaptionMode.Always,
                        rowFocusRequester = firstRowFocus.takeIf { rowIndex == 0 },
                        isFirstRow = rowIndex == 0,
                        onExitUp = onFocusExit,
                        onItemFocus = { heroItem = null },
                        onItemClick = onMediaClick,
                    ).also { rowIndex++ }
                }
                if (hotTopics.isNotEmpty()) {
                    val isFirst = rowIndex == 0
                    DesktopMediaRow(
                        title = "Hot topics",
                        items = hotTopics,
                        baseUrl = state.baseUrl,
                        horizontalPadding = hPad,
                        cardWidth = 280.dp,
                        captionMode = CaptionMode.None,
                        rowFocusRequester = firstRowFocus.takeIf { isFirst },
                        isFirstRow = isFirst,
                        onExitUp = onFocusExit,
                        onItemFocus = { heroItem = it },
                        onItemClick = onMediaClick,
                    ).also { rowIndex++ }
                }
                if (homeState.resumeItems.isNotEmpty()) {
                    val isFirst = rowIndex == 0
                    DesktopMediaRow(
                        title = "Continue watching",
                        items = homeState.resumeItems,
                        baseUrl = state.baseUrl,
                        horizontalPadding = hPad,
                        cardWidth = 248.dp,
                        captionMode = CaptionMode.OnFocus,
                        rowFocusRequester = firstRowFocus.takeIf { isFirst },
                        isFirstRow = isFirst,
                        onExitUp = onFocusExit,
                        onItemFocus = { heroItem = null },
                        onItemClick = onContinueWatchingClick,
                    ).also { rowIndex++ }
                }
                if (homeState.recentlyAddedItems.isNotEmpty()) {
                    val isFirst = rowIndex == 0
                    DesktopMediaRow(
                        title = "Recently added",
                        items = homeState.recentlyAddedItems,
                        baseUrl = state.baseUrl,
                        horizontalPadding = hPad,
                        cardWidth = 248.dp,
                        captionMode = CaptionMode.OnFocus,
                        rowFocusRequester = firstRowFocus.takeIf { isFirst },
                        isFirstRow = isFirst,
                        onExitUp = onFocusExit,
                        onItemFocus = { heroItem = null },
                        onItemClick = onMediaClick,
                    ).also { rowIndex++ }
                }
                genreRows.forEach { (genre, items) ->
                    val isFirst = rowIndex == 0
                    DesktopMediaRow(
                        title = genre,
                        items = items,
                        baseUrl = state.baseUrl,
                        horizontalPadding = hPad,
                        cardWidth = 248.dp,
                        captionMode = CaptionMode.OnFocus,
                        rowFocusRequester = firstRowFocus.takeIf { isFirst },
                        isFirstRow = isFirst,
                        onExitUp = onFocusExit,
                        onItemFocus = { heroItem = null },
                        onItemClick = onMediaClick,
                    )
                    rowIndex++
                }
            }
        }
    }
}

private enum class CaptionMode { None, OnFocus, Always }

@Composable
private fun DesktopMediaRow(
    title: String,
    items: List<MediaItem>,
    baseUrl: String,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    cardWidth: androidx.compose.ui.unit.Dp,
    captionMode: CaptionMode,
    rowFocusRequester: FocusRequester?,
    isFirstRow: Boolean,
    onExitUp: () -> Unit,
    onItemFocus: (MediaItem) -> Unit,
    onItemClick: (MediaItem) -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var scrollJob by remember { mutableStateOf<Job?>(null) }
    fun alignToStart(index: Int) {
        scrollJob?.cancel()
        scrollJob = scope.launch {
            delay(35)
            listState.animateScrollToItem(index)
        }
    }
    val visible = items.take(DashboardMaxVisibleItems)
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = title, horizontalPadding = horizontalPadding)
        Spacer(Modifier.height(12.dp))
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(DashboardItemSpacing),
            modifier = Modifier
                .fillMaxWidth()
                .then(if (rowFocusRequester != null) Modifier.focusRequester(rowFocusRequester) else Modifier)
                .focusGroup(),
        ) {
            lazyListItemsIndexed(visible) { index, item ->
                DesktopLandscapeCard(
                    item = item,
                    baseUrl = baseUrl,
                    captionMode = captionMode,
                    onClick = { onItemClick(item) },
                    onFocus = {
                        onItemFocus(item)
                        alignToStart(index)
                    },
                    modifier = Modifier
                        .width(cardWidth)
                        .onKeyEvent { e ->
                            if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                            when (e.key) {
                                Key.DirectionUp -> if (isFirstRow) { onExitUp(); true } else false
                                Key.DirectionRight -> index == visible.lastIndex
                                else -> false
                            }
                        },
                )
            }
        }
    }
}

@Composable
private fun DesktopLandscapeCard(
    item: MediaItem,
    baseUrl: String,
    captionMode: CaptionMode,
    onClick: () -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.08f else 1f, label = "cardScale")
    val shape = RoundedCornerShape(10.dp)
    val showCaption = captionMode == CaptionMode.Always ||
        (captionMode == CaptionMode.OnFocus && isFocused)
    Column(modifier = modifier) {
        Surface(
            onClick = onClick,
            shape = shape,
            color = Color.White.copy(alpha = 0.05f),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .onFocusChanged {
                    isFocused = it.isFocused
                    if (it.isFocused) onFocus()
                }
                .zIndex(if (isFocused) 10f else 0f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = if (isFocused) Color.White else Color.Transparent,
                    shape = shape,
                ),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = item.getBackdropUrl(baseUrl) ?: item.getImageUrl(baseUrl),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                if (captionMode == CaptionMode.OnFocus && isFocused) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Transparent,
                                    0.55f to Color.Transparent,
                                    1.0f to Color.Black.copy(alpha = 0.82f),
                                )
                            )
                        )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(
                            item.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        desktopCardSubtitle(item)?.let {
                            Text(
                                it,
                                color = Color.White.copy(alpha = 0.72f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
        if (captionMode == CaptionMode.Always) {
            Spacer(Modifier.height(8.dp))
            Text(
                item.title,
                color = Color.White.copy(alpha = if (isFocused) 1f else 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun desktopCardSubtitle(item: MediaItem): String? {
    val s = when (item.type) {
        MediaType.EPISODE -> buildString {
            item.seriesName?.let { append(it) }
            val season = item.parentIndexNumber?.let { "S${it.toString().padStart(2, '0')}" }
            val episode = item.index?.let { "E${it.toString().padStart(2, '0')}" }
            val label = listOfNotNull(season, episode).joinToString(" ")
            if (label.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(label)
            }
        }
        else -> buildString {
            item.year?.let { append(it) }
            item.genres?.firstOrNull()?.let {
                if (isNotEmpty()) append(" · ")
                append(it)
            }
        }
    }
    return s.takeIf { it.isNotBlank() }
}

@Composable
private fun DesktopLibraryScreen(
    favorites: List<MediaItem>,
    watchLater: List<MediaItem>,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
) {
    val scrollState = rememberScrollState()
    val firstRowFocus = remember { FocusRequester() }
    val rows = remember(favorites, watchLater) {
        buildList {
            if (favorites.isNotEmpty()) add("Favorites" to favorites)
            if (watchLater.isNotEmpty()) add("Watch later" to watchLater)
        }
    }
    if (rows.isNotEmpty()) RequestInitialFocus(firstRowFocus, rows.size)
    if (rows.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Inbox, null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(96.dp))
                Spacer(Modifier.height(20.dp))
                Text("Your library is empty", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 12.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        rows.forEachIndexed { index, (title, items) ->
            DesktopMediaRow(
                title = title,
                items = items,
                baseUrl = baseUrl,
                horizontalPadding = DesktopContentHorizontalPadding,
                cardWidth = 248.dp,
                captionMode = CaptionMode.OnFocus,
                rowFocusRequester = firstRowFocus.takeIf { index == 0 },
                isFirstRow = index == 0,
                onExitUp = {},
                onItemFocus = {},
                onItemClick = if (title == "Watch later") onContinueWatchingClick else onMediaClick,
            )
        }
    }
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
    val historyFirstFocus = remember { FocusRequester() }
    if (hasContent) RequestInitialFocus(historyFirstFocus, hasContent)
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
                        modifier = Modifier.fillMaxWidth().height(164.dp),
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
                                modifier = Modifier
                                    .width(DashboardWideItemWidth)
                                    .then(if (index == 0) Modifier.focusRequester(historyFirstFocus) else Modifier)
                                    .onKeyEvent { e ->
                                        if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                                        when (e.key) {
                                            // Top row: nothing above → consume Up so focus stays.
                                            Key.DirectionUp -> true
                                            Key.DirectionRight -> index == state.resumeItems.lastIndex
                                            else -> false
                                        }
                                    },
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
                            val lastRow = (watchedItems.size - 1) / 4
                            val rowIndex = index / 4
                            val noResume = state.resumeItems.isEmpty()
                            MediaPoster(
                                item = item,
                                baseUrl = state.baseUrl,
                                onClick = { onMediaClick(item) },
                                aspectRatio = 16f / 9f,
                                showLabel = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (index == 0 && noResume) Modifier.focusRequester(historyFirstFocus)
                                        else Modifier
                                    )
                                    .onKeyEvent { e ->
                                        if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                                        when (e.key) {
                                            // Last grid row: nothing below → consume Down.
                                            Key.DirectionDown -> rowIndex == lastRow
                                            // Very last item: consume Right.
                                            Key.DirectionRight -> index == watchedItems.size - 1
                                            // Top of screen (no Continue Watching above) → consume Up.
                                            Key.DirectionUp -> rowIndex == 0 && noResume
                                            else -> false
                                        }
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopSearchContent(onMediaClick: (MediaItem) -> Unit) {
    val searchViewModel: SearchViewModel = koinViewModel()
    val state by searchViewModel.state.collectAsState()

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = DesktopContentLeftPadding, top = 36.dp, end = DesktopContentRightPadding, bottom = 36.dp),
    ) {
        // ── Search bar ──────────────────────────────────────────────────
        TextField(
            value = state.query,
            onValueChange = { searchViewModel.onQueryChange(it) },
            placeholder = {
                Text("Search movies, shows, people...", color = Color.White.copy(alpha = 0.35f), fontSize = 20.sp)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .focusRequester(focusRequester),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(14.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, color = Color.White),
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.45f), modifier = Modifier.size(22.dp))
            },
            trailingIcon = {
                if (state.query.isNotBlank()) {
                    IconButton(onClick = { searchViewModel.clearQuery() }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            },
        )

        Spacer(Modifier.height(20.dp))

        // ── Filter chips ────────────────────────────────────────────────
        if (state.query.isNotBlank()) {
            val filters = listOf(
                null to "All",
                MediaType.MOVIE to "Movies",
                MediaType.SERIES to "TV Shows",
                MediaType.EPISODE to "Episodes",
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                filters.forEach { (type, label) ->
                    val selected = state.selectedFilter == type
                    FilterChip(
                        selected = selected,
                        onClick = { searchViewModel.onFilterChange(type) },
                        label = { Text(label, fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black,
                            containerColor = Color.White.copy(alpha = 0.07f),
                            labelColor = Color.White.copy(alpha = 0.75f),
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            selectedBorderColor = Color.Transparent,
                            borderColor = Color.White.copy(alpha = 0.15f),
                        ),
                    )
                }
            }
        }

        // ── Content area ────────────────────────────────────────────────
        when {
            state.query.isBlank() -> {
                if (state.searchHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "Recent searches",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        TextButton(onClick = { searchViewModel.clearHistory() }) {
                            Text("Clear all", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                        }
                    }
                    // History as chips / pill row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        state.searchHistory.forEach { query ->
                            Surface(
                                onClick = { searchViewModel.onQueryChange(query) },
                                color = Color.White.copy(alpha = 0.07f),
                                shape = RoundedCornerShape(20.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.45f),
                                        modifier = Modifier.size(15.dp),
                                    )
                                    Text(query, color = Color.White, fontSize = 14.sp)
                                    IconButton(
                                        onClick = { searchViewModel.removeHistoryItem(query) },
                                        modifier = Modifier.size(20.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White.copy(alpha = 0.3f),
                                            modifier = Modifier.size(13.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.size(96.dp),
                            )
                            Text(
                                "Search movies, shows & more",
                                color = Color.White.copy(alpha = 0.28f),
                                fontSize = 18.sp,
                            )
                        }
                    }
                }
            }

            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
                }
            }

            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "Search failed", color = Color.White.copy(alpha = 0.45f), fontSize = 16.sp)
                }
            }

            state.displayResults.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("No results for", color = Color.White.copy(alpha = 0.38f), fontSize = 16.sp)
                        Text(
                            "\"${state.query}\"",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            else -> {
                Text(
                    "${state.displayResults.size} result${if (state.displayResults.size != 1) "s" else ""}",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
                androidx.compose.foundation.lazy.LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    lazyListItemsIndexed(state.displayResults) { index, item ->
                        DesktopSearchResultRow(
                            item = item,
                            baseUrl = state.baseUrl,
                            onClick = { onMediaClick(item) },
                            isLast = index == state.displayResults.lastIndex,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopSearchResultRow(
    item: MediaItem,
    baseUrl: String,
    onClick: () -> Unit,
    isLast: Boolean = false,
) {
    Surface(
        onClick = onClick,
        color = Color.White.copy(alpha = 0.0f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .onKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (e.key) {
                    // Single-column list: Right has no target → no-op.
                    Key.DirectionRight -> true
                    // Last result: nothing below → consume Down.
                    Key.DirectionDown -> isLast
                    else -> false
                }
            },
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Poster
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(96.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.06f)),
            ) {
                AsyncImage(
                    model = item.getImageUrl(baseUrl),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                if (item.isPlayed) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(5.dp)
                            .size(9.dp)
                            .background(Color(0xFF4CAF50), CircleShape),
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    item.title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = buildString {
                    append(when (item.type) {
                        MediaType.MOVIE -> "Movie"
                        MediaType.SERIES -> "TV Series"
                        MediaType.EPISODE -> "Episode"
                        else -> item.type.value
                    })
                    item.year?.let { append(" · $it") }
                    if (item.type == MediaType.EPISODE) {
                        item.seriesName?.let { name ->
                            val s = item.parentIndexNumber?.let { "S${it.toString().padStart(2,'0')}" } ?: ""
                            val e = item.index?.let { "E${it.toString().padStart(2,'0')}" } ?: ""
                            val ep = listOf(s, e).filter { it.isNotBlank() }.joinToString("")
                            if (ep.isNotBlank()) append(" · $name $ep") else append(" · $name")
                        }
                    }
                }
                Text(meta, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, maxLines = 1)

                item.runTimeTicks?.let { ticks ->
                    val totalMin = (ticks / 10_000_000 / 60).toInt()
                    if (totalMin > 0) {
                        val h = totalMin / 60; val m = totalMin % 60
                        Text(
                            if (h > 0) "${h}h ${m}m" else "${m}m",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 12.sp,
                        )
                    }
                }

                item.overview?.let { overview ->
                    Text(
                        overview,
                        color = Color.White.copy(alpha = 0.38f),
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Rating
            item.rating?.let { r ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "%.1f".format(r),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopProfilePlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Person, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
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
            .clickable { onClick() }
            .onFocusChanged { isFocused = it.isFocused }
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
