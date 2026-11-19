package com.ssverma.feature.account.ui.debug

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.R
import com.ssverma.shared.domain.model.community.DebugRatingOverride

@Composable
fun RatingSandboxSection(
    ratingOverride: DebugRatingOverride,
    ratingThreshold: Int,
    onRatingOverrideSelected: (DebugRatingOverride) -> Unit,
    onRatingThresholdSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.dev_ratings_sandbox),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        OutlinedCard(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                Text(
                    text = stringResource(R.string.dev_ratings_threshold),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.dev_ratings_sandbox_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = ratingThreshold == 1,
                        onClick = { onRatingThresholdSelected(1) },
                        label = { Text(text = stringResource(R.string.dev_ratings_threshold_1)) }
                    )
                    FilterChip(
                        selected = ratingThreshold == 5,
                        onClick = { onRatingThresholdSelected(5) },
                        label = { Text(text = stringResource(R.string.dev_ratings_threshold_5)) }
                    )
                    FilterChip(
                        selected = ratingThreshold == 10,
                        onClick = { onRatingThresholdSelected(10) },
                        label = { Text(text = stringResource(R.string.dev_ratings_threshold_10)) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.small),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                Text(
                    text = stringResource(R.string.dev_ratings_state),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                        FilterChip(
                            selected = ratingOverride == DebugRatingOverride.AUTO,
                            onClick = { onRatingOverrideSelected(DebugRatingOverride.AUTO) },
                            label = { Text(text = stringResource(R.string.dev_ratings_state_auto)) }
                        )
                        FilterChip(
                            selected = ratingOverride == DebugRatingOverride.FORCE_COLD_START,
                            onClick = { onRatingOverrideSelected(DebugRatingOverride.FORCE_COLD_START) },
                            label = { Text(text = stringResource(R.string.dev_ratings_state_cold)) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                        FilterChip(
                            selected = ratingOverride == DebugRatingOverride.FORCE_BELOW_THRESHOLD,
                            onClick = { onRatingOverrideSelected(DebugRatingOverride.FORCE_BELOW_THRESHOLD) },
                            label = { Text(text = stringResource(R.string.dev_ratings_state_below)) }
                        )
                        FilterChip(
                            selected = ratingOverride == DebugRatingOverride.FORCE_ACTIVE_HIGH,
                            onClick = { onRatingOverrideSelected(DebugRatingOverride.FORCE_ACTIVE_HIGH) },
                            label = { Text(text = stringResource(R.string.dev_ratings_state_active)) }
                        )
                    }
                    FilterChip(
                        selected = ratingOverride == DebugRatingOverride.FORCE_USER_RATED,
                        onClick = { onRatingOverrideSelected(DebugRatingOverride.FORCE_USER_RATED) },
                        label = { Text(text = stringResource(R.string.dev_ratings_state_user)) }
                    )
                }
            }
        }
    }
}
