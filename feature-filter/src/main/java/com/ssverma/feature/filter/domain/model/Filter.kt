package com.ssverma.feature.filter.domain.model

import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.SortBy
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

    sealed interface SelectionFilter : Filter {
        data class Single(
            override val id: FilterId.SelectionTypeId,
            val items: List<StaticFilterItem>,
        ) : SelectionFilter
    }
}

data class DynamicFilterItem(
    val id: String,
    val displayText: String,
    val iconUrl: String? = null
)

sealed interface FilterPayload {
    data class Option(val discoverOption: DiscoverOption) : FilterPayload
    data class Sort(val sortBy: SortBy) : FilterPayload
}

data class StaticFilterItem(
    val payload: FilterPayload
)

sealed interface FilterId {
    val isRemoteSearchSupported: Boolean get() = false

    sealed interface CollectionTypeId : FilterId {
        sealed interface Static : CollectionTypeId {
            object Availability : Static
            object ReleaseType : Static
            object Certification : Static
            object Status : Static
            object Type : Static
            object IncludeAdult : Static
            object IncludeVideo : Static
            object VoteCount : Static
        }

        sealed interface Dynamic : CollectionTypeId {
            object Language : Dynamic
            object Country : Dynamic
            object Person : Dynamic {
                override val isRemoteSearchSupported = true
            }

            object Cast : Dynamic {
                override val isRemoteSearchSupported = true
            }

            object Crew : Dynamic {
                override val isRemoteSearchSupported = true
            }

            object Genre : Dynamic
            object WithoutGenre : Dynamic
            object Keyword : Dynamic {
                override val isRemoteSearchSupported = true
            }

            object WithoutKeyword : Dynamic {
                override val isRemoteSearchSupported = true
            }

            object Company : Dynamic {
                override val isRemoteSearchSupported = true
            }

            object WithoutCompany : Dynamic {
                override val isRemoteSearchSupported = true
            }

            object Network : Dynamic {
                override val isRemoteSearchSupported = true
            }

            object WatchProviders : Dynamic
        }
    }

    sealed interface SelectionTypeId : FilterId {
        object SortBy : SelectionTypeId
        object SortOrder : SelectionTypeId
    }

    sealed interface RangeTypeId : FilterId {
        sealed interface DateRange : RangeTypeId {
            object ReleaseDate : DateRange
            object AirDate : DateRange
        }

        sealed interface NumberRange : RangeTypeId {
            object Rating : NumberRange
            object Runtime : NumberRange
            object VoteAvg : NumberRange
        }
    }
}
