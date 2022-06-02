package com.ssverma.feature.movie.domain.defaults

import com.ssverma.core.domain.DiscoverOption
import com.ssverma.core.domain.MovieDiscoverConfig
import com.ssverma.core.domain.Order
import com.ssverma.core.domain.SortBy
import com.ssverma.core.domain.model.MediaDetailsAppendable
import com.ssverma.core.domain.utils.DateUtils

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