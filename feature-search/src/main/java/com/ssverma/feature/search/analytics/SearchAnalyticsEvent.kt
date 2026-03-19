package com.ssverma.feature.search.analytics

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.feature.search.domain.model.SearchHistory
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.shared.analytics.SharedAnalyticsValues
import com.ssverma.shared.analytics.asAnalyticsValue

sealed class SearchAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap(),
) : AnalyticsEvent {

    data class SearchResultClicked(
        val suggestion: SearchSuggestion
    ) : SearchAnalyticsEvent(
        eventName = SearchAnalyticsEventName.SEARCH_RESULT_CLICKED,
        params = when (suggestion) {
            is SearchSuggestion.Movie -> mapOf(
                SearchAnalyticsKeys.SUGGESTION_ID to suggestion.id,
                SearchAnalyticsKeys.SUGGESTION_NAME to suggestion.title,
                SearchAnalyticsKeys.SUGGESTION_TYPE to SharedAnalyticsValues.MEDIA_TYPE_MOVIE
            )

            is SearchSuggestion.Person -> mapOf(
                SearchAnalyticsKeys.SUGGESTION_ID to suggestion.id,
                SearchAnalyticsKeys.SUGGESTION_NAME to suggestion.name,
                SearchAnalyticsKeys.SUGGESTION_TYPE to SharedAnalyticsValues.MEDIA_TYPE_PERSON
            )

            is SearchSuggestion.TvShow -> mapOf(
                SearchAnalyticsKeys.SUGGESTION_ID to suggestion.id,
                SearchAnalyticsKeys.SUGGESTION_NAME to suggestion.title,
                SearchAnalyticsKeys.SUGGESTION_TYPE to SharedAnalyticsValues.MEDIA_TYPE_TV
            )

            SearchSuggestion.None -> emptyMap()
        }
    )

    data class SearchHistoryClicked(
        val history: SearchHistory
    ) : SearchAnalyticsEvent(
        eventName = SearchAnalyticsEventName.SEARCH_HISTORY_CLICKED,
        params = mapOf(
            SearchAnalyticsKeys.SUGGESTION_ID to history.id,
            SearchAnalyticsKeys.SUGGESTION_NAME to history.name,
            SearchAnalyticsKeys.SUGGESTION_TYPE to history.mediaType.asAnalyticsValue()
        )
    )

    data class SearchHistoryCleared(
        val history: SearchHistory
    ) : SearchAnalyticsEvent(
        eventName = SearchAnalyticsEventName.SEARCH_HISTORY_CLEARED,
        params = mapOf(
            SearchAnalyticsKeys.SUGGESTION_ID to history.id,
            SearchAnalyticsKeys.SUGGESTION_NAME to history.name,
        )
    )
}
