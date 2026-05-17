package org.jellyplus.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import org.jellyplus.client.ui.navigation.RequestInitialFocus
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
import org.jellyplus.client.ui.desktop.DesktopContentLeftPadding
import org.jellyplus.client.ui.desktop.DesktopContentRightPadding

// ── Filter model ─────────────────────────────────────────────────────────────

enum class ListingSortOption(val label: String) {
    NAME_ASC("A → Z"),
    NAME_DESC("Z → A"),
    YEAR_DESC("Newest"),
    YEAR_ASC("Oldest"),
    RATING_DESC("Top Rated"),
}

data class ListingFilter(
    val sortBy: ListingSortOption = ListingSortOption.NAME_ASC,
    val selectedYears: Set<Int> = emptySet(),
    val selectedGenres: Set<String> = emptySet(),
    val minRating: Float? = null,
) {
    val isActive: Boolean
        get() = sortBy != ListingSortOption.NAME_ASC ||
            selectedYears.isNotEmpty() ||
            selectedGenres.isNotEmpty() ||
            minRating != null
}

// ── Main composable ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MediaListingScreen(
    title: String,
    items: List<MediaItem>,
    baseUrl: String,
    isLoadingMore: Boolean = false,
    hasMore: Boolean = false,
    /** Pass false when the screen was opened from a genre chip (genre filter irrelevant). */
    showGenreFilter: Boolean = true,
    onLoadMore: () -> Unit = {},
    onBack: () -> Unit,
    onMediaClick: (MediaItem) -> Unit,
    /** Navigate to the global search screen. */
    onNavigateToSearch: () -> Unit = {},
) {
    var filter by remember { mutableStateOf(ListingFilter()) }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Available options derived from currently-loaded items
    val availableYears = remember(items) {
        items.mapNotNull { it.year }.distinct().sortedDescending()
    }
    val availableGenres = remember(items) {
        items.flatMap { it.genres ?: emptyList() }.distinct().sorted()
    }

    // Client-side filter + sort
    val filteredItems = remember(items, filter) {
        var result = items
        if (filter.selectedGenres.isNotEmpty()) {
            result = result.filter { item ->
                item.genres?.any { it in filter.selectedGenres } == true
            }
        }
        if (filter.selectedYears.isNotEmpty()) {
            result = result.filter { item -> item.year in filter.selectedYears }
        }
        filter.minRating?.let { min ->
            result = result.filter { (it.rating ?: 0f) >= min }
        }
        result = when (filter.sortBy) {
            ListingSortOption.NAME_ASC -> result.sortedBy { it.title }
            ListingSortOption.NAME_DESC -> result.sortedByDescending { it.title }
            ListingSortOption.YEAR_DESC -> result.sortedByDescending { it.year ?: 0 }
            ListingSortOption.YEAR_ASC -> result.sortedBy { it.year ?: Int.MAX_VALUE }
            ListingSortOption.RATING_DESC -> result.sortedByDescending { it.rating ?: 0f }
        }
        result
    }

    val uiType = LocalUiType.current
    val isDesktop = uiType == UiType.Desktop
    val columns = if (isDesktop) 6 else 3
    val gridState = rememberLazyGridState()

    // Infinite-scroll trigger — disabled while a client filter is active
    val shouldLoadMore by remember(filteredItems, hasMore, isLoadingMore, filter) {
        derivedStateOf {
            if (filter.isActive || !hasMore || isLoadingMore || filteredItems.isEmpty()) false
            else {
                val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                last >= filteredItems.lastIndex - columns
            }
        }
    }

    val horizontalPadding = if (isDesktop) {
        PaddingValues(start = DesktopContentLeftPadding, end = DesktopContentRightPadding)
    } else {
        PaddingValues(horizontal = 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF181818))
            .statusBarsPadding()
            .padding(horizontalPadding),
    ) {
        // ── Header row ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (isDesktop) 28.dp else 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val backFocus = remember { FocusRequester() }
            if (filteredItems.isEmpty() && !isLoadingMore) {
                RequestInitialFocus(backFocus, filteredItems.isEmpty())
            }
            IconButton(onClick = onBack, modifier = Modifier.focusRequester(backFocus)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = if (isDesktop) 28.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val countText = buildString {
                    append("${filteredItems.size}")
                    if (filter.isActive && filteredItems.size != items.size) append(" of ${items.size}")
                    append(" title${if (filteredItems.size != 1) "s" else ""}")
                    if (hasMore && !filter.isActive) append(" +")
                }
                Text(countText, color = Color.White.copy(alpha = 0.38f), fontSize = 12.sp)
            }
            // Search → open global search
            IconButton(onClick = onNavigateToSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.8f))
            }
            // Filter / Sort button
            Box {
                IconButton(onClick = { showFilterSheet = true }) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Filter & Sort",
                        tint = if (filter.isActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.8f),
                    )
                }
                if (filter.isActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            .align(Alignment.TopEnd),
                    )
                }
            }
        }

        // ── Active filter chips ───────────────────────────────────────────
        val activeChips: List<Pair<String, () -> Unit>> = buildList {
            if (filter.sortBy != ListingSortOption.NAME_ASC) {
                add("↕ ${filter.sortBy.label}" to { filter = filter.copy(sortBy = ListingSortOption.NAME_ASC) })
            }
            filter.selectedYears.sorted().forEach { y ->
                add(y.toString() to { filter = filter.copy(selectedYears = filter.selectedYears - y) })
            }
            filter.selectedGenres.sorted().forEach { g ->
                add(g to { filter = filter.copy(selectedGenres = filter.selectedGenres - g) })
            }
            filter.minRating?.let { r ->
                add("${r.toInt()}+ ★" to { filter = filter.copy(minRating = null) })
            }
        }

        if (activeChips.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
            ) {
                item {
                    SuggestionChip(
                        onClick = { filter = ListingFilter() },
                        label = { Text("Clear all", fontSize = 12.sp) },
                        icon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color.White.copy(alpha = 0.1f),
                            labelColor = Color.White,
                            iconContentColor = Color.White,
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color.Transparent,
                        ),
                    )
                }
                items(activeChips) { (label, onRemove) ->
                    SuggestionChip(
                        onClick = onRemove,
                        label = { Text(label, fontSize = 12.sp) },
                        icon = {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp))
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            labelColor = MaterialTheme.colorScheme.primary,
                            iconContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color.Transparent,
                        ),
                    )
                }
            }
        }

        // ── Grid ─────────────────────────────────────────────────────────
        if (filteredItems.isEmpty() && !isLoadingMore) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (!filter.isActive) "No items found" else "No matches for current filters",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = if (isDesktop) 18.sp else 15.sp,
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        bottom = (if (hasMore && !filter.isActive) 96.dp else 32.dp) +
                            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                    horizontalArrangement = Arrangement.spacedBy(if (isDesktop) 18.dp else 10.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isDesktop) 24.dp else 16.dp),
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        MediaPoster(
                            item = item,
                            baseUrl = baseUrl,
                            onClick = { onMediaClick(item) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                if (isLoadingMore) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                            .size(44.dp)
                            .background(Color(0xFF181818).copy(alpha = 0.88f), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }

    // ── Filter bottom sheet ───────────────────────────────────────────────
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF1A1D21),
            tonalElevation = 0.dp,
        ) {
            FilterSheetContent(
                filter = filter,
                availableYears = availableYears,
                availableGenres = availableGenres,
                showGenreFilter = showGenreFilter,
                onApply = {
                    filter = it
                    showFilterSheet = false
                },
                onClear = {
                    filter = ListingFilter()
                    showFilterSheet = false
                },
            )
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }
}

// ── Filter bottom-sheet content ───────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSheetContent(
    filter: ListingFilter,
    availableYears: List<Int>,
    availableGenres: List<String>,
    showGenreFilter: Boolean,
    onApply: (ListingFilter) -> Unit,
    onClear: () -> Unit,
) {
    var draft by remember(filter) { mutableStateOf(filter) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.88f),
    ) {
        // ── Fixed header ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Filter & Sort",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // ── Scrollable filter options ─────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            // ── Sort By ──────────────────────────────────────────────────
            FilterSectionLabel("Sort By")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ListingSortOption.values().forEach { option ->
                    val sel = draft.sortBy == option
                    FilterOptionChip(
                        label = option.label,
                        selected = sel,
                        onClick = { draft = draft.copy(sortBy = option) },
                    )
                }
            }

            // ── Year ─────────────────────────────────────────────────────
            if (availableYears.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                FilterSectionLabel("Year")
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    availableYears.take(20).forEach { year ->
                        val sel = year in draft.selectedYears
                        FilterOptionChip(
                            label = year.toString(),
                            selected = sel,
                            onClick = {
                                draft = draft.copy(
                                    selectedYears = if (sel) draft.selectedYears - year
                                    else draft.selectedYears + year,
                                )
                            },
                        )
                    }
                }
            }

            // ── Genre ─────────────────────────────────────────────────────
            if (showGenreFilter && availableGenres.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                FilterSectionLabel("Genre")
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    availableGenres.forEach { genre ->
                        val sel = genre in draft.selectedGenres
                        FilterOptionChip(
                            label = genre,
                            selected = sel,
                            onClick = {
                                draft = draft.copy(
                                    selectedGenres = if (sel) draft.selectedGenres - genre
                                    else draft.selectedGenres + genre,
                                )
                            },
                        )
                    }
                }
            }

            // ── Min Rating ───────────────────────────────────────────────
            Spacer(Modifier.height(22.dp))
            FilterSectionLabel("Min Rating")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(null to "Any", 6f to "6+ ★", 7f to "7+ ★", 8f to "8+ ★", 9f to "9+ ★").forEach { (rating, label) ->
                    val sel = draft.minRating == rating
                    FilterOptionChip(
                        label = label,
                        selected = sel,
                        onClick = { draft = draft.copy(minRating = rating) },
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        // ── Fixed bottom action buttons ───────────────────────────────────
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.weight(1f).height(48.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Clear All", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = { onApply(draft) },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Apply", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(text, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
}

@Composable
private fun FilterOptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.Black,
            containerColor = Color.White.copy(alpha = 0.07f),
            labelColor = Color.White.copy(alpha = 0.78f),
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            selectedBorderColor = Color.Transparent,
            borderColor = Color.White.copy(alpha = 0.13f),
        ),
    )
}
