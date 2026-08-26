package com.ssverma.feature.library.domain

import com.ssverma.feature.library.domain.model.ReceiptItem
import com.ssverma.feature.library.domain.model.ReceiptSnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

object ReceiptGeneratorHelper {

    fun generateSnapshot(
        title: String,
        collectorName: String,
        items: List<ReceiptItem>,
        date: Date = Date()
    ): ReceiptSnapshot {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        val formattedDate = dateFormat.format(date).uppercase(Locale.US)

        val totalItems = items.size
        val totalMinutes =
            items.sumOf { if (it.runtimeMinutes > 0) it.runtimeMinutes else 115 } // standard feature length fallback

        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        val formattedTotalTime = if (hours > 0) {
            String.format(Locale.US, "%.1f HRS (%d MIN)", totalMinutes / 60.0, totalMinutes)
        } else {
            "$mins MIN"
        }

        val genreCounts = mutableMapOf<String, Int>()
        items.forEach { item ->
            item.genres.forEach { genre ->
                if (genre.isNotBlank()) {
                    genreCounts[genre] = genreCounts.getOrDefault(genre, 0) + 1
                }
            }
        }
        val topGenre = genreCounts.maxByOrNull { it.value }?.key?.uppercase(Locale.US) ?: "CINEMA"

        val receiptCode = SimpleDateFormat("yyyyMMdd", Locale.US).format(date)
        val hash = abs((items.hashCode() + collectorName.hashCode()) % 9000) + 1000
        val receiptNumber = "ST-$receiptCode-$hash"
        val barcodeNumber = "978${abs(items.hashCode() % 1000000000L).toString().padStart(9, '0')}"

        return ReceiptSnapshot(
            title = title,
            collectorName = collectorName.ifBlank { "SHOWTIME CINEPHILE" }.uppercase(Locale.US),
            formattedDate = formattedDate,
            receiptNumber = receiptNumber,
            items = items,
            totalItems = totalItems,
            totalMinutes = totalMinutes,
            formattedTotalTime = formattedTotalTime,
            topGenre = topGenre,
            barcodeNumber = barcodeNumber
        )
    }
}
