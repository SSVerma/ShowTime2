package com.ssverma.feature.filter.domain.processor

import com.ssverma.feature.filter.domain.model.FilterId
import com.ssverma.feature.filter.domain.model.FilterPayload
import com.ssverma.feature.filter.ui.filter.FilterGroupContentType
import com.ssverma.feature.filter.ui.filter.FilterItem
import com.ssverma.feature.filter.ui.filter.mapDynamicOption

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
