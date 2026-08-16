package com.ssverma.feature.person.ui.details.content

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.core.ui.layout.HorizontalLazyListIndexed
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.person.R
import com.ssverma.feature.person.analytics.PersonAnalyticsEvent
import com.ssverma.feature.person.analytics.PersonAnalyticsScreenName
import com.ssverma.feature.person.analytics.PersonAnalyticsValues
import com.ssverma.feature.person.ui.common.PersonDetailUiState
import com.ssverma.feature.person.ui.details.component.PersonDetailsBackdropHeader
import com.ssverma.feature.person.ui.details.component.PersonMediaTabRow
import com.ssverma.feature.person.ui.details.component.PersonTimelineItem
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.ui.TmdbPersonAspectRatio
import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.ImageShotItem
import com.ssverma.shared.ui.component.section.OverviewSection
import com.ssverma.shared.ui.component.section.SectionDefaults
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionVerticalSpacing
import com.ssverma.core.ui.R as CoreUiR
import com.ssverma.shared.ui.R as SharedUiR

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PersonDetailsContent(
    personId: Int,
    initialName: String?,
    initialImageUrl: String?,
    personState: PersonDetailUiState,
    onRetry: () -> Unit,
    onBackPress: () -> Unit,
    openImagePage: (Int) -> Unit,
    openMovieDetails: (movieId: Int) -> Unit,
    openTvShowDetails: (tvShowId: Int) -> Unit,
    openPersonAllImages: (personId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics = LocalAnalytics.current
    val person = (personState as? UiState.Success)?.data

    var selectedMediaType by remember(person?.mediaByType) {
        mutableStateOf(person?.mediaByType?.keys?.firstOrNull() ?: MediaType.Movie)
    }

    var clickedMediaInfo: String? by remember {
        mutableStateOf(null)
    }

    if (clickedMediaInfo != null) {
        Dialog(onDismissRequest = { clickedMediaInfo = null }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.large)) {
                    Text(
                        text = stringResource(id = SharedUiR.string.overview),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.smallMedium))
                    Text(
                        text = clickedMediaInfo.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }

    val displayName = person?.name ?: initialName
    val displayProfileUrl = person?.imageUrl ?: initialImageUrl.orEmpty()
    val displayBackdropUrl =
        person?.imageShots?.lastOrNull()?.imageUrl ?: person?.imageUrl ?: initialImageUrl.orEmpty()

    LazyColumn(modifier = modifier) {
        item {
            PersonDetailsBackdropHeader(
                personId = personId,
                backdropImageUrl = displayBackdropUrl,
                profileImageUrl = displayProfileUrl,
                onBackPress = onBackPress
            )
        }

        item {
            if (!displayName.isNullOrBlank()) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = MaterialTheme.spacing.medium,
                            start = MaterialTheme.spacing.medium,
                            end = MaterialTheme.spacing.medium
                        )
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.medium)
                        .padding(horizontal = MaterialTheme.spacing.medium)
                ) {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .width(180.dp)
                            .height(28.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }

        if (person != null) {
            item {
                val na = stringResource(id = R.string.na)

                Highlights(
                    highlights = remember(person) {
                        listOf(
                            Highlight(labelRes = R.string.known_for, person.knownFor),
                            Highlight(labelRes = R.string.place_of_birth, person.placeOfBirth),
                            Highlight(labelRes = R.string.dob, person.dob ?: na),
                        )
                    },
                    modifier = Modifier.padding(top = MaterialTheme.spacing.large)
                )
            }

            item {
                OverviewSection(
                    overview = person.biography,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
                )
            }

            item {
                Section(
                    sectionHeader = {
                        SectionHeader(
                            title = stringResource(id = SharedUiR.string.shots),
                            trailingActionLabel = stringResource(id = R.string.see_more),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onTrailingActionClicked = {
                                openPersonAllImages(person.id)
                            },
                        )
                    },
                    headerContentSpacing = SectionDefaults.SectionContentHeaderSpacing,
                    hideIf = person.imageShots.isEmpty(),
                    modifier = Modifier.padding(top = SectionVerticalSpacing)
                ) {
                    HorizontalLazyListIndexed(items = person.imageShots) { index, imageShot ->
                        ImageShotItem(
                            imageShot = imageShot,
                            onClick = { openImagePage(index) },
                            modifier = Modifier
                                .height(200.dp)
                                .aspectRatio(TmdbPersonAspectRatio)
                        )
                    }
                }
            }

            if (person.mediaByType.isNotEmpty()) {
                stickyHeader {
                    PersonMediaTabRow(
                        personMediaByType = person.mediaByType,
                        selectedMediaType = selectedMediaType,
                        onMediaTypeSelected = { selectedMediaType = it },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .statusBarsPadding()
                    )
                }

                val mediaList = person.mediaByType[selectedMediaType] ?: emptyList()

                itemsIndexed(mediaList) { index, media ->
                    PersonTimelineItem(
                        media = media,
                        onInfoIconClick = { clickedMediaInfo = media.overview },
                        openMovieDetails = { movieId ->
                            analytics.logEvent(
                                PersonAnalyticsEvent.MediaClicked(
                                    media = media,
                                    section = PersonAnalyticsValues.SECTION_MOVIE_CREDITS,
                                    sourceScreen = PersonAnalyticsScreenName.PERSON_DETAILS
                                )
                            )
                            openMovieDetails(movieId)
                        },
                        openTvShowDetails = { tvShowId ->
                            analytics.logEvent(
                                PersonAnalyticsEvent.MediaClicked(
                                    media = media,
                                    section = PersonAnalyticsValues.SECTION_TV_CREDITS,
                                    sourceScreen = PersonAnalyticsScreenName.PERSON_DETAILS
                                )
                            )
                            openTvShowDetails(tvShowId)
                        },
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.medium)
                            .padding(
                                vertical = if (index == 0 || index == mediaList.lastIndex)
                                    MaterialTheme.spacing.medium
                                else
                                    MaterialTheme.spacing.small
                            )
                    )
                }
            }
        } else if (personState is UiState.Error) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.large)
                ) {
                    Text(
                        text = stringResource(id = CoreUiR.string.something_went_wrong),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    Button(onClick = onRetry) {
                        Text(text = stringResource(id = CoreUiR.string.retry))
                    }
                }
            }
        } else {
            // Loading placeholder shimmers below header and name
            item {
                Highlights(
                    highlights = listOf(
                        Highlight(labelRes = R.string.known_for, value = "—"),
                        Highlight(labelRes = R.string.place_of_birth, value = "—"),
                        Highlight(labelRes = R.string.dob, value = "—"),
                    ),
                    modifier = Modifier.padding(top = MaterialTheme.spacing.large)
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .padding(top = MaterialTheme.spacing.large)
                ) {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(16.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
            }
        }
    }
}
