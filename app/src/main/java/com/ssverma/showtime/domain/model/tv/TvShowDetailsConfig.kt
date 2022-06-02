package com.ssverma.showtime.domain.model.tv

import com.ssverma.showtime.domain.defaults.tv.TvShowDefaults
import com.ssverma.core.domain.model.MediaDetailsAppendable

data class TvShowDetailsConfig(
    val tvShowId: Int,
    val appendable: List<MediaDetailsAppendable> = TvShowDefaults.allTvShowDetailsAppendable()
)

data class TvSeasonConfig(
    val tvShowId: Int,
    val seasonNumber: Int,
    val appendable: List<MediaDetailsAppendable> = TvShowDefaults.tvSeasonAppendable()
)

data class TvEpisodeConfig(
    val tvShowId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val appendable: List<MediaDetailsAppendable> = TvShowDefaults.tvEpisodeAppendable()
)