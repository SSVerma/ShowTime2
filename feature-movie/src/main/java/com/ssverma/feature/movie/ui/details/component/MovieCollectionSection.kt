package com.ssverma.feature.movie.ui.details.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.R
import com.ssverma.shared.domain.model.movie.MovieCollection
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.media.MediaCardRatingBadge
import com.ssverma.shared.ui.component.media.ShowTimeMediaGridCard
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionContentHeaderSpacing

@Composable
fun MovieCollectionSection(
    movieCollection: MovieCollection,
    modifier: Modifier = Modifier,
    currentMovieId: Int = -1,
    isLoadingParts: Boolean = false,
    onMovieClick: (movieId: Int) -> Unit = {}
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.franchise_collection),
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                hideTrailingAction = true
            )
        },
        headerContentSpacing = SectionContentHeaderSpacing,
        modifier = modifier
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                CollectionBanner(movieCollection = movieCollection)

                if (movieCollection.overview.isNotBlank()) {
                    ExpandableCollectionOverview(overview = movieCollection.overview)
                }

                if (movieCollection.parts.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = MaterialTheme.spacing.medium,
                                end = MaterialTheme.spacing.medium,
                                top = MaterialTheme.spacing.small
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Movie,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Text(
                            text = stringResource(id = R.string.franchise_timeline),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.small
                        ),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        items(
                            items = movieCollection.parts,
                            key = { it.id },
                            contentType = { "collection_movie" }
                        ) { partMovie ->
                            val isCurrentMovie = partMovie.id == currentMovieId
                            val yearText = partMovie.releaseDate?.year?.toString()
                                ?: partMovie.displayReleaseDate.orEmpty()

                            ShowTimeMediaGridCard(
                                title = partMovie.title,
                                posterImageUrl = partMovie.posterImageUrl,
                                onClick = {
                                    if (!isCurrentMovie) {
                                        onMovieClick(partMovie.id)
                                    }
                                },
                                topStartBadge = if (!isCurrentMovie && partMovie.voteAvg > 0f) {
                                    { MediaCardRatingBadge(rating = partMovie.voteAvg) }
                                } else null,
                                bottomStartBadge = if (isCurrentMovie) {
                                    { NowViewingBadge() }
                                } else null,
                                subtitle = {
                                    Text(
                                        text = yearText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isCurrentMovie) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontWeight = if (isCurrentMovie) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                border = if (isCurrentMovie) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else {
                                    BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                },
                                containerColor = if (isCurrentMovie) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                                modifier = Modifier.width(115.dp)
                            )
                        }
                    }
                } else if (isLoadingParts) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.small
                        )
                    ) {
                        repeat(3) {
                            ShimmerPlaceholder(
                                modifier = Modifier
                                    .width(115.dp)
                                    .height(180.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableCollectionOverview(
    overview: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small
            )
    ) {
        Text(
            text = overview,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (isExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded) {
                    hasOverflow = textLayoutResult.hasVisualOverflow
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = hasOverflow || isExpanded) {
                    isExpanded = !isExpanded
                }
        )

        if (hasOverflow || isExpanded) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.extraSmall)
                    .clickable { isExpanded = !isExpanded }
            ) {
                Text(
                    text = stringResource(id = if (isExpanded) R.string.show_less else R.string.show_more),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun CollectionBanner(
    movieCollection: MovieCollection,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        if (movieCollection.backdropImageUrl.isNotBlank()) {
            NetworkImage(
                url = movieCollection.backdropImageUrl,
                contentDescription = movieCollection.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.92f),
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.75f),
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.medium)
        ) {
            if (movieCollection.posterImageUrl.isNotBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .width(68.dp)
                        .aspectRatio(TmdbPosterAspectRatio)
                        .clip(MaterialTheme.shapes.small)
                ) {
                    NetworkImage(
                        url = movieCollection.posterImageUrl,
                        contentDescription = movieCollection.name,
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = MaterialTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = stringResource(id = R.string.franchise_collection).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = movieCollection.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                val countText = if (movieCollection.parts.isNotEmpty()) {
                    stringResource(
                        id = R.string.n_movies_in_collection,
                        movieCollection.parts.size
                    )
                } else {
                    stringResource(
                        id = R.string.part_of_collection,
                        movieCollection.name
                    )
                }

                Text(
                    text = countText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NowViewingBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = stringResource(R.string.now_viewing),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
