package com.ssverma.showtime.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.LiveData
import com.ssverma.showtime.R
import com.ssverma.showtime.api.ApiResource
import com.ssverma.showtime.api.Resource

@Composable
fun <T> DriveCompose(
    observable: LiveData<Resource<T>>,
    content: @Composable (data: Resource.Success<T>) -> Unit,
    loading: @Composable () -> Unit,
    error: @Composable (error: Resource.Error<T>) -> Unit,
) {
    val resource by observable.observeAsState(initial = Resource.Loading())

    when (resource) {
        is Resource.Success -> {
            content(resource as Resource.Success<T>)
        }
        is Resource.Loading -> {
            loading()
        }
        is Resource.Error -> {
            error(resource as Resource.Error<T>)
        }
    }
}

@Composable
fun <T> NetworkCompose(
    observable: LiveData<Resource<T>>,
    loading: @Composable () -> Unit = { DefaultLoadingIndicator() },
    error: @Composable (error: ApiResource.Error<T, *>) -> Unit = { DefaultErrorView(error = it) },
    content: @Composable (data: ApiResource.Success<T>) -> Unit,
) {
    val resource by observable.observeAsState(initial = Resource.Loading())

    when (resource) {
        is Resource.Success -> {
            content(resource as ApiResource.Success<T>)
        }
        is Resource.Loading -> {
            loading()
        }
        is Resource.Error -> {
            error(resource as ApiResource.Error<T, *>)
        }
    }
}

@Composable
fun <T, D> NetworkCompose(
    observable: LiveData<Resource<T>>,
    mapper: (T) -> D,
    loading: @Composable () -> Unit = { DefaultLoadingIndicator() },
    error: @Composable (error: ApiResource.Error<T, *>) -> Unit = { DefaultErrorView(error = it) },
    content: @Composable (data: ApiResource.Success<T>, D) -> Unit,
) {
    val resource by observable.observeAsState(initial = Resource.Loading())

    when (resource) {
        is Resource.Success -> {
            val succeededRes = resource as ApiResource.Success<T>
            content(succeededRes, remember(succeededRes.data) { mapper(succeededRes.data) })
        }
        is Resource.Loading -> {
            loading()
        }
        is Resource.Error -> {
            error(resource as ApiResource.Error<T, *>)
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
    error: ApiResource.Error<T, *>,
    modifier: Modifier = Modifier
) {

    return Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = error.displayErrorMessage)
        OutlinedButton(
            onClick = {
                error.retry?.invoke()
            }
        ) {
            Text(text = stringResource(R.string.retry))
        }
    }
}