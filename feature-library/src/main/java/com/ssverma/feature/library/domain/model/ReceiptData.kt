package com.ssverma.feature.library.domain.model

enum class ReceiptStyle {
    THERMAL,
    GOLDEN_PASS,
    CYBERPUNK
}

enum class ReceiptSource {
    HISTORY,
    FAVORITES,
    WATCHLIST,
    THIS_MONTH
}

data class ReceiptItem(
    val id: Int,
    val title: String,
    val year: String,
    val runtimeMinutes: Int,
    val rating: Float = 0f,
    val genres: List<String> = emptyList(),
    val formattedDate: String = ""
)

data class ReceiptSnapshot(
    val title: String,
    val collectorName: String,
    val formattedDate: String,
    val receiptNumber: String,
    val items: List<ReceiptItem>,
    val totalItems: Int,
    val totalMinutes: Int,
    val formattedTotalTime: String,
    val topGenre: String,
    val barcodeNumber: String
)
