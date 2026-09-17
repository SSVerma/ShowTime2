package com.ssverma.feature.person.ui.details.content

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.platform.LocalContext
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
import com.ssverma.feature.person.ui.details.component.PersonHighlightsCard
import com.ssverma.feature.person.ui.details.component.PersonKnownForShelf
import com.ssverma.feature.person.ui.details.component.PersonMediaTabRow
import com.ssverma.feature.person.ui.details.component.PersonSocialLinksRow
import com.ssverma.feature.person.ui.details.component.PersonTimelineItem
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.ui.TmdbPersonAspectRatio
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
    modifier: Modifier = Modifier,
    source: String = "default"
) {
    val context = LocalContext.current
    val analytics = LocalAnalytics.current
    val person = (personState as? UiState.Success)?.data

    var selectedMediaType by remember(person?.mediaByType) {
        mutableStateOf(person?.mediaByType?.keys?.firstOrNull() ?: MediaType.Movie)
    }

    var clickedMediaInfo: String? by remember {
        mutableStateOf(null)
    }

    val onSharePerson: () -> Unit = {
        if (person != null) {
            val shareText = context.getString(
                R.string.person_share_text,
                person.name,
                "https://www.themoviedb.org/person/${person.id}"
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    context.getString(R.string.person_share_subject, person.name)
                )
                putExtra(Intent.EXTRA_TEXT, shareText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, person.name))
        }
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
                onBackPress = onBackPress,
                onShareClick = onSharePerson,
                source = source
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
                            top = 8.dp,
                            start = MaterialTheme.spacing.medium,
                            end = MaterialTheme.spacing.medium
                        )
                )
            } else {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
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
            // Social Links Row (IMDb, Instagram, X/Twitter, TikTok, Website)
            item {
                PersonSocialLinksRow(
                    externalIds = person.externalIds,
                    homepage = person.homepage,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // Rich Biographical Highlights Card (Age, Place of Birth, Known For, Aliases)
            item {
                PersonHighlightsCard(
                    person = person,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }

            // Overview / Biography
            item {
                OverviewSection(
                    overview = person.biography,
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )
            }

            // Known For & Signature Career Highlights
            item {
                val knownForItems = remember(person) {
                    person.popularMedia?.takeIf { it.isNotEmpty() }
                        ?: person.mediaByType.values.flatten().sortedByDescending { it.voteAverage }
                            .take(6)
                }
                PersonKnownForShelf(
                    mediaList = knownForItems,
                    onMediaClick = { media ->
                        when (media.mediaType) {
                            MediaType.Movie -> openMovieDetails(media.id)
                            MediaType.Tv -> openTvShowDetails(media.id)
                            else -> { /* no-op */
                            }
                        }
                    }
                )
            }

            // Photos / Shots Gallery
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

            // Complete Filmography Timeline
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

                itemsIndexed(
                    items = mediaList,
                    key = { index, media -> "${media.id}_${media.character}_${media.job}_$index" },
                    contentType = { _, _ -> "person_timeline_item" }
                ) { index, media ->
                    PersonTimelineItem(
                        media = media,
                        isFirstItem = index == 0,
                        isLastItem = index == mediaList.lastIndex,
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
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .padding(top = MaterialTheme.spacing.large)
                ) {
                    ShimmerPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                }
            }
        }

        item {
            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(48.dp)
            )
        }
    }
}
