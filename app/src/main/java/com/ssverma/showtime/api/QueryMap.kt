package com.ssverma.showtime.api

object DiscoverMovieQueryMap {
    fun of(
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