package com.ssverma.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.ScreenLoadingIndicator
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.feature.auth.R

@Composable
fun ApprovalAskedContent(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.redirecting),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun ApprovalGrantedContent(
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.authorizing),
            style = MaterialTheme.typography.body1
        )
        Spacer(modifier = Modifier.height(16.dp))
        ShowTimeLoadingIndicator()
    }
}

@Composable
fun ApprovalRejectedContent(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.approval_rejection_info),
            style = MaterialTheme.typography.body1
        )
    }
}