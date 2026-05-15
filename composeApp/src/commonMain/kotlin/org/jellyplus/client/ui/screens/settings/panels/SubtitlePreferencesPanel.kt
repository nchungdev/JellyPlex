package org.jellyplus.client.ui.screens.settings.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.ui.screens.settings.components.PreferenceGroupCard
import org.jellyplus.client.ui.screens.settings.components.PreferenceInfoRow
import org.jellyplus.client.ui.screens.settings.components.PreferenceSelectRow
import org.jellyplus.client.ui.viewmodels.PlaybackPreferencesState

@Composable
internal fun SubtitlePreferencesPanel(
    state: PlaybackPreferencesState,
    onLanguageChange: (String) -> Unit,
    onModeChange: (String) -> Unit,
    onTextSizeChange: (String) -> Unit,
    onTextWeightChange: (String) -> Unit,
    onFontStyleChange: (String) -> Unit,
    onTextColorChange: (String) -> Unit,
    onBackgroundColorChange: (String) -> Unit,
    onShadowChange: (String) -> Unit,
    onVerticalPositionChange: (String) -> Unit,
) {
    val languageOptions = listOf("Any", "Vietnamese", "English", "Japanese", "Korean", "Chinese")
    val subtitleModes = listOf("Default", "Smart", "Always play", "Forced only", "None")
    val sizeOptions = listOf("Smaller", "Small", "Normal", "Large", "Larger", "Very large")
    val weightOptions = listOf("Normal", "Bold")
    val fontOptions = listOf("Default", "Monospace", "Serif", "Casual", "Small caps")
    val colorOptions = listOf("White", "Light gray", "Gray", "Yellow", "Green", "Cyan", "Blue", "Red", "Black")
    val shadowOptions = listOf("None", "Raised", "Depressed", "Uniform", "Drop shadow")
    val positionOptions = listOf("Bottom", "Lower", "Middle", "Upper", "Top")

    Column(modifier = Modifier.fillMaxWidth()) {
        PreferenceGroupCard("Subtitle loading") {
            PreferenceSelectRow("Preferred subtitle language", state.subtitleLanguage, languageOptions, onLanguageChange)
            PreferenceSelectRow("Subtitle mode", state.subtitleMode, subtitleModes, onModeChange)
            PreferenceInfoRow(
                "Mode behavior",
                "Default respects flags and preferred language. Smart loads subtitles when audio is foreign. Forced only loads forced subtitles.",
            )
        }
        PreferenceGroupCard("Subtitle appearance", showDivider = false) {
            PreferenceInfoRow("Device subtitles", "These settings affect subtitle rendering on this device.")
            PreferenceInfoRow("Scope", "Graphic subtitles and ASS/SSA subtitles with embedded styles may ignore appearance settings.")
            PreferenceSelectRow("Text size", state.subtitleTextSize, sizeOptions, onTextSizeChange)
            PreferenceSelectRow("Text weight", state.subtitleTextWeight, weightOptions, onTextWeightChange)
            PreferenceSelectRow("Typeface", state.subtitleFontStyle, fontOptions, onFontStyleChange)
            PreferenceSelectRow("Text color", state.subtitleTextColor, colorOptions, onTextColorChange)
            PreferenceSelectRow("Background color", state.subtitleBackgroundColor, colorOptions, onBackgroundColorChange)
            PreferenceSelectRow("Shadow", state.subtitleShadow, shadowOptions, onShadowChange)
            PreferenceSelectRow("Vertical position", state.subtitleVerticalPosition, positionOptions, onVerticalPositionChange)
            SubtitlePreview(
                textColor = state.subtitleTextColor,
                backgroundColor = state.subtitleBackgroundColor,
                textSize = state.subtitleTextSize,
                textWeight = state.subtitleTextWeight,
            )
        }
    }
}

@Composable
private fun SubtitlePreview(
    textColor: String,
    backgroundColor: String,
    textSize: String,
    textWeight: String,
) {
    val fg = subtitleColor(textColor)
    val bg = subtitleColor(backgroundColor).copy(alpha = 0.72f)
    val size = when (textSize) {
        "Smaller" -> 13.sp
        "Small" -> 15.sp
        "Large" -> 19.sp
        "Larger" -> 21.sp
        "Very large" -> 23.sp
        else -> 17.sp
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .height(92.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "Subtitle preview",
            color = fg,
            fontSize = size,
            fontWeight = if (textWeight == "Bold") FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(bg)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

private fun subtitleColor(name: String): Color = when (name) {
    "Light gray" -> Color(0xFFD6D6D6)
    "Gray" -> Color(0xFF888888)
    "Yellow" -> Color(0xFFFFEB3B)
    "Green" -> Color(0xFF4CAF50)
    "Cyan" -> Color(0xFF00BCD4)
    "Blue" -> Color(0xFF2196F3)
    "Red" -> Color(0xFFF44336)
    "Black" -> Color.Black
    else -> Color.White
}
