package com.ssverma.showtime.ui.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.ssverma.showtime.R

@Composable
fun <T : Any> PagedList(
    pagingItems: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical =
        if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    pagingLoadingIndicator: @Composable () -> Unit = { DefaultPagingLoadingIndicator() },
    placeholderItemContent: @Composable () -> Unit = { DefaultPagingPlaceholder() },
    itemContent: @Composable (item: T) -> Unit
) {
    LazyColumn(
        contentPadding = contentPadding,
        state = state,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        modifier = modifier
    ) {
        items(pagingItems.itemCount) { index ->
            val pagingItem = pagingItems.getAsState(index = index).value
            if (pagingItem == null) {
                placeholderItemContent()
            } else {
                itemContent(pagingItem)
            }
        }

        when (pagingItems.loadState.append) {
            is LoadState.NotLoading -> {
                /*Need not to show anything*/
            }
            is LoadState.Loading -> {
                item { pagingLoadingIndicator() }
            }
            is LoadState.Error -> {
                item { DefaultPagingErrorIndicator(onRetry = { pagingItems.retry() }) }
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun <T : Any> PagedGrid(
    pagingItems: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    cells: GridCells = GridCells.Fixed(count = 2),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pagingLoadingIndicator: @Composable () -> Unit = { DefaultPagingLoadingIndicator() },
    pagingErrorIndicator: @Composable (pagingItems: LazyPagingItems<T>) -> Unit = { DefaultPagingErrorIndicator { it.retry() } },
    placeholderItemContent: @Composable () -> Unit = { DefaultPagingPlaceholder() },
    itemContent: @Composable (item: T) -> Unit
) {
    LazyVerticalGrid(
        cells = cells,
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(pagingItems.itemCount) { index ->
            val pagingItem = pagingItems.getAsState(index = index).value
            if (pagingItem == null) {
                placeholderItemContent()
            } else {
                itemContent(pagingItem)
            }
        }

        //TODO: update span-count to 1 for paging indicators
        when (pagingItems.loadState.append) {
            is LoadState.NotLoading -> {
                /*Need not to show anything*/
            }
            is LoadState.Loading -> {
                item { pagingLoadingIndicator() }
            }
            is LoadState.Error -> {
                item { pagingErrorIndicator(pagingItems) }
            }
        }
    }
}

@Composable
fun <T : Any> PagedContent(
    pagingItems: LazyPagingItems<T>,
    content: @Composable (pagingItems: LazyPagingItems<T>) -> Unit
) {
    when (pagingItems.loadState.refresh) {
        is LoadState.NotLoading -> {
            content(pagingItems)
        }
        is LoadState.Loading -> {
            ScreenLoadingIndicator()
        }
        is LoadState.Error -> {
            ScreenErrorIndicator(
                errorMessage = stringResource(id = R.string.something_went_wrong),
                onRetryClicked = {
                    pagingItems.refresh()
                }
            )
        }
    }
}

@Composable
private fun DefaultPagingLoadingIndicator() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(32.dp)
    ) {
        AppLoadingIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun DefaultPagingErrorIndicator(
    modifier: Modifier = Modifier,
    errorMessage: String = stringResource(id = R.string.something_went_wrong),
    onRetry: () -> Unit
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )
        OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
private fun DefaultPagingPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colors.onSurface.copy(alpha = 0.54f))
            .size(200.dp)
            .aspectRatio(1.15f)
    )
}