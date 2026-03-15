package com.ssverma.feature.filter.ui.filter.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.MultiSelectableState
import com.ssverma.core.ui.SelectableState
import com.ssverma.core.ui.SingleSelectableState
import com.ssverma.core.ui.Toggleable
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.feature.filter.R
import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.feature.filter.ui.filter.FilterItem
import com.ssverma.shared.ui.component.ClickThroughFilterChip
import kotlinx.coroutines.delay

@Composable
fun NonSelectedFilterChip(
    text: String,
    modifier: Modifier = Modifier
) {
    ClickThroughFilterChip(
        modifier = modifier,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.56f)
        ),
        selected = false
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SelectedFilterChip(
    text: String,
    modifier: Modifier = Modifier
) {
    ClickThroughFilterChip(
        modifier = modifier,
        tonalElevation = 4.dp,
        selected = true
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun SingleSelectableFilterRow(
    items: List<FilterItem>,
    selectableState: SingleSelectableState<FilterItem>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]
            Toggleable(
                item = item,
                selectableState = selectableState,
                onContent = { SelectedFilterChip(text = item.text.asString()) },
                offContent = { NonSelectedFilterChip(text = item.text.asString()) }
            )
        }
    }
}

@Composable
fun MultiSelectableFilterRow(
    items: List<FilterItem>,
    selectableState: MultiSelectableState<FilterItem>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items.size) { index ->
            val item = items[index]
            Toggleable(
                item = item,
                selectableState = selectableState,
                onContent = { SelectedFilterChip(text = item.text.asString()) },
                offContent = { NonSelectedFilterChip(text = item.text.asString()) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SingleSelectableFilterFlowRow(
    items: List<FilterItem>,
    selectableState: SingleSelectableState<FilterItem>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        items.forEach { item ->
            Toggleable(
                item = item,
                selectableState = selectableState,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                onContent = { SelectedFilterChip(text = item.text.asString()) },
                offContent = { NonSelectedFilterChip(text = item.text.asString()) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectableFilterFlowRow(
    items: List<FilterItem>,
    selectableState: MultiSelectableState<FilterItem>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        items.forEach { item ->
            Toggleable(
                item = item,
                selectableState = selectableState,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                onContent = { SelectedFilterChip(text = item.text.asString()) },
                offContent = { NonSelectedFilterChip(text = item.text.asString()) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPickerChip(
    items: List<FilterItem>,
    selectableState: SelectableState<FilterItem>,
    modifier: Modifier = Modifier,
    groupId: FilterId? = null,
    isSearching: Boolean = false,
    onSearchQueryChanged: (FilterId, String) -> Unit = { _, _ -> },
    onPickerOpened: () -> Unit = {}
) {
    var showPicker by remember { mutableStateOf(false) }

    val summary = when (selectableState) {
        is SingleSelectableState -> {
            selectableState.selected()?.text?.asString()
        }

        is MultiSelectableState -> {
            val selected = selectableState.selected()
            if (selected.isEmpty()) null
            else if (selected.size == 1) selected.first().text.asString()
            else "${selected.first().text.asString()} +${selected.size - 1}"
        }

        else -> null
    }

    OutlinedCard(
        onClick = {
            onPickerOpened()
            showPicker = true
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = summary ?: stringResource(id = R.string.select),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (summary == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
    }

    if (showPicker) {
        FilterPickerBottomSheet(
            items = items,
            selectableState = selectableState,
            groupId = groupId,
            isSearching = isSearching,
            onSearchQueryChanged = onSearchQueryChanged,
            onDismissRequest = { showPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterPickerBottomSheet(
    items: List<FilterItem>,
    selectableState: SelectableState<FilterItem>,
    onDismissRequest: () -> Unit,
    groupId: FilterId? = null,
    isSearching: Boolean = false,
    onSearchQueryChanged: (FilterId, String) -> Unit = { _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val context = LocalContext.current
    val filteredItems = remember(searchQuery, items) {
        if (searchQuery.isBlank() || (groupId?.isRemoteSearchSupported == true)) items
        else items.filter {
            it.text.asString(context).contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(searchQuery) {
        if (groupId != null) {
            onSearchQueryChanged(groupId, searchQuery)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text(text = stringResource(id = R.string.search)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Rounded.Close, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true
                )

                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ShowTimeLoadingIndicator()
                    }
                } else if (filteredItems.isEmpty()) {
                    val shouldShowNoItems = searchQuery.isNotEmpty() ||
                            (groupId !is FilterId.CollectionTypeId.Dynamic.Keyword &&
                                    groupId !is FilterId.CollectionTypeId.Dynamic.WithoutKeyword &&
                                    groupId !is FilterId.CollectionTypeId.Dynamic.Company &&
                                    groupId !is FilterId.CollectionTypeId.Dynamic.WithoutCompany)

                    if (shouldShowNoItems) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(filteredItems.size) { index ->
                        val item = filteredItems[index]
                        val isSelected = selectableState.isSelected(item)

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = item.text.asString(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            trailingContent = {
                                if (selectableState is MultiSelectableState) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { selectableState.onSelectionChanged(item) }
                                    )
                                } else {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectableState.onSelectionChanged(item)
                                            onDismissRequest()
                                        }
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            modifier = Modifier.clickable {
                                selectableState.onSelectionChanged(item)
                                if (selectableState is SingleSelectableState) {
                                    onDismissRequest()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
