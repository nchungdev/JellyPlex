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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.domain.models.Constants
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.isDebug
import org.jellyplus.client.ui.components.AppActionRow
import org.jellyplus.client.ui.viewmodels.MainState
import org.jellyplus.client.ui.viewmodels.SessionViewModel

@Composable
internal fun ProfileContent(
    state: MainState,
    baseUrl: String,
    sessionViewModel: SessionViewModel,
    onMediaClick: (MediaItem) -> Unit,
    onFavorites: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onHeaderAlphaChange: (Float) -> Unit,
    onLogout: () -> Unit,
    paddingValues: PaddingValues,
) {
    val sessionState by sessionViewModel.uiState.collectAsState()
    val isDemoServer = sessionViewModel.getBaseUrl().contains(Constants.DEMO_SERVER_HOST, ignoreCase = true)
    val favoriteItems = state.items.filter { it.userData?.isFavorite == true }
    val userName = sessionViewModel.getUserName() ?: "Jellyfin User"
    val userInitial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "J"
    val serverHost = sessionViewModel.getBaseUrl().removePrefix("https://").removePrefix("http://").trimEnd('/')
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val collapseDistancePx = with(density) { 220.dp.toPx() }
    val collapseProgress by remember {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 1f
                else -> (listState.firstVisibleItemScrollOffset / collapseDistancePx).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(collapseProgress) { onHeaderAlphaChange(collapseProgress) }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(bottom = 96.dp),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        // Hero avatar card
        item { ProfileHeroCard(userName, userInitial, serverHost, collapseProgress) }

        // Stats
        item {
            ProfileStatsRow(state.movies.size, state.tvShows.size, favoriteItems.size)
            Spacer(Modifier.height(24.dp))
        }

        // Library shortcuts
        item {
            ProfileSectionLabel("YOUR LIBRARY")
            AppActionRow(Icons.Default.Favorite, "Favorites",
                "${favoriteItems.size} saved title${if (favoriteItems.size == 1) "" else "s"}", onFavorites)
            AppActionRow(Icons.Default.History, "Watch history",
                "Continue watching and recently played", onHistory)
            Spacer(Modifier.height(20.dp))
        }

        // App settings
        item {
            ProfileSectionLabel("APP")
            AppActionRow(Icons.Default.Settings, "Settings",
                "Playback, subtitles, display, home and controls", onSettings)
            Spacer(Modifier.height(20.dp))
        }

        // Developer (debug / demo only)
        if (isDebug() || isDemoServer) {
            item {
                ProfileSectionLabel("DEVELOPER")
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Keep trying demo", color = Color.White, fontSize = 15.sp)
                        Text("Persist demo session across restarts", color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    Switch(
                        checked = sessionState.persistDemo,
                        onCheckedChange = { sessionViewModel.togglePersistDemo(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.18f),
                        ),
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        // Sign out
        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Color(0xFF3A1A1A))
                    .clickable(onClick = onLogout).padding(horizontal = 20.dp, vertical = 14.dp),
            ) {
                Text("Sign Out", color = Color(0xFFFF5252), fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun ProfileHeroCard(userName: String, userInitial: String, serverHost: String, collapseProgress: Float) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), Color.Transparent)))
            .padding(top = 104.dp, bottom = 28.dp)
            .graphicsLayer { alpha = (1f - collapseProgress * 1.2f).coerceIn(0f, 1f); translationY = -20f * collapseProgress },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center) {
                Text(userInitial, color = Color.Black, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
            Text(userName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF4CAF50)))
                Text(serverHost, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ProfileStatsRow(movieCount: Int, tvCount: Int, favoriteCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.05f)).padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileStatItem(movieCount.toString(), "Movies")
        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.12f)))
        ProfileStatItem(tvCount.toString(), "Shows")
        Box(modifier = Modifier.width(1.dp).height(32.dp).background(Color.White.copy(alpha = 0.12f)))
        ProfileStatItem(favoriteCount.toString(), "Favorites")
    }
}

@Composable
private fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp)
    }
}

@Composable
internal fun ProfileSectionLabel(text: String) {
    Text(
        text,
        color = Color.White.copy(alpha = 0.35f),
        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
    )
}
