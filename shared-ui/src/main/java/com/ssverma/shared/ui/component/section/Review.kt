package com.ssverma.shared.ui.component.section

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
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
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                onTrailingActionClicked = onReviewsViewAllClick,
                hideTrailingAction = reviews.size <= MaxReviews
            )
        },
        headerContentSpacing = SectionDefaults.SectionContentHeaderSpacing,
        hideIf = reviews.isEmpty(),
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium),
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
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

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        onClick = {
            onClick(review)
            expanded = !expanded
        },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
                .animateContentSize()
        ) {
            // Author row: Avatar + Name & Rating + Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Avatar(
                    imageUrl = review.author.avatarImageUrl,
                    size = 40.dp,
                    modifier = Modifier.padding(end = MaterialTheme.spacing.smallMedium)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    val authorName = if (!review.author.name.isNullOrEmpty()) {
                        review.author.name.orEmpty()
                    } else if (review.author.userName.isNotEmpty()) {
                        review.author.userName
                    } else {
                        stringResource(id = R.string.unknown)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        Text(
                            text = authorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Rating badge if author gave a rating > 0
                        if (review.author.rating > 0f) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(
                                        horizontal = MaterialTheme.spacing.extraSmall,
                                        vertical = 2.dp
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = stringResource(id = R.string.rating_icon_cd),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = review.author.rating.toInt().toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Timestamp
                    review.displayCreatedAt?.let { date ->
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))

            // Review Content with clean typography
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else MaxCollapsedLines,
                overflow = TextOverflow.Ellipsis
            )

            // Explicit Read More / Show Less hint
            Text(
                text = stringResource(id = if (expanded) R.string.show_less else R.string.read_more),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = MaterialTheme.spacing.small)
            )
        }
    }
}

private const val MaxReviews = 3
private const val MaxCollapsedLines = 4
