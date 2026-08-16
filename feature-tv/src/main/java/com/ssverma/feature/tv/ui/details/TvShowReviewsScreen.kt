package com.ssverma.feature.tv.ui.details

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.layout.AppPage
import com.ssverma.feature.tv.R
import com.ssverma.shared.ui.component.ReviewsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowReviewsScreen(
    onBackPress: () -> Unit,
    viewModel: TvShowReviewsViewModel
) {
    val reviewPagingItems = viewModel.pagedReviews.collectAsLazyPagingItems()

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        AppPage(
            topBar = { scrollBehavior ->
                ShowTimeTopAppBar(
                    title = stringResource(id = R.string.reviews),
                    onBackPressed = onBackPress,
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            ReviewsList(
                reviewItems = reviewPagingItems,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
