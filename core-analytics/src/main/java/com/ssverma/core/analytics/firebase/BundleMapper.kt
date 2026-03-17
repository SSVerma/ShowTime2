package com.ssverma.core.analytics.firebase

import android.os.Bundle
import com.ssverma.core.analytics.AnalyticsParam
import com.ssverma.core.analytics.BooleanParam
import com.ssverma.core.analytics.IntListParam
import com.ssverma.core.analytics.NestedListParam
import com.ssverma.core.analytics.NestedParam
import com.ssverma.core.analytics.NumberParam
import com.ssverma.core.analytics.StringListParam
import com.ssverma.core.analytics.StringParam

fun Map<String, AnalyticsParam>.toBundle(): Bundle {
    val bundle = Bundle()

    this.forEach { (key, param) ->
        when (param) {
            is StringParam -> bundle.putString(key, param.value)
            is BooleanParam -> bundle.putBoolean(key, param.value)
            is NumberParam -> {
                when (val num = param.value) {
                    is Int -> bundle.putInt(key, num)
                    is Long -> bundle.putLong(key, num)
                    is Double -> bundle.putDouble(key, num)
                    is Float -> bundle.putFloat(key, num)
                }
            }

            is StringListParam -> bundle.putStringArrayList(key, ArrayList(param.value))
            is IntListParam -> bundle.putIntegerArrayList(key, ArrayList(param.value))

            // Recursively build nested bundles
            is NestedParam -> bundle.putBundle(key, param.value.toBundle())

            // Map List of Objects to Array of Bundles (Firebase standard for 'items')
            is NestedListParam -> {
                val bundleArray = param.value.map { it.toBundle() }.toTypedArray()
                bundle.putParcelableArray(key, bundleArray)
            }
        }
    }

    return bundle
}
