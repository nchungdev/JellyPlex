package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.ui.desktop.DesktopContentLeftPadding
import org.jellyplus.client.ui.desktop.DesktopContentRightPadding
import org.jellyplus.client.ui.viewmodels.DownloadsState

@Composable
fun DesktopDownloadsScreen(state: DownloadsState) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(start = DesktopContentLeftPadding, top = 36.dp, end = DesktopContentRightPadding, bottom = 36.dp),
    ) {
        Text("Downloads", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(48.dp))

        if (state.tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active downloads", color = Color.Gray, fontSize = 24.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                items(state.tasks) { task ->
                    Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                        Text(
                            task.mediaItem.title,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 24.sp,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { task.progress },
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                            color = Color(0xFFFFB300),
                            trackColor = Color.DarkGray.copy(alpha = 0.5f),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                if (task.isCompleted) "Completed" else "Downloading",
                                color = Color.Gray,
                                fontSize = 18.sp,
                            )
                            Text("${(task.progress * 100).toInt()}%", color = Color.Gray, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}
