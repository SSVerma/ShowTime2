package com.ssverma.feature.person.ui.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.person.R
import com.ssverma.feature.person.ui.details.component.asUiText
import com.ssverma.shared.domain.model.person.Person
import com.ssverma.shared.domain.model.person.PersonMedia
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.Avatar
import com.ssverma.shared.ui.component.AvatarDefaults
import com.ssverma.shared.ui.component.media.MediaItem
import com.ssverma.shared.ui.component.media.TextBadge

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
    val tonalElevation by animateDpAsState(
        targetValue = if (showPopularMedia) 8.dp else 0.dp,
        label = "PersonItemElevation"
    )

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = tonalElevation,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Index and Avatar
                Box(contentAlignment = Alignment.BottomEnd) {
                    Avatar(
                        imageUrl = person.imageUrl,
                        onClick = onClick,
                        modifier = Modifier.size(AvatarDefaults.Size * 1.5f),
                        borderWidth = 2.dp
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = person.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
                    ) {
                        TextBadge(
                            text = stringResource(id = person.gender.asUiText().resId),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        TextBadge(
                            text = person.knownFor,
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                if (!person.popularMedia.isNullOrEmpty()) {
                    IconButton(
                        onClick = { onPopularMediaBtnClick(person.id) }
                    ) {
                        Icon(
                            imageVector = if (showPopularMedia) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = stringResource(id = R.string.popular_media),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showPopularMedia,
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium)
            ) {
                person.popularMedia?.let {
                    Column {
                        Text(
                            text = stringResource(id = R.string.popular_media),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                        )
                        HorizontalLazyList(
                            items = it,
                            contentPadding = PaddingValues(0.dp)
                        ) { media ->
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
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

private val PopularMediaItemWidth = 80.dp
