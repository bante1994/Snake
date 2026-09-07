package com.example.util

import java.security.MessageDigest

data class AntiCheatReport(
    val isClean: Boolean,
    val token: String,
    val integrityRating: String,
    val flags: List<String>,
    val movesPerApple: Float
)

object AntiCheatEngine {
    private const val SECRET_SALT = "RETRO_SNAKE_INTEGRITY_SALT_2026"

    private fun checkFlags(
        score: Int,
        applesEaten: Int,
        movesCount: Int,
        durationSeconds: Int
    ): Pair<List<String>, Float> {
        val flags = mutableListOf<String>()

        // 1. Min moves check: At least 1.8 moves per apple on average after warm-up
        val movesPerApple = if (applesEaten > 0) movesCount.toFloat() / applesEaten else 0f
        if (applesEaten > 3 && movesPerApple < 1.8f) {
            flags.add("Impossible move efficiency ($movesPerApple moves/apple)")
        }

        // 2. Duration check: Player cannot eat 10 apples in 2 seconds
        if (durationSeconds > 0) {
            val applesPerSecond = applesEaten.toFloat() / durationSeconds
            if (applesPerSecond > 4.0f) {
                flags.add("Abnormal speed rate ($applesPerSecond apples/sec)")
            }
        }

        // 3. Score sanity check: Score must correlate with apples eaten
        val minExpectedScore = applesEaten * 10
        if (score < minExpectedScore && applesEaten > 0) {
            flags.add("Score mismatch with apples consumed")
        }

        return Pair(flags, movesPerApple)
    }

    private fun computeHash(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun generateVerificationToken(
        score: Int,
        applesEaten: Int,
        goldenApplesEaten: Int,
        movesCount: Int,
        durationSeconds: Int,
        seed: Long
    ): String {
        val raw = "$score|$applesEaten|$goldenApplesEaten|$movesCount|$durationSeconds|$seed|$SECRET_SALT"
        val hex = computeHash(raw)
        val (flags, _) = checkFlags(score, applesEaten, movesCount, durationSeconds)
        val prefix = if (flags.isEmpty()) "VERIFIED" else "FLAGGED"
        return "ACS-${prefix}-${hex.take(10).uppercase()}"
    }

    fun isScoreLegitimate(
        score: Int,
        applesEaten: Int,
        movesCount: Int,
        durationSeconds: Int,
        seed: Long = 42L
    ): AntiCheatReport {
        val (flags, movesPerApple) = checkFlags(score, applesEaten, movesCount, durationSeconds)
        val isClean = flags.isEmpty()
        val integrityRating = when {
            isClean && score >= 300 -> "GRANDMASTER_CLEAN"
            isClean -> "VERIFIED_CLEAN"
            flags.size == 1 -> "MINOR_ANOMALY"
            else -> "FLAGGED_CHEATING"
        }

        val raw = "$score|$applesEaten|0|$movesCount|$durationSeconds|$seed|$SECRET_SALT"
        val hex = computeHash(raw)
        val prefix = if (isClean) "VERIFIED" else "FLAGGED"
        val token = "ACS-${prefix}-${hex.take(10).uppercase()}"

        return AntiCheatReport(
            isClean = isClean,
            token = token,
            integrityRating = integrityRating,
            flags = flags,
            movesPerApple = movesPerApple
        )
    }

    /**
     * Generates a sharable tournament seed code, e.g., "#RNK-8821"
     */
    fun formatTournamentSeed(seed: Long): String {
        val code = (seed % 9000 + 1000).toString()
        return "#RNK-$code"
    }

    /**
     * Parses a tournament code into a reproducible Long seed
     */
    fun parseTournamentCode(code: String): Long {
        val clean = code.replace("#", "").replace("RNK-", "").trim()
        val num = clean.toLongOrNull() ?: 424242L
        return num * 1337L
    }
}
