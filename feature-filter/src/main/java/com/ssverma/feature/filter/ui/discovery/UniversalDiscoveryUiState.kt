package com.ssverma.feature.filter.ui.discovery

import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem

data class UniversalDiscoveryUiState(
    val filter: UniversalDiscoveryFilter = UniversalDiscoveryFilter(),
    val availableProviders: List<ProviderInfo> = emptyList(),
    val availableRegions: List<WatchProviderRegion> = emptyList(),
    val items: List<UniversalMediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasReachedEnd: Boolean = false,
    val errorMessage: String? = null,
    val isGridView: Boolean = true,
    val isFilterSheetOpen: Boolean = false,
    val isRegionSheetOpen: Boolean = false,
    val rouletteItem: UniversalMediaItem? = null,
    val isRouletteSpinning: Boolean = false
)
