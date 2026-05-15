package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import org.jellyplus.client.domain.models.Person
import org.jellyplus.client.ui.viewmodels.MovieDetailViewModel

@Composable
fun DesktopMovieDetailScreen(
    item: MediaItem,
    baseUrl: String,
    viewModel: MovieDetailViewModel,
    onBack: () -> Unit,
    onPlay: (MediaItem) -> Unit,
    isFavorite: (MediaItem) -> Boolean,
    onToggleFavorite: (MediaItem) -> Unit,
    isWatchLater: (MediaItem) -> Boolean,
    onToggleWatchLater: (MediaItem) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val fullItem = state.fullItem ?: item

    DesktopHeroDetailScaffold(
        item = fullItem,
        baseUrl = baseUrl,
        primaryLabel = "Watch",
        metadata = fullItem.genres?.take(3)?.joinToString("   ") ?: "Movie",
        onBack = onBack,
        onPrimaryAction = { onPlay(fullItem) },
        isFavorite = isFavorite(fullItem),
        isWatchLater = isWatchLater(fullItem),
        onToggleFavorite = { onToggleFavorite(fullItem) },
        onToggleWatchLater = { onToggleWatchLater(fullItem) },
        overview = fullItem.overview,
    ) {
        if (state.cast.isNotEmpty()) {
            Column {
                Text("Cast", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.cast.take(10)) { person ->
                        CastCard(person, baseUrl)
                    }
                }
            }
        }
    }
}

@Composable
private fun CastCard(person: Person, baseUrl: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(92.dp)) {
        AsyncImage(
            model = person.getImageUrl(baseUrl),
            contentDescription = person.name,
            modifier = Modifier.size(74.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            person.name,
            color = Color.White.copy(alpha = 0.78f),
            fontSize = 12.sp,
            maxLines = 1,
        )
    }
}
