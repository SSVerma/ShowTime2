package com.ssverma.showtime.domain

import androidx.annotation.IntRange
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
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie, OptionScope.Tv {
        object Free : Monetization()
        object Ads : Monetization()
        object Rent : Monetization()
        object Buy : Monetization()
        object Stream : Monetization()
    }

    sealed class ReleaseType(
        override val mode: OptionMode = OptionMode.SingleValue
    ) : OptionScope.Movie {
        object Premiere : ReleaseType()
        object TheatricalLimited : ReleaseType()
        object Theatrical : ReleaseType()
        object Digital : ReleaseType()
        object Physical : ReleaseType()
        object Tv : ReleaseType()
    }

    sealed class Certification(
        override val mode: OptionMode = OptionMode.SingleValue
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

    data class Region(
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
    }

    sealed interface Rating : OptionScope.Movie, OptionScope.Tv {
        data class From(
            @IntRange(from = 0, to = 10) val from: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Rating

        data class To(
            @IntRange(from = 0, to = 10) val to: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Rating
    }

    sealed interface Runtime : OptionScope.Movie {
        data class From(
            @IntRange(from = 0) val from: Int,
            override val mode: OptionMode = OptionMode.SingleValue
        ) : Runtime

        data class To(
            @IntRange(from = 0) val to: Int,
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
    }

    data class Person(
        val personId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    data class Genre(
        val genreId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv

    data class Keyword(
        val keywordId: Int,
        override val mode: OptionMode = OptionMode.MultiValue(valueMode = MultiValueMode.Or)
    ) : OptionScope.Movie, OptionScope.Tv
}

sealed interface SortBy {
    val order: Order

    data class Popularity(override val order: Order = Order.Descending) : SortBy
    data class ReleaseDate(override val order: Order = Order.Descending) : SortBy
    data class Revenue(override val order: Order = Order.Descending) : SortBy
    data class Title(override val order: Order = Order.Descending) : SortBy
    data class Rating(override val order: Order = Order.Descending) : SortBy
    data class Vote(override val order: Order = Order.Descending) : SortBy
    data class AirDate(override val order: Order = Order.Descending) : SortBy
    object None : SortBy {
        override val order: Order
            get() = Order.Descending
    }
}

sealed interface Order {
    object Ascending : Order
    object Descending : Order
}