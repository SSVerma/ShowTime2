package com.ssverma.showtime.ui

import androidx.annotation.StringRes
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.movie.Movie
import com.ssverma.showtime.domain.model.tv.TvEpisode
import com.ssverma.showtime.domain.model.tv.TvSeason
import com.ssverma.showtime.domain.model.tv.TvShow
import com.ssverma.showtime.extension.emptyIfAbsent
import com.ssverma.showtime.extension.emptyIfNull
import com.ssverma.showtime.utils.DateUtils

data class Highlight(
    @StringRes val labelRes: Int,
    val value: String
)

fun Movie.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent()
        ),
        Highlight(
            labelRes = R.string.release_date,
            value = displayReleaseDate.emptyIfNull(),
        ),
        Highlight(
            labelRes = R.string.status,
            value = status
        ),
        Highlight(
            labelRes = R.string.language,
            value = originalLanguage
        ),
        Highlight(
            labelRes = R.string.runtime,
            value = if (runtime == 0) runtime.emptyIfAbsent() else DateUtils.formatMinutes(runtime)
        ),
        Highlight(
            labelRes = R.string.revenue,
            value = if (revenue == 0L) revenue.emptyIfAbsent() else "$$revenue"
        )
    )
}

fun TvShow.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent()
        ),
        Highlight(
            labelRes = R.string.first_air_date,
            value = displayFirstAirDate.emptyIfNull(),
        ),
        Highlight(
            labelRes = R.string.status,
            value = status
        ),
        Highlight(
            labelRes = R.string.language,
            value = originalLanguage
        ),
        Highlight(
            labelRes = R.string.seasons,
            value = seasonCount.toString()
        ),
        Highlight(
            labelRes = R.string.episode_number,
            value = episodeCount.toString()
        )
    )
}

fun TvSeason.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.season_number,
            value = seasonNumber.toString()
        ),
        Highlight(
            labelRes = R.string.episodes,
            value = episodes.size.toString()
        ),
        Highlight(
            labelRes = R.string.air_date,
            value = displayAirDate.emptyIfNull()
        ),
    )
}

fun TvEpisode.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.season_number,
            value = seasonNumber.toString()
        ),
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.toString()
        ),
        Highlight(
            labelRes = R.string.air_date,
            value = displayAirDate.emptyIfNull()
        ),
    )
}