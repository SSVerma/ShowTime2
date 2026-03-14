package com.ssverma.feature.search.ui.suggestion.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.search.R
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.feature.search.ui.common.Label
import com.ssverma.feature.search.ui.common.SearchSuggestionDefaults
import com.ssverma.feature.search.ui.common.SuggestionText
import com.ssverma.shared.ui.TmdbPosterAspectRatio

@Composable
fun SearchMovieItem(
    movie: SearchSuggestion.Movie,
    query: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    vertical = SearchSuggestionDefaults.VerticalPadding,
                    horizontal = SearchSuggestionDefaults.HorizontalPadding
                )
        ) {
            NetworkImage(
                url = movie.posterImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(SearchSuggestionDefaults.MediaPosterWidth)
                    .aspectRatio(TmdbPosterAspectRatio)
                    .clip(MaterialTheme.shapes.medium)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = SearchSuggestionDefaults.TitleHorizontalSpacing)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SuggestionText(
                        primaryText = movie.title,
                        query = query,
                        modifier = Modifier.weight(1f)
                    )
                    Label(
                        text = stringResource(id = R.string.movie),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (movie.displayReleaseDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = movie.displayReleaseDate,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (movie.overview.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = movie.overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SearchMovieItemPreview() {
    SearchMovieItem(
        movie = SearchSuggestion.Movie(
            id = 1,
            title = "Movie title",
            overview = "overview",
            posterImageUrl = "",
            backdropImageUrl = "",
            voteAvg = 5.6f,
            voteAvgPercentage = 56f,
            voteCount = 2325,
            displayReleaseDate = "2021-12-03",
            popularity = 1.2f,
            displayPopularity = "1k",
            originalLanguage = "hi"
        ),
        query = "Movie",
        onClick = {}
    )
}