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
import com.ssverma.core.ui.UiText
import com.ssverma.feature.filter.R
import com.ssverma.feature.filter.domain.model.DynamicFilterItem
import com.ssverma.feature.filter.domain.model.Filter
import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.feature.filter.domain.model.FilterPayload
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.SortBy
import java.time.LocalDate

// 1. UI CONFIGURATION REGISTRY (OCP Compliant)

data class FilterUiConfig(
    val titleRes: Int,
    val icon: ImageVector? = null,
    val defaultDisplayMode: ListDisplayMode = ListDisplayMode.HorizontalRow
)

val FilterUiRegistry: Map<FilterId, FilterUiConfig> = mapOf(
    // Static Collections
    FilterId.CollectionTypeId.Static.Availability to FilterUiConfig(
        titleRes = R.string.availability,
        icon = Icons.Rounded.EventAvailable,
        defaultDisplayMode = ListDisplayMode.FlowRow
    ),
    FilterId.CollectionTypeId.Static.Certification to FilterUiConfig(
        titleRes = R.string.certification,
        icon = Icons.Rounded.Verified
    ),
    FilterId.CollectionTypeId.Static.ReleaseType to FilterUiConfig(
        titleRes = R.string.release_type,
        icon = Icons.Rounded.NewReleases
    ),
    FilterId.CollectionTypeId.Static.Status to FilterUiConfig(
        titleRes = R.string.status,
        icon = Icons.Rounded.Info
    ),
    FilterId.CollectionTypeId.Static.Type to FilterUiConfig(
        titleRes = R.string.type,
        icon = Icons.Rounded.Category
    ),
    FilterId.CollectionTypeId.Static.IncludeAdult to FilterUiConfig(
        titleRes = R.string.include_adult,
        icon = Icons.Rounded.Explicit
    ),
    FilterId.CollectionTypeId.Static.IncludeVideo to FilterUiConfig(
        titleRes = R.string.include_video,
        icon = Icons.Rounded.Movie
    ),
    FilterId.CollectionTypeId.Static.VoteCount to FilterUiConfig(
        titleRes = R.string.vote_count,
        icon = Icons.Rounded.HowToVote
    ),

    // Dynamic Collections
    FilterId.CollectionTypeId.Dynamic.Country to FilterUiConfig(
        titleRes = R.string.country,
        icon = Icons.Rounded.Public,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.Genre to FilterUiConfig(
        titleRes = R.string.genres,
        icon = Icons.Rounded.Movie,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.WithoutGenre to FilterUiConfig(
        titleRes = R.string.without_genres,
        icon = null,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.Keyword to FilterUiConfig(
        titleRes = R.string.keywords,
        icon = null,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.WithoutKeyword to FilterUiConfig(
        titleRes = R.string.without_keywords,
        icon = null,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.Language to FilterUiConfig(
        titleRes = R.string.language,
        icon = Icons.Rounded.Language,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.Person to FilterUiConfig(
        titleRes = R.string.people,
        icon = null,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.Cast to FilterUiConfig(
        titleRes = R.string.cast,
        icon = null,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.Crew to FilterUiConfig(
        titleRes = R.string.crew,
        icon = null,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.Company to FilterUiConfig(
        titleRes = R.string.company,
        icon = null,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.WithoutCompany to FilterUiConfig(
        titleRes = R.string.without_companies,
        icon = null,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.Network to FilterUiConfig(
        titleRes = R.string.network,
        icon = null,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.CollectionTypeId.Dynamic.WatchProviders to FilterUiConfig(
        titleRes = R.string.watch_providers,
        icon = null,
        defaultDisplayMode = ListDisplayMode.Picker
    ),

    // Selections & Ranges
    FilterId.SelectionTypeId.SortBy to FilterUiConfig(
        titleRes = R.string.sort_by,
        icon = Icons.AutoMirrored.Rounded.Sort,
        defaultDisplayMode = ListDisplayMode.Picker
    ),
    FilterId.RangeTypeId.NumberRange.Rating to FilterUiConfig(
        titleRes = R.string.rating,
        icon = Icons.Rounded.Star
    ),
    FilterId.RangeTypeId.NumberRange.VoteAvg to FilterUiConfig(
        titleRes = R.string.rating,
        icon = Icons.Rounded.Star
    ),
    FilterId.RangeTypeId.NumberRange.Runtime to FilterUiConfig(
        titleRes = R.string.runtime,
        icon = Icons.Rounded.Schedule
    ),
    FilterId.RangeTypeId.DateRange.AirDate to FilterUiConfig(
        titleRes = R.string.air_date,
        icon = Icons.Rounded.DateRange
    ),
    FilterId.RangeTypeId.DateRange.ReleaseDate to FilterUiConfig(
        titleRes = R.string.release_date,
        icon = Icons.Rounded.DateRange
    )
)

// 2. DOMAIN -> UI MAPPERS

fun List<Filter>.asUiFilters(
    initialOptions: List<DiscoverOption> = emptyList(),
    initialSortBy: SortBy? = null,
    initialOrder: Order? = null
): List<FilterGroup> {
    val result = mutableListOf<FilterGroup>()

    this.forEach { filter ->
        val config = FilterUiRegistry[filter.id] ?: return@forEach

        when (filter) {
            is Filter.CollectionFilter.Static -> {
                val group: FilterGroup? = when (filter.id) {
                    is FilterId.CollectionTypeId.Static.Availability -> createSafeStaticGroup<DiscoverOption.Monetization>(
                        filter = filter,
                        config = config,
                        initialOptions = initialOptions,
                        textMapper = ::asMonetizationText
                    )

                    is FilterId.CollectionTypeId.Static.Certification -> createSafeStaticGroup<DiscoverOption.Certification>(
                        filter = filter,
                        config = config,
                        initialOptions = initialOptions,
                        textMapper = ::asCertificationText
                    )

                    is FilterId.CollectionTypeId.Static.ReleaseType -> createSafeStaticGroup<DiscoverOption.ReleaseType>(
                        filter = filter,
                        config = config,
                        initialOptions = initialOptions,
                        textMapper = ::asReleaseTypeText
                    )

                    is FilterId.CollectionTypeId.Static.Status -> createSafeStaticGroup<DiscoverOption.Status>(
                        filter = filter,
                        config = config,
                        initialOptions = initialOptions,
                        textMapper = ::asStatusText
                    )

                    is FilterId.CollectionTypeId.Static.Type -> createSafeStaticGroup<DiscoverOption.TvType>(
                        filter = filter,
                        config = config,
                        initialOptions = initialOptions,
                        textMapper = ::asTvTypeText
                    )

                    is FilterId.CollectionTypeId.Static.IncludeAdult -> createSafeStaticGroup<DiscoverOption.IncludeAdult>(
                        filter = filter,
                        config = config,
                        initialOptions = initialOptions,
                        textMapper = ::asIncludeAdultText
                    )

                    is FilterId.CollectionTypeId.Static.IncludeVideo -> createSafeStaticGroup<DiscoverOption.IncludeVideo>(
                        filter = filter,
                        config = config,
                        initialOptions = initialOptions,
                        textMapper = ::asIncludeVideoText
                    )

                    is FilterId.CollectionTypeId.Static.VoteCount -> createSafeStaticGroup<DiscoverOption.VoteCount.AtLeast>(
                        filter = filter,
                        config = config,
                        initialOptions = initialOptions,
                        textMapper = ::asVoteCountText
                    )

                }
                group?.let { result.add(it) }
            }

            is Filter.CollectionFilter.Dynamic -> {
                result.add(
                    FilterGroup(
                        groupId = filter.id,
                        title = UiText.StaticText(resId = config.titleRes),
                        icon = config.icon,
                        groupContent = filter.items.asUiFilterGroup(
                            groupId = filter.id,
                            singleSelectable = filter.singleSelectable,
                            displayMode = config.defaultDisplayMode,
                            initialOptions = initialOptions
                        )
                    )
                )
            }

            is Filter.SelectionFilter.Single -> {
                when (filter.id) {
                    is FilterId.SelectionTypeId.SortBy -> {
                        val sortByItems = filter.items.flatMap { filterItem ->
                            val sortByOption = (filterItem.payload as? FilterPayload.Sort)?.sortBy
                                ?: return@flatMap emptyList() // Return empty list instead of null

                            if (sortByOption == SortBy.None) {
                                listOf(asSortByUiFilterItem(option = sortByOption))
                            } else {
                                listOf(
                                    asSortByUiFilterItem(option = sortByOption.withOrder(order = Order.Descending)),
                                    asSortByUiFilterItem(option = sortByOption.withOrder(order = Order.Ascending))
                                )
                            }
                        }

                        result.add(
                            FilterGroup(
                                groupId = filter.id,
                                title = UiText.StaticText(resId = config.titleRes),
                                icon = config.icon,
                                groupContent = FilterGroupContentType.ListType.SingleSelectableListType(
                                    displayMode = config.defaultDisplayMode,
                                    items = sortByItems,
                                    defaultSelectedItem = sortByItems.find { item ->
                                        val option =
                                            (item as? FilterItem.Static)?.payload as? FilterPayload.Sort
                                        option != null && initialSortBy != null &&
                                                option.sortBy::class == initialSortBy::class &&
                                                option.sortBy.order == initialOrder
                                    } ?: sortByItems.find { item ->
                                        val option =
                                            ((item as? FilterItem.Static)?.payload as? FilterPayload.Sort)?.sortBy
                                        option is SortBy.Popularity && option.order == Order.Descending
                                    }
                                )
                            )
                        )
                    }

                    is FilterId.SelectionTypeId.SortOrder -> {}
                }
            }

            is Filter.RangeFilter.IntRangeFilter -> {
                val groupContent = when (filter.id) {
                    is FilterId.RangeTypeId.NumberRange.Rating -> {
                        FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType(
                            min = filter.from,
                            max = filter.to,
                            primaryGap = 2,
                            secondaryGap = 1,
                            defaultMin = initialOptions.filterIsInstance<DiscoverOption.Rating.From>()
                                .firstOrNull()?.from,
                            defaultMax = initialOptions.filterIsInstance<DiscoverOption.Rating.To>()
                                .firstOrNull()?.to
                        )
                    }

                    is FilterId.RangeTypeId.NumberRange.Runtime -> {
                        FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType(
                            min = filter.from,
                            max = filter.to,
                            primaryGap = 60,
                            secondaryGap = 30,
                            defaultMin = initialOptions.filterIsInstance<DiscoverOption.Runtime.From>()
                                .firstOrNull()?.from,
                            defaultMax = initialOptions.filterIsInstance<DiscoverOption.Runtime.To>()
                                .firstOrNull()?.to
                        )
                    }

                    is FilterId.RangeTypeId.NumberRange.VoteAvg -> {
                        FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType(
                            min = filter.from,
                            max = filter.to,
                            primaryGap = 5,
                            secondaryGap = 1,
                            isRange = true,
                            defaultMin = null,
                            defaultMax = null
                        )
                    }
                }
                result.add(
                    FilterGroup(
                        groupId = filter.id,
                        title = UiText.StaticText(resId = config.titleRes),
                        icon = config.icon,
                        groupContent = groupContent
                    )
                )
            }

            is Filter.RangeFilter.DateRangeFilter -> {
                val groupContent = when (filter.id) {
                    is FilterId.RangeTypeId.DateRange.AirDate -> {
                        FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType(
                            min = filter.from,
                            max = filter.to,
                            defaultMin = initialOptions.filterIsInstance<DiscoverOption.AirDate.From>()
                                .firstOrNull()?.date,
                            defaultMax = initialOptions.filterIsInstance<DiscoverOption.AirDate.To>()
                                .firstOrNull()?.date
                        )
                    }

                    is FilterId.RangeTypeId.DateRange.ReleaseDate -> {
                        FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType(
                            min = filter.from,
                            max = filter.to,
                            defaultMin = initialOptions.filterIsInstance<DiscoverOption.ReleaseDate.From>()
                                .firstOrNull()?.date
                                ?: initialOptions.filterIsInstance<DiscoverOption.PrimaryReleaseDate.From>()
                                    .firstOrNull()?.date,
                            defaultMax = initialOptions.filterIsInstance<DiscoverOption.ReleaseDate.To>()
                                .firstOrNull()?.date
                                ?: initialOptions.filterIsInstance<DiscoverOption.PrimaryReleaseDate.To>()
                                    .firstOrNull()?.date
                        )
                    }
                }
                result.add(
                    FilterGroup(
                        groupId = filter.id,
                        title = UiText.StaticText(resId = config.titleRes),
                        icon = config.icon,
                        groupContent = groupContent
                    )
                )
            }
        }
    }
    return result
}

// 3. SAFE COMPONENT BUILDERS (Handles Rehydration Visuals)

inline fun <reified T : DiscoverOption> createSafeStaticGroup(
    filter: Filter.CollectionFilter.Static,
    config: FilterUiConfig,
    initialOptions: List<DiscoverOption>,
    crossinline textMapper: (T) -> UiText
): FilterGroup? {

    val safeItems = filter.items.mapNotNull { staticItem ->
        val payloadOption = (staticItem.payload as? FilterPayload.Option)?.discoverOption
        val typedOption = payloadOption as? T ?: return@mapNotNull null

        FilterItem.Static(
            payload = staticItem.payload,
            text = textMapper(typedOption)
        )
    }

    if (safeItems.isEmpty()) return null

    val isItemSelected: (FilterItem.Static) -> Boolean = { item ->
        val opt = (item.payload as? FilterPayload.Option)?.discoverOption
        initialOptions.any { option -> option == opt }
    }

    val groupContent = if (filter.singleSelectable) {
        FilterGroupContentType.ListType.SingleSelectableListType(
            displayMode = config.defaultDisplayMode,
            items = safeItems,
            defaultSelectedItem = safeItems.find(isItemSelected)
        )
    } else {
        FilterGroupContentType.ListType.MultiSelectableListType(
            displayMode = config.defaultDisplayMode,
            items = safeItems,
            defaultSelectedItems = safeItems.filter(isItemSelected).toSet()
        )
    }

    return FilterGroup(
        groupId = filter.id,
        title = UiText.StaticText(resId = config.titleRes),
        icon = config.icon,
        groupContent = groupContent
    )
}

private fun List<DynamicFilterItem>.asUiFilterGroup(
    groupId: FilterId,
    singleSelectable: Boolean,
    displayMode: ListDisplayMode = ListDisplayMode.HorizontalRow,
    initialOptions: List<DiscoverOption> = emptyList()
): FilterGroupContentType {

    val items = this.map { dynamicItem ->
        FilterItem.Dynamic(
            id = dynamicItem.id,
            text = UiText.DynamicText(text = dynamicItem.displayText),
            iconUrl = dynamicItem.iconUrl
        )
    }

    val isItemSelected: (FilterItem.Dynamic) -> Boolean = { item ->
        initialOptions.any { option ->
            isDynamicOptionMatch(groupId = groupId, itemId = item.id, option = option)
        }
    }

    return if (singleSelectable) {
        FilterGroupContentType.ListType.SingleSelectableListType(
            displayMode = displayMode,
            items = items,
            defaultSelectedItem = items.find { item -> isItemSelected(item as FilterItem.Dynamic) }
        )
    } else {
        FilterGroupContentType.ListType.MultiSelectableListType(
            displayMode = displayMode,
            items = items,
            defaultSelectedItems = items.filter { item -> isItemSelected(item as FilterItem.Dynamic) }
                .toSet()
        )
    }
}

// 4. UI -> DOMAIN MAPPERS (Extracting for the API Payload)

fun List<FilterGroup>.asDiscoverOptions(
    watchRegion: String? = null
): Pair<List<DiscoverOption>, Pair<SortBy?, Order?>> {

    val options = mutableListOf<DiscoverOption>()
    watchRegion?.let { region -> options.add(DiscoverOption.WatchRegion(iso3 = region)) }

    var sortBy: SortBy? = null
    var order: Order? = null

    this.forEach { group ->
        when (val content = group.groupContent) {
            is FilterGroupContentType.ListType.SingleSelectableListType -> {
                val selected = content.selectionState.selected() ?: return@forEach

                when(selected) {
                    is FilterItem.Dynamic -> {
                        mapDynamicOption(
                            groupId = group.groupId,
                            id = selected.id
                        )?.let { options.add(it) }
                    }
                    is FilterItem.Static -> {
                        when (val payload = selected.payload) {
                            is FilterPayload.Option -> options.add(payload.discoverOption)
                            is FilterPayload.Sort -> {
                                sortBy = payload.sortBy
                                order = payload.sortBy.order
                            }
                        }
                    }
                }
            }

            is FilterGroupContentType.ListType.MultiSelectableListType -> {
                content.selectionState.selected().forEach { selected ->
                    when(selected) {
                        is FilterItem.Dynamic -> {
                            mapDynamicOption(
                                groupId = group.groupId,
                                id = selected.id
                            )?.let { options.add(it) }
                        }
                        is FilterItem.Static -> {
                            if (selected.payload is FilterPayload.Option) {
                                options.add(selected.payload.discoverOption)
                            }
                        }
                    }
                }
            }

            is FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType -> {
                mapIntRange(
                    groupId = group.groupId,
                    from = content.state.fromValue,
                    to = content.state.toValue
                )?.let { resolvedOptions -> options.addAll(resolvedOptions) }
            }

            is FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType -> {
                mapDateRange(
                    groupId = group.groupId,
                    from = content.state.fromValue,
                    to = content.state.toValue
                )?.let { resolvedOptions -> options.addAll(resolvedOptions) }
            }
        }
    }

    return Pair(first = options, second = Pair(first = sortBy, second = order))
}

// 5. HELPER MAPPERS & EXTRACTORS

fun mapDynamicOption(groupId: FilterId, id: String): DiscoverOption? {
    return when (groupId) {
        is FilterId.CollectionTypeId.Dynamic.Genre -> DiscoverOption.Genre(genreId = id.toInt())
        is FilterId.CollectionTypeId.Dynamic.WithoutGenre -> DiscoverOption.WithoutGenre(genreId = id.toInt())
        is FilterId.CollectionTypeId.Dynamic.Language -> DiscoverOption.Language(iso3 = id)
        is FilterId.CollectionTypeId.Dynamic.Country -> DiscoverOption.Country(iso3 = id)
        is FilterId.CollectionTypeId.Dynamic.Keyword -> DiscoverOption.Keyword(keywordId = id.toInt())
        is FilterId.CollectionTypeId.Dynamic.WithoutKeyword -> DiscoverOption.WithoutKeyword(
            keywordId = id.toInt()
        )

        is FilterId.CollectionTypeId.Dynamic.Company -> DiscoverOption.Company(companyId = id.toInt())
        is FilterId.CollectionTypeId.Dynamic.WithoutCompany -> DiscoverOption.WithoutCompany(
            companyId = id.toInt()
        )

        is FilterId.CollectionTypeId.Dynamic.Network -> DiscoverOption.Network(networkId = id.toInt())
        is FilterId.CollectionTypeId.Dynamic.Person -> DiscoverOption.Person(personId = id.toInt())
        is FilterId.CollectionTypeId.Dynamic.Cast -> DiscoverOption.Cast(personId = id.toInt())
        is FilterId.CollectionTypeId.Dynamic.Crew -> DiscoverOption.Crew(personId = id.toInt())
        is FilterId.CollectionTypeId.Dynamic.WatchProviders -> DiscoverOption.WatchProvider(
            providerId = id.toInt()
        )

        else -> null
    }
}

fun isDynamicOptionMatch(groupId: FilterId, itemId: String, option: DiscoverOption): Boolean {
    return when (groupId) {
        is FilterId.CollectionTypeId.Dynamic.Genre -> option is DiscoverOption.Genre && option.genreId == itemId.toInt()
        is FilterId.CollectionTypeId.Dynamic.WithoutGenre -> option is DiscoverOption.WithoutGenre && option.genreId == itemId.toInt()
        is FilterId.CollectionTypeId.Dynamic.Language -> option is DiscoverOption.Language && option.iso3 == itemId
        is FilterId.CollectionTypeId.Dynamic.Country -> option is DiscoverOption.Country && option.iso3 == itemId
        is FilterId.CollectionTypeId.Dynamic.Keyword -> option is DiscoverOption.Keyword && option.keywordId == itemId.toInt()
        is FilterId.CollectionTypeId.Dynamic.WithoutKeyword -> option is DiscoverOption.WithoutKeyword && option.keywordId == itemId.toInt()
        is FilterId.CollectionTypeId.Dynamic.Company -> option is DiscoverOption.Company && option.companyId == itemId.toInt()
        is FilterId.CollectionTypeId.Dynamic.WithoutCompany -> option is DiscoverOption.WithoutCompany && option.companyId == itemId.toInt()
        is FilterId.CollectionTypeId.Dynamic.Network -> option is DiscoverOption.Network && option.networkId == itemId.toInt()
        is FilterId.CollectionTypeId.Dynamic.Person -> option is DiscoverOption.Person && option.personId == itemId.toInt()
        is FilterId.CollectionTypeId.Dynamic.Cast -> option is DiscoverOption.Cast && option.personId == itemId.toInt()
        is FilterId.CollectionTypeId.Dynamic.Crew -> option is DiscoverOption.Crew && option.personId == itemId.toInt()
        is FilterId.CollectionTypeId.Dynamic.WatchProviders -> option is DiscoverOption.WatchProvider && option.providerId == itemId.toInt()
        else -> false
    }
}

private fun mapIntRange(groupId: FilterId, from: Int?, to: Int?): List<DiscoverOption>? {
    val options = mutableListOf<DiscoverOption>()
    when (groupId) {
        is FilterId.RangeTypeId.NumberRange.Rating,
        is FilterId.RangeTypeId.NumberRange.VoteAvg -> {
            from?.let { value -> options.add(DiscoverOption.Rating.From(from = value)) }
            to?.let { value -> options.add(DiscoverOption.Rating.To(to = value)) }
        }

        is FilterId.RangeTypeId.NumberRange.Runtime -> {
            from?.let { value -> options.add(DiscoverOption.Runtime.From(from = value)) }
            to?.let { value -> options.add(DiscoverOption.Runtime.To(to = value)) }
        }

        else -> {}
    }
    return if (options.isEmpty()) null else options
}

private fun mapDateRange(
    groupId: FilterId,
    from: LocalDate?,
    to: LocalDate?
): List<DiscoverOption>? {
    val options = mutableListOf<DiscoverOption>()
    when (groupId) {
        is FilterId.RangeTypeId.DateRange.ReleaseDate -> {
            from?.let { value -> options.add(DiscoverOption.ReleaseDate.From(date = value)) }
            to?.let { value -> options.add(DiscoverOption.ReleaseDate.To(date = value)) }
        }

        is FilterId.RangeTypeId.DateRange.AirDate -> {
            from?.let { value -> options.add(DiscoverOption.AirDate.From(date = value)) }
            to?.let { value -> options.add(DiscoverOption.AirDate.To(date = value)) }
        }

        else -> {}
    }
    return if (options.isEmpty()) null else options
}

// 6. TEXT STRING MAPPERS

private fun asMonetizationText(option: DiscoverOption.Monetization): UiText {
    val textRes = when (option) {
        DiscoverOption.Monetization.Ads -> R.string.ads
        DiscoverOption.Monetization.Buy -> R.string.buy
        DiscoverOption.Monetization.Free -> R.string.free
        DiscoverOption.Monetization.Rent -> R.string.rent
        DiscoverOption.Monetization.Flatrate -> R.string.flatrate
    }
    return UiText.StaticText(resId = textRes)
}

private fun asCertificationText(option: DiscoverOption.Certification): UiText {
    val textRes = when (option) {
        DiscoverOption.Certification.A -> R.string.cert_a
        DiscoverOption.Certification.U -> R.string.cert_u
        DiscoverOption.Certification.UA -> R.string.cert_ua
    }
    return UiText.StaticText(resId = textRes)
}

private fun asReleaseTypeText(option: DiscoverOption.ReleaseType): UiText {
    val textRes = when (option) {
        DiscoverOption.ReleaseType.Digital -> R.string.digital
        DiscoverOption.ReleaseType.Physical -> R.string.physical
        DiscoverOption.ReleaseType.Premiere -> R.string.premiere
        DiscoverOption.ReleaseType.Theatrical -> R.string.theatrical
        DiscoverOption.ReleaseType.TheatricalLimited -> R.string.theatrical_limited
        DiscoverOption.ReleaseType.Tv -> R.string.tv
    }
    return UiText.StaticText(resId = textRes)
}

private fun asStatusText(option: DiscoverOption.Status): UiText {
    val textRes = when (option.statusId) {
        0 -> R.string.returning_series
        1 -> R.string.planned
        2 -> R.string.in_production
        3 -> R.string.ended
        4 -> R.string.canceled
        5 -> R.string.pilot
        else -> null
    }
    return if (textRes != null) {
        UiText.StaticText(resId = textRes)
    } else {
        UiText.DynamicText(text = option.statusId.toString())
    }
}

private fun asTvTypeText(option: DiscoverOption.TvType): UiText {
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
    return if (textRes != null) {
        UiText.StaticText(resId = textRes)
    } else {
        UiText.DynamicText(text = option.typeId.toString())
    }
}

private fun asIncludeAdultText(option: DiscoverOption.IncludeAdult): UiText {
    return UiText.StaticText(resId = if (option.include) R.string.yes else R.string.no)
}

private fun asIncludeVideoText(option: DiscoverOption.IncludeVideo): UiText {
    return UiText.StaticText(resId = if (option.include) R.string.yes else R.string.no)
}

private fun asVoteCountText(option: DiscoverOption.VoteCount.AtLeast): UiText {
    val textRes = when (option.value) {
        0 -> R.string.vote_count_0
        100 -> R.string.vote_count_100
        500 -> R.string.vote_count_500
        1000 -> R.string.vote_count_1000
        5000 -> R.string.vote_count_5000
        else -> null
    }
    return if (textRes != null) {
        UiText.StaticText(resId = textRes)
    } else {
        UiText.DynamicText(text = "${option.value}+")
    }
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

    val uiText = if (option == SortBy.None) {
        UiText.StaticText(resId = sortNameRes)
    } else {
        UiText.StaticText(
            resId = R.string.sort_by_value,
            formatArgs = arrayOf(
                UiText.StaticText(resId = sortNameRes),
                UiText.StaticText(resId = orderNameRes)
            )
        )
    }

    return FilterItem.Static(
        text = uiText,
        payload = FilterPayload.Sort(sortBy = option)
    )
}
