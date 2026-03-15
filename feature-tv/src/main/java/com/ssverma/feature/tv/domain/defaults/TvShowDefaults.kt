package com.ssverma.feature.tv.domain.defaults

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.MediaDetailsAppendable
import com.ssverma.shared.domain.utils.DateUtils

object TvShowDefaults {
    object DiscoverDefaults {
        fun popular(watchRegion: String? = null, originalLanguage: String? = null) =
            TvDiscoverConfig.builder(sortBy = SortBy.Popularity())
                .apply {
                    watchRegion?.let { with(DiscoverOption.WatchRegion(iso3 = it)) }
                    originalLanguage?.let { with(DiscoverOption.OriginalLanguage(iso2 = it)) }
                }
                .build()

        fun nowAiring(watchRegion: String? = null, originalLanguage: String? = null) =
            TvDiscoverConfig.builder(sortBy = SortBy.ReleaseDate())
                .with(DiscoverOption.AirDate.To(date = DateUtils.currentDate()))
                .apply {
                    watchRegion?.let { with(DiscoverOption.WatchRegion(iso3 = it)) }
                    originalLanguage?.let { with(DiscoverOption.OriginalLanguage(iso2 = it)) }
                }
                .build()

        fun todayAiring(watchRegion: String? = null, originalLanguage: String? = null) =
            TvDiscoverConfig.builder(sortBy = SortBy.ReleaseDate())
                .with(
                    DiscoverOption.AirDate.From(date = DateUtils.currentDate()),
                    DiscoverOption.AirDate.To(date = DateUtils.currentDate()),
                )
                .apply {
                    watchRegion?.let { with(DiscoverOption.WatchRegion(iso3 = it)) }
                    originalLanguage?.let { with(DiscoverOption.OriginalLanguage(iso2 = it)) }
                }
                .build()

        fun upcoming(watchRegion: String? = null, originalLanguage: String? = null) =
            TvDiscoverConfig
                .builder(sortBy = SortBy.ReleaseDate(order = Order.Ascending))
                .with(
                    DiscoverOption.AirDate.From(date = DateUtils.currentDate().plusDays(1)),
                )
                .apply {
                    watchRegion?.let { with(DiscoverOption.WatchRegion(iso3 = it)) }
                    originalLanguage?.let { with(DiscoverOption.OriginalLanguage(iso2 = it)) }
                }
                .build()

        fun topRated(watchRegion: String? = null, originalLanguage: String? = null) =
            TvDiscoverConfig.builder(sortBy = SortBy.Rating())
                .with(
                    DiscoverOption.Rating.VoteCount(200),
                    DiscoverOption.WithoutGenre(10770), // TV Movie
                    DiscoverOption.WithoutGenre(99), // Documentary
                    DiscoverOption.WithoutGenre(10763), // News
                    DiscoverOption.WithoutGenre(10764), // Reality
                    DiscoverOption.WithoutGenre(10767) // Talk
                )
                .apply {
                    watchRegion?.let { with(DiscoverOption.WatchRegion(iso3 = it)) }
                    originalLanguage?.let { with(DiscoverOption.OriginalLanguage(iso2 = it)) }
                }
                .build()
    }

    fun allTvShowDetailsAppendable(): List<MediaDetailsAppendable> {
        return listOf(
            MediaDetailsAppendable.Keywords,
            MediaDetailsAppendable.Credits,
            MediaDetailsAppendable.Images,
            MediaDetailsAppendable.Videos,
            MediaDetailsAppendable.Lists,
            MediaDetailsAppendable.Reviews,
            MediaDetailsAppendable.Similar,
            MediaDetailsAppendable.Recommendations,
            MediaDetailsAppendable.WatchProviders,
        )
    }

    fun tvSeasonAppendable(): List<MediaDetailsAppendable> {
        return listOf(
            MediaDetailsAppendable.Credits,
            MediaDetailsAppendable.Images,
            MediaDetailsAppendable.WatchProviders,
        )
    }

    fun tvEpisodeAppendable(): List<MediaDetailsAppendable> {
        return listOf(
            MediaDetailsAppendable.Credits,
            MediaDetailsAppendable.Images,
        )
    }
}