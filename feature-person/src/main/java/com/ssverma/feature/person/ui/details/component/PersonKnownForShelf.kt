package com.ssverma.feature.person.ui.details.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.person.R
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.person.PersonMedia
import com.ssverma.shared.domain.utils.FormatterUtils
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.media.MediaItem
import com.ssverma.shared.ui.component.section.SectionDefaults

@Composable
fun PersonKnownForShelf(
    mediaList: List<PersonMedia>,
    onMediaClick: (PersonMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    if (mediaList.isEmpty()) return

    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = R.string.person_known_for_shelf),
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
            )
        },
        headerContentSpacing = SectionDefaults.SectionContentHeaderSpacing,
        modifier = modifier.padding(top = SectionDefaults.SectionVerticalSpacing)
    ) {
        HorizontalLazyList(
            items = mediaList,
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium)
        ) { media ->
            Column(
                modifier = Modifier.width(KnownForItemWidth)
            ) {
                MediaItem(
                    title = media.title,
                    posterImageUrl = media.posterImageUrl,
                    titleTextStyle = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(KnownForItemWidth),
                    posterModifier = Modifier
                        .width(KnownForItemWidth)
                        .aspectRatio(TmdbPosterAspectRatio)
                        .clip(RoundedCornerShape(12.dp)),
                    indicator = {
                        if (media.voteAverage > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
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

                val releaseYear = media.displayReleaseDate?.takeLast(4)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(KnownForItemWidth)
                        .padding(top = 2.dp)
                ) {
                    if (releaseYear != null) {
                        Text(
                            text = releaseYear,
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

private val KnownForItemWidth = 110.dp
