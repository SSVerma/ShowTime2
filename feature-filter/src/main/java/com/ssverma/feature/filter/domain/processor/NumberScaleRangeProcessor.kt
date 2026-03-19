package com.ssverma.feature.filter.domain.processor

import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.feature.filter.ui.filter.FilterGroupContentType
import com.ssverma.shared.domain.DiscoverOption

class NumberScaleRangeProcessor : FilterGroupProcessor<
        FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType,
        FilterId.RangeTypeId.NumberRange> {

    override fun process(
        groupId: FilterId.RangeTypeId.NumberRange,
        content: FilterGroupContentType.RangeType.ScaleRangeType.IntScaleRangeType
    ): DiscoverFilterState {
        val options = mutableListOf<DiscoverOption>()
        val from = content.state.fromValue
        val to = content.state.toValue

        when (groupId) {
            is FilterId.RangeTypeId.NumberRange.Rating,
            is FilterId.RangeTypeId.NumberRange.VoteAvg -> {
                from?.let { value -> options.add(DiscoverOption.Rating.From(from = value)) }
                to?.let { value -> options.add(DiscoverOption.Rating.To(to = value)) }
            }

            is FilterId.RangeTypeId.NumberRange.Runtime -> {
                from?.let { value -> options.add(DiscoverOption.Runtime.From(from = value)) }
                to?.let { value -> options.add(DiscoverOption.Runtime.To(to = value)) }
            }

        }

        return DiscoverFilterState.options(options)
    }
}
