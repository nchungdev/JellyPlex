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

        val baseline = extractBaselinePrefix(lines)

        // Pass 1: mark EXTINF+URL pairs that are clearly ad URLs (hardcoded SSAI patterns).
        // We no longer use the baseline mismatch heuristic here — Jellyfin can legitimately
        // emit #EXT-X-DISCONTINUITY (codec/bitrate change, seek restart) and the new segment
        // URLs may differ from the pre-discontinuity prefix, causing false positives.
        var i = 0
        while (i < lines.size) {
            if (lines[i].startsWith("#EXTINF:") && i + 1 < lines.size) {
                val url = lines[i + 1].trim()
                if (isAdUrl(url)) {
                    skip[i] = true
                    skip[i + 1] = true
                }
            }
            i++
        }

        // Pass 2: mark segments inside an ad pod — a DISCONTINUITY-bounded block whose URLs
        // don't match the content baseline (classic SSAI injection pattern). We require BOTH
        // a surrounding DISCONTINUITY pair AND a URL mismatch, so a Jellyfin-internal
        // DISCONTINUITY (with same-origin URLs) is never misidentified as an ad pod.
        if (baseline != null) {
            var inDiscontinuity = false
            var podStart = -1
            var j = 0
            while (j < lines.size) {
                val line = lines[j].trim()
                when {
                    line == "#EXT-X-DISCONTINUITY" -> {
                        if (!inDiscontinuity) {
                            inDiscontinuity = true
                            podStart = j
                        } else {
                            // Second DISCONTINUITY closes the pod — check if it was an ad pod
                            // (i.e., at least one segment in the block had a mismatched URL).
                            val podIsAd = (podStart + 1 until j).any { k ->
                                lines[k].startsWith("#EXTINF:") && k + 1 < lines.size &&
                                    !lines[k + 1].trim().startsWith(baseline)
                            }
                            if (podIsAd) {
                                for (k in podStart..j) skip[k] = true
                            }
                            inDiscontinuity = false
                            podStart = -1
                        }
                    }
                }
                j++
            }
        }

        // Pass 3: remove DISCONTINUITY / KEY:METHOD=NONE adjacent to skipped segments.
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
     * Derives a content URL prefix from segments before the first #EXT-X-DISCONTINUITY.
     * Used only for ad-pod detection (Pass 2), not for individual segment checks.
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
            val schemeEnd = first.indexOf("//") + 2
            val hostEnd = first.indexOf('/', schemeEnd).takeIf { it > 0 } ?: first.length
            first.substring(0, hostEnd)
        } else {
            val lastSlash = first.lastIndexOf('/')
            if (lastSlash > 0) first.substring(0, lastSlash + 1) else null
        }
    }

    // Patterns that unambiguously identify SSAI ad segment URLs.
    // Intentionally conservative — false negatives (missing an ad) are better than
    // false positives (dropping real content from a Jellyfin stream).
    private fun isAdUrl(url: String): Boolean {
        val u = url.trim()
        return u.contains("doubleclick") ||
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
