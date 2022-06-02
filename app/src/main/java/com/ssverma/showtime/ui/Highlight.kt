package com.ssverma.showtime.ui

import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.emptyIfAbsent
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.tv.TvEpisode
import com.ssverma.showtime.domain.model.tv.TvSeason
import com.ssverma.showtime.domain.model.tv.TvShow

fun TvShow.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent()
        ),
        Highlight(
            labelRes = R.string.first_air_date,
            value = displayFirstAirDate.orEmpty(),
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
            value = displayAirDate.orEmpty()
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
            value = displayAirDate.orEmpty()
        ),
    )
}