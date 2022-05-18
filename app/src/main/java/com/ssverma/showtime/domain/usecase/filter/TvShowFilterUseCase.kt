package com.ssverma.showtime.domain.usecase.filter

import com.ssverma.core.domain.Result
import com.ssverma.core.domain.failure.Failure
import com.ssverma.core.domain.usecase.NoParamFlowUseCase
import com.ssverma.showtime.di.DefaultDispatcher
import com.ssverma.showtime.domain.DiscoverOption
import com.ssverma.showtime.domain.Filter
import com.ssverma.showtime.domain.FilterId
import com.ssverma.showtime.domain.StaticFilterItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TvShowFilterUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher
) : NoParamFlowUseCase<Result<List<Filter>, Failure.CoreFailure>>(
    coroutineDispatcher
) {
    override fun execute(): Flow<Result<List<Filter>, Failure.CoreFailure>> {
        val result = mutableListOf<Filter>()

        val staticFilters = prepareStaticFilters()
        result.addAll(staticFilters)

        val ratingFilter = Filter.RangeFilter.IntRangeFilter(
            id = FilterId.RangeTypeId.NumberRange.Rating,
            from = 0,
            to = 10,
        )

        result.add(ratingFilter)

        return flow {
            emit(Result.Success(data = result))
        }
    }

    private fun prepareStaticFilters(): List<Filter.CollectionFilter.Static> {
        return listOf(
            Filter.CollectionFilter.Static(
                id = FilterId.CollectionTypeId.Static.Availability,
                items = listOf(
                    StaticFilterItem(option = DiscoverOption.Monetization.Ads),
                    StaticFilterItem(option = DiscoverOption.Monetization.Buy),
                    StaticFilterItem(option = DiscoverOption.Monetization.Free),
                    StaticFilterItem(option = DiscoverOption.Monetization.Stream),
                    StaticFilterItem(option = DiscoverOption.Monetization.Rent)
                )
            )
        )
    }
}