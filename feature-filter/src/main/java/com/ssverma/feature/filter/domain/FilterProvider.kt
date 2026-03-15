package com.ssverma.feature.filter.domain

import com.ssverma.feature.filter.domain.model.DynamicFilterItem
import com.ssverma.feature.filter.domain.model.Filter
import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
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
