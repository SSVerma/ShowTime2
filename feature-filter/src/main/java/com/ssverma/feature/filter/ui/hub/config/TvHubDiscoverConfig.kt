package com.ssverma.feature.filter.ui.hub.config

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.utils.DateUtils

object TvHubDiscoverConfig {

    fun heroItems(providerId: Int): TvDiscoverConfig {
        val watchProvider = DiscoverOption.WatchProvider(providerId = providerId)

        return TvDiscoverConfig.builder()
            .with(watchProvider)
            .sortBy(SortBy.Popularity(Order.Descending))
            .build()
    }

    fun newReleases(providerId: Int): TvDiscoverConfig {
        val watchProvider = DiscoverOption.WatchProvider(providerId = providerId)

        val today = DateUtils.currentDate()
        val lastWeek = today.minusWeeks(1)

        return TvDiscoverConfig.builder()
            .with(watchProvider)
            .with(DiscoverOption.AirDate.From(lastWeek))
            .with(DiscoverOption.AirDate.To(today))
            .sortBy(SortBy.AirDate(Order.Descending))
            .build()
    }

    fun upcoming(providerId: Int): TvDiscoverConfig {
        val watchProvider = DiscoverOption.WatchProvider(providerId = providerId)

        val tomorrow = DateUtils.currentDate().plusDays(1)

        return TvDiscoverConfig.builder()
            .with(watchProvider)
            .with(
                DiscoverOption.AirDate.From(tomorrow),
                DiscoverOption.AirDate.From(tomorrow),
            )
            .sortBy(SortBy.AirDate(Order.Ascending))
            .build()
    }

    fun topRated(
        providerId: Int,
        atLeastRating: Int = 7,
        atLeastVoteCount: Int = 100,
    ): TvDiscoverConfig {
        val watchProvider = DiscoverOption.WatchProvider(providerId = providerId)

        return TvDiscoverConfig.builder()
            .with(watchProvider)
            .with(DiscoverOption.Rating.From(atLeastRating))
            .with(DiscoverOption.VoteCount.AtLeast(atLeastVoteCount))
            .sortBy(SortBy.Rating(Order.Descending))
            .build()
    }
}
