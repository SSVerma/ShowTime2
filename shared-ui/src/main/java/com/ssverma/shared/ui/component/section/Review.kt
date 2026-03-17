package com.ssverma.shared.ui.component.section

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.domain.model.Review
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.Avatar

@Composable
fun ReviewsSection(
    reviews: List<Review>,
    onReviewsViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    onReviewClick: (Review) -> Unit = {}
) {
    val reviewCount = if (reviews.size < MaxReviews) reviews.size else MaxReviews

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.reviews),
                modifier = Modifier.padding(horizontal = 16.dp),
                onTrailingActionClicked = {
                    onReviewsViewAllClick()
                },
                hideTrailingAction = reviews.size <= MaxReviews
            )
        },
        headerContentSpacing = SectionDefaults.SectionContentHeaderSpacing,
        hideIf = reviews.isEmpty(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            for (i in 0 until reviewCount) {
                ReviewItem(
                    review = reviews[i],
                    onClick = onReviewClick
                )
            }
        }
    }
}

@Composable
fun ReviewItem(
    review: Review,
    modifier: Modifier = Modifier,
    onClick: (Review) -> Unit = {}
) {

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
    ) {
        Avatar(
            imageUrl = review.author.avatarImageUrl,
            onClick = {
                //TODO
            },
            modifier = Modifier
                .padding(end = 16.dp)
        )

        Surface(
            shape = MaterialTheme.shapes.medium.copy(topStart = CornerSize(0.dp)),
            onClick = {
                onClick(review)
                expanded = !expanded
            },
            tonalElevation = if (expanded) 2.dp else 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .animateContentSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    val authorName = if (!review.author.name.isNullOrEmpty()) {
                        review.author.name
                    } else if (review.author.userName.isNotEmpty()) {
                        review.author.userName
                    } else {
                        stringResource(id = R.string.unknown)
                    }

                    /*Author*/
                    BoxWithConstraints {
                        Text(
                            text = authorName.orEmpty(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(horizontal = 4.dp)
                                .widthIn(max = maxWidth / 2)
                        )
                    }

                    /*Timestamp*/
                    review.displayCreatedAt?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.End
                        )
                    }
                }

                /*Review content*/
                Text(
                    text = review.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

private const val MaxReviews = 3
