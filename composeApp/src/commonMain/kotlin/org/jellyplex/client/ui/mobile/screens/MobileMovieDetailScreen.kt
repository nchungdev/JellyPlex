package org.jellyplex.client.ui.mobile.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import org.jellyplex.client.domain.models.MediaItem
import org.jellyplex.client.ui.viewmodels.MovieDetailViewModel

@Composable
fun MobileMovieDetailScreen(
    item: MediaItem,
    viewModel: MovieDetailViewModel,
    onBack: () -> Unit,
    onPlay: (MediaItem) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val fullItem = state.fullItem ?: item
    val baseUrl = state.baseUrl

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1113))) {
        AsyncImage(
            model = item.getBackdropUrl(baseUrl),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(300.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xFF0F1113)),
                        startY = 100f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.height(180.dp))

            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                AsyncImage(
                    model = item.getImageUrl(baseUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .width(120.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                    Text(
                        fullItem.title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 32.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("${fullItem.year ?: ""} • TV-MA", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFFFFB300), shape = RoundedCornerShape(4.dp)) {
                            val ratingText = fullItem.rating?.let { (it * 10).toInt().toFloat() / 10 }.toString()
                            Text(
                                ratingText,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onPlay(fullItem) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24D366)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Play", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(onClick = {}) { Icon(Icons.Default.Add, null, tint = Color.White) }
                }
            }

            Text(
                "Overview",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                fullItem.overview ?: "No overview available.",
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(16.dp),
                lineHeight = 22.sp
            )

            Text(
                "Cast",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(state.cast) { person ->
                    MobileCastCard(person, baseUrl)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Default.ArrowBack, "Back", tint = Color.White)
        }
    }
}

@Composable
fun MobileCastCard(person: org.jellyplex.client.domain.models.Person, baseUrl: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) {
        AsyncImage(
            model = person.getImageUrl(baseUrl),
            contentDescription = null,
            modifier = Modifier.size(80.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            person.name,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 2
        )
    }
}
