package com.ssverma.feature.movie.domain.defaults

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.model.MediaDetailsAppendable
import com.ssverma.shared.domain.utils.DateUtils

object MovieDefaults {

    object DiscoverDefaults {
        fun popular() = MovieDiscoverConfig
            .builder(sortBy = SortBy.Popularity(order = Order.Descending))
            .build()

        fun inCinemas() = MovieDiscoverConfig.builder()
            .with(
                DiscoverOption.PrimaryReleaseDate.To(date = DateUtils.currentDate()),
                DiscoverOption.ReleaseType.Theatrical,
                DiscoverOption.ReleaseType.TheatricalLimited,

                )
            .sortBy(sortBy = SortBy.ReleaseDate(order = Order.Descending))
            .build()

        fun upcoming() = MovieDiscoverConfig
            .builder(sortBy = SortBy.ReleaseDate(order = Order.Ascending))
            .with(
                DiscoverOption.PrimaryReleaseDate.From(date = DateUtils.currentDate().plusDays(1)),
            )
            .build()

        fun topRated() = MovieDiscoverConfig
            .builder(sortBy = SortBy.Rating(order = Order.Descending))
            .with(DiscoverOption.VoteCount.AtLeast(100))
            .with(DiscoverOption.Rating.From(6))
            .build()
    }

    fun allMovieDetailsAppendable(): List<MediaDetailsAppendable> {
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
}
