package com.ssverma.feature.filter.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.domain.DiscoverOption
import com.ssverma.core.domain.MovieDiscoverConfig
import com.ssverma.core.domain.TvDiscoverConfig
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.component.SliderScale
import com.ssverma.feature.filter.R
import com.ssverma.feature.filter.domain.FilterId
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun <T : DiscoverOption.OptionScope> FiltersScreen(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    filterGroups: List<FilterGroup>,
    onFilterApplied: (discoverOptions: List<T>) -> Unit
) {
    val selectedCountry = remember { Locale.getDefault().country }

    Box(modifier) {
        FilterContent(filterGroups, listState, selectedCountry)
        ExtendedFloatingActionButton(
            text = {
                Text(
                    text = stringResource(id = R.string.apply),
                    color = MaterialTheme.colors.onSecondary
                )
            },
            onClick = {
                val options = buildDiscoverConfig<T>(filterGroups, selectedCountry)
                onFilterApplied(options)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(ApplyButtonHeight)
                .padding(vertical = ApplyButtonVerticalSpacing)
        )
    }
}

@Composable
fun FilterContent(
    filterGroups: List<FilterGroup>,
    lisState: LazyListState,
    selectedCountry: String
) {

    LaunchedEffect(key1 = true) {
        delay(1000)
        lisState.scrollToItem(0)
    }

    LazyColumn(state = lisState) {
        itemsIndexed(filterGroups) { index, group ->
            FilterGroupItem(
                title = group.title.asString(),
                showDivider = index != filterGroups.lastIndex,
                groupFilterContent = {
                    if (group.groupId is FilterId.CollectionTypeId.Static.Availability) {
                        Text(
                            text = selectedCountry,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier
                                .border(1.dp, color = MaterialTheme.colors.onBackground)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            ) {
                when (group.groupContent) {
                    is FilterGroupContentType.ListType.SingleSelectableListType -> {
                        if (group.groupContent.horizontal) {
                            SingleSelectableFilterRow(
                                items = group.groupContent.items,
                                selectableState = group.groupContent.selectionState
                            )
                        }
                    }
                    is FilterGroupContentType.ListType.MultiSelectableListType -> {
                        if (group.groupContent.horizontal) {
                            MultiSelectableFilterRow(
                                items = group.groupContent.items,
                                selectableState = group.groupContent.selectionState
                            )
                        }
                    }
                    is FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType -> {
                        //
                    }
                    is FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType -> {
                        val rangeContent = group.groupContent
                        SliderScale(
                            secondaryGap = rangeContent.secondaryGap,
                            primaryGap = rangeContent.primaryGap,
                            min = rangeContent.min,
                            max = rangeContent.max,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            current = group.groupContent.state.toValue?.toFloat() ?: 0f,
                            onValueChange = {
                                group.groupContent.state.onToValueSelected(it.toInt())
                            }
                        )
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
    groupFilterContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                groupFilterContent()
            }
        }
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

private fun <T : DiscoverOption.OptionScope> buildDiscoverConfig(
    filterGroups: List<FilterGroup>,
    selectedCountry: String
): List<T> {
    val appliedOptions = mutableListOf<T>()

    filterGroups.forEach { group ->
        when (group.groupContent) {
            is FilterGroupContentType.ListType.MultiSelectableListType -> {
                group.groupContent.selectionState.selected().forEach { filterItem ->
                    val options = filterItem.asDiscoverOptions<T>(
                        filterId = group.groupId,
                        selectedCountry = selectedCountry
                    )
                    appliedOptions.addAll(options)
                }
            }
            is FilterGroupContentType.ListType.SingleSelectableListType -> {
                val filterItem = group.groupContent.selectionState.selected()
                val options = filterItem.asDiscoverOptions<T>(
                    filterId = group.groupId,
                    selectedCountry = selectedCountry
                )
                appliedOptions.addAll(options)
            }
            is FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType -> {
                if (group.groupId is FilterId.RangeTypeId.DateRange) {
                    when (group.groupId) {
                        FilterId.RangeTypeId.DateRange.AirDate -> {
                            //
                        }
                        FilterId.RangeTypeId.DateRange.ReleaseDate -> {
                            //
                        }
                    }
                }
            }
            is FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType -> {
                if (group.groupId is FilterId.RangeTypeId.NumberRange) {
                    when (group.groupId) {
                        FilterId.RangeTypeId.NumberRange.Rating -> {
                            group.groupContent.state.from?.let {
                                val option = DiscoverOption.Rating.From(
                                    from = it
                                )
                                appliedOptions.add(option as T)
                            }
                            group.groupContent.state.toValue?.let {
                                val option = DiscoverOption.Rating.To(
                                    to = it
                                )
                                appliedOptions.add(option as T)
                            }
                        }
                    }
                }
            }
        }
    }

    return appliedOptions
}

private fun <T : DiscoverOption.OptionScope> FilterItem?.asDiscoverOptions(
    filterId: FilterId,
    selectedCountry: String
): List<T> {
    val result = mutableListOf<T>()

    when (this) {
        is FilterItem.Dynamic -> {
            if (filterId is FilterId.CollectionTypeId.Dynamic) {
                val option = buildDiscoverOption(
                    filterId = filterId,
                    key = this.id
                )
                result.add(option as T)
            }
        }
        is FilterItem.Static -> {
            if (this.discoverOption is DiscoverOption.Monetization) {
                result.add(DiscoverOption.Region(iso3 = selectedCountry) as T)
            }
            result.add(this.discoverOption as T)
        }
        null -> {
            //No op
        }
    }
    return result
}

private fun buildDiscoverOption(
    filterId: FilterId.CollectionTypeId.Dynamic,
    key: String
): DiscoverOption {
    return when (filterId) {
        FilterId.CollectionTypeId.Dynamic.Country -> {
            DiscoverOption.Country(iso3 = key)
        }
        FilterId.CollectionTypeId.Dynamic.Genre -> {
            DiscoverOption.Genre(genreId = key.toIntOrNull() ?: 0)
        }
        FilterId.CollectionTypeId.Dynamic.Keyword -> {
            DiscoverOption.Keyword(keywordId = key.toIntOrNull() ?: 0)
        }
        FilterId.CollectionTypeId.Dynamic.Language -> {
            DiscoverOption.Language(iso3 = key)
        }
        FilterId.CollectionTypeId.Dynamic.Person -> {
            DiscoverOption.Person(personId = key.toIntOrNull() ?: 0)
        }
    }
}

class RangeState<T>(
    val from: T? = null,
    val to: T? = null
) {
    private var _fromValue: T? by mutableStateOf(from)
    private var _toValue: T? by mutableStateOf(to)

    val fromValue get() = _fromValue
    val toValue get() = _toValue

    fun onFromValueSelected(value: T?) {
        this._fromValue = value
    }

    fun onToValueSelected(value: T?) {
        this._toValue = value
    }
}