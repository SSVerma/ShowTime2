package com.ssverma.feature.filter.domain.usecase

import com.ssverma.core.di.DefaultDispatcher
import com.ssverma.feature.filter.domain.FilterProvider
import com.ssverma.feature.filter.domain.model.DynamicFilterItem
import com.ssverma.feature.filter.domain.model.Filter
import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.feature.filter.domain.model.FilterPayload
import com.ssverma.feature.filter.domain.model.StaticFilterItem
import com.ssverma.feature.filter.domain.repository.FilterRepository
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.repository.WatchProviderRepository
import com.ssverma.shared.domain.usecase.NoParamFlowUseCase
import com.ssverma.shared.domain.utils.DateUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

enum class FilterType {
    Movie,
    Tv
}

class GetFiltersUseCase @Inject constructor(
    @DefaultDispatcher coroutineDispatcher: CoroutineDispatcher,
    private val filterRepository: FilterRepository,
    private val watchProviderRepository: WatchProviderRepository
) : NoParamFlowUseCase<Result<List<Filter>, Failure.CoreFailure>>(coroutineDispatcher),
    FilterProvider {

    private var filterType: FilterType = FilterType.Movie

    fun setFilterType(type: FilterType) {
        this.filterType = type
    }

    override fun execute(): Flow<Result<List<Filter>, Failure.CoreFailure>> = flow {
        val filters = mutableListOf<Filter>()

        // Sorting
        filters.add(
            Filter.SelectionFilter.Single(
                id = FilterId.SelectionTypeId.SortBy,
                items = getSortByItems(type = filterType)
            )
        )

        // Watch Providers
        filters.add(
            Filter.CollectionFilter.Dynamic(
                id = FilterId.CollectionTypeId.Dynamic.WatchProviders,
                items = emptyList(),
                singleSelectable = false
            )
        )

        // Static filters
        filters.addAll(getStaticFilters(type = filterType))

        // Genres
        filters.add(
            Filter.CollectionFilter.Dynamic(
                id = FilterId.CollectionTypeId.Dynamic.Genre,
                items = emptyList()
            )
        )

        // Languages
        filters.add(
            Filter.CollectionFilter.Dynamic(
                id = FilterId.CollectionTypeId.Dynamic.Language,
                items = emptyList()
            )
        )

        // Countries
        filters.add(
            Filter.CollectionFilter.Dynamic(
                id = FilterId.CollectionTypeId.Dynamic.Country,
                items = emptyList()
            )
        )

        // Vote Count (Static instead of Range)
        filters.add(
            Filter.CollectionFilter.Static(
                id = FilterId.CollectionTypeId.Static.VoteCount,
                items = listOf(
                    StaticFilterItem(
                        payload = FilterPayload.Option(
                            discoverOption = DiscoverOption.VoteCount.AtLeast(value = 0)
                        )
                    ),
                    StaticFilterItem(
                        payload = FilterPayload.Option(
                            discoverOption = DiscoverOption.VoteCount.AtLeast(value = 100)
                        )
                    ),
                    StaticFilterItem(
                        payload = FilterPayload.Option(
                            discoverOption = DiscoverOption.VoteCount.AtLeast(value = 500)
                        )
                    ),
                    StaticFilterItem(
                        payload = FilterPayload.Option(
                            discoverOption = DiscoverOption.VoteCount.AtLeast(value = 1000)
                        )
                    ),
                    StaticFilterItem(
                        payload = FilterPayload.Option(
                            discoverOption = DiscoverOption.VoteCount.AtLeast(value = 5000)
                        )
                    ),
                )
            )
        )

        if (filterType == FilterType.Movie) {
            // Release Date
            filters.add(
                Filter.RangeFilter.DateRangeFilter(
                    id = FilterId.RangeTypeId.DateRange.ReleaseDate,
                    from = DateUtils.currentDate(),
                    to = DateUtils.currentDate(),
                )
            )

            // Runtime
            filters.add(
                Filter.RangeFilter.IntRangeFilter(
                    id = FilterId.RangeTypeId.NumberRange.Runtime,
                    from = 0,
                    to = 400, // Max runtime in minutes
                )
            )
        } else {
            // Air Date
            filters.add(
                Filter.RangeFilter.DateRangeFilter(
                    id = FilterId.RangeTypeId.DateRange.AirDate,
                    from = DateUtils.currentDate(),
                    to = DateUtils.currentDate(),
                )
            )
        }

        // Keywords
        filters.add(
            Filter.CollectionFilter.Dynamic(
                id = FilterId.CollectionTypeId.Dynamic.Keyword,
                items = emptyList()
            )
        )

        if (filterType == FilterType.Movie) {
            // Companies
            filters.add(
                Filter.CollectionFilter.Dynamic(
                    id = FilterId.CollectionTypeId.Dynamic.Company,
                    items = emptyList()
                )
            )
        } else {
            // Networks
            filters.add(
                Filter.CollectionFilter.Dynamic(
                    id = FilterId.CollectionTypeId.Dynamic.Network,
                    items = emptyList()
                )
            )
        }

        emit(Result.Success(data = filters))
    }

    private fun getSortByItems(type: FilterType): List<StaticFilterItem> {
        return if (type == FilterType.Movie) {
            listOf(
                StaticFilterItem(payload = FilterPayload.Sort(sortBy = SortBy.Popularity())),
                StaticFilterItem(payload = FilterPayload.Sort(sortBy = SortBy.ReleaseDate())),
                StaticFilterItem(payload = FilterPayload.Sort(sortBy = SortBy.Revenue())),
                StaticFilterItem(payload = FilterPayload.Sort(sortBy = SortBy.Title())),
                StaticFilterItem(payload = FilterPayload.Sort(sortBy = SortBy.Rating())),
                StaticFilterItem(payload = FilterPayload.Sort(sortBy = SortBy.Vote()))
            )
        } else {
            listOf(
                StaticFilterItem(payload = FilterPayload.Sort(sortBy = SortBy.Popularity())),
                StaticFilterItem(payload = FilterPayload.Sort(sortBy = SortBy.AirDate())),
                StaticFilterItem(payload = FilterPayload.Sort(sortBy = SortBy.Rating())),
                StaticFilterItem(payload = FilterPayload.Sort(sortBy = SortBy.Vote()))
            )
        }
    }

    private fun getStaticFilters(type: FilterType): List<Filter.CollectionFilter.Static> {
        val common = mutableListOf<Filter.CollectionFilter.Static>()

        common.add(
            Filter.CollectionFilter.Static(
                id = FilterId.CollectionTypeId.Static.Availability,
                singleSelectable = false,
                items = listOf(
                    StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.Monetization.Ads)),
                    StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.Monetization.Buy)),
                    StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.Monetization.Free)),
                    StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.Monetization.Flatrate)),
                    StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.Monetization.Rent))
                )
            )
        )

        if (type == FilterType.Movie) {
            common.add(
                Filter.CollectionFilter.Static(
                    id = FilterId.CollectionTypeId.Static.Certification,
                    singleSelectable = false,
                    items = listOf(
                        StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.Certification.A)),
                        StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.Certification.U)),
                        StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.Certification.UA))
                    )
                )
            )
            common.add(
                Filter.CollectionFilter.Static(
                    id = FilterId.CollectionTypeId.Static.ReleaseType,
                    singleSelectable = false,
                    items = listOf(
                        StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.ReleaseType.Premiere)),
                        StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.ReleaseType.Digital)),
                        StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.ReleaseType.Theatrical)),
                        StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.ReleaseType.TheatricalLimited)),
                        StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.ReleaseType.Tv)),
                        StaticFilterItem(payload = FilterPayload.Option(discoverOption = DiscoverOption.ReleaseType.Physical)),
                    )
                )
            )
            common.add(
                Filter.CollectionFilter.Static(
                    id = FilterId.CollectionTypeId.Static.IncludeVideo,
                    items = listOf(
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.IncludeVideo(
                                    include = true
                                )
                            )
                        ),
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.IncludeVideo(
                                    include = false
                                )
                            )
                        )
                    )
                )
            )
        } else {
            common.add(
                Filter.CollectionFilter.Static(
                    id = FilterId.CollectionTypeId.Static.Status,
                    items = listOf(
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.Status(
                                    statusId = 0
                                )
                            )
                        ), // Returning Series
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.Status(
                                    statusId = 1
                                )
                            )
                        ), // Planned
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.Status(
                                    statusId = 2
                                )
                            )
                        ), // In Production
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.Status(
                                    statusId = 3
                                )
                            )
                        ), // Ended
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.Status(
                                    statusId = 4
                                )
                            )
                        ), // Canceled
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.Status(
                                    statusId = 5
                                )
                            )
                        ), // Pilot
                    )
                )
            )
            common.add(
                Filter.CollectionFilter.Static(
                    id = FilterId.CollectionTypeId.Static.Type,
                    items = listOf(
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.TvType(
                                    typeId = 0
                                )
                            )
                        ), // Documentary
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.TvType(
                                    typeId = 1
                                )
                            )
                        ), // News
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.TvType(
                                    typeId = 2
                                )
                            )
                        ), // Miniseries
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.TvType(
                                    typeId = 3
                                )
                            )
                        ), // Reality
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.TvType(
                                    typeId = 4
                                )
                            )
                        ), // Scripted
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.TvType(
                                    typeId = 5
                                )
                            )
                        ), // Talk Show
                        StaticFilterItem(
                            payload = FilterPayload.Option(
                                discoverOption = DiscoverOption.TvType(
                                    typeId = 6
                                )
                            )
                        ), // Video
                    )
                )
            )
        }

        common.add(
            Filter.CollectionFilter.Static(
                id = FilterId.CollectionTypeId.Static.IncludeAdult,
                items = listOf(
                    StaticFilterItem(
                        payload = FilterPayload.Option(
                            discoverOption = DiscoverOption.IncludeAdult(
                                include = true
                            )
                        )
                    ),
                    StaticFilterItem(
                        payload = FilterPayload.Option(
                            discoverOption = DiscoverOption.IncludeAdult(
                                include = false
                            )
                        )
                    )
                )
            )
        )

        return common
    }

    override fun provideFilters(): Flow<Result<List<Filter>, Failure.CoreFailure>> {
        return invoke()
    }

    override suspend fun searchFilterItems(
        groupId: FilterId,
        query: String
    ): Result<List<DynamicFilterItem>, Failure.CoreFailure> {
        return when (groupId) {
            FilterId.CollectionTypeId.Dynamic.Keyword,
            FilterId.CollectionTypeId.Dynamic.WithoutKeyword -> {
                filterRepository.searchKeywords(query = query).asSuccess { keywords ->
                    keywords.map {
                        DynamicFilterItem(
                            id = it.id.toString(),
                            displayText = it.name.orEmpty()
                        )
                    }
                }
            }

            FilterId.CollectionTypeId.Dynamic.Company,
            FilterId.CollectionTypeId.Dynamic.WithoutCompany -> {
                filterRepository.searchCompanies(query = query).asSuccess { companies ->
                    companies.map {
                        DynamicFilterItem(
                            id = it.id.toString(),
                            displayText = it.name
                        )
                    }
                }
            }

            FilterId.CollectionTypeId.Dynamic.Network -> {
                filterRepository.searchNetworks(query = query).asSuccess { networks ->
                    networks.map { DynamicFilterItem(id = it.id.toString(), displayText = it.name) }
                }
            }

            else -> Result.Success(emptyList())
        }
    }

    override suspend fun fetchFilterOptions(groupId: FilterId): Result<List<DynamicFilterItem>, Failure.CoreFailure> {
        return when (groupId) {
            FilterId.CollectionTypeId.Dynamic.Genre -> {
                val result = if (filterType == FilterType.Movie) {
                    filterRepository.fetchMovieGenres()
                } else {
                    filterRepository.fetchTvGenres()
                }
                result.asSuccess { genres ->
                    genres.map { DynamicFilterItem(id = it.id.toString(), displayText = it.name) }
                }
            }

            FilterId.CollectionTypeId.Dynamic.Language -> {
                filterRepository.fetchLanguages().asSuccess { languages ->
                    languages
                        .sortedBy { it.englishName }
                        .map { DynamicFilterItem(id = it.iso6391, displayText = it.englishName) }
                }
            }

            FilterId.CollectionTypeId.Dynamic.Country -> {
                filterRepository.fetchCountries().asSuccess { countries ->
                    countries
                        .sortedBy { it.englishName }
                        .map { DynamicFilterItem(id = it.iso31661, displayText = it.englishName) }
                }
            }

            FilterId.CollectionTypeId.Dynamic.WatchProviders -> {
                val result = if (filterType == FilterType.Movie) {
                    watchProviderRepository.fetchAllMovieWatchProviders()
                } else {
                    watchProviderRepository.fetchAllTvShowWatchProviders()
                }
                result.asSuccess { providers ->
                    providers.map {
                        DynamicFilterItem(
                            id = it.providerId.toString(),
                            displayText = it.providerName,
                            iconUrl = it.logoPath
                        )
                    }
                }
            }

            else -> Result.Success(emptyList())
        }
    }
}