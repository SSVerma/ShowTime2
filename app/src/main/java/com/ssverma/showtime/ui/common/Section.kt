package com.ssverma.showtime.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.R

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    avatarUrl: String? = null,
    onTrailingActionClicked: (() -> Unit)? = null,
    @StringRes trailingActionTextRes: Int = R.string.see_all,
    textColor: Color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        if (!avatarUrl.isNullOrEmpty()) {
            NetworkImage(
                url = avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.subtitle1,
                    color = textColor
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
        onTrailingActionClicked?.let {
            TextButton(onClick = it, modifier = Modifier.padding(end = 8.dp)) {
                Text(text = stringResource(id = trailingActionTextRes))
            }
        }
    }
}

@Composable
fun Section(
    modifier: Modifier = Modifier,
    sectionHeader: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        sectionHeader()
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
fun SectionLoadingIndicator() {
    DefaultLoadingIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun <T> HorizontalListSection(
    modifier: Modifier = Modifier,
    sectionHeader: @Composable () -> Unit,
    itemContent: @Composable (item: T) -> Unit,
    contentPadding: PaddingValues = SectionListContentPadding,
    horizontalArrangement: Arrangement.Horizontal = SectionListHorizontalArrangement,
    items: List<T>
) = Section(sectionHeader = sectionHeader, modifier = modifier) {
    HorizontalList(
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        itemContent = itemContent,
        items = items
    )
}

@Composable
fun <T> HorizontalList(
    itemContent: @Composable (item: T) -> Unit,
    contentPadding: PaddingValues = SectionListContentPadding,
    horizontalArrangement: Arrangement.Horizontal = SectionListHorizontalArrangement,
    items: List<T>
) {
    LazyRow(
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        content = {
            items(items) { item ->
                itemContent(item)
            }
        }
    )
}

private val SectionListContentPadding = PaddingValues(start = 16.dp, end = 16.dp)
private val SectionListHorizontalArrangement = Arrangement.spacedBy(16.dp)