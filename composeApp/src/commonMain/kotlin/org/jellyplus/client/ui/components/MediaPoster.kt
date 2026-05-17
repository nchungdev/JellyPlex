package org.jellyplus.client.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.domain.models.MediaType


private enum class PosterImageState {
    Loading,
    Loaded,
    Failed,
}

@Composable
fun MediaPoster(
    item: MediaItem,
    baseUrl: String,
    onClick: () -> Unit,
    onFocus: () -> Unit = {},
    aspectRatio: Float = 2f / 3f,
    @Suppress("UNUSED_PARAMETER") focusGutter: Dp = 0.dp,
    showLabel: Boolean = false,
    progress: Float? = null,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusedScale = if (aspectRatio > 1f) 7f / 6f else 1.2f
    val scale by animateFloatAsState(if (isFocused) focusedScale else 1.0f)
    val interactionSource = remember { MutableInteractionSource() }
    val imageUrl = item.getImageUrl(baseUrl)
    var imageState by remember(imageUrl) {
        mutableStateOf(if (imageUrl.isNullOrBlank()) PosterImageState.Failed else PosterImageState.Loading)
    }

    val shape = RoundedCornerShape(8.dp)

    Surface(
        onClick = onClick,
        shape = shape,
        color = Color.Transparent,
        modifier = modifier
            .aspectRatio(aspectRatio)
            .onFocusChanged {
                isFocused = it.isFocused
                if (it.isFocused) onFocus()
            }
            .zIndex(if (isFocused) 10f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0.5f)
            },
        interactionSource = interactionSource
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.05f))) {
            when (imageState) {
                PosterImageState.Loading -> MediaPosterPlaceholder(Modifier.fillMaxSize())
                PosterImageState.Failed -> MediaPosterFallback(Modifier.fillMaxSize())
                PosterImageState.Loaded -> Unit
            }

            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onLoading = { imageState = PosterImageState.Loading },
                    onSuccess = { imageState = PosterImageState.Loaded },
                    onError = { imageState = PosterImageState.Failed },
                )
            }

            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.36f)),
                            )
                        )
                )
            }

            progress?.takeIf { it > 0f }?.let { p ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.28f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(p.coerceIn(0f, 1f))
                            .fillMaxSize()
                            .background(Color(0xFF00D4A8)),
                    )
                }
            }

            if (showLabel) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.00f to Color.Transparent,
                                    0.54f to Color.Transparent,
                                    1.00f to Color.Black.copy(alpha = 0.82f),
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    mediaPosterSubtitle(item)?.let { subtitle ->
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

private fun mediaPosterSubtitle(item: MediaItem): String? {
    val subtitle = when (item.type) {
        MediaType.EPISODE -> buildString {
            item.seriesName?.let { append(it) }
            val season = item.parentIndexNumber?.let { "S${it.toString().padStart(2, '0')}" }
            val episode = item.index?.let { "E${it.toString().padStart(2, '0')}" }
            val episodeLabel = listOfNotNull(season, episode).joinToString(" ")
            if (episodeLabel.isNotBlank()) {
                if (isNotEmpty()) append(" · ")
                append(episodeLabel)
            }
        }
        else -> buildString {
            item.year?.let { append(it) }
            item.runTimeTicks?.let { ticks ->
                val minutes = ticks / 10_000_000 / 60
                if (minutes > 0) {
                    if (isNotEmpty()) append(" · ")
                    append("${minutes / 60}h ${minutes % 60}m")
                }
            }
        }
    }
    return subtitle.takeIf { it.isNotBlank() }
}
