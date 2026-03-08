package com.ssverma.shared.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageAppBar(
    onSearchIconPressed: () -> Unit,
    onAccountIconPressed: () -> Unit,
    modifier: Modifier = Modifier,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(32.dp),
                    painter = painterResource(id = R.drawable.ic_launcher),
                    contentDescription = null
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        actions = {
            ThemeToggleButton()
            IconButton(onClick = onSearchIconPressed) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }
            IconButton(onClick = onAccountIconPressed) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = stringResource(R.string.account)
                )
            }
        },
        colors = colors,
        modifier = modifier
    )
}
