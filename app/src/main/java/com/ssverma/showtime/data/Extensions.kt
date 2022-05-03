package com.ssverma.showtime.data

import com.ssverma.api.service.tmdb.QueryMultiValue
import com.ssverma.api.service.tmdb.TmdbApiTiedConstants
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.showtime.domain.*
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.utils.formatAsIso

suspend fun <RemoteSuccess, RemoteError, DomainSuccess, FeatureFailure> ApiResponse<RemoteSuccess, RemoteError>.asDomainResult(
    mapFeatureFailure: (ApiResponse.Error.ClientError<RemoteError>) -> Failure<FeatureFailure> = {
        Failure.CoreFailure.UnexpectedFailure
    },
    mapRemoteToDomain: suspend (ApiResponse.Success<RemoteSuccess>) -> DomainSuccess
): DomainResult<DomainSuccess, Failure<FeatureFailure>> {
    return when (this) {
        is ApiResponse.Error.ClientError -> {
            DomainResult.Error(mapFeatureFailure(this))
        }
        is ApiResponse.Error.UnexpectedError -> {
            DomainResult.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        is ApiResponse.Error.NetworkError -> {
            DomainResult.Error(Failure.CoreFailure.NetworkFailure)
        }
        is ApiResponse.Error.ServerError -> {
            DomainResult.Error(Failure.CoreFailure.ServiceFailure)
        }
        is ApiResponse.Success -> {
            DomainResult.Success(data = mapRemoteToDomain(this))
        }
    }
}

suspend fun <RemoteSuccess, RemoteError, DomainSuccess> ApiResponse<RemoteSuccess, RemoteError>.asDomainResult(
    mapRemoteToDomain: suspend (ApiResponse.Success<RemoteSuccess>) -> DomainSuccess
): DomainResult<DomainSuccess, Failure.CoreFailure> {
    return when (this) {
        is ApiResponse.Error.ClientError,
        is ApiResponse.Error.UnexpectedError -> {
            DomainResult.Error(Failure.CoreFailure.UnexpectedFailure)
        }
        is ApiResponse.Error.NetworkError -> {
            DomainResult.Error(Failure.CoreFailure.NetworkFailure)
        }
        is ApiResponse.Error.ServerError -> {
            DomainResult.Error(Failure.CoreFailure.ServiceFailure)
        }
        is ApiResponse.Success -> {
            DomainResult.Success(data = mapRemoteToDomain(this))
        }
    }
}


fun DiscoverConfig.asQueryMap(): Map<String, String> {
    val queryMap = mutableMapOf<String, String>()

    val multiValueOptions = mutableMapOf<String, Pair<MultiValueMode, MutableSet<String>>>()

    this.discoverOptions.forEach { option ->
        val entry = option.asQueryMapEntry()
        when (option.mode) {
            is OptionMode.MultiValue -> {
                val pair = multiValueOptions.getOrDefault(
                    entry.first,
                    Pair(first = (option.mode as OptionMode.MultiValue).valueMode, mutableSetOf())
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
                        TmdbApiTiedConstants.AvailableSortingOptions.ReleaseDateDesc
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
                        TmdbApiTiedConstants.AvailableSortingOptions.VoteAvgDesc
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
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.country,
                second = iso3
            )
        }
        is DiscoverOption.Genre -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withGenres,
                second = genreId.toString()
            )
        }
        is DiscoverOption.Language -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.language,
                second = iso3
            )
        }
        DiscoverOption.MediaType.Movie -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableMediaType.Movie,
                second = TmdbApiTiedConstants.AvailableMediaType.Movie
            )
        }
        DiscoverOption.MediaType.Tv -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableMediaType.Tv,
                second = TmdbApiTiedConstants.AvailableMediaType.Tv
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
        DiscoverOption.Monetization.Stream -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withMonetizationType,
                second = TmdbApiTiedConstants.AvailableMonetizationTypes.Stream
            )
        }
        is DiscoverOption.Person -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.withPeople,
                second = personId.toString()
            )
        }
        is DiscoverOption.ReleaseDate.From -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.primaryReleaseDateGte,
                second = date.formatAsIso().orEmpty()
            )
        }
        is DiscoverOption.ReleaseDate.To -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.primaryReleaseDateLte,
                second = date.formatAsIso().orEmpty()
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
        is DiscoverOption.Region -> {
            return Pair(
                first = TmdbApiTiedConstants.AvailableDiscoverOptions.watchRegion,
                second = iso3
            )
        }
    }
}