package com.ssverma.shared.domain.filter

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.filter.DynamicFilterItem
import com.ssverma.shared.domain.model.filter.Filter
import com.ssverma.shared.domain.model.filter.FilterId
import kotlinx.coroutines.flow.Flow
import javax.inject.Qualifier

interface FilterProvider {
    fun provideFilters(): Flow<Result<List<Filter>, Failure.CoreFailure>>

    suspend fun searchFilterItems(
        groupId: FilterId,
        query: String
    ): Result<List<DynamicFilterItem>, Failure.CoreFailure> {
        return Result.Success(emptyList())
    }

    suspend fun fetchFilterOptions(
        groupId: FilterId
    ): Result<List<DynamicFilterItem>, Failure.CoreFailure> {
        return Result.Success(emptyList())
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MovieFilter

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TvFilter
