package com.ssverma.feature.filter.ui.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.component.RangeSliderScale
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.component.SliderScale
import com.ssverma.feature.filter.R
import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.feature.filter.domain.processor.DiscoverFilterState
import com.ssverma.feature.filter.domain.processor.asDiscoverOptions
import com.ssverma.feature.filter.ui.filter.component.FilterPickerChip
import com.ssverma.feature.filter.ui.filter.component.MultiSelectableFilterFlowRow
import com.ssverma.feature.filter.ui.filter.component.MultiSelectableFilterRow
import com.ssverma.feature.filter.ui.filter.component.NonSelectedFilterChip
import com.ssverma.feature.filter.ui.filter.component.SelectedFilterChip
import com.ssverma.feature.filter.ui.filter.component.SingleSelectableFilterFlowRow
import com.ssverma.feature.filter.ui.filter.component.SingleSelectableFilterRow
import com.ssverma.shared.domain.DiscoverConfig
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.domain.utils.formatLocally
import com.ssverma.shared.ui.component.ClickThroughFilterChip
import com.ssverma.shared.ui.component.WatchProviderLogo
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersScreen(
    isTv: Boolean,
    onBackPressed: () -> Unit,
    onFilterApplied: (filterState: DiscoverFilterState) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FilterViewModel = hiltViewModel(),
    listState: LazyListState = rememberLazyListState(),
    initialConfig: DiscoverConfig?,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(isTv) {
        viewModel.init(
            isTv = isTv,
            initialConfig = initialConfig,
        )
    }

    val selectedCountry = remember { Locale.getDefault().country }

    val isAnyFilterSelected by remember(uiState.filters) {
        derivedStateOf {
            uiState.filters.any { !it.groupContent.isEffectivelyEmpty() }
        }
    }

    Scaffold(
        topBar = {
            ShowTimeTopAppBar(
                title = stringResource(id = R.string.sort_and_filters),
                onBackPressed = onBackPressed,
                navIcon = Icons.Rounded.Close,
                actions = {
                    if (isAnyFilterSelected) {
                        TextButton(onClick = { uiState.filters.forEach { it.groupContent.reset() } }) {
                            Text(text = stringResource(id = R.string.clear_all))
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.filters.isNotEmpty()) {
                Button(
                    onClick = {
                        val finalState = uiState.filters.asDiscoverOptions()
                        onFilterApplied(finalState)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(id = R.string.apply),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                ShowTimeLoadingIndicator()
            }
        } else {
            FilterContent(
                filterGroups = uiState.filters,
                lisState = listState,
                selectedCountry = selectedCountry,
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onFilterPickerOpened = viewModel::onFilterPickerOpened,
                isSearching = uiState.isSearching,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun FilterContent(
    filterGroups: List<FilterGroup>,
    lisState: LazyListState,
    selectedCountry: String,
    onSearchQueryChanged: (FilterId, String) -> Unit,
    onFilterPickerOpened: (FilterId) -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {

    LazyColumn(state = lisState, modifier = modifier) {
        items(
            count = filterGroups.size,
            key = { filterGroups[it].groupId.hashCode() }
        ) { index ->
            val group = filterGroups[index]

            FilterGroupItem(
                title = group.title.asString(),
                icon = group.icon,
                showDivider = index != filterGroups.lastIndex,
                showClear = !group.groupContent.isEffectivelyEmpty(),
                onClearClick = { group.groupContent.reset() },
                groupFilterContent = {
                    if (group.groupId is FilterId.CollectionTypeId.Static.Availability) {
                        Text(
                            text = selectedCountry,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.extraSmall
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            ) {
                if (group.groupId == FilterId.CollectionTypeId.Dynamic.WatchProviders &&
                    group.groupContent is FilterGroupContentType.ListType.MultiSelectableListType
                ) {
                    WatchProviderFilterRow(
                        items = group.groupContent.items,
                        selectableState = group.groupContent.selectionState,
                        onPickerOpened = { onFilterPickerOpened(group.groupId) },
                        isSearching = isSearching,
                        onSearchQueryChanged = onSearchQueryChanged
                    )
                } else {
                    when (group.groupContent) {
                        is FilterGroupContentType.ListType.SingleSelectableListType -> {
                            when (group.groupContent.displayMode) {
                                ListDisplayMode.HorizontalRow -> {
                                    SingleSelectableFilterRow(
                                        items = group.groupContent.items,
                                        selectableState = group.groupContent.selectionState
                                    )
                                }

                                ListDisplayMode.FlowRow -> {
                                    SingleSelectableFilterFlowRow(
                                        items = group.groupContent.items,
                                        selectableState = group.groupContent.selectionState
                                    )
                                }

                                ListDisplayMode.Picker -> {
                                    FilterPickerChip(
                                        items = group.groupContent.items,
                                        selectableState = group.groupContent.selectionState,
                                        groupId = group.groupId,
                                        isSearching = isSearching,
                                        onSearchQueryChanged = onSearchQueryChanged,
                                        onPickerOpened = { onFilterPickerOpened(group.groupId) }
                                    )
                                }
                            }
                        }

                        is FilterGroupContentType.ListType.MultiSelectableListType -> {
                            when (group.groupContent.displayMode) {
                                ListDisplayMode.HorizontalRow -> {
                                    MultiSelectableFilterRow(
                                        items = group.groupContent.items,
                                        selectableState = group.groupContent.selectionState
                                    )
                                }

                                ListDisplayMode.FlowRow -> {
                                    MultiSelectableFilterFlowRow(
                                        items = group.groupContent.items,
                                        selectableState = group.groupContent.selectionState
                                    )
                                }

                                ListDisplayMode.Picker -> {
                                    FilterPickerChip(
                                        items = group.groupContent.items,
                                        selectableState = group.groupContent.selectionState,
                                        groupId = group.groupId,
                                        isSearching = isSearching,
                                        onSearchQueryChanged = onSearchQueryChanged,
                                        onPickerOpened = { onFilterPickerOpened(group.groupId) }
                                    )
                                }
                            }
                        }

                        is FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType -> {
                            val dateRangeContent = group.groupContent
                            var showDatePickerForFrom by remember { mutableStateOf(false) }
                            var showDatePickerForTo by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterDateChip(
                                    label = "From",
                                    date = dateRangeContent.state.fromValue,
                                    onClick = { showDatePickerForFrom = true }
                                )
                                FilterDateChip(
                                    label = "To",
                                    date = dateRangeContent.state.toValue,
                                    onClick = { showDatePickerForTo = true }
                                )
                            }

                            if (showDatePickerForFrom) {
                                FilterDatePickerDialog(
                                    initialDate = dateRangeContent.state.fromValue
                                        ?: dateRangeContent.min,
                                    onDateSelected = { selectedDate ->
                                        dateRangeContent.state.onFromValueSelected(selectedDate)
                                        // Validation: if from > to, clear to or set to = from
                                        dateRangeContent.state.toValue?.let { to ->
                                            if (selectedDate.isAfter(to)) {
                                                dateRangeContent.state.onToValueSelected(null)
                                            }
                                        }
                                        showDatePickerForFrom = false
                                    },
                                    onDismiss = { showDatePickerForFrom = false }
                                )
                            }

                            if (showDatePickerForTo) {
                                FilterDatePickerDialog(
                                    initialDate = dateRangeContent.state.toValue
                                        ?: dateRangeContent.max,
                                    onDateSelected = { selectedDate ->
                                        dateRangeContent.state.onToValueSelected(selectedDate)
                                        // Validation: if to < from, clear from or set from = to
                                        dateRangeContent.state.fromValue?.let { from ->
                                            if (selectedDate.isBefore(from)) {
                                                dateRangeContent.state.onFromValueSelected(null)
                                            }
                                        }
                                        showDatePickerForTo = false
                                    },
                                    onDismiss = { showDatePickerForTo = false }
                                )
                            }
                        }

                        is FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType -> {
                            val rangeContent = group.groupContent
                            if (rangeContent.isRange) {
                                RangeSliderScale(
                                    secondaryGap = rangeContent.secondaryGap,
                                    primaryGap = rangeContent.primaryGap,
                                    min = rangeContent.min,
                                    max = rangeContent.max,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    currentStart = rangeContent.state.fromValue?.toFloat()
                                        ?: rangeContent.min.toFloat(),
                                    currentEnd = rangeContent.state.toValue?.toFloat()
                                        ?: rangeContent.max.toFloat(),
                                    labelFormatter = {
                                        when (group.groupId) {
                                            FilterId.RangeTypeId.NumberRange.Runtime -> "${it.toInt()}m"
                                            else -> "${it.toInt()}"
                                        }
                                    },
                                    onValueChange = { start: Float, end: Float ->
                                        rangeContent.state.onFromValueSelected(start.toInt())
                                        rangeContent.state.onToValueSelected(end.toInt())
                                    }
                                )
                            } else {
                                SliderScale(
                                    secondaryGap = rangeContent.secondaryGap,
                                    primaryGap = rangeContent.primaryGap,
                                    min = rangeContent.min,
                                    max = rangeContent.max,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    current = rangeContent.state.toValue?.toFloat()
                                        ?: rangeContent.min.toFloat(),
                                    labelFormatter = {
                                        when (group.groupId) {
                                            FilterId.RangeTypeId.NumberRange.Runtime -> "${it.toInt()}m"
                                            else -> "${it.toInt()}"
                                        }
                                    },
                                    onValueChange = {
                                        rangeContent.state.toValue?.let { _ ->
                                            rangeContent.state.onToValueSelected(it.toInt())
                                        } ?: rangeContent.state.onToValueSelected(it.toInt())
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "footer") {
            Spacer(modifier = Modifier.height(ApplyButtonHeight + ApplyButtonVerticalSpacing))
        }
    }
}

@Composable
fun WatchProviderFilterRow(
    items: List<FilterItem>,
    selectableState: com.ssverma.core.ui.MultiSelectableState<FilterItem>,
    onPickerOpened: () -> Unit,
    modifier: Modifier = Modifier,
    isSearching: Boolean = false,
    onSearchQueryChanged: (FilterId, String) -> Unit = { _, _ -> }
) {
    val selectedItems = selectableState.selected()
    var showPicker by remember { mutableStateOf(false) }

    androidx.compose.foundation.lazy.LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            ClickThroughFilterChip(
                onClick = {
                    onPickerOpened()
                    showPicker = true
                },
                selected = false,
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4285F4),
                            Color(0xFF9B72CB),
                            Color(0xFFD96570),
                            Color(0xFFF4AF5F)
                        )
                    )
                ),
            ) {
                Text(
                    text = stringResource(id = R.string.select),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        items(selectedItems.size) { index ->
            val item = selectedItems.elementAt(index)
            val dynamicItem = item as? FilterItem.Dynamic
            if (dynamicItem != null) {
                WatchProviderLogo(
                    provider = com.ssverma.shared.domain.model.ProviderInfo(
                        logoPath = dynamicItem.iconUrl.orEmpty(),
                        providerId = dynamicItem.id.toIntOrNull() ?: 0,
                        providerName = dynamicItem.text.asString(),
                        displayPriority = 0
                    ),
                    onClick = { selectableState.onSelectionChanged(item) },
                    size = 40.dp,
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                )
            } else {
                com.ssverma.core.ui.Toggleable(
                    item = item,
                    modifier = Modifier,
                    selectableState = selectableState,
                    onContent = { SelectedFilterChip(text = item.text.asString()) },
                    offContent = { NonSelectedFilterChip(text = item.text.asString()) }
                )
            }
        }
    }

    if (showPicker) {
        com.ssverma.feature.filter.ui.filter.component.FilterPickerBottomSheet(
            items = items,
            selectableState = selectableState,
            groupId = FilterId.CollectionTypeId.Dynamic.WatchProviders,
            isSearching = isSearching,
            onSearchQueryChanged = onSearchQueryChanged,
            onDismissRequest = { showPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = DateUtils.toMillis(initialDate)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(DateUtils.fromMillis(it))
                }
                onDismiss()
            }) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDateChip(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        selected = date != null,
        onClick = onClick,
        label = {
            Text(
                text = if (date != null) "$label: ${date.formatLocally()}" else "Select $label Date"
            )
        },
        modifier = modifier
    )
}

@Composable
private fun FilterGroupItem(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    showDivider: Boolean = true,
    showClear: Boolean = false,
    onClearClick: () -> Unit = {},
    groupFilterContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                if (showClear) {
                    TextButton(onClick = onClearClick) {
                        Text(
                            text = stringResource(id = R.string.clear),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                groupFilterContent()
            }
        }
        content()
        Spacer(modifier = Modifier.height(16.dp))
        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
        }
    }
}

private val ApplyButtonVerticalSpacing = 16.dp
private val ApplyButtonHeight = 56.dp
