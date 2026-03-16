package com.ssverma.feature.filter.ui.filter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Explicit
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.ui.graphics.vector.ImageVector
import com.ssverma.core.ui.MultiSelectableState
import com.ssverma.core.ui.SingleSelectableState
import com.ssverma.core.ui.UiText
import com.ssverma.feature.filter.R
import com.ssverma.feature.filter.domain.model.DynamicFilterItem
import com.ssverma.feature.filter.domain.model.Filter
import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.SortBy
import java.time.LocalDate

data class FilterUiState(
    val filters: List<FilterGroup>,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false
)

data class FilterGroup(
    val groupId: FilterId,
    val title: UiText,
    val icon: ImageVector? = null,
    val groupContent: FilterGroupContentType
)

enum class ListDisplayMode {
    HorizontalRow,
    FlowRow,
    Picker
}

sealed interface FilterGroupContentType {
    fun clear()
    fun reset()
    fun isDefault(): Boolean
    fun isEffectivelyEmpty(): Boolean

    sealed interface ListType : FilterGroupContentType {
        data class SingleSelectableListType(
            val displayMode: ListDisplayMode,
            val defaultSelectedItem: FilterItem? = null,
            val items: List<FilterItem>,
            val selectionState: SingleSelectableState<FilterItem> = SingleSelectableState(
                defaultSelectedItem
            )
        ) : ListType {
            override fun clear() = selectionState.clear()
            override fun reset() = selectionState.reset()
            override fun isDefault(): Boolean = selectionState.isDefault()
            override fun isEffectivelyEmpty(): Boolean = selectionState.selected() == null
        }

        data class MultiSelectableListType(
            val displayMode: ListDisplayMode,
            val defaultSelectedItems: Set<FilterItem> = emptySet(),
            val items: List<FilterItem>,
            val selectionState: MultiSelectableState<FilterItem> = MultiSelectableState(
                defaultSelectedItems
            )
        ) : ListType {
            override fun clear() = selectionState.clear()
            override fun reset() = selectionState.reset()
            override fun isDefault(): Boolean = selectionState.isDefault()
            override fun isEffectivelyEmpty(): Boolean = selectionState.selected().isEmpty()
        }
    }

    sealed interface RangeType<T> : FilterGroupContentType {
        val min: T
        val max: T

        sealed interface ScaleRangeType<T> : RangeType<T> {
            override val min: T
            override val max: T
            val primaryGap: T
            val secondaryGap: T
            val defaultMin: T?
            val defaultMax: T?
            val state: RangeState<T>

            data class IntScaleRangeType(
                override val min: Int,
                override val max: Int,
                override val primaryGap: Int,
                override val secondaryGap: Int,
                override val defaultMin: Int? = null,
                override val defaultMax: Int? = null,
                val isRange: Boolean = false,
                override val state: RangeState<Int> = RangeState(from = defaultMin, to = defaultMax)
            ) : ScaleRangeType<Int> {
                override fun clear() = state.clear()
                override fun reset() = state.reset()
                override fun isDefault(): Boolean = state.isDefault()
                override fun isEffectivelyEmpty(): Boolean =
                    state.fromValue == null && state.toValue == null
            }
        }

        sealed interface PickerRangeType<T> : RangeType<T> {
            override val min: T
            override val max: T
            val defaultMin: T?
            val defaultMax: T?
            val state: RangeState<T>

            data class DatePickerRangeType(
                override val min: LocalDate,
                override val max: LocalDate,
                override val defaultMin: LocalDate? = null,
                override val defaultMax: LocalDate? = null,
                override val state: RangeState<LocalDate> = RangeState(
                    from = defaultMin,
                    to = defaultMax
                )
            ) : PickerRangeType<LocalDate> {
                override fun clear() = state.clear()
                override fun reset() = state.reset()
                override fun isDefault(): Boolean = state.isDefault()
                override fun isEffectivelyEmpty(): Boolean =
                    state.fromValue == null && state.toValue == null
            }
        }
    }
}

sealed interface FilterItem {
    val text: UiText

    data class Static(
        val option: Any,
        override val text: UiText
    ) : FilterItem

    data class Dynamic(
        val id: String,
        override val text: UiText
    ) : FilterItem
}

fun List<Filter>.asUiFilters(
    initialOptions: List<DiscoverOption> = emptyList(),
    initialSortBy: SortBy? = null,
    initialOrder: Order? = null
): List<FilterGroup> {
    val result = mutableListOf<FilterGroup>()

    this.forEach { filter ->
        when (filter) {
            is Filter.CollectionFilter.Static -> {
                when (filter.id) {
                    is FilterId.CollectionTypeId.Static.Availability -> {
                        val filterGroupAvailability = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.availability),
                            icon = Icons.Rounded.EventAvailable,
                            groupContent = if (filter.singleSelectable) {
                                val items = filter.items.map {
                                    asMonetizationUiFilterItem(it.option as DiscoverOption.Monetization)
                                }
                                FilterGroupContentType.ListType.SingleSelectableListType(
                                    displayMode = ListDisplayMode.FlowRow,
                                    items = items,
                                    defaultSelectedItem = items.find { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }
                                )
                            } else {
                                val items = filter.items.map {
                                    asMonetizationUiFilterItem(it.option as DiscoverOption.Monetization)
                                }
                                FilterGroupContentType.ListType.MultiSelectableListType(
                                    displayMode = ListDisplayMode.FlowRow,
                                    items = items,
                                    defaultSelectedItems = items.filter { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }.toSet()
                                )
                            }
                        )
                        result.add(filterGroupAvailability)
                    }

                    is FilterId.CollectionTypeId.Static.Certification -> {
                        val filterGroupCertification = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.certification),
                            icon = Icons.Rounded.Verified,
                            groupContent = if (filter.singleSelectable) {
                                val items = filter.items.map {
                                    asCertificationUiFilterItem(it.option as DiscoverOption.Certification)
                                }
                                FilterGroupContentType.ListType.SingleSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItem = items.find { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }
                                )
                            } else {
                                val items = filter.items.map {
                                    asCertificationUiFilterItem(it.option as DiscoverOption.Certification)
                                }
                                FilterGroupContentType.ListType.MultiSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItems = items.filter { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }.toSet()
                                )
                            }
                        )
                        result.add(filterGroupCertification)
                    }

                    is FilterId.CollectionTypeId.Static.ReleaseType -> {
                        val filterGroupReleaseType = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.release_type),
                            icon = Icons.Rounded.NewReleases,
                            groupContent = if (filter.singleSelectable) {
                                val items = filter.items.map {
                                    asReleaseTypeUiFilterItem(it.option as DiscoverOption.ReleaseType)
                                }
                                FilterGroupContentType.ListType.SingleSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItem = items.find { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }
                                )
                            } else {
                                val items = filter.items.map {
                                    asReleaseTypeUiFilterItem(it.option as DiscoverOption.ReleaseType)
                                }
                                FilterGroupContentType.ListType.MultiSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItems = items.filter { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }.toSet()
                                )
                            }
                        )
                        result.add(filterGroupReleaseType)
                    }

                    is FilterId.CollectionTypeId.Static.Status -> {
                        val filterGroupStatus = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.status),
                            icon = Icons.Rounded.Info,
                            groupContent = if (filter.singleSelectable) {
                                val items = filter.items.map { asStatusUiFilterItem(it.option as DiscoverOption.Status) }
                                FilterGroupContentType.ListType.SingleSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItem = items.find { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }
                                )
                            } else {
                                val items = filter.items.map { asStatusUiFilterItem(it.option as DiscoverOption.Status) }
                                FilterGroupContentType.ListType.MultiSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItems = items.filter { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }.toSet()
                                )
                            }
                        )
                        result.add(filterGroupStatus)
                    }

                    is FilterId.CollectionTypeId.Static.Type -> {
                        val filterGroupType = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.type),
                            icon = Icons.Rounded.Category,
                            groupContent = if (filter.singleSelectable) {
                                val items = filter.items.map { asTvTypeUiFilterItem(it.option as DiscoverOption.TvType) }
                                FilterGroupContentType.ListType.SingleSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItem = items.find { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }
                                )
                            } else {
                                val items = filter.items.map { asTvTypeUiFilterItem(it.option as DiscoverOption.TvType) }
                                FilterGroupContentType.ListType.MultiSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItems = items.filter { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }.toSet()
                                )
                            }
                        )
                        result.add(filterGroupType)
                    }

                    is FilterId.CollectionTypeId.Static.IncludeAdult -> {
                        val filterGroupIncludeAdult = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.include_adult),
                            icon = Icons.Rounded.Explicit,
                            groupContent = if (filter.singleSelectable) {
                                val items = filter.items.map { asIncludeAdultUiFilterItem(it.option as DiscoverOption.IncludeAdult) }
                                FilterGroupContentType.ListType.SingleSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItem = items.find { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }
                                )
                            } else {
                                val items = filter.items.map { asIncludeAdultUiFilterItem(it.option as DiscoverOption.IncludeAdult) }
                                FilterGroupContentType.ListType.MultiSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItems = items.filter { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }.toSet()
                                )
                            }
                        )
                        result.add(filterGroupIncludeAdult)
                    }

                    is FilterId.CollectionTypeId.Static.IncludeVideo -> {
                        val filterGroupIncludeVideo = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.include_video),
                            icon = Icons.Rounded.Movie,
                            groupContent = if (filter.singleSelectable) {
                                val items = filter.items.map { asIncludeVideoUiFilterItem(it.option as DiscoverOption.IncludeVideo) }
                                FilterGroupContentType.ListType.SingleSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItem = items.find { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }
                                )
                            } else {
                                val items = filter.items.map { asIncludeVideoUiFilterItem(it.option as DiscoverOption.IncludeVideo) }
                                FilterGroupContentType.ListType.MultiSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItems = items.filter { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }.toSet()
                                )
                            }
                        )
                        result.add(filterGroupIncludeVideo)
                    }

                    is FilterId.CollectionTypeId.Static.VoteCount -> {
                        val filterGroupVoteCount = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.vote_count),
                            icon = Icons.Rounded.HowToVote,
                            groupContent = if (filter.singleSelectable) {
                                val items = filter.items.map { asVoteCountUiFilterItem(it.option as DiscoverOption.Rating.VoteCount) }
                                FilterGroupContentType.ListType.SingleSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItem = items.find { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }
                                )
                            } else {
                                val items = filter.items.map { asVoteCountUiFilterItem(it.option as DiscoverOption.Rating.VoteCount) }
                                FilterGroupContentType.ListType.MultiSelectableListType(
                                    displayMode = ListDisplayMode.HorizontalRow,
                                    items = items,
                                    defaultSelectedItems = items.filter { item ->
                                        initialOptions.any { it == (item as FilterItem.Static).option }
                                    }.toSet()
                                )
                            }
                        )
                        result.add(filterGroupVoteCount)
                    }
                }
            }

            is Filter.CollectionFilter.Dynamic -> {
                when (filter.id) {
                    FilterId.CollectionTypeId.Dynamic.Country -> {
                        val filterGroupCountry = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.country),
                            icon = Icons.Rounded.Public,
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupCountry)
                    }

                    FilterId.CollectionTypeId.Dynamic.Genre -> {
                        val filterGroupGenre = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.genres),
                            icon = Icons.Rounded.Movie,
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupGenre)
                    }

                    FilterId.CollectionTypeId.Dynamic.WithoutGenre -> {
                        val filterGroupGenre = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.without_genres),
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupGenre)
                    }

                    FilterId.CollectionTypeId.Dynamic.Keyword -> {
                        val filterGroupKeyword = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.keywords),
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupKeyword)
                    }

                    FilterId.CollectionTypeId.Dynamic.WithoutKeyword -> {
                        val filterGroupKeyword = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.without_keywords),
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupKeyword)
                    }

                    FilterId.CollectionTypeId.Dynamic.Language -> {
                        val filterGroupLanguage = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.language),
                            icon = Icons.Rounded.Language,
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupLanguage)
                    }

                    FilterId.CollectionTypeId.Dynamic.Person -> {
                        val filterGroupPerson = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.people),
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupPerson)
                    }

                    FilterId.CollectionTypeId.Dynamic.Cast -> {
                        val filterGroupPerson = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.cast),
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupPerson)
                    }

                    FilterId.CollectionTypeId.Dynamic.Crew -> {
                        val filterGroupPerson = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.crew),
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupPerson)
                    }

                    FilterId.CollectionTypeId.Dynamic.Company -> {
                        val filterGroupCompany = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.company),
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupCompany)
                    }

                    FilterId.CollectionTypeId.Dynamic.WithoutCompany -> {
                        val filterGroupCompany = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.without_companies),
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupCompany)
                    }

                    FilterId.CollectionTypeId.Dynamic.Network -> {
                        val filterGroupNetwork = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.network),
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupNetwork)
                    }

                    FilterId.CollectionTypeId.Dynamic.WatchProviders -> {
                        val filterGroupWatchProviders = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.watch_providers),
                            groupContent = filter.items.asUiFilterGroup(
                                groupId = filter.id,
                                singleSelectable = filter.singleSelectable,
                                displayMode = ListDisplayMode.Picker,
                                initialOptions = initialOptions
                            )
                        )
                        result.add(filterGroupWatchProviders)
                    }
                }
            }

            is Filter.SelectionFilter.Single -> {
                when (filter.id) {
                    FilterId.SelectionTypeId.SortBy -> {
                        val sortByItems = mutableListOf<FilterItem>()
                        filter.items.forEach { filterItem ->
                            val sortBy = filterItem.option as SortBy
                            if (sortBy != SortBy.None) {
                                sortByItems.add(asSortByUiFilterItem(sortBy.withOrder(Order.Descending)))
                                sortByItems.add(asSortByUiFilterItem(sortBy.withOrder(Order.Ascending)))
                            }
                        }

                        val filterGroupSortBy = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.sort_by),
                            icon = Icons.AutoMirrored.Rounded.Sort,
                            groupContent = FilterGroupContentType.ListType.SingleSelectableListType(
                                displayMode = ListDisplayMode.Picker,
                                items = sortByItems,
                                defaultSelectedItem = sortByItems.find { item ->
                                    val option = (item as? FilterItem.Static)?.option as? SortBy
                                    option != null && initialSortBy != null && 
                                    option::class == initialSortBy::class && 
                                    option.order == initialOrder
                                } ?: sortByItems.find { item ->
                                    val option = (item as? FilterItem.Static)?.option as? SortBy
                                    option is SortBy.Popularity && option.order == Order.Descending
                                }
                            )
                        )
                        result.add(filterGroupSortBy)
                    }

                    FilterId.SelectionTypeId.SortOrder -> {}
                }
            }

            is Filter.RangeFilter.IntRangeFilter -> {
                when (filter.id) {
                    FilterId.RangeTypeId.NumberRange.Rating -> {
                        val filterGroupRating = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.rating),
                            icon = Icons.Rounded.Star,
                            groupContent = FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType(
                                min = filter.from,
                                max = filter.to,
                                primaryGap = 2,
                                secondaryGap = 1,
                                defaultMin = initialOptions.filterIsInstance<DiscoverOption.Rating.From>()
                                    .firstOrNull()?.from,
                                defaultMax = initialOptions.filterIsInstance<DiscoverOption.Rating.To>()
                                    .firstOrNull()?.to
                            )
                        )
                        result.add(filterGroupRating)
                    }

                    FilterId.RangeTypeId.NumberRange.Runtime -> {
                        val filterGroupRuntime = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.runtime),
                            icon = Icons.Rounded.Schedule,
                            groupContent = FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType(
                                min = filter.from,
                                max = filter.to,
                                primaryGap = 60,
                                secondaryGap = 30,
                                defaultMin = initialOptions.filterIsInstance<DiscoverOption.Runtime.From>()
                                    .firstOrNull()?.from,
                                defaultMax = initialOptions.filterIsInstance<DiscoverOption.Runtime.To>()
                                    .firstOrNull()?.to
                            )
                        )
                        result.add(filterGroupRuntime)
                    }

                    FilterId.RangeTypeId.NumberRange.VoteAvg -> {
                        val filterGroupRating = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.rating),
                            groupContent = FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType(
                                min = filter.from,
                                max = filter.to,
                                primaryGap = 5,
                                secondaryGap = 1,
                                isRange = true
                            )
                        )
                        result.add(filterGroupRating)
                    }
                }
            }

            is Filter.RangeFilter.DateRangeFilter -> {
                when (filter.id) {
                    FilterId.RangeTypeId.DateRange.AirDate -> {
                        val filterGroupRating = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.air_date),
                            icon = Icons.Rounded.DateRange,
                            groupContent = FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType(
                                min = filter.from,
                                max = filter.to,
                                defaultMin = initialOptions.filterIsInstance<DiscoverOption.AirDate.From>()
                                    .firstOrNull()?.date,
                                defaultMax = initialOptions.filterIsInstance<DiscoverOption.AirDate.To>()
                                    .firstOrNull()?.date
                            )
                        )
                        result.add(filterGroupRating)
                    }

                    FilterId.RangeTypeId.DateRange.ReleaseDate -> {
                        val filterGroupReleaseDate = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.release_date),
                            icon = Icons.Rounded.DateRange,
                            groupContent = FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType(
                                min = filter.from,
                                max = filter.to,
                                defaultMin = (initialOptions.filterIsInstance<DiscoverOption.ReleaseDate.From>()
                                    .firstOrNull()?.date
                                    ?: initialOptions.filterIsInstance<DiscoverOption.PrimaryReleaseDate.From>()
                                        .firstOrNull()?.date),
                                defaultMax = (initialOptions.filterIsInstance<DiscoverOption.ReleaseDate.To>()
                                    .firstOrNull()?.date
                                    ?: initialOptions.filterIsInstance<DiscoverOption.PrimaryReleaseDate.To>()
                                        .firstOrNull()?.date)
                            )
                        )
                        result.add(filterGroupReleaseDate)
                    }
                }
            }
        }
    }

    return result
}

private fun List<DynamicFilterItem>.asUiFilterGroup(
    groupId: FilterId,
    singleSelectable: Boolean,
    displayMode: ListDisplayMode = ListDisplayMode.HorizontalRow,
    initialOptions: List<DiscoverOption> = emptyList()
): FilterGroupContentType {
    val items = this.map {
        FilterItem.Dynamic(
            text = UiText.DynamicText(text = it.displayText),
            id = it.id
        )
    }
    return if (singleSelectable) {
        FilterGroupContentType.ListType.SingleSelectableListType(
            displayMode = displayMode,
            items = items,
            defaultSelectedItem = items.find { item ->
                val dynamicItem = item as FilterItem.Dynamic
                initialOptions.any { option ->
                    isDynamicOptionMatch(groupId, dynamicItem.id, option)
                }
            }
        )
    } else {
        FilterGroupContentType.ListType.MultiSelectableListType(
            displayMode = displayMode,
            items = items,
            defaultSelectedItems = items.filter { item ->
                val dynamicItem = item as FilterItem.Dynamic
                initialOptions.any { option ->
                    isDynamicOptionMatch(groupId, dynamicItem.id, option)
                }
            }.toSet()
        )
    }
}

private fun isDynamicOptionMatch(groupId: FilterId, itemId: String, option: DiscoverOption): Boolean {
    return when (groupId) {
        FilterId.CollectionTypeId.Dynamic.Genre -> option is DiscoverOption.Genre && option.genreId == itemId.toInt()
        FilterId.CollectionTypeId.Dynamic.WithoutGenre -> option is DiscoverOption.WithoutGenre && option.genreId == itemId.toInt()
        FilterId.CollectionTypeId.Dynamic.Language -> option is DiscoverOption.Language && option.iso3 == itemId
        FilterId.CollectionTypeId.Dynamic.Country -> option is DiscoverOption.Country && option.iso3 == itemId
        FilterId.CollectionTypeId.Dynamic.Keyword -> option is DiscoverOption.Keyword && option.keywordId == itemId.toInt()
        FilterId.CollectionTypeId.Dynamic.WithoutKeyword -> option is DiscoverOption.WithoutKeyword && option.keywordId == itemId.toInt()
        FilterId.CollectionTypeId.Dynamic.Company -> option is DiscoverOption.Company && option.companyId == itemId.toInt()
        FilterId.CollectionTypeId.Dynamic.WithoutCompany -> option is DiscoverOption.WithoutCompany && option.companyId == itemId.toInt()
        FilterId.CollectionTypeId.Dynamic.Network -> option is DiscoverOption.Network && option.networkId == itemId.toInt()
        FilterId.CollectionTypeId.Dynamic.Person -> option is DiscoverOption.Person && option.personId == itemId.toInt()
        FilterId.CollectionTypeId.Dynamic.Cast -> option is DiscoverOption.Cast && option.personId == itemId.toInt()
        FilterId.CollectionTypeId.Dynamic.Crew -> option is DiscoverOption.Crew && option.personId == itemId.toInt()
        FilterId.CollectionTypeId.Dynamic.WatchProviders -> option is DiscoverOption.WatchProvider && option.providerId == itemId.toInt()
        else -> false
    }
}

private fun asMonetizationUiFilterItem(option: DiscoverOption.Monetization): FilterItem.Static {
    return when (option) {
        DiscoverOption.Monetization.Ads -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.ads),
                option = option
            )
        }

        DiscoverOption.Monetization.Buy -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.buy),
                option = option
            )
        }

        DiscoverOption.Monetization.Free -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.free),
                option = option
            )
        }

        DiscoverOption.Monetization.Rent -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.rent),
                option = option
            )
        }

        DiscoverOption.Monetization.Flatrate -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.flatrate),
                option = option
            )
        }
    }
}

private fun asCertificationUiFilterItem(option: DiscoverOption.Certification): FilterItem.Static {
    return when (option) {
        DiscoverOption.Certification.A -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.cert_a),
                option = option
            )
        }

        DiscoverOption.Certification.U -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.cert_u),
                option = option
            )
        }

        DiscoverOption.Certification.UA -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.cert_ua),
                option = option
            )
        }
    }
}

private fun asReleaseTypeUiFilterItem(option: DiscoverOption.ReleaseType): FilterItem.Static {
    return when (option) {
        DiscoverOption.ReleaseType.Digital -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.digital),
                option = option
            )
        }

        DiscoverOption.ReleaseType.Physical -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.physical),
                option = option
            )
        }

        DiscoverOption.ReleaseType.Premiere -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.premiere),
                option = option
            )
        }

        DiscoverOption.ReleaseType.Theatrical -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.theatrical),
                option = option
            )
        }

        DiscoverOption.ReleaseType.TheatricalLimited -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.theatrical_limited),
                option = option
            )
        }

        DiscoverOption.ReleaseType.Tv -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.tv),
                option = option
            )
        }
    }
}

private fun asStatusUiFilterItem(option: DiscoverOption.Status): FilterItem.Static {
    val textRes = when (option.statusId) {
        0 -> R.string.returning_series
        1 -> R.string.planned
        2 -> R.string.in_production
        3 -> R.string.ended
        4 -> R.string.canceled
        5 -> R.string.pilot
        else -> null
    }
    return FilterItem.Static(
        text = if (textRes != null) UiText.StaticText(resId = textRes) else UiText.DynamicText(text = option.statusId.toString()),
        option = option
    )
}

private fun asTvTypeUiFilterItem(option: DiscoverOption.TvType): FilterItem.Static {
    val textRes = when (option.typeId) {
        0 -> R.string.documentary
        1 -> R.string.news
        2 -> R.string.miniseries
        3 -> R.string.reality
        4 -> R.string.scripted
        5 -> R.string.talk_show
        6 -> R.string.video
        else -> null
    }
    return FilterItem.Static(
        text = if (textRes != null) UiText.StaticText(resId = textRes) else UiText.DynamicText(text = option.typeId.toString()),
        option = option
    )
}

private fun asIncludeAdultUiFilterItem(option: DiscoverOption.IncludeAdult): FilterItem.Static {
    val textRes = if (option.include) R.string.yes else R.string.no
    return FilterItem.Static(
        text = UiText.StaticText(resId = textRes),
        option = option
    )
}

private fun asIncludeVideoUiFilterItem(option: DiscoverOption.IncludeVideo): FilterItem.Static {
    val textRes = if (option.include) R.string.yes else R.string.no
    return FilterItem.Static(
        text = UiText.StaticText(resId = textRes),
        option = option
    )
}

private fun asVoteCountUiFilterItem(option: DiscoverOption.Rating.VoteCount): FilterItem.Static {
    val textRes = when (option.voteCount) {
        0 -> R.string.vote_count_0
        100 -> R.string.vote_count_100
        500 -> R.string.vote_count_500
        1000 -> R.string.vote_count_1000
        5000 -> R.string.vote_count_5000
        else -> null
    }
    return FilterItem.Static(
        text = if (textRes != null) UiText.StaticText(resId = textRes) else UiText.DynamicText(text = "${option.voteCount}+"),
        option = option
    )
}

private fun asSortByUiFilterItem(option: SortBy): FilterItem.Static {
    val sortNameRes = when (option) {
        is SortBy.Popularity -> R.string.popularity
        is SortBy.ReleaseDate -> R.string.release_date
        is SortBy.Revenue -> R.string.revenue
        is SortBy.Title -> R.string.title
        is SortBy.Rating -> R.string.rating
        is SortBy.Vote -> R.string.vote_count
        is SortBy.AirDate -> R.string.air_date
        SortBy.None -> R.string.none
    }

    val orderNameRes = when (option.order) {
        Order.Ascending -> R.string.ascending
        Order.Descending -> R.string.descending
    }

    return FilterItem.Static(
        text = if (option == SortBy.None) {
            UiText.StaticText(resId = sortNameRes)
        } else {
            UiText.StaticText(
                resId = R.string.sort_by_value,
                UiText.StaticText(resId = sortNameRes),
                UiText.StaticText(resId = orderNameRes)
            )
        },
        option = option
    )
}
