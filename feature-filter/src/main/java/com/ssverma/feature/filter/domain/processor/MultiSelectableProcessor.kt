package com.ssverma.feature.filter.domain.processor

import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.feature.filter.domain.model.FilterPayload
import com.ssverma.feature.filter.ui.filter.FilterGroupContentType
import com.ssverma.feature.filter.ui.filter.FilterItem
import com.ssverma.feature.filter.ui.filter.mapDynamicOption

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
