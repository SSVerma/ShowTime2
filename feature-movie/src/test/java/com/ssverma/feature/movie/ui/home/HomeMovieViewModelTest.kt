package com.ssverma.feature.movie.ui.home

import com.google.android.gms.ads.nativead.NativeAd
import com.google.common.truth.Truth.assertThat
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.testing.dispatcher.MainDispatcherRule
import com.ssverma.core.ui.UiState
import com.ssverma.feature.movie.domain.usecase.InCinemaMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.MovieGenresUseCase
import com.ssverma.feature.movie.domain.usecase.PopularMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.TopRatedMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.TrendingMoviesUseCase
import com.ssverma.feature.movie.domain.usecase.UpcomingMoviesUseCase
import com.ssverma.shared.ads.injection.InjectableAd
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.CinemaGameRepository
import com.ssverma.shared.domain.usecase.FetchAllWatchProvidersUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeMovieViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val trendingMoviesUseCase: TrendingMoviesUseCase = mockk(relaxed = true)
    private val topRatedMoviesUseCase: TopRatedMoviesUseCase = mockk(relaxed = true)
    private val upcomingMoviesUseCase: UpcomingMoviesUseCase = mockk(relaxed = true)
    private val inCinemaMoviesUseCase: InCinemaMoviesUseCase = mockk(relaxed = true)
    private val popularMoviesUseCase: PopularMoviesUseCase = mockk(relaxed = true)
    private val movieGenreUseCase: MovieGenresUseCase = mockk(relaxed = true)
    private val fetchAllWatchProvidersUseCase: FetchAllWatchProvidersUseCase = mockk(relaxed = true)
    private val appConfigRepository: AppConfigRepository = mockk(relaxed = true)
    private val adConfigProvider: AdConfigProvider = mockk(relaxed = true)
    private val cinemaGameRepository: CinemaGameRepository = mockk(relaxed = true)

    private lateinit var viewModel: HomeMovieViewModel

    @Before
    fun setUp() {
        every { appConfigRepository.watchProviderRegion } returns MutableStateFlow("US")
        every { appConfigRepository.isTranslationEnabled } returns MutableStateFlow(false)
        every { appConfigRepository.contentLanguage } returns MutableStateFlow("en")
        every { appConfigRepository.preferredOriginalLanguage } returns MutableStateFlow("en")
        every { adConfigProvider.isAdsEnabled } returns false

        every { cinemaGameRepository.gameStatsFlow } returns MutableStateFlow(com.ssverma.shared.domain.model.game.CinemaGameStats())
        coEvery { cinemaGameRepository.isTodayPuzzleCompleted() } returns false

        coEvery { trendingMoviesUseCase(any()) } returns Result.Success(emptyList())
        coEvery { inCinemaMoviesUseCase() } returns Result.Success(emptyList())
        coEvery { popularMoviesUseCase() } returns Result.Success(emptyList())
        coEvery { topRatedMoviesUseCase() } returns Result.Success(emptyList())
        coEvery { upcomingMoviesUseCase() } returns Result.Success(emptyList())
        coEvery { movieGenreUseCase() } returns Result.Success(emptyList())
        coEvery { fetchAllWatchProvidersUseCase.fetchMovieWatchProviders() } returns Result.Success(
            emptyList()
        )

        viewModel = HomeMovieViewModel(
            trendingMoviesUseCase = trendingMoviesUseCase,
            topRatedMoviesUseCase = topRatedMoviesUseCase,
            upcomingMoviesUseCase = upcomingMoviesUseCase,
            inCinemaMoviesUseCase = inCinemaMoviesUseCase,
            popularMoviesUseCase = popularMoviesUseCase,
            movieGenreUseCase = movieGenreUseCase,
            fetchAllWatchProvidersUseCase = fetchAllWatchProvidersUseCase,
            appConfigRepository = appConfigRepository,
            adConfigProvider = adConfigProvider,
            cinemaGameRepository = cinemaGameRepository
        )
    }

    @Test
    fun `fetchMovieGenres success updates genres state`() = runTest {
        val genres = listOf(Genre(id = 28, name = "Action"))
        coEvery { movieGenreUseCase() } returns Result.Success(genres)

        viewModel.fetchMovieGenres()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.genres).isInstanceOf(UiState.Success::class.java)
        val successData = (state.genres as UiState.Success<List<Genre>>).data
        assertThat(successData).hasSize(1)
        assertThat(successData.first().name).isEqualTo("Action")
    }

    @Test
    fun `onNativeAdLoaded updates matching ad in specific section without touching other sections`() =
        runTest {
            every { adConfigProvider.isAdsEnabled } returns true
            val movie1 = mockk<Movie>(relaxed = true) { every { id } returns 101 }
            val movie2 = mockk<Movie>(relaxed = true) { every { id } returns 102 }

            coEvery { popularMoviesUseCase() } returns Result.Success(listOf(movie1, movie2))
            coEvery { topRatedMoviesUseCase() } returns Result.Success(listOf(movie1, movie2))

            viewModel.fetchPopularMovies()
            viewModel.fetchTopRatedMovies()
            advanceUntilIdle()

            val popularWithAds = (viewModel.uiState.value.popularMovies as UiState.Success).data
            val topRatedWithAds = (viewModel.uiState.value.topRatedMovies as UiState.Success).data

            val popularAd = popularWithAds.filterIsInstance<InjectableAd>().first()
            val topRatedAd = topRatedWithAds.filterIsInstance<InjectableAd>().first()

            assertThat(popularAd.id).isEqualTo("movie_popular_ad_Grid_1")
            assertThat(topRatedAd.id).isEqualTo("movie_top_rated_ad_Grid_1")

            val mockNativeAd = mockk<NativeAd>()
            viewModel.onNativeAdLoaded(popularAd, mockNativeAd)

            val updatedPopular = (viewModel.uiState.value.popularMovies as UiState.Success).data
            val updatedTopRated = (viewModel.uiState.value.topRatedMovies as UiState.Success).data

            val updatedPopularAd = updatedPopular.filterIsInstance<InjectableAd>().first()
            val untouchedTopRatedAd = updatedTopRated.filterIsInstance<InjectableAd>().first()

            assertThat(updatedPopularAd.ad).isEqualTo(mockNativeAd)
            assertThat(untouchedTopRatedAd.ad).isNull()
        }

    @Test
    fun `onNativeAdFailed removes matching ad in specific section without touching other sections`() =
        runTest {
            every { adConfigProvider.isAdsEnabled } returns true
            val movie1 = mockk<Movie>(relaxed = true) { every { id } returns 101 }
            val movie2 = mockk<Movie>(relaxed = true) { every { id } returns 102 }

            coEvery { popularMoviesUseCase() } returns Result.Success(listOf(movie1, movie2))
            coEvery { topRatedMoviesUseCase() } returns Result.Success(listOf(movie1, movie2))

            viewModel.fetchPopularMovies()
            viewModel.fetchTopRatedMovies()
            advanceUntilIdle()

            val popularWithAds = (viewModel.uiState.value.popularMovies as UiState.Success).data
            val popularAd = popularWithAds.filterIsInstance<InjectableAd>().first()

            viewModel.onNativeAdFailed(popularAd)

            val updatedPopular = (viewModel.uiState.value.popularMovies as UiState.Success).data
            val updatedTopRated = (viewModel.uiState.value.topRatedMovies as UiState.Success).data

            assertThat(updatedPopular.any { it is InjectableAd }).isFalse()
            assertThat(updatedTopRated.any { it is InjectableAd }).isTrue()
        }
}
