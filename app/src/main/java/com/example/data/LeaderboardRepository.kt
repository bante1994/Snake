package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

class LeaderboardRepository(
    private val matchDao: MatchDao,
    private val profileDao: ProfileDao
) {
    private val _communityScores = MutableStateFlow<List<LeaderboardEntry>>(defaultLeaderboard)
    val communityScores: Flow<List<LeaderboardEntry>> = _communityScores.asStateFlow()

    private val _activeTournaments = MutableStateFlow<List<TournamentChallenge>>(defaultTournaments)
    val activeTournaments: Flow<List<TournamentChallenge>> = _activeTournaments.asStateFlow()

    private val _voiceState = MutableStateFlow(SquadVoiceState())
    val voiceState: Flow<SquadVoiceState> = _voiceState.asStateFlow()

    fun toggleMic() {
        _voiceState.update { current ->
            current.copy(isMicMuted = !current.isMicMuted)
        }
    }

    fun switchChannel(newChannel: String) {
        _voiceState.update { current ->
            current.copy(channelName = newChannel)
        }
    }

    fun submitUserScore(
        playerTag: String,
        score: Int,
        apples: Int,
        speedTier: String,
        token: String,
        isVerified: Boolean
    ) {
        val newEntry = LeaderboardEntry(
            rank = 0,
            playerTag = playerTag,
            score = score,
            apples = apples,
            speedTier = speedTier,
            region = "ALL",
            avatarId = "snake_vip",
            isVerified = isVerified,
            antiCheatToken = token,
            isCurrentUser = true,
            tierRank = when {
                score >= 400 -> "GRANDMASTER"
                score >= 250 -> "MASTER"
                score >= 150 -> "DIAMOND"
                score >= 80 -> "PLATINUM"
                else -> "GOLD"
            }
        )

        _communityScores.update { currentList ->
            val updated = (currentList.filterNot { it.isCurrentUser } + newEntry)
                .sortedByDescending { it.score }
                .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
            updated
        }
    }

    fun addTournament(challenge: TournamentChallenge) {
        _activeTournaments.update { list ->
            listOf(challenge) + list
        }
    }

    companion object {
        val defaultLeaderboard = listOf(
            LeaderboardEntry(1, "KRONOS_VIPER", 580, 52, "TURBO_X", "ALL", "snake_crown", true, "ACS-VERIFIED-7FA9C102", false, "GRANDMASTER"),
            LeaderboardEntry(2, "RETRO_GLITCH", 520, 48, "HYPER_SPEED", "NA", "snake_cyber", true, "ACS-VERIFIED-44BC91D3", false, "GRANDMASTER"),
            LeaderboardEntry(3, "CYBER_NEXUS", 470, 44, "HYPER_SPEED", "EU", "snake_amber", true, "ACS-VERIFIED-89DF02E1", false, "GRANDMASTER"),
            LeaderboardEntry(4, "PIXEL_SAMURAI", 390, 37, "SPEED_LVL_5", "APAC", "snake_matrix", true, "ACS-VERIFIED-12EE77A0", false, "MASTER"),
            LeaderboardEntry(5, "SHADOW_COIL", 340, 32, "SPEED_LVL_4", "ALL", "snake_classic", true, "ACS-VERIFIED-991A54F8", false, "MASTER"),
            LeaderboardEntry(6, "NEON_VIPER", 290, 28, "SPEED_LVL_4", "NA", "snake_neon", true, "ACS-VERIFIED-38DBC982", false, "DIAMOND"),
            LeaderboardEntry(7, "8BIT_CHAMP", 240, 23, "SPEED_LVL_3", "EU", "snake_gameboy", true, "ACS-VERIFIED-66EA29B4", false, "DIAMOND"),
            LeaderboardEntry(8, "TURBO_TAIL", 210, 20, "SPEED_LVL_3", "APAC", "snake_classic", true, "ACS-VERIFIED-41CBA771", false, "PLATINUM"),
            LeaderboardEntry(9, "ARCADE_WIZARD", 170, 16, "SPEED_LVL_2", "NA", "snake_amber", true, "ACS-VERIFIED-178A2BC9", false, "PLATINUM"),
            LeaderboardEntry(10, "BYTE_HUNTER", 140, 14, "SPEED_LVL_2", "ALL", "snake_synth", true, "ACS-VERIFIED-09AA55EF", false, "GOLD")
        )

        val defaultTournaments = listOf(
            TournamentChallenge(
                code = "#RNK-8821",
                title = "Cyber Speedrun Cup",
                targetScore = 350,
                creatorTag = "KRONOS_VIPER",
                seed = 88218821L,
                participants = 1420,
                rewardTitle = "Viper Overlord"
            ),
            TournamentChallenge(
                code = "#RNK-4040",
                title = "Retro Pixel Showdown",
                targetScore = 200,
                creatorTag = "RETRO_GLITCH",
                seed = 40404040L,
                participants = 890,
                rewardTitle = "8-Bit Champion"
            ),
            TournamentChallenge(
                code = "#RNK-1337",
                title = "Elite No-Wall Marathon",
                targetScore = 450,
                creatorTag = "CYBER_NEXUS",
                seed = 13371337L,
                participants = 620,
                rewardTitle = "Matrix Legend"
            )
        )
    }
}
