package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing
import com.ssverma.showtime.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CinephileHubBottomSheet(
    hubCategories: List<CinephileHubCategory>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ShowTimeBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(bottom = MaterialTheme.spacing.large)
        ) {
            // Sheet Header
            Text(
                text = stringResource(R.string.hub_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            Text(
                text = stringResource(R.string.hub_sheet_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // Categorized Sections
            hubCategories.forEach { category ->
                Text(
                    text = stringResource(category.type.titleRes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
                )

                val chunkedItems = category.items.chunked(2)
                chunkedItems.forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = MaterialTheme.spacing.small),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        rowItems.forEach { item ->
                            HubTile(
                                item = item.copy(
                                    onClick = {
                                        onDismiss()
                                        item.onClick()
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            }
        }
    }
}
