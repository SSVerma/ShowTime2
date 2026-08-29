package com.ssverma.shared.ui.component.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.ui.R

@Composable
fun DailyPollCard(
    poll: DailyPoll,
    onOptionClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!poll.isEnabled || poll.options.isEmpty() || poll.question.isBlank()) {
        return
    }

    val haptic = LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Poll,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.daily_poll_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                AnimatedVisibility(
                    visible = poll.totalVotes > 0,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = stringResource(
                                id = R.string.daily_poll_votes_count,
                                poll.totalVotes
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text
            Text(
                text = poll.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Options Column
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                poll.options.forEachIndexed { index, optionText ->
                    val isSelected = poll.selectedOptionIndex == index
                    val percentage = poll.getPercentage(optionIndex = index)

                    PollOptionItem(
                        text = optionText,
                        isSelected = isSelected,
                        hasVoted = poll.hasVoted,
                        percentage = percentage,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOptionClick(index)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PollOptionItem(
    text: String,
    isSelected: Boolean,
    hasVoted: Boolean,
    percentage: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (hasVoted) (percentage / 100f).coerceIn(0f, 1f) else 0f,
        animationSpec = spring(),
        label = "PollOptionProgress"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        },
        animationSpec = spring(),
        label = "PollOptionBg"
    )

    val progressFillColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
        },
        animationSpec = spring(),
        label = "PollProgressFill"
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

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = borderStroke,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Animated Progress Fill Bar
            if (hasVoted && animatedProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .matchParentSize()
                        .background(progressFillColor)
                )
            }

            // Content Row (Label + Percentage Badge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                if (hasVoted) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
