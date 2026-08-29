package com.ssverma.shared.ui.component.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.community.MediaReactionTag
import com.ssverma.shared.domain.model.community.MediaReactions
import com.ssverma.shared.ui.R

@Composable
fun MediaReactionsSection(
    reactions: MediaReactions,
    onTagClick: (MediaReactionTag) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.community_vibes_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            AnimatedVisibility(
                visible = reactions.totalReactions > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.community_reactions_count,
                            reactions.totalReactions
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = MediaReactionTag.entries,
                key = { it.tagKey },
                contentType = { "reaction_pill" }
            ) { tag ->
                val isSelected = reactions.isTagSelected(tag = tag)
                val percentage = reactions.getPercentageForTag(tag = tag)
                val count = reactions.getCountForTag(tag = tag)

                ReactionPill(
                    tag = tag,
                    isSelected = isSelected,
                    percentage = percentage,
                    count = count,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTagClick(tag)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReactionPill(
    tag: MediaReactionTag,
    isSelected: Boolean,
    percentage: Float,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = spring(),
        label = "PillContainerColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(),
        label = "PillContentColor"
    )

    val borderStroke = if (isSelected) {
        BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary
        )
    } else {
        BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }

    val emoji = when (tag) {
        MediaReactionTag.MIND_BENDING -> "🧠"
        MediaReactionTag.COMFORT_WATCH -> "☕"
        MediaReactionTag.PLOT_TWIST -> "🌀"
        MediaReactionTag.IMAX_ESSENTIAL -> "🎬"
        MediaReactionTag.EMOTIONAL_TEARJERKER -> "😭"
        MediaReactionTag.OVERRATED -> "⚠️"
    }

    val labelRes = when (tag) {
        MediaReactionTag.MIND_BENDING -> R.string.tag_mind_bending
        MediaReactionTag.COMFORT_WATCH -> R.string.tag_comfort_watch
        MediaReactionTag.PLOT_TWIST -> R.string.tag_plot_twist
        MediaReactionTag.IMAX_ESSENTIAL -> R.string.tag_imax_essential
        MediaReactionTag.EMOTIONAL_TEARJERKER -> R.string.tag_cried_eyes_out
        MediaReactionTag.OVERRATED -> R.string.tag_overrated
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = borderStroke,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = stringResource(id = labelRes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )

            if (count > 0) {
                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    }
                ) {
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
