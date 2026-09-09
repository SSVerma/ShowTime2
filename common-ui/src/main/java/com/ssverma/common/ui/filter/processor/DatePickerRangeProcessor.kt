package com.ssverma.common.ui.filter.processor

import com.ssverma.common.ui.filter.FilterGroupContentType
import com.ssverma.shared.domain.DiscoverOption
import com.ssverma.shared.domain.model.filter.DiscoverFilterState
import com.ssverma.shared.domain.model.filter.FilterId

class DatePickerRangeProcessor : FilterGroupProcessor<
        FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType,
        FilterId.RangeTypeId.DateRange> {

    override fun process(
        groupId: FilterId.RangeTypeId.DateRange,
        content: FilterGroupContentType.RangeType.PickerRangeType.DatePickerRangeType
    ): DiscoverFilterState {
        val options = mutableListOf<DiscoverOption>()
        val from = content.state.fromValue
        val to = content.state.toValue

        when (groupId) {
            FilterId.RangeTypeId.DateRange.ReleaseDate -> {
                from?.let { value -> options.add(DiscoverOption.ReleaseDate.From(date = value)) }
                to?.let { value -> options.add(DiscoverOption.ReleaseDate.To(date = value)) }
            }

            FilterId.RangeTypeId.DateRange.AirDate -> {
                from?.let { value -> options.add(DiscoverOption.AirDate.From(date = value)) }
                to?.let { value -> options.add(DiscoverOption.AirDate.To(date = value)) }
            }
        }

        return DiscoverFilterState.options(options)
    }
}
