package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.WatchProviderRegion
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.viewmodel.WatchRegionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchRegionSelector(
    modifier: Modifier = Modifier,
    viewModel: WatchRegionViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showSheet by remember { mutableStateOf(false) }
    val currentRegion by viewModel.currentRegion.collectAsState()

    IconButton(
        onClick = {
            showSheet = true
            viewModel.loadAvailableRegions()
        },
        modifier = modifier
    ) {
        BadgedBox(
            badge = {
                Badge {
                    Text(text = currentRegion)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Rounded.Public,
                contentDescription = stringResource(R.string.watch_region)
            )
        }
    }

    if (showSheet) {
        val regionsState by viewModel.regionsState.collectAsState()
        var searchQuery by remember { mutableStateOf("") }
        var selectedRegionIso by remember(currentRegion) { mutableStateOf(currentRegion) }

        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                searchQuery = ""
            },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.medium)
            ) {
                Text(
                    text = stringResource(R.string.select_watch_region),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.large,
                        vertical = MaterialTheme.spacing.medium
                    )
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = MaterialTheme.spacing.large,
                            vertical = MaterialTheme.spacing.small
                        ),
                    placeholder = { Text(stringResource(R.string.search_region)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    } else null,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 200.dp, max = 500.dp)
                ) {
                    when (regionsState) {
                        is UiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(MaterialTheme.spacing.large)
                            )
                        }

                        is UiState.Error<*> -> {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(MaterialTheme.spacing.large),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.unexpected_error_msg),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
                                )
                                Button(onClick = { viewModel.loadAvailableRegions() }) {
                                    Text(text = stringResource(id = R.string.retry))
                                }
                            }
                        }

                        is UiState.Success<List<WatchProviderRegion>> -> {
                            val regions = regionsState.asSuccess().data
                            val filteredRegions = remember(regions, searchQuery) {
                                if (searchQuery.isBlank()) regions
                                else regions.filter {
                                    it.englishName.contains(searchQuery, ignoreCase = true) ||
                                            it.nativeName.contains(
                                                searchQuery,
                                                ignoreCase = true
                                            ) ||
                                            it.iso31661.contains(searchQuery, ignoreCase = true)
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(filteredRegions) { region ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .selectable(
                                                selected = region.iso31661 == selectedRegionIso,
                                                onClick = {
                                                    selectedRegionIso = region.iso31661
                                                }
                                            )
                                            .padding(
                                                vertical = MaterialTheme.spacing.medium,
                                                horizontal = MaterialTheme.spacing.large
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = region.iso31661 == selectedRegionIso,
                                            onClick = null
                                        )
                                        Column(modifier = Modifier.padding(start = MaterialTheme.spacing.medium)) {
                                            Text(
                                                text = region.englishName,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            if (region.nativeName != region.englishName) {
                                                Text(
                                                    text = region.nativeName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                Button(
                    onClick = {
                        viewModel.updateRegion(selectedRegionIso)
                        showSheet = false
                        searchQuery = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.large)
                        .padding(bottom = MaterialTheme.spacing.large)
                ) {
                    Text(text = stringResource(id = R.string.done))
                }
            }
        }
    }
}

private fun <T> UiState<T, *>.asSuccess(): UiState.Success<T> = this as UiState.Success<T>
