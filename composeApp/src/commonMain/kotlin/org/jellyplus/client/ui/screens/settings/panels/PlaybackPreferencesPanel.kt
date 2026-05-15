package org.jellyplus.client.ui.screens.settings.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jellyplus.client.ui.screens.settings.components.PreferenceGroupCard
import org.jellyplus.client.ui.screens.settings.components.PreferenceInfoRow
import org.jellyplus.client.ui.screens.settings.components.PreferenceSelectRow
import org.jellyplus.client.ui.screens.settings.components.PreferenceSwitchRow
import org.jellyplus.client.ui.viewmodels.PlaybackPreferencesState

@Composable
internal fun PlaybackPreferencesPanel(
    state: PlaybackPreferencesState,
    onAutoSkipIntroChange: (Boolean) -> Unit,
    onAutoSkipOutroChange: (Boolean) -> Unit,
    onAutoSkipRecapChange: (Boolean) -> Unit,
    onAutoSkipPreviewChange: (Boolean) -> Unit,
    onAutoNextChange: (Boolean) -> Unit,
    onAutoPipChange: (Boolean) -> Unit,
    onSeamlessTransitionChange: (Boolean) -> Unit,
    onPreferOriginalAudioChange: (Boolean) -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    onMaxAudioChannelsChange: (String) -> Unit,
    onPreferredAudioLanguageChange: (String) -> Unit,
    onPlayDefaultAudioTrackChange: (Boolean) -> Unit,
    onInternetVideoQualityChange: (String) -> Unit,
    onMaxTranscodeResolutionChange: (String) -> Unit,
    onLimitMaxVideoResolutionChange: (Boolean) -> Unit,
    onMusicInternetQualityChange: (String) -> Unit,
    onRememberAudioTrackChange: (Boolean) -> Unit,
    onRememberSubtitleTrackChange: (Boolean) -> Unit,
    onShowNextVideoInfoChange: (Boolean) -> Unit,
    onAudioNormalizationChange: (String) -> Unit,
) {
    val videoQualityOptions = listOf("Auto", "120 Mbps", "80 Mbps", "60 Mbps", "40 Mbps", "20 Mbps", "10 Mbps", "4 Mbps", "720 kbps")
    val musicQualityOptions = listOf("Auto", "2 Mbps", "1 Mbps", "320 kbps", "192 kbps", "128 kbps", "64 kbps")
    val resolutionOptions = listOf("Auto", "Screen", "360p", "480p", "720p", "1080p", "4K", "8K")
    val audioChannelOptions = listOf("Auto", "Mono", "Stereo", "5.1", "7.1")
    val audioLanguageOptions = listOf("Any", "Vietnamese", "English", "Japanese", "Korean", "Chinese")
    val normalizationOptions = listOf("Off", "Track", "Album")

    Column(modifier = Modifier.fillMaxWidth()) {
        PreferenceGroupCard("Audio") {
            PreferenceSelectRow("Max audio channels", state.maxAudioChannels, audioChannelOptions, onMaxAudioChannelsChange)
            PreferenceSelectRow("Preferred audio language", state.preferredAudioLanguage, audioLanguageOptions, onPreferredAudioLanguageChange)
            PreferenceSwitchRow("Play default audio track", "Use the default audio track regardless of language", state.playDefaultAudioTrack, onPlayDefaultAudioTrackChange)
            PreferenceSwitchRow("Prefer original audio", "Choose original language tracks when Jellyfin can identify them", state.preferOriginalAudio, onPreferOriginalAudioChange)
            PreferenceSwitchRow("Set audio from previous item", "Try to match the last selected audio track", state.rememberAudioTrack, onRememberAudioTrackChange)
        }

        PreferenceGroupCard("Streaming quality") {
            PreferenceSelectRow("Video quality", state.internetVideoQuality, videoQualityOptions, onInternetVideoQualityChange)
            PreferenceSelectRow("Max transcode resolution", state.maxTranscodeResolution, resolutionOptions, onMaxTranscodeResolutionChange)
            PreferenceSwitchRow("Limit supported resolution", "Use max transcode resolution as the supported video ceiling", state.limitMaxVideoResolution, onLimitMaxVideoResolutionChange)
            PreferenceSelectRow("Music quality", state.musicInternetQuality, musicQualityOptions, onMusicInternetQualityChange)
            PreferenceSelectRow("Audio normalization", state.audioNormalization, normalizationOptions, onAudioNormalizationChange)
        }

        PreferenceGroupCard("Segment actions") {
            PreferenceSwitchRow("Intro", "Skip intro segments when available", state.autoSkipIntro, onAutoSkipIntroChange)
            PreferenceSwitchRow("Outro / Credits", "Jump credits when the server marks them", state.autoSkipOutro, onAutoSkipOutroChange)
            PreferenceSwitchRow("Recap", "Skip recap segments before an episode starts", state.autoSkipRecap, onAutoSkipRecapChange)
            PreferenceSwitchRow("Preview", "Skip preview/trailer segments", state.autoSkipPreview, onAutoSkipPreviewChange)
            PreferenceInfoRow("Commercial", "Ask to skip when segment markers are available")
        }

        PreferenceGroupCard("Behavior", showDivider = false) {
            PreferenceSwitchRow("Auto Next", "Continue to the next episode automatically", state.autoNext, onAutoNextChange)
            PreferenceSwitchRow("Set subtitles from previous item", "Try to match the last selected subtitle track", state.rememberSubtitleTrack, onRememberSubtitleTrackChange)
            PreferenceSwitchRow("Show next video info", "Show next item information near the end of playback", state.showNextVideoInfo, onShowNextVideoInfoChange)
            PreferenceSwitchRow("Seamless transition", "Preload the next episode for faster handoff", state.seamlessTransition, onSeamlessTransitionChange)
            PreferenceSwitchRow("Auto PiP", "Enter picture-in-picture when leaving playback", state.autoPictureInPicture, onAutoPipChange)
        }
    }
}
