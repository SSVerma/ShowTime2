package com.ssverma.feature.filter.ui.hub

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.core.ui.UiState
import com.ssverma.feature.filter.ui.hub.config.MovieHubDiscoverConfig
import com.ssverma.feature.filter.ui.hub.config.TvHubDiscoverConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.movie.asMoviePreview
import com.ssverma.shared.domain.model.tv.asTvShowPreview
import com.ssverma.shared.domain.repository.DiscoveryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = WatchProviderHubViewModel.Factory::class)
class WatchProviderHubViewModel @AssistedInject constructor(
    private val discoveryRepository: DiscoveryRepository,
    @Assisted("providerId") val providerId: Int,
    @Assisted("providerName") val providerName: String,
    @Assisted("logoPath") private val logoPath: String,
    @Assisted("isMovie") private val isMovie: Boolean
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("providerId") providerId: Int,
            @Assisted("providerName") providerName: String,
            @Assisted("logoPath") logoPath: String,
            @Assisted("isMovie") isMovie: Boolean
        ): WatchProviderHubViewModel
    }

    private val _uiState = MutableStateFlow(
        WatchProviderHubUiState(
            provider = ProviderInfo(
                providerId = providerId,
                providerName = providerName,
                logoPath = Uri.decode(logoPath),
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
        val heroDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieHubDiscoverConfig.heroItems(providerId = providerId)
            )
        }

        val newDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieHubDiscoverConfig.newReleases(providerId = providerId)
            )
        }

        val upcomingDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieHubDiscoverConfig.upcoming(providerId = providerId)
            )
        }

        val topDeferred = viewModelScope.async {
            discoveryRepository.discoverMovies(
                discoverConfig = MovieHubDiscoverConfig.topRated(providerId = providerId)
            )
        }

        val genresDeferred = viewModelScope.async {
            discoveryRepository.fetchMovieGenre()
        }

        val heroResult = heroDeferred.await()
        val newResult = newDeferred.await()
        val upcomingResult = upcomingDeferred.await()
        val ratedResult = topDeferred.await()
        val genresResult = genresDeferred.await()

        val heroItems = heroResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }

        val newItems = newResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }

        val upcomingItems = upcomingResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }

        val ratedItems = ratedResult.getOrDefault(emptyList())
            .map { MediaPreview.Movie(it.asMoviePreview()) }

        val genres = genresResult.getOrDefault(emptyList())

        // Show error only if all primary sections fail
        if (heroItems.isEmpty() && newItems.isEmpty() && upcomingItems.isEmpty() && ratedItems.isEmpty()) {
            val error = listOf(heroResult, newResult, upcomingResult, ratedResult)
                .filterIsInstance<Result.Error<Failure.CoreFailure>>()
                .firstOrNull()?.error ?: Failure.CoreFailure.UnexpectedFailure

            _uiState.update { it.copy(hubContentState = UiState.Error(error)) }
            return
        }

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
        val heroDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvHubDiscoverConfig.heroItems(providerId = providerId)
            )
        }

        val newDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvHubDiscoverConfig.newReleases(providerId = providerId)
            )
        }

        val upcomingDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvHubDiscoverConfig.upcoming(providerId = providerId)
            )
        }

        val ratedDeferred = viewModelScope.async {
            discoveryRepository.discoverTvShows(
                discoverConfig = TvHubDiscoverConfig.topRated(providerId = providerId)
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

        val heroItems = heroResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }

        val newItems = newResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }

        val upcomingItems = upcomingResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }

        val ratedItems = ratedResult.getOrDefault(emptyList())
            .map { MediaPreview.TvShow(it.asTvShowPreview()) }

        val genres = genresResult.getOrDefault(emptyList())

        if (heroItems.isEmpty() && newItems.isEmpty() && upcomingItems.isEmpty() && ratedItems.isEmpty()) {
            val error = listOf(heroResult, newResult, upcomingResult, ratedResult)
                .filterIsInstance<Result.Error<Failure.CoreFailure>>()
                .firstOrNull()?.error ?: Failure.CoreFailure.UnexpectedFailure

            _uiState.update { it.copy(hubContentState = UiState.Error(error)) }
            return
        }

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
