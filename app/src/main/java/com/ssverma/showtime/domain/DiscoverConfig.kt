package com.ssverma.showtime.domain

abstract class DiscoverConfig(
    val discoverOptions: Set<DiscoverOption.OptionScope>,
    val sortBy: SortBy
) {

    abstract class Builder<C, O : DiscoverOption.OptionScope> internal constructor() {
        protected val options = mutableSetOf<O>()

        fun with(vararg option: O) = apply {
            this.options.addAll(option)
        }

        abstract fun build(): C
    }

    class MovieBuilder(
        private val sortBy: SortBy
    ) : Builder<MovieDiscoverConfig, DiscoverOption.OptionScope.Movie>() {
        override fun build(): MovieDiscoverConfig {
            return MovieDiscoverConfig(options, sortBy)
        }
    }

    class TvBuilder(
        private val sortBy: SortBy
    ) : Builder<TvDiscoverConfig, DiscoverOption.OptionScope.Tv>() {
        override fun build(): TvDiscoverConfig {
            return TvDiscoverConfig(options, sortBy)
        }
    }
}

class MovieDiscoverConfig(
    val movieOptions: Set<DiscoverOption.OptionScope.Movie>,
    sortBy: SortBy
) : DiscoverConfig(movieOptions, sortBy) {
    companion object {
        fun builder(sortBy: SortBy = SortBy.None): MovieBuilder {
            return MovieBuilder(sortBy)
        }
    }
}

class TvDiscoverConfig(
    val tvOptions: Set<DiscoverOption.OptionScope.Tv>,
    sortBy: SortBy
) : DiscoverConfig(tvOptions, sortBy) {
    companion object {
        fun builder(sortBy: SortBy = SortBy.None): TvBuilder {
            return TvBuilder(sortBy)
        }
    }
}