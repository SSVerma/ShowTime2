package com.ssverma.feature.filter.ui.hub

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.MovieDiscoverConfig
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.TvDiscoverConfig
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.asMoviePreview
import com.ssverma.shared.domain.model.tv.asTvShowPreview
import com.ssverma.shared.domain.repository.DiscoveryRepository
import com.ssverma.showtime.feature.filter.navigation.WatchProviderHubDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchProviderHubViewModel @Inject constructor(
    private val discoveryRepository: DiscoveryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val providerId: Int =
        savedStateHandle.get<Int>(WatchProviderHubDestination.ArgProviderId) ?: 0

    val providerName: String =
        savedStateHandle.get<String>(WatchProviderHubDestination.ArgProviderName).orEmpty()

    private val logoPath: String =
        Uri.decode(savedStateHandle.get<String>(WatchProviderHubDestination.ArgLogoPath).orEmpty())

    private val isMovie: Boolean =
        savedStateHandle.get<Boolean>(WatchProviderHubDestination.ArgIsMovie) ?: false

    private val _uiState = MutableStateFlow(
        WatchProviderHubUiState(
            provider = ProviderInfo(
                providerId = providerId,
                providerName = providerName,
                logoPath = logoPath,
                displayPriority = 0
            ),
            isMovieMode = isMovie,
            hubContentState = UiState.Loading
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        fetchHubContent()
    }

    fun fetchHubContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(hubContentState = UiState.Loading) }
            if (isMovie) {
                fetchMovieHub()
            } else {
                fetchTvHub()
            }
        }
    }

    private suspend fun fetchMovieHub() {
        val watchProvider = DiscoverOption.WatchProvider(providerId)

        val heroDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieDiscoverConfig.builder()
                    .with(watchProvider)
                    .sortBy(SortBy.Popularity(Order.Descending))
                    .build()
            )
        }

        val today = java.time.LocalDate.now()
        val lastWeek = today.minusWeeks(1)

        val newDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieDiscoverConfig.builder()
                    .with(watchProvider)
                    .with(DiscoverOption.PrimaryReleaseDate.From(lastWeek))
                    .with(DiscoverOption.PrimaryReleaseDate.To(today))
                    .sortBy(SortBy.ReleaseDate(Order.Descending))
                    .build()
            )
        }

        val upcomingDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieDiscoverConfig.builder()
                    .with(watchProvider)
                    .with(DiscoverOption.PrimaryReleaseDate.From(today.plusDays(1)))
                    .sortBy(SortBy.ReleaseDate(Order.Ascending))
                    .build()
            )
        }

        val ratedDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieDiscoverConfig.builder()
                    .with(watchProvider)
                    .sortBy(SortBy.Rating(Order.Descending))
                    .build()
            )
        }

        val genresDeferred = viewModelScope.async {
            discoveryRepository.fetchMovieGenre()
        }

        val heroResult = heroDeferred.await()
        val newResult = newDeferred.await()
        val upcomingResult = upcomingDeferred.await()
        val ratedResult = ratedDeferred.await()
        val genresResult = genresDeferred.await()

        val results = listOf(heroResult, newResult, upcomingResult, ratedResult, genresResult)

        val errorResult = results.filterIsInstance<Result.Error<Failure.CoreFailure>>()
            .firstOrNull()

        if (errorResult != null) {
            _uiState.update { it.copy(hubContentState = UiState.Error(errorResult.error)) }
            return
        }

        val heroItems = heroResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }

        val newItems = newResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }

        val upcomingItems = upcomingResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }

        val ratedItems = ratedResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }

        val genres = genresResult.getOrDefault(emptyList())

        _uiState.update {
            it.copy(
                hubContentState = UiState.Success(
                    HubContent(
                        heroItems = heroItems,
                        newItems = newItems,
                        upcomingItems = upcomingItems,
                        topRatedItems = ratedItems,
                        genres = genres
                    )
                )
            )
        }
    }

    private suspend fun fetchTvHub() {
        val watchProvider = DiscoverOption.WatchProvider(providerId)

        val heroDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvDiscoverConfig.builder()
                    .with(watchProvider)
                    .sortBy(SortBy.Popularity(Order.Descending))
                    .build()
            )
        }

        val today = java.time.LocalDate.now()
        val lastWeek = today.minusWeeks(1)

        val newDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvDiscoverConfig.builder()
                    .with(watchProvider)
                    .with(DiscoverOption.AirDate.From(lastWeek))
                    .with(DiscoverOption.AirDate.To(today))
                    .sortBy(SortBy.AirDate(Order.Descending))
                    .build()
            )
        }

        val upcomingDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvDiscoverConfig.builder()
                    .with(watchProvider)
                    .with(DiscoverOption.AirDate.From(today.plusDays(1)))
                    .sortBy(SortBy.AirDate(Order.Ascending))
                    .build()
            )
        }

        val ratedDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvDiscoverConfig.builder()
                    .with(watchProvider)
                    .sortBy(SortBy.Rating(Order.Descending))
                    .build()
            )
        }

        val genresDeferred = viewModelScope.async {
            discoveryRepository.fetchTvShowGenre()
        }

        val heroResult = heroDeferred.await()
        val newResult = newDeferred.await()
        val upcomingResult = upcomingDeferred.await()
        val ratedResult = ratedDeferred.await()
        val genresResult = genresDeferred.await()

        val results = listOf(heroResult, newResult, upcomingResult, ratedResult, genresResult)

        val errorResult =
            results.filterIsInstance<Result.Error<Failure.CoreFailure>>().firstOrNull()
        if (errorResult != null) {
            _uiState.update { it.copy(hubContentState = UiState.Error(errorResult.error)) }
            return
        }

        val heroItems = heroResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }

        val newItems = newResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }

        val upcomingItems = upcomingResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }

        val ratedItems = ratedResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }

        val genres = genresResult.getOrDefault(emptyList())

        _uiState.update {
            it.copy(
                hubContentState = UiState.Success(
                    HubContent(
                        heroItems = heroItems,
                        newItems = newItems,
                        upcomingItems = upcomingItems,
                        topRatedItems = ratedItems,
                        genres = genres
                    )
                )
            )
        }
    }

    private fun <T> Result<T, Failure.CoreFailure>.getOrDefault(default: T): T {
        return if (this is Result.Success) this.data else default
    }
}
