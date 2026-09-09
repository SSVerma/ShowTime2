package com.ssverma.common.ui.filter.processor

import com.ssverma.common.ui.filter.FilterGroupContentType
import com.ssverma.common.ui.filter.FilterItem
import com.ssverma.common.ui.filter.mapDynamicOption
import com.ssverma.shared.domain.model.filter.DiscoverFilterState
import com.ssverma.shared.domain.model.filter.FilterId
import com.ssverma.shared.domain.model.filter.FilterPayload

class SingleSelectableProcessor :
    FilterGroupProcessor<FilterGroupContentType.ListType.SingleSelectableListType, FilterId> {

    override fun process(
        groupId: FilterId,
        content: FilterGroupContentType.ListType.SingleSelectableListType
    ): DiscoverFilterState {
        val selected = content.selectionState.selected() ?: return DiscoverFilterState.empty()

        return when (selected) {
            is FilterItem.Dynamic -> {
                val option = mapDynamicOption(groupId = groupId, id = selected.id)
                if (option != null) DiscoverFilterState.options(option) else DiscoverFilterState.empty()
            }

            is FilterItem.Static -> {
                when (val payload = selected.payload) {
                    is FilterPayload.Option -> DiscoverFilterState.options(payload.discoverOption)
                    is FilterPayload.Sort -> DiscoverFilterState.sort(sortBy = payload.sortBy)
                }
            }
        }
    }
}
