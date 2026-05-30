package com.ssverma.feature.tv.ui.home.content

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ads.ui.rememberNativeAd
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.analytics.TvAnalyticsEvent
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.feature.tv.analytics.TvAnalyticsValues
import com.ssverma.feature.tv.domain.model.TvShowListingConfig
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.feature.tv.ui.home.HomeTvShowViewModel
import com.ssverma.feature.tv.ui.home.component.DiscoverySection
import com.ssverma.feature.tv.ui.home.component.HeroSection
import com.ssverma.feature.tv.ui.home.component.TvGenres
import com.ssverma.feature.tv.ui.list.component.TvIndicator
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.ads.injection.InjectableContent
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.ui.component.AppSection
import com.ssverma.shared.ui.component.AttributionFooter
import com.ssverma.shared.ui.component.MediaListItemShimmer
import com.ssverma.shared.ui.component.WatchProviderHubSection
import com.ssverma.shared.ui.component.media.TvShowListItem

@Composable
fun TvShowHomeContent(
    viewModel: HomeTvShowViewModel,
    openTvShowList: (TvShowListingRoute) -> Unit,
    openTvShowDetails: (Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openWatchProviderHub: (ProviderInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val analytics = LocalAnalytics.current

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            HeroSection(
                trendingTvShowsState = uiState.trendingTvShows,
                onSearchClicked = openSearchPage,
                onAccountClicked = openAccountPage,
                onTvShowClicked = { tvShow ->
                    analytics.logEvent(
                        TvAnalyticsEvent.TvShowClicked(
                            tvShow = tvShow,
                            section = TvAnalyticsValues.SECTION_TRENDING_CAROUSEL,
                            sourceScreen = TvAnalyticsScreenName.TV_HOME
                        )
                    )
                    openTvShowDetails(tvShow.id)
                },
                onWatchProviderClick = openWatchProviderHub,
                onRetry = { viewModel.fetchTrendingTvShows() },
                onAdLoaded = viewModel::onNativeAdLoaded
            )
        }

        item {
            TvGenres(
                genresUiState = uiState.genres,
                onGenreClicked = { genre ->
                    analytics.logEvent(
                        TvAnalyticsEvent.GenreClicked(
                            genre = genre,
                            sourceScreen = TvAnalyticsScreenName.TV_HOME
                        )
                    )
                    openTvShowList(
                        TvShowListingRoute(
                            args = TvShowListingArgs.ByGenre(
                                genreId = genre.id,
                                title = genre.name
                            )
                        )
                    )
                },
                onRetry = { viewModel.fetchTvGenres() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.large)
            )
        }

        item {
            WatchProviderHubSection(
                providersUiState = uiState.watchProviders,
                onProviderClick = { provider ->
                    analytics.logEvent(
                        TvAnalyticsEvent.WatchProviderClicked(
                            providerInfo = provider,
                            sourceScreen = TvAnalyticsScreenName.TV_HOME
                        )
                    )
                    openWatchProviderHub(provider)
                },
                onRetry = { viewModel.fetchWatchProviders() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
            )
        }

        item {
            ShowTimeNativeAd(
                ad = uiState.feedInlineAd,
                loadInternally = uiState.feedInlineAd == null,
                onAdLoaded = viewModel::onFeedInlineAdLoaded,
                style = NativeAdStyle.List,
                analyticsEventPrefix = "tv_home_feed_inline_native",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.medium)
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }

        item {
            DiscoverySection(
                popularTvShowsState = uiState.popularTvShows,
                topRatedTvShowsState = uiState.topRatedTvShows,
                upcomingTvShowsState = uiState.upcomingTvShows,
                onTvShowClicked = { tvShow ->
                    analytics.logEvent(
                        TvAnalyticsEvent.TvShowClicked(
                            tvShow = tvShow,
                            section = TvAnalyticsValues.SECTION_DISCOVERY,
                            sourceScreen = TvAnalyticsScreenName.TV_HOME
                        )
                    )
                    openTvShowDetails(tvShow.id)
                },
                onSeeAllClicked = { route ->
                    analytics.logEvent(
                        TvAnalyticsEvent.SeeAllClicked(
                            section = TvAnalyticsValues.SECTION_DISCOVERY,
                            sourceScreen = TvAnalyticsScreenName.TV_HOME
                        )
                    )
                    openTvShowList(route)
                },
                onFetchPopular = { viewModel.fetchPopularTvShows() },
                onFetchTopRated = { viewModel.fetchTopRatedTvShows() },
                onFetchUpcoming = { viewModel.fetchUpcomingTvShows() },
                onWatchProviderClick = openWatchProviderHub,
                onAdLoaded = viewModel::onNativeAdLoaded,
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
            )
        }

        item {
            AppSection(
                title = stringResource(R.string.airing_today),
                uiState = uiState.todayAiringTvShows,
                isVertical = true,
                onTrailingActionClicked = {
                    analytics.logEvent(
                        TvAnalyticsEvent.SeeAllClicked(
                            section = TvAnalyticsValues.SECTION_ON_THE_AIR,
                            sourceScreen = TvAnalyticsScreenName.TV_HOME
                        )
                    )
                    openTvShowList(
                        TvShowListingRoute(
                            args = TvShowListingArgs.TodayAiring(titleRes = R.string.airing_today)
                        )
                    )
                },
                onRetry = { viewModel.fetchTodayAiringTvShows() },
                loadingPlaceholder = { MediaListItemShimmer() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                content = { injectableItem: AdInjectable<TvShowPreview> ->
                    when (injectableItem) {
                        is InjectableAd -> {
                            ShowTimeNativeAd(
                                ad = injectableItem.ad,
                                onAdLoaded = { ad ->
                                    viewModel.onNativeAdLoaded(
                                        injectableItem,
                                        ad
                                    )
                                },
                                style = injectableItem.style
                            )
                        }

                        is InjectableContent<*> -> {
                            val tvShowPreview =
                                (injectableItem as InjectableContent<TvShowPreview>).item
                            TvShowListItem(
                                tvShow = tvShowPreview,
                                showRating = true,
                                onWatchProviderClick = openWatchProviderHub,
                                onClick = {
                                    analytics.logEvent(
                                        TvAnalyticsEvent.TvShowClicked(
                                            tvShow = it,
                                            section = TvAnalyticsValues.SECTION_ON_THE_AIR,
                                            sourceScreen = TvAnalyticsScreenName.TV_HOME
                                        )
                                    )
                                    openTvShowDetails(it.id)
                                },
                                indicator = {
                                    TvIndicator(
                                        config = TvShowListingConfig.Filterable.TodayAiring(),
                                        tvShow = it
                                    )
                                },
                            )
                        }
                    }
                }
            )
        }

        item {
            AppSection(
                title = stringResource(R.string.now_airing),
                uiState = uiState.nowAiringTvShows,
                isVertical = true,
                onTrailingActionClicked = {
                    analytics.logEvent(
                        TvAnalyticsEvent.SeeAllClicked(
                            section = TvAnalyticsValues.SECTION_ON_THE_AIR,
                            sourceScreen = TvAnalyticsScreenName.TV_HOME
                        )
                    )
                    openTvShowList(
                        TvShowListingRoute(
                            args = TvShowListingArgs.NowAiring(titleRes = R.string.now_airing)
                        )
                    )
                },
                onRetry = { viewModel.fetchNowAiringTvShows() },
                loadingPlaceholder = { MediaListItemShimmer() },
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                content = { injectableItem: AdInjectable<TvShowPreview> ->
                    when (injectableItem) {
                        is InjectableAd -> {
                            ShowTimeNativeAd(
                                ad = injectableItem.ad,
                                onAdLoaded = { ad ->
                                    viewModel.onNativeAdLoaded(
                                        injectableItem,
                                        ad
                                    )
                                },
                                style = injectableItem.style
                            )
                        }

                        is InjectableContent<*> -> {
                            val tvShowPreview =
                                (injectableItem as InjectableContent<TvShowPreview>).item
                            TvShowListItem(
                                tvShow = tvShowPreview,
                                showRating = true,
                                onWatchProviderClick = openWatchProviderHub,
                                onClick = {
                                    analytics.logEvent(
                                        TvAnalyticsEvent.TvShowClicked(
                                            tvShow = it,
                                            section = TvAnalyticsValues.SECTION_ON_THE_AIR,
                                            sourceScreen = TvAnalyticsScreenName.TV_HOME
                                        )
                                    )
                                    openTvShowDetails(it.id)
                                },
                                indicator = {
                                    TvIndicator(
                                        config = TvShowListingConfig.Filterable.NowAiring(),
                                        tvShow = it
                                    )
                                },
                            )
                        }
                    }
                }
            )
        }


        item { AttributionFooter(modifier = Modifier.padding(top = MaterialTheme.spacing.large)) }
    }
}
