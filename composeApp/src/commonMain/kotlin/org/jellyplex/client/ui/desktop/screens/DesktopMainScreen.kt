package org.jellyplex.client.ui.desktop.screens

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusRequester
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
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.domain.models.MediaType
import org.jellyplex.client.ui.common.navigation.DpadSectionNavigator
import org.jellyplex.client.ui.common.navigation.gridItemDpadHandler
import org.jellyplex.client.ui.common.navigation.rememberDpadGridNavigator
import org.jellyplex.client.ui.common.navigation.rememberDpadSectionNavigator
import org.jellyplex.client.ui.common.navigation.sectionItemDpadHandler
import org.jellyplex.client.ui.components.MediaPoster
import org.jellyplex.client.ui.viewmodels.MainViewModel
import androidx.compose.foundation.lazy.itemsIndexed as lazyListItemsIndexed

// Height of a single dashboard section (header ~72dp + poster row ~228dp)
private val SECTION_HEIGHT = 350.dp

private enum class NavDestination { Home, Movies, TvShows, Favorites, Search }

@Composable
fun DesktopMainScreen(
    viewModel: MainViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onViewAll: (MediaType, String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    var selectedNav by remember { mutableStateOf(NavDestination.Home) }
    val sidebarFocusRequester = remember { FocusRequester() }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1113))) {
        // Vertical Navigation Sidebar
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp)
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Icon(Icons.Default.Movie, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(32.dp))
            NavIcon(Icons.Default.Home, selectedNav == NavDestination.Home, sidebarFocusRequester) { selectedNav = NavDestination.Home }
            NavIcon(Icons.Default.Movie, selectedNav == NavDestination.Movies, sidebarFocusRequester) { selectedNav = NavDestination.Movies }
            NavIcon(Icons.Default.Tv, selectedNav == NavDestination.TvShows, sidebarFocusRequester) { selectedNav = NavDestination.TvShows }
            NavIcon(Icons.Default.Favorite, selectedNav == NavDestination.Favorites, sidebarFocusRequester) { selectedNav = NavDestination.Favorites }
            NavIcon(Icons.Default.Search, selectedNav == NavDestination.Search, sidebarFocusRequester) { selectedNav = NavDestination.Search }
            Spacer(Modifier.weight(1f))
            NavIcon(Icons.Default.Settings, false, sidebarFocusRequester) { }
        }

        // Main Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedNav) {
                NavDestination.Home -> MainDashboard(
                    viewModel = viewModel,
                    state = state,
                    onMediaClick = onMediaClick,
                    onViewAll = onViewAll,
                    onFocusExit = {
                        try { sidebarFocusRequester.requestFocus() } catch (_: IllegalStateException) {}
                    },
                )
                NavDestination.Movies -> MediaGrid("Movies", state.movies, state.baseUrl, onMediaClick)
                NavDestination.TvShows -> MediaGrid("TV Series", state.tvShows, state.baseUrl, onMediaClick)
                NavDestination.Favorites -> MediaGrid("Favorites", state.items.take(3), state.baseUrl, onMediaClick)
                NavDestination.Search -> SearchPlaceholder()
            }
        }
    }
}

@Composable
private fun MainDashboard(
    viewModel: MainViewModel,
    state: org.jellyplex.client.ui.viewmodels.MainState,
    onMediaClick: (MediaItem) -> Unit,
    onViewAll: (MediaType, String) -> Unit,
    onFocusExit: () -> Unit,
) {
    var focusedMediaItem by remember { mutableStateOf<MediaItem?>(null) }
    val navigator = rememberDpadSectionNavigator(count = 3, onExitTop = onFocusExit)

    LaunchedEffect(state.movies.isNotEmpty()) {
        if (state.movies.isNotEmpty()) {
            kotlinx.coroutines.delay(100)
            navigator.focusSection(0)
        }
    }

    var titleFontSize by remember(focusedMediaItem) { mutableStateOf(42.sp) }

    Box(modifier = Modifier.fillMaxSize()) {
        focusedMediaItem?.let { item ->
            AsyncImage(
                model = item.getBackdropUrl(state.baseUrl),
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
                                0.0f to Color.Black.copy(alpha = 0.2f),
                                0.4f to Color.Black.copy(alpha = 0.5f),
                                1.0f to Color(0xFF0F1113),
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .padding(start = 64.dp, top = 64.dp)
                    .fillMaxWidth(0.5f),
            ) {
                Text(
                    text = item.title.uppercase(),
                    color = Color.White,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = titleFontSize * 1.5f,
                    maxLines = 2,
                    overflow = TextOverflow.Visible,
                    onTextLayout = { result ->
                        if (result.hasVisualOverflow && titleFontSize > 20.sp) {
                            titleFontSize *= 0.85f
                        }
                    },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.overview ?: "",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 16.sp,
                    maxLines = 2,
                )
            }
        }

        // Use a scrollable Column instead of LazyColumn to ensure all sections are kept in composition.
        // This fixes focus issues where requesters become detached when items are recycled.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 400.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading && state.items.isEmpty()) {
                // GLOBAL SKELETON
                Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                    DashboardSection(title = "Movies", items = emptyList(), baseUrl = state.baseUrl, isLoading = true, onItemFocus = {}, onItemClick = {}, onViewAll = {}, sectionIndex = 0, navigator = navigator, onExitLeft = {})
                    DashboardSection(title = "TV Series", items = emptyList(), baseUrl = state.baseUrl, isLoading = true, onItemFocus = {}, onItemClick = {}, onViewAll = {}, sectionIndex = 1, navigator = navigator, onExitLeft = {})
                }
            } else if (!state.isLoading && state.items.isEmpty() && state.error != null) {
                // ERROR STATE
                Column(
                    modifier = Modifier.fillMaxWidth().padding(64.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(120.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Connection Error", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text(state.error ?: "Unable to connect to server", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp)
                    Spacer(Modifier.height(32.dp))
                    org.jellyplex.client.ui.components.FocusableButton(
                        onClick = { viewModel.loadData() },
                        modifier = Modifier.width(200.dp).height(56.dp)
                    ) {
                        Text("Retry", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (state.movies.isEmpty() && state.tvShows.isEmpty()) {
                // EMPTY STATE
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inbox, null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(160.dp))
                    Spacer(Modifier.height(32.dp))
                    Text("Library is empty", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("This server has no movies or TV shows to display", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp)
                }
            } else {
                // DATA STATE
                if (state.movies.isNotEmpty()) {
                    DashboardSection(
                        title = "Movies",
                        items = state.movies,
                        baseUrl = state.baseUrl,
                        isLoading = false,
                        onItemFocus = { focusedMediaItem = it },
                        onItemClick = onMediaClick,
                        onViewAll = { onViewAll(MediaType.MOVIE, "Movies") },
                        sectionIndex = 0,
                        navigator = navigator,
                        onExitLeft = onFocusExit,
                    )
                }

                if (state.tvShows.isNotEmpty()) {
                    DashboardSection(
                        title = "TV Series",
                        items = state.tvShows,
                        baseUrl = state.baseUrl,
                        isLoading = false,
                        onItemFocus = { focusedMediaItem = it },
                        onItemClick = onMediaClick,
                        onViewAll = { onViewAll(MediaType.SERIES, "TV Series") },
                        sectionIndex = 1,
                        navigator = navigator,
                        onExitLeft = onFocusExit,
                    )
                }

                if (state.items.isNotEmpty()) {
                    DashboardSection(
                        title = "Continue Watching",
                        items = state.items.reversed(),
                        baseUrl = state.baseUrl,
                        isLoading = false,
                        onItemFocus = { focusedMediaItem = it },
                        onItemClick = onMediaClick,
                        onViewAll = { },
                        sectionIndex = 2,
                        navigator = navigator,
                        onExitLeft = onFocusExit,
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
    onViewAll: () -> Unit,
    sectionIndex: Int,
    navigator: DpadSectionNavigator,
    onExitLeft: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = title, onViewAll = onViewAll)
        if (isLoading && items.isEmpty()) {
            org.jellyplex.client.ui.components.MediaRowPlaceholder(padding = PaddingValues(horizontal = 64.dp))
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 64.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                lazyListItemsIndexed(items) { index, item ->
                    MediaPoster(
                        item = item,
                        baseUrl = baseUrl,
                        onClick = { onItemClick(item) },
                        onFocus = { onItemFocus(item) },
                        modifier = Modifier
                            .width(150.dp)
                            .then(
                                if (index == 0) Modifier.focusRequester(navigator.requesters[sectionIndex])
                                else Modifier
                            )
                            .sectionItemDpadHandler(
                                itemIndex = index,
                                itemCount = items.size,
                                sectionIndex = sectionIndex,
                                navigator = navigator,
                                onExitLeft = onExitLeft,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onViewAll: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 64.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        if (onViewAll != null) {
            IconButton(onClick = onViewAll) {
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.5f))
            }
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
            .padding(start = 64.dp, top = 64.dp, end = 64.dp, bottom = 0.dp)
    ) {
        Text(title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No content found", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            val gridNavigator = rememberDpadGridNavigator(rowCount = (items.size + 4) / 5)

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    val rowIndex = index / 5
                    val colIndex = index % 5

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
                                colCount = if (rowIndex == (items.size - 1) / 5) (items.size % 5).let { if (it == 0) 5 else it } else 5,
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
private fun SearchPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(120.dp))
            Spacer(Modifier.height(24.dp))
            Text("Search functionality coming soon", color = Color.White.copy(alpha = 0.5f), fontSize = 24.sp)
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

    Box(
        modifier = Modifier
            .size(56.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .background(
                color = if (isFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, null,
            tint = when {
                isSelected || isFocused -> MaterialTheme.colorScheme.primary
                else -> Color.White.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(32.dp),
        )
    }
}
