package com.ssverma.core.analytics

sealed interface AnalyticsParam

@JvmInline
value class StringParam(val value: String) : AnalyticsParam

@JvmInline
value class NumberParam(val value: Number) : AnalyticsParam

@JvmInline
value class BooleanParam(val value: Boolean) : AnalyticsParam

// For Lists
@JvmInline
value class StringListParam(val value: List<String>) : AnalyticsParam

@JvmInline
value class IntListParam(val value: List<Int>) : AnalyticsParam

// For Nested Objects (Maps to a nested Bundle in Android)
class NestedParam(val value: Map<String, AnalyticsParam>) : AnalyticsParam

// For List of Nested Objects (e.g., Firebase movie 'items' array)
class NestedListParam(val value: List<Map<String, AnalyticsParam>>) : AnalyticsParam
