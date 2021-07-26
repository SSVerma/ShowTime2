package com.ssverma.showtime.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.ssverma.showtime.domain.model.Review
import com.ssverma.showtime.ui.common.PagedContent
import com.ssverma.showtime.ui.common.PagedList
import com.ssverma.showtime.ui.movie.ReviewItem

@Composable
fun ReviewsList(
    reviewItems: LazyPagingItems<Review>,
    modifier: Modifier = Modifier
) {
    PagedContent(pagingItems = reviewItems) {
        PagedList(
            pagingItems = reviewItems,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
        ) {
            ReviewItem(review = it)
        }
    }
}