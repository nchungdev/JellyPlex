package org.jellyplus.client.media

import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational

object PlayerPipController {
    @Volatile private var active = false
    @Volatile private var autoEnterEnabled = false

    fun update(active: Boolean, autoEnterEnabled: Boolean) {
        this.active = active
        this.autoEnterEnabled = autoEnterEnabled
    }

    fun clear() {
        active = false
        autoEnterEnabled = false
    }

    fun enterIfEnabled(activity: Activity) {
        if (!active || !autoEnterEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .build()
        activity.enterPictureInPictureMode(params)
    }
}
