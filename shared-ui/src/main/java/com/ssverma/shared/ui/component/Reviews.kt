package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.domain.model.Review
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.core.ui.paging.PagedList
import com.ssverma.shared.ui.component.section.ReviewItem

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