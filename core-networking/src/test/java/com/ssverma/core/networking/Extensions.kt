package com.ssverma.core.networking

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.buffer
import okio.source
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets

internal fun MockWebServer.enqueueResponse(
    fileName: String,
    httpCode: Int = HttpURLConnection.HTTP_OK,
    mockResponseBuilder: (MockResponse.() -> MockResponse)? = null
) {
    val inputStream = javaClass.classLoader?.getResourceAsStream("response/$fileName")

    val source = inputStream?.source()?.buffer() ?: return

    enqueue(
        MockResponse()
            .setResponseCode(httpCode)
            .setBody(source.readString(StandardCharsets.UTF_8))
            .apply { mockResponseBuilder?.invoke(this) }
    )
}