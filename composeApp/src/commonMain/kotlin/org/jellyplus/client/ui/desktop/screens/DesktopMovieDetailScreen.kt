package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.components.DetailActionIcon
import org.jellyplus.client.ui.components.FocusableButton
import org.jellyplus.client.ui.components.FocusableOutlinedButton
import org.jellyplus.client.ui.viewmodels.MovieDetailViewModel

@Composable
fun DesktopMovieDetailScreen(
    item: MediaItem,
    baseUrl: String,
    viewModel: MovieDetailViewModel,
    onBack: () -> Unit,
    onPlay: (MediaItem) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val fullItem = state.fullItem ?: item
    val playFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        try {
            playFocusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1113))) {
        // Immersive Backdrop
        AsyncImage(
            model = fullItem.getBackdropUrl(baseUrl),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(500.dp),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF0F1113)),
                        startY = 150f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(280.dp))

            // Info Section
            Column(modifier = Modifier.padding(horizontal = 64.dp)) {
                Text(
                    text = fullItem.title,
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 64.sp,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Action, Thriller", color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
                    Text(
                        "  •  TV-MA",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        "  •  ${fullItem.year}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        "  •  1 hr",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFFFB300), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "8.0",
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("93%", color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Play Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FocusableButton(
                        onClick = { onPlay(fullItem) },
                        modifier = Modifier
                            .height(56.dp)
                            .width(160.dp)
                            .focusRequester(playFocusRequester),
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("Play", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    FocusableOutlinedButton(
                        onClick = { },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Watch Trailer", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailActionIcon(Icons.Default.Add, "Add to list")
                        DetailActionIcon(Icons.Default.FavoriteBorder, "Favorite")
                        DetailActionIcon(Icons.Default.Share, "Share")
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text("Overview", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = fullItem.overview ?: "No overview available.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    modifier = Modifier.fillMaxWidth(0.75f)
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Cast Section
                if (state.cast.isNotEmpty()) {
                    Text("Cast", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        items(state.cast.take(12)) { person ->
                            CastCard(person, baseUrl)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Floating Back Button
        var isBackFocused by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .padding(32.dp)
                .size(48.dp)
                .onFocusChanged { isBackFocused = it.isFocused }
                .focusable()
                .clickable { onBack() }
                .background(
                    if (isBackFocused) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.5f),
                    CircleShape
                )
                .border(
                    width = 2.dp,
                    color = if (isBackFocused) Color.White else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                "Back",
                tint = if (isBackFocused) Color.Black else Color.White
            )
        }
    }
}


@Composable
private fun CastCard(person: org.jellyplus.client.domain.models.Person, baseUrl: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(120.dp)) {
        AsyncImage(
            model = person.getImageUrl(baseUrl),
            contentDescription = person.name,
            modifier = Modifier.size(120.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            person.name,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            person.role ?: "",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
