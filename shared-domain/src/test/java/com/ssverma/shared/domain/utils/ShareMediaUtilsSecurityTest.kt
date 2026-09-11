package com.ssverma.shared.domain.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShareMediaUtilsSecurityTest {

    @Test
    fun `generateSecretShareCode creates high-entropy Base32 code with SL- prefix`() {
        val code = ShareMediaUtils.generateSecretShareCode()
        assertTrue(code.startsWith("SL-"))
        assertEquals(9, code.length) // "SL-" (3) + 6 characters

        val suffix = code.removePrefix("SL-")
        assertEquals(ShareMediaUtils.SecretShareCodeLength, suffix.length)
        suffix.forEach { ch ->
            assertTrue(
                "Character $ch must belong to unconfusable alphabet",
                ch in ShareMediaUtils.SecretShareCodeAlphabet
            )
        }
    }

    @Test
    fun `generateSecretShareCode produces unique codes without collisions`() {
        val count = 2000
        val generated = (1..count).map { ShareMediaUtils.generateSecretShareCode() }.toSet()
        assertEquals("All $count generated codes must be strictly unique", count, generated.size)
    }

    @Test
    fun `normalizeSecretShareCode correctly handles high-entropy codes and legacy codes`() {
        assertEquals("SL-7K9B2X", ShareMediaUtils.normalizeSecretShareCode("7K9B2X"))
        assertEquals("SL-7K9B2X", ShareMediaUtils.normalizeSecretShareCode("sl-7k9b2x"))
        assertEquals("SL-7K9B2X", ShareMediaUtils.normalizeSecretShareCode("SL-7K9B2X"))
        assertEquals(
            "SL-7K9B2X",
            ShareMediaUtils.normalizeSecretShareCode("https://showtime.ssverma.in/l/SL-7K9B2X")
        )
        assertEquals(
            "SL-7K9B2X",
            ShareMediaUtils.normalizeSecretShareCode("https://showtime.ssverma.in/l/7K9B2X?source=share#top")
        )

        // Backward compatibility with legacy 4-digit codes
        assertEquals("SL-4821", ShareMediaUtils.normalizeSecretShareCode("4821"))
        assertEquals("SL-4821", ShareMediaUtils.normalizeSecretShareCode("sl-4821"))
        assertEquals("SL-4821", ShareMediaUtils.normalizeSecretShareCode("SL-4821"))
    }
}
