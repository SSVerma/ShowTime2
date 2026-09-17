package com.ssverma.feature.filter.ui.discovery.component.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.filter.R
import com.ssverma.shared.domain.model.discovery.DiscoveryRuntimeRange

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RuntimeSection(
    selectedRuntime: DiscoveryRuntimeRange,
    onRuntimeSelected: (DiscoveryRuntimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FilterSectionHeader(
            title = stringResource(R.string.filter_section_runtime),
            icon = Icons.Rounded.Schedule
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier.fillMaxWidth()
        ) {
            DiscoveryRuntimeRange.entries.forEach { runtime ->
                val isSelected = runtime == selectedRuntime
                Surface(
                    onClick = { onRuntimeSelected(runtime) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = 0.4f
                        )
                    )
                ) {
                    Text(
                        text = runtime.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.smallMedium,
                            vertical = MaterialTheme.spacing.small
                        )
                    )
                }
            }
        }
    }
}
