package com.ssverma.showtime.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.LiveData
import com.ssverma.showtime.R
import com.ssverma.showtime.domain.Result

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
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = error.displayMessage)
        OutlinedButton(
            onClick = {
                error.retry?.invoke()
            }
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}