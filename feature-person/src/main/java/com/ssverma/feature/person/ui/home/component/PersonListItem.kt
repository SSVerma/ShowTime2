package com.ssverma.feature.person.ui.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Column(
                modifier = Modifier.padding(MaterialTheme.spacing.medium)
            ) {
                // Top row: Avatar + Info + Expand button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar with rank badge
                    GradientAvatar(
                        imageUrl = person.imageUrl,
                        rank = index + 1,
                        onClick = onClick
                    )

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

                    // Name and descriptive data
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            IconPillBadge(
                                text = person.knownFor,
                                icon = Icons.Rounded.Movie,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            IconPillBadge(
                                text = stringResource(id = person.gender.asUiText().resId),
                                icon = Icons.Rounded.Person,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        if (person.placeOfBirth.isNotBlank()) {
                            DescriptiveLabel(
                                label = stringResource(id = R.string.place_of_birth),
                                value = person.placeOfBirth
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

                // Biography snippet
                if (person.biography.isNotBlank()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    Text(
                        text = person.biography,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }
            }

            // Expandable popular media section
            AnimatedVisibility(visible = showPopularMedia) {
                person.popularMedia?.let { mediaList ->
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .padding(bottom = MaterialTheme.spacing.medium)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = MaterialTheme.spacing.medium,
                                    vertical = MaterialTheme.spacing.small
                                )
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
                            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium)
                        ) { media ->
                            Column(
                                modifier = Modifier.width(PopularMediaItemWidth)
                            ) {
                                MediaItem(
                                    title = media.title,
                                    posterImageUrl = media.posterImageUrl,
                                    titleTextStyle = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.width(PopularMediaItemWidth),
                                    posterModifier = Modifier
                                        .width(PopularMediaItemWidth)
                                        .aspectRatio(TmdbPosterAspectRatio),
                                    indicator = {
                                        if (media.voteAverage > 0) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                shape = RoundedCornerShape(topStart = 4.dp, bottomEnd = 4.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Star,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.tertiary,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = String.format("%.1f", media.voteAverage),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = { onMediaClick(media) }
                                )
                                
                                val releaseDate = media.displayReleaseDate
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(PopularMediaItemWidth)
                                ) {
                                    if (releaseDate != null) {
                                        Text(
                                            text = releaseDate.takeLast(4),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    
                                    if (media.character.isNotBlank()) {
                                        Text(
                                            text = media.character,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
private fun IconPillBadge(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
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
private fun DescriptiveLabel(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val cleanLabel = label.removeSuffix(":").removeSuffix(": ")
        Text(
            text = "$cleanLabel:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GradientAvatar(
    imageUrl: String,
    rank: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Avatar(
            imageUrl = imageUrl,
            onClick = onClick,
            modifier = Modifier.size(AvatarSize),
            borderWidth = 2.dp,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            borderSpacing = 2.dp
        )

        // Rank badge - simple circle
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = CircleShape,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopStart)
                .offset(x = (-4).dp, y = (-4).dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
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
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "ExpandButtonColor"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.size(36.dp),
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
                contentDescription = null,
                tint = if (expanded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private val AvatarSize = 80.dp
private val PopularMediaItemWidth = 100.dp
