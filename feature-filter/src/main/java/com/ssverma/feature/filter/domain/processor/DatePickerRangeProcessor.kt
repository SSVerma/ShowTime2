package com.ssverma.feature.filter.domain.processor

import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.feature.filter.ui.filter.FilterGroupContentType
import com.ssverma.shared.domain.DiscoverOption

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
