package com.ssverma.common.ui.region

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.ui.region.RegionSelectionBottomSheet as SharedRegionSelectionBottomSheet

@Composable
fun RegionSelectionBottomSheet(
    selectedRegionCode: String,
    availableRegions: List<WatchProviderRegion>,
    onRegionSelected: (WatchProviderRegion) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null
) {
    SharedRegionSelectionBottomSheet(
        selectedRegionCode = selectedRegionCode,
        availableRegions = availableRegions,
        onRegionSelected = onRegionSelected,
        onDismissRequest = onDismissRequest,
        title = title,
        description = description,
        modifier = modifier
    )
}
