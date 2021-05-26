package com.ssverma.showtime.domain.model

import android.content.Context
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import java.time.LocalDate

interface Filter

interface SingleValueFilter<T> {
    val filterValue: T
}

interface RangeValueFilter<T> {
    val fromFilterValue: T
    val toFilterValue: T
}

interface LabelFilter : SingleValueFilter<String> {
    val displayValue: String?
    val displayValueRes: Int
    fun requireDisplayValue(context: Context): String {
        if (displayValueRes == 0) {
            return displayValue
                ?: throw IllegalStateException("Provide display value either from resource or direct setting up field")
        }
        return context.getString(displayValueRes)
    }
}

data class DynamicLabelFilter(
    override val filterValue: String,
    override val displayValue: String?,
    @StringRes override val displayValueRes: Int = 0
) : LabelFilter

data class StaticLabelFilter(
    override val filterValue: String,
    @StringRes override val displayValueRes: Int,
    override val displayValue: String? = null
) : LabelFilter

data class NumberRangeFilter(
    @IntRange(from = 0)
    val min: Int,
    @IntRange(from = 1)
    val max: Int,
    @IntRange(from = 1)
    val secondaryGap: Int,
    @IntRange(from = 1)
    val primaryGap: Int,
    val defaultFromValue: Int? = null,
    val defaultToValue: Int? = null,
) : RangeValueFilter<Number?> {
    override val fromFilterValue: Number?
        get() = defaultFromValue

    override
    val toFilterValue: Number?
        get() = defaultToValue
}

data class DateRangeFilter(
    val defaultFromValue: LocalDate? = null,
    val defaultToValue: LocalDate? = null
) : RangeValueFilter<LocalDate?> {
    override val fromFilterValue: LocalDate?
        get() = defaultFromValue
    override val toFilterValue: LocalDate?
        get() = defaultToValue
}
