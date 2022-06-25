package com.ssverma.feature.search.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.search.R
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.feature.search.ui.common.Label
import com.ssverma.feature.search.ui.common.SearchSuggestionDefaults
import com.ssverma.feature.search.ui.common.SuggestionText
import com.ssverma.shared.ui.TmdbPosterAspectRatio

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchTvShowItem(
    tvShow: SearchSuggestion.TvShow,
    query: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colors.background,
        modifier = modifier,
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
                url = tvShow.posterImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(SearchSuggestionDefaults.MediaPosterWidth)
                    .aspectRatio(TmdbPosterAspectRatio)
                    .clip(MaterialTheme.shapes.medium)
            )

            SuggestionText(
                primaryText = tvShow.title,
                query = query,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = SearchSuggestionDefaults.TitleHorizontalSpacing)
            )

            Label(text = stringResource(id = R.string.tv))
        }
    }
}

@Preview
@Composable
private fun SearchTvShowItemPreview() {
    SearchTvShowItem(
        tvShow = SearchSuggestion.TvShow(
            id = 1,
            title = "Tv show title",
            overview = "overview",
            posterImageUrl = "/stTEycfG9928HYGEISBFaG1ngjM.jpg".convertToFullTmdbImageUrl(),
            backdropImageUrl = "",
            voteAvg = 5.6f,
            voteAvgPercentage = 56f,
            voteCount = 2325,
            displayFirstAirDate = "2021-12-03",
            popularity = 1.2f,
            displayPopularity = "1k",
            originalLanguage = "hi"
        ),
        query = "Tv sh",
        onClick = {}
    )
}