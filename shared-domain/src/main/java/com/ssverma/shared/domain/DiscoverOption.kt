package com.ssverma.shared.domain

import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
sealed interface OptionMode {
    @Serializable
    data class MultiValue(val valueMode: MultiValueMode) : OptionMode

    @Serializable
    object SingleValue : OptionMode
}

@Serializable
sealed interface MultiValueMode {
    @Serializable
    object Or : MultiValueMode

    @Serializable
    object And : MultiValueMode
}

@Serializable
sealed interface DiscoverOption {
    val mode: OptionMode

    @Serializable
    sealed interface OptionScope : DiscoverOption {
        @Serializable
        sealed interface Movie : OptionScope

        @Serializable
        sealed interface Tv : OptionScope
    }

    @Serializable
    sealed class MediaType(
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv {
        @Serializable
        object Movie : MediaType()

        @Serializable
        object Tv : MediaType()
    }

    @Serializable
    sealed class Monetization(
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv {
        @Serializable
        object Free : Monetization()

        @Serializable
        object Ads : Monetization()

        @Serializable
        object Rent : Monetization()

        @Serializable
        object Buy : Monetization()

        @Serializable
        object Flatrate : Monetization()
    }

    @Serializable
    sealed class ReleaseType(
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie {
        @Serializable
        object Premiere : ReleaseType()

        @Serializable
        object TheatricalLimited : ReleaseType()

        @Serializable
        object Theatrical : ReleaseType()

        @Serializable
        object Digital : ReleaseType()

        @Serializable
        object Physical : ReleaseType()

        @Serializable
        object Tv : ReleaseType()
    }

    @Serializable
    sealed class Certification(
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie {
        @Serializable
        object U : Certification()

        @Serializable
        object UA : Certification()

        @Serializable
        object A : Certification()
    }

    @Serializable
    data class Language(
        val iso3: String,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class Country(
        val iso3: String,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class OriginalLanguage(
        val iso2: String,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class WatchRegion(
        val iso3: String,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    sealed interface ReleaseDate : OptionScope.Movie {
        @Serializable
        data class From(
            @Serializable(with = LocalDateSerializer::class)
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : ReleaseDate

        @Serializable
        data class To(
            @Serializable(with = LocalDateSerializer::class)
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : ReleaseDate

        @Serializable
        data class Year(
            val year: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : ReleaseDate
    }

    @Serializable
    sealed interface PrimaryReleaseDate : OptionScope.Movie {
        @Serializable
        data class From(
            @Serializable(with = LocalDateSerializer::class)
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : PrimaryReleaseDate

        @Serializable
        data class To(
            @Serializable(with = LocalDateSerializer::class)
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : PrimaryReleaseDate

        @Serializable
        data class Year(
            val year: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : PrimaryReleaseDate
    }

    @Serializable
    sealed interface Rating : OptionScope.Movie, OptionScope.Tv {
        @Serializable
        data class From(
            val from: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Rating

        @Serializable
        data class To(
            val to: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Rating
    }

    @Serializable
    sealed interface VoteCount : OptionScope.Movie, OptionScope.Tv {
        @Serializable
        data class AtLeast(
            val value: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : VoteCount

        @Serializable
        data class AtMax(
            val value: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : VoteCount
    }

    @Serializable
    sealed interface Runtime : OptionScope.Movie {
        @Serializable
        data class From(
            val from: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Runtime

        @Serializable
        data class To(
            val to: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Runtime
    }

    @Serializable
    sealed interface AirDate : OptionScope.Tv {
        @Serializable
        data class From(
            @Serializable(with = LocalDateSerializer::class)
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : AirDate

        @Serializable
        data class To(
            @Serializable(with = LocalDateSerializer::class)
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : AirDate
    }

    @Serializable
    sealed interface FirstAirDate : OptionScope.Tv {
        @Serializable
        data class From(
            @Serializable(with = LocalDateSerializer::class)
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : AirDate

        @Serializable
        data class To(
            @Serializable(with = LocalDateSerializer::class)
            val date: LocalDate,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : AirDate

        @Serializable
        data class Year(
            val year: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : AirDate
    }

    @Serializable
    data class Person(
        val personId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class Cast(
        val personId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class Crew(
        val personId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class Genre(
        val genreId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class WithoutGenre(
        val genreId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.And)
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class Keyword(
        val keywordId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class WithoutKeyword(
        val keywordId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.And)
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class Company(
        val companyId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class WithoutCompany(
        val companyId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.And)
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class Network(
        val networkId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Tv

    @Serializable
    data class Status(
        val statusId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Tv

    @Serializable
    data class TvType(
        val typeId: Int,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Tv

    @Serializable
    data class IncludeAdult(
        val include: Boolean,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv

    @Serializable
    data class IncludeVideo(
        val include: Boolean,
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie

    @Serializable
    data class WatchProvider(
        val providerId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv
}

@Serializable
sealed interface SortBy {
    val order: Order

    fun withOrder(order: Order): SortBy

    @Serializable
    data class Popularity(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    @Serializable
    data class ReleaseDate(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    @Serializable
    data class Revenue(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    @Serializable
    data class Title(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    @Serializable
    data class Rating(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    @Serializable
    data class Vote(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    @Serializable
    data class AirDate(override val order: Order = Order.Descending) : SortBy {
        override fun withOrder(order: Order) = copy(order = order)
    }

    @Serializable
    object None : SortBy {
        override val order: Order
            get() = Order.Descending

        override fun withOrder(order: Order) = this
    }
}

@Serializable
sealed interface Order {
    @Serializable
    object Ascending : Order

    @Serializable
    object Descending : Order
}