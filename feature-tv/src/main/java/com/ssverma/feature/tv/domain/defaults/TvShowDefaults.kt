package com.ssverma.feature.tv.domain.defaults

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.MediaDetailsAppendable
import com.ssverma.shared.domain.utils.DateUtils

object TvShowDefaults {
    object DiscoverDefaults {

        fun popular() = TvDiscoverConfig
            .builder(sortBy = SortBy.Popularity(order = Order.Descending))
            .build()

        fun nowAiring(): TvDiscoverConfig {
            val today = DateUtils.currentDate()

            return TvDiscoverConfig
                .builder(sortBy = SortBy.AirDate(order = Order.Descending))
                .with(
                    DiscoverOption.AirDate.To(date = today),
                    DiscoverOption.AirDate.To(date = today),
                )
                .build()
        }

        fun todayAiring() = TvDiscoverConfig.builder(sortBy = SortBy.Popularity())
            .with(
                DiscoverOption.AirDate.From(date = DateUtils.currentDate()),
                DiscoverOption.AirDate.To(date = DateUtils.currentDate()),
            )
            .build()

        fun upcoming(): TvDiscoverConfig {
            val tomorrow = DateUtils.currentDate().plusDays(1)

            return TvDiscoverConfig
                .builder(sortBy = SortBy.AirDate(order = Order.Ascending))
                .with(
                    DiscoverOption.FirstAirDate.From(date = tomorrow),
                )
                .build()
        }

        fun topRated() = TvDiscoverConfig
            .builder(sortBy = SortBy.Rating(order = Order.Descending))
            .with(DiscoverOption.VoteCount.AtLeast(100))
            .with(DiscoverOption.Rating.From(6))
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