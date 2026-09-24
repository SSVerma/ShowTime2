package com.ssverma.feature.filter.ui.hub

import android.net.Uri
import com.google.android.gms.ads.nativead.NativeAd
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.ui.UiState
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.repository.AffiliateRepository
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.DiscoveryRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WatchProviderHubViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val discoveryRepository: DiscoveryRepository = mockk(relaxed = true)
    private val adConfigProvider: AdConfigProvider = mockk(relaxed = true)
    private val affiliateRepository: AffiliateRepository = mockk(relaxed = true)
    private val appConfigRepository: AppConfigRepository = mockk(relaxed = true)

    private lateinit var viewModel: WatchProviderHubViewModel

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        every { Uri.decode(any()) } answers { firstArg<String>() }

        every { appConfigRepository.watchProviderRegion } returns MutableStateFlow("US")
        every { adConfigProvider.isAdsEnabled } returns true

        val movie1 = mockk<Movie>(relaxed = true) { every { id } returns 1 }
        val movie2 = mockk<Movie>(relaxed = true) { every { id } returns 2 }
        val movie3 = mockk<Movie>(relaxed = true) { every { id } returns 3 }
        val movie4 = mockk<Movie>(relaxed = true) { every { id } returns 4 }

        coEvery { discoveryRepository.discoverMovies(any()) } returns Result.Success(
            listOf(movie1, movie2, movie3, movie4)
        )
        coEvery { discoveryRepository.fetchMovieGenre() } returns Result.Success(emptyList())

        viewModel = WatchProviderHubViewModel(
            discoveryRepository = discoveryRepository,
            adConfigProvider = adConfigProvider,
            affiliateRepository = affiliateRepository,
            appConfigRepository = appConfigRepository,
            providerId = 8,
            providerName = "Netflix",
            logoPath = "/netflix.jpg",
            isMovie = true
        )
    }

    @Test
    fun `watch provider hub injects separate unique IDs for all 4 horizontal sections`() = runTest {
        advanceUntilIdle()

        val hubContent = (viewModel.uiState.value.hubContentState as UiState.Success).data

        val heroAd = hubContent.heroItems.filterIsInstance<InjectableAd>().first()
        val newAd = hubContent.newItems.filterIsInstance<InjectableAd>().first()
        val upcomingAd = hubContent.upcomingItems.filterIsInstance<InjectableAd>().first()
        val topRatedAd = hubContent.topRatedItems.filterIsInstance<InjectableAd>().first()

        // Verify distinct IDs across all 4 horizontal sections
        assertThat(heroAd.id).isEqualTo("hub_movie_hero_ad_Carousel_1")
        assertThat(newAd.id).isEqualTo("hub_movie_new_ad_Grid_1")
        assertThat(upcomingAd.id).isEqualTo("hub_movie_upcoming_ad_Grid_1")
        assertThat(topRatedAd.id).isEqualTo("hub_movie_top_rated_ad_Grid_1")
    }

    @Test
    fun `onCarouselNativeAdLoaded updates matching ad in specific section without affecting others`() =
        runTest {
            advanceUntilIdle()

            val initialContent = (viewModel.uiState.value.hubContentState as UiState.Success).data
            val newAd = initialContent.newItems.filterIsInstance<InjectableAd>().first()

            val mockNativeAd = mockk<NativeAd>()
            viewModel.onCarouselNativeAdLoaded(newAd, mockNativeAd)

            val updatedContent = (viewModel.uiState.value.hubContentState as UiState.Success).data

            val updatedNewAd = updatedContent.newItems.filterIsInstance<InjectableAd>().first()
            val untouchedUpcomingAd =
                updatedContent.upcomingItems.filterIsInstance<InjectableAd>().first()
            val untouchedTopRatedAd =
                updatedContent.topRatedItems.filterIsInstance<InjectableAd>().first()

            assertThat(updatedNewAd.ad).isEqualTo(mockNativeAd)
            assertThat(untouchedUpcomingAd.ad).isNull()
            assertThat(untouchedTopRatedAd.ad).isNull()
        }

    @Test
    fun `onCarouselNativeAdFailed removes matching ad in specific section without removing others`() =
        runTest {
            advanceUntilIdle()

            val initialContent = (viewModel.uiState.value.hubContentState as UiState.Success).data
            val newAd = initialContent.newItems.filterIsInstance<InjectableAd>().first()

            viewModel.onCarouselNativeAdFailed(newAd)

            val updatedContent = (viewModel.uiState.value.hubContentState as UiState.Success).data

            // New Items ad slot is removed
            assertThat(updatedContent.newItems.any { it is InjectableAd }).isFalse()
            // Upcoming and Top Rated keep their ad slots!
            assertThat(updatedContent.upcomingItems.any { it is InjectableAd }).isTrue()
            assertThat(updatedContent.topRatedItems.any { it is InjectableAd }).isTrue()
        }
}
