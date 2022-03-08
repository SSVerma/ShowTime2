package com.ssverma.showtime.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.core.Failure
import com.ssverma.showtime.ui.FetchDataUiState

@Composable
fun <T> DriveCompose(
    observable: LiveData<Result<T>>,
    loading: @Composable () -> Unit = { DefaultLoadingIndicator() },
    error: @Composable (error: Result.Error<T>) -> Unit = { DefaultErrorView(error = it) },
    content: @Composable (data: T) -> Unit,
) {
    val result by observable.observeAsState(initial = Result.Loading())

    when (result) {
        is Result.Success -> {
            content((result as Result.Success<T>).data)
        }
        is Result.Loading -> {
            loading()
        }
        is Result.Error -> {
            error(result as Result.Error)
        }
    }
}

@Composable
fun DefaultLoadingIndicator(modifier: Modifier = Modifier) {
    return Box(modifier = modifier.fillMaxSize()) {
        AppLoadingIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun <T> DefaultErrorView(
    error: Result.Error<T>,
    modifier: Modifier = Modifier
) {

    return Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = error.displayMessage)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                error.retry?.invoke()
            }
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}

@Composable
fun <S, F> DriveCompose(
    uiState: FetchDataUiState<S, F>,
    loading: @Composable () -> Unit = { DefaultLoadingIndicator() },
    coreErrorContent: @Composable (error: Failure.CoreFailure) -> Unit = {
        DefaultCoreErrorView(error = it, onRetry = onRetry)
    },
    onRetry: () -> Unit = {},
    featureErrorContent: @Composable (error: Failure.FeatureFailure<F>) -> Unit = {},
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