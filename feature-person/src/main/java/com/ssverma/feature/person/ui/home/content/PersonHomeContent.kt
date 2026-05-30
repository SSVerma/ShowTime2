package com.ssverma.feature.person.ui.home.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.ssverma.core.analytics.ui.LocalAnalytics
import com.ssverma.core.ui.paging.PagedListIndexed
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.person.analytics.PersonAnalyticsEvent
import com.ssverma.feature.person.analytics.PersonAnalyticsScreenName
import com.ssverma.feature.person.analytics.PersonAnalyticsValues
import com.ssverma.feature.person.ui.home.component.PersonListItem
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.person.Person
import com.ssverma.shared.ui.component.HomePageAppBar

@Composable
fun PersonHomeContent(
    pagedPersons: LazyPagingItems<Person>,
    openPersonDetailsScreen: (personId: Int) -> Unit,
    openMovieDetailsScreen: (movieId: Int) -> Unit,
    openTvShowDetailsScreen: (tvShowId: Int) -> Unit,
    openSearchPage: () -> Unit,
    openAccountPage: () -> Unit,
    openLibraryPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val analytics = LocalAnalytics.current
    var selectedPersonId by rememberSaveable { mutableIntStateOf(-1) }

    PagedListIndexed(
        pagingItems = pagedPersons,
        contentPadding = PaddingValues(
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        header = {
            HomePageAppBar(
                onSearchIconPressed = openSearchPage,
                onAccountIconPressed = openAccountPage,
                onLibraryIconPressed = openLibraryPage,
                modifier = Modifier.statusBarsPadding()
            )
        },
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
                openPersonDetailsScreen(person.id)
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
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        )
    }
}
