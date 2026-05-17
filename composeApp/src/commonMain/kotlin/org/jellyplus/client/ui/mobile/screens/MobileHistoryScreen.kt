package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import org.jellyplus.client.domain.models.MediaType
import org.jellyplus.client.ui.viewmodels.HistoryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MobileHistoryScreen(
    paddingValues: PaddingValues,
    onMediaClick: (MediaItem) -> Unit,
) {
    val viewModel: HistoryViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadHistory() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding(),
            bottom = paddingValues.calculateBottomPadding() + 24.dp,
        )
    ) {
        when {
            state.isLoading && state.watchedItems.isEmpty() && state.resumeItems.isEmpty() -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            !state.isLoading && state.watchedItems.isEmpty() && state.resumeItems.isEmpty() && state.error != null -> {
                item { HistoryErrorState(error = state.error, onRetry = { viewModel.loadHistory() }) }
            }

            !state.isLoading && state.watchedItems.isEmpty() && state.resumeItems.isEmpty() -> {
                item { HistoryEmptyState() }
            }

            else -> {
                if (state.resumeItems.isNotEmpty()) {
                    item {
                        HistorySectionHeader("Đang xem")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.resumeItems) { item ->
                                MobileContinueWatchingCard(
                                    item = item,
                                    baseUrl = state.baseUrl,
                                    onClick = { onMediaClick(item) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (state.watchedItems.isNotEmpty()) {
                    item { HistorySectionHeader("Đã xem") }
                    items(state.watchedItems) { item ->
                        HistoryItemRow(item = item, baseUrl = state.baseUrl, onClick = { onMediaClick(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySectionHeader(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
    )
}

@Composable
private fun HistoryItemRow(item: MediaItem, baseUrl: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(96.dp).height(54.dp).clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            AsyncImage(
                model = item.getBackdropUrl(baseUrl) ?: item.getImageUrl(baseUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val displayTitle = if (item.type == MediaType.EPISODE && item.seriesName != null) {
                item.seriesName
            } else {
                item.title
            }
            Text(displayTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            val subTitle = when (item.type) {
                MediaType.EPISODE -> buildString {
                    if (item.seriesName != null) append(item.title)
                    if (item.parentIndexNumber != null && item.index != null) {
                        if (isNotEmpty()) append(" · ")
                        append("S${item.parentIndexNumber}E${item.index}")
                    }
                }
                else -> item.year?.toString() ?: ""
            }
            if (subTitle.isNotEmpty()) {
                Text(subTitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 1)
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun HistoryEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.History, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(90.dp))
        Spacer(Modifier.height(20.dp))
        Text("Chưa có lịch sử xem", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Bắt đầu xem để lưu lịch sử", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
    }
}

@Composable
private fun HistoryErrorState(error: String?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 120.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Không thể tải lịch sử", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(error ?: "Lỗi kết nối", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("Thử lại", color = Color.Black)
        }
    }
}
