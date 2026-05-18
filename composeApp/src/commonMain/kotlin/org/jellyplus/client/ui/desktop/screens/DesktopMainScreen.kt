package org.jellyplus.client.ui.desktop.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.jellyplus.client.isDebug
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.ui.navigation.DpadSectionNavigator
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import org.jellyplus.client.logDebug
import org.jellyplus.client.utils.format
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

private const val ShowDesktopLayoutBounds = false

@Composable
private fun DebugBoundsFrame(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        content()
        if (ShowDesktopLayoutBounds && isDebug()) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(999f)
                    .border(1.dp, color),
            )
            Surface(
                color = color.copy(alpha = 0.88f),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .zIndex(1000f),
            ) {
                Text(
                    text = label,
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                    maxLines = 1,
                )
            }
        }
    }
}

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
private val DashboardSectionHeaderHeight = 22.dp
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
    val homeScrollState = rememberScrollState()
    var homeHeroFocusRequest by remember { mutableStateOf(0) }
    var homeRowsFocused by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    RequestInitialFocus(navFocusRequester, selectedNav)

    LaunchedEffect(homeState.featuredItems, homeState.topPickItems, homeState.resumeItems, homeState.recentlyAddedItems) {
        viewModel.registerItems(
            homeState.featuredItems + homeState.topPickItems + homeState.resumeItems + homeState.recentlyAddedItems
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
                    scrollState = homeScrollState,
                    heroFocusRequest = homeHeroFocusRequest,
                    onRowsFocusChanged = { homeRowsFocused = it },
                    onFocusExit = {
                        homeRowsFocused = false
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

        if (!(selectedNav == NavDestination.ForYou && homeRowsFocused)) {
            DesktopTopNav(
                selected = selectedNav,
                navFocusRequester = navFocusRequester,
                onSelect = { onSelectedNavIndexChange(it.ordinal) },
                onNavFocused = {
                    homeRowsFocused = false
                    if (selectedNav == NavDestination.ForYou && homeScrollState.value != 0) {
                        scope.launch {
                            homeScrollState.animateScrollTo(
                                value = 0,
                                animationSpec = DesktopVerticalScrollAnimation,
                            )
                        }
                    }
                },
                onNavigateDown = {
                    if (selectedNav == NavDestination.ForYou) {
                        homeHeroFocusRequest++
                        true
                    } else {
                        false
                    }
                },
                modifier = Modifier.align(Alignment.TopStart),
            )
        }
    }
}

@Composable
private fun DesktopTopNav(
    selected: NavDestination,
    navFocusRequester: FocusRequester,
    onSelect: (NavDestination) -> Unit,
    onNavFocused: () -> Unit,
    onNavigateDown: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    var clockText by remember { mutableStateOf(currentTopNavClockText()) }
    LaunchedEffect(Unit) {
        while (true) {
            clockText = currentTopNavClockText()
            delay(30_000)
        }
    }

    DebugBoundsFrame(
        label = "top-nav",
        color = Color(0xFF64B5F6),
        modifier = modifier
            .fillMaxWidth()
            .height(DesktopTopNavHeight)
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Black.copy(alpha = 0.54f),
                        1.0f to Color.Transparent,
                    )
                )
            )
            .padding(horizontal = DesktopContentHorizontalPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF7B1FC8),
                modifier = Modifier
                    .size(DesktopSidebarLogoSize)
                    .onFocusChanged { if (it.isFocused) onNavFocused() },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("J", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }

            Surface(
                color = Color(0xFF2A2F34).copy(alpha = 0.88f),
                shape = RoundedCornerShape(999.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    NavIconTab(
                        icon = Icons.Default.Search,
                        selected = selected == NavDestination.Search,
                        onClick = { onSelect(NavDestination.Search) },
                        onFocus = onNavFocused,
                        onNavigateDown = onNavigateDown,
                    )
                    NavTab(
                        label = "Home",
                        selected = selected == NavDestination.ForYou,
                        focusRequester = navFocusRequester,
                        onClick = { onSelect(NavDestination.ForYou) },
                        onFocus = onNavFocused,
                        onNavigateDown = onNavigateDown,
                    )
                    NavTab(
                        label = "Library",
                        selected = selected == NavDestination.Library,
                        onClick = { onSelect(NavDestination.Library) },
                        onFocus = onNavFocused,
                        onNavigateDown = onNavigateDown,
                    )
                }
            }

            NavIconTab(
                icon = Icons.Default.Settings,
                selected = selected == NavDestination.Settings,
                onClick = { onSelect(NavDestination.Settings) },
                onFocus = onNavFocused,
                onNavigateDown = onNavigateDown,
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = buildAnnotatedString {
                    append("$clockText | ")
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.82f))) {
                        append("Jelly")
                    }
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("Plus")
                    }
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

private fun currentTopNavClockText(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
}

@Composable
private fun NavTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onFocus: () -> Unit = {},
    onNavigateDown: () -> Boolean = { false },
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    focusRequester: FocusRequester? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val bg = if (isFocused || selected) Color(0xFF3B4246) else Color.Transparent
    val fg = Color.White.copy(alpha = if (isFocused || selected) 0.95f else 0.72f)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = bg,
        modifier = Modifier
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .onFocusChangedCompat {
                isFocused = it
                if (it) onFocus()
            }
            .onKeyEvent { e ->
                e.type == KeyEventType.KeyDown && e.key == Key.DirectionDown && onNavigateDown()
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
            Text(label, color = fg, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun NavIconTab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    onFocus: () -> Unit = {},
    onNavigateDown: () -> Boolean = { false },
) {
    var isFocused by remember { mutableStateOf(false) }
    val bg = if (isFocused || selected) Color(0xFF3B4246) else Color.Transparent
    val fg = Color.White.copy(alpha = if (isFocused || selected) 0.95f else 0.78f)
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bg,
        modifier = Modifier
            .size(43.dp)
            .onFocusChangedCompat {
                isFocused = it
                if (it) onFocus()
            }
            .onKeyEvent { e ->
                e.type == KeyEventType.KeyDown && e.key == Key.DirectionDown && onNavigateDown()
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
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
    scrollState: androidx.compose.foundation.ScrollState,
    heroFocusRequest: Int,
    onRowsFocusChanged: (Boolean) -> Unit,
    onFocusExit: () -> Unit,
) {
    val hPad = DesktopContentHorizontalPadding
    val hasHomeContent = homeState.featuredItems.isNotEmpty() ||
        homeState.topPickItems.isNotEmpty() ||
        homeState.resumeItems.isNotEmpty() ||
        homeState.recentlyAddedItems.isNotEmpty()
    val hasMainContent = state.movies.isNotEmpty() || state.tvShows.isNotEmpty()
    val isHomeLoading = !hasHomeContent && !hasMainContent && (homeState.isLoading || state.isLoading)

    val featured = homeState.featuredItems
    val topPicks = homeState.topPickItems
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
    val dashboardRowCount =
        (if (topPicks.isNotEmpty()) 1 else 0) +
            (if (homeState.resumeItems.isNotEmpty()) 1 else 0) +
            (if (homeState.recentlyAddedItems.isNotEmpty()) 1 else 0) +
            genreRows.size
    val rowFocusRequesters = remember(dashboardRowCount) {
        List(dashboardRowCount) { FocusRequester() }
    }

    var heroIndex by remember(featured) { mutableStateOf(0) }
    // Section 0 default = auto-sliding featured hero. When a Top picks card is
    // focused, [topPickHeroItem] holds it and the hero area previews that item
    // with its own banner/background — a separate state from the featured carousel.
    var topPickHeroItem by remember(featured) { mutableStateOf<MediaItem?>(null) }
    var topPickHeroInfoExpanded by remember { mutableStateOf(false) }
    val heroCurrent = topPickHeroItem ?: featured.getOrNull(heroIndex)
    val heroDotIndex = heroIndex
    var heroPaused by remember { mutableStateOf(false) }
    var section0Focused by remember { mutableStateOf(false) }
    var hideSection0HeroBackground by remember { mutableStateOf(false) }
    var verticalScrollReason by remember { mutableStateOf("init") }
    val scope = rememberCoroutineScope()
    val heroButtonFocus = remember { FocusRequester() }
    val markVerticalScrollReason: (String) -> Unit = { reason ->
        verticalScrollReason = reason
        logDebug("JellyScroll", "MARK reason=$reason offset=${scrollState.value}")
    }
    val focusRow: (Int) -> Unit = { index ->
        val requester = rowFocusRequesters.getOrNull(index)
        if (requester != null) {
            scope.launch {
                delay(16)
                try { requester.requestFocus() } catch (_: IllegalStateException) {}
            }
        }
    }
    val focusPreviousRowOrNav: (Int) -> Unit = { index ->
        if (index == 0 && featured.isEmpty()) {
            onRowsFocusChanged(false)
            onFocusExit()
        } else {
            focusRow(index - 1)
        }
    }

    LaunchedEffect(Unit) {
        markVerticalScrollReason("initial-dashboard-scroll-top")
        scrollState.scrollTo(0)
    }

    LaunchedEffect(heroPaused) {
        if (heroPaused && scrollState.value != 0) {
            markVerticalScrollReason("heroPaused-effect-scroll-top")
            scrollState.animateScrollTo(
                value = 0,
                animationSpec = DesktopVerticalScrollAnimation,
            )
        }
    }

    LaunchedEffect(heroFocusRequest, featured.size, rowFocusRequesters.size) {
        if (heroFocusRequest <= 0) return@LaunchedEffect
        if (featured.isEmpty()) {
            focusRow(0)
            return@LaunchedEffect
        }
        topPickHeroItem = null
        topPickHeroInfoExpanded = false
        hideSection0HeroBackground = false
        heroPaused = true
        markVerticalScrollReason("top-nav-down-to-section0")
        scrollState.animateScrollTo(
            value = 0,
            animationSpec = DesktopVerticalScrollAnimation,
        )
        repeat(10) {
            try {
                heroButtonFocus.requestFocus()
                delay(24)
                markVerticalScrollReason("top-nav-down-to-section0-after-focus")
                scrollState.animateScrollTo(
                    value = 0,
                    animationSpec = DesktopVerticalScrollAnimation,
                )
                return@LaunchedEffect
            } catch (_: IllegalStateException) {
                delay(24)
            }
        }
    }

    // Rotate section 0 only while section 0 is focused.
    LaunchedEffect(heroIndex, section0Focused, featured.size, topPickHeroItem) {
        if (section0Focused && topPickHeroItem == null && featured.size > 1) {
            delay(5000)
            heroIndex = (heroIndex + 1) % featured.size
        }
    }

    LaunchedEffect(topPickHeroItem?.id) {
        if (topPickHeroItem == null) {
            topPickHeroInfoExpanded = false
        } else {
            topPickHeroInfoExpanded = false
            delay(90)
            topPickHeroInfoExpanded = true
        }
    }

    val focusHero: () -> Unit = {
        topPickHeroItem = null
        topPickHeroInfoExpanded = false
        hideSection0HeroBackground = false
        heroPaused = true
        section0Focused = true
        onRowsFocusChanged(false)
        scope.launch {
            if (scrollState.value != 0) {
                markVerticalScrollReason("top-picks-up-to-section0")
                scrollState.animateScrollTo(
                    value = 0,
                    animationSpec = DesktopVerticalScrollAnimation,
                )
            }
            delay(16)
            try { heroButtonFocus.requestFocus() } catch (_: IllegalStateException) {}
            delay(16)
            if (scrollState.value != 0) {
                markVerticalScrollReason("section0-watch-now-focus-after-request")
                scrollState.animateScrollTo(
                    value = 0,
                    animationSpec = DesktopVerticalScrollAnimation,
                )
            }
        }
    }

    val keepTopPickHeroInfoVisible: () -> Unit = {
        scope.launch {
            listOf(0L, 16L, 48L).forEach { delayMs ->
                if (delayMs > 0) delay(delayMs)
                if (scrollState.value != 0) {
                    markVerticalScrollReason("top-picks-keep-hero-info-visible")
                    scrollState.scrollTo(0)
                }
            }
        }
    }

    LaunchedEffect(scrollState) {
        var previous = scrollState.value
        snapshotFlow { scrollState.value }.collect { current ->
            if (current != previous) {
                logDebug(
                    "JellyScroll",
                    "VERTICAL offset $previous->$current delta=${current - previous} " +
                        "reason=$verticalScrollReason section0Focused=$section0Focused " +
                        "heroPaused=$heroPaused topPick=${topPickHeroItem?.id ?: "-"}",
                )
                previous = current
                verticalScrollReason = "external/unknown"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Crossfade(
            targetState = heroCurrent,
            animationSpec = tween(durationMillis = 420),
            label = "desktopHero",
        ) { item ->
            if (item != null && !hideSection0HeroBackground) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = item.getBackdropUrl(state.baseUrl) ?: item.getImageUrl(state.baseUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                    // Flat base dim — Google TV keeps the whole backdrop quite
                    // dark, even the visible part on the right.
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.38f)))
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.45f),
                                    0.45f to Color.Black.copy(alpha = 0.30f),
                                    0.80f to Color(0xFF0E1013).copy(alpha = 0.94f),
                                    1.0f to Color(0xFF0E1013),
                                )
                            )
                        )
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color(0xFF0E1013).copy(alpha = 0.96f),
                                    0.40f to Color.Black.copy(alpha = 0.82f),
                                    0.68f to Color.Black.copy(alpha = 0.42f),
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

            else -> BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val viewportHeight = maxHeight
                // Hero wraps its content + a FIXED ~40dp gap to "Top picks" in
                // every state (Google-TV). Content is top-anchored (122dp) so it
                // never slides under the nav. No viewport-fraction min height —
                // that forced the box taller than the content and left a big
                // empty band below it.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 44.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    DebugBoundsFrame(
                        label = "section-0 hero",
                        color = Color(0xFFFFD54F),
                        // In Top-picks PREVIEW the hero is a FIXED height so the
                        // "Top picks" row never moves between items (variable
                        // title/overview length would otherwise shift it and make
                        // the focused card drag the vertical scroll up). Featured
                        // hero still wraps its content.
                        modifier = if (topPickHeroItem != null) {
                            Modifier.fillMaxWidth().height(viewportHeight * 0.52f)
                        } else {
                            Modifier.fillMaxWidth()
                        },
                    ) {
                        heroCurrent?.let { item ->
                            DebugBoundsFrame(
                                label = "hero-content",
                                color = Color(0xFFFF8A65),
                                modifier = Modifier
                                    // Google-TV places the hero block in the
                                    // upper-middle (eyebrow ~122dp from the top),
                                    // NOT pinned to the bottom. Top-anchored also
                                    // keeps it stable when overview/CTA appear.
                                    .align(Alignment.TopStart)
                                    .fillMaxWidth()
                                    .padding(start = hPad, end = hPad, top = 122.dp),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Title/eyebrow keep a CONSTANT size in both
                                    // collapsed and expanded states (Google-TV
                                    // behaviour) — the only difference on expand
                                    // is that the genre, overview and CTA appear.
                                    val eyebrowSp = 20f
                                    val titleSp = 34f
                                    val titleLhSp = 41f
                                    Column(
                                        modifier = Modifier.fillMaxWidth(
                                            if (topPickHeroItem != null) 0.5f else 0.42f
                                        )
                                    ) {
                                        if (topPickHeroItem != null) {
                                            // Top-pick focused preview: large title
                                            // → source → overview → IMDb·genre·year·runtime meta row.
                                            Text(
                                                item.title,
                                                color = Color.White,
                                                fontSize = 40.sp,
                                                lineHeight = 46.sp,
                                                fontWeight = FontWeight.Normal,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Spacer(Modifier.height(20.dp))
                                            Text(
                                                desktopTopPickSource(item),
                                                color = Color.White.copy(alpha = 0.92f),
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Medium,
                                            )
                                            AnimatedVisibility(
                                                visible = topPickHeroInfoExpanded,
                                                enter = fadeIn(animationSpec = tween(220)) +
                                                    expandVertically(animationSpec = tween(260, easing = FastOutSlowInEasing)),
                                                exit = fadeOut(animationSpec = tween(120)) +
                                                    shrinkVertically(animationSpec = tween(160, easing = FastOutSlowInEasing)),
                                            ) {
                                                Column {
                                                    item.overview?.takeIf { it.isNotBlank() }?.let { ov ->
                                                        Spacer(Modifier.height(18.dp))
                                                        Text(
                                                            ov,
                                                            color = Color.White.copy(alpha = 0.55f),
                                                            fontSize = 15.sp,
                                                            lineHeight = 22.sp,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis,
                                                        )
                                                    }
                                                    Spacer(Modifier.height(16.dp))
                                                    DesktopTopPickMetaRow(item)
                                                }
                                            }
                                        } else {
                                            Text(
                                                desktopHeroEyebrow(item),
                                                color = Color.White,
                                                fontSize = eyebrowSp.sp,
                                                fontWeight = FontWeight.Medium,
                                            )
                                            Spacer(Modifier.height(14.dp))
                                            Text(
                                                item.title,
                                                color = Color.White,
                                                fontSize = titleSp.sp,
                                                lineHeight = titleLhSp.sp,
                                                fontWeight = FontWeight.Light,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            // Genre shows in BOTH states (Google
                                            // TV default already shows it). Only
                                            // overview + CTA are focus-gated.
                                            item.genres?.firstOrNull()?.let { g ->
                                                Spacer(Modifier.height(18.dp))
                                                Text(
                                                    g,
                                                    color = Color.White.copy(alpha = 0.92f),
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                            if (heroPaused) {
                                                item.overview?.takeIf { it.isNotBlank() }?.let { ov ->
                                                    Spacer(Modifier.height(10.dp))
                                                    Text(
                                                        ov,
                                                        color = Color.White.copy(alpha = 0.60f),
                                                        fontSize = 15.sp,
                                                        lineHeight = 22.sp,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (heroPaused) Spacer(Modifier.height(24.dp))

                                    // CTA + dots share one row so the dots are
                                    // vertically centered on the CTA when expanded.
                                    // When collapsed the CTA shrinks to 1dp so this
                                    // row sits right under the title → dots align
                                    // with the title's bottom. Same focusable node
                                    // is kept alive (no swap) for D-pad Down.
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        DebugBoundsFrame(
                                            label = "hero-cta",
                                            color = Color(0xFF00E676),
                                            modifier = if (heroPaused) {
                                                Modifier.height(44.dp).width(176.dp)
                                            } else {
                                                Modifier.size(1.dp).alpha(0f)
                                            },
                                        ) {
                                            org.jellyplus.client.ui.components.FocusableButton(
                                                onClick = { onMediaClick(item) },
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .focusRequester(heroButtonFocus)
                                                    .onFocusChanged {
                                                        if (it.isFocused) {
                                                            topPickHeroItem = null
                                                            hideSection0HeroBackground = false
                                                            heroPaused = true
                                                            section0Focused = true
                                                            onRowsFocusChanged(false)
                                                            scope.launch {
                                                                if (scrollState.value != 0) {
                                                                    markVerticalScrollReason("section0-watch-now-focused")
                                                                    scrollState.animateScrollTo(
                                                                        value = 0,
                                                                        animationSpec = DesktopVerticalScrollAnimation,
                                                                    )
                                                                }
                                                                delay(16)
                                                                if (scrollState.value != 0) {
                                                                    markVerticalScrollReason("section0-watch-now-focused-after-frame")
                                                                    scrollState.animateScrollTo(
                                                                        value = 0,
                                                                        animationSpec = DesktopVerticalScrollAnimation,
                                                                    )
                                                                }
                                                            }
                                                        } else {
                                                            heroPaused = false
                                                            section0Focused = false
                                                        }
                                                    }
                                                    .onKeyEvent { e ->
                                                        if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                                                        when (e.key) {
                                                            Key.DirectionUp -> {
                                                                onFocusExit()
                                                                true
                                                            }
                                                            Key.DirectionDown -> {
                                                                scope.launch {
                                                                    scrollState.animateScrollTo(
                                                                        value = 0,
                                                                        animationSpec = DesktopVerticalScrollAnimation,
                                                                    )
                                                                    delay(16)
                                                                    try { rowFocusRequesters.firstOrNull()?.requestFocus() } catch (_: IllegalStateException) {}
                                                                    delay(16)
                                                                    if (scrollState.value != 0) {
                                                                        scrollState.animateScrollTo(
                                                                            value = 0,
                                                                            animationSpec = DesktopVerticalScrollAnimation,
                                                                        )
                                                                    }
                                                                }
                                                                true
                                                            }
                                                            else -> false
                                                        }
                                                    },
                                                shape = RoundedCornerShape(999.dp),
                                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFE4F3FF),
                                                    contentColor = Color(0xFF111820),
                                                ),
                                            ) {
                                                Text(
                                                    if (item.type == MediaType.SERIES) "View seasons" else "Watch now",
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 15.sp,
                                                )
                                            }
                                        }

                                        Spacer(Modifier.weight(1f))

                                        if (featured.size > 1 && topPickHeroItem == null) {
                                            DebugBoundsFrame(
                                                label = "hero-dots",
                                                color = Color(0xFFBA68C8),
                                                modifier = Modifier,
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                                                ) {
                                                    featured.indices.forEach { i ->
                                                        Box(
                                                            modifier = Modifier
                                                                .size(if (i == heroDotIndex) 9.dp else 7.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    if (i == heroDotIndex) Color.White
                                                                    else Color.White.copy(alpha = 0.24f)
                                                                ),
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (topPicks.isNotEmpty()) {
                        val currentRowIndex = 0
                        DesktopMediaRow(
                            title = "Top picks for you",
                            items = topPicks,
                            baseUrl = state.baseUrl,
                            horizontalPadding = hPad,
                            cardWidth = 150.dp,
                            captionMode = CaptionMode.None,
                            rowFocusRequester = rowFocusRequesters.getOrNull(currentRowIndex),
                            verticalScrollState = scrollState,
                            onSameRowHorizontalFocus = { rowTitle, fromIndex, toIndex, verticalOffset ->
                                markVerticalScrollReason("same-row-horizontal row=$rowTitle $fromIndex->$toIndex")
                                logDebug(
                                    "JellyScroll",
                                    "HORIZONTAL focus row=$rowTitle item=$fromIndex->$toIndex verticalBefore=$verticalOffset",
                                )
                            },
                            onNavigateUp = {
                                if (featured.isNotEmpty()) {
                                    focusHero()
                                } else {
                                    onRowsFocusChanged(false)
                                    onFocusExit()
                                }
                            },
                            onNavigateDown = { focusRow(currentRowIndex + 1) },
                            onItemFocus = { item ->
                                onRowsFocusChanged(true)
                                section0Focused = false
                                hideSection0HeroBackground = false
                                heroPaused = false
                                if (topPickHeroItem?.id != item.id) {
                                    topPickHeroInfoExpanded = false
                                }
                                topPickHeroItem = item
                                keepTopPickHeroInfoVisible()
                            },
                            onItemClick = onMediaClick,
                        )
                    }

                    var rowIndex = if (topPicks.isNotEmpty()) 1 else 0
                    if (homeState.resumeItems.isNotEmpty()) {
                        val currentRowIndex = rowIndex
                        DesktopMediaRow(
                            title = "Continue watching",
                            items = homeState.resumeItems,
                            baseUrl = state.baseUrl,
                            horizontalPadding = hPad,
                            cardWidth = 150.dp,
                            captionMode = CaptionMode.OnFocus,
                            rowFocusRequester = rowFocusRequesters.getOrNull(currentRowIndex),
                            verticalScrollState = scrollState,
                            onSameRowHorizontalFocus = { rowTitle, fromIndex, toIndex, verticalOffset ->
                                markVerticalScrollReason("same-row-horizontal row=$rowTitle $fromIndex->$toIndex")
                                logDebug(
                                    "JellyScroll",
                                    "HORIZONTAL focus row=$rowTitle item=$fromIndex->$toIndex verticalBefore=$verticalOffset",
                                )
                            },
                            onNavigateUp = { focusPreviousRowOrNav(currentRowIndex) },
                            onNavigateDown = { focusRow(currentRowIndex + 1) },
                            onItemFocus = {
                                onRowsFocusChanged(true)
                                section0Focused = false
                                hideSection0HeroBackground = true
                                topPickHeroInfoExpanded = false
                                topPickHeroItem = null
                            },
                            onItemClick = onContinueWatchingClick,
                        )
                        rowIndex++
                    }
                    if (homeState.recentlyAddedItems.isNotEmpty()) {
                        val currentRowIndex = rowIndex
                        DesktopMediaRow(
                            title = "Recently added",
                            items = homeState.recentlyAddedItems,
                            baseUrl = state.baseUrl,
                            horizontalPadding = hPad,
                            cardWidth = 150.dp,
                            captionMode = CaptionMode.OnFocus,
                            rowFocusRequester = rowFocusRequesters.getOrNull(currentRowIndex),
                            verticalScrollState = scrollState,
                            onSameRowHorizontalFocus = { rowTitle, fromIndex, toIndex, verticalOffset ->
                                markVerticalScrollReason("same-row-horizontal row=$rowTitle $fromIndex->$toIndex")
                                logDebug(
                                    "JellyScroll",
                                    "HORIZONTAL focus row=$rowTitle item=$fromIndex->$toIndex verticalBefore=$verticalOffset",
                                )
                            },
                            onNavigateUp = { focusPreviousRowOrNav(currentRowIndex) },
                            onNavigateDown = { focusRow(currentRowIndex + 1) },
                            onItemFocus = {
                                onRowsFocusChanged(true)
                                section0Focused = false
                                hideSection0HeroBackground = true
                                topPickHeroInfoExpanded = false
                                topPickHeroItem = null
                            },
                            onItemClick = onMediaClick,
                        )
                        rowIndex++
                    }
                    genreRows.forEach { (genre, items) ->
                        val currentRowIndex = rowIndex
                        DesktopMediaRow(
                            title = genre,
                            items = items,
                            baseUrl = state.baseUrl,
                            horizontalPadding = hPad,
                            cardWidth = 150.dp,
                            captionMode = CaptionMode.OnFocus,
                            rowFocusRequester = rowFocusRequesters.getOrNull(currentRowIndex),
                            verticalScrollState = scrollState,
                            onSameRowHorizontalFocus = { rowTitle, fromIndex, toIndex, verticalOffset ->
                                markVerticalScrollReason("same-row-horizontal row=$rowTitle $fromIndex->$toIndex")
                                logDebug(
                                    "JellyScroll",
                                    "HORIZONTAL focus row=$rowTitle item=$fromIndex->$toIndex verticalBefore=$verticalOffset",
                                )
                            },
                            onNavigateUp = { focusPreviousRowOrNav(currentRowIndex) },
                            onNavigateDown = { focusRow(currentRowIndex + 1) },
                            onItemFocus = {
                                onRowsFocusChanged(true)
                                section0Focused = false
                                hideSection0HeroBackground = true
                                topPickHeroInfoExpanded = false
                                topPickHeroItem = null
                            },
                            onItemClick = onMediaClick,
                        )
                        rowIndex++
                    }
                }
            }
        }
    }
}

private fun desktopHeroEyebrow(item: MediaItem): String =
    item.genres?.firstOrNull()?.takeIf { it.isNotBlank() }
        ?: when (item.type) {
            MediaType.MOVIE -> "Movie"
            MediaType.SERIES -> "TV Series"
            MediaType.EPISODE -> "Episode"
            else -> item.type.value
        }


private enum class CaptionMode { None, OnFocus, Always }

private val LandscapeCardFocusGap = 2.5.dp
private val LandscapeCardCornerRadius = 8.dp
private val LandscapeCardMetadataTopGap = 10.dp
private val LandscapeCardTitleLineHeight = 16.dp
private val LandscapeCardSubtitleLineHeight = 15.dp
private val LandscapeCardMetadataTitleSubtitleGap = 4.dp
private val LandscapeCardMetadataHeight =
    LandscapeCardMetadataTopGap +
        LandscapeCardTitleLineHeight * 2 +
        LandscapeCardMetadataTitleSubtitleGap +
        LandscapeCardSubtitleLineHeight
private val DesktopVerticalScrollAnimation = tween<Float>(
    durationMillis = 360,
    easing = FastOutSlowInEasing,
)

@Composable
private fun DesktopMediaRow(
    title: String,
    items: List<MediaItem>,
    baseUrl: String,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    cardWidth: androidx.compose.ui.unit.Dp,
    captionMode: CaptionMode,
    rowFocusRequester: FocusRequester?,
    verticalScrollState: ScrollState? = null,
    onSameRowHorizontalFocus: (rowTitle: String, fromIndex: Int, toIndex: Int, verticalOffset: Int?) -> Unit = { _, _, _, _ -> },
    onNavigateUp: () -> Unit,
    onNavigateDown: () -> Unit,
    onItemFocus: (MediaItem) -> Unit,
    onItemClick: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var scrollJob by remember { mutableStateOf<Job?>(null) }
    var rowFocusReleaseJob by remember { mutableStateOf<Job?>(null) }
    var lastFocusedIndex by remember(items) { mutableStateOf(0) }
    var rowFocused by remember { mutableStateOf(false) }
    fun alignToStart(index: Int) {
        scrollJob?.cancel()
        scrollJob = scope.launch {
            val itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
            if (itemInfo != null) {
                // LazyRow item offsets are measured in content coordinates; offset 0
                // is exactly where item 0 sits with the row's start padding applied.
                // Keep every focused item pinned to that same left anchor.
                val delta = itemInfo.offset
                if (delta != 0) {
                    listState.animateScrollBy(
                        value = delta.toFloat(),
                        animationSpec = tween(
                            durationMillis = 260,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                }
            } else {
                listState.animateScrollToItem(index)
            }
        }
    }
    val visible = items.take(DashboardMaxVisibleItems)
    DebugBoundsFrame(
        label = "row: $title",
        color = if (title == "Top picks for you") Color(0xFF4DD0E1) else Color(0xFFA5D6A7),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            SectionHeader(
                title = title,
                horizontalPadding = horizontalPadding,
                focused = captionMode == CaptionMode.OnFocus && rowFocused,
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusGroup(),
            ) {
                lazyListItemsIndexed(visible) { index, item ->
                    DesktopLandscapeCard(
                        item = item,
                        baseUrl = baseUrl,
                        captionMode = captionMode,
                        rowFocused = rowFocused,
                        onClick = { onItemClick(item) },
                        onFocusedChange = { focused ->
                            if (focused) {
                                rowFocusReleaseJob?.cancel()
                                rowFocused = true
                            } else {
                                rowFocusReleaseJob?.cancel()
                                rowFocusReleaseJob = scope.launch {
                                    delay(80)
                                    rowFocused = false
                                }
                            }
                        },
                        onFocus = {
                            val sameRowItemChange = rowFocused && index != lastFocusedIndex
                            val previousVerticalScroll = verticalScrollState?.value
                            if (sameRowItemChange) {
                                onSameRowHorizontalFocus(title, lastFocusedIndex, index, previousVerticalScroll)
                            }
                            lastFocusedIndex = index
                            onItemFocus(item)
                            alignToStart(index)
                            if (sameRowItemChange && previousVerticalScroll != null) {
                                scope.launch {
                                    delay(16)
                                    if (verticalScrollState.value != previousVerticalScroll) {
                                        logDebug(
                                            "JellyScroll",
                                            "RESTORE vertical after horizontal row=$title target=$previousVerticalScroll actual=${verticalScrollState.value} pass=1",
                                        )
                                        verticalScrollState.scrollTo(previousVerticalScroll)
                                    }
                                    delay(16)
                                    if (verticalScrollState.value != previousVerticalScroll) {
                                        logDebug(
                                            "JellyScroll",
                                            "RESTORE vertical after horizontal row=$title target=$previousVerticalScroll actual=${verticalScrollState.value} pass=2",
                                        )
                                        verticalScrollState.scrollTo(previousVerticalScroll)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .width(cardWidth)
                            .then(if (index == lastFocusedIndex && rowFocusRequester != null) Modifier.focusRequester(rowFocusRequester) else Modifier)
                            .onKeyEvent { e ->
                                if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                                when (e.key) {
                                    Key.DirectionUp -> {
                                        onNavigateUp()
                                        true
                                    }
                                    Key.DirectionDown -> {
                                        onNavigateDown()
                                        true
                                    }
                                    Key.DirectionLeft -> index == 0
                                    Key.DirectionRight -> index == visible.lastIndex
                                    else -> false
                                }
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun DesktopLandscapeCard(
    item: MediaItem,
    baseUrl: String,
    captionMode: CaptionMode,
    rowFocused: Boolean,
    onClick: () -> Unit,
    onFocusedChange: (Boolean) -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val heartbeat by rememberInfiniteTransition(label = "cardFocusHeartbeat").animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cardFocusHeartbeatAlpha",
    )
    val shape = RoundedCornerShape(LandscapeCardCornerRadius)
    val showCaption = captionMode == CaptionMode.Always ||
        (captionMode == CaptionMode.OnFocus && isFocused)
    val glowColor = remember(item.id, item.imageTags, item.backdropImageTags, item.parentBackdropImageTags) {
        item.focusGlowColor()
    }
    val imageAlpha = when {
        isFocused -> 1f
        rowFocused -> 0.78f
        else -> 0.42f
    }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .zIndex(if (isFocused) 10f else 0f)
                .drawBehind {
                    if (isFocused) {
                        val strokePx = (1f + heartbeat * 0.6f).dp.toPx()
                        val gapPx = LandscapeCardFocusGap.toPx()
                        val outset = gapPx + strokePx / 2f
                        val corner = LandscapeCardCornerRadius.toPx() + gapPx
                        val glowOutsetX = size.width * 0.52f
                        val glowOutsetY = size.height * 0.62f
                        val glowBrush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.30f * heartbeat),
                                glowColor.copy(alpha = 0.16f * heartbeat),
                                glowColor.copy(alpha = 0.06f * heartbeat),
                                Color.Transparent,
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.5f),
                            radius = maxOf(size.width, size.height) * 1.45f,
                        )
                        drawRoundRect(
                            brush = glowBrush,
                            topLeft = Offset(-glowOutsetX, -glowOutsetY),
                            size = Size(size.width + glowOutsetX * 2f, size.height + glowOutsetY * 2f),
                            cornerRadius = CornerRadius(
                                LandscapeCardCornerRadius.toPx() + glowOutsetY,
                                LandscapeCardCornerRadius.toPx() + glowOutsetY,
                            ),
                        )
                        drawRoundRect(
                            brush = glowBrush,
                            topLeft = Offset(-glowOutsetX * 0.62f, -glowOutsetY * 0.62f),
                            size = Size(size.width + glowOutsetX * 1.24f, size.height + glowOutsetY * 1.24f),
                            cornerRadius = CornerRadius(
                                LandscapeCardCornerRadius.toPx() + glowOutsetY * 0.62f,
                                LandscapeCardCornerRadius.toPx() + glowOutsetY * 0.62f,
                            ),
                            style = Stroke(width = 22.dp.toPx()),
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.58f + heartbeat * 0.42f),
                            topLeft = Offset(-outset, -outset),
                            size = Size(size.width + outset * 2f, size.height + outset * 2f),
                            cornerRadius = CornerRadius(corner, corner),
                            style = Stroke(width = strokePx),
                        )
                    }
                },
        ) {
            Surface(
                onClick = onClick,
                shape = shape,
                color = Color.White.copy(alpha = 0.05f),
                modifier = Modifier
                    .fillMaxSize()
                    .onFocusChanged {
                        isFocused = it.isFocused
                        onFocusedChange(it.isFocused)
                        if (it.isFocused) onFocus()
                    },
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = item.getBackdropUrl(baseUrl) ?: item.getImageUrl(baseUrl),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize().alpha(imageAlpha),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
        if (captionMode == CaptionMode.OnFocus && rowFocused) {
            Box(modifier = Modifier.height(LandscapeCardMetadataHeight)) {
                if (isFocused) {
                    Column {
                        Spacer(Modifier.height(LandscapeCardMetadataTopGap))
                        Text(
                            item.title,
                            color = Color.White.copy(alpha = 0.94f),
                            fontSize = 13.sp,
                            lineHeight = LandscapeCardTitleLineHeight.value.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(LandscapeCardMetadataTitleSubtitleGap))
                        Text(
                            desktopCardSubtitle(item).orEmpty(),
                            color = Color.White.copy(alpha = 0.66f),
                            fontSize = 12.sp,
                            lineHeight = LandscapeCardSubtitleLineHeight.value.sp,
                            fontWeight = FontWeight.Medium,
                            minLines = 1,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        if (captionMode == CaptionMode.Always) {
            Spacer(Modifier.height(8.dp))
            Text(
                item.title,
                color = Color.White.copy(alpha = if (isFocused) 1f else 0.85f),
                fontSize = if (isFocused) 14.sp else 12.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
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

private fun MediaItem.focusGlowColor(): Color {
    val seed = buildString {
        imageTags?.entries?.sortedBy { it.key }?.forEach { append(it.key).append(it.value) }
        backdropImageTags?.forEach { append(it) }
        parentBackdropImageTags?.forEach { append(it) }
        append(id)
        append(title)
    }
    val hash = seed.fold(0x45D9F3B) { acc, c -> acc * 31 + c.code }
    fun channel(shift: Int): Float = 0.32f + (((hash ushr shift) and 0xFF) / 255f) * 0.58f
    val r = channel(0)
    val g = channel(8)
    val b = channel(16)
    val max = maxOf(r, g, b)
    return Color(
        red = r + (max - r) * 0.18f,
        green = g + (max - g) * 0.18f,
        blue = b + (max - b) * 0.18f,
    )
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
    val rows = remember(favorites, watchLater) {
        buildList {
            if (favorites.isNotEmpty()) add("Favorites" to favorites)
            if (watchLater.isNotEmpty()) add("Watch later" to watchLater)
        }
    }
    val rowFocusRequesters = remember(rows.size) { List(rows.size) { FocusRequester() } }
    val scope = rememberCoroutineScope()
    fun focusRow(index: Int) {
        val requester = rowFocusRequesters.getOrNull(index) ?: return
        scope.launch {
            delay(16)
            try { requester.requestFocus() } catch (_: IllegalStateException) {}
        }
    }
    rowFocusRequesters.firstOrNull()?.let { RequestInitialFocus(it, rows.size) }
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
                rowFocusRequester = rowFocusRequesters.getOrNull(index),
                verticalScrollState = scrollState,
                onNavigateUp = { focusRow(index - 1) },
                onNavigateDown = { focusRow(index + 1) },
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

private fun desktopTopPickSource(item: MediaItem): String =
    item.studios?.firstOrNull()?.name?.takeIf { it.isNotBlank() }
        ?: item.seriesName?.takeIf { it.isNotBlank() }
        ?: "Jellyfin"

@Composable
private fun DesktopTopPickMetaRow(item: MediaItem) {
    val parts = buildList {
        item.genres?.firstOrNull()?.let { add(it) }
        item.year?.let { add(it.toString()) }
        item.runTimeTicks?.let { ticks ->
            val minutes = ticks / 10_000_000 / 60
            if (minutes > 0) {
                val h = minutes / 60
                val m = minutes % 60
                add(if (h > 0) "$h hr $m min" else "$m min")
            }
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item.rating?.let { rating ->
            Surface(color = Color(0xFFF5C518), shape = RoundedCornerShape(3.dp)) {
                Text(
                    "IMDb",
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                rating.toString(),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            if (parts.isNotEmpty()) {
                Text("•", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
            }
        }
        parts.forEachIndexed { i, p ->
            Text(
                p,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            if (i < parts.lastIndex) {
                Text("•", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
            }
        }
    }
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
    focused: Boolean = false,
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
            color = Color.White.copy(alpha = if (focused) 0.92f else 0.58f),
            fontSize = if (focused) 18.sp else 13.sp,
            fontWeight = if (focused) FontWeight.Medium else FontWeight.Normal,
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
                        r.format(1),
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
