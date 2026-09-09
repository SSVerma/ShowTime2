package com.ssverma.common.ui.filter.processor

import com.ssverma.common.ui.filter.FilterGroup
import com.ssverma.common.ui.filter.FilterGroupContentType
import com.ssverma.shared.domain.model.filter.DiscoverFilterState
import com.ssverma.shared.domain.model.filter.FilterId

class FilterExtractionEngine {
    private val processors = mutableMapOf<Class<*>, FilterGroupProcessor<*, *>>()

    internal inline fun <reified C : FilterGroupContentType, reified ID : FilterId> register(
        processor: FilterGroupProcessor<C, ID>
    ) {
        processors[C::class.java] = processor
    }

    @Suppress("UNCHECKED_CAST")
    fun extract(groups: List<FilterGroup>): DiscoverFilterState {
        return groups.fold(DiscoverFilterState.empty()) { accState, group ->
            val contentClass = group.groupContent::class.java

            val processor = processors.entries
                .find { it.key.isAssignableFrom(contentClass) }?.value
                    as? FilterGroupProcessor<FilterGroupContentType, FilterId>

            val extractedState = processor?.process(group.groupId, group.groupContent)
                ?: DiscoverFilterState.empty()

            accState.merge(extractedState)
        }
    }
}

private val DefaultFilterExtractionEngine by lazy {
    FilterExtractionEngine().apply {
        register(processor = SingleSelectableProcessor())
        register(processor = MultiSelectableProcessor())
        register(processor = NumberScaleRangeProcessor())
        register(processor = DatePickerRangeProcessor())
    }
}

fun List<FilterGroup>.asDiscoverOptions(
    engine: FilterExtractionEngine = DefaultFilterExtractionEngine
): DiscoverFilterState {
    return engine.extract(groups = this)
}
