package org.jellyplus.client.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import org.jellyplus.client.ui.desktop.DesktopContentLeftPadding
import org.jellyplus.client.ui.desktop.DesktopContentRightPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.LocalUiType
import org.jellyplus.client.UiType
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.components.MediaPoster

@Composable
fun MediaListingScreen(
    title: String,
    items: List<MediaItem>,
    baseUrl: String,
    isLoadingMore: Boolean = false,
    hasMore: Boolean = false,
    onLoadMore: () -> Unit = {},
    onBack: () -> Unit,
    onMediaClick: (MediaItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isEmpty()) items
        else items.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }
    val uiType = LocalUiType.current
    val isDesktop = uiType == UiType.Desktop
    val columns = if (isDesktop) 6 else 3
    val gridState = rememberLazyGridState()
    val shouldLoadMore by remember(filteredItems, searchQuery, hasMore, isLoadingMore) {
        derivedStateOf {
            if (searchQuery.isNotEmpty() || !hasMore || isLoadingMore || filteredItems.isEmpty()) {
                false
            } else {
                val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                lastVisibleIndex >= filteredItems.lastIndex - columns
            }
        }
    }
    val horizontalPadding = if (isDesktop) {
        PaddingValues(start = DesktopContentLeftPadding, end = DesktopContentRightPadding)
    } else {
        PaddingValues(horizontal = 16.dp)
    }
    val contentTopPadding = if (isDesktop) 28.dp else 8.dp
    val searchBarMaxWidth = if (isDesktop) 520.dp else androidx.compose.ui.unit.Dp.Unspecified

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1113))
            .statusBarsPadding()
            .padding(horizontalPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = contentTopPadding, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = if (isDesktop) 30.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (searchQuery.isBlank()) {
                        "${items.size} loaded${if (hasMore) " +" else ""}"
                    } else {
                        "${filteredItems.size} result${if (filteredItems.size == 1) "" else "s"}"
                    },
                    color = Color.White.copy(alpha = 0.42f),
                    fontSize = 13.sp,
                )
            }
        }

        Surface(
            color = Color.White.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .then(if (searchBarMaxWidth == androidx.compose.ui.unit.Dp.Unspecified) Modifier.fillMaxWidth() else Modifier.width(searchBarMaxWidth))
                .height(52.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.5f))
                Spacer(Modifier.width(12.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search in $title...", color = Color.White.copy(alpha = 0.3f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF00D4A8)
                    ),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (filteredItems.isEmpty() && !isLoadingMore) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isBlank()) "No items found" else "No results for \"$searchQuery\"",
                    color = Color.White.copy(alpha = 0.48f),
                    fontSize = if (isDesktop) 18.sp else 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(columns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(if (isDesktop) 18.dp else 10.dp),
                verticalArrangement = Arrangement.spacedBy(if (isDesktop) 24.dp else 16.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    MediaPoster(
                        item = item,
                        baseUrl = baseUrl,
                        onClick = { onMediaClick(item) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (isLoadingMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }
}
