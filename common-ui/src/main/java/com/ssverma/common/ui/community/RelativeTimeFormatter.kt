package com.ssverma.common.ui.community

import android.content.Context
import com.ssverma.shared.ui.R
import java.util.concurrent.TimeUnit

object RelativeTimeFormatter {

    fun format(context: Context, epochMs: Long, nowMs: Long = System.currentTimeMillis()): String {
        return format(
            stringResolver = { resId, args ->
                if (args.isEmpty()) context.getString(resId)
                else context.getString(resId, *args)
            },
            epochMs = epochMs,
            nowMs = nowMs
        )
    }

    fun format(
        stringResolver: (resId: Int, args: Array<out Any>) -> String,
        epochMs: Long,
        nowMs: Long = System.currentTimeMillis()
    ): String {
        val diff = (nowMs - epochMs).coerceAtLeast(0L)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> stringResolver(
                R.string.time_just_now,
                emptyArray()
            )

            diff < TimeUnit.HOURS.toMillis(1) -> stringResolver(
                R.string.time_minutes_ago,
                arrayOf(minutes)
            )

            diff < TimeUnit.DAYS.toMillis(1) -> stringResolver(
                R.string.time_hours_ago,
                arrayOf(hours)
            )

            diff < TimeUnit.DAYS.toMillis(7) -> stringResolver(
                R.string.time_days_ago,
                arrayOf(days)
            )

            else -> stringResolver(R.string.time_weeks_ago, arrayOf(days / 7))
        }
    }
}
