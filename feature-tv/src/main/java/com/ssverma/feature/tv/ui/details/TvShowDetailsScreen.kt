package com.ssverma.feature.tv.ui.details

import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ads.ui.rememberNativeAd
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.HorizontalLazyListSection
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.tv.R
import com.ssverma.feature.tv.analytics.TvAnalyticsEvent
import com.ssverma.feature.tv.analytics.TvAnalyticsScreenName
import com.ssverma.feature.tv.analytics.TvAnalyticsValues
import com.ssverma.feature.tv.navigation.args.TvSeasonArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingArgs
import com.ssverma.feature.tv.navigation.args.TvShowListingRoute
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvSeason
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.domain.utils.ShareMediaUtils
import com.ssverma.shared.ui.component.ActionSize
import com.ssverma.shared.ui.component.BackdropHeader
import com.ssverma.shared.ui.component.GenreItem
import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.media.MediaItem
import com.ssverma.shared.ui.component.section.CreditSection
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.OverviewSection
import com.ssverma.shared.ui.component.section.ReviewsSection
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionContentHeaderSpacing
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionVerticalSpacing
import com.ssverma.shared.ui.component.section.TagsSection
import com.ssverma.shared.ui.component.section.VideoShotsSection
import com.ssverma.shared.ui.component.section.WatchProvidersSection
import com.ssverma.shared.ui.emptyIfAbsent

@Composable
fun TvShowDetailsScreen(
    onBackPressed: () -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: (tvShowId: Int) -> Unit,
    openPersonDetails: (Cast) -> Unit,
    openTvShowList: (listingRoute: TvShowListingRoute) -> Unit,
    openTvSeasonDetails: (seasonArgs: TvSeasonArgs) -> Unit,
    openWatchHub: (providerInfo: ProviderInfo) -> Unit,
    viewModel: TvShowDetailsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrackScreenView(screenName = TvAnalyticsScreenName.TV_DETAILS)

    Surface(color = MaterialTheme.colorScheme.background) {
        DriveCompose(
            uiState = uiState,
            onRetry = { viewModel.fetchTvShowDetails() }
        ) { data ->
            TvShowContent(
                tvShow = data.tvShow,
                viewModel = viewModel,
                uiState = data,
                onBackPressed = onBackPressed,
                openTvShowDetails = openTvShowDetails,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                openReviewsList = { openReviewsList(data.tvShow.id) },
                openYoutube = { videoId -> viewModel.openYoutubeApp(videoId = videoId) },
                openPersonDetails = openPersonDetails,
                openTvShowList = openTvShowList,
                openTvSeasonDetails = openTvSeasonDetails,
                openWatchHub = openWatchHub
            )
        }
    }
}

@Composable
private fun TvShowContent(
    tvShow: TvShow,
    viewModel: TvShowDetailsViewModel,
    uiState: TvShowDetailsData,
    onBackPressed: () -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: () -> Unit,
    openYoutube: (videoId: String) -> Unit,
    openPersonDetails: (Cast) -> Unit,
    openTvShowList: (listingRoute: TvShowListingRoute) -> Unit,
    openTvSeasonDetails: (seasonArgs: TvSeasonArgs) -> Unit,
    openWatchHub: (providerInfo: ProviderInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val watchProviderRegion by viewModel.watchProviderRegion.collectAsStateWithLifecycle()
    val analytics = LocalAnalytics.current
    val watchProviderAd = rememberNativeAd(analyticsEventPrefix = "tv_details_watch_provider")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
    ) {
        item {
            BackdropHeader(
                backdropImageUrl = tvShow.backdropImageUrl,
                onCloseIconClick = onBackPressed,
                showTrailerFab = tvShow.primaryTrailer != null,
                onTrailerFabClick = {
                    analytics.logEvent(
                        TvAnalyticsEvent.TrailerClicked(
                            tvShowId = tvShow.id,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    viewModel.onPlayTrailerClicked(tvShow)
                },
                secondaryActions = {
                    MediaStatsAction(
                        mediaType = MediaType.Tv,
                        mediaId = tvShow.id,
                        modifier = Modifier.size(ActionSize),
                        onClick = {
                            analytics.logEvent(
                                TvAnalyticsEvent.AddToStatsClicked(
                                    tvShowId = tvShow.id,
                                    sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                                )
                            )
                        }
                    )
                    FloatingActionButton(
                        onClick = {
                            analytics.logEvent(
                                TvAnalyticsEvent.ShareClicked(
                                    tvShowId = tvShow.id,
                                    sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                                )
                            )
                            val shareableText = ShareMediaUtils.buildShareableMediaText(
                                mediaTitle = tvShow.title,
                                mediaTagline = tvShow.tagline,
                                mediaOverview = tvShow.overview,
                                appPackageName = context.packageName
                            )
                            context.dispatchShareTextIntent(text = shareableText)
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(ActionSize)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null
                        )
                    }
                }
            )
        }

        /*Title*/
        item {
            Text(
                text = tvShow.title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
            )
        }

        /*Tagline*/
        tvShow.tagline?.let { tagline ->
            item {
                Emphasize {
                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 4.dp)
                    )
                }
            }
        }

        /*Highlights*/
        item {
            Highlights(
                highlights = remember(tvShow) { tvShow.highlightedItems() },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        val watchProvider = tvShow.watchProviders[watchProviderRegion]
        if (watchProvider != null && watchProvider.hasProviders) {
            item(key = "watch_providers") {
                WatchProvidersSection(
                    watchProvider = watchProvider,
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                    adContent = {
                        ShowTimeNativeAd(
                            ad = watchProviderAd,
                            loadInternally = false,
                            style = NativeAdStyle.CircularLogo,
                            modifier = Modifier.size(44.dp),
                            analyticsEventPrefix = "tv_details_watch_provider"
                        )
                    },
                    onWatchProviderClick = {
                        analytics.logEvent(
                            TvAnalyticsEvent.WatchProviderClicked(
                                providerInfo = it,
                                sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                            )
                        )
                        openWatchHub(it)
                    },
                    onJustWatchClick = {
                        analytics.logEvent(
                            TvAnalyticsEvent.JustWatchClicked(
                                sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                            )
                        )
                    }
                )
            }
        }

        /*Overview section title*/
        item {
            OverviewSection(
                overview = tvShow.overview,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = SectionVerticalSpacing)
            )
        }

        /*Genre*/
        item {
            HorizontalLazyList(
                items = tvShow.generes,
                contentPadding = PaddingValues(
                    top = SectionVerticalSpacing,
                    start = 16.dp,
                    end = 16.dp
                )
            ) { genre ->
                GenreItem(genre = genre) {
                    analytics.logEvent(
                        TvAnalyticsEvent.GenreClicked(
                            genre = genre,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
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
                }
            }
        }

        /*Seasons*/
        item {
            SeasonsSection(
                seasons = tvShow.seasons,
                onSeasonClick = { season ->
                    analytics.logEvent(
                        TvAnalyticsEvent.SeasonClicked(
                            season = season,
                            tvShowId = tvShow.id,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openTvSeasonDetails(
                        TvSeasonArgs(
                            tvShowId = viewModel.tvShowId,
                            seasonNumber = season.seasonNumber
                        )
                    )
                },
                modifier = Modifier
                    .padding(top = SectionVerticalSpacing)
            )
        }

        /*Cast*/
        item {
            CreditSection(
                casts = tvShow.casts,
                onPersonClick = { cast ->
                    analytics.logEvent(
                        TvAnalyticsEvent.CastClicked(
                            cast = cast,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openPersonDetails(cast)
                },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Image shots*/
        item {
            ImageShotsSection(
                imageShots = uiState.imageShots,
                openImageShotsList = {
                    analytics.logEvent(
                        TvAnalyticsEvent.SeeAllClicked(
                            section = TvAnalyticsValues.SECTION_SHOTS,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openImageShotsList()
                },
                openImageShot = { index ->
                    analytics.logEvent(
                        TvAnalyticsEvent.ImageShotClicked(
                            index = index,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openImageShot(index)
                },
                maxImageShots = 6,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Video shots*/
        item {
            VideoShotsSection(
                videos = tvShow.videos,
                onVideoClick = {
                    analytics.logEvent(
                        TvAnalyticsEvent.VideoClicked(
                            video = it,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openYoutube(it.key)
                },
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        /*Reviews*/
        item {
            ReviewsSection(
                reviews = tvShow.reviews,
                onReviewsViewAllClick = {
                    analytics.logEvent(
                        TvAnalyticsEvent.SeeAllClicked(
                            section = TvAnalyticsValues.SECTION_REVIEWS,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openReviewsList()
                },
                modifier = Modifier.padding(top = SectionVerticalSpacing),
                onReviewClick = {
                    analytics.logEvent(
                        TvAnalyticsEvent.ReviewClicked(
                            review = it,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                }
            )
        }

        /*Similar TV shows*/
        item {
            SimilarTvShowsSection(
                tvShows = tvShow.similarTvShows,
                sectionTitleRes = R.string.similar_shows,
                onTvShowClick = { tvShowPreview ->
                    analytics.logEvent(
                        TvAnalyticsEvent.TvShowClicked(
                            tvShowId = tvShowPreview.id,
                            tvShowTitle = tvShowPreview.title,
                            section = TvAnalyticsValues.SECTION_SIMILAR,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openTvShowDetails(tvShowPreview.id)
                },
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        /*Recommendations*/
        item {
            SimilarTvShowsSection(
                tvShows = tvShow.recommendations,
                sectionTitleRes = R.string.recommendations,
                onTvShowClick = { tvShowPreview ->
                    analytics.logEvent(
                        TvAnalyticsEvent.TvShowClicked(
                            tvShowId = tvShowPreview.id,
                            tvShowTitle = tvShowPreview.title,
                            section = TvAnalyticsValues.SECTION_RECOMMENDED,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openTvShowDetails(tvShowPreview.id)
                },
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        /*Keyword*/
        item {
            TagsSection(
                keywords = tvShow.keywords,
                onClick = { keyword ->
                    analytics.logEvent(
                        TvAnalyticsEvent.KeywordClicked(
                            keyword = keyword,
                            sourceScreen = TvAnalyticsScreenName.TV_DETAILS
                        )
                    )
                    openTvShowList(
                        TvShowListingRoute(
                            args = TvShowListingArgs.ByKeyword(
                                keywordId = keyword.id,
                                title = keyword.name
                            )
                        )
                    )
                },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Bottom spacing*/
        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SimilarTvShowsSection(
    tvShows: List<TvShow>,
    @StringRes sectionTitleRes: Int,
    onTvShowClick: (tvShow: TvShow) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalLazyListSection(
        items = tvShows,
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = sectionTitleRes),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        itemContent = {
            MediaItem(
                title = it.title,
                posterImageUrl = it.posterImageUrl,
                modifier = Modifier.width(100.dp),
                onClick = {
                    onTvShowClick(it)
                }
            )
        },
        hideIf = tvShows.isEmpty(),
        modifier = modifier
    )
}

@Composable
private fun SeasonsSection(
    seasons: List<TvSeason>,
    onSeasonClick: (season: TvSeason) -> Unit,
    modifier: Modifier = Modifier
) {
    var seasonCount by rememberSaveable {
        mutableIntStateOf(if (seasons.size < 3) seasons.size else 3)
    }

    val showSeasonViewAll by remember { derivedStateOf { seasonCount < seasons.size } }

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.seasons_n, seasons.size),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        hideIf = seasons.isEmpty(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateContentSize()
        ) {
            for (i in 0 until seasonCount) {
                TvSeasonItem(
                    tvSeason = seasons[i],
                    onClick = {
                        onSeasonClick(seasons[i])
                    }
                )
            }
            if (showSeasonViewAll) {
                TextButton(
                    onClick = { seasonCount = seasons.size },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.view_more))
                }
            }
        }
    }
}

private fun TvShow.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent()
        ),
        Highlight(
            labelRes = R.string.first_air_date,
            value = displayFirstAirDate.orEmpty(),
        ),
        Highlight(
            labelRes = R.string.status,
            value = status
        ),
        Highlight(
            labelRes = R.string.language,
            value = originalLanguage
        ),
        Highlight(
            labelRes = R.string.seasons,
            value = seasonCount.toString()
        ),
        Highlight(
            labelRes = R.string.episode_number,
            value = episodeCount.toString()
        )
    )
}
