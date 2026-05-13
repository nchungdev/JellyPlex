package org.jellyplus.client.ui.desktop.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jellyplus.client.ui.mobile.screens.MobileAuthLogo

@Composable
internal fun DesktopAuthScaffold(
    modifier: Modifier = Modifier,
    maxContentWidth: androidx.compose.ui.unit.Dp = 420.dp,
    logoSize: androidx.compose.ui.unit.Dp = 104.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 36.dp),
    ) {
        MobileAuthLogo(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(logoSize),
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = maxContentWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}
