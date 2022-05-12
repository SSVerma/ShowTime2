package com.ssverma.showtime.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.failure.Failure
import com.ssverma.showtime.ui.FetchDataUiState

@Composable
fun DefaultLoadingIndicator(modifier: Modifier = Modifier) {
    return Box(modifier = modifier.fillMaxSize()) {
        AppLoadingIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun <S, FeatureFailure> DriveCompose(
    uiState: FetchDataUiState<S, FeatureFailure>,
    loading: @Composable () -> Unit = { DefaultLoadingIndicator() },
    coreErrorContent: @Composable (error: Failure.CoreFailure) -> Unit = {
        DefaultCoreErrorView(error = it, onRetry = onRetry)
    },
    onRetry: () -> Unit = {},
    featureErrorContent: @Composable (error: Failure.FeatureFailure<FeatureFailure>) -> Unit = {},
    idleContent: @Composable () -> Unit = {},
    content: @Composable (data: S) -> Unit,
) {
    when (uiState) {
        is FetchDataUiState.Idle -> {
            idleContent()
        }
        is FetchDataUiState.Error -> {
            when (val errorResult = uiState.failure) {
                Failure.CoreFailure.NetworkFailure,
                Failure.CoreFailure.ServiceFailure,
                Failure.CoreFailure.UnexpectedFailure -> {
                    coreErrorContent(errorResult as Failure.CoreFailure)
                }
                is Failure.FeatureFailure -> {
                    featureErrorContent(errorResult)
                }
            }
        }
        FetchDataUiState.Loading -> {
            loading()
        }
        is FetchDataUiState.Success -> {
            content(uiState.data)
        }
    }
}

@Composable
private fun DefaultCoreErrorView(
    error: Failure.CoreFailure,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {

    //TODO: use [error] to create separate screens based on the error context

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = stringResource(id = R.string.something_went_wrong))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(R.string.retry))
        }
    }
}