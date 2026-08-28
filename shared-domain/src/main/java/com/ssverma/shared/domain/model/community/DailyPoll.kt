package com.ssverma.shared.domain.model.community

import java.time.LocalDate

data class DailyPoll(
    val dateString: String,
    val questionId: Int,
    val question: String,
    val options: List<String>,
    val voteCounts: List<Int>,
    val totalVotes: Int,
    val selectedOptionIndex: Int?,
    val isEnabled: Boolean = true
) {
    val hasVoted: Boolean
        get() = selectedOptionIndex != null

    fun getPercentage(optionIndex: Int): Float {
        if (totalVotes <= 0 || optionIndex !in voteCounts.indices) return 0f
        val count = voteCounts[optionIndex]
        return (count.toFloat() / totalVotes) * 100f
    }

    fun getVoteCount(optionIndex: Int): Int {
        if (optionIndex !in voteCounts.indices) return 0
        return voteCounts[optionIndex]
    }

    companion object {
        fun empty(date: LocalDate): DailyPoll {
            val dateStr = date.toString()
            return DailyPoll(
                dateString = dateStr,
                questionId = 0,
                question = "",
                options = emptyList(),
                voteCounts = emptyList(),
                totalVotes = 0,
                selectedOptionIndex = null,
                isEnabled = true
            )
        }
    }
}
