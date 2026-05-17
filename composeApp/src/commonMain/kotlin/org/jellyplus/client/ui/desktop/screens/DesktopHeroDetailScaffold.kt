package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import org.jellyplus.client.ui.components.MediaPoster
import org.jellyplus.client.ui.navigation.RequestInitialFocus
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.launch
import org.jellyplus.client.logDebug
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.components.DetailActionIcon
import org.jellyplus.client.ui.components.FocusableButton
import org.jellyplus.client.ui.desktop.DesktopContentLeftPadding
import org.jellyplus.client.ui.desktop.DesktopContentRightPadding
import org.jellyplus.client.ui.desktop.DesktopSidebarLogoSize
import org.jellyplus.client.ui.desktop.DesktopSidebarTopPadding
import org.jellyplus.client.ui.desktop.DesktopSidebarWidth

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun DesktopHeroDetailScaffold(
    item: MediaItem,
    baseUrl: String,
    primaryLabel: String,
    metadata: String,
    onBack: () -> Unit,
    onPrimaryAction: () -> Unit,
    isFavorite: Boolean = item.userData?.isFavorite == true,
    isWatchLater: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onToggleWatchLater: () -> Unit = {},
    modifier: Modifier = Modifier,
    overview: String? = item.overview,
    detailContentSpacing: Dp = 34.dp,
    focusScrollBottomClearance: Dp = 0.dp,
    bottomContent: @Composable (backFocusRequester: FocusRequester) -> Unit = {},
) {
    val backFocusRequester = remember { FocusRequester() }
    val playFocusRequester = remember { FocusRequester() }
    var showOverviewDialog by remember { mutableStateOf(false) }
    val bottomClearancePx = with(LocalDensity.current) { focusScrollBottomClearance.toPx() }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scrollScope = androidx.compose.runtime.rememberCoroutineScope()
    // When a hero element (Back / Play / actions / Xem thêm) is focused, the
    // top info (title + metadata) must be visible — scroll the column fully
    // back to the top so the user always sees what they're acting on.
    val scrollToTop: () -> Unit = {
        scrollScope.launch { listState.animateScrollToItem(0) }
    }

    LaunchedEffect(item.id) {
        kotlinx.coroutines.delay(120)
        try { playFocusRequester.requestFocus() } catch (_: IllegalStateException) {}
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF181818))) {
        AsyncImage(
            model = item.getBackdropUrl(baseUrl) ?: item.getImageUrl(baseUrl),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.36f))
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(alpha = 0.94f),
                            0.34f to Color.Black.copy(alpha = 0.76f),
                            0.72f to Color.Black.copy(alpha = 0.32f),
                            1.00f to Color.Black.copy(alpha = 0.54f),
                        )
                    )
                )
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Black.copy(alpha = 0.22f),
                            0.54f to Color.Transparent,
                            1.00f to Color(0xFF181818).copy(alpha = 0.96f),
                        )
                    )
                )
        )

        HeroBackButton(
            onBack = onBack,
            onFocused = scrollToTop,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(
                    start = (DesktopSidebarWidth - DesktopSidebarLogoSize) / 2,
                    top = DesktopSidebarTopPadding,
                )
                .focusRequester(backFocusRequester)
                .focusProperties { down = playFocusRequester },
        )

        @Composable
        fun HeroContent(modifier: Modifier = Modifier) {
            Column(
                modifier = modifier
                    .widthIn(max = 560.dp)
                    .onFocusChanged { if (it.hasFocus) scrollToTop() },
            ) {
                if (metadata.isNotBlank()) {
                    Text(
                        metadata,
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(10.dp))
                }

                Text(
                    item.title,
                    color = Color.White,
                    fontSize = 44.sp,
                    lineHeight = 50.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(color = Color(0xFFFFB300), shape = RoundedCornerShape(4.dp)) {
                        Text("IMDb", color = Color.Black, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    item.rating?.let { Text(it.toString(), color = Color.White.copy(alpha = 0.78f), fontSize = 14.sp) }
                    item.year?.let { Text(it.toString(), color = Color.White.copy(alpha = 0.78f), fontSize = 14.sp) }
                    item.runTimeTicks?.let { ticks ->
                        val minutes = ticks / 10_000_000 / 60
                        if (minutes > 0) Text("${minutes / 60}h ${minutes % 60}m", color = Color.White.copy(alpha = 0.78f), fontSize = 14.sp)
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 26.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    FocusableButton(
                        onClick = onPrimaryAction,
                        modifier = Modifier
                            .height(50.dp)
                            .width(150.dp)
                            .focusRequester(playFocusRequester)
                            .focusProperties { up = backFocusRequester }
                            .onFocusChanged { if (it.isFocused) logDebug("JellyDpad", "FOCUS GAINED detail=PlayButton") }
                            .onKeyEvent { e ->
                                if (e.type == KeyEventType.KeyDown) {
                                    logDebug("JellyDpad", "KEY ${e.key} @ detail=PlayButton")
                                }
                                false
                            },
                        shape = RoundedCornerShape(25.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(primaryLabel, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        DetailActionIcon(
                            Icons.Default.Add,
                            "Watch Later",
                            selected = isWatchLater,
                            onClick = onToggleWatchLater,
                        )
                        DetailActionIcon(
                            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Favorite",
                            selected = isFavorite,
                            onClick = onToggleFavorite,
                        )
                    }
                }

                overview?.takeIf { it.isNotBlank() }?.let { ov ->
                    Text(
                        text = ov,
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 18.dp),
                    )
                    var moreFocused by remember { mutableStateOf(false) }
                    Text(
                        text = "Xem thêm",
                        color = if (moreFocused) Color.White else MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showOverviewDialog = true }
                            .onFocusChanged {
                                moreFocused = it.isFocused
                                if (it.isFocused) logDebug("JellyDpad", "FOCUS GAINED detail=XemThem")
                            }
                    )
                }
            }
        }

        CompositionLocalProvider(
            LocalBringIntoViewSpec provides remember(bottomClearancePx) {
                object : BringIntoViewSpec {
                    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
                        // The visible region must keep [bottomClearancePx] free at
                        // the bottom (and reveal the top fully). The previous
                        // impl treated an element flush at containerSize as
                        // "visible" (clearance ignored) → no scroll → cut off.
                        val visibleBottom = containerSize - bottomClearancePx
                        val trailingEdge = offset + size
                        return when {
                            // Already inside the padded visible region.
                            offset >= 0f && trailingEdge <= visibleBottom -> 0f
                            // Below the safe region → scroll up so the bottom of
                            // the element sits [bottomClearancePx] above the edge.
                            trailingEdge > visibleBottom -> trailingEdge - visibleBottom
                            // Above the top → scroll down to reveal it fully.
                            offset < 0f -> offset
                            else -> 0f
                        }
                    }
                }
            }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                // No right padding: section rows (Similar / episodes) bleed to
                // the screen edge like a TV carousel. Hero text is width-capped
                // and left-aligned, so it's unaffected.
                contentPadding = PaddingValues(start = DesktopContentLeftPadding, end = 0.dp, top = 64.dp, bottom = 64.dp),
                verticalArrangement = Arrangement.spacedBy(detailContentSpacing),
            ) {
                item(key = "hero") {
                    HeroContent()
                }

                item(key = "detail-content") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        bottomContent(backFocusRequester)
                    }
                }
            }
        }

        if (showOverviewDialog) {
            OverviewDialog(
                title = item.title,
                overview = overview.orEmpty(),
                onDismiss = { showOverviewDialog = false },
            )
        }
    }
}

@Composable
private fun OverviewDialog(title: String, overview: String, onDismiss: () -> Unit) {
    org.jellyplus.client.AppBackHandler(enabled = true, onBack = onDismiss)
    val closeFocus = remember { FocusRequester() }
    RequestInitialFocus(closeFocus)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = Color(0xFF1B1B1B),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 720.dp).padding(48.dp),
        ) {
            Column(modifier = Modifier.padding(28.dp)) {
                Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text(
                    overview,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                )
                Spacer(Modifier.height(20.dp))
                FocusableButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(46.dp).width(130.dp).focusRequester(closeFocus),
                    shape = RoundedCornerShape(23.dp),
                ) {
                    Text("Đóng", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun DesktopSimilarSection(
    items: List<MediaItem>,
    baseUrl: String,
    onClick: (MediaItem) -> Unit,
    backFocusRequester: FocusRequester? = null,
) {
    if (items.isEmpty()) return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Similar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            // 32dp after the last item so the last focused poster is never
            // flush against the right screen edge (row still bleeds while
            // scrolling middle items).
            contentPadding = PaddingValues(top = 4.dp, bottom = 22.dp, end = 32.dp),
        ) {
            itemsIndexed(items, key = { _, it -> it.id }) { index, related ->
                MediaPoster(
                    item = related,
                    baseUrl = baseUrl,
                    onClick = { onClick(related) },
                    onFocus = { logDebug("JellyDpad", "FOCUS GAINED detail=Similar item=$index") },
                    modifier = Modifier
                        .width(120.dp)
                        // Standard TV rule: Left on the first item jumps to the
                        // Back button.
                        .then(
                            if (index == 0 && backFocusRequester != null)
                                Modifier.focusProperties { left = backFocusRequester }
                            else Modifier
                        )
                        // Similar is the last section in detail: consume Down so
                        // focus never escapes the content to nothing, and
                        // consume Right at the last item for the same reason.
                        .onKeyEvent { e ->
                            if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                            logDebug("JellyDpad", "KEY ${e.key} @ detail=Similar item=$index/${items.size}")
                            when (e.key) {
                                Key.DirectionDown -> true
                                Key.DirectionRight -> index == items.lastIndex
                                else -> false
                            }
                        },
                )
            }
        }
    }
}

@Composable
private fun HeroBackButton(onBack: () -> Unit, onFocused: () -> Unit = {}, modifier: Modifier = Modifier) {
    var isBackFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(DesktopSidebarLogoSize)
            .clickable { onBack() }
            .onFocusChanged {
                isBackFocused = it.isFocused
                if (it.isFocused) {
                    logDebug("JellyDpad", "FOCUS GAINED detail=BackButton")
                    onFocused()
                }
            }
            .background(
                if (isBackFocused) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.44f),
                CircleShape,
            )
            .border(2.dp, if (isBackFocused) Color.White else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = if (isBackFocused) Color.Black else Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}
