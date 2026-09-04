package com.ssverma.feature.library.ui.wrapped.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.stats.CinephileMilestone
import com.ssverma.shared.domain.model.stats.MilestoneTier

fun getMilestoneIcon(id: String): ImageVector = when (id) {
    "first_reel" -> Icons.Rounded.Movie
    "silver_explorer" -> Icons.Rounded.Explore
    "century_club" -> Icons.Rounded.EmojiEvents
    "cinephile_legend" -> Icons.Rounded.WorkspacePremium
    "five_star_connoisseur" -> Icons.Rounded.Stars
    "nostalgia_junkie" -> Icons.Rounded.Replay
    "marathon_master" -> Icons.Rounded.Timer
    "binge_overlord" -> Icons.Rounded.Tv
    "decade_hopper" -> Icons.Rounded.HistoryEdu
    "curator_pro" -> Icons.Rounded.Diamond
    else -> Icons.Rounded.MilitaryTech
}

@Composable
fun WrappedMilestonesGrid(
    milestones: List<CinephileMilestone>,
    onMilestoneClick: (CinephileMilestone) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 6.dp)
                        )
                        Text(
                            text = "Cinephile Milestones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Achievements & viewing badges",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val unlockedCount = milestones.count { it.isUnlocked }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$unlockedCount / ${milestones.size} Unlocked",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val sortedMilestones = remember(milestones) {
                milestones.sortedWith(
                    compareByDescending<CinephileMilestone> { it.isUnlocked }
                        .thenByDescending { it.progressPercentage }
                        .thenBy { it.remainingProgress }
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                sortedMilestones.forEach { milestone ->
                    MilestoneCardItem(
                        milestone = milestone,
                        onClick = { onMilestoneClick(milestone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneCardItem(
    milestone: CinephileMilestone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (milestone.maxProgress > 0) {
        (milestone.currentProgress.toFloat() / milestone.maxProgress.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "milestone_progress"
    )

    val tierColor = MilestonePalette.getTierColor(milestone.tier)
    val milestoneIcon = getMilestoneIcon(milestone.id)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (milestone.isUnlocked) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (milestone.isUnlocked) {
                tierColor.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Left Badge Medallion (46.dp) with integrated status overlay (lock / check)
            Box(modifier = Modifier.size(46.dp)) {
                Surface(
                    shape = RoundedCornerShape(13.dp),
                    color = if (milestone.isUnlocked) {
                        tierColor.copy(alpha = 0.18f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    border = BorderStroke(
                        width = if (milestone.isUnlocked) 1.5.dp else 1.dp,
                        color = if (milestone.isUnlocked) {
                            tierColor.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        }
                    ),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = milestoneIcon,
                            contentDescription = null,
                            tint = if (milestone.isUnlocked) {
                                tierColor
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Mini Status Badge Overlay (Bottom-End of the Medallion)
                Surface(
                    shape = CircleShape,
                    color = if (milestone.isUnlocked) {
                        tierColor
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (milestone.isUnlocked) Icons.Rounded.Check else Icons.Rounded.Lock,
                            contentDescription = if (milestone.isUnlocked) "Unlocked" else "Locked",
                            tint = if (milestone.isUnlocked) Color.White else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Content Area
            Column(modifier = Modifier.weight(1f)) {
                // Header Row: Title on the left, TierBadge on the right
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = milestone.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (milestone.isUnlocked) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    TierBadge(tier = milestone.tier, isUnlocked = milestone.isUnlocked)
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Info & Count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (milestone.isUnlocked) {
                            "Completed"
                        } else {
                            val remaining = milestone.remainingProgress
                            if (remaining == 1) "1 more to unlock" else "$remaining more to unlock"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (milestone.isUnlocked) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (milestone.isUnlocked) tierColor else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${milestone.currentProgress} / ${milestone.maxProgress}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (milestone.isUnlocked) tierColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    color = if (milestone.isUnlocked) tierColor else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
private fun TierBadge(
    tier: MilestoneTier,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val tierColor = MilestonePalette.getTierColor(tier)
    val (tierEmoji, tierName) = when (tier) {
        MilestoneTier.BRONZE -> "🥉" to "Bronze"
        MilestoneTier.SILVER -> "🥈" to "Silver"
        MilestoneTier.GOLD -> "🥇" to "Gold"
        MilestoneTier.PLATINUM -> "💎" to "Platinum"
        MilestoneTier.DIAMOND -> "👑" to "Diamond"
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isUnlocked) {
            tierColor.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (isUnlocked) {
                tierColor.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = tierEmoji,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = tierName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) tierColor else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
