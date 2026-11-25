package com.ssverma.core.navigation.nav3

import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShowTimeListDetailSceneStrategyTest {

    private data object HomeRootKey : NavKey
    private data object ListPrimaryKey : NavKey
    private data object ListSecondaryKey : NavKey
    private data object SearchListKey : NavKey
    private data class SampleDetailKey(val itemId: Int) : AdaptiveDetailNavKey
    private data class SampleExtraKey(val itemId: Int) : AdaptiveExtraNavKey

    private fun entry(
        key: NavKey,
        metadata: Map<String, Any> = emptyMap()
    ): NavEntry<NavKey> = NavEntry(
        key = key,
        metadata = metadata + mapOf(Nav3MetadataKeys.NavKey.toString() to key),
        content = {}
    )

    @Test
    fun emptyEntries_returnsFalse() {
        assertThat(shouldActivateListDetailScene(emptyList())).isFalse()
    }

    @Test
    fun singleListEntry_returnsFalse() {
        val entries = listOf(
            entry(key = ListPrimaryKey, metadata = adaptiveListPaneMetadata())
        )

        assertThat(shouldActivateListDetailScene(entries)).isFalse()
    }

    @Test
    fun stackedListEntries_withoutDetailOrAlwaysShowSplit_returnsFalse() {
        val entries = listOf(
            entry(key = ListPrimaryKey, metadata = adaptiveListPaneMetadata()),
            entry(key = ListSecondaryKey, metadata = adaptiveListPaneMetadata())
        )

        assertThat(shouldActivateListDetailScene(entries)).isFalse()
    }

    @Test
    fun homeFollowedByStackedListEntries_returnsFalse() {
        val entries = listOf(
            entry(key = HomeRootKey),
            entry(key = ListPrimaryKey, metadata = adaptiveListPaneMetadata()),
            entry(key = ListSecondaryKey, metadata = adaptiveListPaneMetadata())
        )

        assertThat(shouldActivateListDetailScene(entries)).isFalse()
    }

    @Test
    fun alwaysShowSplitListEntry_returnsTrueEvenWhenAlone() {
        val entries = listOf(
            entry(
                key = SearchListKey,
                metadata = adaptiveListPaneMetadata(alwaysShowSplit = true)
            )
        )

        assertThat(shouldActivateListDetailScene(entries)).isTrue()
    }

    @Test
    fun listEntryFollowedByDetailEntry_returnsTrue() {
        val entries = listOf(
            entry(key = ListPrimaryKey, metadata = adaptiveListPaneMetadata()),
            entry(key = SampleDetailKey(itemId = 42), metadata = adaptiveDetailPaneMetadata())
        )

        assertThat(shouldActivateListDetailScene(entries)).isTrue()
    }

    @Test
    fun homeFollowedByDetailEntry_returnsFalse() {
        val entries = listOf(
            entry(key = HomeRootKey),
            entry(key = SampleDetailKey(itemId = 42), metadata = adaptiveDetailPaneMetadata())
        )

        assertThat(shouldActivateListDetailScene(entries)).isFalse()
    }

    @Test
    fun homeFollowedByDetailAndExtraEntry_returnsTrue() {
        val entries = listOf(
            entry(key = HomeRootKey),
            entry(key = SampleDetailKey(itemId = 42), metadata = adaptiveDetailPaneMetadata()),
            entry(key = SampleExtraKey(itemId = 42), metadata = adaptiveExtraPaneMetadata())
        )

        assertThat(shouldActivateListDetailScene(entries)).isTrue()
    }
}
