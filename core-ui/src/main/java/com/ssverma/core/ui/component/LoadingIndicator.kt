package com.ssverma.core.ui.component

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShowTimeLoadingIndicator(modifier: Modifier = Modifier) {
    LoadingIndicator(modifier = modifier)
}
