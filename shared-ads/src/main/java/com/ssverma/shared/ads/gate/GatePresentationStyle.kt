package com.ssverma.shared.ads.gate

/**
 * Controls the visual presentation style of a [ShowTimeFeatureGate].
 *
 * - [BottomSheet]: Modal bottom sheet via [ShowTimeBottomSheet] (default for most gates).
 * - [Dialog]: Standard [AlertDialog] for compact, lightweight gates (e.g. SecretShare themes).
 */
enum class GatePresentationStyle {
    BottomSheet,
    Dialog
}
