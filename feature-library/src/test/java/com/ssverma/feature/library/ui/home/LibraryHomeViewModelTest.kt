package com.ssverma.feature.library.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.feature.library.ui.home.component.MediaTypeFilter
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.usecase.community.CloneCommunityListUseCase
import com.ssverma.shared.domain.usecase.community.GetCommunityListDetailsUseCase
import com.ssverma.shared.domain.usecase.community.GetCommunityListsUseCase
import com.ssverma.shared.domain.usecase.community.PublishCustomListUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommunityListUpvoteUseCase
import com.ssverma.shared.domain.usecase.community.UnpublishCustomListUseCase
import com.ssverma.shared.testing.fakes.FakeCommunityRepository
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LibraryHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var fakeCommunityRepository: FakeCommunityRepository
    private lateinit var viewModel: LibraryHomeViewModel

    @Before
    fun setUp() {
        fakeLibraryRepository = FakeLibraryRepository()
        fakeCommunityRepository = FakeCommunityRepository()
        viewModel = LibraryHomeViewModel(
            libraryRepository = fakeLibraryRepository,
            getCommunityListsUseCase = GetCommunityListsUseCase(
                communityRepository = fakeCommunityRepository
            ),
            getCommunityListDetailsUseCase = GetCommunityListDetailsUseCase(
                communityRepository = fakeCommunityRepository
            ),
            publishCustomListUseCase = PublishCustomListUseCase(
                communityRepository = fakeCommunityRepository,
                libraryRepository = fakeLibraryRepository
            ),
            unpublishCustomListUseCase = UnpublishCustomListUseCase(
                communityRepository = fakeCommunityRepository,
                libraryRepository = fakeLibraryRepository
            ),
            toggleCommunityListUpvoteUseCase = ToggleCommunityListUpvoteUseCase(
                communityRepository = fakeCommunityRepository
            ),
            cloneCommunityListUseCase = CloneCommunityListUseCase(
                libraryRepository = fakeLibraryRepository,
                communityRepository = fakeCommunityRepository
            )
        )
    }

    @Test
    fun `watchlistItems reflects added and removed watchlist items`() = runTest {
        viewModel.watchlistItems.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.toggleWatchlist(
                mediaId = 101,
                mediaType = MediaType.Movie,
                title = "Inception",
                posterImageUrl = "/inception.jpg",
                backdropImageUrl = "/backdrop.jpg",
                voteAvg = 8.8f,
                releaseDate = "2010-07-16"
            )

            val updated = awaitItem()
            assertThat(updated).hasSize(1)
            assertThat(updated.first().title).isEqualTo("Inception")
            assertThat(updated.first().mediaId).isEqualTo(101)

            viewModel.removeFromWatchlist(101)
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `favoriteItems reflects added and removed favorite items`() = runTest {
        viewModel.favoriteItems.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.toggleFavorite(
                mediaId = 202,
                mediaType = MediaType.Movie,
                title = "Interstellar",
                posterImageUrl = "/interstellar.jpg",
                backdropImageUrl = "/backdrop2.jpg",
                voteAvg = 8.6f,
                releaseDate = "2014-11-07"
            )

            val updated = awaitItem()
            assertThat(updated).hasSize(1)
            assertThat(updated.first().title).isEqualTo("Interstellar")

            viewModel.removeFromFavorites(202)
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `historyItems reflects logged history and clear action`() = runTest {
        viewModel.historyItems.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.logWatchHistory(
                mediaId = 303,
                mediaType = MediaType.Movie,
                title = "Oppenheimer",
                posterImageUrl = "/oppenheimer.jpg",
                voteAvg = 8.9f
            )

            val updated = awaitItem()
            assertThat(updated).hasSize(1)
            assertThat(updated.first().title).isEqualTo("Oppenheimer")

            viewModel.clearHistory()
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `filter state updates correctly`() = runTest {
        assertThat(viewModel.watchlistFilter.value).isEqualTo(MediaTypeFilter.ALL)
        viewModel.setWatchlistFilter(MediaTypeFilter.MOVIE)
        assertThat(viewModel.watchlistFilter.value).isEqualTo(MediaTypeFilter.MOVIE)

        viewModel.setFavoritesFilter(MediaTypeFilter.TV)
        assertThat(viewModel.favoritesFilter.value).isEqualTo(MediaTypeFilter.TV)

        viewModel.setHistoryFilter(MediaTypeFilter.MOVIE)
        assertThat(viewModel.historyFilter.value).isEqualTo(MediaTypeFilter.MOVIE)
    }

    @Test
    fun `custom lists create, select, and delete work as expected`() = runTest {
        viewModel.customLists.test {
            assertThat(awaitItem()).isEmpty()

            var createdListId = ""
            viewModel.createCustomList(
                title = "Sci-Fi Masterpieces",
                description = "Mind bending movies",
                onCreated = { createdListId = it }
            )

            val lists = awaitItem()
            assertThat(lists).hasSize(1)
            assertThat(lists.first().title).isEqualTo("Sci-Fi Masterpieces")

            viewModel.selectCustomList(lists.first().listId)
            assertThat(viewModel.selectedCustomListId.value).isEqualTo(lists.first().listId)

            viewModel.deleteCustomList(lists.first().listId)
            val emptyLists = awaitItem()
            assertThat(emptyLists).isEmpty()
            assertThat(viewModel.selectedCustomListId.value).isNull()
        }
    }

    @Test
    fun `publishCustomList and unpublishCustomList work as expected`() = runTest {
        viewModel.communityLists.test {
            assertThat(awaitItem()).isEmpty()

            // 1. Create a local list
            fakeLibraryRepository.createCustomList("Cyberpunk Essentials", "Top neon cinema")
            val localLists = fakeLibraryRepository.getAllCustomLists()
            val localList = localLists.first()

            // 2. Publish to community
            viewModel.publishCustomList(localList, "Sci-Fi Essentials")

            val publishedLists = awaitItem()
            assertThat(publishedLists).hasSize(1)
            assertThat(publishedLists.first().title).isEqualTo("Cyberpunk Essentials")
            assertThat(publishedLists.first().categoryTag).isEqualTo("Sci-Fi Essentials")

            // 3. Unpublish from community
            viewModel.unpublishCustomList(localList.listId)
            val afterUnpublish = awaitItem()
            assertThat(afterUnpublish).isEmpty()
        }
    }

    @Test
    fun `toggleCommunityListUpvote updates upvote status`() = runTest {
        viewModel.communityLists.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.createCustomList("A24 Wonders", "Artistic movies")
            val localList = fakeLibraryRepository.getAllCustomLists().first()
            viewModel.publishCustomList(localList, "A24 Gems")

            val list = awaitItem().first()
            assertThat(list.isUpvotedByMe).isFalse()

            viewModel.toggleCommunityListUpvote(list.listId)
            val upvotedList = awaitItem().first()
            assertThat(upvotedList.isUpvotedByMe).isTrue()
            assertThat(upvotedList.upvotesCount).isEqualTo(1L)
        }
    }

    @Test
    fun `cloneCommunityList clones collection into local library`() = runTest {
        viewModel.communityLists.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.createCustomList("Classic Noir", "Dark alleys")
            val localList = fakeLibraryRepository.getAllCustomLists().first()
            viewModel.publishCustomList(localList, "Hidden Gems")
            val published = awaitItem().first()

            var clonedId: String? = null
            viewModel.cloneCommunityList(published) { id -> clonedId = id }

            val afterClone = awaitItem()
            assertThat(afterClone.first().isClonedByMe).isTrue()

            val myLists = fakeLibraryRepository.getAllCustomLists()
            assertThat(myLists.any { it.title.contains("Classic Noir") }).isTrue()
        }
    }
}
