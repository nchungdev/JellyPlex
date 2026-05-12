package org.jellyplus.client.media

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.hls.playlist.HlsMediaPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsMultivariantPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylist
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParser
import androidx.media3.exoplayer.hls.playlist.HlsPlaylistParserFactory
import androidx.media3.exoplayer.upstream.ParsingLoadable
import java.io.InputStream

@OptIn(UnstableApi::class)
class AdFilteringPlaylistParser : ParsingLoadable.Parser<HlsPlaylist> {
    private val defaultParser = HlsPlaylistParser()

    override fun parse(uri: Uri, inputStream: InputStream): HlsPlaylist {
        val rawManifest = inputStream.bufferedReader().use { it.readText() }
        val cleanManifest = removeAdSegments(rawManifest)
        return defaultParser.parse(uri, cleanManifest.byteInputStream())
    }

    private fun removeAdSegments(manifest: String): String {
        val lines = manifest.split("\n")
        val skip = BooleanArray(lines.size)

        // Pass 1: mark EXTINF+URL pairs whose URL matches known ad patterns
        var i = 0
        while (i < lines.size) {
            if (lines[i].startsWith("#EXTINF:") && i + 1 < lines.size) {
                if (isAdUrl(lines[i + 1])) {
                    skip[i] = true
                    skip[i + 1] = true
                }
            }
            i++
        }

        // Pass 2: remove DISCONTINUITY tags and EXT-X-KEY:METHOD=NONE adjacent to skipped segments
        for (j in lines.indices) {
            val line = lines[j].trim()
            if (line == "#EXT-X-DISCONTINUITY" || line == "#EXT-X-KEY:METHOD=NONE") {
                val prevSkipped = (j - 1 downTo maxOf(0, j - 3)).any { skip[it] }
                val nextSkipped = (j + 1..minOf(lines.size - 1, j + 3)).any { skip[it] }
                if (prevSkipped || nextSkipped) skip[j] = true
            }
        }

        return lines.filterIndexed { idx, _ -> !skip[idx] }.joinToString("\n")
    }

    private fun isAdUrl(url: String): Boolean {
        val u = url.trim()
        return u.contains("ads") ||
            u.contains("doubleclick") ||
            u.contains("dai.google.com") ||
            u.startsWith("convertv7/") ||
            Regex("^/v[0-9]+/[a-f0-9]+/").containsMatchIn(u)
    }
}

@OptIn(UnstableApi::class)
class CustomHlsPlaylistParserFactory : HlsPlaylistParserFactory {
    override fun createPlaylistParser(): ParsingLoadable.Parser<HlsPlaylist> {
        return AdFilteringPlaylistParser()
    }

    override fun createPlaylistParser(
        multivariantPlaylist: HlsMultivariantPlaylist,
        previousMediaPlaylist: HlsMediaPlaylist?
    ): ParsingLoadable.Parser<HlsPlaylist> {
        return AdFilteringPlaylistParser()
    }
}
