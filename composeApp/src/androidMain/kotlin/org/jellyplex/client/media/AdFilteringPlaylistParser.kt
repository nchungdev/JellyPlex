package org.jellyplex.client.media

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
        val result = mutableListOf<String>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            // Logic to identify ad segments.
            // We skip the #EXTINF tag and the following URL if it matches ad patterns.
            if (line.startsWith("#EXTINF:") && i + 1 < lines.size) {
                val nextLine = lines[i + 1]
                if (nextLine.contains("ads") || nextLine.contains("doubleclick") || nextLine.contains("dai.google.com")) {
                    i += 2
                    continue
                }
            }

            result.add(line)
            i++
        }

        return result.joinToString("\n")
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
