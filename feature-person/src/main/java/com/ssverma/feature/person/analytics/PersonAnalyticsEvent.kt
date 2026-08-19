package com.ssverma.feature.person.analytics

import com.ssverma.core.analytics.AnalyticsEvent
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.to
import com.ssverma.shared.analytics.SharedAnalyticsKeys
import com.ssverma.shared.domain.model.person.PersonMedia

sealed class PersonAnalyticsEvent(
    override val eventName: String,
    override val params: Map<String, AnalyticsParam> = emptyMap(),
) : AnalyticsEvent {

    data class PersonClicked(
        val personId: Int,
        val personName: String,
        val sourceScreen: String,
    ) : PersonAnalyticsEvent(
        eventName = PersonAnalyticsEventName.PERSON_CLICKED,
        params = mapOf(
            PersonAnalyticsKeys.PERSON_ID to personId,
            PersonAnalyticsKeys.PERSON_NAME to personName,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    )

    data class MediaClicked(
        val mediaId: Int,
        val mediaTitle: String,
        val section: String,
        val sourceScreen: String,
    ) : PersonAnalyticsEvent(
        eventName = PersonAnalyticsEventName.MEDIA_CLICKED,
        params = mapOf(
            PersonAnalyticsKeys.MEDIA_ID to mediaId,
            PersonAnalyticsKeys.MEDIA_TITLE to mediaTitle,
            SharedAnalyticsKeys.SECTION to section,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    ) {
        constructor(
            media: PersonMedia,
            section: String,
            sourceScreen: String
        ) : this(
            mediaId = media.id,
            mediaTitle = media.title,
            section = section,
            sourceScreen = sourceScreen
        )
    }

    data class ExpandMediaClicked(
        val personId: Int,
        val personName: String,
        val sourceScreen: String,
    ) : PersonAnalyticsEvent(
        eventName = PersonAnalyticsEventName.EXPAND_MEDIA_CLICKED,
        params = mapOf(
            PersonAnalyticsKeys.PERSON_ID to personId,
            PersonAnalyticsKeys.PERSON_NAME to personName,
            SharedAnalyticsKeys.SOURCE_SCREEN to sourceScreen,
        )
    )
}
