package org.jellyplus.client.media;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerView;

public class PlayerViewUtils {
    @OptIn(markerClass = UnstableApi.class)
    public static void setTextureViewSurface(PlayerView playerView) {
        playerView.setSurfaceType(PlayerView.SURFACE_TYPE_TEXTURE_VIEW);
    }
}
