package com.ssverma.feature.person.ui.details.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.person.R
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.person.PersonMedia
import com.ssverma.shared.ui.TmdbPosterAspectRatio

@Composable
fun PersonTimelineItem(
    media: PersonMedia,
    onInfoIconClick: () -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {

        Column(
            modifier = Modifier
                .weight(0.25f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = media.displayReleaseDate ?: "N/A",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
            VerticalDivider(
                modifier = Modifier.weight(1f),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        Surface(
            onClick = {
                when (media.mediaType) {
                    MediaType.Movie -> {
                        openMovieDetails(media.id)
                    }

                    MediaType.Tv -> {
                        openTvShowDetails(media.id)
                    }

                    else -> {
                        // no-op
                    }
                }
            },
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.75f)
                .padding(start = MaterialTheme.spacing.small)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                NetworkImage(
                    url = media.posterImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .width(48.dp)
                        .aspectRatio(TmdbPosterAspectRatio)
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.smallMedium)
                        .weight(1f)
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Emphasize {
                        Text(
                            text = stringResource(id = R.string.as_n, media.character),
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                IconButton(
                    onClick = onInfoIconClick,
                    modifier = Modifier.padding(end = MaterialTheme.spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
