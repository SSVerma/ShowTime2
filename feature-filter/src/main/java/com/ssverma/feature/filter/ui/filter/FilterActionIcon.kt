package com.ssverma.feature.filter.ui.filter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.shared.ui.component.FilterActionIcon as SharedFilterActionIcon

@Composable
fun FilterActionIcon(
    isFilterApplied: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SharedFilterActionIcon(
        isFilterApplied = isFilterApplied,
        onClick = onClick,
        modifier = modifier
    )
}

