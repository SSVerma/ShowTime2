package com.ssverma.shared.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.layout.VerticalGrid
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.placeholderIfNullOrEmpty

data class Highlight(
    @StringRes val labelRes: Int,
    val value: String
)

@Composable
fun Highlights(
    highlights: List<Highlight>,
    modifier: Modifier = Modifier,
    columnCount: Int = 3,
    @StringRes absentPlaceholderRes: Int = R.string.na,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(24.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceAround
) {

    VerticalGrid(
        items = highlights,
        columnCount = columnCount,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
    ) { _, item ->
        HighlightItem(
            labelRes = item.labelRes,
            value = item.value.placeholderIfNullOrEmpty(placeholderRes = absentPlaceholderRes),
            modifier = Modifier
        )
    }

}

@Composable
fun HighlightItem(
    @StringRes labelRes: Int,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Emphasize {
            Text(text = stringResource(id = labelRes), style = MaterialTheme.typography.caption)
        }

        Text(
            text = value,
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}