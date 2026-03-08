package com.ssverma.feature.person.ui.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.person.R
import com.ssverma.feature.person.ui.details.component.asUiText
import com.ssverma.shared.domain.model.person.Person
import com.ssverma.shared.domain.model.person.PersonMedia
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.Avatar
import com.ssverma.shared.ui.component.media.MediaItem

@Composable
fun PersonListItem(
    person: Person,
    index: Int,
    onClick: () -> Unit,
    onPopularMediaBtnClick: (personId: Int) -> Unit,
    onMediaClick: (media: PersonMedia) -> Unit,
    showPopularMedia: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        ) {
            // Top row: Avatar + Info + Expand button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar with gradient ring and rank badge
                GradientAvatar(
                    imageUrl = person.imageUrl,
                    rank = index + 1,
                    onClick = onClick
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

                // Name and badges
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                    ) {
                        IconPillBadge(
                            text = stringResource(id = person.gender.asUiText().resId),
                            icon = Icons.Rounded.Person,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        IconPillBadge(
                            text = person.knownFor,
                            icon = Icons.Rounded.Movie,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                // Expand/Collapse button
                if (!person.popularMedia.isNullOrEmpty()) {
                    ExpandCollapseButton(
                        expanded = showPopularMedia,
                        onClick = { onPopularMediaBtnClick(person.id) }
                    )
                }
            }

            // Expandable popular media section
            AnimatedVisibility(
                visible = showPopularMedia
            ) {
                person.popularMedia?.let { mediaList ->
                    Column {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = MaterialTheme.spacing.medium),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = MaterialTheme.spacing.small)
                        ) {
                            Text(
                                text = stringResource(id = R.string.popular_media),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalLazyList(
                            items = mediaList,
                            contentPadding = PaddingValues(0.dp)
                        ) { media ->
                            Column(
                                modifier = Modifier.widthIn(max = PopularMediaItemWidth)
                            ) {
                                MediaItem(
                                    title = media.title,
                                    posterImageUrl = media.posterImageUrl,
                                    titleTextStyle = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.widthIn(max = PopularMediaItemWidth),
                                    posterModifier = Modifier
                                        .width(PopularMediaItemWidth)
                                        .aspectRatio(TmdbPosterAspectRatio),
                                    onClick = { onMediaClick(media) }
                                )
                                if (media.character.isNotBlank()) {
                                    Text(
                                        text = media.character,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = PopularMediaItemWidth)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientAvatar(
    imageUrl: String,
    rank: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(AvatarSize)
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(gradientColors),
                    shape = CircleShape
                )
                .padding(4.dp)
        ) {
            Avatar(
                imageUrl = imageUrl,
                onClick = onClick,
                modifier = Modifier.size(AvatarSize - 8.dp),
                borderWidth = 0.dp,
                borderSpacing = 0.dp
            )
        }

        // Rank badge - top left overlap
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.TopStart)
                .offset(x = (-4).dp, y = (-4).dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun IconPillBadge(
    text: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpandCollapseButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (expanded) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "ExpandButtonColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (expanded) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(durationMillis = 300),
        label = "ExpandIconColor"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = backgroundColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (expanded) {
                    Icons.Rounded.KeyboardArrowUp
                } else {
                    Icons.Rounded.KeyboardArrowDown
                },
                contentDescription = stringResource(id = R.string.popular_media),
                tint = iconTint
            )
        }
    }
}

private val AvatarSize = 80.dp
private val PopularMediaItemWidth = 100.dp
