package com.ssverma.core.ui.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.FloatingBottomBarDefaults

@Composable
fun ShowTimeSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    floatingBottomBar: Boolean = false
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomPadding = if (floatingBottomBar) {
        FloatingBottomBarDefaults.Height + FloatingBottomBarDefaults.BottomMargin + 8.dp + bottomInset
    } else {
        16.dp
    }

    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(bottom = bottomPadding),
        snackbar = { snackbarData ->
            ShowTimeSnackbar(snackbarData = snackbarData)
        }
    )
}

@Composable
fun ShowTimeSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        actionColor = MaterialTheme.colorScheme.primary,
        dismissActionContentColor = MaterialTheme.colorScheme.inverseOnSurface
    )
}

suspend fun SnackbarHostState.showImmediateSnackbar(
    message: String,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = duration
    )
}
