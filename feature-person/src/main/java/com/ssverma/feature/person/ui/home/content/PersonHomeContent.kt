package com.ssverma.feature.person.ui.home.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.ui.paging.PagedListIndexed
import com.ssverma.feature.person.analytics.PersonAnalyticsEvent
import com.ssverma.feature.person.analytics.PersonAnalyticsScreenName
import com.ssverma.feature.person.analytics.PersonAnalyticsValues
import com.ssverma.feature.person.ui.home.component.PersonListItem
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.person.Person

@Composable
fun PersonHomeContent(
    pagedPersons: LazyPagingItems<Person>,
    openPersonDetailsScreen: (Person) -> Unit,
    openMovieDetailsScreen: (movieId: Int) -> Unit,
    openTvShowDetailsScreen: (tvShowId: Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val analytics = LocalAnalytics.current
    val layoutDirection = LocalLayoutDirection.current
    var selectedPersonId by rememberSaveable { mutableIntStateOf(-1) }

    val combinedPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(layoutDirection),
        top = contentPadding.calculateTopPadding() + 8.dp,
        end = contentPadding.calculateEndPadding(layoutDirection),
        bottom = contentPadding.calculateBottomPadding() + 80.dp
    )

    PagedListIndexed(
        pagingItems = pagedPersons,
        contentPadding = combinedPadding,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) { index, person ->
        PersonListItem(
            person = person,
            index = index,
            showPopularMedia = selectedPersonId == person.id,
            onClick = {
                analytics.logEvent(
                    PersonAnalyticsEvent.PersonClicked(
                        personId = person.id,
                        personName = person.name,
                        sourceScreen = PersonAnalyticsScreenName.PERSON_LISTING
                    )
                )
                openPersonDetailsScreen(person)
            },
            onPopularMediaBtnClick = { personId ->
                PersonAnalyticsEvent.ExpandMediaClicked(
                    personId = personId,
                    personName = person.name,
                    sourceScreen = PersonAnalyticsScreenName.PERSON_LISTING
                )
                selectedPersonId = if (selectedPersonId == personId) -1 else personId
            },
            onMediaClick = { media ->
                analytics.logEvent(
                    PersonAnalyticsEvent.MediaClicked(
                        media = media,
                        section = PersonAnalyticsValues.SECTION_KNOW_FOR,
                        sourceScreen = PersonAnalyticsScreenName.PERSON_LISTING
                    )
                )
                when (media.mediaType) {
                    MediaType.Movie -> openMovieDetailsScreen(media.id)
                    MediaType.Tv -> openTvShowDetailsScreen(media.id)
                    else -> { /* no-op */
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
