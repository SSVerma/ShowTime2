package com.ssverma.feature.search.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.feature.search.domain.model.SearchSuggestion
import com.ssverma.feature.search.ui.common.SearchSuggestionDefaults
import com.ssverma.shared.domain.model.Gender
import com.ssverma.shared.ui.component.Avatar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchPersonItem(
    person: SearchSuggestion.Person,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colors.background,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    vertical = SearchSuggestionDefaults.VerticalPadding,
                    horizontal = 12.dp
                )
        ) {
            Avatar(
                imageUrl = person.imageUrl,
                onClick = {},
                borderWidth = 1.dp,
                borderSpacing = 2.dp,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = person.name,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
        }
    }
}

@Preview
@Composable
private fun SearchPersonItemPreview() {
    SearchPersonItem(
        person = SearchSuggestion.Person(
            id = 1,
            name = "Mr Stark",
            imageUrl = "/stTEycfG9928HYGEISBFaG1ngjM.jpg".convertToFullTmdbImageUrl(),
            department = "acting",
            gender = Gender.Male,
            popularity = 9f
        ),
        onClick = {}
    )
}