package org.jellyplus.client.ui.screens.settings.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jellyplus.client.ui.screens.settings.components.PreferenceGroupCard
import org.jellyplus.client.ui.screens.settings.components.PreferenceSelectRow
import org.jellyplus.client.ui.screens.settings.components.PreferenceSwitchRow
import org.jellyplus.client.ui.viewmodels.PlaybackPreferencesState

@Composable
internal fun ControlsPreferencesPanel(
    state: PlaybackPreferencesState,
    onShowGestureHintsChange: (Boolean) -> Unit,
    onDoubleTapSeekEnabledChange: (Boolean) -> Unit,
    onSeekBackChange: (Int) -> Unit,
    onSeekForwardChange: (Int) -> Unit,
    onHoldSpeedEnabledChange: (Boolean) -> Unit,
    onHoldSpeedMultiplierChange: (String) -> Unit,
    onSwipeLeftBrightnessChange: (Boolean) -> Unit,
    onSwipeRightVolumeChange: (Boolean) -> Unit,
) {
    val seekBackOptions = listOf(5, 10, 15, 30)
    val seekForwardOptions = listOf(5, 10, 15, 30)
    val speedOptions = listOf("1.5x", "2x", "2.5x", "3x")

    Column(modifier = Modifier.fillMaxWidth()) {
        PreferenceGroupCard("Double tap seek") {
            PreferenceSwitchRow(
                "Double tap left/right",
                "Double tap the left side to rewind and the right side to fast-forward.",
                state.doubleTapSeekEnabled,
                onDoubleTapSeekEnabledChange,
            )
            PreferenceSelectRow(
                "Left side seek",
                "${state.seekBackSeconds}s",
                seekBackOptions.map { "${it}s" },
                { value -> onSeekBackChange(value.removeSuffix("s").toInt()) },
            )
            PreferenceSelectRow(
                "Right side seek",
                "${state.seekForwardSeconds}s",
                seekForwardOptions.map { "${it}s" },
                { value -> onSeekForwardChange(value.removeSuffix("s").toInt()) },
            )
        }
        PreferenceGroupCard("Hold to speed up") {
            PreferenceSwitchRow(
                "Hold left/right",
                "Press and hold either side of the player to temporarily speed up playback.",
                state.holdSpeedEnabled,
                onHoldSpeedEnabledChange,
            )
            PreferenceSelectRow("Hold speed", state.holdSpeedMultiplier, speedOptions, onHoldSpeedMultiplierChange)
        }
        PreferenceGroupCard("Swipe controls", showDivider = false) {
            PreferenceSwitchRow(
                "Left side brightness",
                "Swipe up or down on the left side of the player to adjust brightness.",
                state.swipeLeftBrightnessEnabled,
                onSwipeLeftBrightnessChange,
            )
            PreferenceSwitchRow(
                "Right side volume",
                "Swipe up or down on the right side of the player to adjust volume.",
                state.swipeRightVolumeEnabled,
                onSwipeRightVolumeChange,
            )
            PreferenceSwitchRow(
                "Gesture hints",
                "Show visual feedback while seeking, speeding up, changing brightness or volume.",
                state.showGestureHints,
                onShowGestureHintsChange,
            )
        }
    }
}
