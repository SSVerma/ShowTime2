package com.ssverma.api.service.tmdb

object DiscoverQueryMap {
    fun ofMovie(
        sortBy: String? = null,
        primaryReleaseDateLte: String? = null,
        primaryReleaseDateGte: String? = null,
        monetizationType: String? = null,
        releaseType: QueryMultiValue? = null,
        genres: QueryMultiValue? = null,
        keywords: QueryMultiValue? = null,
        voteAvgLte: String? = null,
        voteAvgGte: String? = null,
        runtimeLte: String? = null,
        runtimeGte: String? = null,
    ) = of(
        sortBy = sortBy,
        primaryReleaseDateLte = primaryReleaseDateLte,
        primaryReleaseDateGte = primaryReleaseDateGte,
        monetizationType = monetizationType,
        releaseType = releaseType,
        genres = genres,
        keywords = keywords,
        voteAvgLte = voteAvgLte,
        voteAvgGte = voteAvgGte,
        runtimeLte = runtimeLte,
        runtimeGte = runtimeGte
    )

    fun ofTv(
        sortBy: String? = null,
        monetizationType: String? = null,
        genres: QueryMultiValue? = null,
        keywords: QueryMultiValue? = null,
        voteAvgLte: String? = null,
        voteAvgGte: String? = null,
        runtimeLte: String? = null,
        runtimeGte: String? = null,
        firstAirDateLte: String? = null,
        firstAirDateGte: String? = null,
        airDateLte: String? = null,
        airDateGte: String? = null,
    ) = of(
        sortBy = sortBy,
        monetizationType = monetizationType,
        genres = genres,
        keywords = keywords,
        voteAvgLte = voteAvgLte,
        voteAvgGte = voteAvgGte,
        runtimeLte = runtimeLte,
        runtimeGte = runtimeGte,
        firstAirDateLte = firstAirDateLte,
        firstAirDateGte = firstAirDateGte,
        airDateLte = airDateLte,
        airDateGte = airDateGte
    )

    private fun of(
        sortBy: String? = null,
        primaryReleaseDateLte: String? = null,
        primaryReleaseDateGte: String? = null,
        monetizationType: String? = null,
        releaseType: QueryMultiValue? = null,
        genres: QueryMultiValue? = null,
        keywords: QueryMultiValue? = null,
        voteAvgLte: String? = null,
        voteAvgGte: String? = null,
        runtimeLte: String? = null,
        runtimeGte: String? = null,
        firstAirDateLte: String? = null,
        firstAirDateGte: String? = null,
        airDateLte: String? = null,
        airDateGte: String? = null,
    ): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            sortBy?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.sortBy, it)
            }
            primaryReleaseDateLte?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.primaryReleaseDateLte, it)
            }
            primaryReleaseDateGte?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.primaryReleaseDateGte, it)
            }
            monetizationType?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.withMonetizationType, it)
            }
            releaseType?.asFormattedValues()?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.releaseType, it)
            }
            genres?.asFormattedValues()?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.withGenres, it)
            }
            keywords?.asFormattedValues()?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.withKeywords, it)
            }
            voteAvgLte?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.voteAvgLte, it)
            }
            voteAvgGte?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.voteAvgGte, it)
            }
            runtimeLte?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.runtimeLte, it)
            }
            runtimeGte?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.runtimeGte, it)
            }
            firstAirDateLte?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.firstAirDateLte, it)
            }
            firstAirDateGte?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.firstAirDateGte, it)
            }
            airDateLte?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.airDateLte, it)
            }
            airDateGte?.let {
                put(TmdbApiTiedConstants.AvailableDiscoverOptions.airDateGte, it)
            }
        }
    }
}

object AppendableQueryMap {
    fun of(
        appendToResponse: QueryMultiValue.AndBuilder? = null
    ): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            appendToResponse?.build()?.asFormattedValues()?.let {
                put(TmdbApiTiedConstants.AppendToResponse, it)
            }
        }
    }
}

class QueryMultiValue private constructor(
    private val formattedQueryParamValue: String?
) {
    companion object {
        fun andBuilder(): AndBuilder {
            return AndBuilder(delimiter = ",")
        }

        fun orBuilder(): OrBuilder {
            return OrBuilder(delimiter = "|")
        }
    }

    fun asFormattedValues(): String? {
        return formattedQueryParamValue
    }

    open class Builder(private val delimiter: String) {
        private var queryParamValues: String = ""

        protected open fun applyDelimiter(value: String) {
            queryParamValues = queryParamValues + value + delimiter
        }

        protected open fun applyDelimiter(value: Int) {
            queryParamValues = queryParamValues + value + delimiter
        }

        fun build(): QueryMultiValue {
            val resultLocal = queryParamValues
            val result = if (resultLocal.isEmpty()) {
                null
            } else {
                resultLocal.substring(0, resultLocal.length - 1)
            }
            return QueryMultiValue(result)
        }
    }

    class AndBuilder(delimiter: String) : Builder(delimiter = delimiter) {
        fun and(value: String): AndBuilder {
            super.applyDelimiter(value)
            return this
        }

        fun and(value: Int): AndBuilder {
            super.applyDelimiter(value)
            return this
        }
    }

    class OrBuilder(delimiter: String) : Builder(delimiter = delimiter) {
        fun or(value: String): OrBuilder {
            super.applyDelimiter(value)
            return this
        }

        fun or(value: Int): OrBuilder {
            super.applyDelimiter(value)
            return this
        }
    }
}