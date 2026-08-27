package com.ssverma.feature.person.ui.home.component

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ssverma.feature.person.R
import com.ssverma.feature.person.ui.details.component.asUiText
import com.ssverma.shared.domain.model.person.Person
import com.ssverma.shared.domain.model.person.PersonMedia
import com.ssverma.shared.domain.utils.FormatterUtils
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.Avatar
import com.ssverma.shared.ui.component.media.MediaItem
import com.ssverma.shared.ui.component.personSharedContentKey

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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                // Top row: Avatar + Info + Expand button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar with rank badge
                    RankAvatar(
                        personId = person.id,
                        imageUrl = person.imageUrl,
                        rank = index + 1,
                        onClick = onClick
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    // Name and descriptive data
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (person.knownFor.isNotBlank()) {
                                M3PillBadge(
                                    text = person.knownFor,
                                    icon = Icons.Rounded.Movie,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                        alpha = 0.6f
                                    ),
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            M3PillBadge(
                                text = stringResource(id = person.gender.asUiText().resId),
                                icon = Icons.Rounded.Person,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (person.placeOfBirth.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = person.placeOfBirth,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Expand/Collapse button for known-for media
                    if (!person.popularMedia.isNullOrEmpty()) {
                        IconButton(
                            onClick = { onPopularMediaBtnClick(person.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (showPopularMedia)
                                    Icons.Rounded.KeyboardArrowUp
                                else
                                    Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Biography snippet (if available)
                if (person.biography.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = person.biography,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }

            // Expandable popular media section
            AnimatedVisibility(visible = showPopularMedia) {
                person.popularMedia?.let { mediaList ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(bottom = 12.dp)
                    ) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            thickness = 0.5.dp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.popular_media),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalLazyList(
                            items = mediaList,
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) { media ->
                            Column(
                                modifier = Modifier.width(PopularMediaItemWidth)
                            ) {
                                MediaItem(
                                    title = media.title,
                                    posterImageUrl = media.posterImageUrl,
                                    titleTextStyle = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(PopularMediaItemWidth),
                                    posterModifier = Modifier
                                        .width(PopularMediaItemWidth)
                                        .aspectRatio(TmdbPosterAspectRatio)
                                        .clip(RoundedCornerShape(10.dp)),
                                    indicator = {
                                        if (media.voteAverage > 0) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(
                                                    alpha = 0.85f
                                                ),
                                                shape = RoundedCornerShape(
                                                    topStart = 4.dp,
                                                    bottomEnd = 4.dp
                                                )
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(
                                                        horizontal = 4.dp,
                                                        vertical = 2.dp
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Star,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = FormatterUtils.formatRating(media.voteAverage * 10f),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
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
                                    modifier = Modifier
                                        .width(PopularMediaItemWidth)
                                        .padding(top = 2.dp)
                                ) {
                                    if (releaseDate != null) {
                                        Text(
                                            text = releaseDate.takeLast(4),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
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
        }
    }
}

@Composable
private fun M3PillBadge(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
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
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RankAvatar(
    personId: Int,
    imageUrl: String,
    rank: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Avatar(
            imageUrl = imageUrl,
            onClick = onClick,
            size = AvatarSize,
            borderWidth = 1.dp,
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            borderSpacing = 2.dp,
            enableSharedTransition = true,
            sharedContentKey = personSharedContentKey(personId, source = "person_list")
        )

        // Rank badge - subtle circular pill at corner
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shape = CircleShape,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}

private val AvatarSize = 56.dp
private val PopularMediaItemWidth = 92.dp
