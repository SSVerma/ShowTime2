package com.ssverma.feature.filter.ui.discovery.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.filter.R
import com.ssverma.feature.filter.ui.discovery.component.filter.CertificationSection
import com.ssverma.feature.filter.ui.discovery.component.filter.EraDecadeSection
import com.ssverma.feature.filter.ui.discovery.component.filter.HideWatchedSection
import com.ssverma.feature.filter.ui.discovery.component.filter.LanguageSection
import com.ssverma.feature.filter.ui.discovery.component.filter.MonetizationSection
import com.ssverma.feature.filter.ui.discovery.component.filter.RatingThresholdSection
import com.ssverma.feature.filter.ui.discovery.component.filter.RuntimeSection
import com.ssverma.feature.filter.ui.discovery.component.filter.SortOrderSection
import com.ssverma.feature.filter.ui.discovery.component.filter.StudioNetworkSection
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.discovery.DiscoveryDecade
import com.ssverma.shared.domain.model.discovery.DiscoveryLanguage
import com.ssverma.shared.domain.model.discovery.DiscoveryRatingThreshold
import com.ssverma.shared.domain.model.discovery.DiscoveryRuntimeRange
import com.ssverma.shared.domain.model.discovery.DiscoverySortOrder
import com.ssverma.shared.domain.model.discovery.DiscoveryVibePreset
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryFilterSheet(
    filter: UniversalDiscoveryFilter,
    onApply: (UniversalDiscoveryFilter) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draftFilter by remember(filter) { mutableStateOf(filter) }

    ShowTimeBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Row with Title and Reset CTA
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = MaterialTheme.spacing.mediumLarge,
                        vertical = MaterialTheme.spacing.extraSmall
                    )
            ) {
                Text(
                    text = stringResource(R.string.filter_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = {
                        draftFilter = draftFilter.copy(
                            vibePreset = DiscoveryVibePreset.ALL,
                            decade = DiscoveryDecade.ALL_TIME,
                            sortOrder = DiscoverySortOrder.POPULARITY_DESC,
                            studioHub = null,
                            tvNetworkHub = null,
                            ratingThreshold = DiscoveryRatingThreshold.ALL,
                            runtimeRange = DiscoveryRuntimeRange.ALL,
                            monetizationTypes = emptySet(),
                            language = DiscoveryLanguage.ALL,
                            certification = null,
                            selectedGenreIds = emptySet(),
                            selectedProviderIds = emptySet(),
                            minRating = null,
                            hideWatched = true
                        )
                        onReset()
                    }
                ) {
                    Text(stringResource(R.string.filter_reset))
                }
            }

            // Scrollable Content of Modular Filter Sections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = MaterialTheme.spacing.mediumLarge,
                        vertical = MaterialTheme.spacing.small
                    )
            ) {
                HideWatchedSection(
                    hideWatched = draftFilter.hideWatched,
                    onHideWatchedChange = { draftFilter = draftFilter.copy(hideWatched = it) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))

                SortOrderSection(
                    selectedSortOrder = draftFilter.sortOrder,
                    onSortOrderSelected = { draftFilter = draftFilter.copy(sortOrder = it) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))

                RatingThresholdSection(
                    selectedThreshold = draftFilter.ratingThreshold,
                    onThresholdSelected = { draftFilter = draftFilter.copy(ratingThreshold = it) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))

                EraDecadeSection(
                    selectedDecade = draftFilter.decade,
                    onDecadeSelected = { draftFilter = draftFilter.copy(decade = it) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))

                StudioNetworkSection(
                    mediaType = draftFilter.mediaType,
                    selectedStudio = draftFilter.studioHub,
                    selectedTvNetwork = draftFilter.tvNetworkHub,
                    onStudioSelected = { draftFilter = draftFilter.copy(studioHub = it) },
                    onTvNetworkSelected = { draftFilter = draftFilter.copy(tvNetworkHub = it) }
                )

                if (draftFilter.mediaType == MediaType.Movie) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))
                    RuntimeSection(
                        selectedRuntime = draftFilter.runtimeRange,
                        onRuntimeSelected = { draftFilter = draftFilter.copy(runtimeRange = it) }
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))

                MonetizationSection(
                    selectedMonetizationTypes = draftFilter.monetizationTypes,
                    onToggleMonetizationType = { type ->
                        val current = draftFilter.monetizationTypes
                        val updated = if (current.contains(type)) current - type else current + type
                        draftFilter = draftFilter.copy(monetizationTypes = updated)
                    }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))

                LanguageSection(
                    selectedLanguage = draftFilter.language,
                    onLanguageSelected = { draftFilter = draftFilter.copy(language = it) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.mediumLarge))

                CertificationSection(
                    watchRegion = draftFilter.watchRegion,
                    mediaType = draftFilter.mediaType,
                    selectedCertification = draftFilter.certification,
                    onCertificationSelected = { draftFilter = draftFilter.copy(certification = it) }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
            }

            // Pinned Bottom CTA Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smallMedium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            horizontal = MaterialTheme.spacing.mediumLarge,
                            vertical = MaterialTheme.spacing.smallMedium
                        )
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    val activeCount = draftFilter.activeFilterCount()
                    Button(
                        onClick = { onApply(draftFilter) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(2f)
                    ) {
                        Text(
                            text = if (activeCount > 0) {
                                stringResource(R.string.filter_apply_with_count, activeCount)
                            } else {
                                stringResource(R.string.filter_apply_cta)
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
