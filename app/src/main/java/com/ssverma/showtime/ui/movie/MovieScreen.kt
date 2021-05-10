package com.ssverma.showtime.ui.movie

import MovieItem
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.model.Genre
import com.ssverma.showtime.domain.model.Movie
import com.ssverma.showtime.ui.common.*
import com.ssverma.showtime.ui.home.HomePageAppBar
import com.ssverma.showtime.ui.home.HomeViewModel
import dev.chrisbanes.accompanist.insets.statusBarsHeight

@Composable
fun MovieScreen(viewModel: HomeViewModel) {
    MovieContent(viewModel = viewModel)
}

@Composable
private fun MovieContent(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .animateContentSize()
    ) {
        HeaderSection(viewModel = viewModel)
        MoviesSection(
            liveMovies = viewModel.popularMovies,
            sectionTitleRes = R.string.popuplar,
            subtitleRes = R.string.popular_info,
            onViewAllClicked = {
                //
            }
        )
        MoviesSection(liveMovies = viewModel.topRatedMovies, sectionTitleRes = R.string.top_rated)
        MoviesSection(
            liveMovies = viewModel.nowPlayingMovies,
            sectionTitleRes = R.string.now_in_cinemas
        )
        MoviesSection(liveMovies = viewModel.upcomingMovies, sectionTitleRes = R.string.upcoming)
        Spacer(modifier = Modifier.height(160.dp))
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MoviesSection(
    liveMovies: LiveData<Result<List<Movie>>>,
    @StringRes sectionTitleRes: Int,
    modifier: Modifier = Modifier,
    @StringRes subtitleRes: Int = 0,
    leadingIconUrl: String? = null,
    onViewAllClicked: (() -> Unit)? = null
) {

    var shouldVisible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = shouldVisible,
        modifier = modifier.padding(top = DefaultMovieSectionSpacing)
    ) {
        Section(
            sectionHeader = {
                SectionHeader(
                    modifier = Modifier.padding(start = 16.dp),
                    title = stringResource(sectionTitleRes),
                    subtitle = if (subtitleRes == 0) null else stringResource(id = subtitleRes),
                    leadingIconUrl = leadingIconUrl,
                    onTrailingActionClicked = onViewAllClicked
                )
            },
        ) {
            DriveCompose(
                observable = liveMovies,
                loading = { SectionLoadingIndicator() }
            ) { movies ->

                if (movies.isEmpty()) {
                    shouldVisible = false
                } else {
                    shouldVisible = true
                    HorizontalList(
                        itemContent = { movie ->
                            MovieItem(movie = movie)
                        },
                        items = movies
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(viewModel: HomeViewModel) {
    val blurColor = MaterialTheme.colors.surface
    val scrimColor = MaterialTheme.colors.onSurface

    Box {
        NetworkImage(
            url = viewModel.movieBackdrop,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .aspectRatio(1f)
                .scrim(
                    colors = listOf(
                        scrimColor.copy(alpha = 0.20f),
                        scrimColor.copy(alpha = 0.50f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            blurColor.copy(alpha = 0.0f),
                            blurColor.copy(alpha = 0.33f),
                            blurColor.copy(alpha = 0.85f),
                            blurColor.copy(alpha = 1f),
                            blurColor.copy(alpha = 1f)
                        )
                    )
                )
        ) {
            Spacer(modifier = Modifier.statusBarsHeight())
            HomePageAppBar()
            Spacer(modifier = Modifier.height(DefaultMovieSectionSpacing))
            MovieGenres(liveGenres = viewModel.genres)
            MoviesSection(
                liveMovies = viewModel.dailyTrendingMovies,
                sectionTitleRes = R.string.trending_today
            )
        }
    }
}

@Composable
fun MovieGenres(liveGenres: LiveData<Result<List<Genre>>>) {
    DriveCompose(
        observable = liveGenres,
        loading = { SectionLoadingIndicator() }
    ) { genres ->
        HorizontalList(
            itemContent = { genre ->
                GenreItem(genre = genre)
            },
            items = genres,
        )
    }
}

private val DefaultMovieSectionSpacing = 32.dp
