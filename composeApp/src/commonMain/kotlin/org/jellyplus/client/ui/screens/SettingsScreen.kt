package org.jellyplus.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jellyplus.client.isDebug
import org.jellyplus.client.ui.screens.settings.panels.AccountPreferencesPanel
import org.jellyplus.client.ui.screens.settings.panels.ChangePasswordPanel
import org.jellyplus.client.ui.screens.settings.panels.ControlsPreferencesPanel
import org.jellyplus.client.ui.screens.settings.panels.DisplayPreferencesPanel
import org.jellyplus.client.ui.screens.settings.panels.HomePreferencesPanel
import org.jellyplus.client.ui.screens.settings.panels.PlaybackPreferencesPanel
import org.jellyplus.client.ui.screens.settings.panels.QuickConnectPanel
import org.jellyplus.client.ui.screens.settings.panels.SubtitlePreferencesPanel
import org.jellyplus.client.ui.viewmodels.AccountSettingsViewModel
import org.jellyplus.client.ui.viewmodels.PlaybackPreferencesViewModel
import org.jellyplus.client.ui.viewmodels.SessionViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    sessionViewModel: SessionViewModel,
    onBack: () -> Unit,
) {
    val playbackViewModel: PlaybackPreferencesViewModel = koinViewModel()
    val playbackState by playbackViewModel.state.collectAsState()
    val accountSettingsViewModel: AccountSettingsViewModel = koinViewModel()
    val accountState by accountSettingsViewModel.state.collectAsState()
    var selectedSection by remember { mutableStateOf<SettingsSection?>(null) }
    val serverHost = sessionViewModel.getBaseUrl()
        .removePrefix("https://").removePrefix("http://").trimEnd('/')

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF181818)).statusBarsPadding().navigationBarsPadding(),
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                when (selectedSection) {
                    null -> onBack()
                    // Sub-sections of Account go back to Account, not to the root list
                    SettingsSection.ChangePassword,
                    SettingsSection.QuickConnect -> selectedSection = SettingsSection.Account
                    else -> selectedSection = null
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                selectedSection?.title ?: "Settings",
                color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))

        // ── Body ─────────────────────────────────────────────────────────────
        if (selectedSection == null) {
            LazyColumn(contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)) {
                item {
                    SettingsSectionLabel("ACCOUNT")
                    SettingsCategoryRow(Icons.Default.Person, "Profile & account",
                        "Signed-in user, server and session info") { selectedSection = SettingsSection.Account }
                }
                item {
                    SettingsSectionLabel("APP EXPERIENCE")
                    SettingsCategoryRow(Icons.Default.PlayArrow, "Playback",
                        "Auto next, skip segments, PiP, speed and seek buttons") { selectedSection = SettingsSection.Playback }
                    SettingsCategoryRow(Icons.Default.Info, "Subtitles",
                        "Subtitle language, mode, text size and style") { selectedSection = SettingsSection.Subtitles }
                    SettingsCategoryRow(Icons.Default.Settings, "Display",
                        "Theme, poster density, images and metadata") { selectedSection = SettingsSection.Display }
                    SettingsCategoryRow(Icons.Default.Home, "Home",
                        "Default tab, section order and content rows") { selectedSection = SettingsSection.Home }
                    SettingsCategoryRow(Icons.Default.Search, "Controls",
                        "Gestures, keyboard, remote and quick actions") { selectedSection = SettingsSection.Controls }
                }
                item {
                    SettingsSectionLabel("SYSTEM")
                    SettingsCategoryRow(Icons.Default.Info, "About",
                        "App, server and build information") { selectedSection = SettingsSection.About }
                }
            }
        } else {
            when (selectedSection) {
                SettingsSection.Account -> SettingsDetailList {
                    AccountPreferencesPanel(
                        userName = sessionViewModel.getUserName() ?: "-",
                        serverHost = serverHost,
                        onChangePasswordTap = { selectedSection = SettingsSection.ChangePassword },
                        onQuickConnectTap = { selectedSection = SettingsSection.QuickConnect },
                    )
                }
                SettingsSection.ChangePassword -> SettingsDetailList {
                    LaunchedEffect(Unit) { accountSettingsViewModel.clearMessages() }
                    ChangePasswordPanel(
                        state = accountState,
                        onChangePassword = accountSettingsViewModel::changePassword,
                    )
                }
                SettingsSection.QuickConnect -> SettingsDetailList {
                    LaunchedEffect(Unit) { accountSettingsViewModel.clearMessages() }
                    QuickConnectPanel(
                        state = accountState,
                        onAuthorizeQuickConnect = accountSettingsViewModel::authorizeQuickConnect,
                    )
                }
                SettingsSection.Playback -> SettingsDetailList {
                    PlaybackPreferencesPanel(
                        state = playbackState,
                        onAutoSkipIntroChange = playbackViewModel::setAutoSkipIntro,
                        onAutoSkipOutroChange = playbackViewModel::setAutoSkipOutro,
                        onAutoSkipRecapChange = playbackViewModel::setAutoSkipRecap,
                        onAutoSkipPreviewChange = playbackViewModel::setAutoSkipPreview,
                        onAutoNextChange = playbackViewModel::setAutoNext,
                        onAutoPipChange = playbackViewModel::setAutoPictureInPicture,
                        onSeamlessTransitionChange = playbackViewModel::setSeamlessTransition,
                        onPreferOriginalAudioChange = playbackViewModel::setPreferOriginalAudio,
                        onPlaybackSpeedChange = playbackViewModel::setPlaybackSpeed,
                        onMaxAudioChannelsChange = playbackViewModel::setMaxAudioChannels,
                        onPreferredAudioLanguageChange = playbackViewModel::setPreferredAudioLanguage,
                        onPlayDefaultAudioTrackChange = playbackViewModel::setPlayDefaultAudioTrack,
                        onInternetVideoQualityChange = playbackViewModel::setInternetVideoQuality,
                        onMaxTranscodeResolutionChange = playbackViewModel::setMaxTranscodeResolution,
                        onLimitMaxVideoResolutionChange = playbackViewModel::setLimitMaxVideoResolution,
                        onMusicInternetQualityChange = playbackViewModel::setMusicInternetQuality,
                        onRememberAudioTrackChange = playbackViewModel::setRememberAudioTrack,
                        onRememberSubtitleTrackChange = playbackViewModel::setRememberSubtitleTrack,
                        onShowNextVideoInfoChange = playbackViewModel::setShowNextVideoInfo,
                        onAudioNormalizationChange = playbackViewModel::setAudioNormalization,
                    )
                }
                SettingsSection.Subtitles -> SettingsDetailList {
                    SettingsSectionLabel("SUBTITLES")
                    SubtitlePreferencesPanel(
                        state = playbackState,
                        onLanguageChange = playbackViewModel::setSubtitleLanguage,
                        onModeChange = playbackViewModel::setSubtitleMode,
                        onTextSizeChange = playbackViewModel::setSubtitleTextSize,
                        onTextWeightChange = playbackViewModel::setSubtitleTextWeight,
                        onFontStyleChange = playbackViewModel::setSubtitleFontStyle,
                        onTextColorChange = playbackViewModel::setSubtitleTextColor,
                        onBackgroundColorChange = playbackViewModel::setSubtitleBackgroundColor,
                        onShadowChange = playbackViewModel::setSubtitleShadow,
                        onVerticalPositionChange = playbackViewModel::setSubtitleVerticalPosition,
                    )
                }
                SettingsSection.Display -> SettingsDetailList {
                    SettingsSectionLabel("DISPLAY")
                    DisplayPreferencesPanel(
                        state = playbackState,
                        onLanguageChange = playbackViewModel::setAppLanguage,
                        onThemeChange = playbackViewModel::setAppTheme,
                        onPosterDensityChange = playbackViewModel::setPosterDensity,
                        onImageQualityChange = playbackViewModel::setImageQuality,
                        onMetadataDisplayChange = playbackViewModel::setMetadataDisplay,
                    )
                }
                SettingsSection.Home -> SettingsDetailList {
                    SettingsSectionLabel("HOME")
                    HomePreferencesPanel(
                        state = playbackState,
                        onSectionOrderChange = playbackViewModel::setHomeSectionOrder,
                        onEnabledSectionsChange = playbackViewModel::setHomeEnabledSections,
                    )
                }
                SettingsSection.Controls -> SettingsDetailList {
                    SettingsSectionLabel("CONTROLS")
                    ControlsPreferencesPanel(
                        state = playbackState,
                        onShowGestureHintsChange = playbackViewModel::setShowGestureHints,
                        onDoubleTapSeekEnabledChange = playbackViewModel::setDoubleTapSeekEnabled,
                        onSeekBackChange = playbackViewModel::setSeekBackSeconds,
                        onSeekForwardChange = playbackViewModel::setSeekForwardSeconds,
                        onHoldSpeedEnabledChange = playbackViewModel::setHoldSpeedEnabled,
                        onHoldSpeedMultiplierChange = playbackViewModel::setHoldSpeedMultiplier,
                        onSwipeLeftBrightnessChange = playbackViewModel::setSwipeLeftBrightnessEnabled,
                        onSwipeRightVolumeChange = playbackViewModel::setSwipeRightVolumeEnabled,
                    )
                }
                SettingsSection.About -> SettingsDetailList {
                    SettingsSectionLabel("ABOUT")
                    SettingsInfoRow(icon = Icons.Default.Info, title = "App", value = "JellyPlus")
                    SettingsInfoRow(icon = Icons.Default.Info, title = "Server", value = serverHost)
                    SettingsInfoRow(icon = Icons.Default.Info, title = "Build", value = if (isDebug()) "Debug" else "Release")
                }
                null -> Unit
            }
        }
    }
}

// ── Navigation enum ──────────────────────────────────────────────────────────

internal enum class SettingsSection(val title: String) {
    Account("Profile & account"),
    // Account sub-sections
    ChangePassword("Change password"),
    QuickConnect("Quick Connect"),
    // Other sections
    Playback("Playback"),
    Subtitles("Subtitles"),
    Display("Display"),
    Home("Home"),
    Controls("Controls"),
    About("About"),
}

// ── Shared row components (used by panels via internal visibility) ────────────

@Composable
private fun SettingsDetailList(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 40.dp),
        content = content,
    )
}

@Composable
internal fun SettingsCategoryRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .background(Color.White.copy(alpha = 0.055f))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.White.copy(alpha = 0.46f), fontSize = 12.sp, lineHeight = 16.sp,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.32f), modifier = Modifier.size(20.dp))
    }
}

@Composable
internal fun SettingsSectionLabel(text: String) {
    Text(
        text,
        color = Color.White.copy(alpha = 0.35f),
        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
internal fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = Color.White.copy(alpha = 0.45f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(title, color = Color.White.copy(alpha = 0.55f), fontSize = 14.sp, modifier = Modifier.width(110.dp))
        Text(value, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}
