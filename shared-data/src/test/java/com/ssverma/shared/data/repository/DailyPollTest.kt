package com.ssverma.shared.data.repository

import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.domain.model.community.DailyPollQuestion
import com.ssverma.shared.domain.model.community.DailyPollQuestionBank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyPollTest {

    private val sampleQuestions = listOf(
        DailyPollQuestion(
            id = 1,
            question = "Which Christopher Nolan Sci-Fi masterpiece is the greatest?",
            options = listOf("Inception", "Interstellar", "Oppenheimer", "Tenet")
        ),
        DailyPollQuestion(
            id = 2,
            question = "Who gave the definitive live-action Batman performance?",
            options = listOf("Christian Bale", "Robert Pattinson", "Michael Keaton", "Ben Affleck")
        ),
        DailyPollQuestion(
            id = 3,
            question = "Which cinematic sequel topped its original?",
            options = listOf("The Empire Strikes Back", "The Dark Knight", "Terminator 2")
        )
    )

    @Test
    fun dailyPoll_percentageCalculation_isAccurate() {
        val poll = DailyPoll(
            dateString = "2026-08-29",
            questionId = 1,
            question = "Best Sci-Fi?",
            options = listOf("Inception", "Interstellar", "Tenet"),
            voteCounts = listOf(50, 30, 20),
            totalVotes = 100,
            selectedOptionIndex = 0,
            isEnabled = true
        )

        assertTrue(poll.hasVoted)
        assertEquals(50f, poll.getPercentage(0), 0.01f)
        assertEquals(30f, poll.getPercentage(1), 0.01f)
        assertEquals(20f, poll.getPercentage(2), 0.01f)
        assertEquals(0f, poll.getPercentage(99), 0.01f)
    }

    @Test
    fun dailyPoll_zeroVotes_returnsZeroPercentage() {
        val emptyPoll = DailyPoll.empty(LocalDate.of(2026, 8, 29))

        assertFalse(emptyPoll.hasVoted)
        assertEquals(0, emptyPoll.totalVotes)
        assertEquals(0f, emptyPoll.getPercentage(0), 0.01f)
    }

    @Test
    fun questionBank_deterministicRotation_returnsConsistentQuestion() {
        val date = LocalDate.of(2026, 8, 29)
        val question1 = DailyPollQuestionBank.resolveQuestionForDate(date, sampleQuestions)
        val question2 = DailyPollQuestionBank.resolveQuestionForDate(date, sampleQuestions)

        assertNotNull(question1)
        assertEquals(question1?.id, question2?.id)
        assertEquals(question1?.question, question2?.question)
    }

    @Test
    fun questionBank_emptyQuestions_returnsNull() {
        val date = LocalDate.of(2026, 8, 29)
        val question = DailyPollQuestionBank.resolveQuestionForDate(date, emptyList())
        assertNull(question)
    }

    @Test
    fun questionBank_scheduledDate_overridesGeneralPool() {
        val customQuestions = listOf(
            DailyPollQuestion(id = 1, question = "General Q1", options = listOf("A", "B")),
            DailyPollQuestion(id = 2, question = "General Q2", options = listOf("A", "B")),
            DailyPollQuestion(
                id = 99,
                question = "Oscar Night Special",
                options = listOf("Film 1", "Film 2"),
                scheduledDate = "2026-03-15"
            )
        )

        val oscarDate = LocalDate.of(2026, 3, 15)
        val resolved = DailyPollQuestionBank.resolveQuestionForDate(oscarDate, customQuestions)

        assertNotNull(resolved)
        assertEquals(99, resolved?.id)
        assertEquals("Oscar Night Special", resolved?.question)
    }
}
