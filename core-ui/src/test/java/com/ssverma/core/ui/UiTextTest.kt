package com.ssverma.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UiTextTest {

    @Test
    fun dynamicText_holdsCorrectValue() {
        val text = UiText.DynamicText("Hello ShowTime")
        assertEquals("Hello ShowTime", text.text)
    }

    @Test
    fun dynamicText_equalityAndHashCode() {
        val text1 = UiText.DynamicText("ShowTime")
        val text2 = UiText.DynamicText("ShowTime")
        val text3 = UiText.DynamicText("Different")

        assertEquals(text1, text2)
        assertEquals(text1.hashCode(), text2.hashCode())
        assertNotEquals(text1, text3)
    }

    @Test
    fun staticText_holdsCorrectResIdAndArgs() {
        val text = UiText.StaticText(100, "arg1", 42)
        assertEquals(100, text.resId)
        assertTrue(arrayOf<Any>("arg1", 42).contentEquals(text.formatArgs))
    }

    @Test
    fun staticText_equalityAndHashCode() {
        val text1 = UiText.StaticText(100, "arg1", 42)
        val text2 = UiText.StaticText(100, "arg1", 42)
        val text3 = UiText.StaticText(100, "arg2", 42)
        val text4 = UiText.StaticText(200, "arg1", 42)

        assertEquals(text1, text2)
        assertEquals(text1.hashCode(), text2.hashCode())
        assertNotEquals(text1, text3)
        assertNotEquals(text1, text4)
    }

    @Test
    fun pluralText_holdsCorrectResIdCountAndArgs() {
        val text = UiText.PluralText(300, 5, "arg1")
        assertEquals(300, text.resId)
        assertEquals(5, text.count)
        assertTrue(arrayOf<Any>("arg1").contentEquals(text.formatArgs))
    }

    @Test
    fun pluralText_equalityAndHashCode() {
        val text1 = UiText.PluralText(300, 5, "arg1")
        val text2 = UiText.PluralText(300, 5, "arg1")
        val text3 = UiText.PluralText(300, 2, "arg1")
        val text4 = UiText.PluralText(400, 5, "arg1")

        assertEquals(text1, text2)
        assertEquals(text1.hashCode(), text2.hashCode())
        assertNotEquals(text1, text3)
        assertNotEquals(text1, text4)
    }

    @Test
    fun uiTextEmpty_isDynamicTextWithEmptyString() {
        assertEquals(UiText.DynamicText(""), UiText.Empty)
    }

    @Test
    fun extensionHelpers_createExpectedInstances() {
        val staticFromInt = 123.asUiText("format")
        val dynamicFromString = "dynamic".asUiText()

        assertTrue(staticFromInt is UiText.StaticText)
        assertEquals(123, (staticFromInt as UiText.StaticText).resId)

        assertTrue(dynamicFromString is UiText.DynamicText)
        assertEquals("dynamic", (dynamicFromString as UiText.DynamicText).text)
    }
}
