package com.ssverma.shared.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.shared.ui.R

@Composable
fun HomePageAppBar(
    modifier: Modifier = Modifier,
    onSearchIconPressed: () -> Unit,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    TopAppBar(
        elevation = elevation,
        backgroundColor = backgroundColor,
        modifier = modifier.height(56.dp),
        contentColor = contentColor
    ) {
        Image(
            modifier = Modifier
                .padding(8.dp)
                .size(32.dp)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.ic_launcher),
            contentDescription = null
        )
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = onSearchIconPressed
        ) {
            Icon(
                imageVector = AppIcons.Search,
                contentDescription = stringResource(R.string.search)
            )
        }
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically),
            onClick = { /* todo */ }
        ) {
            Icon(
                imageVector = AppIcons.AccountCircle,
                contentDescription = stringResource(R.string.account)
            )
        }
    }
}