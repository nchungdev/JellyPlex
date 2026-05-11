package org.jellyplex.client.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplex.client.domain.models.MediaItem

@Composable
fun MediaRow(
    title: String,
    items: List<MediaItem>,
    baseUrl: String,
    onItemClick: (MediaItem) -> Unit,
    emptyText: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                Text(
                    text = actionLabel,
                    color = Color(0xFFFFB300),
                    fontSize = 14.sp,
                    modifier = Modifier.clickable(onClick = onActionClick),
                )
            }
        }

        if (items.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                items(items) { item ->
                    MediaPoster(
                        item = item,
                        baseUrl = baseUrl,
                        onClick = { onItemClick(item) },
                        modifier = Modifier.width(150.dp)
                    )
                }
            }
        } else if (!emptyText.isNullOrBlank()) {
            Text(
                text = emptyText,
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }
}
