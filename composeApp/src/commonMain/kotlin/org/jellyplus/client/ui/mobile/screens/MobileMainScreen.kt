package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.ui.viewmodels.HomeViewModel
import org.jellyplus.client.ui.viewmodels.MainViewModel
import org.jellyplus.client.ui.viewmodels.PlaybackPreferencesViewModel
import org.jellyplus.client.ui.viewmodels.SessionViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MobileMainScreen(
    viewModel: MainViewModel,
    sessionViewModel: SessionViewModel,
    selectedTab: Int,
    onSelectedTabChange: (Int) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
    onViewAll: (MediaType, String) -> Unit,
    onViewAllGenre: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    onSettings: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val homeViewModel: HomeViewModel = koinViewModel()
    val homeState by homeViewModel.state.collectAsState()
    val prefsViewModel: PlaybackPreferencesViewModel = koinViewModel()
    val prefsState by prefsViewModel.state.collectAsState()

    LaunchedEffect(homeState.featuredItems, homeState.resumeItems, homeState.recentlyAddedItems) {
        viewModel.registerItems(
            homeState.featuredItems + homeState.resumeItems + homeState.recentlyAddedItems
        )
    }
    var homeHeaderAlpha by remember { mutableStateOf(0f) }
    var profileHeaderAlpha by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            MobileTopHeader(
                title = when (selectedTab) { 1 -> "Favorites"; 2 -> "Library"; 3 -> "Profile"; else -> null },
                backgroundAlpha = when (selectedTab) { 0 -> homeHeaderAlpha; 3 -> profileHeaderAlpha; else -> 1f },
                showSearchButton = selectedTab != 3,
                onSearch = onSearch,
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.Black.copy(alpha = 0.95f), contentColor = Color.White, tonalElevation = 0.dp) {
                NavigationBarItem(selectedTab == 0, { onSelectedTabChange(0) }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") }, colors = navigationColors())
                NavigationBarItem(selectedTab == 1, { onSelectedTabChange(1) }, icon = { Icon(Icons.Default.Favorite, null) }, label = { Text("Favorites") }, colors = navigationColors())
                NavigationBarItem(selectedTab == 2, { onSelectedTabChange(2) }, icon = { Icon(Icons.Default.Bookmark, null) }, label = { Text("Library") }, colors = navigationColors())
                NavigationBarItem(selectedTab == 3, { onSelectedTabChange(3) }, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") }, colors = navigationColors())
            }
        },
        containerColor = Color(0xFF181818),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeContent(
                    viewModel = viewModel, homeViewModel = homeViewModel,
                    state = state, baseUrl = state.baseUrl,
                    onMediaClick = onMediaClick, onContinueWatchingClick = onContinueWatchingClick,
                    onContinueWatchingHeaderClick = { onSelectedTabChange(2) }, // Library
                    onViewAll = onViewAll, onViewAllGenre = onViewAllGenre,
                    onToggleWatchLater = { viewModel.toggleWatchLater(it) },
                    isWatchLater = { viewModel.isWatchLater(it) },
                    isFavorite = { viewModel.isFavorite(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    paddingValues = paddingValues,
                    homeSectionOrder = prefsState.homeSectionOrder,
                    homeEnabledSections = prefsState.homeEnabledSections,
                    onHeaderAlphaChange = { homeHeaderAlpha = it },
                )
                1 -> MediaCollectionContent("No favorites yet", state.favoriteItems, state.baseUrl, onMediaClick, paddingValues)
                2 -> MobileLibraryScreen(
                    watchLaterItems = state.watchLaterItems,
                    baseUrl = state.baseUrl,
                    onMediaClick = onMediaClick,
                    onContinueWatchingClick = onContinueWatchingClick,
                    paddingValues = paddingValues,
                )
                3 -> ProfileContent(
                    state = state, baseUrl = state.baseUrl, sessionViewModel = sessionViewModel,
                    onMediaClick = onMediaClick, onFavorites = { onSelectedTabChange(1) },
                    onHistory = { onSelectedTabChange(2) }, onSettings = onSettings,
                    onSwitchServer = { sessionViewModel.switchServer() },
                    onHeaderAlphaChange = { profileHeaderAlpha = it },
                    onLogout = { sessionViewModel.logout() }, paddingValues = paddingValues,
                )
            }
        }
    }
}

// ── Top header ────────────────────────────────────────────────────────────────

@Composable
private fun AppLogoText() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Jelly", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text("Plus", color = MaterialTheme.colorScheme.primary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun MobileTopHeader(
    title: String?,
    backgroundAlpha: Float = 1f,
    showSearchButton: Boolean = false,
    onSearch: () -> Unit = {},
    showSettingsButton: Boolean = false,
    onSettings: () -> Unit = {},
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFF181818).copy(alpha = backgroundAlpha.coerceIn(0f, 1f)))
            .statusBarsPadding().height(56.dp).padding(start = 16.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (title == null) AppLogoText()
        else Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        when {
            showSearchButton -> IconButton(onClick = onSearch) { Icon(Icons.Default.Search, "Search", tint = Color.White) }
            showSettingsButton -> IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings", tint = Color.White) }
            else -> Spacer(Modifier.size(48.dp))
        }
    }
}

// ── Library tab (Watch Later + History) ───────────────────────────────────────

@Composable
private fun MobileLibraryScreen(
    watchLaterItems: List<MediaItem>,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
    paddingValues: PaddingValues,
) {
    var sub by remember { mutableStateOf(0) } // 0 = Watch Later, 1 = History
    Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LibraryTab("Watch Later", sub == 0) { sub = 0 }
            LibraryTab("History", sub == 1) { sub = 1 }
        }
        Box(Modifier.weight(1f)) {
            val innerPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
            if (sub == 0) {
                MediaCollectionContent("Nothing saved for later", watchLaterItems, baseUrl, onMediaClick, innerPadding)
            } else {
                MobileHistoryScreen(paddingValues = innerPadding, onMediaClick = onContinueWatchingClick)
            }
        }
    }
}

@Composable
private fun LibraryTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text,
            color = if (selected) Color.Black else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Favorites tab ─────────────────────────────────────────────────────────────

@Composable
private fun MediaCollectionContent(
    emptyText: String,
    items: List<MediaItem>,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
    paddingValues: PaddingValues,
) {
    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp, vertical = 24.dp)) {
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(emptyText, color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items) { item -> FavoriteItemCard(item, baseUrl, onMediaClick) }
            }
        }
    }
}

@Composable
private fun FavoriteItemCard(item: MediaItem, baseUrl: String, onClick: (MediaItem) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick(item) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = item.getImageUrl(baseUrl), contentDescription = null,
            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.size(16.dp))
        Column {
            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(item.year?.toString() ?: "", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(16.dp))
    }
}

// ── Nav bar colors ────────────────────────────────────────────────────────────

@Composable
private fun navigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = Color.White.copy(alpha = 0.5f),
    unselectedTextColor = Color.White.copy(alpha = 0.5f),
    indicatorColor = Color.Transparent,
)
