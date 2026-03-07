package com.ssverma.feature.movie.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.feature.movie.R
import com.ssverma.shared.ui.component.ReviewsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieReviewsScreen(
    onBackPress: () -> Unit,
    viewModel: MovieReviewsViewModel = hiltViewModel()
) {
    val reviewPagingItems = viewModel.pagedReviews.collectAsLazyPagingItems()

    Column(
        modifier = Modifier.systemBarsPadding()
    ) {
        ShowTimeTopAppBar(
            title = stringResource(id = R.string.reviews),
            onBackPressed = onBackPress
        )
        ReviewsList(reviewItems = reviewPagingItems)
    }
}
