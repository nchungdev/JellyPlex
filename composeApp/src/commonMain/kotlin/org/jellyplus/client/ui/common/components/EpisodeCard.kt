package org.jellyplus.client.ui.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem

private val FocusColor = Color(0xFFFFB300)

@Composable
fun EpisodeCard(
    episode: MediaItem,
    baseUrl: String,
    cardWidth: androidx.compose.ui.unit.Dp,
    onPlay: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .width(cardWidth)
                .clip(RoundedCornerShape(12.dp))
                .onFocusChanged { isFocused = it.isFocused }
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = if (isFocused) FocusColor else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable(onClick = onPlay)
                .focusable(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1C22)),
        ) {
            episode.getImageUrl(baseUrl)?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = episode.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(if (isFocused) Color.Black.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(36.dp)
                            .background(if (isFocused) FocusColor else Color.Black.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        null,
                        tint = if (isFocused) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Episode ${episode.index ?: 0}: ${episode.title}",
            color = if (isFocused) FocusColor else Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
