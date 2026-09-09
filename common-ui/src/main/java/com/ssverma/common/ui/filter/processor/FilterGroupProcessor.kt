package com.ssverma.common.ui.filter.processor

import com.ssverma.common.ui.filter.FilterGroupContentType
import com.ssverma.shared.domain.model.filter.DiscoverFilterState
import com.ssverma.shared.domain.model.filter.FilterId

/**
 * @param C The specific Content Type this processor handles.
 * @param ID The specific family of FilterIds this processor supports.
 */
interface FilterGroupProcessor<C : FilterGroupContentType, ID : FilterId> {
    fun process(groupId: ID, content: C): DiscoverFilterState
}
