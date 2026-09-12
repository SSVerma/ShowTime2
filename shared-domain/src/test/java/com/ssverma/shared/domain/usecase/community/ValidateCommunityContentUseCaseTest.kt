package com.ssverma.shared.domain.usecase.community

import com.google.common.truth.Truth.assertThat
import com.ssverma.shared.domain.model.community.CommunityModerationConfig
import com.ssverma.shared.domain.model.community.ContentModerationResult
import com.ssverma.shared.domain.repository.CommunityRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class ValidateCommunityContentUseCaseTest {

    private lateinit var communityRepository: CommunityRepository
    private lateinit var useCase: ValidateCommunityContentUseCase

    @Before
    fun setUp() {
        communityRepository = mockk(relaxed = true)
        every { communityRepository.getCommunitySevereBlockedRegex() } returns
                CommunityModerationConfig.DEFAULT_SEVERE_BLOCKED_REGEX
        every { communityRepository.getCommunitySensitiveConfirmRegex() } returns
                CommunityModerationConfig.DEFAULT_SENSITIVE_CONFIRM_REGEX

        useCase = ValidateCommunityContentUseCase(communityRepository = communityRepository)
    }

    @Test
    fun `benign cinema titles with substring triggers do not cause false positives`() {
        val benignTitles = listOf(
            "Classic 90s Thrillers",
            "The Assassin Anthology",
            "Cocktail Hour: Best Bar Scenes",
            "Moby Dick Adaptations",
            "Dick Cavett Interviews",
            "Scunthorpe History in Film",
            "Documentaries on Cockatoos",
            "Classics of World Cinema"
        )

        for (title in benignTitles) {
            val result =
                useCase(title = title, description = "A curated collection of great movies")
            assertThat(result).isEqualTo(ContentModerationResult.Approved)
        }
    }

    @Test
    fun `mature cinema themes trigger sensitive warning allowing confirmation`() {
        val sensitiveThemes = listOf(
            "Cinema with artistic nudity scenes" to "nudity",
            "Erotic Thrillers of the 80s" to "erotic",
            "Sex, Lies, and Videotape and Other Indie Hits" to "sex"
        )

        for ((title, expectedTerm) in sensitiveThemes) {
            val result = useCase(title = title, description = "Essential films")
            assertThat(result).isInstanceOf(ContentModerationResult.SensitiveWarning::class.java)
            val warning = result as ContentModerationResult.SensitiveWarning
            assertThat(warning.flaggedTerm.lowercase()).isEqualTo(expectedTerm.lowercase())
        }
    }

    @Test
    fun `severe prohibited terms are strictly hard blocked`() {
        val prohibitedPhrases = listOf(
            "illegal cp collection",
            "child porn vault",
            "underage exploitation list"
        )

        for (phrase in prohibitedPhrases) {
            val result = useCase(title = phrase, description = "Forbidden content")
            assertThat(result).isInstanceOf(ContentModerationResult.Prohibited::class.java)
        }
    }

    @Test
    fun `empty and blank text returns approved`() {
        assertThat(
            useCase(
                title = "",
                description = null
            )
        ).isEqualTo(ContentModerationResult.Approved)
        assertThat(
            useCase(
                title = "   ",
                description = "   "
            )
        ).isEqualTo(ContentModerationResult.Approved)
    }

    @Test
    fun `custom regex from repository overrides default rules dynamically`() {
        every { communityRepository.getCommunitySevereBlockedRegex() } returns
                """\b(?i)(custom_banned_word)\b"""

        val normalResult = useCase(title = "Classic Film", description = null)
        assertThat(normalResult).isEqualTo(ContentModerationResult.Approved)

        val bannedResult =
            useCase(title = "This is a custom_banned_word movie list", description = null)
        assertThat(bannedResult).isInstanceOf(ContentModerationResult.Prohibited::class.java)
    }
}
