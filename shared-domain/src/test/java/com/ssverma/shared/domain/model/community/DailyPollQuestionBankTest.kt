package com.ssverma.shared.domain.model.community

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DailyPollQuestionBankTest {

    @Test
    fun `resolveQuestionForDate returns null on empty questions list`() {
        val result = DailyPollQuestionBank.resolveQuestionForDate(
            date = LocalDate.of(2026, 9, 20),
            questions = emptyList()
        )
        assertNull(result)
    }

    @Test
    fun `resolveQuestionForDate filters out questions with empty or blank questions and options`() {
        val corruptedQuestions = listOf(
            DailyPollQuestion(id = 1, question = "", options = listOf("A", "B")),
            DailyPollQuestion(id = 2, question = "Valid Question", options = emptyList()),
            DailyPollQuestion(id = 3, question = "   ", options = listOf("A")),
            DailyPollQuestion(id = 4, question = "Healthy Question", options = listOf("Yes", "No"))
        )

        val result = DailyPollQuestionBank.resolveQuestionForDate(
            date = LocalDate.of(2026, 9, 20),
            questions = corruptedQuestions
        )

        assertNotNull(result)
        assertEquals(4, result?.id)
        assertEquals("Healthy Question", result?.question)
        assertEquals(listOf("Yes", "No"), result?.options)
    }

    @Test
    fun `resolveQuestionForDate prioritizes scheduledDate matching current date`() {
        val today = LocalDate.of(2026, 9, 20)
        val questions = listOf(
            DailyPollQuestion(id = 1, question = "General Q1", options = listOf("A", "B")),
            DailyPollQuestion(
                id = 2,
                question = "Oscar Special",
                options = listOf("Film A", "Film B"),
                scheduledDate = "2026-09-20"
            ),
            DailyPollQuestion(
                id = 3,
                question = "Future Special",
                options = listOf("X", "Y"),
                scheduledDate = "2026-12-31"
            )
        )

        val result = DailyPollQuestionBank.resolveQuestionForDate(
            date = today,
            questions = questions
        )

        assertNotNull(result)
        assertEquals(2, result?.id)
        assertEquals("Oscar Special", result?.question)
    }

    @Test
    fun `resolveQuestionForDate is deterministic for same date`() {
        val date = LocalDate.of(2026, 9, 20)
        val questions = (1..10).map {
            DailyPollQuestion(
                id = it,
                question = "Question $it",
                options = listOf("Opt 1", "Opt 2")
            )
        }

        val result1 = DailyPollQuestionBank.resolveQuestionForDate(date, questions)
        val result2 = DailyPollQuestionBank.resolveQuestionForDate(date, questions)

        assertEquals(result1?.id, result2?.id)
    }
}
