package com.ssverma.feature.filter.domain.processor

import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.feature.filter.ui.filter.FilterGroupContentType

/**
 * @param C The specific Content Type this processor handles.
 * @param ID The specific family of FilterIds this processor supports.
 */
interface FilterGroupProcessor<C : FilterGroupContentType, ID : FilterId> {
    fun process(groupId: ID, content: C): DiscoverFilterState
}
