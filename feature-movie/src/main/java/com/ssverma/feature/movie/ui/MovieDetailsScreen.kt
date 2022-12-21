package com.ssverma.feature.movie.ui

import MediaItem
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchShareTextIntent
import com.ssverma.core.ui.DriveCompose
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.HorizontalLazyListSection
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.feature.account.ui.stats.MediaStatsAction
import com.ssverma.feature.movie.R
import com.ssverma.feature.movie.navigation.args.MovieListingArgs
import com.ssverma.feature.movie.navigation.args.MovieListingAvailableTypes
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.ShareMediaUtils
import com.ssverma.shared.ui.component.*
import com.ssverma.shared.ui.component.section.*
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionVerticalSpacing
import com.ssverma.shared.ui.emptyIfAbsent

@Composable
fun MovieDetailsScreen(
    viewModel: MovieDetailsViewModel,
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: (movieId: Int) -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
) {
    Surface(
        color = MaterialTheme.colors.background
    ) {
        DriveCompose(
            uiState = viewModel.movieDetailsUiState,
            onRetry = {
                viewModel.fetchMovieDetails()
            }
        ) { movie ->
            MovieContent(
                movie = movie,
                viewModel = viewModel,
                onBackPressed = onBackPressed,
                openMovieDetails = openMovieDetails,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                openReviewsList = { openReviewsList(movie.id) },
                openYoutube = { videoId ->
                    viewModel.openYoutubeApp(videoId = videoId)
                },
                openPersonDetails = openPersonDetails,
                openMovieList = openMovieList
            )
        }
    }
}

@Composable
fun MovieContent(
    movie: Movie,
    viewModel: MovieDetailsViewModel,
    onBackPressed: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openImageShotsList: () -> Unit,
    openImageShot: (pageIndex: Int) -> Unit,
    openReviewsList: () -> Unit,
    openYoutube: (videoId: String) -> Unit,
    openPersonDetails: (personId: Int) -> Unit,
    openMovieList: (listingArgs: MovieListingArgs) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {

        /*Backdrop*/
        item {
            BackdropHeader(
                backdropImageUrl = movie.backdropImageUrl,
                onCloseIconClick = onBackPressed,
                onTrailerFabClick = { viewModel.onPlayTrailerClicked(movie) },
                secondaryActions = {
                    MediaStatsAction(
                        mediaType = MediaType.Movie,
                        mediaId = movie.id
                    )
                    FloatingActionButton(
                        onClick = {
                            val shareableText = ShareMediaUtils.buildShareableMediaText(
                                mediaTitle = movie.title,
                                mediaTagline = movie.tagline,
                                mediaOverview = movie.overview,
                                appPackageName = context.packageName
                            )

                            context.dispatchShareTextIntent(text = shareableText)
                        },
                        backgroundColor = MaterialTheme.colors.surface,
                        modifier = modifier.size(ActionSize)
                    ) {
                        Icon(imageVector = AppIcons.Send, contentDescription = null)
                    }
                }
            )
        }

        /*Title*/
        item {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.h5,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
            )
        }

        /*Tagline*/
        movie.tagline?.let { tagline ->
            item {
                Emphasize {
                    Text(
                        text = tagline,
                        style = MaterialTheme.typography.caption,
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
                highlights = remember(movie) { movie.highlightedItems() },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Overview section*/
        item {
            OverviewSection(
                overview = movie.overview,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = SectionVerticalSpacing)
            )
        }

        /*Genre*/
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
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.Genre,
                            title = genre.name,
                            genreId = genre.id
                        )
                    )
                }
            }
        }

        /*Cast*/
        item {
            CreditSection(
                casts = movie.casts,
                onPersonClick = openPersonDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Image shots*/
        item {
            ImageShotsSection(
                imageShots = viewModel.imageShots,
                openImageShotsList = openImageShotsList,
                openImageShot = openImageShot,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Video shots*/
        item {
            VideoShotsSection(
                videos = movie.videos,
                onVideoClick = { openYoutube(it.key) },
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Reviews*/
        item {
            ReviewsSection(
                reviews = movie.reviews,
                onReviewsViewAllClick = openReviewsList,
                modifier = Modifier.padding(top = SectionVerticalSpacing)
            )
        }

        /*Similar movies*/
        item {
            RelevantMoviesSection(
                movies = movie.similarMovies,
                sectionTitleRes = R.string.similar_movies,
                openMovieDetails = openMovieDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        /*Recommendations*/
        item {
            RelevantMoviesSection(
                movies = movie.recommendations,
                sectionTitleRes = R.string.recommendations,
                openMovieDetails = openMovieDetails,
                modifier = Modifier.padding(top = SectionVerticalSpacing),
            )
        }

        /*Keyword*/
        item {
            TagsSection(
                keywords = movie.keywords,
                onClick = { keyword ->
                    openMovieList(
                        MovieListingArgs(
                            listingType = MovieListingAvailableTypes.Keyword,
                            title = keyword.name,
                            keywordId = keyword.id
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
fun RelevantMoviesSection(
    movies: List<Movie>,
    @StringRes sectionTitleRes: Int,
    openMovieDetails: (movieId: Int) -> Unit,
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
                onClick = {
                    openMovieDetails(it.id)
                }
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
            value = if (runtime == 0) runtime.emptyIfAbsent() else DateUtils.formatMinutes(runtime)
        ),
        Highlight(
            labelRes = R.string.revenue,
            value = if (revenue == 0L) revenue.emptyIfAbsent() else "$$revenue"
        )
    )
}