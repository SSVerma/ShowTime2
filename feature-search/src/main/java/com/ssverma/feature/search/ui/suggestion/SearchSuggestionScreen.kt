package com.ssverma.feature.search.ui.suggestion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.analytics.ui.TrackScreenView
import com.ssverma.feature.search.R
import com.ssverma.feature.search.analytics.SearchAnalyticsEvent
import com.ssverma.feature.search.analytics.SearchAnalyticsScreenName
import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.feature.search.ui.suggestion.component.SearchHistoryItem
import com.ssverma.feature.search.ui.suggestion.component.SearchMovieItem
import com.ssverma.feature.search.ui.suggestion.component.SearchPersonItem
import com.ssverma.feature.search.ui.suggestion.component.SearchTvShowItem
import com.ssverma.shared.domain.model.MediaType

import androidx.compose.material3.Surface

@Composable
fun SearchSuggestionScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchSuggestionViewModel = hiltViewModel(),
    onMovieClick: (movieId: Int) -> Unit,
    onTvShowClick: (tvShowId: Int) -> Unit,
    onPersonClick: (personId: Int) -> Unit,
    onBackPressed: () -> Unit
) {
    TrackScreenView(screenName = SearchAnalyticsScreenName.TYPEAHEAD)

    val analytics = LocalAnalytics.current

    val query by viewModel.searchQuery.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val historyItems by viewModel.searchHistory.collectAsState()
    val showHistory by remember { derivedStateOf { query.isBlank() } }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            SearchBar(
                query = query,
                onQueryChanged = { viewModel.onQueryUpdated(it) },
                onBackPressed = onBackPressed
            )

            HorizontalDivider()

            LazyColumn {
                historyItems(
                    items = historyItems,
                    show = showHistory,
                    onHistoryItemClick = { history ->
                        analytics.logEvent(SearchAnalyticsEvent.SearchHistoryClicked(history))

                        when (history.mediaType) {
                            MediaType.Movie -> {
                                onMovieClick(history.id)
                            }

                            MediaType.Person -> {
                                onPersonClick(history.id)
                            }

                            MediaType.Tv -> {
                                onTvShowClick(history.id)
                            }

                            MediaType.Unknown -> {
                                // No op
                            }
                        }
                    },
                    onHistoryClearIconClick = { history ->
                        analytics.logEvent(SearchAnalyticsEvent.SearchHistoryCleared(history))
                        viewModel.clearHistoryItem(history)
                    }
                )

                suggestions(
                    items = searchSuggestions,
                    query = query,
                    onSuggestionClick = { suggestion ->
                        analytics.logEvent(SearchAnalyticsEvent.SearchResultClicked(suggestion))
                        when (suggestion) {
                            is SearchSuggestion.Movie -> {
                                viewModel.saveSearchHistory(suggestion)
                                onMovieClick(suggestion.id)
                            }

                            is SearchSuggestion.Person -> {
                                viewModel.saveSearchHistory(suggestion)
                                onPersonClick(suggestion.id)
                            }

                            is SearchSuggestion.TvShow -> {
                                viewModel.saveSearchHistory(suggestion)
                                onTvShowClick(suggestion.id)
                            }

                            SearchSuggestion.None -> {
                                // No op
                            }
                        }
                    }
                )
            }
        }
    }
}

private fun LazyListScope.historyItems(
    items: List<SearchHistory>,
    show: Boolean,
    onHistoryItemClick: (SearchHistory) -> Unit,
    onHistoryClearIconClick: (SearchHistory) -> Unit,
) = items(items) { history ->
    AnimatedVisibility(visible = show) {
        SuggestionItem {
            SearchHistoryItem(
                history = history,
                onClick = { onHistoryItemClick(history) },
                onHistoryClearClick = { onHistoryClearIconClick(history) }
            )
        }
    }
}

private fun LazyListScope.suggestions(
    items: List<SearchSuggestion>,
    query: String,
    onSuggestionClick: (SearchSuggestion) -> Unit
) = items(items) { suggestion ->
    SuggestionItem {
        when (suggestion) {
            is SearchSuggestion.Movie -> {
                SearchMovieItem(
                    movie = suggestion,
                    query = query,
                    onClick = {
                        onSuggestionClick(suggestion)
                    }
                )
            }

            is SearchSuggestion.Person -> {
                SearchPersonItem(
                    person = suggestion,
                    query = query,
                    onClick = {
                        onSuggestionClick(suggestion)
                    }
                )
            }

            is SearchSuggestion.TvShow -> {
                SearchTvShowItem(
                    tvShow = suggestion,
                    query = query,
                    onClick = {
                        onSuggestionClick(suggestion)
                    }
                )
            }

            SearchSuggestion.None -> {
                // No op
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier) {
        content()
        HorizontalDivider(thickness = 0.5.dp)
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SearchBarMinHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackPressed) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
        }
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.search),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 1,
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
        AnimatedVisibility(visible = query.isNotBlank()) {
            IconButton(onClick = { onQueryChanged("") }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
        }
    }
}

private val SearchBarMinHeight = 56.dp

@Preview
@Composable
private fun SearchBarPreview() {
    SearchBar(
        query = "avengers",
        onQueryChanged = {},
        onBackPressed = {}
    )
}