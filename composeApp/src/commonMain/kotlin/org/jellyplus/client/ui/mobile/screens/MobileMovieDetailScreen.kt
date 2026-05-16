package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.components.MediaPoster
import org.jellyplus.client.ui.viewmodels.MovieDetailViewModel

@Composable
fun MobileMovieDetailScreen(
    item: MediaItem,
    viewModel: MovieDetailViewModel,
    recommendedItems: List<MediaItem> = emptyList(),
    onBack: () -> Unit,
    onPlay: (MediaItem) -> Unit,
    onRecommendedClick: (MediaItem) -> Unit = {},
    isFavorite: (MediaItem) -> Boolean,
    onToggleFavorite: (MediaItem) -> Unit,
    isWatchLater: (MediaItem) -> Boolean,
    onToggleWatchLater: (MediaItem) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val fullItem = state.fullItem ?: item
    val baseUrl = state.baseUrl
    var overviewExpanded by remember { mutableStateOf(false) }
    val favorite = isFavorite(fullItem)
    val watchLater = isWatchLater(fullItem)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF181818))
            .verticalScroll(rememberScrollState())
    ) {
        // ── Backdrop + overlaid info ─────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            AsyncImage(
                model = item.getBackdropUrl(baseUrl) ?: item.getImageUrl(baseUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Top gradient — status bar readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.72f), Color.Transparent)
                        )
                    )
            )
            // Bottom scrim — behind info overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF181818))
                        )
                    )
            )
            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            // Info overlay at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = fullItem.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    fullItem.year?.let {
                        Text("$it", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        MetaDot()
                    }
                    fullItem.genres?.firstOrNull()?.let {
                        Text(it, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        MetaDot()
                    }
                    Text(
                        "HD",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                    fullItem.rating?.let { rating ->
                        MetaDot()
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(13.dp))
                        Text(
                            "${(rating * 10).toInt() / 10f}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    DetailActionButton(
                        icon = Icons.Default.Add,
                        label = "Later",
                        tint = if (watchLater) MaterialTheme.colorScheme.primary else Color.White,
                        onClick = { onToggleWatchLater(fullItem) }
                    )
                    DetailActionButton(
                        icon = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        label = "Favorite",
                        tint = if (favorite) MaterialTheme.colorScheme.primary else Color.White,
                        onClick = { onToggleFavorite(fullItem) }
                    )
                    DetailActionButton(Icons.Default.Download, "Download")
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { onPlay(fullItem) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Play", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // ── Overview ────────────────────────────────────────────────────────
        if (!fullItem.overview.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = fullItem.overview,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                maxLines = if (overviewExpanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = if (overviewExpanded) "Less" else "More",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 16.dp, top = 2.dp)
                    .clickable { overviewExpanded = !overviewExpanded }
            )
        }

        // ── Cast ─────────────────────────────────────────────────────────────
        if (state.cast.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            DetailSectionHeader("Cast")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.cast) { person ->
                    MobileCastCard(person, baseUrl)
                }
            }
        }

        if (recommendedItems.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            DetailSectionHeader("Similar")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recommendedItems) { related ->
                    MediaPoster(
                        item = related,
                        baseUrl = baseUrl,
                        onClick = { onRecommendedClick(related) },
                        modifier = Modifier.width(118.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ── Shared detail-screen helpers ─────────────────────────────────────────────

@Composable
private fun MetaDot() {
    Text("•", color = Color.White.copy(alpha = 0.35f), fontSize = 13.sp)
}

@Composable
internal fun DetailActionButton(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit = {},
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(26.dp))
        }
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
    }
}

@Composable
fun DetailSectionHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    )
}

@Composable
fun MobileCastCard(person: org.jellyplus.client.domain.models.Person, baseUrl: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f))
        ) {
            AsyncImage(
                model = person.getImageUrl(baseUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            person.name,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2,
            lineHeight = 15.sp
        )
        person.role?.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 10.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
