package com.ssverma.showtime.ui.movie

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.ReviewsList
import com.ssverma.showtime.ui.common.AppTopAppBar

@Composable
fun MovieReviewsScreen(
    viewModel: MovieReviewsViewModel,
    onBackPress: () -> Unit
) {
    val reviewPagingItems = viewModel.pagedReviews.collectAsLazyPagingItems()

    Column(
        modifier = Modifier.systemBarsPadding()
    ) {
        AppTopAppBar(title = stringResource(id = R.string.reviews), onBackPressed = onBackPress)
        ReviewsList(reviewItems = reviewPagingItems)
    }
}