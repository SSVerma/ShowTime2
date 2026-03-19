package com.ssverma.feature.filter.ui.filter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.ssverma.core.ui.MultiSelectableState
import com.ssverma.core.ui.SingleSelectableState
import com.ssverma.core.ui.UiText
import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.feature.filter.domain.model.FilterPayload
import java.time.LocalDate

data class FilterUiState(
    val filters: List<FilterGroup>,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false
)

data class FilterGroup(
    val groupId: FilterId,
    val title: UiText,
    val icon: ImageVector? = null,
    val groupContent: FilterGroupContentType
)

enum class ListDisplayMode {
    HorizontalRow, FlowRow, Picker
}

sealed interface FilterItem {
    val text: UiText
    val iconUrl: String? get() = null

    data class Static(
        override val text: UiText,
        override val iconUrl: String? = null,
        val payload: FilterPayload,
    ) : FilterItem

    data class Dynamic(
        val id: String,
        override val text: UiText,
        override val iconUrl: String? = null
    ) : FilterItem
}

sealed interface FilterGroupContentType {
    fun clear()
    fun reset()
    fun isDefault(): Boolean
    fun isEffectivelyEmpty(): Boolean

    sealed interface ListType : FilterGroupContentType {
        data class SingleSelectableListType(
            val displayMode: ListDisplayMode,
            val defaultSelectedItem: FilterItem? = null,
            val items: List<FilterItem>,
            val selectionState: SingleSelectableState<FilterItem> = SingleSelectableState(
                defaultSelectedItem
            )
        ) : ListType {
            override fun clear() = selectionState.clear()
            override fun reset() = selectionState.reset()
            override fun isDefault() = selectionState.isDefault()
            override fun isEffectivelyEmpty() = selectionState.selected() == null
        }

        data class MultiSelectableListType(
            val displayMode: ListDisplayMode,
            val defaultSelectedItems: Set<FilterItem> = emptySet(),
            val items: List<FilterItem>,
            val selectionState: MultiSelectableState<FilterItem> = MultiSelectableState(
                defaultSelectedItems
            )
        ) : ListType {
            override fun clear() = selectionState.clear()
            override fun reset() = selectionState.reset()
            override fun isDefault() = selectionState.isDefault()
            override fun isEffectivelyEmpty() = selectionState.selected().isEmpty()
        }
    }

    sealed interface RangeType<T> : FilterGroupContentType {
        val min: T
        val max: T

        sealed interface ScaleRangeType<T> : RangeType<T> {
            override val min: T
            override val max: T
            val primaryGap: T
            val secondaryGap: T
            val defaultMin: T?
            val defaultMax: T?
            val state: RangeState<T>

            data class IntScaleRangeType(
                override val min: Int,
                override val max: Int,
                override val primaryGap: Int,
                override val secondaryGap: Int,
                override val defaultMin: Int? = null,
                override val defaultMax: Int? = null,
                val isRange: Boolean = false,
                override val state: RangeState<Int> = RangeState(from = defaultMin, to = defaultMax)
            ) : ScaleRangeType<Int> {
                override fun clear() = state.clear()
                override fun reset() = state.reset()
                override fun isDefault() = state.isDefault()
                override fun isEffectivelyEmpty() = state.fromValue == null && state.toValue == null
            }
        }

        sealed interface PickerRangeType<T> : RangeType<T> {
            override val min: T
            override val max: T
            val defaultMin: T?
            val defaultMax: T?
            val state: RangeState<T>

            data class DatePickerRangeType(
                override val min: LocalDate,
                override val max: LocalDate,
                override val defaultMin: LocalDate? = null,
                override val defaultMax: LocalDate? = null,
                override val state: RangeState<LocalDate> = RangeState(
                    from = defaultMin,
                    to = defaultMax
                )
            ) : PickerRangeType<LocalDate> {
                override fun clear() = state.clear()
                override fun reset() = state.reset()
                override fun isDefault() = state.isDefault()
                override fun isEffectivelyEmpty() = state.fromValue == null && state.toValue == null
            }
        }
    }
}

class RangeState<T>(
    private val from: T? = null,
    private val to: T? = null
) {
    private var _fromValue: T? by mutableStateOf(from)
    private var _toValue: T? by mutableStateOf(to)

    val fromValue get() = _fromValue
    val toValue get() = _toValue

    fun onFromValueSelected(value: T?) {
        this._fromValue = value
    }

    fun onToValueSelected(value: T?) {
        this._toValue = value
    }

    fun clear() {
        _fromValue = from
        _toValue = to
    }

    fun reset() {
        _fromValue = null
        _toValue = null
    }

    fun isDefault() = _fromValue == from && _toValue == to
}
