package com.ssverma.showtime.ui.movie

import MovieItem
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.R
import com.ssverma.showtime.data.domain.asMovie
import com.ssverma.showtime.data.domain.asMovies
import com.ssverma.showtime.ui.common.*
import com.ssverma.showtime.ui.home.HomeCategories
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
    ) {
        HeaderSection(viewModel = viewModel)

        SectionSpacer()

        SectionHeader(
            title = stringResource(R.string.latest),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        NetworkCompose(
            observable = viewModel.latestMovie,
            loading = { SectionLoadingIndicator() }
        ) {
            MovieItem(
                movie = remember(it.data) { it.data.asMovie() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                imageModifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        SectionSpacer()
        Section(
            sectionHeader = {
                SectionHeader(
                    modifier = Modifier.padding(start = 16.dp),
                    title = stringResource(R.string.free),
                    onTrailingActionClicked = {
                        //
                    }
                )
            }
        ) {
            NetworkCompose(
                observable = viewModel.freeMovies,
                mapper = { it.results?.asMovies() ?: emptyList() },
                loading = { SectionLoadingIndicator() }
            ) { _, movies ->
                HorizontalList(
                    itemContent = { movie ->
                        MovieItem(movie = movie)
                    },
                    items = movies
                )
            }
        }

        SectionSpacer()
        Section(
            sectionHeader = {
                SectionHeader(
                    modifier = Modifier.padding(start = 16.dp),
                    title = stringResource(R.string.now_in_cinemas),
                    onTrailingActionClicked = {
                        //
                    }
                )
            }
        ) {
            NetworkCompose(
                observable = viewModel.justReleasedMovies,
                mapper = { it.results?.asMovies() ?: emptyList() },
                loading = { SectionLoadingIndicator() }
            ) { _, movies ->
                HorizontalList(
                    itemContent = { movie ->
                        MovieItem(movie = movie)
                    },
                    items = movies
                )
            }
        }

        Spacer(modifier = Modifier.height(160.dp))
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
            HomeCategories(modifier = Modifier.padding(top = 16.dp), viewModel = viewModel)
            SectionSpacer()
            Section(
                sectionHeader = {
                    SectionHeader(
                        modifier = Modifier.padding(start = 16.dp),
                        title = stringResource(R.string.trending_today),
                        onTrailingActionClicked = {
                            //
                        }
                    )
                }
            ) {
                NetworkCompose(
                    observable = viewModel.dailyTrendingMovies,
                    mapper = { it.results?.asMovies() ?: emptyList() },
                    loading = { SectionLoadingIndicator() }
                ) { _, movies ->
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
fun SectionSpacer() = Spacer(modifier = Modifier.height(24.dp))
