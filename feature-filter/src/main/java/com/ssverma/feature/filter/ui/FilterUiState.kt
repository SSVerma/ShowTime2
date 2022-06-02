package com.ssverma.feature.filter.ui

import com.ssverma.core.domain.DiscoverOption
import com.ssverma.core.ui.MultiSelectableState
import com.ssverma.core.ui.SingleSelectableState
import com.ssverma.core.ui.UiText
import com.ssverma.feature.filter.R
import com.ssverma.feature.filter.domain.DynamicFilterItem
import com.ssverma.feature.filter.domain.Filter
import com.ssverma.feature.filter.domain.FilterId
import java.time.LocalDate

data class FilterUiState(
    val filters: List<FilterGroup>
)

data class FilterGroup(
    val groupId: FilterId,
    val title: UiText,
    val groupContent: FilterGroupContentType
)

sealed interface FilterGroupContentType {
    sealed interface ListType : FilterGroupContentType {
        data class SingleSelectableListType(
            val horizontal: Boolean,
            val defaultSelectedItem: FilterItem? = null,
            val items: List<FilterItem>,
            val selectionState: SingleSelectableState<FilterItem> = SingleSelectableState(
                defaultSelectedItem
            )
        ) : ListType

        data class MultiSelectableListType(
            val horizontal: Boolean,
            val defaultSelectedItems: Set<FilterItem> = emptySet(),
            val items: List<FilterItem>,
            val selectionState: MultiSelectableState<FilterItem> = MultiSelectableState(
                defaultSelectedItems
            )
        ) : ListType
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
                override val state: RangeState<Int> = RangeState(from = defaultMin, to = defaultMax)
            ) : ScaleRangeType<Int>
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
            ) : PickerRangeType<LocalDate>
        }
    }
}

sealed interface FilterItem {
    val text: UiText

    data class Static(
        val discoverOption: DiscoverOption,
        override val text: UiText
    ) : FilterItem

    data class Dynamic(
        val id: String,
        override val text: UiText
    ) : FilterItem
}

fun List<Filter>.asUiFilters(): List<FilterGroup> {
    val result = mutableListOf<FilterGroup>()

    this.forEach { filter ->
        when (filter) {
            is Filter.CollectionFilter.Static -> {
                when (filter.id) {
                    is FilterId.CollectionTypeId.Static.Availability -> {
                        val filterGroupAvailability = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.availability),
                            groupContent = FilterGroupContentType.ListType.SingleSelectableListType(
                                horizontal = true,
                                items = filter.items.map {
                                    (it.option as DiscoverOption.Monetization).asUiFilterItem()
                                }
                            )
                        )
                        result.add(filterGroupAvailability)
                    }
                    is FilterId.CollectionTypeId.Static.Certification -> {
                        val filterGroupCertification = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.certification),
                            groupContent = FilterGroupContentType.ListType.SingleSelectableListType(
                                horizontal = true,
                                items = filter.items.map {
                                    (it.option as DiscoverOption.Certification).asUiFilterItem()
                                }
                            )
                        )
                        result.add(filterGroupCertification)
                    }
                    is FilterId.CollectionTypeId.Static.ReleaseType -> {
                        val filterGroupReleaseType = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.release_type),
                            groupContent = FilterGroupContentType.ListType.SingleSelectableListType(
                                horizontal = true,
                                items = filter.items.map {
                                    (it.option as DiscoverOption.ReleaseType).asUiFilterItem()
                                }
                            )
                        )
                        result.add(filterGroupReleaseType)
                    }
                }
            }
            is Filter.CollectionFilter.Dynamic -> {
                when (filter.id) {
                    is FilterId.CollectionTypeId.Dynamic.Country -> {
                        val filterGroupCountry = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.country),
                            groupContent = filter.items.asUiFilterGroup(filter.singleSelectable)
                        )
                        result.add(filterGroupCountry)
                    }
                    FilterId.CollectionTypeId.Dynamic.Genre -> {
                        val filterGroupGenre = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.genres),
                            groupContent = filter.items.asUiFilterGroup(filter.singleSelectable)
                        )
                        result.add(filterGroupGenre)
                    }
                    FilterId.CollectionTypeId.Dynamic.Keyword -> {
                        val filterGroupKeyword = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.keywords),
                            groupContent = filter.items.asUiFilterGroup(filter.singleSelectable)
                        )
                        result.add(filterGroupKeyword)
                    }
                    FilterId.CollectionTypeId.Dynamic.Language -> {
                        val filterGroupLanguage = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.language),
                            groupContent = filter.items.asUiFilterGroup(filter.singleSelectable)
                        )
                        result.add(filterGroupLanguage)
                    }
                    FilterId.CollectionTypeId.Dynamic.Person -> {
                        val filterGroupPerson = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.people),
                            groupContent = filter.items.asUiFilterGroup(filter.singleSelectable)
                        )
                        result.add(filterGroupPerson)
                    }
                }
            }
            is Filter.RangeFilter.IntRangeFilter -> {
                when (filter.id) {
                    FilterId.RangeTypeId.NumberRange.Rating -> {
                        val filterGroupRating = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.rating),
                            groupContent = FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType(
                                min = filter.from,
                                max = filter.to,
                                primaryGap = 5,
                                secondaryGap = 1
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
                            groupContent = FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType(
                                min = filter.from,
                                max = filter.to,
                            )
                        )
                        result.add(filterGroupRating)
                    }
                    FilterId.RangeTypeId.DateRange.ReleaseDate -> {
                        val filterGroupReleaseDate = FilterGroup(
                            groupId = filter.id,
                            title = UiText.StaticText(resId = R.string.release_date),
                            groupContent = FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType(
                                min = filter.from,
                                max = filter.to,
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
    singleSelectable: Boolean,
    horizontal: Boolean = true
): FilterGroupContentType {
    return if (singleSelectable) {
        FilterGroupContentType.ListType.SingleSelectableListType(
            horizontal = horizontal,
            items = this.map {
                FilterItem.Dynamic(
                    text = UiText.DynamicText(text = it.displayText),
                    id = it.id
                )
            },
            defaultSelectedItem = null
        )
    } else {
        FilterGroupContentType.ListType.MultiSelectableListType(
            horizontal = horizontal,
            items = this.map {
                FilterItem.Dynamic(
                    text = UiText.DynamicText(text = it.displayText),
                    id = it.id
                )
            },
            defaultSelectedItems = emptySet()
        )
    }
}

private fun DiscoverOption.Monetization.asUiFilterItem(): FilterItem.Static {
    return when (this) {
        DiscoverOption.Monetization.Ads -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.ads),
                discoverOption = this
            )
        }
        DiscoverOption.Monetization.Buy -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.buy),
                discoverOption = this
            )
        }
        DiscoverOption.Monetization.Free -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.free),
                discoverOption = this
            )
        }
        DiscoverOption.Monetization.Rent -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.rent),
                discoverOption = this
            )
        }
        DiscoverOption.Monetization.Stream -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.stream),
                discoverOption = this
            )
        }
    }
}

private fun DiscoverOption.Certification.asUiFilterItem(): FilterItem.Static {
    return when (this) {
        DiscoverOption.Certification.A -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.cert_a),
                discoverOption = this
            )
        }
        DiscoverOption.Certification.U -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.cert_u),
                discoverOption = this
            )
        }
        DiscoverOption.Certification.UA -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.cert_ua),
                discoverOption = this
            )
        }
    }
}

private fun DiscoverOption.ReleaseType.asUiFilterItem(): FilterItem.Static {
    return when (this) {
        DiscoverOption.ReleaseType.Digital -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.digital),
                discoverOption = this
            )
        }
        DiscoverOption.ReleaseType.Physical -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.physical),
                discoverOption = this
            )
        }
        DiscoverOption.ReleaseType.Premiere -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.premiere),
                discoverOption = this
            )
        }
        DiscoverOption.ReleaseType.Theatrical -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.theatrical),
                discoverOption = this
            )
        }
        DiscoverOption.ReleaseType.TheatricalLimited -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.theatrical_limited),
                discoverOption = this
            )
        }
        DiscoverOption.ReleaseType.Tv -> {
            FilterItem.Static(
                text = UiText.StaticText(resId = R.string.tv),
                discoverOption = this
            )
        }
    }
}