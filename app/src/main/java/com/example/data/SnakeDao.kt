package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches ORDER BY score DESC LIMIT :limit")
    fun getTopMatches(limit: Int = 10): Flow<List<MatchEntity>>

    @Query("SELECT MAX(score) FROM matches")
    fun getHighScore(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity): Long

    @Query("DELETE FROM matches")
    suspend fun clearHistory()
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET selectedThemeId = :themeId WHERE id = 1")
    suspend fun updateTheme(themeId: String)

    @Query("UPDATE user_profile SET wallMode = :mode WHERE id = 1")
    suspend fun updateWallMode(mode: String)

    @Query("UPDATE user_profile SET gamerTag = :tag WHERE id = 1")
    suspend fun updateGamerTag(tag: String)

    @Query("UPDATE user_profile SET avatarId = :avatarId WHERE id = 1")
    suspend fun updateAvatar(avatarId: String)

    @Query("UPDATE user_profile SET soundEnabled = :sound, vibrationEnabled = :vibe, scanlinesEnabled = :scanlines WHERE id = 1")
    suspend fun updateSettings(sound: Boolean, vibe: Boolean, scanlines: Boolean)
}
