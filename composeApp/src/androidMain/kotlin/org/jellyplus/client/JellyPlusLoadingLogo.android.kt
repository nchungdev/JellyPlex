package org.jellyplus.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource

@Composable
actual fun JellyPlusLoadingLogo(modifier: Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_splash_logo),
        contentDescription = "JellyPlus",
        modifier = modifier.clip(RoundedCornerShape(percent = 38)),
    )
}
