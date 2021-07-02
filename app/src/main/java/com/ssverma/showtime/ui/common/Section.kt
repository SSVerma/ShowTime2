package com.ssverma.showtime.ui.common

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.showtime.R

@Composable
fun SectionHeader(
    titleContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitleContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        leadingContent?.let { it() }

        Column(modifier = Modifier.weight(1f)) {
            titleContent()
            subtitleContent?.let { it() }
        }

        trailingContent?.let { it() }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIconUrl: String? = null,
    onTrailingActionClicked: () -> Unit = {},
    hideTrailingAction: Boolean = false,
    @StringRes trailingActionTextRes: Int = R.string.see_all,
    textColor: Color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
) {

    SectionHeader(
        modifier = modifier,
        titleContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.SemiBold),
                color = textColor,
                modifier = Modifier.fillMaxWidth()
            )
        },
        subtitleContent = {
            subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.body1.copy(fontSize = 12.sp),
                )
            }
        },
        leadingContent = {
            if (!leadingIconUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    NetworkImage(
                        url = leadingIconUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        },
        trailingContent = {
            if (!hideTrailingAction) {
                TextButton(onClick = onTrailingActionClicked) {
                    Text(text = stringResource(id = trailingActionTextRes))
                }
            }
        }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Section(
    modifier: Modifier = Modifier,
    hideIf: Boolean = false,
    sectionHeader: @Composable () -> Unit,
    headerContentSpacing: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(visible = !hideIf) {
        Column(modifier = modifier) {
            sectionHeader()
            Spacer(modifier = Modifier.height(headerContentSpacing))
            content()
        }
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
    items: List<T>,
    contentPadding: PaddingValues = SectionListContentPadding,
    horizontalArrangement: Arrangement.Horizontal = SectionListHorizontalArrangement,
    itemContent: @Composable (item: T) -> Unit
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