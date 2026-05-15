package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.domain.models.MediaItem
import org.jellyplus.client.ui.components.MediaPoster
import org.jellyplus.client.ui.desktop.DesktopContentLeftPadding
import org.jellyplus.client.ui.desktop.DesktopContentRightPadding
import org.jellyplus.client.ui.viewmodels.SearchState

@Composable
fun DesktopSearchScreen(
    state: SearchState,
    onQueryChange: (String) -> Unit,
    onMediaClick: (MediaItem) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(start = DesktopContentLeftPadding, top = 36.dp, end = DesktopContentRightPadding, bottom = 36.dp),
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(0.7f),
            placeholder = { Text("Search Movies, TV Shows...", color = Color.Gray, fontSize = 24.sp) },
            singleLine = true,
            textStyle = TextStyle(fontSize = 24.sp, color = Color.White),
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.DarkGray.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.DarkGray.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color(0xFF00D4A8),
                ),
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF00D4A8), modifier = Modifier.size(64.dp))
            }
        } else if (state.results.isEmpty() && state.query.isNotEmpty()) {
            Text("No results found for '${state.query}'", color = Color.Gray, fontSize = 20.sp)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 180.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(6.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                items(state.results) { item ->
                    MediaPoster(
                        item = item,
                        baseUrl = state.baseUrl,
                        onClick = { onMediaClick(item) },
                        modifier = Modifier.width(192.dp)
                    )
                }
            }
        }
    }
}
