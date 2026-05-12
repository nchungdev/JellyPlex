package org.jellyplex.client.data.datasource.local

import kotlinx.coroutines.flow.StateFlow
import org.jellyplex.client.domain.models.HomeContent
import org.jellyplex.client.domain.models.MediaItem

interface IMediaLocalDataSource {
    val homeContent: StateFlow<HomeContent?>
    val movies: StateFlow<List<MediaItem>?>
    val tvShows: StateFlow<List<MediaItem>?>

    suspend fun saveHomeCache(content: HomeContent)
    suspend fun saveMoviesCache(items: List<MediaItem>)
    suspend fun saveTvShowsCache(items: List<MediaItem>)
    fun clear()
}
