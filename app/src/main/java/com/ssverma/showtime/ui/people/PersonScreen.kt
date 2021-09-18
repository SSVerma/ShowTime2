package com.ssverma.showtime.ui.people

import TmdbPosterAspectRatio
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.insets.statusBarsPadding
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.model.Person
import com.ssverma.showtime.ui.common.NetworkImage
import com.ssverma.showtime.ui.common.PagedContent
import com.ssverma.showtime.ui.common.PagedListIndexed
import com.ssverma.showtime.ui.home.HomePageAppBar
import com.ssverma.showtime.ui.home.HomeViewModel

@Composable
fun PersonScreen(
    viewModel: HomeViewModel,
    openPersonDetailsScreen: (personId: Int) -> Unit
) {
    val pagedPersons = viewModel.popularPersons.collectAsLazyPagingItems()

    Column(modifier = Modifier.statusBarsPadding()) {
        HomePageAppBar(backgroundColor = MaterialTheme.colors.background)

        PagedContent(pagingItems = pagedPersons) {
            PagedListIndexed(
                pagingItems = it,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) { index, person ->
                PersonItem(
                    person = person,
                    index = index,
                    onClick = {
                        openPersonDetailsScreen(person.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun PersonItem(
    person: Person,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Max)
        .clickable { onClick() }
        .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "${index + 1}",
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(16.dp))

        NetworkImage(
            url = person.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .width(96.dp)
                .aspectRatio(TmdbPosterAspectRatio)
                .clip(MaterialTheme.shapes.medium.copy(CornerSize(8.dp)))
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text = person.name, style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    id = R.string.gender_n,
                    stringResource(id = person.gender.displayGenderRes)
                ),
                style = MaterialTheme.typography.caption
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.known_for_n, person.knownFor),
                style = MaterialTheme.typography.caption
            )

            Box(modifier = Modifier.weight(1f))

            TextButton(onClick = { /*TODO*/ }, contentPadding = PaddingValues(horizontal = 0.dp)) {
                Text(text = stringResource(id = R.string.popular_media))
            }
        }
    }
}