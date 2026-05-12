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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.isDebug
import org.jellyplus.client.ui.components.MediaPoster
import org.jellyplus.client.ui.components.MediaPosterPlaceholder
import org.jellyplus.client.ui.components.MediaRowPlaceholder
import org.jellyplus.client.ui.viewmodels.MainViewModel
import org.jellyplus.client.ui.viewmodels.SessionViewModel

@Composable
fun MobileMainScreen(
    viewModel: MainViewModel,
    sessionViewModel: SessionViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onViewAll: (MediaType, String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val sessionState by sessionViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
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
                    icon = { Icon(Icons.Default.History, null) },
                    label = { Text("History") },
                    colors = navigationColors()
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    label = { Text("Favorites") },
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
                0 -> HomeContent(viewModel, state, state.baseUrl, onMediaClick, onViewAll, paddingValues)
                1 -> MobileHistoryScreen(paddingValues = paddingValues, onMediaClick = onMediaClick)
                2 -> FavoritesContent(state, state.baseUrl, onMediaClick, paddingValues)
                3 -> ProfileContent(
                    sessionViewModel = sessionViewModel,
                    onLogout = { sessionViewModel.logout() },
                    paddingValues = paddingValues
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    viewModel: MainViewModel,
    state: org.jellyplus.client.ui.viewmodels.MainState,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
    onViewAll: (MediaType, String) -> Unit,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
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
            // DATA STATE
            val heroItem = state.items.firstOrNull()

            item {
                Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                    heroItem?.let { item ->
                        AsyncImage(
                            model = item.getImageUrl(baseUrl),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Status Bar Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF0F1113)),
                                        startY = 600f
                                    )
                                )
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
                                Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                                Spacer(Modifier.width(8.dp))
                                Text("Play", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    } ?: if (state.isLoading) {
                        MediaPosterPlaceholder(Modifier.fillMaxSize())
                    } else {
                        Spacer(Modifier.height(1.dp))
                    }
                }
            }

            if (state.movies.isNotEmpty()) {
                item {
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

            if (state.items.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    SectionHeader("Continue Watching")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.items.reversed().take(5)) { item ->
                            MobileContinueWatchingCard(item, baseUrl, onClick = { onMediaClick(item) })
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun SearchContent(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).statusBarsPadding()
    ) {
        Text("Search", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.5f))
                Spacer(Modifier.width(12.dp))
                Text("Search for movies, shows...", color = Color.White.copy(alpha = 0.3f))
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("Popular Searches", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FavoritesContent(
    state: org.jellyplus.client.ui.viewmodels.MainState,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).statusBarsPadding()
    ) {
        Text("Favorites", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorites yet", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(state.items.take(3)) { item ->
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
    sessionViewModel: SessionViewModel,
    onLogout: () -> Unit,
    paddingValues: PaddingValues
) {
    val sessionState by sessionViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Profile",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(48.dp))
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = Color.Black, modifier = Modifier.size(64.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Jellyfin User", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(sessionViewModel.getBaseUrl(), color = Color.White.copy(alpha = 0.5f))

        Spacer(Modifier.height(48.dp))
        ProfileOption("Account Settings", Icons.Default.Settings)
        ProfileOption("Playback Preferences", Icons.Default.PlayArrow)

        // Persist Demo Option - Only for Debug builds
        if (isDebug()) {
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

@Composable
private fun ProfileOption(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
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
private fun SectionHeader(title: String, onViewAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
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
