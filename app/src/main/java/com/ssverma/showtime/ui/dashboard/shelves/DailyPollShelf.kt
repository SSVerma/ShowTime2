package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.ui.component.section.DailyPollCard

fun LazyListScope.dailyPollShelf(
    poll: DailyPoll,
    onOptionClick: (Int) -> Unit
) {
    if (poll.isEnabled && poll.options.isNotEmpty() && poll.question.isNotBlank()) {
        item(key = "daily_poll_shelf") {
            DailyPollCard(
                poll = poll,
                onOptionClick = onOptionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 18.dp)
            )
        }
    }
}
