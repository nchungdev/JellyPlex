package org.jellyplus.client.ui.home

internal const val HomeSectionIdHero = "hero"
internal const val HomeSectionIdContinue = "continue"
internal const val HomeSectionIdRecent = "recent"
internal const val HomeSectionIdGenreRows = "genreRows"
internal const val HomeSectionIdMovies = "movies"
internal const val HomeSectionIdTv = "tv"
internal const val HomeSectionIdGenres = "genres"

internal val DefaultHomeSectionIds = listOf(
    HomeSectionIdHero,
    HomeSectionIdContinue,
    HomeSectionIdRecent,
    HomeSectionIdGenreRows,
    HomeSectionIdMovies,
    HomeSectionIdTv,
    HomeSectionIdGenres,
)

internal val HomeSectionLabels = mapOf(
    HomeSectionIdHero to "Hero banner",
    HomeSectionIdContinue to "Continue watching",
    HomeSectionIdRecent to "Recently added",
    HomeSectionIdGenreRows to "Genre rows",
    HomeSectionIdMovies to "Movies",
    HomeSectionIdTv to "TV Series",
    HomeSectionIdGenres to "Genres",
)

internal fun parseHomeSectionIds(value: String): List<String> =
    value.split(',')
        .map { it.trim() }
        .filter { it in DefaultHomeSectionIds }
        .distinct()

internal fun orderedHomeSectionIds(value: String): List<String> {
    val saved = parseHomeSectionIds(value)
    return saved + DefaultHomeSectionIds.filterNot { it in saved }
}
