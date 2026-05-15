package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import org.jellyplus.client.ui.components.FocusableOutlinedButton
import org.jellyplus.client.ui.desktop.DesktopContentLeftPadding
import org.jellyplus.client.ui.desktop.DesktopContentRightPadding
import org.jellyplus.client.ui.desktop.DesktopSidebarLogoSize
import org.jellyplus.client.ui.desktop.DesktopSidebarTopPadding
import org.jellyplus.client.ui.desktop.DesktopSidebarWidth
import kotlin.math.abs

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
    secondaryLabel: String? = "Trailer",
    overview: String? = item.overview,
    detailContentSpacing: Dp = 34.dp,
    focusScrollBottomClearance: Dp = 0.dp,
    bottomContent: @Composable () -> Unit = {},
) {
    val backFocusRequester = remember { FocusRequester() }
    val playFocusRequester = remember { FocusRequester() }
    val bottomClearancePx = with(LocalDensity.current) { focusScrollBottomClearance.toPx() }

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
            Column(modifier = modifier.widthIn(max = 560.dp)) {
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
                            .focusProperties { up = backFocusRequester },
                        shape = RoundedCornerShape(25.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(primaryLabel, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    if (secondaryLabel != null) {
                        FocusableOutlinedButton(
                            onClick = {},
                            modifier = Modifier.height(50.dp).width(140.dp),
                            shape = RoundedCornerShape(25.dp),
                        ) {
                            Text(secondaryLabel, color = Color.White, fontWeight = FontWeight.Bold)
                        }
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
                        DetailActionIcon(Icons.Default.Share, "Share")
                    }
                }

                overview?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }
        }

        CompositionLocalProvider(
            LocalBringIntoViewSpec provides remember(bottomClearancePx) {
                object : BringIntoViewSpec {
                    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
                        val trailingEdge = offset + size
                        if (offset >= 0f && trailingEdge <= containerSize) return 0f
                        return when {
                            offset >= 0f && trailingEdge <= containerSize -> 0f
                            offset < 0f && trailingEdge > containerSize -> 0f
                            abs(offset) < abs(trailingEdge - containerSize) -> offset
                            else -> trailingEdge - containerSize + bottomClearancePx
                        }
                    }
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = DesktopContentLeftPadding, end = DesktopContentRightPadding, top = 132.dp, bottom = 64.dp),
                verticalArrangement = Arrangement.spacedBy(detailContentSpacing),
            ) {
                item(key = "hero") {
                    HeroContent()
                }

                item(key = "detail-content") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        bottomContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var isBackFocused by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .size(DesktopSidebarLogoSize)
            .onFocusChanged { isBackFocused = it.isFocused }
            .focusable()
            .clickable { onBack() }
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
