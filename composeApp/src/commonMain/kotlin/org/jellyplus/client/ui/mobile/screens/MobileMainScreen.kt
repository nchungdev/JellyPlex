package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.Constants
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.isDebug
import org.jellyplus.client.ui.components.MediaPoster
import org.jellyplus.client.ui.components.MediaPosterPlaceholder
import org.jellyplus.client.ui.components.MediaRowPlaceholder
import org.jellyplus.client.ui.viewmodels.HomeViewModel
import org.jellyplus.client.ui.viewmodels.MainViewModel
import org.jellyplus.client.ui.viewmodels.PlaybackPreferencesState
import org.jellyplus.client.ui.viewmodels.PlaybackPreferencesViewModel
import org.jellyplus.client.ui.viewmodels.SearchViewModel
import org.jellyplus.client.ui.viewmodels.SessionViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MobileMainScreen(
    viewModel: MainViewModel,
    sessionViewModel: SessionViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
    onViewAll: (MediaType, String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sessionState by sessionViewModel.uiState.collectAsState()
    val homeViewModel: HomeViewModel = koinViewModel()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) homeViewModel.loadHomeContent()
    }

    Scaffold(
        topBar = {
            MobileTopHeader(
                title = when (selectedTab) {
                    1 -> "Search"
                    2 -> "History"
                    3 -> "Profile"
                    else -> null
                },
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black.copy(alpha = 0.95f),
                contentColor = Color.White,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") },
                    colors = navigationColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Search, null) },
                    label = { Text("Search") },
                    colors = navigationColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, null) },
                    label = { Text("History") },
                    colors = navigationColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Profile") },
                    colors = navigationColors()
                )
            }
        },
        containerColor = Color(0xFF0F1113)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> HomeContent(
                    viewModel,
                    homeViewModel,
                    state,
                    state.baseUrl,
                    onMediaClick,
                    onContinueWatchingClick,
                    onContinueWatchingHeaderClick = { selectedTab = 2 },
                    onViewAll,
                    paddingValues,
                )
                1 -> SearchContent(
                    paddingValues = paddingValues,
                    onMediaClick = onMediaClick,
                )
                2 -> MobileHistoryScreen(
                    paddingValues = paddingValues,
                    onMediaClick = onContinueWatchingClick,
                )
                3 -> ProfileContent(
                    state = state,
                    baseUrl = state.baseUrl,
                    sessionViewModel = sessionViewModel,
                    onMediaClick = onMediaClick,
                    onLogout = { sessionViewModel.logout() },
                    paddingValues = paddingValues,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    viewModel: MainViewModel,
    homeViewModel: HomeViewModel,
    state: org.jellyplus.client.ui.viewmodels.MainState,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
    onContinueWatchingClick: (MediaItem) -> Unit,
    onContinueWatchingHeaderClick: () -> Unit,
    onViewAll: (MediaType, String) -> Unit,
    paddingValues: PaddingValues,
) {
    val homeState by homeViewModel.state.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding(),
            bottom = paddingValues.calculateBottomPadding(),
        )
    ) {
        if (state.isLoading && state.items.isEmpty()) {
            // GLOBAL SKELETON
            item {
                MediaPosterPlaceholder(Modifier.fillMaxWidth().height(500.dp))
            }
            item {
                Column(Modifier.padding(top = 16.dp)) {
                    SectionHeader("Movies")
                    MediaRowPlaceholder()
                    Spacer(Modifier.height(20.dp))
                    SectionHeader("TV Series")
                    MediaRowPlaceholder()
                }
            }
        } else if (!state.isLoading && state.items.isEmpty() && state.error != null) {
            // ERROR STATE
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 100.dp, start = 32.dp, end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CloudOff, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Connection Error", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(state.error ?: "Unable to reach server", color = Color.White.copy(alpha = 0.5f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = { viewModel.loadData() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Retry", color = Color.Black)
                    }
                }
            }
        } else if (state.items.isEmpty()) {
            // EMPTY STATE
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inbox, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(100.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Library is empty", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("No media found on this server", color = Color.White.copy(alpha = 0.5f))
                }
            }
        } else {
            val heroItem = homeState.featuredItems.firstOrNull()

            if (heroItem != null || homeState.isLoading) item {
                Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                    if (heroItem != null) {
                        val item = heroItem
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onMediaClick(item) },
                        ) {
                            AsyncImage(
                                model = item.getBackdropUrl(baseUrl) ?: item.getImageUrl(baseUrl),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF0F1113)), startY = 300f))
                        )
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(item.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { onMediaClick(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(52.dp).fillMaxWidth(0.8f)
                            ) {
                                Icon(Icons.Default.Info, null, tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text("Details", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (homeState.isLoading) {
                        MediaPosterPlaceholder(Modifier.fillMaxSize())
                    }
                }
            }

            // Continue Watching — đặt đầu tiên vì đây là nội dung ưu tiên
            if (homeState.resumeItems.isNotEmpty()) {
                item {
                    SectionHeader("Continue Watching", onViewAll = onContinueWatchingHeaderClick)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(homeState.resumeItems) { item ->
                            MobileContinueWatchingCard(item, baseUrl, onClick = { onContinueWatchingClick(item) })
                        }
                    }
                }
            }

            // Recently Added
            if (homeState.recentlyAddedItems.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    SectionHeader("Recently Added")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(homeState.recentlyAddedItems) { item ->
                            MediaPoster(item, baseUrl, onClick = { onMediaClick(item) }, modifier = Modifier.width(120.dp))
                        }
                    }
                }
            }

            if (state.movies.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    SectionHeader("Movies", onViewAll = { onViewAll(MediaType.MOVIE, "Movies") })
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.movies) { item ->
                            MediaPoster(item, baseUrl, onClick = { onMediaClick(item) }, modifier = Modifier.width(120.dp))
                        }
                    }
                }
            }

            if (state.tvShows.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    SectionHeader("TV Series", onViewAll = { onViewAll(MediaType.SERIES, "TV Series") })
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.tvShows) { item ->
                            MediaPoster(item, baseUrl, onClick = { onMediaClick(item) }, modifier = Modifier.width(120.dp))
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

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
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F1113).copy(alpha = 0.96f))
            .statusBarsPadding()
            .height(56.dp)
            .padding(start = 16.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (title == null) {
            AppLogoText()
        } else {
            Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.size(48.dp))
    }
}

@Composable
private fun SearchContent(
    paddingValues: PaddingValues,
    onMediaClick: (MediaItem) -> Unit,
) {
    val searchViewModel: SearchViewModel = koinViewModel()
    val state by searchViewModel.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
            TextField(
                value = state.query,
                onValueChange = { searchViewModel.onQueryChange(it) },
                placeholder = { Text("Movies, shows, people...", color = Color.White.copy(alpha = 0.4f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.08f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.5f)) },
                trailingIcon = {
                    if (state.query.isNotBlank()) {
                        IconButton(onClick = { searchViewModel.clearQuery() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.White)
                        }
                    }
                },
            )
            Spacer(Modifier.height(12.dp))

            when {
                state.query.isBlank() -> {
                    if (state.searchHistory.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Recent searches", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { searchViewModel.clearHistory() }) {
                                Text("Clear", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(state.searchHistory) { query ->
                                SearchHistoryRow(
                                    query = query,
                                    onClick = { searchViewModel.onQueryChange(query) },
                                )
                            }
                        }
                    } else {
                        Text(
                            "Search for movies and shows",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                }
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                state.error != null -> {
                    Text(state.error ?: "Search failed", color = Color.White.copy(alpha = 0.55f), fontSize = 14.sp)
                }
                state.results.isEmpty() -> {
                    Text("No results found for '${state.query}'", color = Color.White.copy(alpha = 0.55f), fontSize = 14.sp)
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.results) { item ->
                            SearchResultRow(
                                item = item,
                                baseUrl = state.baseUrl,
                                onClick = { onMediaClick(item) },
                            )
                        }
                    }
                }
            }
    }
}

@Composable
private fun SearchHistoryRow(query: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.History, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(query, color = Color.White, fontSize = 15.sp)
    }
}

@Composable
private fun SearchResultRow(item: MediaItem, baseUrl: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = item.getImageUrl(baseUrl),
            contentDescription = null,
            modifier = Modifier
                .width(56.dp)
                .height(84.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.06f)),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Text(
                text = when (item.type) {
                    MediaType.MOVIE -> item.year?.let { "Movie · $it" } ?: "Movie"
                    MediaType.SERIES -> item.year?.let { "TV Series · $it" } ?: "TV Series"
                    MediaType.EPISODE -> item.seriesName?.let { "$it · Episode" } ?: "Episode"
                    else -> item.type.value
                },
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FavoritesContent(
    state: org.jellyplus.client.ui.viewmodels.MainState,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)
    ) {
        val favoriteItems = state.items.filter { it.userData?.isFavorite == true }
        if (favoriteItems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorites yet", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(favoriteItems) { item ->
                    FavoriteItemCard(item, baseUrl, onMediaClick)
                }
            }
        }
    }
}

@Composable
private fun FavoriteItemCard(item: MediaItem, baseUrl: String, onClick: (MediaItem) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick(item) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.getImageUrl(baseUrl),
            contentDescription = null,
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(item.title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(item.year?.toString() ?: "", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun ProfileContent(
    state: org.jellyplus.client.ui.viewmodels.MainState,
    baseUrl: String,
    sessionViewModel: SessionViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onLogout: () -> Unit,
    paddingValues: PaddingValues,
) {
    val sessionState by sessionViewModel.uiState.collectAsState()
    val favoriteItems = state.items.filter { it.userData?.isFavorite == true }
    val isDemoServer = sessionViewModel.getBaseUrl().contains(Constants.DEMO_SERVER_HOST, ignoreCase = true)
    val playbackPreferencesViewModel: PlaybackPreferencesViewModel = koinViewModel()
    val playbackPreferences by playbackPreferencesViewModel.state.collectAsState()
    var showPlaybackPreferences by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = Color.Black, modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(sessionViewModel.getUserName() ?: "Jellyfin User", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(sessionViewModel.getBaseUrl(), color = Color.White.copy(alpha = 0.5f))

            Spacer(Modifier.height(40.dp))
            SectionHeader("Favorites")
            if (favoriteItems.isEmpty()) {
                Text(
                    "No favorites yet",
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        items(favoriteItems) { item ->
            FavoriteItemCard(item, baseUrl, onMediaClick)
            Spacer(Modifier.height(12.dp))
        }

        item {
            Spacer(Modifier.height(24.dp))
            ProfileOption("Account Settings", Icons.Default.Settings)
            ProfileOption(
                label = "Playback Preferences",
                icon = Icons.Default.PlayArrow,
                onClick = { showPlaybackPreferences = !showPlaybackPreferences },
            )
            if (showPlaybackPreferences) {
                PlaybackPreferencesPanel(
                    state = playbackPreferences,
                    onAutoSkipIntroChange = playbackPreferencesViewModel::setAutoSkipIntro,
                    onAutoSkipOutroChange = playbackPreferencesViewModel::setAutoSkipOutro,
                    onAutoSkipRecapChange = playbackPreferencesViewModel::setAutoSkipRecap,
                    onAutoSkipPreviewChange = playbackPreferencesViewModel::setAutoSkipPreview,
                    onAutoNextChange = playbackPreferencesViewModel::setAutoNext,
                    onAutoPipChange = playbackPreferencesViewModel::setAutoPictureInPicture,
                    onSeamlessTransitionChange = playbackPreferencesViewModel::setSeamlessTransition,
                    onPreferOriginalAudioChange = playbackPreferencesViewModel::setPreferOriginalAudio,
                    onShowGestureHintsChange = playbackPreferencesViewModel::setShowGestureHints,
                    onPlaybackSpeedChange = playbackPreferencesViewModel::setPlaybackSpeed,
                    onSeekBackChange = playbackPreferencesViewModel::setSeekBackSeconds,
                    onSeekForwardChange = playbackPreferencesViewModel::setSeekForwardSeconds,
                )
            }

            if (isDebug() || isDemoServer) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, null, tint = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Keep trying demo", color = Color.White, fontSize = 16.sp)
                        Text("Persist demo session across restarts", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = sessionState.persistDemo,
                        onCheckedChange = { sessionViewModel.togglePersistDemo(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            ProfileOption("Help & Support", Icons.Default.Info)
            Spacer(Modifier.height(32.dp))
            TextButton(onClick = onLogout) {
                Text("Sign Out", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.7f))
        Spacer(Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 16.sp)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f))
    }
}

@Composable
private fun PlaybackPreferencesPanel(
    state: PlaybackPreferencesState,
    onAutoSkipIntroChange: (Boolean) -> Unit,
    onAutoSkipOutroChange: (Boolean) -> Unit,
    onAutoSkipRecapChange: (Boolean) -> Unit,
    onAutoSkipPreviewChange: (Boolean) -> Unit,
    onAutoNextChange: (Boolean) -> Unit,
    onAutoPipChange: (Boolean) -> Unit,
    onSeamlessTransitionChange: (Boolean) -> Unit,
    onPreferOriginalAudioChange: (Boolean) -> Unit,
    onShowGestureHintsChange: (Boolean) -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    onSeekBackChange: (Int) -> Unit,
    onSeekForwardChange: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(14.dp),
    ) {
        PreferenceGroupTitle("Speed")
        PreferenceChips(
            labels = listOf("0.75x", "1x", "1.25x", "1.5x", "2x"),
            selectedIndex = listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).indexOf(state.playbackSpeed).coerceAtLeast(1),
            onSelect = { index ->
                onPlaybackSpeedChange(listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)[index])
            },
        )

        PreferenceGroupTitle("Auto skip")
        PreferenceSwitchRow("Intro", "Skip intro segments when available", state.autoSkipIntro, onAutoSkipIntroChange)
        PreferenceSwitchRow("Outro / Credits", "Jump credits when the server marks them", state.autoSkipOutro, onAutoSkipOutroChange)
        PreferenceSwitchRow("Recap", "Skip recap segments before an episode starts", state.autoSkipRecap, onAutoSkipRecapChange)
        PreferenceSwitchRow("Preview", "Skip preview/trailer segments", state.autoSkipPreview, onAutoSkipPreviewChange)

        PreferenceGroupTitle("Episode flow")
        PreferenceSwitchRow("Auto Next", "Continue to the next episode automatically", state.autoNext, onAutoNextChange)
        PreferenceSwitchRow("Seamless transition", "Preload the next episode for faster handoff", state.seamlessTransition, onSeamlessTransitionChange)

        PreferenceGroupTitle("Language & mobile")
        PreferenceSwitchRow("Prefer original audio", "Choose original language tracks when Jellyfin can identify them", state.preferOriginalAudio, onPreferOriginalAudioChange)
        PreferenceSwitchRow("Auto PiP", "Enter picture-in-picture when leaving playback", state.autoPictureInPicture, onAutoPipChange)
        PreferenceSwitchRow("Gesture hints", "Show brightness/volume feedback while swiping", state.showGestureHints, onShowGestureHintsChange)

        PreferenceGroupTitle("Seek buttons")
        PreferenceChips(
            labels = listOf("-5s", "-10s", "-15s"),
            selectedIndex = listOf(5, 10, 15).indexOf(state.seekBackSeconds).coerceAtLeast(0),
            onSelect = { index -> onSeekBackChange(listOf(5, 10, 15)[index]) },
        )
        Spacer(Modifier.height(8.dp))
        PreferenceChips(
            labels = listOf("+10s", "+30s", "+60s"),
            selectedIndex = listOf(10, 30, 60).indexOf(state.seekForwardSeconds).coerceAtLeast(0),
            onSelect = { index -> onSeekForwardChange(listOf(10, 30, 60)[index]) },
        )
    }
}

@Composable
private fun PreferenceGroupTitle(title: String) {
    Text(
        title,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
    )
}

@Composable
private fun PreferenceSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.18f),
            ),
        )
    }
}

@Composable
private fun PreferenceChips(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.10f))
                    .clickable { onSelect(index) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color = if (selected) Color.Black else Color.White,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onViewAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onViewAll != null) Modifier.clickable(onClick = onViewAll) else Modifier)
            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        if (onViewAll != null) {
            IconButton(onClick = onViewAll) {
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
internal fun MobileContinueWatchingCard(item: MediaItem, baseUrl: String, onClick: () -> Unit) {
    val progress = if (item.runTimeTicks != null && item.runTimeTicks > 0) {
        item.playbackPositionTicks.toFloat() / item.runTimeTicks.toFloat()
    } else 0f

    Column(
        modifier = Modifier.width(240.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }) {
        Box(modifier = Modifier.height(135.dp).fillMaxWidth()) {
            AsyncImage(
                model = item.getBackdropUrl(baseUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Progress Bar
            if (progress > 0) {
                Box(
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp)
                        .background(Color.Gray.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            val subText = if (item.type == MediaType.EPISODE) {
                "S${item.parentIndexNumber ?: 0}E${item.index ?: 0}"
            } else {
                "Resume watching"
            }
            Text(subText, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
private fun navigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    unselectedIconColor = Color.White.copy(alpha = 0.5f),
    unselectedTextColor = Color.White.copy(alpha = 0.5f),
    indicatorColor = Color.Transparent
)
