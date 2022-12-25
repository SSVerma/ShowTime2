package com.ssverma.api.service.tmdb

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