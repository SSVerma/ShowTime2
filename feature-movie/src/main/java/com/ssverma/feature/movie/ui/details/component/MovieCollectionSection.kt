package com.ssverma.feature.movie.ui.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.R
import com.ssverma.shared.domain.model.movie.MovieCollection
import com.ssverma.shared.ui.TmdbBackdropAspectRatio
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionContentHeaderSpacing

@Composable
fun MovieCollectionSection(
    movieCollection: MovieCollection,
    modifier: Modifier = Modifier
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                // Background collection backdrop with gradient scrim
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
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
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
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .width(60.dp)
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
                        Text(
                            text = stringResource(id = R.string.franchise_collection),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primaryContainer
                        )
                        Text(
                            text = movieCollection.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(
                                id = R.string.part_of_collection,
                                movieCollection.name
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
