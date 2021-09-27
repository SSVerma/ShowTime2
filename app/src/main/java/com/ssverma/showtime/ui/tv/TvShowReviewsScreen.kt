package com.ssverma.showtime.ui.tv

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.insets.systemBarsPadding
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.ReviewsList
import com.ssverma.showtime.ui.common.AppTopAppBar

@Composable
fun TvShowReviewsScreen(
    viewModel: TvShowReviewsViewModel,
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