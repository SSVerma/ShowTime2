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
    fun `hub injects ad only in hero section and no ads in secondary sections`() = runTest {
        advanceUntilIdle()

        val hubContent = (viewModel.uiState.value.hubContentState as UiState.Success).data

        val heroAds = hubContent.heroItems.filterIsInstance<InjectableAd>()
        val newAds = hubContent.newItems.filterIsInstance<InjectableAd>()
        val upcomingAds = hubContent.upcomingItems.filterIsInstance<InjectableAd>()
        val topRatedAds = hubContent.topRatedItems.filterIsInstance<InjectableAd>()

        // Only hero carousel gets an ad slot
        assertThat(heroAds).hasSize(1)
        assertThat(heroAds.first().id).isEqualTo("hub_movie_hero_ad_Carousel_1")

        // Secondary sections have zero ad slots to avoid phantom over-requesting
        assertThat(newAds).isEmpty()
        assertThat(upcomingAds).isEmpty()
        assertThat(topRatedAds).isEmpty()
    }

    @Test
    fun `onCarouselNativeAdLoaded updates hero ad`() = runTest {
        advanceUntilIdle()

        val initialContent = (viewModel.uiState.value.hubContentState as UiState.Success).data
        val heroAd = initialContent.heroItems.filterIsInstance<InjectableAd>().first()

        val mockNativeAd = mockk<NativeAd>()
        viewModel.onCarouselNativeAdLoaded(injectableAd = heroAd, nativeAd = mockNativeAd)

        val updatedContent = (viewModel.uiState.value.hubContentState as UiState.Success).data
        val updatedHeroAd = updatedContent.heroItems.filterIsInstance<InjectableAd>().first()

        assertThat(updatedHeroAd.ad).isEqualTo(mockNativeAd)
    }

    @Test
    fun `onCarouselNativeAdFailed removes hero ad slot`() = runTest {
        advanceUntilIdle()

        val initialContent = (viewModel.uiState.value.hubContentState as UiState.Success).data
        val heroAd = initialContent.heroItems.filterIsInstance<InjectableAd>().first()

        viewModel.onCarouselNativeAdFailed(injectableAd = heroAd)

        val updatedContent = (viewModel.uiState.value.hubContentState as UiState.Success).data

        // Hero ad slot is removed on failure
        assertThat(updatedContent.heroItems.any { it is InjectableAd }).isFalse()
    }
}
