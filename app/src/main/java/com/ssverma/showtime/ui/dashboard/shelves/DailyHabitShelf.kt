package com.ssverma.showtime.ui.dashboard.shelves

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.ui.component.DailyGameBanner

fun LazyListScope.dailyHabitShelf(
    gameStats: CinemaGameStats,
    isTodayCompleted: Boolean,
    onOpenGame: () -> Unit
) {
    item(key = "daily_habit_shelf") {
        DailyGameBanner(
            gameStats = gameStats,
            isTodayCompleted = isTodayCompleted,
            onOpenGame = onOpenGame,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        )
    }
}
