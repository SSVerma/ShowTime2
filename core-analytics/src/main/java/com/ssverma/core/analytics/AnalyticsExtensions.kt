package com.ssverma.core.analytics

infix fun String.to(value: String): Pair<String, AnalyticsParam> = this to StringParam(value)
infix fun String.to(value: Number): Pair<String, AnalyticsParam> = this to NumberParam(value)
infix fun String.to(value: Boolean): Pair<String, AnalyticsParam> = this to BooleanParam(value)

// Solves Type Erasure for Lists
@JvmName("bindStringList")
infix fun String.to(value: List<String>) = this to StringListParam(value)

@JvmName("bindIntList")
infix fun String.to(value: List<Int>) = this to IntListParam(value)

// For Nested Objects
@JvmName("bindNestedMap")
infix fun String.to(value: Map<String, AnalyticsParam>) = this to NestedParam(value)

@JvmName("bindNestedList")
infix fun String.to(value: List<Map<String, AnalyticsParam>>) = this to NestedListParam(value)
