package com.ssverma.showtime.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.domain.model.Keyword
import com.ssverma.showtime.ui.common.Chip

@Composable
fun TagItem(keyword: Keyword, onTagClicked: () -> Unit) {
    Chip(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.24f),
        modifier = Modifier
            .clickable {
                onTagClicked()
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.87f)
            )
    ) {
        Text(
            text = "#${keyword.name}",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface
        )
    }
}