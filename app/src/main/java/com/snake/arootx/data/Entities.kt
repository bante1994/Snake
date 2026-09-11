package com.snake.arootx.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val applesEaten: Int,
    val goldenApplesEaten: Int,
    val speedLevelReached: Int,
    val durationSeconds: Int,
    val deathReason: String,
    val themeId: String,
    val wallMode: String = "WALL",
    val timestamp: Long = System.currentTimeMillis(),
    val antiCheatVerified: Boolean = true,
    val antiCheatToken: String
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val gamerTag: String = "PIXEL_VIPER",
    val avatarId: String = "snake_classic",
    val selectedThemeId: String = "cyberpunk",
    val wallMode: String = "WALL",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val scanlinesEnabled: Boolean = true,
    val highScore: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalApplesEaten: Int = 0,
    val totalPlaytimeSeconds: Long = 0L,
    val streakDays: Int = 1,
    val lastPlayedDay: Long = 0L,
    val integrityPercent: Int = 100,
    val adMobAppId: String = com.snake.arootx.config.AdMobConfig.ADMOB_APP_ID,
    val adMobRewardedUnitId: String = com.snake.arootx.config.AdMobConfig.ADMOB_REWARDED_AD_UNIT_ID
)
