package com.ssverma.shared.domain.model.community

import java.time.LocalDate
import kotlin.math.abs

data class DailyPollQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val scheduledDate: String? = null
)

object DailyPollQuestionBank {

    fun resolveQuestionForDate(
        date: LocalDate,
        questions: List<DailyPollQuestion>
    ): DailyPollQuestion? {
        if (questions.isEmpty()) return null
        val dateString = date.toString()

        // 1. Check for explicit scheduled date override
        val scheduledQuestion = questions.find { it.scheduledDate == dateString }
        if (scheduledQuestion != null) {
            return scheduledQuestion
        }

        // 2. Filter questions eligible for general rotation (those without future scheduled dates)
        val rotatingQuestions =
            questions.filter { it.scheduledDate == null || it.scheduledDate == dateString }
        val pool = if (rotatingQuestions.isNotEmpty()) rotatingQuestions else questions

        // 3. Deterministic date hash calculation
        val epochDay = date.toEpochDay()
        val index = abs(epochDay.hashCode()) % pool.size
        return pool[index]
    }
}
