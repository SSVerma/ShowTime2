package com.ssverma.shared.ui.component.section

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.Cast
import com.ssverma.core.ui.layout.HorizontalLazyList
import com.ssverma.core.ui.layout.Section
import com.ssverma.core.ui.layout.SectionHeader
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.Avatar

@Composable
fun CreditSection(
    casts: List<Cast>,
    onPersonClick: (personId: Int) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes titleRes: Int = R.string.casts,
) {
    Section(
        sectionHeader = {
            SectionHeader(
                title = stringResource(id = titleRes),
                modifier = Modifier.padding(horizontal = 16.dp),
                hideTrailingAction = true
            )
        },
        headerContentSpacing = SectionDefaults.SectionContentHeaderSpacing,
        hideIf = casts.isEmpty(),
        modifier = modifier
    ) {
        HorizontalLazyList(items = casts) {
            CastItem(
                cast = it,
                onClick = {
                    onPersonClick(it.id)
                },
                modifier = Modifier.width(104.dp)
            )
        }
    }
}

@Composable
fun CastItem(
    cast: Cast,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Avatar(
            imageUrl = cast.avatarImageUrl,
            onClick = onClick,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = cast.name,
            style = MaterialTheme.typography.body1,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = cast.character,
            style = MaterialTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}