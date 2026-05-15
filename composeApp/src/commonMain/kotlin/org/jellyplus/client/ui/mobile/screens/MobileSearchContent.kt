package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.ui.viewmodels.SearchViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SearchContent(paddingValues: PaddingValues, onMediaClick: (MediaItem) -> Unit) {
    val searchViewModel: SearchViewModel = koinViewModel()
    val state by searchViewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) { try { focusRequester.requestFocus() } catch (_: Exception) {} }
    LaunchedEffect(state.results) { if (state.results.isNotEmpty()) keyboardController?.hide() }

    Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
        // Search bar
        TextField(
            value = state.query,
            onValueChange = { searchViewModel.onQueryChange(it) },
            placeholder = { Text("Movies, shows, people...", color = Color.White.copy(alpha = 0.4f)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).focusRequester(focusRequester),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.08f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.5f)) },
            trailingIcon = {
                if (state.query.isNotBlank()) {
                    IconButton(onClick = { searchViewModel.clearQuery() }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                    }
                }
            },
        )

        // Type filter chips
        if (state.query.isNotBlank()) {
            val filters = listOf(null to "All", MediaType.MOVIE to "Movies", MediaType.SERIES to "TV Shows", MediaType.EPISODE to "Episodes")
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                filters.forEach { (type, label) ->
                    val selected = state.selectedFilter == type
                    FilterChip(
                        selected = selected, onClick = { searchViewModel.onFilterChange(type) },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = Color.Black,
                            containerColor = Color.White.copy(alpha = 0.07f), labelColor = Color.White.copy(alpha = 0.75f),
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected,
                            selectedBorderColor = Color.Transparent, borderColor = Color.White.copy(alpha = 0.15f)),
                    )
                }
            }
        }

        when {
            state.query.isBlank() -> SearchEmptyOrHistory(state.searchHistory, searchViewModel)
            state.isLoading -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            state.error != null -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Search failed", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
            }
            state.displayResults.isEmpty() -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("No results for", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                    Text("\"${state.query}\"", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            else -> SearchResults(state.displayResults, state.selectedFilter, state.baseUrl, onMediaClick)
        }
    }
}

@Composable
private fun SearchEmptyOrHistory(searchHistory: List<String>, searchViewModel: SearchViewModel) {
    if (searchHistory.isNotEmpty()) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent searches", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { searchViewModel.clearHistory() }) {
                Text("Clear all", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items(searchHistory) { query ->
                SearchHistoryRow(query, onClick = { searchViewModel.onQueryChange(query) },
                    onRemove = { searchViewModel.removeHistoryItem(query) })
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.12f), modifier = Modifier.size(72.dp))
                Text("Search movies, shows & more", color = Color.White.copy(alpha = 0.3f), fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<MediaItem>,
    activeFilter: MediaType?,
    baseUrl: String,
    onMediaClick: (MediaItem) -> Unit,
) {
    Text("${results.size} result${if (results.size != 1) "s" else ""}",
        color = Color.White.copy(alpha = 0.38f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))

    if (activeFilter != null) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(results) { item -> SearchResultRow(item, baseUrl) { onMediaClick(item) } }
        }
        return
    }

    val byType = mapOf(
        "Movies" to results.filter { it.type == MediaType.MOVIE },
        "TV Shows" to results.filter { it.type == MediaType.SERIES },
        "Episodes" to results.filter { it.type == MediaType.EPISODE },
        "Other" to results.filter { it.type != MediaType.MOVIE && it.type != MediaType.SERIES && it.type != MediaType.EPISODE },
    )
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        byType.forEach { (label, group) ->
            if (group.isNotEmpty()) {
                item {
                    Text("$label (${group.size})", color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
                }
                items(group) { item -> SearchResultRow(item, baseUrl) { onMediaClick(item) } }
            }
        }
    }
}

@Composable
private fun SearchHistoryRow(query: String, onClick: () -> Unit, onRemove: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick)
        .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.History, null, tint = Color.White.copy(alpha = 0.35f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(query, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        IconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White.copy(alpha = 0.28f), modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SearchResultRow(item: MediaItem, baseUrl: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(72.dp).height(108.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.06f))) {
            AsyncImage(model = item.getImageUrl(baseUrl), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (item.isPlayed) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(8.dp).background(Color(0xFF4CAF50), CircleShape))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(item.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val meta = buildString {
                append(when (item.type) { MediaType.MOVIE -> "Movie"; MediaType.SERIES -> "TV Series"; MediaType.EPISODE -> "Episode"; else -> item.type.value })
                item.year?.let { append(" · $it") }
                if (item.type == MediaType.EPISODE) item.seriesName?.let { name ->
                    val s = item.parentIndexNumber?.let { "S${it.toString().padStart(2, '0')}" } ?: ""
                    val e = item.index?.let { "E${it.toString().padStart(2, '0')}" } ?: ""
                    val ep = listOf(s, e).filter { it.isNotBlank() }.joinToString("")
                    if (ep.isNotBlank()) append(" · $name $ep") else append(" · $name")
                }
            }
            Text(meta, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 1)
            item.runTimeTicks?.let { ticks ->
                val min = (ticks / 10_000_000 / 60).toInt()
                if (min > 0) Text(if (min >= 60) "${min / 60}h ${min % 60}m" else "${min}m",
                    color = Color.White.copy(alpha = 0.32f), fontSize = 11.sp)
            }
        }
        item.rating?.let { r ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(start = 8.dp, end = 2.dp)) {
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(13.dp))
                Text(kotlin.math.round(r * 10).toInt().let { "${it / 10}.${it % 10}" },
                    color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
