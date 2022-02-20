package com.ssverma.core.networking

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.networking.utils.mergeOrderedWith
import org.junit.Test

class MergeUtilsTest {

    @Test
    fun `verify mergeWith when source and with are empty then produces new empty list`() {
        val source = emptyList<Int>()
        val with = emptyList<Int>()

        val result = source.mergeOrderedWith(with) { oldValue, newValue -> oldValue == newValue }

        assertThat(result).isEmpty()
        assertThat(result).isNotSameInstanceAs(source)
        assertThat(result).isNotSameInstanceAs(with)
    }

    @Test
    fun `verify mergeWith when source is empty and with is not empty then produces updated list`() {
        val source = emptyList<Int>()
        val with = listOf(1, 3, 2)

        val result = source.mergeOrderedWith(with) { oldValue, newValue -> oldValue == newValue }

        assertThat(result).isNotEmpty()
        assertThat(result).containsExactly(1, 3, 2).inOrder()
    }

    @Test
    fun `verify mergeWith when source is not empty and with is empty then produces new list identical with source`() {
        val source = listOf(1, 3, 2)
        val with = emptyList<Int>()

        val result = source.mergeOrderedWith(with) { oldValue, newValue -> oldValue == newValue }

        assertThat(result).isNotEmpty()
        assertThat(result).containsExactly(1, 3, 2).inOrder()
        assertThat(result).isNotSameInstanceAs(source)
        assertThat(result).isNotSameInstanceAs(with)
    }

    @Test
    fun `verify mergeWith removes identical values`() {
        val source = emptyList<Int>()
        val with = listOf(1, 3, 2, 3, 5)

        val result = source.mergeOrderedWith(with) { oldValue, newValue -> oldValue == newValue }

        assertThat(result).isNotEmpty()
        assertThat(result).containsExactly(1, 3, 2, 5).inOrder()
    }

    @Test
    fun `verify mergeWith removes same type values and keeps only most last instances`() {
        val source = emptyList<Int>()

        val nameOne = Any()
        val nameTwo = Any()
        val nameThree = "name3"

        val with = listOf(nameOne, nameTwo, nameThree)

        val result = source.mergeOrderedWith(with) { oldValue, newValue ->
            oldValue == newValue || oldValue::class.java == newValue::class.java
        }

        assertThat(result).isNotEmpty()
        assertThat(result).containsExactly(nameTwo, nameThree)
    }

    @Test
    fun `verify mergeWith merges values and keeps identical only once`() {
        val source = listOf(4, 1)
        val with = listOf(1, 3, 2)

        val result = source.mergeOrderedWith(with) { oldValue, newValue -> oldValue == newValue }

        assertThat(result).isNotEmpty()
        assertThat(result).containsExactly(4, 1, 3, 2)
    }

    @Test
    fun `verify mergeWith merges values and append new values at start`() {
        val source = listOf(4, 1)
        val with = listOf(1, 3, 2)

        val result = source.mergeOrderedWith(with) { oldValue, newValue -> oldValue == newValue }

        assertThat(result).isNotEmpty()
        assertThat(result).containsExactly(3, 2, 4, 1).inOrder()
    }

}