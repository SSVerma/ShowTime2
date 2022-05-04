package com.ssverma.showtime.domain.defaults.movie

import com.ssverma.showtime.domain.DiscoverOption
import com.ssverma.showtime.domain.MovieDiscoverConfig
import com.ssverma.showtime.domain.Order
import com.ssverma.showtime.domain.SortBy
import com.ssverma.showtime.domain.model.MediaDetailsAppendable
import com.ssverma.showtime.utils.DateUtils

object MovieDefaults {
    val DefaultMovieReleaseType = DiscoverOption.ReleaseType.Theatrical

    object DiscoverDefaults {
        fun popular() = MovieDiscoverConfig.builder(sortBy = SortBy.Popularity())
            .with(DefaultMovieReleaseType)
            .build()

        fun inCinemas() = MovieDiscoverConfig.builder(sortBy = SortBy.ReleaseDate())
            .with(DiscoverOption.ReleaseDate.To(date = DateUtils.currentDate()))
            .build()

        fun upcoming() = MovieDiscoverConfig
            .builder(sortBy = SortBy.ReleaseDate(order = Order.Ascending))
            .with(
                DefaultMovieReleaseType,
                DiscoverOption.ReleaseDate.From(date = DateUtils.currentDate().plusDays(1)),
            )
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
        )
    }
}