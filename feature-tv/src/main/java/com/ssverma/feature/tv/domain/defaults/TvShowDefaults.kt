package com.ssverma.feature.tv.domain.defaults

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.model.MediaDetailsAppendable
import com.ssverma.shared.domain.utils.DateUtils

object TvShowDefaults {
    object DiscoverDefaults {
        fun popular() = TvDiscoverConfig.builder(sortBy = SortBy.Popularity())
            .build()

        fun nowAiring() = TvDiscoverConfig.builder(sortBy = SortBy.ReleaseDate())
            .with(DiscoverOption.AirDate.To(date = DateUtils.currentDate()))
            .build()

        fun todayAiring() = TvDiscoverConfig.builder(sortBy = SortBy.ReleaseDate())
            .with(
                DiscoverOption.AirDate.From(date = DateUtils.currentDate()),
                DiscoverOption.AirDate.To(date = DateUtils.currentDate()),
            )
            .build()

        fun upcoming() = TvDiscoverConfig
            .builder(sortBy = SortBy.ReleaseDate(order = Order.Ascending))
            .with(
                DiscoverOption.AirDate.From(date = DateUtils.currentDate().plusDays(1)),
            )
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
        )
    }

    fun tvSeasonAppendable(): List<MediaDetailsAppendable> {
        return listOf(
            MediaDetailsAppendable.Credits,
            MediaDetailsAppendable.Images,
        )
    }

    fun tvEpisodeAppendable(): List<MediaDetailsAppendable> {
        return listOf(
            MediaDetailsAppendable.Credits,
            MediaDetailsAppendable.Images,
        )
    }
}