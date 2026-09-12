package com.ssverma.feature.library.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.manager.RewardedAdManager
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.testing.fakes.FakeBillingRepository
import com.ssverma.feature.library.ui.home.component.LibraryBackupBannerState
import com.ssverma.feature.library.ui.home.component.MediaTypeFilter
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.model.community.CommunityModerationConfig
import com.ssverma.shared.domain.model.community.CommunityReportReason
import com.ssverma.shared.domain.model.community.ContentModerationResult
import com.ssverma.shared.domain.model.community.ReportCommunityListParams
import com.ssverma.shared.domain.usecase.community.AcceptCommunityGuidelinesUseCase
import com.ssverma.shared.domain.usecase.community.BlockCommunityUserUseCase
import com.ssverma.shared.domain.usecase.community.CloneCommunityListUseCase
import com.ssverma.shared.domain.usecase.community.DeleteCommunityListUseCase
import com.ssverma.shared.domain.usecase.community.GetCommunityListDetailsUseCase
import com.ssverma.shared.domain.usecase.community.GetCommunityListsUseCase
import com.ssverma.shared.domain.usecase.community.HasAcceptedCommunityGuidelinesUseCase
import com.ssverma.shared.domain.usecase.community.PublishCustomListUseCase
import com.ssverma.shared.domain.usecase.community.ReportCommunityListUseCase
import com.ssverma.shared.domain.usecase.community.ToggleCommunityListUpvoteUseCase
import com.ssverma.shared.domain.usecase.community.UnpublishCustomListUseCase
import com.ssverma.shared.domain.usecase.community.ValidateCommunityContentUseCase
import com.ssverma.shared.testing.fakes.FakeBackupRepository
import com.ssverma.shared.testing.fakes.FakeCommunityRepository
import com.ssverma.shared.testing.fakes.FakeLibraryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LibraryHomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeLibraryRepository: FakeLibraryRepository
    private lateinit var fakeCommunityRepository: FakeCommunityRepository
    private lateinit var fakeBackupRepository: FakeBackupRepository
    private val fakeBillingRepository = FakeBillingRepository(initialProActive = false)
    private val mockRewardManager: RewardManager = mockk(relaxed = true)
    private val mockRewardedAdManager: RewardedAdManager = mockk(relaxed = true)
    private lateinit var viewModel: LibraryHomeViewModel

    @Before
    fun setUp() {
        fakeLibraryRepository = FakeLibraryRepository()
        fakeCommunityRepository = FakeCommunityRepository()
        fakeBackupRepository = FakeBackupRepository()
        coEvery {
            mockRewardManager.canPerformQuotaAction(
                CustomListPassKey,
                any(),
                any(),
                any()
            )
        } returns true

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
            ),
            deleteCommunityListUseCase = DeleteCommunityListUseCase(
                communityRepository = fakeCommunityRepository
            ),
            reportCommunityListUseCase = ReportCommunityListUseCase(
                communityRepository = fakeCommunityRepository
            ),
            blockCommunityUserUseCase = BlockCommunityUserUseCase(
                communityRepository = fakeCommunityRepository
            ),
            validateCommunityContentUseCase = ValidateCommunityContentUseCase(
                communityRepository = fakeCommunityRepository
            ),
            hasAcceptedCommunityGuidelinesUseCase = HasAcceptedCommunityGuidelinesUseCase(
                communityRepository = fakeCommunityRepository
            ),
            acceptCommunityGuidelinesUseCase = AcceptCommunityGuidelinesUseCase(
                communityRepository = fakeCommunityRepository
            ),
            communityRepository = fakeCommunityRepository,
            rewardManager = mockRewardManager,
            rewardedAdManager = mockRewardedAdManager,
            billingRepository = fakeBillingRepository,
            backupRepository = fakeBackupRepository
        )
    }

    @Test
    fun `onAttemptCreateList opens create list dialog when under quota`() = runTest {
        coEvery {
            mockRewardManager.canPerformQuotaAction(
                CustomListPassKey,
                0,
                any(),
                false
            )
        } returns true

        viewModel.onAttemptCreateList()

        assertThat(viewModel.isCreateListDialogVisible.value).isTrue()
        assertThat(viewModel.isQuotaGateVisible.value).isFalse()
    }

    @Test
    fun `onAttemptCreateList triggers quota gate when limit reached for free user`() = runTest {
        coEvery {
            mockRewardManager.canPerformQuotaAction(
                CustomListPassKey,
                any(),
                any(),
                false
            )
        } returns false

        viewModel.onAttemptCreateList()

        assertThat(viewModel.isQuotaGateVisible.value).isTrue()
        assertThat(viewModel.isCreateListDialogVisible.value).isFalse()
    }

    @Test
    fun `dismissQuotaGate closes bottom sheet`() = runTest {
        coEvery {
            mockRewardManager.canPerformQuotaAction(
                CustomListPassKey,
                any(),
                any(),
                false
            )
        } returns false
        viewModel.onAttemptCreateList()
        assertThat(viewModel.isQuotaGateVisible.value).isTrue()

        viewModel.dismissQuotaGate()
        assertThat(viewModel.isQuotaGateVisible.value).isFalse()
    }

    @Test
    fun `dismissCreateListDialog closes create dialog`() = runTest {
        viewModel.onAttemptCreateList()
        assertThat(viewModel.isCreateListDialogVisible.value).isTrue()

        viewModel.dismissCreateListDialog()
        assertThat(viewModel.isCreateListDialogVisible.value).isFalse()
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
    fun `favoriteItems reflects added and removed favorites`() = runTest {
        viewModel.favoriteItems.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.toggleFavorite(
                mediaId = 202,
                mediaType = MediaType.Tv,
                title = "Dark",
                posterImageUrl = "/dark.jpg",
                backdropImageUrl = "/dark_back.jpg",
                voteAvg = 9.0f,
                releaseDate = "2017-12-01"
            )

            val updated = awaitItem()
            assertThat(updated).hasSize(1)
            assertThat(updated.first().title).isEqualTo("Dark")

            viewModel.removeFromFavorites(202)
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `historyItems reflects added history and clears history`() = runTest {
        viewModel.historyItems.test {
            assertThat(awaitItem()).isEmpty()

            fakeLibraryRepository.toggleWatchHistory(
                mediaId = 303,
                mediaType = MediaType.Movie,
                title = "Interstellar",
                posterImageUrl = "/interstellar.jpg",
                voteAvg = 8.6f
            )

            val updated = awaitItem()
            assertThat(updated).hasSize(1)
            assertThat(updated.first().title).isEqualTo("Interstellar")

            viewModel.clearHistory()
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `customLists reflects created, updated, and deleted lists`() = runTest {
        viewModel.customLists.test {
            assertThat(awaitItem()).isEmpty()

            viewModel.createCustomList("Cyberpunk Essentials", "Top neon cinema")

            val created = awaitItem()
            assertThat(created).hasSize(1)
            val list = created.first()
            assertThat(list.title).isEqualTo("Cyberpunk Essentials")

            viewModel.updateCustomList(list.listId, "Neo Tokyo", "Updated description")
            val updated = awaitItem()
            assertThat(updated.first().title).isEqualTo("Neo Tokyo")

            viewModel.deleteCustomList(list.listId)
            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun `filters update state flow correctly`() = runTest {
        viewModel.setWatchlistFilter(MediaTypeFilter.MOVIE)
        assertThat(viewModel.watchlistFilter.value).isEqualTo(MediaTypeFilter.MOVIE)

        viewModel.setFavoritesFilter(MediaTypeFilter.TV)
        assertThat(viewModel.favoritesFilter.value).isEqualTo(MediaTypeFilter.TV)

        viewModel.setHistoryFilter(MediaTypeFilter.MOVIE)
        assertThat(viewModel.historyFilter.value).isEqualTo(MediaTypeFilter.MOVIE)
    }

    @Test
    fun `backupBannerState shows RESTORE_AVAILABLE when library is empty and cloud backup exists`() =
        runTest {
            viewModel.backupBannerState.test {
                assertThat(awaitItem()).isEqualTo(LibraryBackupBannerState.HIDDEN)

                val sampleBackup = BackupMetadata(
                    timestamp = System.currentTimeMillis(),
                    formattedDate = "Today",
                    deviceName = "Pixel 8",
                    sizeBytes = 1024,
                    formattedSize = "1 KB",
                    favoritesCount = 2,
                    watchlistCount = 3,
                    historyCount = 1,
                    customListsCount = 0,
                    customListItemsCount = 0
                )

                fakeBackupRepository.setLastBackupMetadata(sampleBackup)
                assertThat(awaitItem()).isEqualTo(LibraryBackupBannerState.RESTORE_AVAILABLE)
            }
        }

    @Test
    fun `backupBannerState shows BACKUP_RECOMMENDED when library has 3 or more items and no backup exists`() =
        runTest {
            viewModel.backupBannerState.test {
                assertThat(awaitItem()).isEqualTo(LibraryBackupBannerState.HIDDEN)

                fakeLibraryRepository.toggleWatchlist(
                    1,
                    MediaType.Movie,
                    "Movie 1",
                    "/p1.jpg",
                    "",
                    7f,
                    "2024-01-01"
                )
                fakeLibraryRepository.toggleWatchlist(
                    2,
                    MediaType.Movie,
                    "Movie 2",
                    "/p2.jpg",
                    "",
                    8f,
                    "2024-01-01"
                )
                fakeLibraryRepository.toggleFavorite(
                    3,
                    MediaType.Movie,
                    "Movie 3",
                    "/p3.jpg",
                    "",
                    9f,
                    "2024-01-01"
                )

                assertThat(awaitItem()).isEqualTo(LibraryBackupBannerState.BACKUP_RECOMMENDED)
            }
        }

    @Test
    fun `backupBannerState shows HIDDEN when dismissed by user`() = runTest {
        viewModel.backupBannerState.test {
            assertThat(awaitItem()).isEqualTo(LibraryBackupBannerState.HIDDEN)

            val sampleBackup = BackupMetadata(
                timestamp = System.currentTimeMillis(),
                formattedDate = "Today",
                deviceName = "Pixel 8",
                sizeBytes = 1024,
                formattedSize = "1 KB",
                favoritesCount = 2,
                watchlistCount = 3,
                historyCount = 1,
                customListsCount = 0,
                customListItemsCount = 0
            )

            fakeBackupRepository.setLastBackupMetadata(sampleBackup)
            assertThat(awaitItem()).isEqualTo(LibraryBackupBannerState.RESTORE_AVAILABLE)

            viewModel.dismissBackupBanner()
            assertThat(awaitItem()).isEqualTo(LibraryBackupBannerState.HIDDEN)
        }
    }

    @Test
    fun `backupBannerState shows BACKUP_RECOMMENDED when local items exceed cloud backup count`() =
        runTest {
            val existingCloudBackup = BackupMetadata(
                timestamp = System.currentTimeMillis(),
                formattedDate = "Today",
                deviceName = "Pixel 8",
                sizeBytes = 1024,
                formattedSize = "1 KB",
                favoritesCount = 1,
                watchlistCount = 1,
                historyCount = 0,
                customListsCount = 0,
                customListItemsCount = 0
            )
            fakeBackupRepository.setLastBackupMetadata(existingCloudBackup)

            // Local has 3 items (more than cloud's 2 items)
            fakeLibraryRepository.toggleWatchlist(
                1,
                MediaType.Movie,
                "M1",
                "/p1.jpg",
                "",
                7f,
                "2024-01-01"
            )
            fakeLibraryRepository.toggleWatchlist(
                2,
                MediaType.Movie,
                "M2",
                "/p2.jpg",
                "",
                8f,
                "2024-01-01"
            )
            fakeLibraryRepository.toggleFavorite(
                3,
                MediaType.Movie,
                "M3",
                "/p3.jpg",
                "",
                9f,
                "2024-01-01"
            )

            viewModel.backupBannerState.test {
                assertThat(awaitItem()).isEqualTo(LibraryBackupBannerState.BACKUP_RECOMMENDED)
            }
        }

    @Test
    fun `backupBannerState shows HIDDEN when local items match fresh cloud backup`() = runTest {
        // Local has 3 items
        fakeLibraryRepository.toggleWatchlist(
            1,
            MediaType.Movie,
            "M1",
            "/p1.jpg",
            "",
            7f,
            "2024-01-01"
        )
        fakeLibraryRepository.toggleWatchlist(
            2,
            MediaType.Movie,
            "M2",
            "/p2.jpg",
            "",
            8f,
            "2024-01-01"
        )
        fakeLibraryRepository.toggleFavorite(
            3,
            MediaType.Movie,
            "M3",
            "/p3.jpg",
            "",
            9f,
            "2024-01-01"
        )

        // Cloud also has 3 items
        val matchedCloudBackup = BackupMetadata(
            timestamp = System.currentTimeMillis(),
            formattedDate = "Today",
            deviceName = "Pixel 8",
            sizeBytes = 1024,
            formattedSize = "1 KB",
            favoritesCount = 1,
            watchlistCount = 2,
            historyCount = 0,
            customListsCount = 0,
            customListItemsCount = 0
        )
        fakeBackupRepository.setLastBackupMetadata(matchedCloudBackup)

        viewModel.backupBannerState.test {
            assertThat(awaitItem()).isEqualTo(LibraryBackupBannerState.HIDDEN)
        }
    }

    @Test
    fun cloneCommunityList_updatesDynamicCloneStateToTrue() = runTest {
        val communityList = CommunityCuratedList(
            listId = "comm-1",
            title = "Top Sci-Fi",
            description = "Great films",
            authorId = "user-2",
            authorName = "Jane",
            categoryTag = "Sci-Fi",
            itemCount = 0,
            items = emptyList(),
            previewPosters = emptyList(),
            upvotesCount = 5,
            clonesCount = 1,
            isUpvotedByMe = false,
            isClonedByMe = false,
            isMine = false,
            createdAtEpochMs = 1000L,
            updatedAtEpochMs = 1000L
        )
        fakeCommunityRepository.setCommunityLists(listOf(communityList))

        viewModel.communityLists.test {
            val initial = awaitItem()
            assertThat(initial.first().isClonedByMe).isFalse()

            viewModel.cloneCommunityList(communityList)

            var updated = awaitItem()
            if (!updated.first().isClonedByMe || updated.first().clonesCount == 1L) {
                updated = awaitItem()
            }
            assertThat(updated.first().isClonedByMe).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteCustomList_resetsDynamicCloneStateToFalse() = runTest {
        val communityList = CommunityCuratedList(
            listId = "comm-1",
            title = "Top Sci-Fi",
            description = "Great films",
            authorId = "user-2",
            authorName = "Jane",
            categoryTag = "Sci-Fi",
            itemCount = 0,
            items = emptyList(),
            previewPosters = emptyList(),
            upvotesCount = 5,
            clonesCount = 1,
            isUpvotedByMe = false,
            isClonedByMe = false,
            isMine = false,
            createdAtEpochMs = 1000L,
            updatedAtEpochMs = 1000L
        )
        fakeCommunityRepository.setCommunityLists(listOf(communityList))

        viewModel.communityLists.test {
            assertThat(awaitItem().first().isClonedByMe).isFalse()

            viewModel.cloneCommunityList(communityList)
            var cloned = awaitItem()
            if (!cloned.first().isClonedByMe || cloned.first().clonesCount == 1L) {
                cloned = awaitItem()
            }
            assertThat(cloned.first().isClonedByMe).isTrue()

            val clonedList =
                viewModel.customLists.value.first { it.sourceCommunityListId == "comm-1" }
            viewModel.deleteCustomList(clonedList.listId)

            var unCloned = awaitItem()
            while (unCloned.first().isClonedByMe) {
                unCloned = awaitItem()
            }
            assertThat(unCloned.first().isClonedByMe).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteCommunityList_deletesListFromCommunity() = runTest {
        val communityList = CommunityCuratedList(
            listId = "comm-1",
            title = "My Published List",
            description = "My favorites",
            authorId = "user-1",
            authorName = "Me",
            categoryTag = "Favorites",
            itemCount = 0,
            items = emptyList(),
            previewPosters = emptyList(),
            upvotesCount = 0,
            clonesCount = 0,
            isUpvotedByMe = false,
            isClonedByMe = false,
            isMine = true,
            createdAtEpochMs = 1000L,
            updatedAtEpochMs = 1000L
        )
        fakeCommunityRepository.setCommunityLists(listOf(communityList))

        viewModel.communityLists.test {
            assertThat(awaitItem()).hasSize(1)

            viewModel.deleteCommunityList("comm-1")

            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun reportCommunityList_optimisticallyHidesListAndReports() = runTest {
        val communityList = CommunityCuratedList(
            listId = "comm-report-1",
            title = "Suspicious Collection",
            description = "Some description",
            authorId = "author-bad",
            authorName = "BadAuthor",
            categoryTag = "Crime",
            itemCount = 0,
            items = emptyList(),
            previewPosters = emptyList(),
            upvotesCount = 0,
            clonesCount = 0,
            isUpvotedByMe = false,
            isClonedByMe = false,
            isMine = false,
            createdAtEpochMs = 1000L,
            updatedAtEpochMs = 1000L
        )
        fakeCommunityRepository.setCommunityLists(listOf(communityList))

        viewModel.communityLists.test {
            assertThat(awaitItem()).hasSize(1)

            viewModel.reportCommunityList(
                listId = "comm-report-1",
                authorId = "author-bad",
                reason = CommunityReportReason.InappropriateOrSexual
            )

            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun blockCommunityUser_blocksCreatorAndHidesFromCommunityLists() = runTest {
        val communityList = CommunityCuratedList(
            listId = "comm-block-1",
            title = "Creator Collection",
            description = "Some description",
            authorId = "author-blocked",
            authorName = "BlockedCreator",
            categoryTag = "Drama",
            itemCount = 0,
            items = emptyList(),
            previewPosters = emptyList(),
            upvotesCount = 0,
            clonesCount = 0,
            isUpvotedByMe = false,
            isClonedByMe = false,
            isMine = false,
            createdAtEpochMs = 1000L,
            updatedAtEpochMs = 1000L
        )
        fakeCommunityRepository.setCommunityLists(listOf(communityList))

        viewModel.communityLists.test {
            assertThat(awaitItem()).hasSize(1)

            viewModel.blockCommunityUser(authorId = "author-blocked")

            assertThat(awaitItem()).isEmpty()
        }
    }

    @Test
    fun validateContent_allowsNormalCinemaTitlesWithoutFalsePositives() = runTest {
        val benignTitles = listOf(
            "Classic 90s Thrillers",
            "The Assassin Anthology",
            "Cocktail Recipes in Cinema",
            "Moby Dick Adaptations",
            "The Dick Cavett Show Interviews"
        )
        for (title in benignTitles) {
            val result = viewModel.validateContent(title = title, description = "Great movies")
            assertThat(result).isInstanceOf(ContentModerationResult.Approved::class.java)
        }
    }

    @Test
    fun validateContent_flagsSensitiveThemeForConfirmation() = runTest {
        val result = viewModel.validateContent(
            title = "Cinema with explicit nudity scenes",
            description = "Film study"
        )
        assertThat(result).isInstanceOf(ContentModerationResult.SensitiveWarning::class.java)
    }

    @Test
    fun validateContent_blocksSevereProhibitedTerms() = runTest {
        val result =
            viewModel.validateContent(title = "cp collection illegal", description = "bad stuff")
        assertThat(result).isInstanceOf(ContentModerationResult.Prohibited::class.java)
    }

    @Test
    fun hasAcceptedCommunityGuidelines_defaultsToFalseAndUpdatesOnAccept() = runTest {
        viewModel.hasAcceptedCommunityGuidelines.test {
            assertThat(awaitItem()).isFalse()

            viewModel.acceptCommunityGuidelines()

            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun communityLists_shieldedWhenReportCountAtFlagThreshold() = runTest {
        val flaggedList = CommunityCuratedList(
            listId = "comm-flagged-1",
            title = "Controversial Collection",
            description = "Some description",
            authorId = "author-flagged",
            authorName = "FlaggedAuthor",
            categoryTag = "Drama",
            reportCount = CommunityModerationConfig.DEFAULT_COMMUNITY_LISTS_FLAG_THRESHOLD
        )
        fakeCommunityRepository.setCommunityLists(listOf(flaggedList))

        viewModel.communityLists.test {
            val lists = awaitItem()
            assertThat(lists).hasSize(1)
            assertThat(lists.first().isFlagged).isTrue()
        }
    }

    @Test
    fun communityLists_quarantinedWhenReportCountAtMaxThreshold() = runTest {
        val quarantinedList = CommunityCuratedList(
            listId = "comm-quarantine-1",
            title = "Severe Content Collection",
            description = "Some description",
            authorId = "author-quarantine",
            authorName = "QuarantineAuthor",
            categoryTag = "Crime",
            reportCount = CommunityModerationConfig.DEFAULT_COMMUNITY_LISTS_MAX_REPORT_THRESHOLD
        )
        fakeCommunityRepository.setCommunityLists(listOf(quarantinedList))

        viewModel.communityLists.test {
            val lists = awaitItem()
            assertThat(lists).isEmpty()
        }
    }

    @Test
    fun reportCommunityList_deduplicatesRepeatedReports() = runTest {
        val communityList = CommunityCuratedList(
            listId = "comm-dedup-1",
            title = "Deduplication Test",
            description = "Some description",
            authorId = "author-dedup",
            authorName = "DedupAuthor",
            categoryTag = "Sci-Fi",
            reportCount = 0L
        )
        fakeCommunityRepository.setCommunityLists(listOf(communityList))

        val params = ReportCommunityListParams(
            listId = "comm-dedup-1",
            authorId = "author-dedup",
            reason = CommunityReportReason.SpamOrCommercial,
            details = "First report"
        )

        fakeCommunityRepository.reportCommunityList(params)
        assertThat(fakeCommunityRepository.rawCommunityLists.value.first().reportCount).isEqualTo(1L)

        // Attempting to report the same list a second time should not increase reportCount
        fakeCommunityRepository.reportCommunityList(params.copy(details = "Second report attempt"))
        assertThat(fakeCommunityRepository.rawCommunityLists.value.first().reportCount).isEqualTo(1L)
    }
}
