package org.jellyplus.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.jellyplus.client.data.local.ContextProvider
import org.jellyplus.client.media.PlayerPipController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextProvider.context = applicationContext

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        setContent {
            App()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        PlayerPipController.enterIfEnabled(this)
    }
}
