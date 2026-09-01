package com.ssverma.core.backup.model

enum class BackupFrequency(val intervalDays: Long) {
    OFF(intervalDays = 0),
    DAILY(intervalDays = 1),
    WEEKLY(intervalDays = 7);

    val isAutomated: Boolean
        get() = this != OFF

    companion object {
        fun fromName(name: String?): BackupFrequency {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OFF
        }
    }
}
