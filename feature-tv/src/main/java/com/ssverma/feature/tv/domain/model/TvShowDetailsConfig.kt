package com.ssverma.feature.tv.domain.model

import com.ssverma.shared.domain.model.MediaDetailsAppendable
import com.ssverma.feature.tv.domain.defaults.TvShowDefaults

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