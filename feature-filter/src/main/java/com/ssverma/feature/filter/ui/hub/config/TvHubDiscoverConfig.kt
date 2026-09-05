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
        val threeMonthsAgo = today.minusMonths(3)

        return TvDiscoverConfig.builder()
            .with(watchProvider)
            .with(DiscoverOption.AirDate.From(threeMonthsAgo))
            .with(DiscoverOption.AirDate.To(today))
            .sortBy(SortBy.AirDate(Order.Descending))
            .build()
    }

    fun upcoming(providerId: Int): TvDiscoverConfig {
        val watchProvider = DiscoverOption.WatchProvider(providerId = providerId)

        val tomorrow = DateUtils.currentDate().plusDays(1)
        val oneYearAhead = tomorrow.plusYears(1)

        return TvDiscoverConfig.builder()
            .with(watchProvider)
            .with(DiscoverOption.AirDate.From(tomorrow))
            .with(DiscoverOption.AirDate.To(oneYearAhead))
            .sortBy(SortBy.AirDate(Order.Ascending))
            .build()
    }

    fun topRated(
        providerId: Int,
        atLeastRating: Int = 7,
        atLeastVoteCount: Int = 50,
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
