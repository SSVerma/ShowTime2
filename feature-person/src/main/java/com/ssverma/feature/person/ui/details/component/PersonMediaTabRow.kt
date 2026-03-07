package com.ssverma.feature.person.ui.details.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ssverma.core.ui.UiText
import com.ssverma.core.ui.asString
import com.ssverma.feature.person.R
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.person.PersonMedia

@Composable
fun PersonMediaTabRow(
    personMediaByType: Map<MediaType, List<PersonMedia>>,
    selectedMediaType: MediaType,
    onMediaTypeSelected: (MediaType) -> Unit,
    modifier: Modifier = Modifier
) {
    val mediaTypes = personMediaByType.keys.toList()
    val selectedIndex = mediaTypes.indexOf(selectedMediaType).coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.background,
        divider = {},
        modifier = modifier.fillMaxWidth()
    ) {
        mediaTypes.forEach { mediaType ->
            val count = personMediaByType[mediaType]?.size ?: 0
            Tab(
                text = {
                    Text(
                        text = mediaType.asUiText().asString() + " ($count)",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                selected = selectedMediaType == mediaType,
                onClick = { onMediaTypeSelected(mediaType) },
            )
        }
    }
}

fun MediaType.asUiText(): UiText.StaticText {
    return when (this) {
        MediaType.Movie -> UiText.StaticText(resId = R.string.movie)
        MediaType.Tv -> UiText.StaticText(resId = R.string.tv)
        else -> UiText.StaticText(resId = R.string.unknown)
    }
}
