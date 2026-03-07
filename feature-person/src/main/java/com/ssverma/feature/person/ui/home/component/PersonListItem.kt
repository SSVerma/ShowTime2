package com.ssverma.feature.person.ui.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.person.R
import com.ssverma.feature.person.ui.details.component.asUiText
import com.ssverma.shared.domain.model.person.Person
import com.ssverma.shared.domain.model.person.PersonMedia
import com.ssverma.shared.ui.TmdbPosterAspectRatio
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
    val rowBackgroundColor by animateColorAsState(
        targetValue = if (showPopularMedia) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colorScheme.background
        }, label = "PersonRowBgAnimation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = rowBackgroundColor)
            .clickable { onClick() }
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        Text(
            text = "${index + 1}",
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = MaterialTheme.spacing.small)
        )

        Column {
            Box(modifier = Modifier.height(if (showPopularMedia) MaterialTheme.spacing.medium else 0.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .clickable { onClick() }
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {

                NetworkImage(
                    url = person.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .width(96.dp)
                        .aspectRatio(TmdbPosterAspectRatio)
                        .clip(MaterialTheme.shapes.medium)
                )

                Column(modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)) {
                    Text(text = person.name, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                    Text(
                        text = stringResource(
                            id = R.string.gender_n,
                            stringResource(id = person.gender.asUiText().resId)
                        ),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                    Text(
                        text = stringResource(id = R.string.known_for_n, person.knownFor),
                        style = MaterialTheme.typography.labelSmall
                    )

                    Box(modifier = Modifier.weight(1f))

                    if (!person.popularMedia.isNullOrEmpty()) {
                        TextButton(
                            onClick = { onPopularMediaBtnClick(person.id) },
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = stringResource(id = R.string.popular_media))
                                Spacer(modifier = Modifier.padding(MaterialTheme.spacing.extraSmall))
                                Icon(
                                    imageVector = if (showPopularMedia) {
                                        Icons.Default.KeyboardArrowUp
                                    } else {
                                        Icons.Default.KeyboardArrowDown
                                    }, contentDescription = null
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = showPopularMedia,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.medium)
            ) {
                person.popularMedia?.let {
                    HorizontalLazyList(items = it) { media ->
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

private val PopularMediaItemWidth = 80.dp
