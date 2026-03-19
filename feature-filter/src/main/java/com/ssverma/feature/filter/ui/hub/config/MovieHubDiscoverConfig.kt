package com.ssverma.feature.filter.ui.hub.config

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.utils.DateUtils

object MovieHubDiscoverConfig {

    fun heroItems(providerId: Int): MovieDiscoverConfig {
        val watchProvider = DiscoverOption.WatchProvider(providerId = providerId)

        return MovieDiscoverConfig.builder()
            .with(watchProvider)
            .sortBy(SortBy.Popularity(Order.Descending))
            .build()
    }

    fun newReleases(providerId: Int): MovieDiscoverConfig {
        val watchProvider = DiscoverOption.WatchProvider(providerId = providerId)

        val today = DateUtils.currentDate()
        val lastWeek = today.minusWeeks(1)

        return MovieDiscoverConfig.builder()
            .with(watchProvider)
            .with(DiscoverOption.PrimaryReleaseDate.From(lastWeek))
            .with(DiscoverOption.PrimaryReleaseDate.To(today))
            .sortBy(SortBy.ReleaseDate(Order.Descending))
            .build()
    }

    fun upcoming(providerId: Int): MovieDiscoverConfig {
        val watchProvider = DiscoverOption.WatchProvider(providerId = providerId)

        val tomorrow = DateUtils.currentDate().plusDays(1)

        return MovieDiscoverConfig.builder()
            .with(watchProvider)
            .with(DiscoverOption.PrimaryReleaseDate.From(tomorrow))
            .sortBy(SortBy.ReleaseDate(Order.Ascending))
            .build()
    }

    fun topRated(
        providerId: Int,
        atLeastRating: Int = 7,
        atLeastVoteCount: Int = 100,
    ): MovieDiscoverConfig {
        val watchProvider = DiscoverOption.WatchProvider(providerId = providerId)

        return MovieDiscoverConfig.builder()
            .with(watchProvider)
            .with(DiscoverOption.Rating.From(atLeastRating))
            .with(DiscoverOption.VoteCount.AtLeast(atLeastVoteCount))
            .sortBy(SortBy.Rating(Order.Descending))
            .build()
    }
}
