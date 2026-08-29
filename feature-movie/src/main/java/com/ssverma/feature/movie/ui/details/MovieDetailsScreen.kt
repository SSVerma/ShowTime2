package com.ssverma.feature.movie.ui.details

import androidx.annotation.StringRes
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.ssverma.core.ui.component.ShowTimeSnackbarHost
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.HorizontalLazyListSection
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.library.navigation.LibraryHomeNavKey
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.analytics.MovieAnalyticsEvent
import com.ssverma.feature.movie.analytics.MovieAnalyticsScreenName
import com.ssverma.feature.movie.analytics.MovieAnalyticsValues
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.shared.ads.native.ShowTimeNativeAd
import com.ssverma.shared.ads.ui.NativeAdStyle
import com.ssverma.shared.domain.model.Cast
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.ShareMediaUtils
import com.ssverma.shared.ui.R as SharedR
import com.ssverma.shared.ui.component.section.MediaDiscussionsSection
import com.ssverma.shared.ui.component.ActionSize
import com.ssverma.shared.ui.component.BackdropActionButton
import com.ssverma.shared.ui.component.BackdropHeader
import com.ssverma.shared.ui.component.GenreItem
import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.media.MediaItem
import com.ssverma.shared.ui.component.section.CreditSection
import com.ssverma.shared.ui.component.section.ImageShotsSection
import com.ssverma.shared.ui.component.section.MediaReactionsSection
import com.ssverma.shared.ui.component.section.OverviewSection
import com.ssverma.shared.ui.component.section.ReviewsSection
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionVerticalSpacing
import com.ssverma.shared.ui.component.section.TagsSection
import com.ssverma.shared.ui.component.section.VideoShotsSection
import com.ssverma.shared.ui.component.section.WatchProvidersSection
import com.ssverma.shared.ui.emptyIfAbsent
import kotlinx.coroutines.launch

@Composable
fun MovieDetailsScreen(
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: (movieId: Int) -> Unit,
    openDiscussionsList: (movieId: Int, movieTitle: String?, posterImageUrl: String?, backdropImageUrl: String?) -> Unit = { _, _, _, _ -> },
    openPersonDetails: (Cast) -> Unit,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
    openWatchHub: (providerInfo: ProviderInfo) -> Unit,
    openLibraryPage: (LibraryHomeNavKey) -> Unit = {},
    viewModel: MovieDetailsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrackScreenView(screenName = MovieAnalyticsScreenName.MOVIE_DETAILS)

    Surface(color = MaterialTheme.colorScheme.background) {
        DriveCompose(
            uiState = uiState,
            onRetry = { viewModel.fetchMovieDetails() }
        ) { data ->
            MovieContent(
                data = data,
                viewModel = viewModel,
                onBackPressed = onBackPressed,
                openMovieDetails = openMovieDetails,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                openReviewsList = { openReviewsList(data.movie.id) },
                openDiscussionsList = {
                    openDiscussionsList(
                        data.movie.id,
                        data.movie.title,
                        data.movie.posterImageUrl,
                        data.movie.backdropImageUrl
                    )
                },
                openYoutube = { videoId ->
                    viewModel.openYoutubeApp(videoId = videoId)
                },
                openPersonDetails = openPersonDetails,
                openMovieList = openMovieList,
                openWatchHub = openWatchHub,
                openLibraryPage = openLibraryPage,
            )
        }
    }
}

@Composable
fun MovieContent(
    data: MovieDetailsData,
    viewModel: MovieDetailsViewModel,
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: () -> Unit,
    openDiscussionsList: () -> Unit,
    openYoutube: (videoId: String) -> Unit,
    openPersonDetails: (Cast) -> Unit,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
    openWatchHub: (providerInfo: ProviderInfo) -> Unit,
    openLibraryPage: (LibraryHomeNavKey) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val movie = data.movie
    val context = LocalContext.current
    val watchProviderRegion by viewModel.watchProviderRegion.collectAsStateWithLifecycle()
    val mediaReactions by viewModel.mediaReactions.collectAsStateWithLifecycle()
    val discussions by viewModel.discussions.collectAsStateWithLifecycle()
    val analytics = LocalAnalytics.current
    val watchProviderAd = rememberNativeAd(analyticsEventPrefix = "movie_details_watch_provider")
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { ShowTimeSnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                BackdropHeader(
                    backdropImageUrl = movie.backdropImageUrl,
                    onCloseIconClick = onBackPressed,
                    showTrailerFab = movie.primaryTrailer != null,
                    onTrailerFabClick = {
                        analytics.logEvent(
                            MovieAnalyticsEvent.TrailerClicked(
                                movieId = movie.id,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                        viewModel.onPlayTrailerClicked(movie)
                    },
                    secondaryActions = {
                        MediaStatsAction(
                            mediaType = MediaType.Movie,
                            mediaId = movie.id,
                            title = movie.title,
                            posterImageUrl = movie.posterImageUrl,
                            backdropImageUrl = movie.backdropImageUrl,
                            voteAvg = movie.voteAvg,
                            releaseDate = movie.releaseDate?.toString().orEmpty(),
                            triggerIcon = Icons.Rounded.Add,
                            onShowFeedback = { message, actionLabel, destination ->
                                coroutineScope.launch {
                                    val result = snackbarHostState.showImmediateSnackbar(
                                        message = message,
                                        actionLabel = actionLabel,
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        openLibraryPage(destination ?: LibraryHomeNavKey.Default)
                                    }
                                }
                            },
                            modifier = Modifier.size(ActionSize),
                            onClick = {
                                analytics.logEvent(
                                    MovieAnalyticsEvent.AddToStatsClicked(
                                        movieId = movie.id,
                                        sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                                    )
                                )
                            }
                        )
                        BackdropActionButton(
                            onClick = {
                                analytics.logEvent(
                                    MovieAnalyticsEvent.ShareClicked(
                                        movieId = movie.id,
                                        sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                                    )
                                )
                                val shareableText = ShareMediaUtils.buildShareableMediaText(
                                    mediaTitle = movie.title,
                                    mediaTagline = movie.tagline,
                                    mediaOverview = movie.overview,
                                    appPackageName = context.packageName
                                )
                                context.dispatchShareTextIntent(text = shareableText)
                            },
                            icon = Icons.Rounded.Share,
                            contentDescription = stringResource(id = SharedR.string.share)
                        )
                        BackdropActionButton(
                            onClick = openDiscussionsList,
                            icon = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = stringResource(id = SharedR.string.discussions)
                        )
                    }
                )
            }

            item {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 24.dp)
                )
            }

            movie.tagline?.let { tagline ->
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

            item {
                Highlights(
                    highlights = remember(movie) { movie.highlightedItems() },
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            val watchProvider = movie.watchProviders[watchProviderRegion]
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
                                analyticsEventPrefix = "movie_details_watch_provider"
                            )
                        },
                        onWatchProviderClick = { providerInfo ->
                            analytics.logEvent(
                                MovieAnalyticsEvent.WatchProviderClicked(
                                    providerInfo = providerInfo,
                                    sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                                )
                            )
                            openWatchHub(providerInfo)
                        },
                        onJustWatchClick = {
                            analytics.logEvent(
                                MovieAnalyticsEvent.JustWatchClicked(
                                    sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                                )
                            )
                        }
                    )
                }
            }

            item(key = "community_reactions") {
                MediaReactionsSection(
                    reactions = mediaReactions,
                    onTagClick = { tag ->
                        viewModel.onReactionTagClicked(tag = tag)
                    },
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            item {
                OverviewSection(
                    overview = movie.overview,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = SectionVerticalSpacing)
                )
            }

            item {
                HorizontalLazyList(
                    items = movie.generes,
                    contentPadding = PaddingValues(
                        top = SectionVerticalSpacing,
                        start = 16.dp,
                        end = 16.dp
                    )
                ) { genre ->
                    GenreItem(genre = genre) {
                        analytics.logEvent(
                            MovieAnalyticsEvent.GenreClicked(
                                genre = genre,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                        openMovieList(
                            MovieListingArgs.ByGenre(
                                title = genre.name,
                                genreId = genre.id
                            )
                        )
                    }
                }
            }

            item {
                CreditSection(
                    casts = movie.casts,
                    onPersonClick = { cast ->
                        analytics.logEvent(
                            MovieAnalyticsEvent.CastClicked(
                                cast = cast,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                        openPersonDetails(cast)
                    },
                    source = "movie_credit",
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            item {
                ImageShotsSection(
                    imageShots = data.imageShots,
                    openImageShotsList = {
                        analytics.logEvent(
                            MovieAnalyticsEvent.SeeAllClicked(
                                section = MovieAnalyticsValues.SECTION_SHOTS,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                        openImageShotsList()
                    },
                    openImageShot = { index ->
                        analytics.logEvent(
                            MovieAnalyticsEvent.ImageShotClicked(
                                index = index,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                        openImageShot(index)
                    },
                    maxImageShots = 6,
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                )
            }

            item {
                VideoShotsSection(
                    videos = movie.videos,
                    onVideoClick = { video ->
                        analytics.logEvent(
                            MovieAnalyticsEvent.VideoClicked(
                                video = video,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                        openYoutube(video.key)
                    },
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                )
            }

            item(key = "media_discussions") {
                MediaDiscussionsSection(
                    discussions = discussions,
                    onDiscussionsViewAllClick = openDiscussionsList,
                    onPostComment = viewModel::postComment,
                    onEditComment = viewModel::editComment,
                    onReportComment = viewModel::reportComment,
                    onToggleUpvote = viewModel::toggleCommentUpvote,
                    onDeleteComment = viewModel::deleteComment,
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            item {
                ReviewsSection(
                    reviews = movie.reviews,
                    onReviewsViewAllClick = {
                        analytics.logEvent(
                            MovieAnalyticsEvent.SeeAllClicked(
                                section = MovieAnalyticsValues.SECTION_REVIEWS,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                        openReviewsList()
                    },
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                    onReviewClick = {
                        analytics.logEvent(
                            MovieAnalyticsEvent.ReviewClicked(
                                review = it,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                    }
                )
            }

            item {
                RelevantMoviesSection(
                    movies = movie.similarMovies,
                    sectionTitleRes = R.string.similar_movies,
                    onMovieClick = { moviePreview ->
                        analytics.logEvent(
                            MovieAnalyticsEvent.MovieClicked(
                                movieId = moviePreview.id,
                                movieTitle = moviePreview.title,
                                section = MovieAnalyticsValues.SECTION_SIMILAR,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS,
                            )
                        )
                        openMovieDetails(moviePreview.id)
                    },
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                )
            }

            item {
                RelevantMoviesSection(
                    movies = movie.recommendations,
                    sectionTitleRes = R.string.recommendations,
                    onMovieClick = { moviePreview ->
                        analytics.logEvent(
                            MovieAnalyticsEvent.MovieClicked(
                                movieId = moviePreview.id,
                                movieTitle = moviePreview.title,
                                section = MovieAnalyticsValues.SECTION_RECOMMENDED,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS,
                            )
                        )
                        openMovieDetails(moviePreview.id)
                    },
                    modifier = Modifier.padding(top = SectionVerticalSpacing),
                )
            }

            item {
                TagsSection(
                    keywords = movie.keywords,
                    onClick = { keyword ->
                        analytics.logEvent(
                            MovieAnalyticsEvent.KeywordClicked(
                                keyword = keyword,
                                sourceScreen = MovieAnalyticsScreenName.MOVIE_DETAILS
                            )
                        )
                        openMovieList(
                            MovieListingArgs.ByKeyword(
                                title = keyword.name,
                                keywordId = keyword.id
                            )
                        )
                    },
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                )
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}

@Composable
fun RelevantMoviesSection(
    movies: List<Movie>,
    @StringRes sectionTitleRes: Int,
    onMovieClick: (movie: Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalLazyListSection(
        items = movies,
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
                onClick = { onMovieClick(it) }
            )
        },
        hideIf = movies.isEmpty(),
        modifier = modifier
    )
}

private fun Movie.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent()
        ),
        Highlight(
            labelRes = R.string.release_date,
            value = displayReleaseDate.orEmpty(),
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
            labelRes = R.string.runtime,
            value = if (runtime == 0) 0.emptyIfAbsent() else DateUtils.formatMinutes(runtime)
        ),
        Highlight(
            labelRes = R.string.revenue,
            value = if (revenue == 0L) 0.emptyIfAbsent() else "$$revenue"
        )
    )
}
