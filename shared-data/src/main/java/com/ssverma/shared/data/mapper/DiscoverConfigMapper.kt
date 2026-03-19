package com.ssverma.shared.data.mapper

import com.ssverma.api.service.tmdb.QueryMultiValue
import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.shared.domain.DiscoverConfig
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.MultiValueMode
import com.ssverma.shared.domain.OptionMode
import com.ssverma.shared.domain.Order
import com.ssverma.shared.domain.SortBy
import com.ssverma.shared.domain.utils.formatAsIso

fun DiscoverConfig.asQueryMap(): Map<String, String> {
    val queryMap = mutableMapOf<String, String>()

    val multiValueOptions = mutableMapOf<String, Pair<MultiValueMode, MutableSet<String>>>()

    this.discoverOptions.forEach { option ->
        val entry = option.asQueryMapEntry()
        when (option.mode) {
            is OptionMode.MultiValue -> {
                val pair = multiValueOptions[entry.first]
                    ?: Pair(
                        first = (option.mode as OptionMode.MultiValue).valueMode,
                        second = mutableSetOf()
                    )
                pair.second.add(entry.second)

                multiValueOptions[entry.first] = pair
            }

            OptionMode.SingleValue -> {
                queryMap[entry.first] = entry.second
            }
        }
    }

    multiValueOptions.forEach { (key, value) ->
        val entry = convertMultiValueOptionsAsQueryMapEntry(
            key = key,
            mode = value.first,
            values = value.second
        )
        queryMap[entry.first] = entry.second
    }

    val sortByEntry = sortBy.asQueryMapEntry()
    queryMap[sortByEntry.first] = sortByEntry.second

    return queryMap
}

private fun SortBy.asQueryMapEntry(): Pair<String, String> {
    val sortByKey = TmdbApiTiedConstants.AvailableDiscoverOptions.sortBy

    return Pair(
        first = sortByKey,
        second = when (this) {
            SortBy.None -> ""
            is SortBy.Popularity -> {
                when (order) {
                    Order.Ascending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.PopularityAsc
                    }

                    Order.Descending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.PopularityDesc
                    }
                }
            }

            is SortBy.Rating -> {
                when (order) {
                    Order.Ascending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.VoteAvgAsc
                    }

                    Order.Descending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.VoteAvgDesc
                    }
                }
            }

            is SortBy.ReleaseDate -> {
                when (order) {
                    Order.Ascending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.ReleaseDateAsc
                    }

                    Order.Descending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.ReleaseDateDesc
                    }
                }
            }

            is SortBy.Revenue -> {
                when (order) {
                    Order.Ascending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.RevenueAsc
                    }

                    Order.Descending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.RevenueDesc
                    }
                }
            }

            is SortBy.Title -> {
                when (order) {
                    Order.Ascending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.OriginalTitleAsc
                    }

                    Order.Descending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.OriginalTitleDesc
                    }
                }
            }

            is SortBy.Vote -> {
                when (order) {
                    Order.Ascending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.VoteCountAsc
                    }

                    Order.Descending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.VoteCountDesc
                    }
                }
            }

            is SortBy.AirDate -> {
                when (order) {
                    Order.Ascending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.FirstAirDateAsc
                    }

                    Order.Descending -> {
                        TmdbApiTiedConstants.AvailableSortingOptions.FirstAirDateDesc
                    }
                }
            }
        }
    )
}

private fun convertMultiValueOptionsAsQueryMapEntry(
    key: String,
    mode: MultiValueMode,
    values: Set<String>
): Pair<String, String> {
    return when (mode) {
        MultiValueMode.And -> {
            val builder = QueryMultiValue.andBuilder()
            values.forEach { value ->
                builder.and(value)
            }
            Pair(first = key, builder.build().asFormattedValues().orEmpty())
        }

        MultiValueMode.Or -> {
            val builder = QueryMultiValue.orBuilder()
            values.forEach { value ->
                builder.or(value)
            }
            Pair(first = key, builder.build().asFormattedValues().orEmpty())
        }
    }
}

private fun DiscoverOption.asQueryMapEntry(): Pair<String, String> {
    when (this) {
        is DiscoverOption.AirDate.From -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.firstAirDateGte,
                second = date.formatAsIso().orEmpty()
            )
        }

        is DiscoverOption.AirDate.To -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.firstAirDateLte,
                second = date.formatAsIso().orEmpty()
            )
        }

        is DiscoverOption.AirDate.Year -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.firstAirDateYear,
                second = year.toString()
            )
        }

        DiscoverOption.Certification.A -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.certification,
                second = TmdbApiTiedConstants.AvailableCertificationTypes.A
            )
        }

        DiscoverOption.Certification.U -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.certification,
                second = TmdbApiTiedConstants.AvailableCertificationTypes.U
            )
        }

        DiscoverOption.Certification.UA -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.certification,
                second = TmdbApiTiedConstants.AvailableCertificationTypes.UA
            )
        }

        is DiscoverOption.Country -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withOriginCountry,
                second = iso3
            )
        }

        is DiscoverOption.Genre -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withGenres,
                second = genreId.toString()
            )
        }

        is DiscoverOption.WithoutGenre -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withoutGenres,
                second = genreId.toString()
            )
        }

        is DiscoverOption.Language -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withOriginalLanguage,
                second = iso3
            )
        }

        is DiscoverOption.OriginalLanguage -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withOriginalLanguage,
                second = iso2
            )
        }

        DiscoverOption.MediaType.Movie -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableMediaTypes.Movie,
                second = TmdbApiTiedConstants.AvailableMediaTypes.Movie
            )
        }

        DiscoverOption.MediaType.Tv -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableMediaTypes.Tv,
                second = TmdbApiTiedConstants.AvailableMediaTypes.Tv
            )
        }

        DiscoverOption.Monetization.Ads -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withMonetizationType,
                second = TmdbApiTiedConstants.AvailableMonetizationTypes.Ads
            )
        }

        DiscoverOption.Monetization.Buy -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withMonetizationType,
                second = TmdbApiTiedConstants.AvailableMonetizationTypes.Buy
            )
        }

        DiscoverOption.Monetization.Free -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withMonetizationType,
                second = TmdbApiTiedConstants.AvailableMonetizationTypes.Free
            )
        }

        DiscoverOption.Monetization.Rent -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withMonetizationType,
                second = TmdbApiTiedConstants.AvailableMonetizationTypes.Rent
            )
        }

        DiscoverOption.Monetization.Flatrate -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withMonetizationType,
                second = TmdbApiTiedConstants.AvailableMonetizationTypes.Flatrate
            )
        }

        is DiscoverOption.Person -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withPeople,
                second = personId.toString()
            )
        }

        is DiscoverOption.Cast -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withCast,
                second = personId.toString()
            )
        }

        is DiscoverOption.Crew -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withCrew,
                second = personId.toString()
            )
        }

        is DiscoverOption.ReleaseDate.From -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.releaseDateGte,
                second = date.formatAsIso().orEmpty()
            )
        }

        is DiscoverOption.ReleaseDate.To -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.releaseDateLte,
                second = date.formatAsIso().orEmpty()
            )
        }

        is DiscoverOption.ReleaseDate.Year -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.primaryReleaseYear,
                second = year.toString()
            )
        }

        is DiscoverOption.PrimaryReleaseDate.From -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.primaryReleaseDateGte,
                second = date.formatAsIso().orEmpty()
            )
        }

        is DiscoverOption.PrimaryReleaseDate.To -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.primaryReleaseDateLte,
                second = date.formatAsIso().orEmpty()
            )
        }

        is DiscoverOption.PrimaryReleaseDate.Year -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.primaryReleaseYear,
                second = year.toString()
            )
        }

        DiscoverOption.ReleaseType.Digital -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.releaseType,
                second = TmdbApiTiedConstants.AvailableReleaseTypes.Digital.toString()
            )
        }

        DiscoverOption.ReleaseType.Physical -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.releaseType,
                second = TmdbApiTiedConstants.AvailableReleaseTypes.Physical.toString()
            )
        }

        DiscoverOption.ReleaseType.Premiere -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.releaseType,
                second = TmdbApiTiedConstants.AvailableReleaseTypes.Premiere.toString()
            )
        }

        DiscoverOption.ReleaseType.Theatrical -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.releaseType,
                second = TmdbApiTiedConstants.AvailableReleaseTypes.Theatrical.toString()
            )
        }

        DiscoverOption.ReleaseType.TheatricalLimited -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.releaseType,
                second = TmdbApiTiedConstants.AvailableReleaseTypes.TheatricalLimited.toString()
            )
        }

        DiscoverOption.ReleaseType.Tv -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.releaseType,
                second = TmdbApiTiedConstants.AvailableReleaseTypes.Tv.toString()
            )
        }

        is DiscoverOption.Runtime.From -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.runtimeGte,
                second = from.toString()
            )
        }

        is DiscoverOption.Runtime.To -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.runtimeLte,
                second = to.toString()
            )
        }

        is DiscoverOption.Rating.From -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.voteAvgGte,
                second = from.toString()
            )
        }

        is DiscoverOption.Rating.To -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.voteAvgLte,
                second = to.toString()
            )
        }

        is DiscoverOption.Keyword -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withKeywords,
                second = keywordId.toString()
            )
        }

        is DiscoverOption.WithoutKeyword -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withoutKeywords,
                second = keywordId.toString()
            )
        }

        is DiscoverOption.Company -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withCompanies,
                second = companyId.toString()
            )
        }

        is DiscoverOption.WithoutCompany -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withoutCompanies,
                second = companyId.toString()
            )
        }

        is DiscoverOption.Network -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withNetworks,
                second = networkId.toString()
            )
        }

        is DiscoverOption.Status -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withStatus,
                second = statusId.toString()
            )
        }

        is DiscoverOption.WatchRegion -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.watchRegion,
                second = iso3
            )
        }

        is DiscoverOption.TvType -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withType,
                second = typeId.toString()
            )
        }

        is DiscoverOption.IncludeAdult -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.includeAdult,
                second = include.toString()
            )
        }

        is DiscoverOption.IncludeVideo -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.includeVideo,
                second = include.toString()
            )
        }

        is DiscoverOption.WatchProvider -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withWatchProviders,
                second = providerId.toString()
            )
        }

        is DiscoverOption.VoteCount.AtLeast -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.voteCountGte,
                second = value.toString()
            )
        }

        is DiscoverOption.VoteCount.AtMax -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.voteCountLte,
                second = value.toString()
            )
        }
    }
}
