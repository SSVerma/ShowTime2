package com.ssverma.shared.ui.component.media

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.domain.model.library.SavedMediaItem
import com.ssverma.shared.domain.model.movie.MoviePreview
import com.ssverma.shared.domain.model.tv.TvShowPreview

fun MoviePreview.asUniversalMediaItem(): UniversalMediaItem {
    return UniversalMediaItem(
        id = id,
        mediaType = MediaType.Movie,
        title = title,
        overview = overview,
        posterImageUrl = posterImageUrl,
        backdropImageUrl = backdropImageUrl,
        voteAvg = voteAvg,
        voteCount = voteCount,
        releaseDate = displayReleaseDate.orEmpty(),
        displayYear = displayYear
    )
}

fun TvShowPreview.asUniversalMediaItem(): UniversalMediaItem {
    return UniversalMediaItem(
        id = id,
        mediaType = MediaType.Tv,
        title = title,
        overview = overview,
        posterImageUrl = posterImageUrl,
        backdropImageUrl = backdropImageUrl,
        voteAvg = voteAvg,
        voteCount = voteCount,
        releaseDate = displayFirstAirDate.orEmpty(),
        displayYear = displayYear
    )
}

fun SavedMediaItem.asUniversalMediaItem(
    isInWatchlist: Boolean = false,
    isWatched: Boolean = false,
    isFavorite: Boolean = false
): UniversalMediaItem {
    val parsedYear = Regex("""\b(19|20)\d{2}\b""").find(releaseDate)?.value.orEmpty()
    return UniversalMediaItem(
        id = mediaId,
        mediaType = mediaType,
        title = title,
        overview = "",
        posterImageUrl = posterImageUrl,
        backdropImageUrl = backdropImageUrl,
        voteAvg = voteAvg,
        voteCount = 0,
        releaseDate = releaseDate,
        displayYear = parsedYear,
        isInWatchlist = isInWatchlist,
        isWatched = isWatched,
        isFavorite = isFavorite
    )
}
