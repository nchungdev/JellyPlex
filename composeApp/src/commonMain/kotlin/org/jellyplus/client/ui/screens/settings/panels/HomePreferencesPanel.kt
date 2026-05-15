package org.jellyplus.client.ui.screens.settings.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.ui.home.DefaultHomeSectionIds
import org.jellyplus.client.ui.home.HomeSectionLabels
import org.jellyplus.client.ui.home.orderedHomeSectionIds
import org.jellyplus.client.ui.home.parseHomeSectionIds
import org.jellyplus.client.ui.screens.settings.components.PreferenceInfoRow
import org.jellyplus.client.ui.viewmodels.PlaybackPreferencesState

@Composable
internal fun HomePreferencesPanel(
    state: PlaybackPreferencesState,
    onSectionOrderChange: (String) -> Unit,
    onEnabledSectionsChange: (String) -> Unit,
) {
    val orderedSections = orderedHomeSectionIds(state.homeSectionOrder)
    val enabledSections = parseHomeSectionIds(state.homeEnabledSections).toSet()

    fun saveOrder(next: List<String>) = onSectionOrderChange(next.joinToString(","))
    fun saveEnabled(next: Set<String>) =
        onEnabledSectionsChange(DefaultHomeSectionIds.filter { it in next }.joinToString(","))

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "HOME SECTIONS",
            color = Color.White.copy(alpha = 0.38f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
        )
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            PreferenceInfoRow(
                "Section order",
                "Use the arrow controls to move sections up or down. Disabled sections stay hidden on Home.",
            )
            orderedSections.forEachIndexed { index, sectionId ->
                HomeSectionSettingRow(
                    title = HomeSectionLabels[sectionId] ?: sectionId,
                    enabled = sectionId in enabledSections,
                    canMoveUp = index > 0,
                    canMoveDown = index < orderedSections.lastIndex,
                    onEnabledChange = { enabled ->
                        val next = if (enabled) enabledSections + sectionId else enabledSections - sectionId
                        saveEnabled(next)
                    },
                    onMoveUp = {
                        val next = orderedSections.toMutableList()
                        val moved = next.removeAt(index)
                        next.add(index - 1, moved)
                        saveOrder(next)
                    },
                    onMoveDown = {
                        val next = orderedSections.toMutableList()
                        val moved = next.removeAt(index)
                        next.add(index + 1, moved)
                        saveOrder(next)
                    },
                )
            }
        }
    }
}

@Composable
private fun HomeSectionSettingRow(
    title: String,
    enabled: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White.copy(alpha = 0.42f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move up",
                tint = Color.White.copy(alpha = if (canMoveUp) 0.72f else 0.18f))
        }
        IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(34.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move down",
                tint = Color.White.copy(alpha = if (canMoveDown) 0.72f else 0.18f))
        }
        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.18f),
            ),
        )
    }
}
