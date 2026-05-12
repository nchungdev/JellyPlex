package org.jellyplus.client.ui.common.components.player

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun PlayerSettingsPopup(
    autoSkipIntro: Boolean,
    autoSkipOutro: Boolean,
    autoSkipPreview: Boolean,
    autoNext: Boolean = false,
    isEpisode: Boolean = false,
    currentSpeed: Float,
    onToggleAutoSkip: () -> Unit,
    onToggleAutoSkipOutro: () -> Unit,
    onToggleAutoSkipPreview: () -> Unit,
    onToggleAutoNext: () -> Unit = {},
    onSpeedChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E1E),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.75f),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Settings", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color(0xFF24D366), fontSize = 13.sp)
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(scrollState),
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    PlayerSettingsToggleRow("Auto-skip intro", autoSkipIntro, onToggleAutoSkip)
                    PlayerSettingsToggleRow("Auto-skip outro", autoSkipOutro, onToggleAutoSkipOutro)
                    PlayerSettingsToggleRow("Auto-skip preview", autoSkipPreview, onToggleAutoSkipPreview)
                    if (isEpisode) PlayerSettingsToggleRow("Auto-next episode", autoNext, onToggleAutoNext)
                    Text(
                        "Playback Speed",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    )
                    val speedOptions = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        speedOptions.forEach { speed ->
                            val isSelected = currentSpeed == speed
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF24D366) else Color.White.copy(alpha = 0.1f),
                                modifier = Modifier.weight(1f).clickable { onSpeedChange(speed) },
                            ) {
                                Text(
                                    "${speed}×",
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PlayerSettingsToggleRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 1.dp else 0.dp,
                color = if (isFocused) Color(0xFF24D366) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.scale(0.8f),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF24D366),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
            ),
        )
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
}
