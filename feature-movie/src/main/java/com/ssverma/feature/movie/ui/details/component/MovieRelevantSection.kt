package com.ssverma.feature.movie.ui.details.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ssverma.core.ui.layout.HorizontalLazyListSection
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.R
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.ui.component.media.MediaItemDefaults
import com.ssverma.shared.ui.component.media.ShowFeedbackArgs
import com.ssverma.shared.ui.component.media.UniversalMediaCard
import com.ssverma.shared.ui.component.media.asUniversalMediaItem

fun LazyListScope.movieSimilarMoviesSection(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onShowFeedback: (ShowFeedbackArgs) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isNotEmpty()) {
        item(key = "movie_similar_movies", contentType = "similar_movies") {
            RelevantMoviesSection(
                movies = movies,
                sectionTitleRes = R.string.similar_movies,
                onMovieClick = onMovieClick,
                onShowFeedback = onShowFeedback,
                modifier = modifier
            )
        }
    }
}

fun LazyListScope.movieRecommendationsSection(
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onShowFeedback: (ShowFeedbackArgs) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isNotEmpty()) {
        item(key = "movie_recommendations", contentType = "recommendations") {
            RelevantMoviesSection(
                movies = movies,
                sectionTitleRes = R.string.recommendations,
                onMovieClick = onMovieClick,
                onShowFeedback = onShowFeedback,
                modifier = modifier
            )
        }
    }
}

@Composable
fun RelevantMoviesSection(
    movies: List<Movie>,
    @StringRes sectionTitleRes: Int,
    onMovieClick: (movie: Movie) -> Unit,
    modifier: Modifier = Modifier,
    onShowFeedback: (ShowFeedbackArgs) -> Unit = {}
) {
    HorizontalLazyListSection(
        items = movies,
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = sectionTitleRes),
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                hideTrailingAction = true
            )
        },
        itemContent = {
            UniversalMediaCard(
                item = it.asUniversalMediaItem(),
                isGridView = true,
                onShowFeedback = onShowFeedback,
                onClick = { onMovieClick(it) },
                modifier = Modifier.width(MediaItemDefaults.PosterWidth)
            )
        },
        hideIf = movies.isEmpty(),
        modifier = modifier
    )
}
