package com.ssverma.showtime.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.R
import com.ssverma.showtime.api.QueryMultiValue
import com.ssverma.showtime.data.FilterGroup
import com.ssverma.showtime.data.FilterGroupId
import com.ssverma.showtime.domain.model.LabelFilter
import com.ssverma.showtime.ui.common.*
import com.ssverma.showtime.utils.formatAsIso
import java.time.LocalDate

@Composable
fun FiltersScreen(
    modifier: Modifier = Modifier,
    filterGroups: List<FilterGroup>,
    onFilterApplied: (Map<String, String>) -> Unit
) {
    val filterState = rememberFilterState(filterGroups = filterGroups)

    Box(modifier) {
        FilterContent(filterGroups, filterState)
        ExtendedFloatingActionButton(
            text = {
                Text(text = stringResource(id = R.string.apply), color = MaterialTheme.colors.onSecondary)
                   },
            onClick = {
                onFilterApplied(filterState.asDiscoverMap())
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(ApplyButtonHeight)
                .padding(vertical = ApplyButtonVerticalSpacing)
        )
    }
}

@Composable
fun FilterContent(filterGroups: List<FilterGroup>, filterState: FilterState) {
    LazyColumn {
        itemsIndexed(filterGroups) { index, group ->
            FilterGroupItem(
                title = stringResource(id = group.titleRes),
                showDivider = index != filterGroups.lastIndex
            ) {
                when (group) {
                    is FilterGroup.ListGroup.MultiSelectableGroup -> {
                        MultiSelectableFilterRow(
                            items = group.filters,
                            selectableState = filterState.of(group)
                        )
                    }
                    is FilterGroup.ListGroup.SingleSelectableGroup -> {
                        SingleSelectableFilterRow(
                            items = group.filters,
                            selectableState = filterState.of(group)
                        )
                    }
                    is FilterGroup.RangeGroup.NumberRangeGroup -> {
                        SliderScale(
                            secondaryGap = group.rangeFilter.secondaryGap,
                            primaryGap = group.rangeFilter.primaryGap,
                            min = group.rangeFilter.min,
                            max = group.rangeFilter.max,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            current = filterState.of(group).toValue?.toFloat() ?: 0f,
                            onValueChange = {
                                filterState.of(group).onToValueSelected(it)
                            }
                        )
                    }
                    is FilterGroup.RangeGroup.DateRangeGroup -> {
                        //TODO
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
private fun FilterGroupItem(
    title: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        content()
        Spacer(modifier = Modifier.height(16.dp))
        if (showDivider) {
            Divider(
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.37f),
                thickness = 0.5.dp
            )
        }
    }
}

private val ApplyButtonVerticalSpacing = 16.dp
private val ApplyButtonHeight = 72.dp

@Composable
fun rememberFilterState(filterGroups: List<FilterGroup>): FilterState {
    return remember(filterGroups) {
        FilterState(filterGroups)
    }
}

interface FilterGroupState

class ListFilterGroupState(
    val selectableState: SelectableState<LabelFilter>
) : FilterGroupState

abstract class RangeFilterGroupState<T>(
    defaultRange: Range<T>? = null
) : FilterGroupState {
    private var _fromValue: T? by mutableStateOf(defaultRange?.from)
    private var _toValue: T? by mutableStateOf(defaultRange?.to)

    val fromValue get() = _fromValue
    val toValue get() = _toValue

    fun onFromValueSelected(value: T?) {
        this._fromValue = value
    }

    fun onToValueSelected(value: T?) {
        this._toValue = value
    }
}

class NumberRangeFilterGroupState(defaultRange: Range<Number>? = null) :
    RangeFilterGroupState<Number>(defaultRange)

class DateRangeFilterGroupState(defaultRange: Range<LocalDate>? = null) :
    RangeFilterGroupState<LocalDate>(defaultRange)

data class Range<T>(
    val from: T,
    var to: T
)

class FilterState(
    private val filterGroups: List<FilterGroup>
) {
    private val appliedFilters by mutableStateOf(mutableMapOf<FilterGroupId, FilterGroupState>())

    init {
        filterGroups.forEach {
            when (it) {
                is FilterGroup.ListGroup.MultiSelectableGroup -> {
                    appliedFilters[it.groupId] =
                        ListFilterGroupState(MultiSelectableState(it.default))
                }
                is FilterGroup.ListGroup.SingleSelectableGroup -> {
                    appliedFilters[it.groupId] =
                        ListFilterGroupState(SingleSelectableState(it.default))
                }
                is FilterGroup.RangeGroup.DateRangeGroup -> {
                    appliedFilters[it.groupId] =
                        DateRangeFilterGroupState(it.default)
                }
                is FilterGroup.RangeGroup.NumberRangeGroup -> {
                    appliedFilters[it.groupId] =
                        NumberRangeFilterGroupState(it.default)
                }
            }
        }
    }

    private fun of(groupId: FilterGroupId): FilterGroupState {
        return appliedFilters[groupId] ?: throw IllegalStateException("No state provided")
    }

    fun of(group: FilterGroup.ListGroup.SingleSelectableGroup): SingleSelectableState<LabelFilter> {
        return (of(group.groupId) as ListFilterGroupState).selectableState as SingleSelectableState<LabelFilter>
    }

    fun of(group: FilterGroup.ListGroup.MultiSelectableGroup): MultiSelectableState<LabelFilter> {
        return (of(group.groupId) as ListFilterGroupState).selectableState as MultiSelectableState<LabelFilter>
    }

    fun of(group: FilterGroup.RangeGroup.NumberRangeGroup): NumberRangeFilterGroupState {
        return of(groupId = group.groupId) as NumberRangeFilterGroupState
    }

    fun of(group: FilterGroup.RangeGroup.DateRangeGroup): RangeFilterGroupState<LocalDate> {
        return of(groupId = group.groupId) as DateRangeFilterGroupState
    }


    fun asDiscoverMap(): Map<String, String> {
        val discoverMap = mutableMapOf<String, String>()

        filterGroups.forEach {
            when (it) {
                is FilterGroup.ListGroup.MultiSelectableGroup -> {
                    val builder = QueryMultiValue.orBuilder()
                    of(it).selected().forEach { filter ->
                        builder.or(filter.filterValue)
                    }

                    builder.build().asFormattedValues()?.let { filterValue ->
                        discoverMap[it.groupId.queryKey] = filterValue
                    }
                }
                is FilterGroup.ListGroup.SingleSelectableGroup -> {
                    of(it).selected()?.filterValue?.let { filterValue ->
                        discoverMap[it.groupId.queryKey] = filterValue
                    }
                }
                is FilterGroup.RangeGroup.DateRangeGroup -> {
                    of(it).fromValue?.formatAsIso()?.let { filterValue ->
                        discoverMap[it.groupId.fromQueryKey] = filterValue
                    }
                    of(it).toValue?.formatAsIso()?.let { filterValue ->
                        discoverMap[it.groupId.toQueryKey] = filterValue
                    }
                }
                is FilterGroup.RangeGroup.NumberRangeGroup -> {
                    of(it).fromValue?.toString()?.let { filterValue ->
                        discoverMap[it.groupId.fromQueryKey] = filterValue
                    }

                    of(it).toValue?.toString()?.let { filterValue ->
                        discoverMap[it.groupId.toQueryKey] = filterValue
                    }
                }
            }
        }

        return discoverMap
    }

}