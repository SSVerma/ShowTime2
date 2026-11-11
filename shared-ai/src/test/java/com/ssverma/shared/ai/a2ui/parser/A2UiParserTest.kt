package com.ssverma.shared.ai.a2ui.parser

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.ai.a2ui.model.A2UiComponent
import org.junit.Before
import org.junit.Test

class A2UiParserTest {

    private lateinit var parser: A2UiParser

    @Before
    fun setUp() {
        parser = A2UiParser()
    }

    @Test
    fun `parse returns empty document for blank text`() {
        val result = parser.parse("   ")
        assertThat(result.components).isEmpty()
    }

    @Test
    fun `parse falls back to plain text component when no json code fence exists`() {
        val rawMarkdown = "This is a great neo-noir mystery directed by Christopher Nolan."
        val result = parser.parse(rawMarkdown)

        assertThat(result.components).hasSize(1)
        val textComponent = result.components.first() as A2UiComponent.Text
        assertThat(textComponent.content).isEqualTo(rawMarkdown)
    }

    @Test
    fun `parse successfully decodes a2ui fenced json payload`() {
        val payload = """
            Here is what I found:
            ```a2ui
            {
              "id": "doc-1",
              "title": "Recommended Films",
              "components": [
                {
                  "type": "text",
                  "id": "text-1",
                  "content": "Enjoy these classics"
                },
                {
                  "type": "media_card",
                  "id": "card-1",
                  "tmdbId": 157336,
                  "title": "Interstellar",
                  "releaseYear": "2014",
                  "rating": 8.6
                }
              ]
            }
            ```
            Let me know if you want more!
        """.trimIndent()

        val result = parser.parse(payload)

        assertThat(result.id).isEqualTo("doc-1")
        assertThat(result.title).isEqualTo("Recommended Films")
        assertThat(result.components).hasSize(4)

        val preText = result.components[0] as A2UiComponent.Text
        assertThat(preText.content).isEqualTo("Here is what I found:")

        val first = result.components[1] as A2UiComponent.Text
        assertThat(first.content).isEqualTo("Enjoy these classics")

        val second = result.components[2] as A2UiComponent.MediaCard
        assertThat(second.tmdbId).isEqualTo(157336)
        assertThat(second.title).isEqualTo("Interstellar")
        assertThat(second.releaseYear).isEqualTo("2014")

        val postText = result.components[3] as A2UiComponent.Text
        assertThat(postText.content).isEqualTo("Let me know if you want more!")
    }

    @Test
    fun `parse successfully handles truncated carousel with unclosed fence and string tmdb id`() {
        val payload = """
            Ah, action! Here are top picks:
            ```a2ui
            {
              "type": "carousel",
              "title": "Latest Action Blockbusters",
              "media_carousel": [
                {
                  " tmdbId": "tt1234567",
                  "title": "Mad Max: Fury Road",
                  "release_year": "2015",
                  "rating": 8.1
                },
                {
                  " tmdbId": "tt9876543",
                  "title
        """.trimIndent()

        val result = parser.parse(payload)

        assertThat(result.components).isNotEmpty()
        val preText = result.components[0] as A2UiComponent.Text
        assertThat(preText.content).isEqualTo("Ah, action! Here are top picks:")

        val carousel = result.components[1] as A2UiComponent.MediaCarousel
        assertThat(carousel.title).isEqualTo("Latest Action Blockbusters")
        assertThat(carousel.items).hasSize(2)
        assertThat(carousel.items[0].title).isEqualTo("Mad Max: Fury Road")
        assertThat(carousel.items[0].tmdbId).isEqualTo(1234567)
        assertThat(carousel.items[1].tmdbId).isEqualTo(9876543)
    }

    @Test
    fun `parse deduplicates repeated text from model`() {
        val repeatedText =
            "That is a recommendation for classic science fiction films.That is a recommendation for classic science fiction films."
        val result = parser.parse(repeatedText)

        assertThat(result.components).hasSize(1)
        val textComponent = result.components.first() as A2UiComponent.Text
        assertThat(textComponent.content).isEqualTo("That is a recommendation for classic science fiction films.")
    }

    @Test
    fun `parse deduplicates repeated sentences with space`() {
        val repeatedText = "Here are some great films. Here are some great films."
        val result = parser.parse(repeatedText)

        assertThat(result.components).hasSize(1)
        val textComponent = result.components.first() as A2UiComponent.Text
        assertThat(textComponent.content).isEqualTo("Here are some great films.")
    }
}
