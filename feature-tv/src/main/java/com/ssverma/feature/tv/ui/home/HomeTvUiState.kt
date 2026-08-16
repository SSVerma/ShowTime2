package com.ssverma.feature.tv.ui.home

import com.google.android.gms.ads.nativead.NativeAd
import com.ssverma.core.ui.UiState
import com.ssverma.feature.tv.domain.failure.TvShowFailure
import com.ssverma.shared.ads.injection.AdInjectable
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.Genre
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview

data class HomeTvUiState(
    val trendingTvShows: UiState<List<AdInjectable<TvShowPreview>>, TvShowFailure> = UiState.Idle,
    val topRatedTvShows: UiState<List<AdInjectable<TvShowPreview>>, TvShowFailure> = UiState.Idle,
    val popularTvShows: UiState<List<AdInjectable<TvShowPreview>>, TvShowFailure> = UiState.Idle,
    val todayAiringTvShows: UiState<List<AdInjectable<TvShowPreview>>, TvShowFailure> = UiState.Idle,
    val nowAiringTvShows: UiState<List<AdInjectable<TvShowPreview>>, TvShowFailure> = UiState.Idle,
    val upcomingTvShows: UiState<List<AdInjectable<TvShowPreview>>, TvShowFailure> = UiState.Idle,
    val genres: UiState<List<Genre>, Failure.CoreFailure> = UiState.Idle,
    val watchProviders: UiState<List<ProviderInfo>, Failure.CoreFailure> = UiState.Idle,
    val watchProviderAd: NativeAd? = null,
    val feedInlineAd: NativeAd? = null,
)
