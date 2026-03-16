package com.ssverma.feature.movie.domain.defaults

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.model.MediaDetailsAppendable
import com.ssverma.shared.domain.utils.DateUtils

object MovieDefaults {
    val DefaultMovieReleaseType = DiscoverOption.ReleaseType.Theatrical

    object DiscoverDefaults {
        fun popular(originalLanguage: String? = null) =
            MovieDiscoverConfig.builder(sortBy = SortBy.Popularity())
                .with(DefaultMovieReleaseType)
                .apply {
                    originalLanguage?.let { with(DiscoverOption.OriginalLanguage(it)) }
                }
                .build()

        fun inCinemas(originalLanguage: String? = null) =
            MovieDiscoverConfig.builder(sortBy = SortBy.ReleaseDate())
                .with(DiscoverOption.PrimaryReleaseDate.To(date = DateUtils.currentDate()))
                .apply {
                    originalLanguage?.let { with(DiscoverOption.OriginalLanguage(it)) }
                }
                .build()

        fun upcoming(originalLanguage: String? = null) = MovieDiscoverConfig
            .builder(sortBy = SortBy.ReleaseDate(order = Order.Ascending))
            .with(
                DefaultMovieReleaseType,
                DiscoverOption.PrimaryReleaseDate.From(date = DateUtils.currentDate().plusDays(1)),
            )
            .apply {
                originalLanguage?.let { with(DiscoverOption.OriginalLanguage(it)) }
            }
            .build()

        fun topRated(region: String? = null, originalLanguage: String? = null) = MovieDiscoverConfig
            .builder(sortBy = SortBy.Rating(Order.Descending))
            .apply {
                region?.let { with(DiscoverOption.WatchRegion(it)) }
                originalLanguage?.let { with(DiscoverOption.OriginalLanguage(it)) }
            }
            .build()

        fun discoveryBy(
            watchProviderId: Int? = null,
            watchRegion: String? = null,
            originalLanguage: String? = null,
            sortBy: SortBy = SortBy.Popularity()
        ) = MovieDiscoverConfig.builder(sortBy = sortBy)
            .apply {
                watchProviderId?.let { with(DiscoverOption.WatchProvider(it)) }
                watchRegion?.let { with(DiscoverOption.WatchRegion(it)) }
                originalLanguage?.let { with(DiscoverOption.OriginalLanguage(it)) }
            }
            .build()

        fun watchProviderNew(watchProviderId: Int): MovieDiscoverConfig {
            val today = DateUtils.currentDate()
            val lastWeek = today.minusWeeks(1)

            return MovieDiscoverConfig.builder()
                .with(DiscoverOption.WatchProvider(watchProviderId))
                .with(DiscoverOption.PrimaryReleaseDate.From(lastWeek))
                .with(DiscoverOption.PrimaryReleaseDate.To(today))
                .sortBy(SortBy.ReleaseDate(Order.Descending))
                .build()
        }

        fun watchProviderUpcoming(watchProviderId: Int): MovieDiscoverConfig {
            val tomorrow = DateUtils.currentDate().plusDays(1)

            return MovieDiscoverConfig.builder()
                .with(DiscoverOption.WatchProvider(watchProviderId))
                .with(DiscoverOption.ReleaseDate.From(tomorrow))
                .sortBy(SortBy.ReleaseDate(Order.Ascending))
                .build()
        }

        fun watchProviderTopRated(watchProviderId: Int): MovieDiscoverConfig {
            return MovieDiscoverConfig.builder()
                .with(DiscoverOption.WatchProvider(watchProviderId))
                .sortBy(SortBy.Rating(Order.Descending))
                .build()
        }
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