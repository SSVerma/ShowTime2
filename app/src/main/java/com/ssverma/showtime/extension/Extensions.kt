package com.ssverma.showtime.extension

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ssverma.showtime.domain.ApiData
import com.ssverma.showtime.domain.Result
import com.ssverma.showtime.domain.mapper.ResultMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

fun <R, D> Flow<Result<ApiData<R>>>.asDomainFlow(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
    mapApiData: suspend (input: ApiData<R>) -> D
): Flow<Result<D>> {
    val mapper = object : ResultMapper<ApiData<R>, D>(coroutineDispatcher) {
        override suspend fun mapValue(input: ApiData<R>): D {
            return mapApiData(input)
        }
    }

    return map { mapper.map(it) }.flowOn(coroutineDispatcher)
}

fun String?.emptyIfNull(): String {
    return this ?: ""
}

fun String?.placeholderIfNullOrEmpty(placeholder: String): String {
    if (this.isNullOrEmpty()) {
        return placeholder
    }
    return this
}

@Composable
fun String?.placeholderIfNullOrEmpty(@StringRes placeholderRes: Int): String {
    return placeholderIfNullOrEmpty(stringResource(id = placeholderRes))
}

fun Float.emptyIfAbsent(): String {
    return if (this == 0F) "" else this.toString()
}

fun Long.emptyIfAbsent(): String {
    return if (this == 0L) "" else this.toString()
}

fun Int.emptyIfAbsent(): String {
    return if (this == 0) "" else this.toString()
}
