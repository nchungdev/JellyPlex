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

@UnstableApi
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

        // Derive content URL prefix from segments before the first DISCONTINUITY
        val baseline = extractBaselinePrefix(lines)

        // Pass 1: mark EXTINF+URL pairs as ads (hardcoded patterns OR baseline mismatch)
        var i = 0
        while (i < lines.size) {
            if (lines[i].startsWith("#EXTINF:") && i + 1 < lines.size) {
                val url = lines[i + 1].trim()
                if (isAdUrl(url) || (baseline != null && !url.startsWith(baseline))) {
                    skip[i] = true
                    skip[i + 1] = true
                }
            }
            i++
        }

        // Pass 2: remove DISCONTINUITY / KEY:METHOD=NONE adjacent to skipped segments.
        // Both tags were injected by the SSAI server alongside the ad; removing them lets
        // the surrounding content timestamps flow naturally (no artificial gap for the player).
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

    /**
     * Collect segment URLs that appear before the first #EXT-X-DISCONTINUITY and derive a
     * common prefix. Segments whose URL doesn't share this prefix after a discontinuity boundary
     * are treated as ad insertions.
     */
    private fun extractBaselinePrefix(lines: List<String>): String? {
        val segmentUrls = mutableListOf<String>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line == "#EXT-X-DISCONTINUITY") break
            if (line.startsWith("#EXTINF:") && i + 1 < lines.size) {
                segmentUrls += lines[i + 1].trim()
            }
            i++
        }
        if (segmentUrls.isEmpty()) return null

        val first = segmentUrls.first()
        return if (first.startsWith("http://") || first.startsWith("https://")) {
            // Absolute URL — use scheme + host as baseline
            val schemeEnd = first.indexOf("//") + 2
            val hostEnd = first.indexOf('/', schemeEnd).takeIf { it > 0 } ?: first.length
            first.substring(0, hostEnd)
        } else {
            // Relative URL — use directory path as baseline
            val lastSlash = first.lastIndexOf('/')
            if (lastSlash > 0) first.substring(0, lastSlash + 1) else null
        }
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

@UnstableApi
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
