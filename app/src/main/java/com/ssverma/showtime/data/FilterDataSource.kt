package com.ssverma.showtime.data

import androidx.annotation.StringRes
import com.ssverma.showtime.R
import com.ssverma.showtime.api.TmdbApiTiedConstants
import com.ssverma.showtime.domain.model.DateRangeFilter
import com.ssverma.showtime.domain.model.LabelFilter
import com.ssverma.showtime.domain.model.NumberRangeFilter
import com.ssverma.showtime.domain.model.StaticLabelFilter
import com.ssverma.showtime.ui.Range
import java.time.LocalDate

//Template for similar type filters -> Filter Group
sealed class FilterGroupId(
    open val groupQueryKey: String? = null
) {
    sealed class ListGroupId(
        val queryKey: String,
        override val groupQueryKey: String? = null
    ) : FilterGroupId(groupQueryKey = groupQueryKey) {
        object Availability :
            ListGroupId(
                queryKey = TmdbApiTiedConstants.AvailableDiscoverOptions.withMonetizationType,
                groupQueryKey = TmdbApiTiedConstants.AvailableDiscoverOptions.watchRegion
            )

        object ReleaseType : ListGroupId(TmdbApiTiedConstants.AvailableDiscoverOptions.releaseType)
        object ReleaseCountry : ListGroupId(TmdbApiTiedConstants.AvailableDiscoverOptions.country)
        object Genre : ListGroupId(TmdbApiTiedConstants.AvailableDiscoverOptions.withGenres)
        object Language : ListGroupId(TmdbApiTiedConstants.AvailableDiscoverOptions.language)
        object Keyword : ListGroupId(TmdbApiTiedConstants.AvailableDiscoverOptions.withKeywords)
        object Certification :
            ListGroupId(TmdbApiTiedConstants.AvailableDiscoverOptions.certification)
    }

    sealed class RangeGroupId(
        val fromQueryKey: String,
        val toQueryKey: String,
    ) : FilterGroupId() {
        object Rating : RangeGroupId(
            fromQueryKey = TmdbApiTiedConstants.AvailableDiscoverOptions.voteAvgGte,
            toQueryKey = TmdbApiTiedConstants.AvailableDiscoverOptions.voteAvgLte,
        )

        object Runtime : RangeGroupId(
            fromQueryKey = TmdbApiTiedConstants.AvailableDiscoverOptions.runtimeGte,
            toQueryKey = TmdbApiTiedConstants.AvailableDiscoverOptions.runtimeLte,
        )

        object ReleaseDate : RangeGroupId(
            fromQueryKey = TmdbApiTiedConstants.AvailableDiscoverOptions.primaryReleaseDateGte,
            toQueryKey = TmdbApiTiedConstants.AvailableDiscoverOptions.primaryReleaseDateLte,
        )
    }
}

sealed class FilterGroup(
    @StringRes open val titleRes: Int,
    open val static: Boolean,
    open val groupId: FilterGroupId,
    var groupFilterValue: String? = null
) {

    sealed class ListGroup(
        override val groupId: FilterGroupId.ListGroupId,
        @StringRes override val titleRes: Int,
        override val static: Boolean,
        open val filters: List<LabelFilter>
    ) : FilterGroup(groupId = groupId, static = static, titleRes = titleRes) {
        data class SingleSelectableGroup(
            override val groupId: FilterGroupId.ListGroupId,
            @StringRes override val titleRes: Int,
            override val static: Boolean,
            override val filters: List<LabelFilter>,
            val default: LabelFilter? = null
        ) : ListGroup(groupId = groupId, static = static, titleRes = titleRes, filters = filters)

        data class MultiSelectableGroup(
            override val groupId: FilterGroupId.ListGroupId,
            @StringRes override val titleRes: Int,
            override val static: Boolean,
            override val filters: List<LabelFilter>,
            val default: Set<LabelFilter> = emptySet()
        ) : ListGroup(groupId = groupId, static = static, titleRes = titleRes, filters = filters)
    }

    sealed class RangeGroup(
        override val groupId: FilterGroupId.RangeGroupId,
        @StringRes override val titleRes: Int,
        override val static: Boolean
    ) : FilterGroup(groupId = groupId, static = static, titleRes = titleRes) {

        data class NumberRangeGroup(
            override val groupId: FilterGroupId.RangeGroupId,
            @StringRes override val titleRes: Int,
            override val static: Boolean,
            val rangeFilter: NumberRangeFilter,
            val default: Range<Number>? = null
        ) : RangeGroup(groupId = groupId, static = static, titleRes = titleRes)

        data class DateRangeGroup(
            override val groupId: FilterGroupId.RangeGroupId,
            @StringRes override val titleRes: Int,
            override val static: Boolean,
            val rangeFilter: DateRangeFilter,
            val default: Range<LocalDate>? = null
        ) : RangeGroup(groupId = groupId, static = static, titleRes = titleRes)
    }
}

fun coreFilterGroups(): List<FilterGroup> {
    return listOf(
        FilterGroup.ListGroup.SingleSelectableGroup(
            groupId = FilterGroupId.ListGroupId.Availability,
            titleRes = R.string.availability,
            static = true,
            filters = CoreFilters.availabilityFilters
        ),
        FilterGroup.RangeGroup.NumberRangeGroup(
            groupId = FilterGroupId.RangeGroupId.Rating,
            titleRes = R.string.rating,
            rangeFilter = CoreFilters.ratingFilter,
            static = true
        ),
        FilterGroup.RangeGroup.NumberRangeGroup(
            groupId = FilterGroupId.RangeGroupId.Runtime,
            titleRes = R.string.runtime,
            rangeFilter = CoreFilters.runtimeFilter,
            static = true
        ),
//        FilterGroup.RangeGroup.DateRangeGroup(
//            groupId = FilterGroupId.RangeGroupId.ReleaseDate,
//            titleRes = R.string.release_date,
//            static = true,
//            rangeFilter = CoreFilters.releaseDateFilter
//        )
    )
}

fun movieFilterGroups(): List<FilterGroup> {
    val coreFilterGroups = coreFilterGroups()
    val movieOnlyFilters = listOf(
        FilterGroup.ListGroup.MultiSelectableGroup(
            groupId = FilterGroupId.ListGroupId.Certification,
            titleRes = R.string.certification,
            static = true,
            filters = MovieFilters.certificationsFilter
        ),
        FilterGroup.ListGroup.MultiSelectableGroup(
            groupId = FilterGroupId.ListGroupId.ReleaseType,
            titleRes = R.string.release_type,
            static = true,
            filters = MovieFilters.releaseTypeFilters
        )
    )

    return coreFilterGroups + movieOnlyFilters
}

fun tvFilterGroups(): List<FilterGroup> {
    val coreFilterGroups = coreFilterGroups()
    val tvOnlyFilters = listOf<FilterGroup>(
        //TODO
    )

    return coreFilterGroups + tvOnlyFilters
}


object CoreFilters {
    val availabilityFilters = listOf(
        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableMonetizationTypes.Free,
            displayValueRes = R.string.free
        ),
        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableMonetizationTypes.Stream,
            displayValueRes = R.string.stream
        ),
        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableMonetizationTypes.Ads,
            displayValueRes = R.string.ads
        ),
        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableMonetizationTypes.Rent,
            displayValueRes = R.string.rent
        ),
        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableMonetizationTypes.Buy,
            displayValueRes = R.string.buy
        )
    )

    val ratingFilter = NumberRangeFilter(
        min = 0,
        max = 10,
        secondaryGap = 1,
        primaryGap = 5
    )

    val runtimeFilter = NumberRangeFilter(
        min = 0,
        max = 360,
        secondaryGap = 15,
        primaryGap = 120
    )

    val releaseDateFilter = DateRangeFilter()
}

object MovieFilters {
    val certificationsFilter = listOf(
        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableCertificationTypes.U,
            displayValueRes = R.string.cert_u
        ),
        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableCertificationTypes.UA,
            displayValueRes = R.string.cert_ua
        ),
        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableCertificationTypes.A,
            displayValueRes = R.string.cert_a
        )
    )

    val releaseTypeFilters = listOf(
        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableReleaseTypes.Premiere.toString(),
            displayValueRes = R.string.premiere
        ),

        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableReleaseTypes.TheatricalLimited.toString(),
            displayValueRes = R.string.theatrical_limited
        ),

        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableReleaseTypes.Theatrical.toString(),
            displayValueRes = R.string.theatrical
        ),

        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableReleaseTypes.Digital.toString(),
            displayValueRes = R.string.digital
        ),

        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableReleaseTypes.Physical.toString(),
            displayValueRes = R.string.physical
        ),

        StaticLabelFilter(
            filterValue = TmdbApiTiedConstants.AvailableReleaseTypes.Tv.toString(),
            displayValueRes = R.string.tv
        )
    )
}