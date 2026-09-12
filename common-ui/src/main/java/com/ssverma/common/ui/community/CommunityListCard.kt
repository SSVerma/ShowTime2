package com.ssverma.common.ui.community

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PublicOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.ui.R

@Composable
fun CommunityListCard(
    communityList: CommunityCuratedList,
    onClick: () -> Unit,
    onToggleUpvote: () -> Unit,
    onCloneList: () -> Unit,
    modifier: Modifier = Modifier,
    onMakePrivate: (() -> Unit)? = null,
    onDeleteFromCommunity: (() -> Unit)? = null,
    onReport: (() -> Unit)? = null,
    onBlockAuthor: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var isHeartPopped by remember { mutableStateOf(false) }
    var lastUpvoteClickTime by remember { mutableLongStateOf(0L) }

    val heartScale by animateFloatAsState(
        targetValue = if (isHeartPopped) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = { isHeartPopped = false },
        label = "ListHeartScaleAnimation"
    )

    val heartTint by animateColorAsState(
        targetValue = if (communityList.isUpvotedByMe) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "UpvoteHeartTint"
    )

    var isRevealed by remember(communityList.listId) { mutableStateOf(false) }
    val showWarningShield = communityList.isFlagged && !isRevealed

    OutlinedCard(
        onClick = {
            if (!showWarningShield) {
                onClick()
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (showWarningShield) {
                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        if (showWarningShield) {
                            alpha = 0.12f
                        }
                    }
                    .padding(14.dp)
            ) {
                // Top Row: Category Chip + Clones/Upvotes Badges
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = communityList.categoryTag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "Your List" badge for owner
                        if (communityList.isMine) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.your_list_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Item count pill
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Movie,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${communityList.itemCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Poster Collage Mosaic (up to 4 items)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                ) {
                    val posters = communityList.previewPosters.take(4)
                    posters.forEach { posterUrl ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            NetworkImage(
                                url = posterUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    // Fill remaining slots if fewer than 4 posters
                    repeat(4 - posters.size) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FolderSpecial,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                // List Title
                Text(
                    text = communityList.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Optional Description
                val description = communityList.description
                if (!description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Row: Author info + Like & Clone Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Author info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val avatarUrl = communityList.authorAvatarUrl
                        if (!avatarUrl.isNullOrBlank()) {
                            NetworkImage(
                                url = avatarUrl,
                                contentDescription = communityList.authorName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = communityList.authorName.take(1).uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "by ${communityList.authorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Interactive Upvote & Clone/Owner Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Upvote Button
                        Surface(
                            onClick = {
                                val now = System.currentTimeMillis()
                                if (now - lastUpvoteClickTime >= 350L) {
                                    lastUpvoteClickTime = now
                                    isHeartPopped = true
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onToggleUpvote()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (communityList.isUpvotedByMe) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (communityList.isUpvotedByMe) {
                                        Icons.Rounded.Favorite
                                    } else {
                                        Icons.Rounded.FavoriteBorder
                                    },
                                    contentDescription = stringResource(id = R.string.cd_favorite_button),
                                    tint = heartTint,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .scale(heartScale)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${communityList.upvotesCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (communityList.isUpvotedByMe) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }

                        if (communityList.isMine) {
                            // Owner overflow menu
                            OwnerOverflowMenu(
                                onMakePrivate = onMakePrivate,
                                onDeleteFromCommunity = onDeleteFromCommunity
                            )
                        } else {
                            if (communityList.isClonedByMe) {
                                // Cloned badge (static)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(id = R.string.cloned_badge),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                // Clone button
                                Surface(
                                    onClick = onCloneList,
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.BookmarkAdd,
                                            contentDescription = stringResource(id = R.string.clone_to_my_library),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(id = R.string.clone_short),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            if (onReport != null || onBlockAuthor != null) {
                                CommunityListNonOwnerOverflowMenu(
                                    onReport = onReport,
                                    onBlockAuthor = onBlockAuthor
                                )
                            }
                        }
                    }
                }
            }

            if (showWarningShield) {
                CommunityWarningShield(
                    onShowAnyway = { isRevealed = true },
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
private fun OwnerOverflowMenu(
    onMakePrivate: (() -> Unit)?,
    onDeleteFromCommunity: (() -> Unit)?
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(id = R.string.cd_more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (onMakePrivate != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.make_private_short),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.PublicOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        expanded = false
                        onMakePrivate()
                    }
                )
            }
            if (onDeleteFromCommunity != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.delete_from_community),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        expanded = false
                        onDeleteFromCommunity()
                    }
                )
            }
        }
    }
}

@Composable
internal fun CommunityListNonOwnerOverflowMenu(
    onReport: (() -> Unit)?,
    onBlockAuthor: (() -> Unit)?
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(id = R.string.cd_more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (onReport != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.report_collection),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        expanded = false
                        onReport()
                    }
                )
            }
            if (onBlockAuthor != null) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(id = R.string.block_creator),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    onClick = {
                        expanded = false
                        onBlockAuthor()
                    }
                )
            }
        }
    }
}

@Composable
private fun CommunityWarningShield(
    onShowAnyway: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(id = R.string.community_flagged_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stringResource(id = R.string.community_flagged_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onShowAnyway,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = stringResource(id = R.string.community_flagged_show_anyway),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}


