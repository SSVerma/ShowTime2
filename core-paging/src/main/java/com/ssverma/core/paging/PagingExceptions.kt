package com.ssverma.core.paging

sealed class PagingFailure : Exception() {
    object ClientFailure : PagingFailure()
    object NetworkFailure : PagingFailure()
    object ServiceFailure : PagingFailure()
    object UnexpectedFailure : PagingFailure()
}