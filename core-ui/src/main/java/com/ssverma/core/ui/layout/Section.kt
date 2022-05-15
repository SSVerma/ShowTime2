package com.ssverma.core.ui.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.R
import com.ssverma.core.ui.ScreenLoadingIndicator
import com.ssverma.core.ui.image.NetworkImage

@Composable
fun SectionHeader(
    titleContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    subtitleContent: (@Composable ColumnScope.() -> Unit)? = null,
    leadingContent: (@Composable RowScope.() -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null
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
    titleTextStyle: TextStyle = SectionDefaults.titleTextStyle()
) {
    SectionHeader(
        titleContent = {
            SectionTitle(title = title, textStyle = titleTextStyle)
        },
        modifier = modifier
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    titleTextStyle: TextStyle = SectionDefaults.titleTextStyle(),
    subtitleTextStyle: TextStyle = SectionDefaults.subtitleTextStyle(),
) {
    SectionHeader(
        titleContent = {
            SectionTitle(
                title = title,
                textStyle = titleTextStyle,
                modifier = Modifier.fillMaxWidth()
            )
        },
        subtitleContent = {
            SectionSubtitle(subtitle = subtitle, textStyle = subtitleTextStyle)
        },
        modifier = modifier
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    leadingIconUrl: String,
    modifier: Modifier = Modifier,
    leadingIconSize: Dp = SectionDefaults.leadingIconSize,
    leadingIconEndSpacing: Dp = SectionDefaults.leadingIconEndSpacing,
    titleTextStyle: TextStyle = SectionDefaults.titleTextStyle(),
    subtitleTextStyle: TextStyle = SectionDefaults.subtitleTextStyle(),
) {
    SectionHeader(
        titleContent = {
            SectionTitle(
                title = title,
                textStyle = titleTextStyle,
                modifier = Modifier.fillMaxWidth()
            )
        },
        subtitleContent = {
            SectionSubtitle(subtitle = subtitle, textStyle = subtitleTextStyle)
        },
        leadingContent = {
            SectionLeadingIcon(
                leadingIconUrl = leadingIconUrl,
                leadingIconSize = leadingIconSize,
                modifier = Modifier.padding(end = leadingIconEndSpacing)
            )
        },
        modifier = modifier
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    leadingIconUrl: String? = null,
    leadingIconSize: Dp = SectionDefaults.leadingIconSize,
    leadingIconEndSpacing: Dp = SectionDefaults.leadingIconEndSpacing,
    trailingActionLabel: String = stringResource(id = R.string.see_all),
    onTrailingActionClicked: () -> Unit,
    titleTextStyle: TextStyle = SectionDefaults.titleTextStyle(),
    subtitleTextStyle: TextStyle = SectionDefaults.subtitleTextStyle(),
) {
    SectionHeader(
        titleContent = {
            SectionTitle(
                title = title,
                textStyle = titleTextStyle,
                modifier = Modifier.fillMaxWidth()
            )
        },
        subtitleContent = {
            SectionSubtitle(subtitle = subtitle, textStyle = subtitleTextStyle)
        },
        leadingContent = {
            if (!leadingIconUrl.isNullOrEmpty()) {
                SectionLeadingIcon(
                    leadingIconUrl = leadingIconUrl,
                    leadingIconSize = leadingIconSize,
                    modifier = Modifier.padding(end = leadingIconEndSpacing)
                )
            }
        },
        trailingContent = {
            SectionTrailingAction(
                actionLabel = trailingActionLabel,
                onTrailingActionClicked = onTrailingActionClicked
            )
        },
        modifier = modifier
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIconUrl: String? = null,
    leadingIconSize: Dp = SectionDefaults.leadingIconSize,
    leadingIconEndSpacing: Dp = SectionDefaults.leadingIconEndSpacing,
    trailingActionLabel: String = stringResource(id = R.string.see_all),
    hideTrailingAction: Boolean = false,
    onTrailingActionClicked: () -> Unit = {},
    titleTextStyle: TextStyle = SectionDefaults.titleTextStyle(),
    subtitleTextStyle: TextStyle = SectionDefaults.subtitleTextStyle(),
) {

    SectionHeader(
        modifier = modifier,
        titleContent = {
            SectionTitle(
                title = title,
                textStyle = titleTextStyle,
                modifier = Modifier.fillMaxWidth()
            )
        },
        subtitleContent = {
            subtitle?.let { subtitle ->
                SectionSubtitle(subtitle = subtitle, textStyle = subtitleTextStyle)
            }
        },
        leadingContent = {
            if (!leadingIconUrl.isNullOrEmpty()) {
                SectionLeadingIcon(
                    leadingIconUrl = leadingIconUrl,
                    leadingIconSize = leadingIconSize,
                    modifier = Modifier.padding(end = leadingIconEndSpacing)
                )
            }
        },
        trailingContent = {
            if (!hideTrailingAction) {
                SectionTrailingAction(
                    actionLabel = trailingActionLabel,
                    onTrailingActionClicked = onTrailingActionClicked
                )
            }
        }
    )
}

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
    ScreenLoadingIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun <T> HorizontalLazyListSection(
    items: List<T>,
    sectionHeader: @Composable () -> Unit,
    itemContent: @Composable (item: T) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = SectionListContentPadding,
    horizontalArrangement: Arrangement.Horizontal = SectionListHorizontalArrangement,
) = Section(sectionHeader = sectionHeader, modifier = modifier) {
    HorizontalLazyList(
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        itemContent = itemContent,
        items = items
    )
}

@Composable
fun <T> HorizontalLazyListSection(
    items: List<T>,
    sectionHeader: @Composable () -> Unit,
    itemContent: @Composable (item: T) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = SectionListContentPadding,
    horizontalArrangement: Arrangement.Horizontal = SectionListHorizontalArrangement,
    hideIf: Boolean = false
) {
    AnimatedVisibility(visible = !hideIf, modifier = modifier) {
        Section(sectionHeader = sectionHeader) {
            HorizontalLazyList(
                contentPadding = contentPadding,
                horizontalArrangement = horizontalArrangement,
                itemContent = itemContent,
                items = items
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = textStyle,
        modifier = modifier
    )
}

@Composable
private fun SectionSubtitle(
    subtitle: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = subtitle,
        style = textStyle,
        modifier = modifier
    )
}

@Composable
private fun SectionLeadingIcon(
    leadingIconUrl: String,
    leadingIconSize: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(leadingIconSize)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        NetworkImage(
            url = leadingIconUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun SectionTrailingAction(
    actionLabel: String,
    onTrailingActionClicked: () -> Unit
) {
    TextButton(onClick = onTrailingActionClicked) {
        Text(text = actionLabel)
    }
}

object SectionDefaults {
    val leadingIconSize = 40.dp
    val leadingIconEndSpacing = 16.dp

    @Composable
    fun titleTextStyle(): TextStyle {
        return MaterialTheme.typography.h6.copy(
            fontWeight = FontWeight.SemiBold,
            color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
        )
    }

    @Composable
    fun subtitleTextStyle(): TextStyle {
        return MaterialTheme.typography.body1.copy(
            fontSize = 12.sp
        )
    }
}

private val SectionListContentPadding = PaddingValues(start = 16.dp, end = 16.dp)
private val SectionListHorizontalArrangement = Arrangement.spacedBy(16.dp)