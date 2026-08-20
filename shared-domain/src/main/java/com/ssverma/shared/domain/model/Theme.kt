package com.ssverma.shared.domain.model

enum class AppTheme {
    System,
    Light,
    Dark,
    OledMidnight;

    companion object {
        fun fromName(name: String?): AppTheme {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: System
        }
    }
}
