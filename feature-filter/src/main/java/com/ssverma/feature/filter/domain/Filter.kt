package com.ssverma.feature.filter.domain

import com.ssverma.core.domain.DiscoverOption
import java.time.LocalDate

sealed interface Filter {
    val id: FilterId

    sealed interface CollectionFilter : Filter {
        data class Static(
            val items: List<StaticFilterItem>,
            val singleSelectable: Boolean = true,
            override val id: FilterId.CollectionTypeId.Static
        ) : CollectionFilter

        data class Dynamic(
            val items: List<DynamicFilterItem>,
            val singleSelectable: Boolean = true,
            override val id: FilterId.CollectionTypeId.Dynamic
        ) : CollectionFilter
    }

    sealed interface RangeFilter<T> : Filter {
        val from: T
        val to: T
        override val id: FilterId.RangeTypeId

        data class IntRangeFilter(
            override val id: FilterId.RangeTypeId.NumberRange,
            override val from: Int,
            override val to: Int,
        ) : RangeFilter<Int>

        data class DateRangeFilter(
            override val id: FilterId.RangeTypeId.DateRange,
            override val from: LocalDate,
            override val to: LocalDate,
        ) : RangeFilter<LocalDate>
    }
}

data class DynamicFilterItem(
    val id: String,
    val displayText: String
)

data class StaticFilterItem(
    val option: DiscoverOption
)

sealed interface FilterId {
    sealed interface CollectionTypeId : FilterId {
        sealed interface Static : CollectionTypeId {
            object Availability : Static
            object ReleaseType : Static
            object Certification : Static
        }

        sealed interface Dynamic : CollectionTypeId {
            object Language : Dynamic
            object Country : Dynamic
            object Person : Dynamic
            object Genre : Dynamic
            object Keyword : Dynamic
        }
    }

    sealed interface RangeTypeId : FilterId {
        sealed interface DateRange : RangeTypeId {
            object ReleaseDate : DateRange
            object AirDate : DateRange
        }

        sealed interface NumberRange : RangeTypeId {
            object Rating : NumberRange
        }
    }
}