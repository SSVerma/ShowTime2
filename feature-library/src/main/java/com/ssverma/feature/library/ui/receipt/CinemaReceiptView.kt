package com.ssverma.feature.library.ui.receipt

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.feature.library.R
import com.ssverma.feature.library.domain.model.ReceiptSnapshot
import com.ssverma.feature.library.domain.model.ReceiptStyle

@Composable
fun CinemaReceiptView(
    snapshot: ReceiptSnapshot,
    style: ReceiptStyle,
    modifier: Modifier = Modifier
) {
    when (style) {
        ReceiptStyle.THERMAL -> ThermalReceiptCard(snapshot = snapshot, modifier = modifier)
        ReceiptStyle.GOLDEN_PASS -> GoldenVipPassCard(snapshot = snapshot, modifier = modifier)
        ReceiptStyle.CYBERPUNK -> CyberpunkNeonCard(snapshot = snapshot, modifier = modifier)
    }
}

/* -------------------------------------------------------------------------- */
/*  1. CLASSIC THERMAL RECEIPT                                                */
/* -------------------------------------------------------------------------- */

@Composable
private fun ThermalReceiptCard(
    snapshot: ReceiptSnapshot,
    modifier: Modifier = Modifier
) {
    val paperColor = Color(0xFFF9F8F3)
    val inkColor = Color(0xFF1E1E1E)
    val secondaryInk = Color(0xFF4A4A4A)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(paperColor)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Jagged top edge
        JaggedEdgeDivider(color = paperColor)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.receipt_showtime_cinema),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = 2.sp,
            color = inkColor,
            textAlign = TextAlign.Center
        )

        Text(
            text = "═".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = inkColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        ReceiptMetaRow(label = stringResource(R.string.receipt_collector), value = snapshot.collectorName, color = inkColor)
        ReceiptMetaRow(label = "DATE", value = snapshot.formattedDate, color = inkColor)
        ReceiptMetaRow(label = "REC #", value = snapshot.receiptNumber, color = inkColor)
        ReceiptMetaRow(label = "SECTION", value = snapshot.title.uppercase(), color = inkColor)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "─".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = secondaryInk
        )

        // Column headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "QTY  TITLE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = inkColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "TIME",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = inkColor
            )
        }

        Text(
            text = "─".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = secondaryInk
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Items
        snapshot.items.take(15).forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%02d  %s", index + 1, item.title),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = inkColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${item.runtimeMinutes}M",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = inkColor
                )
            }
        }

        if (snapshot.items.size > 15) {
            Text(
                text = "... +${snapshot.items.size - 15} MORE TITLES",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = secondaryInk,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "═".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = inkColor
        )

        // Summary Stats
        ReceiptSummaryRow(label = stringResource(R.string.receipt_total_movies), value = "${snapshot.totalItems}", color = inkColor)
        ReceiptSummaryRow(label = stringResource(R.string.receipt_total_time), value = snapshot.formattedTotalTime, color = inkColor)
        ReceiptSummaryRow(label = stringResource(R.string.receipt_top_genre), value = snapshot.topGenre, color = inkColor)
        ReceiptSummaryRow(label = stringResource(R.string.receipt_total_bill), value = stringResource(R.string.receipt_priceless), color = inkColor)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "═".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = inkColor
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.receipt_thank_you),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = inkColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Barcode Canvas
        BarcodeCanvas(
            barcodeNumber = snapshot.barcodeNumber,
            color = inkColor,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(36.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = snapshot.barcodeNumber,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = secondaryInk
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ShowTime • Track & Share your Cinema Journey",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = secondaryInk
        )
    }
}

/* -------------------------------------------------------------------------- */
/*  2. GOLDEN VIP CINEMA PASS                                                 */
/* -------------------------------------------------------------------------- */

@Composable
private fun GoldenVipPassCard(
    snapshot: ReceiptSnapshot,
    modifier: Modifier = Modifier
) {
    val goldGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2A210D),
            Color(0xFF191409),
            Color(0xFF100D05)
        )
    )
    val goldAccent = Color(0xFFFFD54F)
    val goldLight = Color(0xFFFFF8E1)
    val goldMuted = Color(0xFFC5A859)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(goldGradient)
            .border(1.5.dp, goldAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // VIP Header Badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = goldAccent.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, goldAccent.copy(alpha = 0.5f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = goldAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.receipt_vip_pass),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = goldAccent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = goldAccent,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = snapshot.collectorName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = goldLight,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${snapshot.title} • ${snapshot.formattedDate}",
            fontSize = 11.sp,
            color = goldMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Perforated Dashed Line
        DashedDivider(color = goldAccent.copy(alpha = 0.4f))

        Spacer(modifier = Modifier.height(14.dp))

        // 3 Key Stats Box
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TicketStatBox(label = "TITLES", value = "${snapshot.totalItems}", accentColor = goldAccent, textColor = goldLight)
            TicketStatBox(label = "RUNTIME", value = snapshot.formattedTotalTime.substringBefore(" ("), accentColor = goldAccent, textColor = goldLight)
            TicketStatBox(label = "GENRE", value = snapshot.topGenre.take(10), accentColor = goldAccent, textColor = goldLight)
        }

        Spacer(modifier = Modifier.height(14.dp))

        DashedDivider(color = goldAccent.copy(alpha = 0.4f))

        Spacer(modifier = Modifier.height(12.dp))

        // Featured Titles list
        Column(modifier = Modifier.fillMaxWidth()) {
            snapshot.items.take(8).forEachIndexed { idx, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "• ${item.title}",
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = goldLight.copy(alpha = 0.85f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${item.runtimeMinutes} min",
                        fontSize = 11.sp,
                        color = goldMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Barcode & Stamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarcodeCanvas(
                barcodeNumber = snapshot.barcodeNumber,
                color = goldAccent,
                modifier = Modifier
                    .width(160.dp)
                    .height(30.dp)
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, goldAccent)
            ) {
                Text(
                    text = stringResource(R.string.receipt_admit_one),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = goldAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ShowTime App • VIP Cinephile Pass",
            fontSize = 9.sp,
            color = goldMuted.copy(alpha = 0.6f)
        )
    }
}

/* -------------------------------------------------------------------------- */
/*  3. CYBERPUNK NEON CARD                                                    */
/* -------------------------------------------------------------------------- */

@Composable
private fun CyberpunkNeonCard(
    snapshot: ReceiptSnapshot,
    modifier: Modifier = Modifier
) {
    val neonCyan = Color(0xFF00E5FF)
    val neonAmber = Color(0xFFFF6D00)
    val neonBg = Color(0xFF07090E)
    val textWhite = Color(0xFFECEFF1)
    val textMuted = Color(0xFF78909C)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(neonBg)
            .border(1.5.dp, neonCyan.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ SHOWTIME HUD",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 2.sp,
                color = neonCyan
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = neonAmber.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, neonAmber)
            ) {
                Text(
                    text = "SYS:ONLINE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = neonAmber,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "═".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = neonCyan.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        ReceiptMetaRow(label = "USER", value = snapshot.collectorName, color = textWhite)
        ReceiptMetaRow(label = "DATE", value = snapshot.formattedDate, color = textWhite)
        ReceiptMetaRow(label = "CORE", value = snapshot.topGenre, color = neonAmber)

        Spacer(modifier = Modifier.height(8.dp))

        // Items Matrix
        Column(modifier = Modifier.fillMaxWidth()) {
            snapshot.items.take(10).forEachIndexed { idx, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "> ${item.title}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textWhite,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${item.runtimeMinutes}M",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = neonCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "─".repeat(32),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = neonCyan.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        ReceiptSummaryRow(label = "DATABASE RECORDS", value = "${snapshot.totalItems} TITLES", color = textWhite)
        ReceiptSummaryRow(label = "CHRONO RUNTIME", value = snapshot.formattedTotalTime, color = textWhite)
        ReceiptSummaryRow(label = "AFFINITY INDEX", value = snapshot.topGenre, color = neonAmber)

        Spacer(modifier = Modifier.height(14.dp))

        BarcodeCanvas(
            barcodeNumber = snapshot.barcodeNumber,
            color = neonCyan,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(30.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "NEO-SHOWTIME • ShowTime App",
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = textMuted
        )
    }
}

/* -------------------------------------------------------------------------- */
/*  HELPER UI ELEMENTS                                                        */
/* -------------------------------------------------------------------------- */

@Composable
private fun ReceiptMetaRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = color.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReceiptSummaryRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = color
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = color
        )
    }
}

@Composable
private fun TicketStatBox(label: String, value: String, accentColor: Color, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DashedDivider(color: Color, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
            strokeWidth = 2f
        )
    }
}

@Composable
private fun JaggedEdgeDivider(color: Color, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
    ) {
        val path = Path()
        val step = 12f
        var x = 0f
        var up = false
        path.moveTo(0f, 6f)
        while (x < size.width) {
            x += step
            val y = if (up) 6f else 0f
            path.lineTo(x, y)
            up = !up
        }
        path.lineTo(size.width, 6f)
        path.close()
    }
}

@Composable
private fun BarcodeCanvas(
    barcodeNumber: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barCount = 48
        val barWidth = width / (barCount * 1.5f)

        var currentX = 0f
        for (i in 0 until barCount) {
            // Generate deterministic line patterns from barcode string
            val charCode = barcodeNumber.getOrElse(i % barcodeNumber.length) { '7' }.code
            val isThick = (charCode + i) % 3 == 0
            val currentBarWidth = if (isThick) barWidth * 1.8f else barWidth

            drawRect(
                color = color,
                topLeft = Offset(currentX, 0f),
                size = androidx.compose.ui.geometry.Size(currentBarWidth, height)
            )
            currentX += currentBarWidth + (barWidth * 0.5f)
            if (currentX >= width) break
        }
    }
}
