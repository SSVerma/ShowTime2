package com.ssverma.feature.library.domain

import com.google.common.truth.Truth.assertThat
import com.ssverma.feature.library.domain.model.ReceiptItem
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class ReceiptGeneratorHelperTest {

    @Test
    fun `generateSnapshot computes stats and runtime correctly`() {
        val calendar = Calendar.getInstance(Locale.US).apply {
            set(2026, Calendar.AUGUST, 20)
        }
        val items = listOf(
            ReceiptItem(
                id = 1,
                title = "Dune: Part Two",
                year = "2024",
                runtimeMinutes = 166,
                rating = 8.5f,
                genres = listOf("Sci-Fi", "Adventure")
            ),
            ReceiptItem(
                id = 2,
                title = "Interstellar",
                year = "2014",
                runtimeMinutes = 169,
                rating = 8.7f,
                genres = listOf("Sci-Fi", "Drama")
            ),
            ReceiptItem(
                id = 3,
                title = "Blade Runner 2049",
                year = "2017",
                runtimeMinutes = 164,
                rating = 8.0f,
                genres = listOf("Sci-Fi", "Mystery")
            )
        )

        val snapshot = ReceiptGeneratorHelper.generateSnapshot(
            title = "Sci-Fi Marathon",
            collectorName = "Alex",
            items = items,
            date = calendar.time
        )

        assertThat(snapshot.title).isEqualTo("Sci-Fi Marathon")
        assertThat(snapshot.collectorName).isEqualTo("ALEX")
        assertThat(snapshot.totalItems).isEqualTo(3)
        assertThat(snapshot.totalMinutes).isEqualTo(499)
        assertThat(snapshot.topGenre).isEqualTo("SCI-FI")
        assertThat(snapshot.formattedTotalTime).contains("8.3 HRS")
        assertThat(snapshot.formattedTotalTime).contains("499 MIN")
        assertThat(snapshot.receiptNumber).startsWith("ST-20260820-")
        assertThat(snapshot.barcodeNumber).startsWith("978")
    }

    @Test
    fun `generateSnapshot falls back to standard duration and default genre when missing`() {
        val items = listOf(
            ReceiptItem(
                id = 10,
                title = "Unknown Indie Film",
                year = "2026",
                runtimeMinutes = 0,
                rating = 7.0f,
                genres = emptyList()
            )
        )

        val snapshot = ReceiptGeneratorHelper.generateSnapshot(
            title = "Watchlist",
            collectorName = "",
            items = items
        )

        assertThat(snapshot.totalItems).isEqualTo(1)
        assertThat(snapshot.totalMinutes).isEqualTo(115) // fallback default
        assertThat(snapshot.topGenre).isEqualTo("CINEMA") // fallback genre
        assertThat(snapshot.collectorName).isEqualTo("SHOWTIME CINEPHILE")
    }
}
