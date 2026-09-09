package com.ssverma.common.ui.filter.processor

import com.ssverma.common.ui.filter.FilterGroupContentType
import com.ssverma.common.ui.filter.FilterItem
import com.ssverma.common.ui.filter.mapDynamicOption
import com.ssverma.shared.domain.model.filter.DiscoverFilterState
import com.ssverma.shared.domain.model.filter.FilterId
import com.ssverma.shared.domain.model.filter.FilterPayload

class MultiSelectableProcessor :
    FilterGroupProcessor<FilterGroupContentType.ListType.MultiSelectableListType, FilterId> {

    override fun process(
        groupId: FilterId,
        content: FilterGroupContentType.ListType.MultiSelectableListType
    ): DiscoverFilterState {
        val options = content.selectionState.selected().mapNotNull { selected ->
            when (selected) {
                is FilterItem.Dynamic -> mapDynamicOption(groupId = groupId, id = selected.id)
                is FilterItem.Static -> (selected.payload as? FilterPayload.Option)?.discoverOption
            }
        }
        return DiscoverFilterState.options(options)
    }
}
