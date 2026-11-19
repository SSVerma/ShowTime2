package com.ssverma.shared.ui.component.community

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ui.R

private val HistogramChartHeight = 84.dp
private val HistogramBarShape = RoundedCornerShape(
    topStart = 3.dp,
    topEnd = 3.dp,
    bottomStart = 1.dp,
    bottomEnd = 1.dp
)

@Composable
fun CommunityRatingHistogram(
    rating: CommunityRatingUiModel,
    modifier: Modifier = Modifier,
    onStepClick: ((Float) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .height(HistogramChartHeight)
                .padding(horizontal = MaterialTheme.spacing.extraSmall)
        ) {
            rating.histogramBars.forEach { bar ->
                val animatedHeightFraction by animateFloatAsState(
                    targetValue = bar.targetFraction,
                    animationSpec = tween(durationMillis = 250),
                    label = "histogram_bar_height_${bar.step}"
                )

                val barColor = if (bar.isUserSelection) {
                    MaterialTheme.colorScheme.primary
                } else if (bar.bucketCount > 0) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }

                val barCd = stringResource(
                    R.string.community_rating_histogram_bar_cd,
                    bar.step,
                    bar.bucketCount,
                    bar.bucketPercentage
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { contentDescription = barCd }
                        .then(
                            if (onStepClick != null) {
                                Modifier.clickable { onStepClick(bar.step) }
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(fraction = animatedHeightFraction)
                            .clip(HistogramBarShape)
                            .background(color = barColor)
                    )
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MaterialTheme.spacing.extraSmall,
                    vertical = MaterialTheme.spacing.extraSmall
                )
        ) {
            Text(
                text = stringResource(R.string.community_rating_axis_min),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.community_rating_axis_mid),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.community_rating_axis_max),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
