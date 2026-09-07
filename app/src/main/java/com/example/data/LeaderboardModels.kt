package com.example.data

data class LeaderboardEntry(
    val rank: Int,
    val playerTag: String,
    val score: Int,
    val apples: Int,
    val speedTier: String,
    val region: String,
    val avatarId: String,
    val isVerified: Boolean,
    val antiCheatToken: String,
    val isCurrentUser: Boolean = false,
    val tierRank: String = "DIAMOND"
)

enum class LeaderboardRegion(val code: String, val label: String) {
    GLOBAL("ALL", "Global"),
    AMERICAS("NA", "Americas"),
    EUROPE("EU", "Europe"),
    ASIA_PACIFIC("APAC", "Asia-Pacific")
}

data class TournamentChallenge(
    val code: String,
    val title: String,
    val targetScore: Int,
    val creatorTag: String,
    val seed: Long,
    val participants: Int,
    val rewardTitle: String
)

data class SquadVoiceState(
    val channelName: String = "ALPHA-1 (Global Squad)",
    val isMicMuted: Boolean = false,
    val isConnected: Boolean = true,
    val activeSpeaker: String? = null,
    val volumeLevels: List<Float> = listOf(0.2f, 0.5f, 0.8f, 0.4f, 0.7f, 0.3f),
    val onlineSquadMembers: List<SquadMember> = listOf(
        SquadMember("PIXEL_VIPER (You)", isMuted = false, isTalking = false, pingMs = 24),
        SquadMember("NEO_GRID_99", isMuted = false, isTalking = true, pingMs = 38),
        SquadMember("8BIT_GHOST", isMuted = true, isTalking = false, pingMs = 45),
        SquadMember("CYBER_SYNTH", isMuted = false, isTalking = false, pingMs = 52)
    )
)

data class SquadMember(
    val tag: String,
    val isMuted: Boolean,
    val isTalking: Boolean,
    val pingMs: Int
)

data class DailyRetentionChallenge(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val rewardBadge: String,
    val isCompleted: Boolean
)
