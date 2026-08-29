package com.ssverma.shared.domain.utils

import java.util.regex.Pattern

sealed interface ContentSafetyResult {
    object Allowed : ContentSafetyResult
    data class Blocked(val reason: String) : ContentSafetyResult
}

object ContentSafetyFilter {

    // Common profanity and abusive terms pattern with word boundaries to prevent false positives
    private val profanityPatterns = listOf(
        "\\b(fuck|fucking|fucker|fucked)\\b",
        "\\b(shit|shitty|bullshit)\\b",
        "\\b(bitch|bitches|asshole|bastard|dickhead|cunt)\\b",
        "\\b(slut|whore|nigger|nigga|faggot|fag|retard)\\b",
        "\\b(idiot|moron|loser|stfu|kill yourself)\\b"
    ).map { Pattern.compile(it, Pattern.CASE_INSENSITIVE) }

    // Repeated character spam pattern (e.g. "aaaaaaa", "!!!!!!!")
    private val spamPattern = Pattern.compile("(.)\\1{7,}")

    fun validateContent(text: String): ContentSafetyResult {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return ContentSafetyResult.Blocked("Comment cannot be empty.")
        }

        if (trimmed.length > 500) {
            return ContentSafetyResult.Blocked("Comment exceeds the 500 character limit.")
        }

        // Check for character spam
        if (spamPattern.matcher(trimmed).find()) {
            return ContentSafetyResult.Blocked("Please avoid repetitive character spam.")
        }

        // Check for profanity / abusive words
        for (pattern in profanityPatterns) {
            if (pattern.matcher(trimmed).find()) {
                return ContentSafetyResult.Blocked("Please keep discussions respectful and free of offensive language.")
            }
        }

        return ContentSafetyResult.Allowed
    }
}
