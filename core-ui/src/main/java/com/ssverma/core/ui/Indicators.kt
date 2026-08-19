package com.ssverma.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.failure.Failure

@Composable
fun DefaultLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ShowTimeLoadingIndicator()
    }
}

@Composable
fun DefaultCoreErrorIndicator(
    failure: Failure.CoreFailure,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, messageRes) = when (failure) {
        Failure.CoreFailure.NetworkFailure -> Icons.Rounded.WifiOff to R.string.network_error
        Failure.CoreFailure.ServiceFailure -> Icons.Rounded.CloudOff to R.string.service_error
        Failure.CoreFailure.UnexpectedFailure -> Icons.Rounded.ErrorOutline to R.string.something_went_wrong
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .defaultMinSize(minHeight = 160.dp)
            .padding(MaterialTheme.spacing.large)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        Text(
            text = stringResource(id = messageRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        OutlinedButton(
            onClick = onRetry,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}
