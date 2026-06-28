package com.ssverma.core.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.component.ShowTimeTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen(
    title: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        Column {
            ShowTimeTopAppBar(title = title, onBackPressed = onBackPressed)
            content()
        }
    }
}

@Composable
fun ScreenLoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        ShowTimeLoadingIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun ScreenErrorIndicator(
    modifier: Modifier = Modifier,
    errorMessage: String = stringResource(id = R.string.something_went_wrong),
    onRetryClick: () -> Unit
) {
    ScreenErrorIndicator(
        modifier = modifier,
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = errorMessage)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = onRetryClick) {
                    Text(text = stringResource(R.string.retry))
                }
            }
        }
    )
}

@Composable
fun ScreenErrorIndicator(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        content()
    }
}

@Composable
fun ScreenEmptyIndicator(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        content()
    }
}

@Composable
fun ScreenEmptyIndicator(
    modifier: Modifier = Modifier,
    emptyMessage: String = stringResource(id = R.string.no_data),
) {
    ScreenEmptyIndicator(modifier = modifier) {
        Text(
            text = emptyMessage,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.54f),
                    shape = MaterialTheme.shapes.medium.copy(CornerSize(8.dp))
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
