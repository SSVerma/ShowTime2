package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.model.Review
import com.ssverma.shared.ui.component.section.ReviewsSection

fun LazyListScope.tvReviewsSection(
    reviews: List<Review>,
    onReviewsViewAllClick: () -> Unit,
    onReviewClick: (Review) -> Unit,
    modifier: Modifier = Modifier
) {
    if (reviews.isNotEmpty()) {
        item(key = "tv_reviews", contentType = "reviews") {
            ReviewsSection(
                reviews = reviews,
                onReviewsViewAllClick = onReviewsViewAllClick,
                onReviewClick = onReviewClick,
                modifier = modifier
            )
        }
    }
}
