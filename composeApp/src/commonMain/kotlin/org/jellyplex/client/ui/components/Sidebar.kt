package org.jellyplex.client.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplex.client.ui.navigation.NavigationItem

@Composable
fun Sidebar(
    selectedItem: NavigationItem,
    onItemSelected: (NavigationItem) -> Unit,
) {
    val items =
        listOf(
            NavigationItem.Search,
            NavigationItem.Home,
            NavigationItem.Movies,
            NavigationItem.TvShows,
            NavigationItem.Settings,
        )

    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .width(240.dp)
                // Plex-like dark background
                .background(Color(0xFF1F1F1F))
                .padding(16.dp),
    ) {
        Text(
            "JellyPlex",
            // Plex Gold
            color = Color(0xFFFFB300),
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 32.dp),
        )

        items.forEach { item ->
            SidebarItem(
                item = item,
                isSelected = selectedItem == item,
                onClick = { onItemSelected(item) },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SidebarItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    val backgroundColor =
        when {
            isFocused -> Color.White.copy(alpha = 0.2f)
            isSelected -> Color.White.copy(alpha = 0.1f)
            else -> Color.Transparent
        }

    val textColor = if (isSelected || isFocused) Color.White else Color.Gray

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .onFocusChanged { isFocused = it.isFocused }
                .focusable()
                .clickable { onClick() }
                .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = textColor,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                color = textColor,
                fontSize = 16.sp,
            )
        }
    }
}
