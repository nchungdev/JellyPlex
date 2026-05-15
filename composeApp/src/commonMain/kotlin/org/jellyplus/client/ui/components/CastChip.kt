package org.jellyplus.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.jellyplus.client.domain.models.Person

private val FocusColor = Color(0xFF00D4A8)

@Composable
fun CastChip(
    person: Person,
    baseUrl: String,
    isCompact: Boolean,
) {
    var isFocused by remember { mutableStateOf(false) }
    val chipShape = RoundedCornerShape(24.dp)

    Row(
        modifier =
            Modifier
                .clip(chipShape)
                .onFocusChanged { isFocused = it.isFocused }
                .background(
                    if (isFocused) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f),
                    chipShape,
                )
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = if (isFocused) FocusColor else Color.Transparent,
                    shape = chipShape,
                )
                .focusable()
                .padding(end = if (isCompact) 12.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = person.getImageUrl(baseUrl),
            contentDescription = person.name,
            modifier =
                Modifier
                    .size(if (isCompact) 36.dp else 40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2C34)),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = person.name,
                color = if (isFocused) FocusColor else Color.White,
                fontSize = if (isCompact) 12.sp else 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            person.role?.let {
                Text(
                    text = it,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = if (isCompact) 10.sp else 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
