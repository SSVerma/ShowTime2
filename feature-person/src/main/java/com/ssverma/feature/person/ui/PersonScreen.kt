package com.ssverma.feature.person.ui

import MediaItem
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.ui.UiText
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.paging.PagedContent
import com.ssverma.core.ui.paging.PagedListIndexed
import com.ssverma.feature.person.R
import com.ssverma.shared.domain.model.Gender
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.person.Person
import com.ssverma.shared.domain.model.person.PersonMedia
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.HomePageAppBar

@Composable
fun PersonScreen(
    viewModel: PersonHomeViewModel,
    openPersonDetailsScreen: (personId: Int) -> Unit,
    openMovieDetailsScreen: (movieId: Int) -> Unit,
    openTvShowDetailsScreen: (tvShowId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit
) {
    val pagedPersons = viewModel.popularPersons.collectAsLazyPagingItems()

    var selectedPersonId by remember { mutableStateOf(-1) }

    PagedContent(pagingItems = pagedPersons) {
        PagedListIndexed(
            pagingItems = it,
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            header = {
                HomePageAppBar(
                    backgroundColor = MaterialTheme.colors.background,
                    onSearchIconPressed = openSearchPage,
                    onAccountIconPressed = openAccountPage,
                    elevation = 0.dp,
                    modifier = Modifier.statusBarsPadding()
                )
            }
        ) { index, person ->
            PersonItem(
                person = person,
                index = index,
                showPopularMedia = selectedPersonId == person.id,
                onClick = { openPersonDetailsScreen(person.id) },
                onPopularMediaBtnClick = { personId ->
                    if (selectedPersonId == personId) {
                        selectedPersonId = -1
                    } else {
                        selectedPersonId = personId
                    }
                },
                onMediaClick = { media ->
                    when (media.mediaType) {
                        MediaType.Movie -> {
                            openMovieDetailsScreen(media.id)
                        }
                        MediaType.Tv -> {
                            openTvShowDetailsScreen(media.id)
                        }
                        else -> {
                            //Do nothing
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun PersonItem(
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
            MaterialTheme.colors.primary.copy(alpha = 0.16f)
        } else {
            MaterialTheme.colors.background
        }
    )

    Row(modifier = modifier
        .fillMaxWidth()
        .background(color = rowBackgroundColor)
        .clickable { onClick() }
        .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "${index + 1}",
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 8.dp)
        )

        Column {
            Box(modifier = Modifier.height(if (showPopularMedia) 16.dp else 0.dp))

            Row(modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .clickable { onClick() }
                .padding(horizontal = 16.dp)
            ) {

                NetworkImage(
                    url = person.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .width(96.dp)
                        .aspectRatio(TmdbPosterAspectRatio)
                        .clip(MaterialTheme.shapes.medium.copy(CornerSize(8.dp)))
                )

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(text = person.name, style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            id = R.string.gender_n,
                            stringResource(id = person.gender.asUiText().resId)
                        ),
                        style = MaterialTheme.typography.caption
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.known_for_n, person.knownFor),
                        style = MaterialTheme.typography.caption
                    )

                    Box(modifier = Modifier.weight(1f))

                    if (!person.popularMedia.isNullOrEmpty()) {
                        TextButton(
                            onClick = { onPopularMediaBtnClick(person.id) },
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = stringResource(id = R.string.popular_media))
                                Spacer(modifier = Modifier.padding(4.dp))
                                Icon(
                                    imageVector = if (showPopularMedia) {
                                        AppIcons.KeyboardArrowUp
                                    } else {
                                        AppIcons.KeyboardArrowDown
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
                    .padding(vertical = 16.dp)
            ) {
                person.popularMedia?.let {
                    HorizontalLazyList(items = it) { media ->
                        MediaItem(
                            title = media.title,
                            posterImageUrl = media.posterImageUrl,
                            titleTextStyle = MaterialTheme.typography.caption,
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

private fun Gender.asUiText(): UiText.StaticText {
    return when (this) {
        Gender.Female -> UiText.StaticText(resId = R.string.female)
        Gender.Male -> UiText.StaticText(resId = R.string.male)
        Gender.Unknown -> UiText.StaticText(resId = R.string.unknown)
    }
}

private val PopularMediaItemWidth = 80.dp