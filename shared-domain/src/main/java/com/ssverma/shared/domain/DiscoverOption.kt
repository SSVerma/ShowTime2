package com.ssverma.shared.domain

import java.time.LocalDate

sealed interface OptionMode {
    data class MultiValue(val valueMode: MultiValueMode) : OptionMode
    object SingleValue : OptionMode
}

sealed interface MultiValueMode {
    object Or : MultiValueMode
    object And : MultiValueMode
}

sealed interface DiscoverOption {
    val mode: OptionMode

    sealed interface OptionScope : DiscoverOption {
        sealed interface Movie : OptionScope
        sealed interface Tv : OptionScope
    }

    sealed class MediaType(
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv {
        object Movie : MediaType()
        object Tv : MediaType()
    }

    sealed class Monetization(
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv {
        object Free : Monetization()
        object Ads : Monetization()
        object Rent : Monetization()
        object Buy : Monetization()
        object Flatrate : Monetization()
    }

    sealed class ReleaseType(
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie {
        object Premiere : ReleaseType()
        object TheatricalLimited : ReleaseType()
        object Theatrical : ReleaseType()
        object Digital : ReleaseType()
        object Physical : ReleaseType()
        object Tv : ReleaseType()
    }

    sealed class Certification(
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie {
        object U : Certification()
        object UA : Certification()
        object A : Certification()
    }

    data class Language(
        val iso3: String,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv

    data class Country(
        val iso3: String,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv

    data class OriginalLanguage(
        val iso2: String,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv

    data class WatchRegion(
        val iso3: String,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv

    sealed interface ReleaseDate : OptionScope.Movie {
        data class From(
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : ReleaseDate

        data class To(
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : ReleaseDate

        data class Year(
            val year: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : ReleaseDate
    }

    sealed interface PrimaryReleaseDate : OptionScope.Movie {
        data class From(
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : PrimaryReleaseDate

        data class To(
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : PrimaryReleaseDate

        data class Year(
            val year: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : PrimaryReleaseDate
    }

    sealed interface Rating : OptionScope.Movie, OptionScope.Tv {
        data class From(
            val from: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Rating

        data class To(
            val to: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Rating

        data class VoteCount(
            val voteCount: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Rating
    }

    sealed interface Runtime : OptionScope.Movie {
        data class From(
            val from: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Runtime

        data class To(
            val to: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Runtime
    }

    sealed interface AirDate : OptionScope.Tv {
        data class From(
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : AirDate

        data class To(
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : AirDate

        data class Year(
            val year: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : AirDate
    }

    data class Person(
        val personId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    data class Cast(
        val personId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    data class Crew(
        val personId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    data class Genre(
        val genreId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    data class WithoutGenre(
        val genreId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.And)
    ) : OptionScope.Movie, OptionScope.Tv

    data class Keyword(
        val keywordId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    data class WithoutKeyword(
        val keywordId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.And)
    ) : OptionScope.Movie, OptionScope.Tv

    data class Company(
        val companyId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    data class WithoutCompany(
        val companyId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.And)
    ) : OptionScope.Movie, OptionScope.Tv

    data class Network(
        val networkId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Tv

    data class Status(
        val statusId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Tv

    data class TvType(
        val typeId: Int,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Tv

    data class IncludeAdult(
        val include: Boolean,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv

    data class IncludeVideo(
        val include: Boolean,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie
}

sealed interface SortBy {
    val order: Order

    fun withOrder(order: Order): SortBy

    data class Popularity(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    data class ReleaseDate(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    data class Revenue(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    data class Title(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    data class Rating(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    data class Vote(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    data class AirDate(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    object None : SortBy {
        override val order: Order
            get() = Order.Descending

        override fun withOrder(order: Order) = this
    }
}

sealed interface Order {
    object Ascending : Order
    object Descending : Order
}