package com.ssverma.feature.movie.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.usecase.NoParamFlowUseCase
import com.ssverma.feature.filter.domain.Filter
import com.ssverma.feature.filter.domain.FilterId
import com.ssverma.feature.filter.domain.StaticFilterItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MovieFilterUseCase @Inject constructor(
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
            ),
            Filter.CollectionFilter.Static(
                id = FilterId.CollectionTypeId.Static.Certification,
                items = listOf(
                    StaticFilterItem(option = DiscoverOption.Certification.A),
                    StaticFilterItem(option = DiscoverOption.Certification.U),
                    StaticFilterItem(option = DiscoverOption.Certification.UA)
                )
            ),
            Filter.CollectionFilter.Static(
                id = FilterId.CollectionTypeId.Static.ReleaseType,
                items = listOf(
                    StaticFilterItem(option = DiscoverOption.ReleaseType.Premiere),
                    StaticFilterItem(option = DiscoverOption.ReleaseType.Digital),
                    StaticFilterItem(option = DiscoverOption.ReleaseType.Theatrical),
                    StaticFilterItem(option = DiscoverOption.ReleaseType.TheatricalLimited),
                    StaticFilterItem(option = DiscoverOption.ReleaseType.Tv),
                    StaticFilterItem(option = DiscoverOption.ReleaseType.Physical),
                )
            )
        )
    }
}