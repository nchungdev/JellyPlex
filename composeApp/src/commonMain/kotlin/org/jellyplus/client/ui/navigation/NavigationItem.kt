package org.jellyplus.client.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val title: String, val icon: ImageVector) {
    object Home : NavigationItem("Home", Icons.Default.Home)

    object Movies : NavigationItem("Movies", Icons.Default.PlayArrow)

    object TvShows : NavigationItem("TV Shows", Icons.AutoMirrored.Filled.List)

    object Search : NavigationItem("Search", Icons.Default.Search)

    object Settings : NavigationItem("Settings", Icons.Default.Settings)
}
