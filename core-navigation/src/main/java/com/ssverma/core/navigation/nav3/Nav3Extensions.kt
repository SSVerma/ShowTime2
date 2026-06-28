package com.ssverma.core.navigation.nav3

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.get

@Composable
inline fun <reified T : NavKey> rememberEntry(
): androidx.navigation3.runtime.NavEntry<NavKey> {
    val entries = LocalNavEntries.current
    return entries.lastOrNull {
        val key = it.metadata[Nav3MetadataKeys.NavKey]
        key is T
    } ?: error("No entry found for NavKey ${T::class.simpleName}")
}

inline fun <reified K : NavKey> EntryProviderScope<NavKey>.showTimeEntry(
    noinline metadata: (K) -> Map<String, Any> = { emptyMap() },
    noinline content: @Composable (K) -> Unit
) {
    entry<K>(
        metadata = { key ->
            val map = metadata(key).toMutableMap()
            map[Nav3MetadataKeys.NavKey.toString()] = key
            map
        },
        content = content
    )
}
