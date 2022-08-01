package com.ssverma.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.feature.auth.R
import com.ssverma.shared.ui.component.Avatar

@Composable
fun LoginContent(
    onTmdbConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        Avatar(imageUrl = "", onClick = {}, modifier = Modifier.size(72.dp))
        Text(
            text = stringResource(R.string.tmdb_connect_info),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = ContentSpacing)
        )

        Button(
            onClick = onTmdbConnectClick,
            modifier = Modifier.padding(top = ContentSpacing)
        ) {
            Text(text = stringResource(R.string.connect_with_tmdb))
        }
    }
}

private val ContentSpacing = 12.dp