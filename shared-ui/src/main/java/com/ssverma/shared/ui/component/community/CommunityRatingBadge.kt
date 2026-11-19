package com.ssverma.shared.ui.component.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.ShowTimeTheme
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityRating
import com.ssverma.shared.ui.R

private val BadgeStarIconSize = 14.dp

@Composable
fun CommunityRatingBadge(
    rating: CommunityRatingUiModel?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.70f),
    tonalElevation: Dp = 2.dp,
    shadowElevation: Dp = 0.dp,
) {
    val hasRatings = rating?.hasSufficientRatings == true
    val formattedUserRating = rating?.formattedUserRating
    val userRated = formattedUserRating != null

    val labelText = when {
        hasRatings -> rating.formattedAverageRating
        formattedUserRating != null -> formattedUserRating
        else -> stringResource(R.string.community_rating_rate_now)
    }

    val subtitleText = if (hasRatings) {
        "(${rating.ratingCount})"
    } else {
        null
    }

    val badgeCd = when {
        hasRatings -> stringResource(
            R.string.community_rating_badge_cd,
            rating.formattedAverageRating,
            rating.ratingCount,
        )

        formattedUserRating != null -> stringResource(
            R.string.community_rating_user_rated_badge_cd,
            formattedUserRating,
        )

        else -> stringResource(R.string.community_rating_rate_now)
    }

    Surface(
        onClick = onClick,
        shape = shape,
        color = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = BorderStroke(
            width = 1.dp,
            color = if (userRated) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            },
        ),
        modifier = modifier.semantics { contentDescription = badgeCd },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.smallMedium,
                vertical = MaterialTheme.spacing.extraSmall,
            ),
        ) {
            if (hasRatings || userRated) {
                Text(
                    text = stringResource(R.string.community_rating_brand),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Icon(
                imageVector = if (hasRatings || userRated) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = null,
                tint = if (userRated) {
                    MaterialTheme.colorScheme.primary
                } else if (hasRatings) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(BadgeStarIconSize),
            )

            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            subtitleText?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
private fun CommunityRatingBadgePreview() {
    ShowTimeTheme {
        CommunityRatingBadge(
            rating = CommunityRating(
                mediaType = MediaType.Movie,
                mediaId = 1,
                ratingCount = 42,
                ratingSum = 184.8,
                averageRating = 4.4f,
                histogram = emptyMap(),
                userRating = null,
            ).toUiModel(),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun CommunityRatingBadgeColdStartPreview() {
    ShowTimeTheme {
        CommunityRatingBadge(
            rating = null,
            onClick = {},
        )
    }
}
