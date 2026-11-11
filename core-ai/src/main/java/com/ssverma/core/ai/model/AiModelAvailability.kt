package com.ssverma.core.ai.model

/**
 * Represents the availability status of the on-device AI model (e.g. Gemini Nano).
 */
sealed interface AiModelAvailability {
    /**
     * The model is downloaded, verified, and ready for on-device inference.
     */
    data object Available : AiModelAvailability

    /**
     * The model is currently being downloaded or initialized by AICore.
     *
     * @property progressFraction Download progress normalized between 0.0f and 1.0f.
     */
    data class Downloading(val progressFraction: Float) : AiModelAvailability

    /**
     * On-device AI is unsupported on this hardware or unavailable in current system conditions.
     *
     * @property reason Human-readable diagnostic reason for unavailability.
     */
    data class Unavailable(val reason: String) : AiModelAvailability
}
