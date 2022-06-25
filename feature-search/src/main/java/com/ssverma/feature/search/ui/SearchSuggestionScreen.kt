package com.ssverma.feature.search.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.feature.search.R
import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.shared.domain.model.MediaType

@Composable
fun SearchSuggestionScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchSuggestionViewModel = hiltViewModel(),
    onMovieClick: (movieId: Int) -> Unit,
    onTvShowClick: (tvShowId: Int) -> Unit,
    onPersonClick: (personId: Int) -> Unit,
    onBackPressed: () -> Unit
) {

    val query by viewModel.searchQuery.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()
    val historyItems by viewModel.searchHistory.collectAsState()
    val showHistory by remember { derivedStateOf { query.isBlank() } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        SearchBar(
            query = query,
            onQueryChanged = { viewModel.onQueryUpdated(it) },
            onBackPressed = onBackPressed
        )

        Divider()

        LazyColumn {
            historyItems(
                items = historyItems,
                show = showHistory,
                onHistoryItemClick = { history ->
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
                    viewModel.clearHistoryItem(history)
                }
            )

            suggestions(
                items = searchSuggestions,
                query = query,
                onSuggestionClick = { suggestion ->
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
        Divider(thickness = 0.5.dp)
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SearchBarMinHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackPressed) {
            Icon(imageVector = AppIcons.ArrowBack, contentDescription = null)
        }
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(text = stringResource(R.string.search))
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                textStyle = MaterialTheme.typography.body1.copy(
                    color = MaterialTheme.colors.onBackground
                ),
                maxLines = 1,
                cursorBrush = SolidColor(MaterialTheme.colors.onBackground),
                modifier = Modifier.fillMaxWidth()
            )
        }
        AnimatedVisibility(visible = query.isNotBlank()) {
            IconButton(onClick = { onQueryChanged("") }) {
                Icon(imageVector = AppIcons.Close, contentDescription = null)
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