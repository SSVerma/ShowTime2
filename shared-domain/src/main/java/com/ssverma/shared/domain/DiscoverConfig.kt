package com.ssverma.shared.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class DiscoverConfig(
    val discoverOptions: Set<DiscoverOption.OptionScope>,
    val sortBy: SortBy
) {

    fun isBare(): Boolean {
        return discoverOptions.isEmpty()
    }

    fun isDefault(): Boolean {
        val isSortDefault = when (sortBy) {
            SortBy.None -> true
            is SortBy.Popularity -> sortBy.order == Order.Descending
            else -> false
        }
        return discoverOptions.isEmpty() && isSortDefault
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiscoverConfig) return false

        if (discoverOptions != other.discoverOptions) return false
        if (sortBy != other.sortBy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = discoverOptions.hashCode()
        result = 31 * result + sortBy.hashCode()
        return result
    }

    abstract class Builder<C, O : DiscoverOption.OptionScope, B : Builder<C, O, B>> internal constructor() {
        protected val options = mutableSetOf<O>()
        protected var sortBy: SortBy = SortBy.None

        protected abstract fun self(): B

        fun with(vararg option: O): B = apply {
            this.options.addAll(option)
        }.self()

        fun sortBy(sortBy: SortBy): B = apply {
            this.sortBy = sortBy
        }.self()

        abstract fun build(): C
    }

    class MovieBuilder :
        Builder<MovieDiscoverConfig, DiscoverOption.OptionScope.Movie, MovieBuilder>() {
        override fun self(): MovieBuilder = this

        override fun build(): MovieDiscoverConfig {
            return MovieDiscoverConfig(options, sortBy)
        }
    }

    class TvBuilder : Builder<TvDiscoverConfig, DiscoverOption.OptionScope.Tv, TvBuilder>() {
        override fun self(): TvBuilder = this

        override fun build(): TvDiscoverConfig {
            return TvDiscoverConfig(options, sortBy)
        }
    }
}

@Serializable
data class MovieDiscoverConfig(
    val movieOptions: Set<DiscoverOption.OptionScope.Movie>,
    val sortByValue: SortBy
) : DiscoverConfig(movieOptions, sortByValue) {
    companion object {
        fun builder(sortBy: SortBy = SortBy.None): MovieBuilder {
            return MovieBuilder().sortBy(sortBy)
        }
    }
}

@Serializable
data class TvDiscoverConfig(
    val tvOptions: Set<DiscoverOption.OptionScope.Tv>,
    val sortByValue: SortBy
) : DiscoverConfig(tvOptions, sortByValue) {
    companion object {
        fun builder(sortBy: SortBy = SortBy.None): TvBuilder {
            return TvBuilder().sortBy(sortBy)
        }
    }
}
